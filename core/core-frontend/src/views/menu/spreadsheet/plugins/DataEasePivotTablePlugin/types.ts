import type {
  FieldItemData,
  PluginDataConfig,
  TablePluginConfig
} from '../../types/plugin'
import type { TableBorderConfig } from '../../components/table-border/border-config'
import type { SlashCellType } from '../DataEaseSlashCellPlugin/types'

export type PivotAggregation =
  | 'sum'
  | 'avg'
  | 'max'
  | 'min'
  | 'count'
  | 'count_distinct'

export interface PivotTableField extends FieldItemData {
  summary?: PivotAggregation
}

export interface PivotTableDataConfig extends PluginDataConfig {
  zones: {
    rows: PivotTableField[]
    columns: PivotTableField[]
  }
}

export interface PivotTableBaseStyle {
  customBlockName: boolean
  blockName: string
  mergeCell: boolean
  slashHeader: boolean
  slashHeaderType: SlashCellType
}

export interface PivotTableCellStyle {
  enable: boolean
  backgroundColor: string
  enableZebra: boolean
  zebraColor: string
  textColor: string
  fontSize: number
  bold: boolean
  italic: boolean
  underline: boolean
  strikethrough: boolean
  border?: TableBorderConfig
  textAlign: 'left' | 'center' | 'right'
  verticalAlign: 'top' | 'middle' | 'bottom'
}

export interface PivotTableHeaderStyle {
  enable: boolean
  backgroundColor: string
  textColor: string
  fontSize: number
  bold: boolean
  italic: boolean
  underline: boolean
  strikethrough: boolean
  border?: TableBorderConfig
  textAlign: 'left' | 'center' | 'right'
  verticalAlign: 'top' | 'middle' | 'bottom'
}

export interface PivotTableStyle {
  base: PivotTableBaseStyle
  rowHeader: PivotTableHeaderStyle
  columnHeader: PivotTableHeaderStyle
  cornerHeader: PivotTableHeaderStyle
  cell: PivotTableCellStyle
}

export interface PivotTableConfig extends TablePluginConfig {
  type: 'pivot'
  data: PivotTableDataConfig
  style: PivotTableStyle
  senior: Record<string, unknown>
}

export interface PivotTableQueryResult {
  data: {
    rowFields: PivotTableField[]
    columnFields: PivotTableField[]
    quotaFields: PivotTableField[]
    rowData: Record<string, unknown>[]
    total?: number
  }
  sql: string
}
