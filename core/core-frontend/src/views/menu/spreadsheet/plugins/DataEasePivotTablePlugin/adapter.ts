import type { DeepPartial } from '../../types/adapter'
import { TablePluginAdapter } from '../../types/adapter'
import type { FieldItemData, FieldZoneSchema, StyleSchema } from '../../types/plugin'
import TableBaseEditor from './components/editor/TableBaseEditor.vue'
import TableCellEditor from './components/editor/TableCellEditor.vue'
import TableColumnHeaderEditor from './components/editor/TableColumnHeaderEditor.vue'
import TableCornerHeaderEditor from './components/editor/TableCornerHeaderEditor.vue'
import PivotTableFieldItem from './components/editor/PivotTableFieldItem.vue'
import TableRowHeaderEditor from './components/editor/TableRowHeaderEditor.vue'
import type { PivotTableConfig } from './types'
import { createDefaultTableBorderConfig } from '../../components/table-border/border-config'
import {
  normalizePivotFields,
  validatePivotZoneUpdate
} from './utils/pivot-config-validator'

export class PivotTableAdapter extends TablePluginAdapter<PivotTableConfig> {
  constructor() {
    super('pivot')
  }

  getPanelTitle(config: DeepPartial<PivotTableConfig>): string {
    const defaultName = `${config.placement?.sheetName || ''}!${config.placement?.startCell || ''}`
    return config.style?.base?.customBlockName
      ? config.style.base.blockName || defaultName
      : defaultName
  }

  getZonesSchema(): FieldZoneSchema[] {
    return [
      {
        id: 'rows',
        name: '行',
        acceptTypes: ['d', 'q'],
        placeholder: '拖动字段至此处',
        allowFieldConfig: true,
        fieldItemComponent: PivotTableFieldItem
      },
      {
        id: 'columns',
        name: '列',
        acceptTypes: ['d', 'q'],
        placeholder: '拖动字段至此处',
        allowFieldConfig: true,
        fieldItemComponent: PivotTableFieldItem
      }
    ]
  }

  validateZoneUpdate(
    config: PivotTableConfig,
    zoneId: string,
    fields: FieldItemData[]
  ): string | undefined {
    return validatePivotZoneUpdate(config, zoneId, normalizePivotFields(fields))
  }

  getStyleSchema(): StyleSchema[] {
    return [
      { component: TableBaseEditor },
      { component: TableRowHeaderEditor },
      { component: TableColumnHeaderEditor },
      { component: TableCornerHeaderEditor },
      { component: TableCellEditor }
    ]
  }

  getSeniorSchema(): never[] {
    return []
  }

  getDefaultConfig(): DeepPartial<PivotTableConfig> {
    return {
      id: `plugin_${Date.now()}_${Math.random().toString(36).slice(2, 11)}`,
      type: 'pivot',
      data: {
        datasetId: '',
        zones: {
          rows: [],
          columns: []
        },
        customFilter: {},
        resultLimit: 1000
      },
      placement: {
        sheetId: '',
        sheetName: 'Sheet1',
        startCell: 'A1'
      },
      style: {
        base: {
          customBlockName: false,
          blockName: '',
          mergeCell: false,
          slashHeader: false,
          slashHeaderType: 'two'
        },
        rowHeader: {
          enable: false,
          backgroundColor: '',
          textColor: '#333333',
          fontSize: 12,
          bold: true,
          italic: false,
          underline: false,
          strikethrough: false,
          border: createDefaultTableBorderConfig(),
          textAlign: 'left',
          verticalAlign: 'middle'
        },
        columnHeader: {
          enable: false,
          backgroundColor: '',
          textColor: '#333333',
          fontSize: 12,
          bold: true,
          italic: false,
          underline: false,
          strikethrough: false,
          border: createDefaultTableBorderConfig(),
          textAlign: 'left',
          verticalAlign: 'middle'
        },
        cornerHeader: {
          enable: false,
          backgroundColor: '',
          textColor: '#333333',
          fontSize: 12,
          bold: true,
          italic: false,
          underline: false,
          strikethrough: false,
          border: createDefaultTableBorderConfig(),
          textAlign: 'left',
          verticalAlign: 'middle'
        },
        cell: {
          enable: false,
          backgroundColor: '',
          enableZebra: false,
          zebraColor: '#f5f7fa',
          textColor: '#333333',
          fontSize: 12,
          bold: false,
          italic: false,
          underline: false,
          strikethrough: false,
          border: createDefaultTableBorderConfig(),
          textAlign: 'left',
          verticalAlign: 'middle'
        }
      },
      senior: {}
    }
  }
}

export default new PivotTableAdapter()
