import ExcelJS from 'exceljs'
import {
  CellValueType,
  DateSystem,
  EXCEL_1904_OFFSET,
  generateRandomId,
  numfmt,
  Tools
} from '@univerjs/core'
import type { ICellData, IColumnData, IRowData, IWorksheetData } from '@univerjs/core'
import { ERROR_TYPE_SET } from '@univerjs/engine-formula'
import { analyzeFormula } from './formula'
import { ImportError, isCached, isMergeChild, loadFile } from './read-workbook'
import type { LoadedSheet } from './read-workbook'
import { FORMULA_MODES, IMPORT_LIMITS, validateSheetNames } from './protocol'
import type {
  FormulaMode,
  ImportAnalysis,
  ImportedSheet,
  ImportOptions,
  ImportRequest,
  ImportResponse
} from './protocol'

const errorLiterals: ReadonlySet<string> = ERROR_TYPE_SET
let loaded: LoadedSheet[] = []
let loadVersion = 0

function cellValue(cell: ExcelJS.Cell) {
  if (cell.type === ExcelJS.ValueType.Formula) {
    // ExcelJS 的 cell.value 会省略 0 和 false，result getter 保留原始缓存。
    return isCached(cell.result) ? cell.result : undefined
  }
  if (cell.type === ExcelJS.ValueType.Merge) return
  const value = cell.value
  if (isCached(value)) return value
  if (!value || typeof value !== 'object') return
  if ('richText' in value) return value.richText.map(run => run.text).join('')
  if ('text' in value) return value.text
  if ('error' in value) return value.error
}

function dateSerial(value: Date, dateSystem: DateSystem) {
  const serial = value.getTime() / 86400000 + 25569
  return dateSystem === DateSystem.Date1904 ? serial - EXCEL_1904_OFFSET : serial
}

function sourceDateSystem(cell: ExcelJS.Cell) {
  return cell.workbook.properties.date1904 ? DateSystem.Date1904 : DateSystem.Date1900
}

function preview(source: LoadedSheet): (string | null)[][] {
  const width = Math.min(source.info.columns, IMPORT_LIMITS.previewColumns)
  const height = Math.min(source.info.rows, IMPORT_LIMITS.previewRows)
  return Array.from({ length: height }, (_, row) =>
    Array.from({ length: width }, (_, column) => {
      if (source.missingGroupCaches.size) {
        const address = Tools.chatAtABC(column) + (row + 1)
        if (source.missingGroupCaches.has(address)) return null
      }
      const cell = source.worksheet.findCell(row + 1, column + 1)
      if (!cell || isMergeChild(source, row, column)) return ''
      const value = cellValue(cell)
      if (value instanceof Date) {
        const dateSystem = sourceDateSystem(cell)
        return numfmt.format(cell.numFmt, dateSerial(value, dateSystem), {
          dateSystem,
          throws: false
        })
      }
      if (value == null && cell.type === ExcelJS.ValueType.Formula) return null
      if (typeof value === 'boolean') return value ? 'TRUE' : 'FALSE'
      return String(value ?? '')
    })
  )
}

function selectedSheets(options: ImportOptions) {
  const names = new Map(options.sheets.map(sheet => [sheet.id, sheet.name.trim()]))
  const sheets = loaded.filter(({ info }) => names.has(info.id))
  if (!sheets.length || sheets.length !== options.sheets.length) {
    throw new ImportError('invalidSelection')
  }
  return sheets.map(source => {
    const { info } = source
    const name = names.get(info.id)
    // Excel 的列宽和列样式可位于已用数据区之外；目标表保留这些列，容量和预览仍按数据区计算。
    const columns = Math.max(20, info.columns + 4, source.worksheet.columns?.length ?? 0)
    const target = {
      name,
      renamed: name !== info.name,
      hasDefinedName: source.hasDefinedName,
      rows: Math.max(120, info.rows + 20),
      columns: Math.min(IMPORT_LIMITS.referenceColumns, columns)
    }
    return { ...source, target }
  })
}

type Selection = ReturnType<typeof selectedSheets>

function inspectFormulas(selection: Selection, mode: FormulaMode) {
  const context = new Map(selection.map(sheet => [sheet.info.name.toLowerCase(), sheet.target]))
  const analysis: ImportAnalysis = { kept: 0, warnings: [], fallbacks: [], errors: [] }
  const retained = new Map<ExcelJS.Cell, string>()
  for (const { info, formulas, missingGroupCaches } of selection) {
    for (const cell of formulas) {
      const result = analyzeFormula(cell, context, mode)
      if (result.formula) {
        retained.set(cell, result.formula)
        analysis.kept++
        if (result.warning) {
          analysis.warnings.push({
            sheetName: info.name,
            address: cell.address,
            reason: result.warning
          })
        }
        continue
      }
      const issue = {
        sheetName: info.name,
        address: cell.address,
        reason: result.reason,
        referencedSheet: result.sheet,
        missingCache: !isCached(cell.result)
      }
      // 保留所有公式时，无法安全迁移的单元格必须阻断整批导入，即使文件中有缓存。
      if (mode === 'all' || issue.missingCache) analysis.errors.push(issue)
      else analysis.fallbacks.push(issue)
    }
    if (mode !== 'all') {
      for (const [address, reason] of missingGroupCaches) {
        analysis.errors.push({ sheetName: info.name, address, reason, missingCache: true })
      }
    }
  }
  return { analysis, retained }
}

function toCellData(cell: ExcelJS.Cell, dateSystem: ImportOptions['dateSystem']): ICellData {
  const value = cellValue(cell)
  if (value instanceof Date) {
    // ExcelJS 已按源纪元解码。日期按目标纪元写入，纯时间和累计时长保留原始数值。
    const clockOnly = numfmt.getFormatInfo(cell.numFmt).type === 'time'
    return {
      t: CellValueType.NUMBER,
      v: dateSerial(value, clockOnly ? sourceDateSystem(cell) : dateSystem)
    }
  }
  if (typeof value === 'number') return { t: CellValueType.NUMBER, v: value }
  if (typeof value === 'boolean') return { t: CellValueType.BOOLEAN, v: value ? 1 : 0 }
  if (typeof value === 'string') {
    // 同样显示为 #N/A 时，Excel 的文本与真正的错误值参与公式计算的结果不同。
    const literal = cell.type !== ExcelJS.ValueType.Error && errorLiterals.has(value)
    return { t: literal ? CellValueType.FORCE_STRING : CellValueType.STRING, v: value }
  }
  return {}
}

function columnWidth(width: number) {
  // Excel 字符宽度以最大数字宽度计量，默认字体下 1 单位为 7px。
  return Math.max(1, Math.round(width * 7))
}

function convertSheet(
  source: Selection[number],
  options: ImportOptions,
  retained: Map<ExcelJS.Cell, string>
): ImportedSheet {
  const { worksheet, info, target, styleOf } = source
  const { keepFormat } = options
  const cellData: IWorksheetData['cellData'] = {}
  const rowData: IWorksheetData['rowData'] = {}
  const columnData: IWorksheetData['columnData'] = {}
  for (let r = 0; r < info.rows; r++) {
    const row = worksheet.findRow(r + 1)
    if (!row) continue
    if (keepFormat) {
      const format: IRowData = {}
      if (row.height > 0) format.h = (row.height * 96) / 72
      if (row.hidden) format.hd = 1
      const style = styleOf(row.style)
      if (style) format.s = style
      if (Object.keys(format).length) rowData[r] = format
    }
    for (let c = 0; c < row.cellCount; c++) {
      const cell = row.findCell(c + 1)
      if (!cell || isMergeChild(source, r, c)) continue
      const formula = retained.get(cell)
      // 可保留的公式由目标工作簿重算，避免残留缓存的文本类型覆盖公式。
      const data: ICellData = formula ? { f: formula } : toCellData(cell, options.dateSystem)
      if (keepFormat) {
        const style = styleOf(cell.style)
        if (style) data.s = style
      }
      if (!Object.keys(data).length) continue
      if (!cellData[r]) cellData[r] = {}
      cellData[r][c] = data
    }
  }
  const sheet: ImportedSheet = {
    id: generateRandomId(),
    name: target.name,
    hidden: 0,
    rowCount: target.rows,
    columnCount: target.columns,
    cellData
  }
  if (keepFormat) {
    worksheet.columns?.forEach((column, index) => {
      if (index >= target.columns || !column) return
      const format: IColumnData = {}
      if (column.width > 0) format.w = columnWidth(column.width)
      if (column.hidden) format.hd = 1
      const style = styleOf(column.style)
      if (style) format.s = style
      if (Object.keys(format).length) columnData[index] = format
    })
    if (worksheet.properties.defaultRowHeight > 0) {
      sheet.defaultRowHeight = (worksheet.properties.defaultRowHeight * 96) / 72
    }
    if (worksheet.properties.defaultColWidth > 0) {
      sheet.defaultColumnWidth = columnWidth(worksheet.properties.defaultColWidth)
    }
    sheet.rowData = rowData
    sheet.columnData = columnData
    sheet.mergeData = source.merges
  }
  return sheet
}

async function handle(request: ImportRequest): Promise<ImportResponse> {
  const { id } = request
  if (request.type === 'load') {
    const version = ++loadVersion
    loaded = []
    const sheets = await loadFile(request.file)
    if (version === loadVersion) loaded = sheets
    return { id, type: 'loaded', sheets: sheets.map(sheet => sheet.info) }
  }
  if (request.type === 'preview') {
    const sheet = loaded.find(sheet => sheet.info.id === request.sheetId)
    if (!sheet) throw new ImportError('invalidSelection')
    return { id, type: 'preview', sheetId: sheet.info.id, rows: preview(sheet) }
  }
  const { options } = request
  if (!FORMULA_MODES.includes(options.formulaMode)) throw new ImportError('invalidSelection')
  const selection = selectedSheets(options)
  const { analysis, retained } = inspectFormulas(selection, options.formulaMode)
  if (request.type === 'analyze') return { id, type: 'analysis', analysis }
  const errors = validateSheetNames(
    selection.map(sheet => sheet.target.name),
    []
  )
  if (errors.some(Boolean) || analysis.errors.length) throw new ImportError('invalidSelection')
  if (
    options.formulaMode === 'compatible' &&
    analysis.fallbacks.length &&
    !options.confirmedFallback
  ) {
    throw new ImportError('unconfirmed')
  }
  const sheets = selection.map(sheet => convertSheet(sheet, options, retained))
  return { id, type: 'converted', sheets }
}

self.onmessage = async ({ data }: MessageEvent<ImportRequest>) => {
  let response: ImportResponse
  try {
    response = await handle(data)
  } catch (error) {
    const code = error instanceof ImportError ? error.message : 'fileInvalid'
    response = { id: data.id, type: 'error', code }
  }
  self.postMessage(response)
}
