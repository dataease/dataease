import type { SpreadsheetFilterDisplayType, SpreadsheetQueryFilterItem } from './plugin'

export interface SpreadsheetOuterParamsRuntime {
  queryValues: Record<string, { self?: string[]; filter?: string[] }>
  fieldFilters: Record<string, SpreadsheetQueryFilterItem[]>
}

export interface SpreadsheetOuterParamsTarget {
  targetId?: string
  targetType: 'query' | 'field'
  targetRefId: string
  targetFieldId: string | null
  matchMode: 'self' | 'filter' | null
}

export interface SpreadsheetOuterParam {
  paramsInfoId?: string
  paramName: string
  checked: boolean
  required: boolean
  enabledDefault: boolean
  defaultValue: string | null
  targetList: SpreadsheetOuterParamsTarget[]
}

export interface SpreadsheetOuterParamsConfig {
  spreadsheetId: string
  checked: boolean
  outerParamsInfoArray: SpreadsheetOuterParam[]
}

export interface SpreadsheetOuterParamsContext {
  conditions: Array<{
    id: string
    name: string
    displayType: SpreadsheetFilterDisplayType
  }>
  dataAreas: Array<{
    id: string
    name: string
    datasetId?: string
    datasetName?: string
    pluginType: 'detail' | 'pivot'
    fields: Array<{ id: string; name: string; deType?: number }>
  }>
}
