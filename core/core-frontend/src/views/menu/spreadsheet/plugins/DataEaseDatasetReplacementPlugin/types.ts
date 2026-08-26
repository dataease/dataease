export type DatasetReplacementScope = 'workbook' | 'component'

export type ReplacementFieldGroup = 'd' | 'q'

export interface DatasetIdentity {
  id: string
  name: string
}

export interface ReplacementField {
  id: string
  name: string
  dataeaseName?: string
  groupType: ReplacementFieldGroup
  deType?: number
  extField?: number
  type?: string
  [key: string]: unknown
}

export interface FieldUsageFragment {
  fieldId: string
  name: string
  dataeaseName?: string
  groupType: ReplacementFieldGroup
  deType?: number
  extField?: number
  type?: string
  occurrences?: number
  metadataComplete?: boolean
}

export interface DatasetUsageFragment {
  dataset: DatasetIdentity
  componentId: string
  componentName?: string
  componentType?: 'plugin' | 'filter'
  fields: FieldUsageFragment[]
}

export interface DatasetUsageComponent {
  id: string
  name: string
  type: 'plugin' | 'filter'
}

export interface FieldUsage extends FieldUsageFragment {
  key: string
  datasetId: string
  componentIds: string[]
  occurrences: number
}

export interface DatasetUsage {
  dataset: DatasetIdentity
  componentIds: string[]
  componentCount: number
  components: DatasetUsageComponent[]
  fields: FieldUsage[]
}

export interface FieldMapping {
  source: FieldUsage
  target?: ReplacementField
  autoMatched: boolean
}

export interface DatasetMapping {
  source: DatasetUsage
  target?: DatasetIdentity
  targetFields: ReplacementField[]
  fields: FieldMapping[]
}

export interface ReplacementDraft {
  scope: DatasetReplacementScope
  componentId?: string
  mappings: DatasetMapping[]
}

export interface ReplacementResult {
  changedComponentIds: string[]
  refreshFailedComponentIds: string[]
}

export interface DatasetReplacementAdapter {
  readonly type: string
  collect(unitId: string, componentId?: string): DatasetUsageFragment[]
  snapshot(unitId: string, componentIds: string[]): unknown
  replace(unitId: string, mappings: DatasetMapping[], componentIds: string[]): void
  restore(unitId: string, snapshot: unknown): void
  refresh(
    unitId: string,
    componentIds: string[],
    mappings?: DatasetMapping[],
    snapshot?: unknown
  ): Promise<string[]>
  getConfigs?(unitId: string, componentIds: string[]): Array<{ id: string; type: string }>
}
