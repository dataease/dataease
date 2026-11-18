import request from '@/config/axios'
import { sortBy } from 'lodash-es'

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

type FieldTreeNode = {
  text?: string | null
  children?: FieldTreeNode[]
}

// 沿用仪表板规则，对每一级的同级节点按显示名称排序。
const sortFieldTree = (nodes: FieldTreeNode[]): FieldTreeNode[] =>
  sortBy(nodes, node => (node.text ?? '').toLowerCase()).map(node =>
    node.children?.length ? { ...node, children: sortFieldTree(node.children) } : node
  )

export const getSpreadsheetFilterFieldTree = async (
  data: SpreadsheetFilterTreeValueRequest
): Promise<Record<string, unknown>[]> => {
  return request
    .post({ url: '/datasetData/getFieldTree', data })
    .then(res => sortFieldTree(res?.data || []))
}
