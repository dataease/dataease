import { useCache } from '@/hooks/web/useCache'

const SPREADSHEET_DRAFT_CACHE_PREFIX = 'DE-SPREADSHEET-CATCH-'

export interface SpreadsheetDraftCache {
  sheetId: string
  sourceVersion: number
  name: string
  remark: string
  sheetData: string
  cachedAt: number
}

const { wsCache } = useCache('localStorage')

const getCacheKey = (sheetId: string | number) =>
  `${SPREADSHEET_DRAFT_CACHE_PREFIX}${sheetId}`

export const getSpreadsheetDraftCache = (
  sheetId: string | number
): SpreadsheetDraftCache | undefined => {
  const cache = wsCache.get(getCacheKey(sheetId)) as SpreadsheetDraftCache | undefined
  if (!cache || String(cache.sheetId) !== String(sheetId) || !cache.sheetData) {
    return undefined
  }
  return cache
}

export const setSpreadsheetDraftCache = (cache: SpreadsheetDraftCache): void => {
  wsCache.set(getCacheKey(cache.sheetId), cache)
}

export const deleteSpreadsheetDraftCache = (sheetId?: string | number): void => {
  if (sheetId === undefined || sheetId === null) {
    return
  }
  wsCache.delete(getCacheKey(sheetId))
}
