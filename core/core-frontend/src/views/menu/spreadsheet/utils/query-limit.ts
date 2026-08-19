import request from '@/config/axios'

export const DEFAULT_SPREADSHEET_QUERY_LIMIT = 100000
export const DEFAULT_PLUGIN_RESULT_LIMIT = 1000

const normalizeMaxLimit = (value: unknown): number => {
  const numericValue = Number(value)
  return Number.isFinite(numericValue) && numericValue > 0
    ? Math.floor(numericValue)
    : DEFAULT_SPREADSHEET_QUERY_LIMIT
}

export const clampSpreadsheetResultLimit = (value: unknown, max: number): number => {
  const normalizedMax = normalizeMaxLimit(max)
  const numericValue = Number(value)
  const normalizedValue = Number.isFinite(numericValue) && numericValue > 0
    ? Math.floor(numericValue)
    : DEFAULT_PLUGIN_RESULT_LIMIT
  return Math.min(normalizedValue, normalizedMax)
}

export const getSpreadsheetQueryLimit = async (): Promise<number> => {
  try {
    const response = await request.get({ url: '/spreadsheetData/queryLimit' })
    return normalizeMaxLimit(response?.data)
  } catch {
    return DEFAULT_SPREADSHEET_QUERY_LIMIT
  }
}
