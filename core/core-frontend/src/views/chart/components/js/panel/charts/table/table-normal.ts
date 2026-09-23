import { useI18n } from '@/hooks/web/useI18n'
import { formatterItem, valueFormatter } from '@/views/chart/components/js/formatter'
import {
  configEmptyDataStyle,
  copyContent,
  CustomDataCell,
  getSummaryRow,
  SortTooltip,
  SummaryCell,
  getLeafNodes,
  getColumns,
  summaryRowStyle,
  calcTreeWidth,
  getStartPosition,
  isNumeric,
  CustomTableColCell,
  reserveTableRightBorderWidth,
  getRowIndex
} from '@/views/chart/components/js/panel/common/common_table'
import { S2ChartView, S2DrawOptions } from '@/views/chart/components/js/panel/types/impl/s2'
import { hexColorToRGBA, isAlphaColor, parseJson } from '@/views/chart/components/js/util'
import {
  type LayoutResult,
  S2DataConfig,
  S2Event,
  S2Options,
  S2Theme,
  ScrollbarPositionType,
  SERIES_NUMBER_FIELD,
  TableColCell,
  TableSheet,
  ViewMeta
} from '@antv/s2'
import { isEqual, merge } from 'lodash-es'
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
      'showTableHeader',
      'headerGroup'
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
      'tableRowFreezeHead',
      'mergeCells'
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
    const customAttr = parseJson(chart.customAttr)
    if (customAttr.basicStyle.tableColumnMode === 'colAdapt') {
      customAttr.basicStyle.tableColumnMode = 'adapt'
    }
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
    const drillFieldMap = {}
    if (chart.drill) {
      // 下钻过滤字段
      const filterFields = chart.drillFilters.map(i => i.fieldId)
      // 下钻入口的字段下标
      const drillFieldId = chart.drillFields[0].id
      const drillFieldIndex = chart.xAxis.findIndex(ele => ele.id === drillFieldId)
      // 当前下钻字段
      const curDrillFieldId = chart.drillFields[filterFields.length].id
      const curDrillField = fields.find(ele => ele.id === curDrillFieldId)
      filterFields.push(curDrillFieldId)
      // 移除下钻字段，把当前下钻字段插入到下钻入口位置
      fields = fields.filter(ele => {
        return !filterFields.includes(ele.id)
      })
      drillFieldMap[curDrillField.dataeaseName] = chart.drillFields[0].dataeaseName
      fields.splice(drillFieldIndex, 0, curDrillField)
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
          if (![2, 3, 4].includes(f.deType) || !isNumeric(value)) {
            return value
          }
          let formatCfg = f.formatterCfg
          if (!formatCfg) {
            formatCfg = formatterItem
          }
          return valueFormatter(value, formatCfg)
        },
        id: ele.id
      })
    })
    const { basicStyle, tableCell, tableHeader, tooltip } = parseJson(chart.customAttr)
    // 表头分组
    const { headerGroup, showTableHeader } = tableHeader
    if (headerGroup && showTableHeader !== false) {
      const { headerGroupConfig } = tableHeader
      if (headerGroupConfig?.columns?.length) {
        const allKeys = columns.map(c => drillFieldMap[c] || c)
        const leafNodes = getLeafNodes(headerGroupConfig.columns as ColumnNode[])
        const leafKeys = leafNodes.map(c => c.key)
        if (isEqual(leafKeys, allKeys)) {
          if (Object.keys(drillFieldMap).length) {
            const originField = Object.values(drillFieldMap)[0]
            const drillField = Object.keys(drillFieldMap)[0]
            const [drillCol] = getColumns([originField], headerGroupConfig.columns as ColumnNode[])
            drillCol.key = drillField
          }
          columns.splice(0, columns.length, ...headerGroupConfig.columns)
          meta.push(...headerGroupConfig.meta)
        }
      }
    }
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

    // options
    const s2Options: S2Options = {
      width: containerDom.getBoundingClientRect().width,
      height: containerDom.offsetHeight,
      showSeriesNumber: tableHeader.showIndex,
      conditions: this.configConditions(chart),
      tooltip: {
        getContainer: () => containerDom,
        renderTooltip: sheet => new SortTooltip(sheet)
      },
      interaction: {
        hoverHighlight: !(basicStyle.showHoverStyle === false),
        scrollbarPosition: newData.length
          ? ScrollbarPositionType.CONTENT
          : ScrollbarPositionType.CANVAS,
        hoverFocus: false
      }
    }
    // 列宽设置
    s2Options.style = this.configStyle(chart, s2DataConfig)
    // 行列冻结（开启单元格合并时冻结失效）
    if (tableCell.tableFreeze && !tableCell.mergeCells) {
      s2Options.frozenColCount = tableCell.tableColumnFreezeHead ?? 0
      s2Options.frozenRowCount = tableCell.tableRowFreezeHead ?? 0
    }
    // tooltip
    this.configTooltip(chart, s2Options)
    // 合并单元格
    this.configMergeCells(chart, s2Options, s2DataConfig)
    // 隐藏表头，保留顶部的分割线, 禁用表头横向 resize
    if (tableHeader.showTableHeader === false) {
      s2Options.style.colCfg.height = 1
      if (tableCell.showHorizonBorder === false) {
        s2Options.style.colCfg.height = 0
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
      s2Options.colCell = (node, sheet, config) => {
        // 配置文本自动换行参数（开启单元格合并时禁用自动换行）
        node.autoWrap = tableCell.mergeCells ? false : basicStyle.autoWrap
        node.maxLines = basicStyle.maxLines
        return new CustomTableColCell(node, sheet, config)
      }
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
        newChart.store.set('lastLayoutResult', newChart.facet.layoutResult)
      })
      newChart.on(S2Event.LAYOUT_AFTER_HEADER_LAYOUT, (ev: LayoutResult) => {
        const lastLayoutResult = newChart.store.get('lastLayoutResult') as LayoutResult
        if (lastLayoutResult) {
          // 拖动表头 resize
          const widthByFieldValue = newChart.options.style?.colCfg?.widthByFieldValue
          const lastLayoutWidthMap: Record<string, number> =
            lastLayoutResult?.colLeafNodes.reduce((p, n) => {
              p[n.value] = widthByFieldValue?.[n.value] ?? n.width
              return p
            }, {}) || {}
          const totalWidth = ev.colLeafNodes.reduce((p, n) => {
            n.width = lastLayoutWidthMap[n.value] || n.width
            n.x = p
            return p + n.width
          }, 0)
          // 处理分组的单元格，宽度为所有叶子节点之和
          ev.colNodes.forEach(n => {
            if (n.colIndex === -1) {
              n.width = calcTreeWidth(n)
              n.x = getStartPosition(n)
            }
          })
          ev.colsHierarchy.width = totalWidth + 1
          newChart.store.set('lastLayoutResult', undefined)
          return
        }
        const containerWidth = containerDom.getBoundingClientRect().width
        // 预留 1px 给最右侧边框，避免边框被裁剪
        const availableWidth = containerWidth - 1
        const scale = availableWidth / ev.colsHierarchy.width
        if (scale <= 1) {
          // 图库计算的布局宽度已经大于等于容器宽度，不需要再扩大，但是需要处理非整数宽度值，不然会出现透明细线
          ev.colLeafNodes.reduce((p, n) => {
            n.width = Math.round(n.width)
            n.x = p
            return p + n.width
          }, 0)
          ev.colsHierarchy.width = ev.colLeafNodes.reduce((p, n) => p + n.width, 0) + 1
          return
        }
        const totalWidth = ev.colLeafNodes.reduce((p, n) => {
          n.width = Math.round(n.width * scale)
          n.x = p
          return p + n.width
        }, 0)
        // 处理分组的单元格，宽度为所有叶子节点之和
        ev.colNodes.forEach(n => {
          if (n.colIndex === -1) {
            n.width = calcTreeWidth(n)
            n.x = getStartPosition(n)
          }
        })
        if (totalWidth > availableWidth) {
          // 从最后一列减掉
          ev.colLeafNodes[ev.colLeafNodes.length - 1].width -= totalWidth - availableWidth
        }
        ev.colsHierarchy.width = containerWidth
      })
    }
    if (basicStyle.tableColumnMode === 'field') {
      newChart.on(S2Event.LAYOUT_AFTER_HEADER_LAYOUT, (ev: LayoutResult) => {
        reserveTableRightBorderWidth(ev, containerDom.getBoundingClientRect().width)
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
    // 合并的单元格直接复用数据单元格的事件
    newChart.on(S2Event.MERGED_CELLS_CLICK, e => newChart.emit(S2Event.DATA_CELL_CLICK, e))
    // tooltip
    const { show } = tooltip
    if (show) {
      newChart.on(S2Event.COL_CELL_HOVER, event => this.showTooltip(newChart, event, meta))
      newChart.on(S2Event.DATA_CELL_HOVER, event => this.showTooltip(newChart, event, meta))
      newChart.on(S2Event.MERGED_CELLS_HOVER, event => this.showTooltip(newChart, event, meta))
      // touch
      this.configTouchEvent(newChart, drawOption, meta)
    }
    // header resize
    newChart.on(S2Event.LAYOUT_RESIZE_COL_WIDTH, ev => resizeAction(ev))
    // right click
    newChart.on(S2Event.GLOBAL_CONTEXT_MENU, event => copyContent(newChart, event, meta))
    // theme
    const customTheme = this.configTheme(chart)
    newChart.setThemeCfg({ theme: customTheme })

    return newChart
  }

  protected configTheme(chart: Chart): S2Theme {
    const theme = super.configTheme(chart)
    const { basicStyle, tableHeader, tableCell } = parseJson(chart.customAttr)
    // 合并单元格主题配置
    if (tableCell.mergeCells) {
      const tableFontColor = hexColorToRGBA(tableCell.tableFontColor, basicStyle.alpha)
      let tableItemBgColor = tableCell.tableItemBgColor
      if (!isAlphaColor(tableItemBgColor)) {
        tableItemBgColor = hexColorToRGBA(tableItemBgColor, basicStyle.alpha)
      }
      const { tableBorderColor } = basicStyle
      const { tableItemAlign, tableItemFontSize } = tableCell
      const fontStyle = tableCell.isItalic ? 'italic' : 'normal'
      const fontWeight = tableCell.isBolder === false ? 'normal' : 'bold'
      const mergeCellTheme: S2Theme = {
        dataCell: {
          cell: {
            crossBackgroundColor: tableItemBgColor
          }
        },
        mergedCell: {
          cell: {
            backgroundColor: tableItemBgColor,
            crossBackgroundColor: tableItemBgColor,
            horizontalBorderColor: tableBorderColor,
            verticalBorderColor: tableBorderColor,
            horizontalBorderWidth: tableCell.showHorizonBorder ? 1 : 0,
            verticalBorderWidth: tableCell.showVerticalBorder ? 1 : 0
          },
          bolderText: {
            fill: tableFontColor,
            textAlign: tableItemAlign,
            fontSize: tableItemFontSize,
            fontStyle,
            fontWeight
          },
          text: {
            fill: tableFontColor,
            textAlign: tableItemAlign,
            fontSize: tableItemFontSize,
            fontStyle,
            fontWeight
          },
          measureText: {
            fill: tableFontColor,
            textAlign: tableItemAlign,
            fontSize: tableItemFontSize,
            fontStyle,
            fontWeight
          },
          seriesText: {
            fill: tableFontColor,
            textAlign: tableItemAlign,
            fontSize: tableItemFontSize,
            fontStyle,
            fontWeight
          }
        }
      }
      merge(theme, mergeCellTheme)
    }
    if (tableCell.tableItemAlign === 'custom') {
      const { alignConfig } = tableCell
      const alignMap = alignConfig.reduce((p, n) => {
        p[n.id] = n.align
        return p
      }, {})
      merge(theme, {
        dataCellAlignConfig: alignMap
      })
    }
    if (tableHeader.tableHeaderAlign === 'custom') {
      const { alignConfig } = tableHeader
      const alignMap = alignConfig.reduce((p, n) => {
        p[n.id] = n.align
        return p
      }, {})
      merge(theme, {
        colCellAlignConfig: alignMap
      })
    }
    return theme
  }

  protected configSummaryRowAndIndex(
    chart: Chart,
    pageInfo: PageInfo,
    s2Options: S2Options,
    s2DataConfig: S2DataConfig
  ) {
    const { tableHeader, basicStyle, tableCell } = parseJson(chart.customAttr)
    // 开启序号之后，第一列就是序号列，修改 label 即可
    if (s2Options.showSeriesNumber) {
      let indexLabel = tableHeader.indexLabel
      if (!indexLabel) {
        indexLabel = ''
      }
      s2Options.layoutCoordinate = (_, __, col) => {
        if (col.colIndex === 0 && col.rowIndex === 0) {
          col.label = indexLabel
          col.value = indexLabel
        }
      }
    }
    const { showSummary, summaryLabel } = basicStyle
    const data = s2DataConfig.data
    const { xAxis, yAxis } = chart
    if (showSummary && data?.length) {
      // 设置汇总行高度和表头一致
      const heightByField = {}
      heightByField[data.length] = tableHeader.tableTitleHeight
      s2Options.style.rowCfg = { heightByField }
      // 计算汇总加入到数据里，冻结最后一行
      s2Options.frozenTrailingRowCount = 1
      const summaryObj = getSummaryRow(
        data,
        yAxis,
        basicStyle.seriesSummary,
        chart.data.customSumResult
      ) as any
      // 同步首列汇总标签到 summaryObj 中
      const defaultTotalLabel = summaryLabel ?? t('chart.total_show')
      if (tableHeader.showIndex) {
        summaryObj[SERIES_NUMBER_FIELD] = defaultTotalLabel
      } else if (xAxis?.length) {
        summaryObj[xAxis[0].dataeaseName] = defaultTotalLabel
      }
      data.push(summaryObj)
    }
    // 提取已合并单元格坐标映射，用于标记底层数据格
    const { mergeCells } = tableCell
    const mergedCellsInfoMap: Record<string, boolean> = {}
    if (mergeCells) {
      s2Options.mergedCellsInfo?.reduce((p, n) => {
        n.forEach(cell => {
          p[`${cell.rowIndex}-${cell.colIndex}`] = true
        })
        return p
      }, mergedCellsInfoMap)
    }
    s2Options.dataCell = viewMeta => {
      // 总计行处理
      if (showSummary && viewMeta.rowIndex === data.length - 1) {
        if (viewMeta.colIndex === 0) {
          if (tableHeader.showIndex || xAxis?.length) {
            viewMeta.fieldValue = summaryLabel ?? t('chart.total_show')
            viewMeta.isSummaryLabel = true
          }
        }
        return new SummaryCell(viewMeta, viewMeta?.spreadsheet)
      }
      if (viewMeta.colIndex === 0 && s2Options.showSeriesNumber) {
        if (tableCell.mergeCells) {
          viewMeta.fieldValue = getRowIndex(s2Options.mergedCellsInfo, viewMeta)
        } else {
          viewMeta.fieldValue =
            pageInfo.pageSize * (pageInfo.currentPage - 1) + viewMeta.rowIndex + 1
        }
      }
      // 配置文本自动换行参数
      viewMeta.autoWrap = tableCell.mergeCells ? false : basicStyle.autoWrap
      viewMeta.maxLines = basicStyle.maxLines
      // 合并单元格标记，被合并覆盖的底层普通单元格不重复绘制文本与背景
      if (mergeCells && mergedCellsInfoMap[`${viewMeta.rowIndex}-${viewMeta.colIndex}`]) {
        viewMeta.isMergedCell = true
      }
      return new CustomDataCell(viewMeta, viewMeta?.spreadsheet)
    }
  }

  constructor() {
    super('table-normal', [])
  }
}
