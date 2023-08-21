import { ColCell, DataCell, S2Event, S2Options, TableSheet } from '@antv/s2/esm/index'
import { formatterItem, valueFormatter } from '../../../formatter'
import { parseJson } from '../../../util'
import { getCurrentField } from '../../common/common_table'
import { S2ChartView, S2DrawOptions } from '../../types/impl/s2'
import { TABLE_EDITOR_PROPERTY, TABLE_EDITOR_PROPERTY_INNER } from './common'
import { useI18n } from '@/hooks/web/useI18n'

const { t } = useI18n()

/**
 * 明细表
 */
export class TableInfo extends S2ChartView<TableSheet> {
  properties = TABLE_EDITOR_PROPERTY
  propertyInner = {
    ...TABLE_EDITOR_PROPERTY_INNER,
    'basic-style-selector': [
      'tableColumnMode',
      'tableBorderColor',
      'tableScrollBarColor',
      'alpha',
      'tablePageMode'
    ]
  }
  axis: AxisType[] = ['xAxis', 'filter', 'drill']
  axisConfig: AxisConfig = {
    xAxis: {
      name: `${t('chart.drag_block_table_data_column')}/${t('chart.dimension_or_quota')}`
    }
  }

  public drawChart(drawOption: S2DrawOptions<TableSheet>): TableSheet {
    const { container, chart, pageInfo, action } = drawOption
    const containerDom = document.getElementById(container)

    // fields
    const fields = chart.data.fields

    const columns = []
    const meta = []
    fields.forEach(ele => {
      const f = getCurrentField(chart.xAxis, ele)
      columns.push(ele.dataeaseName)
      meta.push({
        field: ele.dataeaseName,
        name: ele.name,
        formatter: function (value) {
          if (!f) {
            return value
          }
          if (value === null || value === undefined) {
            return value
          }
          if (f.groupType === 'd') {
            return value
          } else {
            if (f.formatterCfg) {
              const v = valueFormatter(value, f.formatterCfg)
              return v.includes('NaN') ? value : v
            } else {
              const v = valueFormatter(value, formatterItem)
              return v.includes('NaN') ? value : v
            }
          }
        }
      })
    })
    // 空值处理
    // const newData = handleTableEmptyStrategy(tableData, chart)
    // data config
    const s2DataConfig = {
      fields: {
        columns: columns
      },
      meta: meta,
      data: chart.data.tableRow
    }

    const customAttr = parseJson(chart.customAttr)
    // options
    const s2Options: S2Options = {
      width: containerDom.offsetWidth,
      height: containerDom.offsetHeight,
      showSeriesNumber: customAttr.tableHeader.showIndex,
      style: this.configStyle(chart)
      // conditions: getConditions(chart)
    }
    // 开启序号之后，第一列就是序号列，修改 label 即可
    if (s2Options.showSeriesNumber) {
      s2Options.colCell = (node, spreadsheet, headerConfig) => {
        if (node.colIndex === 0) {
          if (!customAttr.tableHeader.indexLabel) {
            node.label = ' '
          } else {
            node.label = customAttr.tableHeader.indexLabel
          }
        }
        return new ColCell(node, spreadsheet, headerConfig)
      }
      s2Options.dataCell = viewMeta => {
        if (viewMeta.colIndex === 0) {
          viewMeta.fieldValue =
            pageInfo.pageSize * (pageInfo.currentPage - 1) + viewMeta.rowIndex + 1
        }
        return new DataCell(viewMeta, viewMeta?.spreadsheet)
      }
    }

    // 开始渲染
    const newChart = new TableSheet(containerDom, s2DataConfig, s2Options)

    // click
    newChart.on(S2Event.DATA_CELL_CLICK, action)

    // theme
    const customTheme = this.configTheme(chart)
    newChart.setThemeCfg({ theme: customTheme })

    return newChart
  }

  constructor() {
    super('table-info', [])
  }
}
