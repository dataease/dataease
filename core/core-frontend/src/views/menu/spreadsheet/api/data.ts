import request from '@/config/axios'
import type { PluginFieldValuesRequest } from '../types/plugin'

/**
 * 查询明细表数据
 */
export const queryData = async (data): Promise<any> => {
  const res = await request.post({ url: '/spreadsheetData/queryData', data })
  return res?.data
}

export const queryFieldValues = async (data: PluginFieldValuesRequest): Promise<string[]> => {
  const res = await request.post({ url: '/spreadsheetData/queryFieldValues', data })
  return Array.isArray(res?.data) ? res.data.filter((value): value is string => typeof value === 'string') : []
}
