import request from '@/config/axios'
import type { BusiTreeRequest } from '@/models/tree/TreeNode'

export enum SpreadsheetPublishStatus {
  Unpublished = 0,
  Published = 1,
  SavedUnpublished = 2
}

export interface SpreadsheetCreator {
  id?: number | string
  name: string
  pid?: number | string
  nodeType: 'folder' | 'sheet'
  orgId?: number | string
}

export interface SpreadsheetEditor {
  id?: number | string
  name?: string
  pid?: number | string
  nodeType?: 'folder' | 'sheet'
  orgId?: number | string
  sheetData?: string
  version?: number
  status?: SpreadsheetPublishStatus
  datasets?: DatasetBindDTO[]
  remark?: string
}

export interface DatasetBindDTO {
  id?: number | string
  sheetId?: number | string
  datasetId: number | string
  sheetName?: string
  rangeConfig?: string
  refreshType?: number
  refreshFreq?: number
}

export interface QueryFieldDTO {
  id: string | number
  name: string
  chartShowName?: string
  groupType: 'd' | 'q'
  deType?: number
  sort?: 'none' | 'asc' | 'desc' | 'custom'
  customSort?: Array<string | number>
}

export interface QueryFilterDTO {
  fieldId: string | number
  fieldName?: string
  operator: string
  values: (string | number)[]
}

export interface DetailTableQueryRequest {
  datasetId: number | string
  fields: QueryFieldDTO[]
  filters?: QueryFilterDTO[]
  resultLimit: number
}

export interface ColumnVO {
  id: string | number
  name: string
  chartShowName?: string
  groupType: 'd' | 'q'
  deType?: number
}

export interface DetailTableQueryResponse {
  columns: ColumnVO[]
  data: Array<(string | number | null)[]>
  total: number
  hasMore: boolean
}

export interface SpreadsheetVO {
  id: number | string
  name: string
  pid?: number | string
  nodeType: string
  orgId?: number | string
  level?: number
  sheetData?: string
  version?: number
  status?: SpreadsheetPublishStatus
  sort?: number
  createTime?: number
  createBy?: string
  creator?: string
  updateTime?: number
  updateBy?: string
  updater?: string
  remark?: string
  datasets?: DatasetBindDTO[]
  favorite?: boolean
}

export interface SpreadsheetTreeVO {
  id: number | string
  name: string
  pid?: number | string
  nodeType: string
  leaf?: boolean
  level?: number
  status?: SpreadsheetPublishStatus
  createTime?: number
  children?: SpreadsheetTreeVO[]
}

export interface SpreadsheetBarVO {
  id: number | string
  name: string
  createBy?: string
  creator?: string
  createTime?: number
  updateBy?: string
  updater?: string
  updateTime?: number
  status?: SpreadsheetPublishStatus
  version?: number
}

/**
 * 创建文件夹
 */
export const createFolder = async (data: SpreadsheetCreator): Promise<SpreadsheetVO> => {
  return request.post({ url: '/spreadsheet/createFolder', data })
}

/**
 * 创建电子表格
 */
export const create = async (data: SpreadsheetEditor): Promise<SpreadsheetVO> => {
  const res = await request.post({ url: '/spreadsheet/create', data })
  return res?.data
}

/**
 * 更新电子表格
 */
export const update = async (data: SpreadsheetEditor): Promise<SpreadsheetVO> => {
  const res = await request.post({ url: '/spreadsheet/update', data })
  return res?.data
}

/**
 * 获取电子表格详情
 */
export const findById = async (id: number | string): Promise<SpreadsheetVO> => {
  const res = await request.post({ url: '/spreadsheet/findById', data: { id } })
  return res?.data
}

export const findEditById = async (id: number | string): Promise<SpreadsheetVO> => {
  const res = await request.post({ url: '/spreadsheet/findEditById', data: { id } })
  return res?.data
}

/**
 * 获取资源树
 */
export const tree = async (data: BusiTreeRequest): Promise<SpreadsheetTreeVO[]> => {
  const res = await request.post({ url: '/spreadsheet/tree', data })
  return res?.data
}

/**
 * 移动资源
 */
export const move = async (data: SpreadsheetEditor): Promise<void> => {
  return request.post({ url: '/spreadsheet/move', data })
}

/**
 * 重命名
 */
export const rename = async (data: SpreadsheetEditor): Promise<void> => {
  return request.post({ url: '/spreadsheet/rename', data })
}

/**
 * 删除资源
 */
export const deleteResource = async (data: { id: number | string; rootOrgNode?: boolean }): Promise<void> => {
  return request.post({ url: `/spreadsheet/delete`, data })
}

/**
 * 名称检查
 */
export const nameCheck = async (data: SpreadsheetEditor): Promise<boolean> => {
  const res = await request.post({ url: '/spreadsheet/nameCheck', data })
  return res?.data
}

/**
 * 更新发布状态
 */
export const updateStatus = async (data: SpreadsheetEditor): Promise<SpreadsheetVO> => {
  const res = await request.post({ url: '/spreadsheet/updateStatus', data })
  return res?.data
}

export const recoverToPublished = async (id: number | string): Promise<SpreadsheetVO> => {
  const res = await request.post({ url: '/spreadsheet/recoverToPublished', data: { id } })
  return res?.data
}
