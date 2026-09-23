import { G2ChartView, G2DrawOptions } from '../../../types/impl/g2'
import { flow, hexColorToRGBA, parseJson } from '@/views/chart/components/js/util'
import { defaultsDeep, isEmpty } from 'lodash-es'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { LINE_EDITOR_PROPERTY_INNER } from '../line/common'
import { useI18n } from '@/hooks/web/useI18n'
import { ChartEvent, Chart as G2Chart, G2Spec } from '@antv/g2'
import { registerSymbol, Symbols } from '@antv/g2/esm/utils/marker'
import {
  getG2Renderer,
  handleChartDashboardHidden,
  TOOLTIP_ITEM_TPL,
  TOOLTIP_TITLE_TPL
} from '../../../common/common_antv'
import { createTooltipWrapper, listenerTooltipShow, tooltipCss } from './barUtil'

const { t } = useI18n()
const DEFAULT_DATA = []
// 均线周期
const MOVING_AVERAGE_DAYS = [5, 10, 20, 60, 120, 180]

const getStockThemeContrastColor = chart => {
  const customAttr = parseJson(chart.customAttr)
  return customAttr.basicStyle?.themeContrastColor ?? customAttr.label?.color ?? '#000000'
}

/**
 * 计算实体或影线范围
 */
const getStockRange = (row, yAxis, rangeType: 'body' | 'wick') => {
  const values = yAxis.map(axis => row[axis.dataeaseName])
  if (
    values.some(
      value =>
        value === null || value === undefined || value === '' || !Number.isFinite(Number(value))
    )
  ) {
    return [null, null]
  }
  // 影线取四值极值，实体取中间两值
  const sortedValues = values.map(Number).sort((a, b) => a - b)
  return rangeType === 'wick'
    ? [sortedValues[0], sortedValues[3]]
    : [sortedValues[1], sortedValues[2]]
}

/**
 * 计算 K 线箱体与均线共同的 Y 轴范围
 * 均线补位使用的 null 不参与极值计算
 */
const getStockYDomain = (stockData, avgDataSeries, yAxis) => {
  let domainMin = Infinity
  let domainMax = -Infinity
  const collectDomainValue = value => {
    if (value === null || value === undefined || value === '') {
      return
    }
    const numberValue = Number(value)
    if (Number.isFinite(numberValue)) {
      domainMin = Math.min(domainMin, numberValue)
      domainMax = Math.max(domainMax, numberValue)
    }
  }
  stockData.forEach(row => {
    yAxis.forEach(axis => collectDomainValue(row[axis.dataeaseName]))
  })
  avgDataSeries.forEach(item => collectDomainValue(item.value))
  if (!Number.isFinite(domainMin) || !Number.isFinite(domainMax)) {
    return
  }
  // 极值相等时增加少量上下边距，避免单值数据无法生成有效比例尺
  if (domainMin === domainMax) {
    const padding = Math.abs(domainMin) * 0.05 || 1
    domainMin -= padding
    domainMax += padding
  }
  return [domainMin, domainMax]
}

// 保留完整 mark 数据，只在缩略轴变化后同步当前可见区的 Y 轴范围
const stockSliderFilter = ({ data, avgDataSeries, dimensionField, yAxis }) => {
  return target => {
    // 自定义交互只管当前图表的横向缩略轴
    const slider = Array.from(target.container.getElementsByClassName?.('slider') || []).find(
      (item: any) => item.attributes?.orientation === 'horizontal'
    ) as any
    if (!slider || !data.length) {
      return
    }
    let pendingValues
    let frameId
    let lastRangeKey
    const render = () => {
      frameId = undefined
      if (!pendingValues) {
        return
      }
      const [start = 0, end = 1] = pendingValues
      pendingValues = undefined
      // 将 0 到 1 的滑块比例换算为原始 K 线数据索引
      const startIndex = Math.max(0, Math.min(data.length - 1, Math.floor(start * data.length)))
      const endIndex = Math.max(startIndex + 1, Math.min(data.length, Math.ceil(end * data.length)))
      const rangeKey = `${startIndex}-${endIndex}`
      // 减少拖动中的无效更新
      if (rangeKey === lastRangeKey) {
        return
      }
      lastRangeKey = rangeKey
      const visibleStockData = data.slice(startIndex, endIndex)
      const visibleDomain = visibleStockData.map(row => row[dimensionField])
      const visibleDimensions = new Set(visibleDomain)
      // 只取当前维度区间的均线值参与 Y 轴极值计算，仍保留完整 mark 数据
      const visibleAvgData = avgDataSeries.filter(item =>
        visibleDimensions.has(item[dimensionField])
      )
      const domain = getStockYDomain(visibleStockData, visibleAvgData, yAxis)
      if (!domain) {
        return
      }
      const patchMarks = marks =>
        marks?.map(mark => ({
          ...mark,
          // 关闭均线形变动画，避免连续更新 domain 时路径过渡叠加
          ...(['line', 'point'].includes(mark.type) ? { animate: false } : {}),
          scale: {
            ...mark.scale,
            x: {
              ...mark.scale?.x,
              domain: visibleDomain,
              nice: false
            },
            y: {
              ...mark.scale?.y,
              key: 'stock-y',
              domain,
              zero: false,
              nice: false
            }
          },
          ...(mark.slider?.x
            ? {
                slider: {
                  ...mark.slider,
                  // 重绘 mark 时保留当前手柄位置，避免缩略轴跳回初始范围
                  x: { ...mark.slider.x, preserve: true }
                }
              }
            : {})
        }))
      target.setState(slider, state => ({
        ...state,
        marks: patchMarks(state.marks),
        children: patchMarks(state.children)
      }))
      target.update()
    }
    const onValueChange = event => {
      pendingValues = event.detail?.value || slider.attributes?.values
      // 拖动过程中每个动画帧最多触发一次图表更新
      if (frameId === undefined) {
        frameId = requestAnimationFrame(render)
      }
    }
    const apply = () => {
      if (frameId !== undefined) {
        cancelAnimationFrame(frameId)
      }
      render()
    }
    slider.addEventListener('valuechange', onValueChange)
    // 松开鼠标时立即应用最后一次范围，确保手柄和图形最终状态一致
    document.addEventListener('pointerup', apply)
    return () => {
      if (frameId !== undefined) {
        cancelAnimationFrame(frameId)
      }
      slider.removeEventListener('valuechange', onValueChange)
      document.removeEventListener('pointerup', apply)
    }
  }
}

/**
 * K线图
 */
export class StockLine extends G2ChartView {
  properties: EditorProperty[] = [
    'background-overall-component',
    'border-style',
    'basic-style-selector',
    'legend-selector',
    'x-axis-selector',
    'y-axis-selector',
    'title-selector',
    'tooltip-selector',
    'function-cfg',
    'jump-set',
    'linkage'
  ]
  propertyInner = {
    ...LINE_EDITOR_PROPERTY_INNER,
    'function-cfg': ['emptyDataStrategy'],
    'y-axis-selector': [
      'name',
      'color',
      'fontSize',
      'position',
      'axisLabel',
      'axisLine',
      'splitLine',
      'axisLabelFormatter'
    ],
    'legend-selector': ['fontSize', 'color', 'show']
  }
  axis: AxisType[] = ['xAxis', 'yAxis', 'filter', 'extLabel', 'extTooltip']
  axisConfig: AxisConfig = {
    xAxis: {
      name: `${t('common.component.date')} / ${t('chart.dimension')}`,
      limit: 1,
      type: 'd'
    },
    yAxis: {
      name: `${t('chart.k_line_yaxis_tip')} / ${t('chart.quota')}`,
      limit: 4,
      type: 'q'
    }
  }

  stockMarker = function (x, y, r) {
    const width = r
    const bodyHalfHeight = r * 0.4
    const wickHalfHeight = r * 0.85
    return [
      // 矩形框
      ['M', x - width - 1 / 2, y - bodyHalfHeight],
      ['L', x + width + 1 / 2, y - bodyHalfHeight],
      ['L', x + width + 1 / 2, y + bodyHalfHeight],
      ['L', x - width - 1 / 2, y + bodyHalfHeight],
      ['Z'],
      // 中线
      ['M', x, y + wickHalfHeight],
      ['L', x, y - wickHalfHeight]
    ]
  }

  RED = '#EF5350'
  GREEN = '#26A29A'
  GREY = '#999999'

  /**
   * 计算收盘价平均值
   * @param data
   * @param dayCount
   * @param chart
   */
  calculateMovingAverage = (data, dayCount, chart) => {
    const xAxis = chart.xAxis
    const yAxis = chart.yAxis
    // 时间字段
    const xAxisDataeaseName = xAxis[0].dataeaseName
    // 收盘价字段
    const yAxisDataeaseName = yAxis[1].dataeaseName
    const result = []
    for (let i = 0; i < data.length; i++) {
      let value = null
      // 第 dayCount 条数据即可形成首个完整均线窗口
      if (i >= dayCount - 1) {
        const values = data.slice(i - dayCount + 1, i + 1).map(item => item[yAxisDataeaseName])
        // 窗口内存在空值或非数值时保持空均线，防止生成错误平均值
        const hasInvalidValue = values.some(
          value =>
            value === null || value === undefined || value === '' || !Number.isFinite(Number(value))
        )
        if (!hasInvalidValue) {
          const sum = values.reduce((sum, value) => sum + Number(value), 0)
          value = parseFloat((sum / dayCount).toFixed(3))
        }
      }
      // 每个日期保留均线占位，空值只用于 tooltip，不参与画线
      result.push({
        series: `MA${dayCount}`,
        [xAxisDataeaseName]: data[i][xAxisDataeaseName],
        value
      })
    }
    return result
  }

  async drawChart(drawOptions: G2DrawOptions<G2Chart>): Promise<G2Chart> {
    const { chart, action, container } = drawOptions
    chart.container = container
    const xAxis = chart.xAxis
    const yAxis = chart.yAxis
    const data = chart.data?.tableRow
    if (!chart.data?.data?.length || !data?.length || !xAxis?.length || yAxis?.length !== 4) {
      return
    }
    const basicStyle = parseJson(chart.customAttr).basicStyle
    const stockStrokeColor = getStockThemeContrastColor(chart)
    const colors = []
    const alpha = basicStyle.alpha
    basicStyle.colors.forEach(ele => {
      colors.push(hexColorToRGBA(ele, alpha))
    })

    // 时间字段
    const dateAxis = xAxis[0].dataeaseName
    // K线图固定按维度升序计算和展示
    data.sort((a, b) => {
      const aValue = a[dateAxis]
      const bValue = b[dateAxis]
      if (aValue === bValue) return 0
      if (aValue == null) return 1
      if (bValue == null) return -1
      return String(aValue).localeCompare(String(bValue), undefined, { numeric: true })
    })

    const initOptions: G2Spec = {
      type: 'view',
      data: {
        value: data
      },
      autoFit: true,
      encode: {
        x: dateAxis
      },
      // 为顶部图例保留少量间距，避免图例标记贴近绘图区
      marginTop: 2,
      scale: {
        color: {
          // 固定颜色域，确保无有效数据的均线仍显示图例
          domain: ['日K', ...MOVING_AVERAGE_DAYS.map(day => `MA${day}`)]
        },
        y: {
          key: '2',
          // 初始渲染与缩略轴重算统一使用整刻度范围
          zero: false,
          nice: true
        }
      },
      children: [
        {
          type: 'interval',
          encode: {
            y: (row): any => getStockRange(row, yAxis, 'wick'),
            color: () => '日K',
            size: 0.5
          },
          tooltip: false
        },
        {
          type: 'interval',
          encode: {
            y: (row): any => getStockRange(row, yAxis, 'body'),
            color: () => '日K'
          },
          slider: {
            x: {
              position: 'bottom',
              style: {
                handleLabelFill: stockStrokeColor,
                handleLabelFillOpacity: 1,
                // 拖动事件时穿透文本到缩略轴
                handleLabelPointerEvents: 'none'
              }
            }
          },
          style: {
            stroke: stockStrokeColor,
            lineWidth: 0.5
          }
        }
      ]
    }
    const newChart = new G2Chart({ container, ...getG2Renderer() })
    const options = this.setupOptions(chart, initOptions)
    handleChartDashboardHidden(chart, options)
    // 开始渲染
    newChart.options(options)
    // K线柱与均线点共用同一份联动参数
    const actionHandler = evt => {
      const selectDate = evt.data.data[dateAxis]
      const paramData = chart.data?.data
      const selectData = paramData.filter(item => item.field === selectDate)
      const quotaList = []
      selectData.forEach(item => {
        quotaList.push({ ...item.quotaList[0], value: item.value })
      })
      if (selectData.length) {
        const param = {
          ...evt,
          data: {
            data: {
              ...evt.data.data,
              value: quotaList[0].value,
              name: selectDate,
              dimensionList: selectData[0].dimensionList,
              quotaList: quotaList
            }
          }
        }
        action(param)
      }
    }
    newChart.on(`interval:${ChartEvent.CLICK}`, actionHandler)
    newChart.on(`point:${ChartEvent.CLICK}`, actionHandler)
    // 默认向上展示，顶部越界时由公共监听切换到鼠标下方
    listenerTooltipShow(newChart, chart)
    return newChart
  }

  protected configAvgLine(chart: Chart, options: G2Spec): G2Spec {
    const avgDataSeries = []
    MOVING_AVERAGE_DAYS.forEach(avgDay => {
      avgDataSeries.push(...this.calculateMovingAverage(options.data.value, avgDay, chart))
    })
    const domain = getStockYDomain(options.data.value, avgDataSeries, chart.yAxis)
    if (domain) {
      options.scale = {
        ...options.scale,
        y: {
          ...options.scale?.y,
          // 所有 K 线与均线 mark 共享同一个比例尺
          key: 'stock-y',
          domain,
          zero: false,
          // 保留实际极值，不让 G2 再扩展或改写 domain
          nice: false
        }
      }
    }
    // 均线 mark 保留 null 占位，保证缺失日期仍可参与共享 tooltip 和图例过滤
    const lineMark = {
      type: 'line',
      data: {
        value: avgDataSeries
      },
      encode: {
        y: 'value',
        color: 'series',
        series: 'series'
      }
    }
    const pointMark = {
      type: 'point',
      data: {
        value: avgDataSeries
      },
      encode: {
        y: 'value',
        color: 'series'
      },
      style: {
        size: 3,
        shape: 'circle'
      },
      tooltip: false
    }
    options.children.push(lineMark, pointMark)
    const [, intervalMark] = options.children
    intervalMark.interaction = {
      ...intervalMark.interaction,
      // 统一更新 X/Y 域
      sliderFilter: false,
      stockSliderFilter: {
        type: stockSliderFilter,
        data: options.data.value,
        avgDataSeries,
        dimensionField: options.encode.x,
        yAxis: chart.yAxis
      }
    }
    return options
  }

  protected configTheme(chart: Chart, options: G2Spec): G2Spec {
    const { basicStyle } = parseJson(chart.customAttr)
    const colors: string[] = [`rgba(255, 0, 0, ${basicStyle.alpha / 100})`]
    basicStyle.colors.forEach(ele => {
      colors.push(hexColorToRGBA(ele, basicStyle.alpha))
    })
    const customStyle = parseJson(chart.customStyle)
    let bgColor
    if (customStyle.background) {
      bgColor = hexColorToRGBA(customStyle.background.color, customStyle.background.alpha)
    }
    const theme = {
      color: colors[0],
      category10: colors,
      category20: colors,
      view: {
        viewFill: bgColor
      }
    }
    return { ...options, theme }
  }

  protected configBasicStyle(chart: Chart, options: G2Spec): G2Spec {
    const { basicStyle } = parseJson(chart.customAttr)
    const stockStrokeColor = getStockThemeContrastColor(chart)
    const [, , lineMark, pointMark] = options.children
    const lineStyleOpt = {
      encode: {
        shape: basicStyle.lineSmooth ? 'smooth' : 'line',
        size: basicStyle.lineWidth
      }
    }
    defaultsDeep(lineMark, lineStyleOpt)
    const pointStyleOpt = {
      encode: {
        shape: basicStyle.lineSymbol,
        size: basicStyle.lineSymbolSize
      }
    }
    if (basicStyle.lineSymbolSize === 0) {
      pointStyleOpt.encode.shape = 'none'
    }
    defaultsDeep(pointMark, pointStyleOpt)
    const [wickMark, intervalMark] = options.children
    const [startAxis, endAxis] = chart.yAxis
    const red = hexColorToRGBA(this.RED, basicStyle.alpha)
    const green = hexColorToRGBA(this.GREEN, basicStyle.alpha)
    const grey = hexColorToRGBA(this.GREY, basicStyle.alpha)
    const wickOpt = {
      style: {
        lineWidth: 0,
        stroke: stockStrokeColor,
        fill: stockStrokeColor
      }
    }
    defaultsDeep(wickMark, wickOpt)
    const intervalOpt = {
      style: {
        fillOpacity: 1,
        fill: d => {
          const offset = d[startAxis.dataeaseName] - d[endAxis.dataeaseName]
          return offset === 0 ? grey : offset > 0 ? green : red
        }
      }
    }
    defaultsDeep(intervalMark, intervalOpt)
    return options
  }

  protected configLegend(chart: Chart, options: G2Spec): G2Spec {
    const { legend } = parseJson(chart.customStyle)
    if (!legend.show) {
      return { ...options, legend: false }
    }
    if (!Symbols.has('stock')) {
      registerSymbol('stock', this.stockMarker)
    }
    const baseLegend = this.getLegend(chart)
    const tmpLegend = {
      legend: {
        color: {
          ...baseLegend,
          orientation: 'horizontal',
          position: 'top',
          layout: {
            justifyContent: 'center',
            alignItems: 'center',
            flexDirection: 'row'
          },
          itemMarkerSize: 12,
          itemMarkerLineWidth: 2,
          itemMarker: d => {
            if (d === '日K') {
              return 'stock'
            }
            return 'hyphen'
          },
          // 增加各均线图例项的水平间距，避免名称和标记拥挤
          colPadding: 20
        }
      }
    }
    defaultsDeep(options, tmpLegend)
    return options
  }

  protected configXAxis(chart: Chart, options: G2Spec): G2Spec {
    const { xAxis } = parseJson(chart.customStyle)
    if (!xAxis.show) {
      const axisHide = {
        axis: {
          x: false
        }
      }
      return defaultsDeep(options, axisHide)
    }
    let lineLineDash = undefined
    if (xAxis.axisLine.lineStyle.style === 'dashed') {
      lineLineDash = [10, 8]
    }
    if (xAxis.axisLine.lineStyle.style === 'dotted') {
      lineLineDash = [1, 2]
    }
    let gridLineDash = [0, 0]
    if (xAxis.splitLine.lineStyle.style === 'dashed') {
      gridLineDash = [10, 8]
    }
    if (xAxis.splitLine.lineStyle.style === 'dotted') {
      gridLineDash = [1, 2]
    }
    const axisStyle = {
      axis: {
        x: {
          position: xAxis.position,
          title: xAxis.nameShow === false ? false : xAxis.name,
          titleFontSize: xAxis.fontSize,
          titleFill: xAxis.color,
          titleOpacity: 1,
          ...this.getAxisLineStyle(chart, xAxis),
          lineLineDash,
          label: xAxis.axisLabel.show,
          labelFill: xAxis.axisLabel.color,
          labelFillOpacity: 1,
          labelOpacity: 1,
          labelFontSize: xAxis.axisLabel.fontSize,
          grid: xAxis.splitLine.show,
          gridStroke: xAxis.splitLine.lineStyle.color,
          gridStrokeOpacity: 1,
          gridLineWidth: xAxis.splitLine.lineStyle.width,
          gridLineDash,
          ...this.getAxisLabelStyle(xAxis),
          transform: [
            {
              type: 'hide',
              keepHeader: true,
              keepTail: true
            }
          ]
        }
      }
    }
    return defaultsDeep(options, axisStyle)
  }

  protected configYAxis(chart: Chart, options: G2Spec): G2Spec {
    const { xAxis, yAxis } = parseJson(chart.customStyle)
    if (!yAxis.show) {
      const axisHide = {
        axis: {
          y: false
        }
      }
      return defaultsDeep(options, axisHide)
    }
    let lineLineDash = undefined
    if (yAxis.axisLine.lineStyle.style === 'dashed') {
      lineLineDash = [10, 8]
    }
    if (yAxis.axisLine.lineStyle.style === 'dotted') {
      lineLineDash = [1, 2]
    }
    let gridLineDash = [0, 0]
    if (yAxis.splitLine.lineStyle.style === 'dashed') {
      gridLineDash = [10, 8]
    }
    if (yAxis.splitLine.lineStyle.style === 'dotted') {
      gridLineDash = [1, 2]
    }
    const axisOption = {
      axis: {
        y: {
          position: yAxis.position,
          title: yAxis.nameShow === false ? false : yAxis.name,
          titleFontSize: yAxis.fontSize,
          titleFill: yAxis.color,
          titleOpacity: 1,
          ...this.getAxisLineStyle(chart, yAxis),
          lineLineDash,
          label: yAxis.axisLabel.show,
          labelFill: yAxis.axisLabel.color,
          labelFillOpacity: 1,
          labelOpacity: 1,
          labelFontSize: yAxis.axisLabel.fontSize,
          grid: yAxis.splitLine.show,
          gridStroke: yAxis.splitLine.lineStyle.color,
          gridStrokeOpacity: 1,
          gridLineWidth: yAxis.splitLine.lineStyle.width,
          gridLineDash,
          ...this.getOverlapGridFilter(xAxis),
          ...this.getAxisLabelStyle(yAxis),
          labelFormatter: d => {
            return valueFormatter(d, yAxis.axisLabelFormatter)
          }
        }
      }
    }
    return defaultsDeep(options, axisOption)
  }

  protected configTooltip(chart: Chart, options: G2Spec): G2Spec {
    const { tooltip: tooltipAttr, basicStyle } = parseJson(chart.customAttr)
    const [, intervalMark, lineMark] = options.children
    if (!tooltipAttr.show) {
      defaultsDeep(lineMark, { tooltip: false })
      defaultsDeep(intervalMark, { tooltip: false })
      return options
    }
    const [openAxis, closeAxis, minAxis, maxAxis] = chart.yAxis
    const yAxisMap = chart.yAxis.reduce((acc, axis) => {
      acc[axis.dataeaseName] = axis
      return acc
    }, {})
    const yAxisKeys = [
      maxAxis.dataeaseName,
      minAxis.dataeaseName,
      closeAxis.dataeaseName,
      openAxis.dataeaseName
    ]
    const maKeys = MOVING_AVERAGE_DAYS.map(day => `MA${day}`)
    // 先建立完整均线 tooltip 占位，实际有值的项目会在 render 中覆盖
    const emptyMaItems = maKeys.map((name, index) => {
      const color = basicStyle.colors[index % basicStyle.colors.length]
      return {
        name,
        value: '-',
        color: color ? hexColorToRGBA(color, basicStyle.alpha) : this.GREY
      }
    })
    const intervalMarkTooltipOptions: G2Spec = {
      tooltip: {
        items: [d => d]
      }
    }
    defaultsDeep(intervalMark, intervalMarkTooltipOptions)
    const lineMarkTooltipOptions: G2Spec = {
      interaction: {
        tooltip: {
          shared: true,
          mount: createTooltipWrapper(chart),
          css: tooltipCss(tooltipAttr),
          render: (_, { title, items }) => {
            const titleHtml = TOOLTIP_TITLE_TPL.replace('{title}', title)
            // K 线明细与固定顺序的均线项目分开整理，避免空均线改变展示顺序
            const stockResult = []
            const maResult = emptyMaItems.map(item => ({ ...item }))
            items.forEach(item => {
              if (maKeys.includes(item.name)) {
                const index = maKeys.indexOf(item.name)
                const value =
                  item.value === null || item.value === undefined
                    ? '-'
                    : valueFormatter(item.value, tooltipAttr.tooltipFormatter)
                maResult[index] = {
                  ...maResult[index],
                  ...item,
                  value,
                  color: item.color || maResult[index].color
                }
              } else {
                if (item.value === null || item.value === undefined) {
                  return
                }
                const offset = item[openAxis.dataeaseName] - item[closeAxis.dataeaseName]
                const color =
                  offset === 0
                    ? hexColorToRGBA(this.GREY, basicStyle.alpha)
                    : offset > 0
                    ? hexColorToRGBA(this.GREEN, basicStyle.alpha)
                    : hexColorToRGBA(this.RED, basicStyle.alpha)
                yAxisKeys.forEach(key => {
                  const axis = yAxisMap[key]
                  const value = valueFormatter(item[key], tooltipAttr.tooltipFormatter)
                  stockResult.unshift({ name: axis.chartShowName ?? axis.name, value, color })
                })
              }
            })
            const stockGroup = stockResult.length
              ? [{ name: '日K', value: '', color: stockResult[0].color }]
              : []
            // G2 不会返回未成形的均线项，渲染前按固定顺序补齐
            const result = [
              ...stockGroup,
              ...stockResult.map(item => ({ ...item, compactMarker: true })),
              ...maResult
            ]
            const itemsHtml = result
              .map(item => {
                if (isEmpty(item)) {
                  return ''
                }
                const marker = item.color
                const label = item.name
                const value = item.value
                const markerSize = item.compactMarker ? 5 : 10
                const markerMarginRight = item.compactMarker ? 9 : 5
                const markerMarginLeft = item.compactMarker ? 2 : 0
                // 使用独立 class，避免 AntV 挂载后将所有 marker 重新写成默认 8px
                return TOOLTIP_ITEM_TPL.replace(
                  'class="g2-tooltip-list-item-marker"',
                  'class="de-stock-tooltip-list-item-marker"'
                )
                  .replace('width: 8px;', `width: ${markerSize}px;`)
                  .replace('height: 8px;', `height: ${markerSize}px;`)
                  .replace('display: inline-block;', 'display: inline-block; flex: 0 0 auto;')
                  .replace(
                    'margin-right: 4px;',
                    `margin-right: ${markerMarginRight}px; margin-left: ${markerMarginLeft}px;`
                  )
                  .replace('{marker}', marker)
                  .replace('{label}', label)
                  .replace('{value}', value)
              })
              .join('')
            const listHtml = `<ul class="g2-tooltip-list" style="margin: 0px; list-style-type: none; padding: 0px;">${itemsHtml}</ul>`
            return `${titleHtml}${listHtml}`
          }
        }
      }
    }
    defaultsDeep(lineMark, lineMarkTooltipOptions)
    return options
  }

  protected customConfigEmptyDataStrategy(chart: Chart, options: G2Spec): G2Spec {
    const data = options.data.value
    if (!data?.length) {
      return options
    }
    const { emptyDataStrategy: strategy } = parseJson(chart.senior).functionCfg
    if (strategy === 'ignoreData') {
      for (let i = data.length - 1; i >= 0; i--) {
        const item = data[i]
        Object.keys(item).forEach(key => {
          if (key.startsWith('f_') && item[key] === null) {
            data.splice(i, 1)
          }
        })
      }
    }
    const updateValues = (strategy: 'breakLine' | 'setZero', data: any[]) => {
      data.forEach(obj => {
        Object.keys(obj).forEach(key => {
          if (key.startsWith('f_') && obj[key] === null) {
            obj[key] = strategy === 'breakLine' ? null : 0
          }
        })
      })
    }
    if (strategy === 'breakLine' || strategy === 'setZero') {
      updateValues(strategy, data)
    }
    return options
  }

  protected setupOptions(chart: Chart, options: G2Spec): G2Spec {
    return flow(
      this.configTheme,
      this.customConfigEmptyDataStrategy,
      this.configAvgLine,
      this.configLegend,
      this.configBasicStyle,
      this.configXAxis,
      this.configYAxis,
      this.configTooltip
    )(chart, options, {}, this)
  }

  constructor(name = 'stock-line') {
    super(name, DEFAULT_DATA)
    this.stockMarker.style = ['stroke', 'fill', 'lineWidth', 'lineOpacity']
  }
}
