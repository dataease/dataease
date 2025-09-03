import { useI18n } from '@/hooks/web/useI18n'
import { formatterItem, valueFormatter } from '@/views/chart/components/js/formatter'
import {
  configEmptyDataStyle,
  copyContent,
  CustomDataCell,
  getSummaryRow,
  SortTooltip,
  SummaryCell,
  summaryRowStyle
} from '@/views/chart/components/js/panel/common/common_table'
import { S2ChartView, S2DrawOptions } from '@/views/chart/components/js/panel/types/impl/s2'
import { parseJson } from '@/views/chart/components/js/util'
import {
  type LayoutResult,
  S2DataConfig,
  S2Event,
  S2Options,
  ScrollbarPositionType,
  TableColCell,
  TableSheet,
  ViewMeta
} from '@antv/s2'
import { isNumber } from 'lodash-es'
import { TABLE_EDITOR_PROPERTY, TABLE_EDITOR_PROPERTY_INNER } from './common'

const { t } = useI18n()
/**
 * 汇总表
 */
export class TableNormal extends S2ChartView<TableSheet> {
  properties = TABLE_EDITOR_PROPERTY
  propertyInner: EditorPropertyInner = {
    ...TABLE_EDITOR_PROPERTY_INNER,
    'table-header-selector': [
      ...TABLE_EDITOR_PROPERTY_INNER['table-header-selector'],
      'tableHeaderSort',
      'showTableHeader'
    ],
    'basic-style-selector': [
      ...TABLE_EDITOR_PROPERTY_INNER['basic-style-selector'],
      'tablePageMode',
      'showHoverStyle'
    ],
    'table-cell-selector': [
      ...TABLE_EDITOR_PROPERTY_INNER['table-cell-selector'],
      'tableFreeze',
      'tableColumnFreezeHead',
      'tableRowFreezeHead'
    ],
    'summary-selector': ['showSummary', 'summaryLabel']
  }
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
    const { container, chart, action, pageInfo, resizeAction } = drawOption
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
    const axisMap = [...chart.xAxis, ...chart.yAxis].reduce((pre, cur) => {
      pre[cur.dataeaseName] = cur
      return pre
    }, {})
    // add drill list
    fields.forEach(ele => {
      const f = axisMap[ele.dataeaseName]
      if (f?.hide === true) {
        return
      }
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
          if (![2, 3, 4].includes(f.deType) || !isNumber(value)) {
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
    const s2DataConfig: S2DataConfig = {
      fields: {
        columns: columns
      },
      meta: meta,
      data: newData
    }

    const { basicStyle, tableCell, tableHeader, tooltip } = parseJson(chart.customAttr)
    // options
    const s2Options: S2Options = {
      width: containerDom.getBoundingClientRect().width,
      height: containerDom.offsetHeight,
      seriesNumber: {
        enable: tableHeader.showIndex,
        text: tableHeader.indexLabel ?? t('chart.index')
      },
      conditions: this.configConditions(chart),
      tooltip: {
        getContainer: () => containerDom,
        render: sheet => new SortTooltip(sheet)
      },
      interaction: {
        hoverHighlight: !(basicStyle.showHoverStyle === false),
        scrollbarPosition: newData.length
          ? ScrollbarPositionType.CONTENT
          : ScrollbarPositionType.CANVAS
      },
      frozen: {}
    }
    // 列宽设置
    s2Options.style = this.configStyle(chart, s2DataConfig)
    // 行列冻结
    if (tableCell.tableFreeze) {
      s2Options.frozen.colCount = tableCell.tableColumnFreezeHead ?? 0
      s2Options.frozen.rowCount = tableCell.tableRowFreezeHead ?? 0
    }
    // tooltip
    this.configTooltip(chart, s2Options)
    // 隐藏表头，保留顶部的分割线, 禁用表头横向 resize
    if (tableHeader.showTableHeader === false) {
      s2Options.style.colCell.height = 1
      if (tableCell.showHorizonBorder === false) {
        s2Options.style.colCell.height = 0
      }
      s2Options.interaction.resize = {
        colCellVertical: false
      }
      s2Options.colCell = (node, sheet, config) => {
        node.label = ' '
        return new TableColCell(node, sheet, config)
      }
    } else {
      // header interaction
      chart.container = container
      this.configHeaderInteraction(chart, s2Options)
    }
    // 配置总计和序号列
    this.configSummaryRowAndIndex(chart, pageInfo, s2Options, s2DataConfig)
    // 开始渲染
    const newChart = new TableSheet(containerDom, s2DataConfig, s2Options)
    // 总计紧贴在单元格后面
    summaryRowStyle(newChart, newData, tableCell, tableHeader, basicStyle.showSummary)
    // 自适应铺满
    if (basicStyle.tableColumnMode === 'adapt') {
      newChart.on(S2Event.LAYOUT_RESIZE_COL_WIDTH, () => {
        newChart.store.set('lastLayoutResult', newChart.facet.getLayoutResult())
      })
      newChart.on(S2Event.LAYOUT_AFTER_HEADER_LAYOUT, (ev: LayoutResult) => {
        const lastLayoutResult = newChart.store.get('lastLayoutResult') as LayoutResult
        if (lastLayoutResult) {
          // 拖动表头 resize
          const widthByField = newChart.options.style?.colCell?.widthByField
          const lastLayoutWidthMap: Record<string, number> =
            lastLayoutResult?.colLeafNodes.reduce((p, n) => {
              p[n.field] = widthByField?.[n.field] ?? n.width
              return p
            }, {}) || {}
          const totalWidth = ev.colLeafNodes.reduce((p, n) => {
            n.width = lastLayoutWidthMap[n.field] || n.width
            n.x = p
            return p + n.width
          }, 0)
          ev.colsHierarchy.width = totalWidth
          newChart.store.set('lastLayoutResult', undefined)
          return
        }
        const containerWidth = containerDom.getBoundingClientRect().width - 1
        const scale = containerWidth / ev.colsHierarchy.width
        if (scale <= 1) {
          // 图库计算的布局宽度已经大于等于容器宽度，不需要再扩大，但是需要处理非整数宽度值，不然会出现透明细线
          ev.colLeafNodes.reduce((p, n) => {
            n.width = Math.round(n.width)
            n.x = p
            return p + n.width
          }, 0)
          return
        }
        const totalWidth = ev.colLeafNodes.reduce((p, n) => {
          n.width = Math.round(n.width * scale)
          n.x = p
          return p + n.width
        }, 0)
        // 从最后一列减掉
        const lastNode = ev.colLeafNodes[ev.colLeafNodes.length - 1]
        if (totalWidth > containerWidth) {
          lastNode.width = Math.floor(lastNode.width - (totalWidth - containerWidth))
        }
        ev.colsHierarchy.width = lastNode.x + lastNode.width
      })
    }
    configEmptyDataStyle(newChart, basicStyle, newData, container)
    // click
    newChart.on(S2Event.DATA_CELL_CLICK, ev => {
      const cell = newChart.getCell(ev.target)
      const meta = cell.getMeta() as ViewMeta
      const nameIdMap = fields.reduce((pre, next) => {
        pre[next['dataeaseName']] = next['id']
        return pre
      }, {})

      const rowData = newChart.dataSet.getRowData(meta)
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
    // tooltip
    const { show } = tooltip
    if (show) {
      newChart.on(S2Event.COL_CELL_HOVER, event => this.showTooltip(newChart, event, meta))
      newChart.on(S2Event.DATA_CELL_HOVER, event => this.showTooltip(newChart, event, meta))
    }
    // header resize
    newChart.on(S2Event.LAYOUT_RESIZE_COL_WIDTH, ev => resizeAction(ev))
    // right click
    newChart.on(S2Event.GLOBAL_CONTEXT_MENU, event => copyContent(newChart, event, meta))
    // touch
    this.configTouchEvent(newChart, drawOption, meta)
    // right click
    newChart.once(S2Event.LAYOUT_AFTER_RENDER, () => {
      newChart.getCanvasElement().addEventListener('contextmenu', e => {
        e.preventDefault()
      })
    })
    // theme
    const customTheme = this.configTheme(chart)
    newChart.setThemeCfg({ theme: customTheme })

    return newChart
  }

  protected configSummaryRowAndIndex(
    chart: Chart,
    pageInfo: PageInfo,
    s2Options: S2Options,
    s2DataConfig: S2DataConfig
  ) {
    const { tableHeader, basicStyle } = parseJson(chart.customAttr)
    const { showSummary, summaryLabel } = basicStyle
    const data = s2DataConfig.data
    const { xAxis, yAxis } = chart
    if (showSummary && data?.length) {
      // 设置汇总行高度和表头一致
      const heightByField = {}
      heightByField[data.length] = tableHeader.tableTitleHeight
      s2Options.style.rowCell = { heightByField }
      // 计算汇总加入到数据里，冻结最后一行
      s2Options.frozen.trailingRowCount = 1
      const summaryObj = getSummaryRow(data, yAxis, basicStyle.seriesSummary) as any
      data.push(summaryObj)
    }
    s2Options.dataCell = (viewMeta, sheet) => {
      // 总计行处理
      if (showSummary && viewMeta.rowIndex === data.length - 1) {
        if (viewMeta.colIndex === 0) {
          if (tableHeader.showIndex || xAxis?.length) {
            viewMeta.fieldValue = summaryLabel ?? t('chart.total_show')
          }
        }
        return new SummaryCell(viewMeta, sheet)
      }
      if (viewMeta.colIndex === 0 && s2Options.seriesNumber?.enable) {
        viewMeta.fieldValue = pageInfo.pageSize * (pageInfo.currentPage - 1) + viewMeta.rowIndex + 1
      }
      return new CustomDataCell(viewMeta, sheet)
    }
  }

  constructor() {
    super('table-normal', [])
  }
}
