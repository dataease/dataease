import request from '@/config/axios'

export interface SpreadsheetFilterEnumValueRequest {
  queryId: string | number
  displayId?: string | number
  sortId?: string | number
  sort?: string
  resultMode?: number
  searchText?: string
  filter?: Array<Record<string, unknown>>
}

export interface SpreadsheetFilterAutoEnumValueRequest {
  fieldIds: Array<string | number>
  resultMode?: number
}

export interface SpreadsheetFilterTreeValueRequest {
  fieldIds: Array<string | number>
  resultMode?: number
  filter?: Array<Record<string, unknown>>
}

export const enumSpreadsheetFilterValueObj = async (
  data: SpreadsheetFilterEnumValueRequest
): Promise<Record<string, unknown>[]> => {
  return request.post({ url: '/datasetData/enumValueObj', data }).then(res => res?.data || [])
}

export const getSpreadsheetFilterEnumValue = async (
  data: SpreadsheetFilterAutoEnumValueRequest
): Promise<unknown[]> => {
  return request.post({ url: '/datasetData/enumValue', data }).then(res => res?.data || [])
}

export const getSpreadsheetFilterFieldTree = async (
  data: SpreadsheetFilterTreeValueRequest
): Promise<Record<string, unknown>[]> => {
  return request.post({ url: '/datasetData/getFieldTree', data }).then(res => res?.data || [])
}
