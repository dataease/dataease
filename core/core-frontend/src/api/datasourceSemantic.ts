import request from '@/config/axios'

export interface TableSemantic {
  tableName: string
  description: string | null
  grain: string | null
  active: boolean
  physicalLabel: string | null
  available: boolean
}

export interface FieldSemantic {
  tableName: string
  fieldName: string
  businessName: string
  description: string
  aliases: string | null
}

export interface DatasourceSemantics {
  relationCounts: Record<string, number>
  tables: TableSemantic[]
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

export const saveDatasourceTableSemantic = (id: string, data: TableSemantic) =>
  request.post({ url: `${path(id)}/table`, data })

export const saveDatasourceFieldSemantic = (id: string, data: FieldSemantic) =>
  request.post({ url: `${path(id)}/field`, data })

export const addDatasourceSemanticTables = (id: string, data: string[]) =>
  request.post({ url: `${path(id)}/tables/add`, data })

export const removeDatasourceSemanticTable = (id: string, tableName: string) =>
  request.post({ url: `${path(id)}/tables/remove`, data: { tableName } })

export const refreshDatasourceSemanticTable = (id: string, tableName: string) =>
  request.post<Promise<{ data: boolean }>>({
    url: `${path(id)}/tables/refresh`,
    data: { tableName }
  })
