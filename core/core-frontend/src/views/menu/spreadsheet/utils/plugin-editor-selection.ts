interface SelectionRangeLike {
  startRow?: number
  startColumn?: number
  startCol?: number
  endRow?: number
  endColumn?: number
  endCol?: number
  row?: number
  col?: number
  actualRow?: number
  actualColumn?: number
}

interface WorkbookLike {
  getActiveCell?: () => { getRange?: () => SelectionRangeLike } | null
}

interface WorksheetLike {
  getSheet?: () => WorksheetLike
  getMergedCell?: (row: number, column: number) => SelectionRangeLike | null
}

interface NormalizedRange {
  startRow: number
  startColumn: number
  endRow: number
  endColumn: number
}

export function isPluginEditorCellSelection(
  workbook: WorkbookLike,
  worksheet: WorksheetLike,
  selections: SelectionRangeLike[]
): boolean {
  if (selections?.length !== 1) {
    return false
  }

  const selection = normalizeRange(selections[0])
  if (!selection) {
    return false
  }

  if (
    selection.startRow === selection.endRow &&
    selection.startColumn === selection.endColumn
  ) {
    return true
  }

  const activeCell = workbook?.getActiveCell?.()?.getRange?.()
  const actualRow = activeCell?.actualRow ?? activeCell?.startRow ?? activeCell?.row
  const actualColumn =
    activeCell?.actualColumn ?? activeCell?.startColumn ?? activeCell?.startCol ?? activeCell?.col
  if (actualRow == null || actualColumn == null) {
    return false
  }

  const sheet = worksheet?.getSheet?.() ?? worksheet
  const mergedRange = normalizeRange(sheet?.getMergedCell?.(actualRow, actualColumn))
  return !!mergedRange && isSameRange(selection, mergedRange)
}

function normalizeRange(range?: SelectionRangeLike | null): NormalizedRange | undefined {
  const startRow = range?.startRow ?? range?.row
  const startColumn = range?.startColumn ?? range?.startCol ?? range?.col
  if (startRow == null || startColumn == null) {
    return undefined
  }

  return {
    startRow,
    startColumn,
    endRow: range?.endRow ?? startRow,
    endColumn: range?.endColumn ?? range?.endCol ?? startColumn
  }
}

function isSameRange(
  first: NormalizedRange,
  second: NormalizedRange
): boolean {
  return (
    first.startRow === second.startRow &&
    first.startColumn === second.startColumn &&
    first.endRow === second.endRow &&
    first.endColumn === second.endColumn
  )
}
