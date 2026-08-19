import { DeepPartial, TablePluginAdapter } from "../../types/adapter";
import { FieldZoneSchema, StyleSchema } from "../../types/plugin";
import { DetailTableConfig } from "./types";
import TableCellEditor from './components/editor/TableCellEditor.vue'
import TableBaseEditor from './components/editor/TableBaseEditor.vue'
import TableHeaderEditor from './components/editor/TableHeaderEditor.vue'
import TableTotalEditor from './components/editor/TableTotalEditor.vue'
import DetailTableFieldItem from './components/editor/DetailTableFieldItem.vue'
import { createDefaultTableBorderConfig } from '../../components/table-border/border-config'

export class DetailTableAdapter extends TablePluginAdapter<DetailTableConfig> {
    constructor() {
        super('detail')
    }
    getPanelTitle(config: DeepPartial<DetailTableConfig>): string {
        const defaultName = `${config.placement?.sheetName || ''}!${config.placement?.startCell || ''}`
        return config.style?.base?.customBlockName
            ? config.style.base.blockName || defaultName
            : defaultName
    }
    getStyleSchema(): StyleSchema[] {
        return [
            {
                component: TableBaseEditor
            },
            {
                component: TableHeaderEditor
            },
            {
                component: TableCellEditor
            },
            {
                component: TableTotalEditor
            }
        ]
    }
    getSeniorSchema() {
        throw new Error("Method not implemented.");
    }
    
    getZonesSchema(): FieldZoneSchema[] {
        return [
            {
              id: 'fields',
              name: '数据列 / 维度或指标',
              acceptTypes: ['d', 'q'],
              minFields: 1,
              placeholder: '拖动字段至此处',
              allowFieldConfig: true,
              fieldItemComponent: DetailTableFieldItem
            }
          ]
    }

    getDefaultConfig() {
        return {
            id: `plugin_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
            type: 'detail',
            data: {
                datasetId: '',
                zones: {},
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
                    hideHeader: false,
                    mergeCell: false
                },
                header: {
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
                    verticalAlign: 'middle',
                    showIndex: false,
                    indexLabel: '序号'
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
                },
                total: {
                    enable: false,
                    label: '总计',
                    fieldConfig: [],
                    customStyle: false,
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
                }
            },
            senior: {}
        }
    }
}

export default new DetailTableAdapter()
