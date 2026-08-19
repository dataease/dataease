export async function ensureSheetSize(
  univerApi: any,
  worksheet: any,
  requiredRows: number,
  requiredColumns: number
): Promise<void> {
  const currentRows = getSheetRowCount(worksheet)
  const currentColumns = getSheetColumnCount(worksheet)

  if (currentRows > 0 && requiredRows > currentRows) {
    await expandRows(univerApi, worksheet, currentRows, requiredRows)
  }
  if (currentColumns > 0 && requiredColumns > currentColumns) {
    await expandColumns(univerApi, worksheet, currentColumns, requiredColumns)
  }

  const expandedRows = getSheetRowCount(worksheet)
  const expandedColumns = getSheetColumnCount(worksheet)
  if (
    expandedRows > 0 &&
    expandedColumns > 0 &&
    (expandedRows < requiredRows || expandedColumns < requiredColumns)
  ) {
    throw new Error(
      `工作表空间不足，自动扩容失败。当前 ${expandedRows} 行 ${expandedColumns} 列，需要 ${requiredRows} 行 ${requiredColumns} 列`
    )
  }
}

function getSheetRowCount(worksheet: any): number {
  return firstFiniteNumber(
    worksheet?.getRowCount?.(),
    worksheet?.getMaxRows?.(),
    worksheet?.getSheet?.()?.getRowCount?.(),
    worksheet?.getSheet?.()?.getMaxRows?.(),
    worksheet?.getSnapshot?.()?.rowCount,
    worksheet?.getSheet?.()?.getSnapshot?.()?.rowCount
  )
}

function getSheetColumnCount(worksheet: any): number {
  return firstFiniteNumber(
    worksheet?.getColumnCount?.(),
    worksheet?.getMaxColumns?.(),
    worksheet?.getSheet?.()?.getColumnCount?.(),
    worksheet?.getSheet?.()?.getMaxColumns?.(),
    worksheet?.getSnapshot?.()?.columnCount,
    worksheet?.getSheet?.()?.getSnapshot?.()?.columnCount
  )
}

function firstFiniteNumber(...values: unknown[]): number {
  const value = values.find(item => Number.isFinite(Number(item)))
  return value === undefined ? 0 : Number(value)
}

async function expandRows(
  univerApi: any,
  worksheet: any,
  currentRows: number,
  requiredRows: number
): Promise<void> {
  const appendCount = requiredRows - currentRows
  if (appendCount <= 0) {
    return
  }

  if (await callFirstAvailable(worksheet, [
    ['setRowCount', requiredRows],
    ['setRowsCount', requiredRows],
    ['resizeRows', requiredRows],
    ['insertRowsAfter', currentRows - 1, appendCount],
    ['insertRows', currentRows, appendCount],
    ['appendRows', appendCount]
  ])) {
    return
  }

  const sheetModel = worksheet?.getSheet?.()
  if (await callFirstAvailable(sheetModel, [
    ['setRowCount', requiredRows],
    ['setRowsCount', requiredRows],
    ['resizeRows', requiredRows],
    ['insertRowsAfter', currentRows - 1, appendCount],
    ['insertRows', currentRows, appendCount],
    ['appendRows', appendCount]
  ])) {
    return
  }

  await executeInsertCommand(univerApi, worksheet, 'sheet.command.insert-row', {
    startRow: currentRows,
    endRow: requiredRows - 1,
    startColumn: 0,
    endColumn: Math.max(getSheetColumnCount(worksheet) - 1, 0)
  })
}

async function expandColumns(
  univerApi: any,
  worksheet: any,
  currentColumns: number,
  requiredColumns: number
): Promise<void> {
  const appendCount = requiredColumns - currentColumns
  if (appendCount <= 0) {
    return
  }

  if (await callFirstAvailable(worksheet, [
    ['setColumnCount', requiredColumns],
    ['setColumnsCount', requiredColumns],
    ['resizeColumns', requiredColumns],
    ['insertColumnsAfter', currentColumns - 1, appendCount],
    ['insertColumns', currentColumns, appendCount],
    ['insertCols', currentColumns, appendCount],
    ['appendColumns', appendCount],
    ['appendCols', appendCount]
  ])) {
    return
  }

  const sheetModel = worksheet?.getSheet?.()
  if (await callFirstAvailable(sheetModel, [
    ['setColumnCount', requiredColumns],
    ['setColumnsCount', requiredColumns],
    ['resizeColumns', requiredColumns],
    ['insertColumnsAfter', currentColumns - 1, appendCount],
    ['insertColumns', currentColumns, appendCount],
    ['insertCols', currentColumns, appendCount],
    ['appendColumns', appendCount],
    ['appendCols', appendCount]
  ])) {
    return
  }

  await executeInsertCommand(univerApi, worksheet, 'sheet.command.insert-col', {
    startRow: 0,
    endRow: Math.max(getSheetRowCount(worksheet) - 1, 0),
    startColumn: currentColumns,
    endColumn: requiredColumns - 1
  })
}

async function callFirstAvailable(target: any, calls: Array<[string, ...unknown[]]>): Promise<boolean> {
  if (!target) {
    return false
  }

  for (const [method, ...args] of calls) {
    if (typeof target[method] !== 'function') {
      continue
    }
    await Promise.resolve(target[method](...args))
    return true
  }
  return false
}

async function executeInsertCommand(
  univerApi: any,
  worksheet: any,
  commandId: string,
  range: { startRow: number; endRow: number; startColumn: number; endColumn: number }
): Promise<void> {
  const workbook = univerApi.getActiveWorkbook?.()
  const unitId = workbook?.getId?.() || workbook?.getUnitId?.()
  const subUnitId = worksheet?.getSheetId?.()
  if (!unitId || !subUnitId) {
    throw new Error('工作表空间不足，且无法自动扩容')
  }

  await univerApi.executeCommand?.(commandId, {
    unitId,
    subUnitId,
    range
  })
}
