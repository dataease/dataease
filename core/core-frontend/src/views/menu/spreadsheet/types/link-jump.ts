export interface SpreadsheetJumpField {
  id: string | number
  value?: string | number | boolean | null
  timeValue?: string | number | Array<string | number> | null
  linkageDateStyle?: 'H_m_s' | 'M_d'
  linkageDatePattern?: 'date_sub' | 'date_split'
}

export interface SpreadsheetJumpParams {
  sourceDvId: string | number
  sourceViewId: string | number
  sourceFieldId: string | number
  sourceResourceTable?: 'core' | 'snapshot'
  dimensionList: SpreadsheetJumpField[]
  quotaList: SpreadsheetJumpField[]
  customFilter?: unknown
}

export interface SpreadsheetLinkageFilter {
  fieldId: string
  operator: 'eq' | 'between'
  value: string[]
  dateStyle?: 'H_m_s' | 'M_d'
  datePattern?: 'date_sub' | 'date_split'
}
