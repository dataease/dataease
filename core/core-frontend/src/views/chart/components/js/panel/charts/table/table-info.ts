import {
  type LayoutResult,
  S2DataConfig,
  S2Event,
  S2Options,
  S2Theme,
  ScrollbarPositionType,
  TableColCell,
  TableSheet,
  ViewMeta
} from '@antv/s2'
import { formatterItem, valueFormatter } from '../../../formatter'
import { hexColorToRGBA, isAlphaColor, parseJson } from '../../../util'
import { S2ChartView, S2DrawOptions } from '../../types/impl/s2'
import { TABLE_EDITOR_PROPERTY, TABLE_EDITOR_PROPERTY_INNER } from './common'
import { useI18n } from '@/hooks/web/useI18n'
import { isEqual, isNumber, merge } from 'lodash-es'
import {
  copyContent,
  CustomDataCell,
  CustomTableColCell,
  getRowIndex,
  calculateHeaderHeight,
  SortTooltip,
  configSummaryRow,
  summaryRowStyle,
  configEmptyDataStyle,
  getLeafNodes,
  getColumns
} from '@/views/chart/components/js/panel/common/common_table'

const { t } = useI18n()
class ImageCell extends CustomDataCell {
  protected drawTextShape(): void {
    const img = new Image()
    const { x, y, width, height, fieldValue } = this.meta
    img.src = fieldValue as string
    img.setAttribute('crossOrigin', 'anonymous')
    img.onload = () => {
      !this.cfg.children && (this.cfg.children = [])
      const { width: imgWidth, height: imgHeight } = img
      const ratio = Math.max(imgWidth / width, imgHeight / height)
      // 不铺满，部分留白
      const imgShowWidth = (imgWidth / ratio) * 0.8
      const imgShowHeight = (imgHeight / ratio) * 0.8
      this.textShape = this.addShape('image', {
        attrs: {
          x: x + (imgShowWidth < width ? (width - imgShowWidth) / 2 : 0),
          y: y + (imgShowHeight < height ? (height - imgShowHeight) / 2 : 0),
          width: imgShowWidth,
          height: imgShowHeight,
          img
        }
      })
    }
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
      'autoWrap',
      'showSummary',
      'summaryLabel'
    ],
    'table-cell-selector': [
      ...TABLE_EDITOR_PROPERTY_INNER['table-cell-selector'],
      'tableFreeze',
      'tableColumnFreezeHead',
      'tableRowFreezeHead',
      'mergeCells'
    ]
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

    // fields
    let fields = chart.data?.fields ?? []
    const columns = []
    const meta = []
    const axisMap = chart.xAxis.reduce((pre, cur) => {
      pre[cur.dataeaseName] = cur
      return pre
    }, {})
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
          if (![2, 3].includes(f.deType) || !isNumber(value)) {
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
          : ScrollbarPositionType.CANVAS
      }
    }
    s2Options.style = this.configStyle(chart, s2DataConfig)
    // 自适应列宽模式下，URL 字段的宽度固定为 120
    if (basicStyle.tableColumnMode === 'adapt') {
      const urlFields = fields.filter(
        field => field.deType === 7 && !axisMap[field.dataeaseName]?.hide
      )
      s2Options.style.colCfg.widthByFieldValue = urlFields?.reduce((p, n) => {
        p[n.chartShowName ?? n.name] = 120
        return p
      }, {})
    }
    if (tableCell.tableFreeze && !tableCell.mergeCells) {
      s2Options.frozenColCount = tableCell.tableColumnFreezeHead ?? 0
      s2Options.frozenRowCount = tableCell.tableRowFreezeHead ?? 0
    }
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
    s2Options.dataCell = viewMeta => {
      const field = fields.filter(f => f.dataeaseName === viewMeta.valueField)?.[0]
      if (field?.deType === 7 && chart.showPosition !== 'dialog') {
        return new ImageCell(viewMeta, viewMeta?.spreadsheet)
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
      return new CustomDataCell(viewMeta, viewMeta?.spreadsheet)
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
        // 配置文本自动换行参数
        node.autoWrap = tableCell.mergeCells ? false : basicStyle.autoWrap
        node.maxLines = basicStyle.maxLines
        return new CustomTableColCell(node, sheet, config)
      }
    }
    // 总计
    configSummaryRow(chart, s2Options, newData, tableHeader, basicStyle, basicStyle.showSummary)
    // 开始渲染
    const newChart = new TableSheet(containerDom, s2DataConfig, s2Options)
    // 总计紧贴在单元格后面
    summaryRowStyle(newChart, newData, tableCell, tableHeader, basicStyle.showSummary)
    // 开启自动换行
    if (basicStyle.autoWrap && !tableCell.mergeCells) {
      // 调整表头宽度时，计算表头高度
      newChart.on(S2Event.LAYOUT_RESIZE_COL_WIDTH, info => {
        calculateHeaderHeight(info, newChart, tableHeader, basicStyle, null)
      })
      newChart.on(S2Event.LAYOUT_AFTER_HEADER_LAYOUT, (ev: LayoutResult) => {
        const maxHeight = newChart.store.get('autoCalcHeight') as number
        if (maxHeight) {
          // 更新列的高度
          ev.colLeafNodes.forEach(n => (n.height = maxHeight))
          ev.colsHierarchy.height = maxHeight
          newChart.store.set('autoCalcHeight', undefined)
        } else {
          if (ev.colLeafNodes?.length) {
            const { value, width } = ev.colLeafNodes[0]
            calculateHeaderHeight(
              { info: { meta: { value }, resizedWidth: width } },
              newChart,
              tableHeader,
              basicStyle,
              ev
            )
          }
        }
      })
    }
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
        const containerWidth = containerDom.getBoundingClientRect().width
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
        if (totalWidth > containerWidth) {
          ev.colLeafNodes[ev.colLeafNodes.length - 1].width -= totalWidth - containerWidth
        }
        ev.colsHierarchy.width = containerWidth
      })
    }
    // 空数据时表格样式
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
    // theme
    const customTheme = this.configTheme(chart)
    newChart.setThemeCfg({ theme: customTheme })
    return newChart
  }

  protected configTheme(chart: Chart): S2Theme {
    const theme = super.configTheme(chart)
    const { basicStyle, tableCell } = parseJson(chart.customAttr)
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
    return theme
  }

  constructor() {
    super('table-info', [])
  }
}

function calcTreeWidth(node) {
  if (!node.children?.length) {
    return node.width
  }
  return node.children.reduce((pre, cur) => {
    return pre + calcTreeWidth(cur)
  }, 0)
}

function getStartPosition(node) {
  if (!node.children?.length) {
    return node.x
  }
  return getStartPosition(node.children[0])
}
