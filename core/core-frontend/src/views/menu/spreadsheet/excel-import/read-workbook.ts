import ExcelJS from 'exceljs'
import { Tools } from '@univerjs/core'
import type { IRange } from '@univerjs/core'
import { createStyleConverter } from './format'
import { IMPORT_LIMITS } from './protocol'
import type { ImportSheetInfo } from './protocol'

export class ImportError extends Error {}

export function isCached(value: ExcelJS.CellValue): value is string | number | boolean | Date {
  if (typeof value === 'number') return Number.isFinite(value)
  if (value instanceof Date) return Number.isFinite(value.getTime())
  return typeof value === 'string' || typeof value === 'boolean'
}

type ResultGroup = { range: IRange; anchor: string; reason: 'array' | 'unsupported' }

interface SheetBounds {
  rows: number
  columns: number
  merges: IRange[]
  mergedCells?: Uint8Array
  resultGroups: ResultGroup[]
}

export interface LoadedSheet extends SheetBounds {
  worksheet: ExcelJS.Worksheet
  info: ImportSheetInfo
  formulas: ExcelJS.Cell[]
  missingGroupCaches: Map<string, ResultGroup['reason']>
  hasDefinedName: (upperName: string) => boolean
  styleOf: ReturnType<typeof createStyleConverter>
}

// ExcelJS 4.4 的读取模型与导出快照不同，合并区域在装载前存放于 mergeCells。
interface ReadCellModel {
  address?: string
  shareType?: string
  ref?: string
  type: ExcelJS.ValueType
  styleId?: number
  result?: ExcelJS.CellFormulaValue['result']
}

interface ReadSheetModel {
  id: number
  rows: { number: number; cells: ReadCellModel[] }[]
  cols?: { min: number; max: number }[]
  mergeCells?: string[]
}

interface ReadWorkbookModel {
  worksheets: ReadSheetModel[]
  sheets: { id: number }[]
  themes?: { theme1?: string }
  definedNames: { name: string; localSheetId?: number }[]
}

function point(address: string) {
  const match = /^\$?([A-Z]{1,3})\$?([1-9]\d*)$/.exec(address)
  if (!match) throw new ImportError('fileInvalid')
  const row = Number(match[2])
  const column = Tools.ABCatNum(match[1]) + 1
  if (row > IMPORT_LIMITS.sheetRows) throw new ImportError('tooManyRows')
  if (column > IMPORT_LIMITS.referenceColumns) throw new ImportError('fileInvalid')
  return { row, column }
}

function sheetBounds(sheet: ReadSheetModel, previousCells: number): SheetBounds {
  let rows = 0
  let columns = 0
  let lastRow = 0
  const resultGroups: ResultGroup[] = []
  function includeRange(ref: string): IRange {
    const parts = ref.split(':')
    if (parts.length > 2) throw new ImportError('fileInvalid')
    const [start, end = start] = parts
    const first = point(start)
    const last = point(end)
    if (first.row > last.row || first.column > last.column) throw new ImportError('fileInvalid')
    rows = Math.max(rows, last.row)
    columns = Math.max(columns, last.column)
    return {
      startRow: first.row - 1,
      startColumn: first.column - 1,
      endRow: last.row - 1,
      endColumn: last.column - 1
    }
  }
  for (const row of sheet.rows) {
    // OOXML 可以省略连续的行号，ExcelJS 对缺省 r 的解析结果是 NaN。
    if (Number.isNaN(row.number)) row.number = lastRow + 1
    if (!Number.isInteger(row.number) || row.number < 1) throw new ImportError('fileInvalid')
    if (row.number > IMPORT_LIMITS.sheetRows) throw new ImportError('tooManyRows')
    lastRow = row.number
    rows = Math.max(rows, row.number)
    let lastColumn = 0
    for (const cell of row.cells) {
      // OOXML 允许省略连续单元格的坐标；不使用可能滞后的 dimension/spans 声明。
      if (cell.address) {
        const pos = point(cell.address)
        if (pos.row !== row.number) throw new ImportError('fileInvalid')
        lastColumn = pos.column
      } else {
        lastColumn++
        if (lastColumn > IMPORT_LIMITS.referenceColumns) throw new ImportError('fileInvalid')
        cell.address = Tools.chatAtABC(lastColumn - 1) + row.number
      }
      columns = Math.max(columns, lastColumn)
      if (cell.ref && (cell.shareType === 'array' || cell.shareType === 'dataTable')) {
        resultGroups.push({
          range: includeRange(cell.ref),
          anchor: cell.address,
          reason: cell.shareType === 'array' ? 'array' : 'unsupported'
        })
      }
    }
  }
  const merges = (sheet.mergeCells || []).map(includeRange)
  if (previousCells + rows * columns > IMPORT_LIMITS.workbookCells) {
    throw new ImportError('tooManyCells')
  }
  for (const col of sheet.cols || []) {
    if (col.min < 1 || col.max < col.min || col.max > IMPORT_LIMITS.referenceColumns) {
      throw new ImportError('fileInvalid')
    }
  }
  if (!merges.length) return { rows, columns, merges, resultGroups }

  // Univer 可以直接接收矩形。用最多每格一字节的标记跳过被合并的值，
  // 避免 ExcelJS 为整个合并区创建单元格并两两扫描已有合并区。
  const mergedCells = new Uint8Array(rows * columns)
  for (const range of merges) {
    for (let r = range.startRow; r <= range.endRow; r++) {
      for (let c = range.startColumn; c <= range.endColumn; c++) {
        const index = r * columns + c
        if (mergedCells[index]) throw new ImportError('fileInvalid')
        mergedCells[index] = 2
      }
    }
    mergedCells[range.startRow * columns + range.startColumn] = 1
  }
  sheet.mergeCells = []
  return { rows, columns, merges, mergedCells, resultGroups }
}

export function isMergeChild(sheet: SheetBounds, row: number, column: number) {
  return sheet.mergedCells?.[row * sheet.columns + column] === 2
}

function missingResultCaches(worksheet: ExcelJS.Worksheet, bounds: SheetBounds) {
  const missing = new Map<string, ResultGroup['reason']>()
  let inspected = 0
  // 数组公式和模拟运算表只在主格保存公式，其余输出格也需要有可用的缓存。
  for (const { range, anchor, reason } of bounds.resultGroups) {
    const { startRow, endRow, startColumn, endColumn } = range
    inspected += (endRow - startRow + 1) * (endColumn - startColumn + 1)
    if (inspected > bounds.rows * bounds.columns) throw new ImportError('fileInvalid')
    for (let r = startRow; r <= endRow; r++) {
      for (let c = startColumn; c <= endColumn; c++) {
        const cell = worksheet.findCell(r + 1, c + 1)
        if (cell?.address === anchor || isCached(cell?.value)) continue
        missing.set(Tools.chatAtABC(c) + (r + 1), reason)
      }
    }
  }
  return missing
}

export async function loadFile(file: File): Promise<LoadedSheet[]> {
  if (!/\.xlsx$/i.test(file.name)) throw new ImportError('fileType')
  if (file.size > IMPORT_LIMITS.fileBytes) throw new ImportError('fileSize')
  const workbook = new ExcelJS.Workbook()
  const bounds = new Map<number, SheetBounds>()
  const workbookNames = new Set<string>()
  const localNames = new Map<number, Set<string>>()
  let theme: string
  const xlsx = workbook.xlsx as ExcelJS.Xlsx & {
    reconcile(model: ReadWorkbookModel, options: unknown): void
  }
  const reconcile = xlsx.reconcile.bind(xlsx)
  // 只拦截本次读取。ExcelJS 会把日期样式下的布尔、文本和错误缓存也强制转成日期。
  xlsx.reconcile = (model, options) => {
    const originalResults = new Map<ReadCellModel, ExcelJS.CellFormulaValue['result']>()
    for (const sheet of model.worksheets) {
      for (const row of sheet.rows || []) {
        for (const cell of row?.cells || []) {
          if (!cell?.styleId || cell.type !== ExcelJS.ValueType.Formula) continue
          if (cell.result !== undefined && typeof cell.result !== 'number') {
            originalResults.set(cell, cell.result)
          }
        }
      }
    }
    reconcile(model, options)
    for (const [cell, result] of originalResults) cell.result = result

    // 此时 ExcelJS 已补齐工作表 ID，尚未实例化工作簿，可先检查容量。
    let total = 0
    for (const sheet of model.worksheets) {
      const size = sheetBounds(sheet, total)
      bounds.set(sheet.id, size)
      total += size.rows * size.columns
    }
    theme = model.themes?.theme1
    // 只记名称和作用域：识别依赖名称的公式，同时避免 ExcelJS 逐格展开整列命名区域。
    for (const { name, localSheetId } of model.definedNames) {
      if (localSheetId === undefined) {
        workbookNames.add(name.toUpperCase())
      } else {
        const id = model.sheets[localSheetId]?.id
        if (id === undefined) continue
        if (!localNames.has(id)) localNames.set(id, new Set())
        localNames.get(id).add(name.toUpperCase())
      }
    }
    model.definedNames = []
  }
  // maxRows 按 XML 实际行数提前中止，稀疏行号则由上方模型检查补足。
  const readOptions = {
    maxRows: IMPORT_LIMITS.sheetRows,
    maxCols: IMPORT_LIMITS.referenceColumns,
    // 只导入链接单元格自身的值或公式，避免 ExcelJS 把带链接的公式改写成链接文本。
    ignoreNodes: [
      'hyperlinks',
      'conditionalFormatting',
      'dataValidations',
      'sheetProtection',
      'extLst'
    ]
  }
  try {
    await xlsx.load(await file.arrayBuffer(), readOptions)
  } catch (error) {
    if (
      error instanceof Error &&
      error.message === 'Max row count (' + readOptions.maxRows + ') exceeded'
    ) {
      throw new ImportError('tooManyRows')
    }
    throw error
  }
  if (!workbook.worksheets.length) throw new ImportError('emptyFile')
  const styleOf = createStyleConverter(theme)
  return workbook.worksheets.map(worksheet => {
    const size = bounds.get(worksheet.id)
    const info = {
      id: worksheet.id,
      name: worksheet.name,
      hidden: worksheet.state !== 'visible',
      rows: size.rows,
      columns: size.columns
    }
    // 修改选表、名称或公式策略时只需复查公式格。
    const formulas: ExcelJS.Cell[] = []
    worksheet.eachRow((row, r) => {
      row.eachCell((cell, c) => {
        if (cell.type === ExcelJS.ValueType.Formula && !isMergeChild(size, r - 1, c - 1))
          formulas.push(cell)
      })
    })
    const sheetNames = localNames.get(worksheet.id)
    const hasDefinedName = (upperName: string) =>
      workbookNames.has(upperName) || !!sheetNames?.has(upperName)
    const missingGroupCaches = missingResultCaches(worksheet, size)
    return { ...size, worksheet, info, formulas, hasDefinedName, styleOf, missingGroupCaches }
  })
}
