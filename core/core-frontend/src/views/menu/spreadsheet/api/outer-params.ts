import request from '@/config/axios'
import type { SpreadsheetOuterParamsConfig } from '../types/outer-params'

export const findOuterParamsSnapshot = async (
  id: string,
  signal?: AbortSignal
): Promise<SpreadsheetOuterParamsConfig> => {
  const res = await request.get<{ data: SpreadsheetOuterParamsConfig }>({
    url: `/spreadsheet/outerParams/snapshot/${encodeURIComponent(id)}`,
    signal
  })
  return res.data
}

export const findOuterParams = async (
  id: string,
  signal?: AbortSignal
): Promise<SpreadsheetOuterParamsConfig> => {
  const res = await request.get<{ data: SpreadsheetOuterParamsConfig }>({
    url: `/spreadsheet/outerParams/${encodeURIComponent(id)}`,
    signal
  })
  return res.data
}

export const saveOuterParams = async (
  data: SpreadsheetOuterParamsConfig
): Promise<SpreadsheetOuterParamsConfig> => {
  const res = await request.post<{ data: SpreadsheetOuterParamsConfig }>({
    url: '/spreadsheet/outerParams/save',
    data
  })
  return res.data
}
