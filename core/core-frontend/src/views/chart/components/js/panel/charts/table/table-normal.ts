import { S2ChartView, S2DrawOptions } from '@/views/chart/components/js/panel/types/impl/s2'
import { S2Event, S2Options, TableSheet } from '@antv/s2'
import { parseJson } from '@/views/chart/components/js/util'
import { formatterItem, valueFormatter } from '@/views/chart/components/js/formatter'
import { getCurrentField } from '@/views/chart/components/js/panel/common/common_table'

/**
 * 汇总表
 */
export class TableNormal extends S2ChartView<TableSheet> {
  properties: EditorProperty[] = [
    'background-overall-component',
    'basic-style-selector',
    'table-header-selector',
    'table-cell-selector',
    'title-selector'
  ]
  propertyInner: EditorPropertyInner = {
    'background-overall-component': ['all'],
    'basic-style-selector': ['lineWidth', 'tableBorderColor', 'tableScrollBarColor', 'alpha'],
    'table-header-selector': [
      'tableHeaderBgColor',
      'tableTitleFontSize',
      'tableHeaderFontColor',
      'tableTitleHeight',
      'tableHeaderAlign',
      'showIndex',
      'indexLabel'
    ],
    'table-cell-selector': [
      'tableItemBgColor',
      'tableItemFontSize',
      'tableFontColor',
      'tableItemAlign',
      'tableItemHeight'
    ],
    'title-selector': [
      'title',
      'fontSize',
      'color',
      'hPosition',
      'isItalic',
      'isBolder',
      'remarkShow',
      'fontFamily',
      'letterSpace',
      'fontShadow'
    ]
  }
  axis: AxisType[] = ['xAxis', 'yAxis', 'drill', 'filter']
  drawChart(drawOption: S2DrawOptions<TableSheet>): TableSheet {
    const { container, chart } = drawOption
    let { chartObj: s2 } = drawOption
    const containerDom = document.getElementById(container)
    if (!containerDom) return

    // fields
    const fields = chart.data.fields
    if (!fields || fields.length === 0) {
      if (s2) {
        s2.destroy()
      }
      return
    }

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
      data: chart.data.tableRow
    }

    const customAttr = parseJson(chart.customAttr)
    // options
    const s2Options: S2Options = {
      width: containerDom.offsetWidth,
      height: containerDom.offsetHeight,
      showSeriesNumber: customAttr.tableHeader.showIndex,
      style: this.configStyle(chart),
      totals: {}
      // conditions: getConditions(chart)
    }
    // 开启序号之后，第一列就是序号列，修改 label 即可
    if (s2Options.showSeriesNumber) {
      s2Options.colCell = node => {
        if (node.colIndex === 0) {
          if (!customAttr.tableHeader.indexLabel) {
            node.label = ' '
          } else {
            node.label = customAttr.tableHeader.indexLabel
          }
        }
        return null
      }
    }

    // 开始渲染
    if (s2) {
      s2.destroy()
    }
    s2 = new TableSheet(containerDom, s2DataConfig, s2Options)

    // click
    s2.on(S2Event.DATA_CELL_CLICK, drawOption.action)

    // theme
    const customTheme = this.configTheme(chart)
    s2.setThemeCfg({ theme: customTheme })

    return s2
  }
  constructor() {
    super('table-normal', [])
  }
}
