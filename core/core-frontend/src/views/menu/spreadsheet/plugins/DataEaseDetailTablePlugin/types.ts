import type {
  PluginDataConfig,
  TableCellStyle,
  TableColumnWidthStyle,
  TablePluginConfig
} from "../../types/plugin"
import type { TableBorderConfig } from '../../components/table-border/border-config'

export interface TableDataResult {
  data: {
    fields: Array<{
      id: string | number
      name: string
      chartShowName?: string
      groupType: 'd' | 'q'
      deType?: number,
      sort?: 'none' | 'asc' | 'desc' | 'custom'
      customSort?: Array<string | number>
      dataeaseName: string
    }>,
    rowData: Record<string, any>[]
    total: number
    customTotalResult?: Record<string, number | string | null>
  }
  sql: string
}

export interface DetailTableQueryTotalField {
  dataeaseName: string
  aggregation: 'CUSTOM'
  originName: string
}

export interface DetailTableQueryConfig {
  totalFields: DetailTableQueryTotalField[]
}

export interface CellPosition {
  row: number
  col: number
}

export interface UniverCellStyle {
  bg?: { rgb: string }
  font?: {
    color?: { rgb: string }
    fontSize?: number
    bold?: boolean
    italic?: boolean
  }
  alignment?: {
    horizontal?: 'left' | 'center' | 'right'
    vertical?: 'top' | 'middle' | 'bottom'
    wrapText?: boolean
  }
  border?: {
    top?: { style: string; color: { rgb: string } }
    bottom?: { style: string; color: { rgb: string } }
    left?: { style: string; color: { rgb: string } }
    right?: { style: string; color: { rgb: string } }
  }
}

export interface DetailTableCellStyle extends TableCellStyle {
  enable: boolean
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

export interface DetailTableBaseStyle {
  customBlockName: boolean
  blockName: string
  hideHeader: boolean
  mergeCell: boolean
}

export interface DetailTableHeaderStyle {
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
  showIndex: boolean
  indexLabel: string
}

export type DetailTotalAggregation = 'MAX' | 'MIN' | 'AVG' | 'SUM' | 'CUSTOM'

export interface DetailTotalFieldConfig {
  fieldId: string | number
  dataeaseName: string
  aggregation: DetailTotalAggregation
  customExpression?: string
}

export interface DetailTableTotalStyle {
  enable: boolean
  label: string
  fieldConfig: DetailTotalFieldConfig[]
  customStyle: boolean
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

export interface DetailTableStyle {
  base?: DetailTableBaseStyle
  header?: DetailTableHeaderStyle
  cell?: DetailTableCellStyle
  total?: DetailTableTotalStyle
  columnWidth?: TableColumnWidthStyle
}


export interface DetailTableSenior {
  [key: string]: unknown
}

export interface DetailTableConfig extends TablePluginConfig {
  data: PluginDataConfig
  style: DetailTableStyle
  senior: DetailTableSenior
}
