import request from '@/config/axios'

export type Cardinality = 'ONE_TO_ONE' | 'ONE_TO_MANY' | 'MANY_TO_ONE' | 'MANY_TO_MANY'
export type JoinType = 'LEFT' | 'INNER' | 'RIGHT' | 'FULL'

export interface Relation {
  id?: string
  name: string
  leftTable: string
  rightTable: string
  conditions: { leftField: string; rightField: string }[]
  cardinality: Cardinality
  joinType: JoinType
  description: string | null
  confirmed: boolean
  valid: boolean
  missingReferences: { tableName: string; fieldName: string | null }[]
}

const path = (id: string) => `/datasource/metadata/${id}`

export const listDatasourceRelations = (id: string) =>
  request.get<Promise<{ data: Relation[] }>>({ url: `${path(id)}/relations` })

export const saveDatasourceRelation = (id: string, data: Relation) =>
  request.post({ url: `${path(id)}/relation`, data })

export const removeDatasourceRelation = (id: string, relationId: string) =>
  request.delete({ url: `${path(id)}/relation/${relationId}` })
