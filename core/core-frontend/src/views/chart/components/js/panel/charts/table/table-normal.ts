import { S2ChartView, S2DrawOptions } from '@/views/chart/components/js/panel/types/impl/s2'
import { S2Event, S2Options, TableSheet } from '@antv/s2/esm/index'
import { parseJson } from '@/views/chart/components/js/util'
import { formatterItem, valueFormatter } from '@/views/chart/components/js/formatter'
import { getCurrentField } from '@/views/chart/components/js/panel/common/common_table'
import { TABLE_EDITOR_PROPERTY, TABLE_EDITOR_PROPERTY_INNER } from './common'
import { useI18n } from '@/hooks/web/useI18n'
import { isNumber } from 'lodash-es'
import { TableColCell } from '@antv/s2'

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
      name: `${t('chart.drag_block_table_data_column')} / ${t('chart.dimension')}`,
      type: 'd'
    },
    yAxis: {
      name: `${t('chart.drag_block_table_data_column')} / ${t('chart.quota')}`,
      type: 'q'
    }
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    chart.xAxis = []
    return chart
  }

  drawChart(drawOption: S2DrawOptions<TableSheet>): TableSheet {
    const { container, chart, action } = drawOption
    const containerDom = document.getElementById(container)
    if (!containerDom) return

    // fields
    let fields = chart.data.fields

    const columns = []
    const meta = []
    if (chart.drill) {
      // 下钻过滤字段
      const filterFields = chart.drillFilters.map(i => i.fieldId)
      // 下钻入口的字段下标
      const drillFieldId = chart.drillFields[0].id
      const drillFieldIndex = chart.xAxis.findIndex(ele => ele.id === drillFieldId)
      // 当前下钻字段
      const curDrillFieldId = chart.drillFields[filterFields.length].id
      const curDrillField = fields.filter(ele => ele.id === curDrillFieldId)
      filterFields.push(curDrillFieldId)
      // 移除下钻字段，把当前下钻字段插入到下钻入口位置
      fields = fields.filter(ele => {
        return !filterFields.includes(ele.id)
      })
      fields.splice(drillFieldIndex, 0, ...curDrillField)
    }
    // add drill list
    fields.forEach(ele => {
      const f = getCurrentField(chart.yAxis, ele)
      columns.push(ele.dataeaseName)
      meta.push({
        field: ele.dataeaseName,
        name: ele.chartShowName ?? ele.name,
        formatter: function (value) {
          if (!f) {
            return value
          }
          if (value === null || value === undefined) {
            return value
          }
          if (f.groupType === 'd' || !isNumber(value)) {
            return value
          }
          let formatCfg = f.formatterCfg
          if (!formatCfg) {
            formatCfg = formatterItem
          }
          return valueFormatter(value, formatCfg)
        }
      })
    })

    // 空值处理
    const newData = this.configEmptyDataStrategy(chart)
    // data config
    const s2DataConfig = {
      fields: {
        columns: columns
      },
      meta: meta,
      data: newData,
      style: this.configStyle(chart)
    }

    const customAttr = parseJson(chart.customAttr)
    // options
    const s2Options: S2Options = {
      width: containerDom.offsetWidth,
      height: containerDom.offsetHeight,
      showSeriesNumber: customAttr.tableHeader.showIndex,
      style: this.configStyle(chart),
      conditions: this.configConditions(chart)
    }
    // 开启序号之后，第一列就是序号列，修改 label 即可
    if (s2Options.showSeriesNumber) {
      s2Options.colCell = (node, sheet, config) => {
        if (node.colIndex === 0) {
          let indexLabel = customAttr.tableHeader.indexLabel
          if (!indexLabel) {
            indexLabel = ''
          }
          const cell = new TableColCell(node, sheet, config)
          const shape = cell.getTextShape() as any
          shape.attrs.text = indexLabel
          return cell
        }
        return new TableColCell(node, sheet, config)
      }
    }

    // 开始渲染
    const newChart = new TableSheet(containerDom, s2DataConfig, s2Options)

    // click
    newChart.on(S2Event.DATA_CELL_CLICK, ev => {
      const cell = newChart.getCell(ev.target)
      const meta = cell.getMeta()
      const nameIdMap = fields.reduce((pre, next) => {
        pre[next['dataeaseName']] = next['id']
        return pre
      }, {})

      const rowData = chart.data.tableRow[meta.rowIndex] as any
      const dimensionList = []
      for (const key in rowData) {
        if (nameIdMap[key]) {
          dimensionList.push({ id: nameIdMap[key], value: rowData[key] })
        }
      }
      const param = {
        x: ev.x,
        y: ev.y,
        data: {
          dimensionList,
          name: nameIdMap[meta.valueField],
          sourceType: 'table-normal',
          quotaList: []
        }
      }
      action(param)
    })
    // theme
    const customTheme = this.configTheme(chart)
    newChart.setThemeCfg({ theme: customTheme })

    return newChart
  }
  constructor() {
    super('table-normal', [])
  }
}
