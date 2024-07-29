import { EXTRA_FIELD, PivotSheet, S2Event, S2Options, TOTAL_VALUE, S2Theme, Totals } from '@antv/s2'
import { formatterItem, valueFormatter } from '../../../formatter'
import { hexColorToRGBA, isAlphaColor, parseJson } from '../../../util'
import { S2ChartView, S2DrawOptions } from '../../types/impl/s2'
import { TABLE_EDITOR_PROPERTY_INNER } from './common'
import { useI18n } from '@/hooks/web/useI18n'
import { isNumber, maxBy, merge, minBy } from 'lodash-es'
import { copyContent } from '../../common/common_table'

const { t } = useI18n()

/**
 * 透视表
 */
export class TablePivot extends S2ChartView<PivotSheet> {
  properties: EditorProperty[] = [
    'background-overall-component',
    'basic-style-selector',
    'table-header-selector',
    'table-cell-selector',
    'table-total-selector',
    'title-selector',
    'tooltip-selector',
    'function-cfg',
    'threshold',
    'linkage',
    'jump-set'
  ]
  propertyInner = {
    ...TABLE_EDITOR_PROPERTY_INNER,
    'table-header-selector': [
      'tableHeaderBgColor',
      'tableTitleFontSize',
      'tableHeaderFontColor',
      'tableTitleHeight',
      'tableHeaderAlign',
      'showColTooltip',
      'showRowTooltip',
      'showHorizonBorder',
      'showVerticalBorder'
    ],
    'table-total-selector': ['row', 'col'],
    'basic-style-selector': [
      'tableColumnMode',
      'tableBorderColor',
      'tableScrollBarColor',
      'alpha',
      'tableLayoutMode'
    ]
  }
  axis: AxisType[] = ['xAxis', 'xAxisExt', 'yAxis', 'filter']
  axisConfig: AxisConfig = {
    xAxis: {
      name: `${t('chart.table_pivot_row')} / ${t('chart.dimension')}`,
      type: 'd'
    },
    xAxisExt: {
      name: `${t('chart.drag_block_table_data_column')} / ${t('chart.dimension')}`,
      type: 'd'
    },
    yAxis: {
      name: `${t('chart.drag_block_table_data_column')} / ${t('chart.quota')}`,
      type: 'q'
    }
  }

  public drawChart(drawOption: S2DrawOptions<PivotSheet>): PivotSheet {
    const { container, chart, chartObj, action } = drawOption
    const containerDom = document.getElementById(container)

    const { xAxisExt: columnFields, xAxis: rowFields, yAxis: valueFields } = chart
    const [c, r, v] = [columnFields, rowFields, valueFields].map(arr =>
      arr.map(i => i.dataeaseName)
    )

    // fields
    const fields = chart.data.fields
    if (!fields || fields.length === 0) {
      if (chartObj) {
        chartObj.destroy()
      }
      return
    }

    const columns = []
    const meta = []

    const valueFieldMap: Record<string, Axis> = [
      ...chart.xAxis,
      ...chart.xAxisExt,
      ...chart.yAxis
    ].reduce((p, n) => {
      p[n.dataeaseName] = n
      return p
    }, {})
    fields.forEach(ele => {
      const f = valueFieldMap[ele.dataeaseName]
      columns.push(ele.dataeaseName)
      meta.push({
        field: ele.dataeaseName,
        name: ele.chartShowName ?? ele.name,
        formatter: value => {
          if (!f) {
            return value
          }
          if (value === null || value === undefined) {
            return value
          }
          if (![2, 3].includes(f.deType) || !isNumber(value)) {
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

    // total config
    const customAttr = parseJson(chart.customAttr)
    const { tableTotal, basicStyle } = customAttr
    tableTotal.row.subTotalsDimensions = r
    tableTotal.col.subTotalsDimensions = c

    // 解析合计、小计排序
    const sortParams = []
    if (
      tableTotal.row.totalSort &&
      tableTotal.row.totalSort !== 'none' &&
      c.length > 0 &&
      tableTotal.row.showGrandTotals &&
      v.indexOf(tableTotal.row.totalSortField) > -1
    ) {
      const sort = {
        sortFieldId: c[0],
        sortMethod: tableTotal.row.totalSort.toUpperCase(),
        sortByMeasure: TOTAL_VALUE,
        query: {
          [EXTRA_FIELD]: tableTotal.row.totalSortField
        }
      }
      sortParams.push(sort)
    }
    if (
      tableTotal.col.totalSort &&
      tableTotal.col.totalSort !== 'none' &&
      r.length > 0 &&
      tableTotal.col.showGrandTotals &&
      v.indexOf(tableTotal.col.totalSortField) > -1
    ) {
      const sort = {
        sortFieldId: r[0],
        sortMethod: tableTotal.col.totalSort.toUpperCase(),
        sortByMeasure: TOTAL_VALUE,
        query: {
          [EXTRA_FIELD]: tableTotal.col.totalSortField
        }
      }
      sortParams.push(sort)
    }
    // 自定义总计小计
    const totals = [
      tableTotal.row.calcTotals,
      tableTotal.row.calcSubTotals,
      tableTotal.col.calcTotals,
      tableTotal.col.calcSubTotals
    ]
    totals.forEach(total => {
      if (total.cfg?.length) {
        delete total.aggregation
        const totalCfgMap = total.cfg.reduce((p, n) => {
          p[n.dataeaseName] = n
          return p
        }, {})
        total.calcFunc = (query, data) => {
          return customCalcFunc(query, data, totalCfgMap)
        }
      }
    })
    // 空值处理
    const newData = this.configEmptyDataStrategy(chart)
    // data config
    const s2DataConfig = {
      fields: {
        rows: r,
        columns: c,
        values: v
      },
      meta: meta,
      data: newData,
      sortParams: sortParams
    }
    // options
    const style = this.configStyle(chart)
    style.hierarchyCollapse = true
    const s2Options: S2Options = {
      width: containerDom.offsetWidth,
      height: containerDom.offsetHeight,
      style,
      totals: tableTotal as Totals,
      conditions: this.configConditions(chart),
      tooltip: {
        getContainer: () => containerDom
      },
      hierarchyType: basicStyle.tableLayoutMode ?? 'grid'
    }

    // tooltip
    this.configTooltip(chart, s2Options)
    // 开始渲染
    const s2 = new PivotSheet(containerDom, s2DataConfig, s2Options as unknown as S2Options)
    // tooltip
    const { show } = customAttr.tooltip
    if (show) {
      s2.on(S2Event.COL_CELL_HOVER, event => this.showTooltip(s2, event, meta))
      s2.on(S2Event.ROW_CELL_HOVER, event => this.showTooltip(s2, event, meta))
      s2.on(S2Event.DATA_CELL_HOVER, event => this.showTooltip(s2, event, meta))
    }
    // click
    s2.on(S2Event.DATA_CELL_CLICK, ev => this.dataCellClickAction(chart, ev, s2, action))
    s2.on(S2Event.ROW_CELL_CLICK, ev => this.headerCellClickAction(chart, ev, s2, action))
    s2.on(S2Event.COL_CELL_CLICK, ev => this.headerCellClickAction(chart, ev, s2, action))
    // right click
    s2.on(S2Event.GLOBAL_CONTEXT_MENU, event => copyContent(s2, event, meta))
    // theme
    const customTheme = this.configTheme(chart)
    s2.setThemeCfg({ theme: customTheme })

    return s2
  }
  private dataCellClickAction(chart: Chart, ev, s2Instance: PivotSheet, callback) {
    const cell = s2Instance.getCell(ev.target)
    const meta = cell.getMeta()
    const nameIdMap = chart.data.fields.reduce((pre, next) => {
      pre[next['dataeaseName']] = next['id']
      return pre
    }, {})
    const rowData = { ...meta.rowQuery, ...meta.colQuery }
    rowData[meta.valueField] = meta.fieldValue
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
        sourceType: 'table-pivot',
        quotaList: []
      }
    }
    callback(param)
  }
  private headerCellClickAction(chart: Chart, ev, s2Instance: PivotSheet, callback) {
    const cell = s2Instance.getCell(ev.target)
    const meta = cell.getMeta()
    const rowData = meta.query
    const nameIdMap = chart.data.fields.reduce((pre, next) => {
      pre[next['dataeaseName']] = next['id']
      return pre
    }, {})
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
        sourceType: 'table-pivot',
        quotaList: []
      }
    }
    callback(param)
  }
  protected configTheme(chart: Chart): S2Theme {
    const theme = super.configTheme(chart)
    const { basicStyle, tableHeader } = parseJson(chart.customAttr)
    let tableHeaderBgColor = tableHeader.tableHeaderBgColor
    if (!isAlphaColor(tableHeaderBgColor)) {
      tableHeaderBgColor = hexColorToRGBA(tableHeaderBgColor, basicStyle.alpha)
    }
    const tableBorderColor = hexColorToRGBA(basicStyle.tableBorderColor, basicStyle.alpha)
    const tableHeaderFontColor = hexColorToRGBA(tableHeader.tableHeaderFontColor, basicStyle.alpha)
    const fontStyle = tableHeader.isItalic ? 'italic' : 'normal'
    const fontWeight = tableHeader.isBolder === false ? 'normal' : 'bold'
    const pivotTheme = {
      rowCell: {
        cell: {
          backgroundColor: tableHeaderBgColor,
          horizontalBorderColor: tableBorderColor,
          verticalBorderColor: tableBorderColor
        },
        text: {
          fill: tableHeaderFontColor,
          fontSize: tableHeader.tableTitleFontSize,
          textAlign: tableHeader.tableHeaderAlign,
          textBaseline: 'top',
          fontStyle,
          fontWeight
        },
        bolderText: {
          fill: tableHeaderFontColor,
          fontSize: tableHeader.tableTitleFontSize,
          textAlign: tableHeader.tableHeaderAlign,
          fontStyle,
          fontWeight
        },
        measureText: {
          fill: tableHeaderFontColor,
          fontSize: tableHeader.tableTitleFontSize,
          textAlign: tableHeader.tableHeaderAlign,
          fontStyle,
          fontWeight
        },
        seriesText: {
          fill: tableHeaderFontColor,
          fontSize: tableHeader.tableTitleFontSize,
          textAlign: tableHeader.tableHeaderAlign,
          fontStyle,
          fontWeight
        }
      }
    }
    merge(theme, pivotTheme)
    if (tableHeader.showHorizonBorder === false) {
      const tmp: S2Theme = {
        cornerCell: {
          cell: {
            horizontalBorderColor: tableHeaderBgColor,
            horizontalBorderWidth: 0
          }
        },
        rowCell: {
          cell: {
            horizontalBorderColor: tableHeaderBgColor,
            horizontalBorderWidth: 0
          }
        }
      }
      merge(theme, tmp)
    }
    if (tableHeader.showVerticalBorder === false) {
      const tmp: S2Theme = {
        cornerCell: {
          cell: {
            verticalBorderColor: tableHeaderBgColor,
            verticalBorderWidth: 0
          }
        },
        rowCell: {
          cell: {
            verticalBorderColor: tableHeaderBgColor,
            verticalBorderWidth: 0
          }
        }
      }
      merge(theme, tmp)
    }
    return theme
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    const { customAttr } = chart
    if (customAttr.basicStyle.tableColumnMode === 'field') {
      customAttr.basicStyle.tableColumnMode = 'custom'
    }
    return chart
  }

  constructor() {
    super('table-pivot', [])
  }
}
function customCalcFunc(query, data, totalCfgMap) {
  if (!data?.length || !query[EXTRA_FIELD]) {
    return 0
  }
  const aggregation = totalCfgMap[query[EXTRA_FIELD]]?.aggregation || 'SUM'
  switch (aggregation) {
    case 'SUM': {
      return data.reduce((p, n) => {
        return p + parseFloat(n[query[EXTRA_FIELD]] ?? 0)
      }, 0)
    }
    case 'AVG': {
      const sum = data.reduce((p, n) => {
        return p + parseFloat(n[query[EXTRA_FIELD]] ?? 0)
      }, 0)
      return sum / data.length
    }
    case 'MIN': {
      const result = minBy(data, n => {
        return parseFloat(n[query[EXTRA_FIELD]])
      })
      return result?.[query[EXTRA_FIELD]]
    }
    case 'MAX': {
      const result = maxBy(data, n => {
        return parseFloat(n[query[EXTRA_FIELD]])
      })
      return result?.[query[EXTRA_FIELD]]
    }
    default: {
      return data.reduce((p, n) => {
        return p + parseFloat(n[query[EXTRA_FIELD]] ?? 0)
      }, 0)
    }
  }
}
