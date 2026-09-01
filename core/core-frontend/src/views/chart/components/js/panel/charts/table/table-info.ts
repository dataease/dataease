import {
  type LayoutResult,
  S2DataConfig,
  S2Event,
  S2Options,
  S2Theme,
  ScrollbarPositionType,
  TableColCell,
  TableDataCell,
  ViewMeta,
  TableSheet
} from '@antv/s2'
import { formatterItem, valueFormatter } from '../../../formatter'
import { hexColorToRGBA, isAlphaColor, parseJson } from '../../../util'
import { S2ChartView, S2DrawOptions } from '../../types/impl/s2'
import { TABLE_EDITOR_PROPERTY, TABLE_EDITOR_PROPERTY_INNER } from './common'
import { useI18n } from '@/hooks/web/useI18n'
import { filter, isEqual, isNumber, merge } from 'lodash-es'
import {
  copyContent,
  CustomDataCell,
  getRowIndex,
  SortTooltip,
  summaryRowStyle,
  getLeafNodes,
  getColumns,
  drawImage,
  getSummaryRow,
  SummaryCell,
  mapKeyToField,
  setupColumnTitle,
  calcTreeWidth,
  getStartPosition,
  getS2Renderer
} from '@/views/chart/components/js/panel/common/common_table'

const { t } = useI18n()

class ImageCell extends TableDataCell {
  drawTextShape(): void {
    drawImage.apply(this)
  }
}

type TableHeaderAlign = Exclude<ChartTableHeaderAttr['tableHeaderAlign'], 'custom'>
type TableHeaderTheme = S2Theme & {
  colCellAlignConfig?: Record<string, TableHeaderAlign>
}

class CustomTableColCell extends TableColCell {
  protected getTextStyle() {
    const textStyle = super.getTextStyle()
    const alignConfig = (this.theme as TableHeaderTheme).colCellAlignConfig
    if (alignConfig) {
      if (this.meta.children?.length) {
        textStyle.textAlign = 'center'
        return textStyle
      }
      const align = alignConfig[this.meta.field]
      if (align) {
        textStyle.textAlign = align
      }
    }
    if (textStyle.textAlign === 'custom') {
      textStyle.textAlign = 'left'
    }
    return textStyle
  }
}
/**
 * 明细表
 */
export class TableInfo extends S2ChartView<TableSheet> {
  properties = TABLE_EDITOR_PROPERTY
  propertyInner = {
    ...TABLE_EDITOR_PROPERTY_INNER,
    'table-header-selector': [
      ...TABLE_EDITOR_PROPERTY_INNER['table-header-selector'],
      'tableHeaderSort',
      'showTableHeader',
      'headerGroup'
    ],
    'basic-style-selector': [
      'tableColumnMode',
      'tableBorderColor',
      'tableScrollBarColor',
      'alpha',
      'tablePageMode',
      'showHoverStyle',
      'autoWrap'
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
  axis: AxisType[] = ['xAxis', 'filter', 'drill']
  axisConfig: AxisConfig = {
    xAxis: {
      name: `${t('chart.drag_block_table_data_column')} / ${t('chart.dimension_or_quota')}`
    }
  }

  public drawChart(drawOption: S2DrawOptions<TableSheet>): TableSheet {
    const { container, chart, pageInfo, action, resizeAction } = drawOption
    const containerDom = document.getElementById(container)
    if (!containerDom) return

    // fields
    let fields = chart.data?.fields ?? []
    const columns = []
    const meta = []
    const displayFieldSet = new Set<string>()
    const axisMap = chart.xAxis.reduce((pre, cur) => {
      pre[cur.dataeaseName] = cur
      return pre
    }, {})
    const drillFieldMap: Record<string, string> = {}
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
    fields.forEach(ele => {
      const f = axisMap[ele.dataeaseName]
      if (f?.hide === true) {
        return
      }
      // 同一字段可能同时参与指标和动态值计算，明细中只保留一个展示列
      if (displayFieldSet.has(ele.dataeaseName)) {
        return
      }
      displayFieldSet.add(ele.dataeaseName)
      columns.push({ field: ele.dataeaseName, title: ele.chartShowName ?? ele.name })
      meta.push({
        field: ele.dataeaseName,
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
    const { basicStyle, tableCell, tableHeader, tooltip } = parseJson(chart.customAttr)
    // 表头分组
    const { headerGroup, showTableHeader } = tableHeader
    if (headerGroup && showTableHeader !== false) {
      const { headerGroupConfig } = tableHeader
      if (headerGroupConfig?.columns?.length) {
        // 存量配置转换
        if (headerGroupConfig.columns[0].key) {
          mapKeyToField(headerGroupConfig.columns as unknown as ColumnNode[])
        }
        const nameMap =
          chart.xAxis?.reduce((pre, cur) => {
            pre[cur.dataeaseName] = cur.name
            return pre
          }, {}) || {}
        if (headerGroupConfig.meta?.length) {
          headerGroupConfig.meta.forEach(m => {
            nameMap[m.field] = m.name
          })
        }
        setupColumnTitle(headerGroupConfig.columns as unknown as ColumnNode[], nameMap)
        const allKeys = columns.map(c => drillFieldMap[c.field] || c.field)
        const leafNodes = getLeafNodes(headerGroupConfig.columns as ColumnNode[])
        const leafKeys = leafNodes.map(c => c.field)
        if (isEqual(leafKeys, allKeys)) {
          if (Object.keys(drillFieldMap).length) {
            const drillNameMap =
              chart.drillFields?.reduce((pre, cur) => {
                pre[cur.dataeaseName] = cur.chartShowName || cur.name
                return pre
              }, {}) || {}
            const originField = Object.values(drillFieldMap)[0]
            const drillField = Object.keys(drillFieldMap)[0]
            const [drillCol] = getColumns([originField], headerGroupConfig.columns as ColumnNode[])
            drillCol.field = drillField
            drillCol.title = drillNameMap[drillField] ?? drillCol.title
          }
          columns.splice(0, columns.length, ...headerGroupConfig.columns)
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
      width: containerDom.offsetWidth,
      height: containerDom.offsetHeight,
      seriesNumber: {
        enable: tableHeader.showIndex,
        text: tableHeader.indexLabel ?? t('relation.index')
      },
      conditions: this.configConditions(
        chart,
        meta.map(item => item.field),
        drillFieldMap
      ),
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
      frozen: {},
      placeholder: {
        empty: {
          icon: '',
          description: t('data_set.no_data')
        }
      },
      transformCanvasConfig() {
        return {
          supportsCSSTransform: true
        }
      }
    }
    s2Options.style = this.configStyle(chart, s2DataConfig)
    // 自适应列宽模式下，URL 字段的宽度固定为 120
    if (basicStyle.tableColumnMode === 'adapt') {
      const urlFields = fields.filter(
        field => field.deType === 7 && !axisMap[field.dataeaseName]?.hide
      )
      s2Options.style.colCell.widthByField = urlFields?.reduce((p, n) => {
        p[n.dataeaseName] = 120
        return p
      }, {})
    }
    if (tableCell.tableFreeze && !tableCell.mergeCells) {
      s2Options.frozen.colCount = tableCell.tableColumnFreezeHead ?? 0
      s2Options.frozen.rowCount = tableCell.tableRowFreezeHead ?? 0
    }
    // tooltip
    this.configTooltip(chart, s2Options)
    // svg renderer
    this.configRenderer(s2Options)
    // 合并单元格
    this.configMergeCells(chart, s2Options, s2DataConfig)
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
      s2Options.colCell = (node, sheet, config) => {
        return new CustomTableColCell(node, sheet, config)
      }
    }
    // 序列号和总计行
    this.configSummaryRowAndIndex(chart, pageInfo, s2Options, s2DataConfig)
    // 开启自动换行
    if (basicStyle.autoWrap && !tableCell.mergeCells) {
      const autoWrapStyle = {
        maxLines: basicStyle.maxLines,
        wordWrap: true,
        textOverflow: 'ellipsis'
      }
      s2Options.style.dataCell = {
        ...s2Options.style.dataCell,
        ...autoWrapStyle
      }
      s2Options.style.colCell = {
        ...s2Options.style.colCell,
        ...autoWrapStyle
      }
    }
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
          // 处理分组的单元格，宽度为所有叶子节点之和
          ev.colNodes.forEach(n => {
            if (n.colIndex === -1) {
              n.width = calcTreeWidth(n)
              n.x = getStartPosition(n)
            }
          })
          ev.colsHierarchy.width = totalWidth
          newChart.store.set('lastLayoutResult', undefined)
          return
        }
        // 第一次渲染初始化，把图片字段固定为 120 进行计算
        const urlFields = fields
          .filter(field => field.deType === 7 && !axisMap[field.dataeaseName]?.hide)
          .map(f => f.dataeaseName)
        const totalWidthWithImg = ev.colLeafNodes.reduce((p, n) => {
          return p + (urlFields.includes(n.field) ? 120 : n.width)
        }, 0)
        const containerWidth = containerDom.offsetWidth - 1
        if (containerWidth <= totalWidthWithImg) {
          // 图库计算的布局宽度已经大于等于容器宽度，不需要再扩大，但是需要处理非整数宽度值，不然会出现透明细线
          ev.colLeafNodes.reduce((p, n) => {
            n.width = Math.round(n.width)
            n.x = p
            return p + n.width
          }, 0)
          return
        }
        // 图片字段固定 120, 剩余宽度按比例均摊到其他字段进行扩大
        const totalWidthWithoutImg = ev.colLeafNodes.reduce((p, n) => {
          return p + (urlFields.includes(n.field) ? 0 : n.width)
        }, 0)
        const restWidth = containerWidth - urlFields.length * 120
        const scale = restWidth / totalWidthWithoutImg
        const totalWidth = ev.colLeafNodes.reduce((p, n) => {
          n.width = urlFields.includes(n.field) ? 120 : Math.round(n.width * scale)
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
        const lastNode = ev.colLeafNodes[ev.colLeafNodes.length - 1]
        if (totalWidth > containerWidth) {
          lastNode.width = Math.floor(lastNode.width - (totalWidth - containerWidth))
        }
        ev.colsHierarchy.width = lastNode?.x + lastNode?.width
      })
    }
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
          sourceType: 'table-info',
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

  protected configTheme(chart: Chart): S2Theme {
    const theme = super.configTheme(chart)
    const { basicStyle, tableCell, tableHeader } = parseJson(chart.customAttr)
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
      const alignMap = (alignConfig ?? []).reduce((p, n) => {
        p[n.id] = n.align
        return p
      }, {})
      merge(theme, {
        dataCellAlignConfig: alignMap
      })
    }
    if (tableHeader.tableHeaderAlign === 'custom') {
      // 将样式面板中的字段级配置交给自定义表头单元格消费
      const tableHeaderTheme = theme as TableHeaderTheme
      tableHeaderTheme.colCellAlignConfig = (tableHeader.alignConfig ?? []).reduce((pre, cur) => {
        pre[cur.id] = cur.align
        return pre
      }, {} as Record<string, TableHeaderAlign>)
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
    const fields = chart.data?.fields ?? []
    const { showSummary, summaryLabel } = basicStyle
    const data = s2DataConfig.data
    const xAxis = chart.xAxis
    if (showSummary && data?.length) {
      // 设置汇总行高度和表头一致
      const heightByField = {}
      heightByField[data.length] = tableHeader.tableTitleHeight
      s2Options.style.rowCell = { heightByField }
      // 计算汇总加入到数据里，冻结最后一行
      s2Options.frozen.trailingRowCount = 1
      const axis = filter(xAxis, axis => [2, 3, 4].includes(axis.deType))
      const summaryObj = getSummaryRow(
        data,
        axis,
        basicStyle.seriesSummary,
        chart.data.customSumResult
      ) as any
      data.push(summaryObj)
    }
    s2Options.dataCell = (viewMeta, sheet) => {
      // 总计行处理
      if (showSummary && viewMeta.rowIndex === data.length - 1) {
        if (viewMeta.colIndex === 0) {
          if (tableHeader.showIndex) {
            viewMeta.fieldValue = summaryLabel ?? t('chart.total_show')
          } else {
            // 第一列不是数值类型的，显示总计
            if (![2, 3, 4].includes(xAxis?.[0]?.deType)) {
              viewMeta.fieldValue = summaryLabel ?? t('chart.total_show')
            }
          }
        }
        return new SummaryCell(viewMeta, sheet)
      }
      const field = fields.find(f => f.dataeaseName === viewMeta.valueField)
      if (field?.deType === 7 && chart.showPosition !== 'dialog') {
        return new ImageCell(viewMeta, sheet)
      }
      if (viewMeta.colIndex === 0 && s2Options.seriesNumber.enable) {
        if (tableCell.mergeCells) {
          viewMeta.fieldValue = getRowIndex(s2Options.mergedCellsInfo, viewMeta)
        } else {
          viewMeta.fieldValue =
            pageInfo.pageSize * (pageInfo.currentPage - 1) + viewMeta.rowIndex + 1
        }
      }
      return new CustomDataCell(viewMeta, sheet)
    }
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    const customAttr = parseJson(chart.customAttr)
    // 列自适应仅适用于透视表，切换图表类型时恢复普通自适应
    if (customAttr.basicStyle.tableColumnMode === 'colAdapt') {
      customAttr.basicStyle.tableColumnMode = 'adapt'
    }
    return chart
  }

  constructor() {
    super('table-info', [])
  }
}
