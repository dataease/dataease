import {
  setWorksheetColumnCountSilently,
  setWorksheetRowCountSilently
} from '../../../utils/silent-worksheet-write'

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

  const workbook = univerApi.getActiveWorkbook?.()
  const unitId = workbook?.getId?.() || workbook?.getUnitId?.()
  const sheetId = worksheet?.getSheetId?.()
  if (!unitId || !sheetId) {
    throw new Error('工作表空间不足，且无法自动扩容')
  }

  await setWorksheetRowCountSilently(univerApi, {
    unitId,
    sheetId,
    count: requiredRows
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

  const workbook = univerApi.getActiveWorkbook?.()
  const unitId = workbook?.getId?.() || workbook?.getUnitId?.()
  const sheetId = worksheet?.getSheetId?.()
  if (!unitId || !sheetId) {
    throw new Error('工作表空间不足，且无法自动扩容')
  }

  await setWorksheetColumnCountSilently(univerApi, {
    unitId,
    sheetId,
    count: requiredColumns
  })
}
