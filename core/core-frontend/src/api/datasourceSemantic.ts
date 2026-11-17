import request from '@/config/axios'

export interface TableSemantic {
  id: string
  tableName: string
  description: string | null
  grain: string | null
  active: boolean
  physicalLabel: string | null
  available: boolean
  confirmed: boolean
}

export interface FieldSemantic {
  tableName: string
  fieldName: string
  businessName: string
  description: string
  aliases: string | null
}

export interface DatasourceSemantics {
  tables: TableSemantic[]
}

export interface TableSemanticDetails {
  relationCount: number
  fields: (Omit<FieldSemantic, 'businessName' | 'description'> & {
    businessName: string | null
    description: string | null
    confirmed: boolean
    physicalType: string | null
    physicalComment: string | null
  })[]
}

const path = (id: string) => `/datasource/metadata/${id}`

export const getDatasourceSemantics = (id: string) =>
  request.get<Promise<{ data: DatasourceSemantics }>>({ url: `${path(id)}/semantics` })

export const getDatasourceSemanticDetails = (id: string, tableId: string) =>
  request.post<Promise<{ data: TableSemanticDetails }>>({
    url: `${path(id)}/semantics/detail`,
    data: { tableId }
  })

export const saveDatasourceTableSemantic = (id: string, data: TableSemantic) =>
  request.post({ url: `${path(id)}/table`, data })

export const saveDatasourceFieldSemantic = (id: string, data: FieldSemantic) =>
  request.post({ url: `${path(id)}/field`, data })

export const setDatasourceTableSemanticConfirmed = (
  id: string,
  tableName: string,
  confirmed: boolean
) => request.post({ url: `${path(id)}/table/confirmation`, data: { tableName, confirmed } })

export const setDatasourceFieldSemanticConfirmed = (
  id: string,
  tableName: string,
  fieldName: string,
  confirmed: boolean
) =>
  request.post({ url: `${path(id)}/field/confirmation`, data: { tableName, fieldName, confirmed } })

export const addDatasourceSemanticTables = (id: string, data: string[]) =>
  request.post({ url: `${path(id)}/tables/add`, data })

export const removeDatasourceSemanticTable = (id: string, tableName: string) =>
  request.post({ url: `${path(id)}/tables/remove`, data: { tableName } })

export const refreshDatasourceSemanticTable = (id: string, tableName: string) =>
  request.post<Promise<{ data: boolean }>>({
    url: `${path(id)}/tables/refresh`,
    data: { tableName }
  })
