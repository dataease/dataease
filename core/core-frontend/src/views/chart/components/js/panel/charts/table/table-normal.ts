import { S2ChartView, S2DrawOptions } from '@/views/chart/components/js/panel/types/impl/s2'
import { ColCell, S2Event, S2Options, TableSheet } from '@antv/s2/esm/index'
import { parseJson } from '@/views/chart/components/js/util'
import { formatterItem, valueFormatter } from '@/views/chart/components/js/formatter'
import { getCurrentField } from '@/views/chart/components/js/panel/common/common_table'
import { TABLE_EDITOR_PROPERTY, TABLE_EDITOR_PROPERTY_INNER } from './common'
import { useI18n } from '@/hooks/web/useI18n'

const { t } = useI18n()
/**
 * 汇总表
 */
export class TableNormal extends S2ChartView<TableSheet> {
  properties = TABLE_EDITOR_PROPERTY
  propertyInner = TABLE_EDITOR_PROPERTY_INNER
  axis: AxisType[] = ['xAxis', 'yAxis', 'drill', 'filter']
  axisConfig: AxisConfig = {
    xAxis: {
      name: `${t('chart.drag_block_table_data_column')}/${t('chart.dimension')}`,
      type: 'd'
    },
    yAxis: {
      name: `${t('chart.drag_block_table_data_column')}/${t('chart.quota')}`,
      type: 'q'
    }
  }

  drawChart(drawOption: S2DrawOptions<TableSheet>): TableSheet {
    const { container, chart } = drawOption
    const containerDom = document.getElementById(container)
    if (!containerDom) return

    // fields
    const fields = chart.data.fields

    const columns = []
    const meta = []

    // add drill list
    fields.forEach(ele => {
      const f = getCurrentField(chart.yAxis, ele)
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
          if (f.formatterCfg) {
            return valueFormatter(value, f.formatterCfg)
          } else {
            return valueFormatter(value, formatterItem)
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
      data: chart.data.tableRow,
      style: this.configStyle(chart)
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
    }

    // 开始渲染
    const newChart = new TableSheet(containerDom, s2DataConfig, s2Options)

    // click
    newChart.on(S2Event.DATA_CELL_CLICK, drawOption.action)

    // theme
    const customTheme = this.configTheme(chart)
    newChart.setThemeCfg({ theme: customTheme })

    return newChart
  }
  constructor() {
    super('table-normal', [])
  }
}
