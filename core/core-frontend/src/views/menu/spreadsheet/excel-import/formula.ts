import { Tools } from '@univerjs/core'
import {
  ALL_IMPLEMENTED_FUNCTIONS_SET,
  ERROR_TYPE_SET,
  FUNCTION_NAMES_CUBE,
  type ISequenceNode,
  LexerTreeBuilder,
  quoteSheetName,
  sequenceNodeType,
  singleReferenceToGrid,
  unquoteSheetName
} from '@univerjs/engine-formula'
import type { Cell, CellModel } from 'exceljs'
import { IMPORT_LIMITS, type FormulaMode } from './protocol'

const lexer = new LexerTreeBuilder()
const implementedFunctions: ReadonlySet<string> = ALL_IMPLEMENTED_FUNCTIONS_SET
const excelErrors: ReadonlySet<string> = ERROR_TYPE_SET
// IMAGE 会在显示时加载图片，其他外部数据或加载项函数也不能随工作簿启用连接。
const externalFunctions = new Set<string>([
  'IMAGE',
  'WEBSERVICE',
  'RTD',
  'SQL.REQUEST',
  'STOCKHISTORY',
  'CALL',
  'REGISTER.ID',
  ...Object.values(FUNCTION_NAMES_CUBE)
])

interface TargetSheet {
  name: string
  renamed: boolean
  rows: number
  columns: number
  hasDefinedName: (upperName: string) => boolean
}
type SheetContext = ReadonlyMap<string, TargetSheet>
type SheetExpansion = { sheet: TargetSheet; rows: number; columns: number }
type FormulaReason =
  | 'unsupported'
  | 'array'
  | 'external'
  | 'range'
  | 'missingSheet'
  | 'resultsOnly'
  | 'unknownFunction'
  | 'dynamicReference'
  | 'namedReference'
type RejectedFormula = { reason: FormulaReason; sheet?: string }
type FormulaResult =
  | { formula: string; expansions: SheetExpansion[]; warning?: 'unknownFunction' }
  | RejectedFormula
type ExcelFormulaModel = CellModel & { shareType?: string }

const unsupported = { reason: 'unsupported' } as const
const cellAddress = /^\$?[A-Z]{1,3}\$?[1-9]\d*$/i
const functionName = /^[\p{L}_][\p{L}\p{M}\p{N}_.]*$/u
const excelFunctionPrefix = /^_(?:XLFN|XLWS|XLETA)\./

function quoteText(value: string) {
  return '"' + value.replace(/"/g, '""') + '"'
}

function rewriteReference(
  token: string,
  currentSheet: string,
  context: SheetContext,
  expansions: SheetExpansion[],
  rowOffset: number,
  columnOffset: number,
  linkTarget = false
): { formula: string } | RejectedFormula {
  if (/[\[\]]/.test(token)) return { reason: 'external' }
  if (token.endsWith('#') || token.startsWith('@')) return { reason: 'array' }
  const separator = token.lastIndexOf('!')
  const qualifier = token.slice(0, Math.max(0, separator))
  let sheetName = currentSheet
  if (separator !== -1) {
    if (!qualifier || qualifier.includes(':')) return unsupported
    sheetName = qualifier
    if (qualifier.startsWith("'")) {
      if (!/^'(?:[^']|'')+'$/.test(qualifier)) return unsupported
      sheetName = unquoteSheetName(qualifier.slice(1, -1))
    } else if (/[\s!'"]/.test(qualifier)) {
      // 未加引号的空格可表示引用交集，不能把整个前缀当作一个表名。
      return unsupported
    }
  }
  const sheet = context.get(sheetName.toLowerCase())
  if (!sheet) return { reason: 'missingSheet', sheet: sheetName }
  // Univer 以第一个 ! 分隔最终表名；保留此类引用会指向错误的页。
  if ((separator !== -1 || linkTarget) && sheet.name.includes('!')) return unsupported

  const addresses = token.slice(separator + 1).split(':')
  if (addresses.length > 2 || !addresses.every(address => cellAddress.test(address.trim()))) {
    return unsupported
  }
  const start = { row: 0, column: 0 }
  const translated: string[] = []
  for (const part of addresses) {
    const address = part.trim()
    const columnAbsolute = address.startsWith('$')
    const rowAbsolute = /\$\d+$/.test(address)
    const point = singleReferenceToGrid(address.toUpperCase())
    const row = point.row + (rowAbsolute ? 0 : rowOffset)
    const column = point.column + (columnAbsolute ? 0 : columnOffset)
    if (
      row < 0 ||
      row >= IMPORT_LIMITS.sheetRows ||
      column < 0 ||
      column >= IMPORT_LIMITS.referenceColumns
    ) {
      return { reason: 'range' }
    }
    if (!translated.length) {
      start.row = row
      start.column = column
    } else {
      const size = (Math.abs(row - start.row) + 1) * (Math.abs(column - start.column) + 1)
      if (size > IMPORT_LIMITS.workbookCells) return { reason: 'range' }
    }
    if (row >= sheet.rows || column >= sheet.columns) {
      expansions.push({ sheet, rows: row + 1, columns: column + 1 })
    }
    if (row === point.row && column === point.column) {
      translated.push(part)
    } else {
      const columnLabel = (columnAbsolute ? '$' : '') + Tools.chatAtABC(column)
      const rowLabel = (rowAbsolute ? '$' : '') + (row + 1)
      translated.push(part.replace(address, () => columnLabel + rowLabel))
    }
  }
  // 未改名的显式引用保持原样，包括引号和大小写。
  let prefix = token.slice(0, separator + 1)
  // 内部超链接默认取编辑器活动页，且按大小写精确查表，需按实际来源限定目标。
  const qualifyLink = linkTarget && (separator === -1 || sheet.name !== sheetName)
  if (qualifyLink || (separator !== -1 && sheet.renamed))
    prefix = `'${quoteSheetName(sheet.name)}'!`
  return { formula: prefix + translated.join(':') }
}

export function analyzeFormula(
  cell: Cell,
  context: SheetContext,
  mode: FormulaMode
): FormulaResult {
  if (mode === 'values') return { reason: 'resultsOnly' }
  try {
    let model = cell.model as ExcelFormulaModel
    let rowOffset = 0
    let columnOffset = 0
    if (model.sharedFormula) {
      // 从主格读取原式，只平移引用 token；ExcelJS 展开共享公式时也会替换字符串中的 A1。
      if (!cellAddress.test(model.sharedFormula)) return unsupported
      const origin = singleReferenceToGrid(model.sharedFormula.toUpperCase())
      const master = cell.worksheet.findCell(origin.row + 1, origin.column + 1)
      if (!master) return unsupported
      model = master.model as ExcelFormulaModel
      if (model.shareType !== 'shared') return unsupported
      rowOffset = cell.fullAddress.row - origin.row - 1
      columnOffset = cell.fullAddress.col - origin.column - 1
    }
    if (model.shareType === 'array') return { reason: 'array' }
    if (model.shareType && model.shareType !== 'normal' && model.shareType !== 'shared') {
      return unsupported
    }
    const raw = model.formula
    if (!raw || raw.length > IMPORT_LIMITS.formulaLength) return unsupported
    const expression = raw.replace(/^=/, '')
    const nodes = lexer.sequenceNodesBuilder('=' + expression)
    if (!nodes?.length || nodes.length > IMPORT_LIMITS.formulaTokens) return unsupported

    let source = ''
    let formula = '='
    const expansions: SheetExpansion[] = []
    let hasUnknownFunction = false
    let textTarget: { node: ISequenceNode; name: 'SHEET' | 'HYPERLINK' } | undefined
    const currentSheet = context.get(cell.worksheet.name.toLowerCase())
    const functionGroups: boolean[] = []
    let followsValue = false
    let followsFunction = false
    for (const [index, node] of nodes.entries()) {
      if (typeof node === 'string') {
        if (/^[#@{}]$/.test(node)) return { reason: 'array' }
        if (!/^[\s+\-*/^%&=<>(),]+$/.test(node)) return unsupported
        source += node
        formula += node
        if (!node.trim()) continue
        // 逗号仅用作函数参数分隔符；Univer 也能解析区域并集和连续调用，首期不支持。
        if (node === '(') {
          if (followsValue && !followsFunction) return unsupported
          functionGroups.push(followsFunction)
        }
        if (node === ',' && !functionGroups[functionGroups.length - 1]) return unsupported
        if (node === ')' && functionGroups.pop() === undefined) return unsupported
        followsValue = node === ')' || node === '%'
        followsFunction = false
        continue
      }
      const token = node.token.trim()
      if (followsValue && node.nodeType !== sequenceNodeType.NORMAL) return unsupported
      followsValue = node.nodeType !== sequenceNodeType.NORMAL
      followsFunction = node.nodeType === sequenceNodeType.FUNCTION && nodes[index + 1] === '('
      let original = node.token
      let rewritten = node.token
      switch (node.nodeType) {
        case sequenceNodeType.REFERENCE: {
          const reference = rewriteReference(
            token,
            cell.worksheet.name,
            context,
            expansions,
            rowOffset,
            columnOffset
          )
          if ('reason' in reference) return reference
          rewritten = node.token.replace(token, () => reference.formula)
          break
        }
        case sequenceNodeType.STRING: {
          // 词法分析会把 Excel 字符串中的 "" 解码为 "，写回表达式时要恢复转义。
          if (token.length < 2 || !token.startsWith('"') || !token.endsWith('"')) return unsupported
          const value = token.slice(1, -1)
          original = node.token.replace(token, () => quoteText(value))
          let target = value
          if (textTarget?.node === node) {
            if (textTarget.name === 'SHEET') {
              const sheet = context.get(value.toLowerCase())
              if (!sheet) return { reason: 'missingSheet', sheet: value }
              if (sheet.renamed) target = sheet.name
            } else if (value.startsWith('#')) {
              // 字符串里的 A1 不随共享公式平移。
              const ref = rewriteReference(
                value.slice(1),
                cell.worksheet.name,
                context,
                expansions,
                0,
                0,
                true
              )
              if ('reason' in ref) return ref
              target = '#' + ref.formula
            }
            textTarget = undefined
          }
          rewritten =
            target === value ? original : node.token.replace(token, () => quoteText(target))
          break
        }
        case sequenceNodeType.FUNCTION: {
          // 词法节点可能把一元负号与函数名放在一起，原文仍用完整 token 写回。
          const name = token.replace(/^-/, '').toUpperCase()
          if (!followsFunction && (name === 'TRUE' || name === 'FALSE' || excelErrors.has(name))) {
            break
          }
          if (/\[[^\]]+\][^!]*!|\|/.test(token)) return { reason: 'external' }
          if (/^[@{]|#$/.test(token)) return { reason: 'array' }
          if (token.includes('[') || token.includes('!')) return { reason: 'namedReference' }
          if (!functionName.test(name)) return unsupported
          if (!followsFunction) return { reason: 'namedReference' }
          // Univer 只剥离一层 Excel 函数前缀。额外前缀不能把已知的外部或动态引用伪装成未知函数。
          const engineName = name.replace(excelFunctionPrefix, '')
          const unprefixed = name.replace(/^(?:_(?:XLFN|XLWS|XLETA)\.)+/, '')
          if (currentSheet?.hasDefinedName(name) || currentSheet?.hasDefinedName(engineName)) {
            return { reason: 'namedReference' }
          }
          if (externalFunctions.has(unprefixed)) return { reason: 'external' }
          if (unprefixed === 'INDIRECT' || unprefixed === 'OFFSET') {
            return { reason: 'dynamicReference' }
          }
          if (unprefixed === 'ANCHORARRAY' || unprefixed === 'SINGLE') return { reason: 'array' }
          if (!implementedFunctions.has(engineName)) hasUnknownFunction = true
          if (engineName === 'SHEET' || engineName === 'HYPERLINK') {
            // 这两个函数能把文本绑定到工作表，只迁移可辨认的单个目标参数。
            const [argument, end] = nodes
              .slice(index + 2)
              .filter(n => typeof n !== 'string' || n.trim())
            if (engineName === 'SHEET' && argument === ')') break
            if (!argument || typeof argument === 'string' || (end !== ')' && end !== ',')) {
              return { reason: 'dynamicReference' }
            }
            if (argument.nodeType === sequenceNodeType.STRING) {
              textTarget = { node: argument, name: engineName }
            } else if (engineName !== 'SHEET' || argument.nodeType !== sequenceNodeType.REFERENCE) {
              return { reason: 'dynamicReference' }
            }
          }
          break
        }
        case sequenceNodeType.NUMBER:
          // Univer 的数字 token 也接受 JS 十六进制等写法，Excel 数值只允许十进制及科学计数法。
          if (!/^[+-]?(?:\d+(?:\.\d*)?|\.\d+)(?:e[+-]?\d+)?$/i.test(token)) return unsupported
          break
        case sequenceNodeType.NORMAL:
          if (token.includes('@')) return { reason: 'array' }
          if (!/^[+\-\s]+$/.test(token)) return unsupported
          break
        case sequenceNodeType.ARRAY:
          return { reason: 'array' }
        case sequenceNodeType.TABLE:
        case sequenceNodeType.DEFINED_NAME:
          return { reason: 'namedReference' }
        default:
          return unsupported
      }
      source += original
      formula += rewritten
    }
    // 仅在整条原式被识别且重写后的表达式仍能被 Univer 解析时保留。
    if (source !== expression || functionGroups.length) return unsupported
    const tree = lexer.treeBuilder(formula)
    if (!tree || typeof tree !== 'object') return unsupported
    // 未知函数不能提前返回，还要核对其余参数是否依赖未选工作表。
    if (hasUnknownFunction && mode !== 'all') return { reason: 'unknownFunction' }
    return { formula, expansions, warning: hasUnknownFunction ? 'unknownFunction' : undefined }
  } catch {
    return unsupported
  }
}
