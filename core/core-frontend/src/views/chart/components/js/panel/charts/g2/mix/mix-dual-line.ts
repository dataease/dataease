import { G2ChartView, G2DrawOptions } from '../../../types/impl/g2'
import {
  flow,
  handleBreakLineMultiDimension,
  handleSetZeroMultiDimension,
  handleSetZeroSingleDimension,
  hexColorToRGBA,
  parseJson
} from '@/views/chart/components/js/util'
import { cloneDeep, defaultsDeep, isEmpty, merge } from 'lodash-es'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { useI18n } from '@/hooks/web/useI18n'
import { Chart as G2Chart, G2Spec } from '@antv/g2'
import {
  DEFAULT_BASIC_STYLE,
  DEFAULT_YAXIS_STYLE
} from '@/views/chart/components/editor/util/chart'
import {
  getG2Renderer,
  getTooltipCrosshairsStyle,
  handleChartDashboardHidden,
  TOOLTIP_ITEM_TPL,
  TOOLTIP_TITLE_TPL
} from '../../../common/common_antv'
import {
  CHART_MIX_EDITOR_PROPERTY,
  CHART_MIX_EDITOR_PROPERTY_INNER,
  configMixCustomLegend,
  filterValidMixTooltipItems,
  getAssistLineAxisIndex,
  getMixLabelTransform,
  MixG2Chart
} from './common'
import G2TooltipCarousel from '@/views/chart/components/js/G2TooltipCarousel'
import {
  bindPlotBackgroundClick,
  createTooltipWrapper,
  getSeriesIndexMapByRelations,
  getMixTooltipGroupIndex,
  getMixTooltipGroupName,
  renderGroupedTooltipItems,
  sortMixTooltipItems,
  tooltipCss,
  tooltipMaxHeight
} from '@/views/chart/components/js/panel/charts/g2/bar/barUtil'

const { t } = useI18n()
/**
 * 双线组合图
 */
export class GroupLineMix extends G2ChartView {
  properties: EditorProperty[] = CHART_MIX_EDITOR_PROPERTY
  propertyInner: EditorPropertyInner = {
    ...CHART_MIX_EDITOR_PROPERTY_INNER,
    'legend-selector': ['icon', 'fontSize', 'color', 'hPosition', 'vPosition'],
    'label-selector': ['vPosition', 'seriesLabelFormatter'],
    'tooltip-selector': [
      'fontSize',
      'color',
      'backgroundColor',
      'show',
      'seriesTooltipFormatter',
      'carousel'
    ]
  }

  axis: AxisType[] = [
    'xAxis',
    'yAxis',
    'drill',
    'filter',
    'extLabel',
    'extTooltip',
    'xAxisExtRight',
    'yAxisExt',
    'xAxisExt'
  ]

  axisConfig: AxisConfig = {
    xAxis: {
      name: `${t('chart.drag_block_type_axis')} / ${t('chart.dimension')}`,
      type: 'd'
    },
    xAxisExt: {
      name: `${t('chart.drag_block_type_axis_left')} / ${t('chart.dimension')}`,
      type: 'd',
      limit: 1,
      allowEmpty: true
    },
    yAxis: {
      name: `${t('chart.drag_block_value_axis_left')} / ${t('chart.column_quota')}`,
      limit: 1,
      type: 'q'
    },
    extBubble: {
      //用这个字段存放右轴分类
      name: `${t('chart.drag_block_type_axis_right')} / ${t('chart.dimension')}`,
      limit: 1,
      type: 'd',
      allowEmpty: true
    },
    yAxisExt: {
      name: `${t('chart.drag_block_value_axis_right')} / ${t('chart.line_quota')}`,
      limit: 1,
      type: 'q',
      allowEmpty: true
    }
  }

  // 提取参与 y 轴域计算的有效数值，避免空值把轴域拉偏
  protected getYAxisValues(data: Record<string, any>[]): number[] {
    return data
      .map(d => d.value)
      .filter(value => value !== null && value !== undefined && value !== '')
      .map(value => Number(value))
      .filter(value => Number.isFinite(value))
  }

  // 生成接近自动轴刻度的可见轴域，供点线和辅助线共用
  protected getNiceDomain(min: number, max: number): [number, number] {
    const range = max - min
    if (range <= 0) {
      return [min, max]
    }
    const roughStep = range / 5
    const magnitude = 10 ** Math.floor(Math.log10(roughStep))
    const residual = roughStep / magnitude
    let niceStep = magnitude
    if (residual > 5) {
      niceStep = 10 * magnitude
    } else if (residual > 2) {
      niceStep = 5 * magnitude
    } else if (residual > 1) {
      niceStep = 2 * magnitude
    }
    return [Math.floor(min / niceStep) * niceStep, Math.ceil(max / niceStep) * niceStep]
  }

  protected getYAxisDomain(data: Record<string, any>[]): [number, number] | undefined {
    const values = this.getYAxisValues(data)
    if (!values.length) {
      return undefined
    }
    const min = Math.min(...values)
    const max = Math.max(...values)
    if (min === max) {
      if (min === 0) {
        return [0, 1]
      }
      const offset = Math.abs(min) * 0.05 || 1
      return this.getNiceDomain(
        min > 0 ? Math.max(0, min - offset) : min - offset,
        max < 0 ? Math.min(0, max + offset) : max + offset
      )
    }
    const offset = (max - min) * 0.05
    return this.getNiceDomain(
      min >= 0 ? Math.max(0, min - offset) : min - offset,
      max <= 0 ? Math.min(0, max + offset) : max + offset
    )
  }

  async drawChart(drawOptions: G2DrawOptions<G2Chart>): Promise<G2Chart> {
    const { chart, action, container } = drawOptions
    chart.container = container
    if (!chart.data?.left?.data?.length && !chart.data?.right?.data?.length) {
      return
    }
    const [left] = cloneDeep(chart.data?.left?.data)
    const [right] = cloneDeep(chart.data?.right?.data)
    const leftData = left?.data || []
    const rightData = right?.data || []
    // options
    const initOptions: G2Spec = {
      type: 'spaceFlex',
      ratio: [1],
      direction: 'col',
      autoFit: true,
      children: [
        {
          type: 'view',
          key: 'chart',
          legend: false,
          children: [
            {
              type: 'line',
              data: {
                type: 'inline',
                value: leftData,
                transform: [
                  {
                    type: 'map',
                    callback: d => ({ ...d, left: true })
                  }
                ]
              },
              encode: {
                x: 'field',
                y: 'value',
                series: 'category',
                color: 'category'
              },
              axis: {
                y: {
                  position: 'left'
                }
              },
              scale: {
                y: {
                  key: 'left',
                  nice: true,
                  independent: true
                }
              },
              style: {
                connect: false
              }
            },
            {
              type: 'point',
              data: leftData,
              encode: {
                x: 'field',
                y: 'value',
                color: 'category'
              },
              scale: {
                y: {
                  key: 'left',
                  nice: true,
                  independent: true
                }
              },
              axis: {
                y: false
              },
              style: {
                stroke: 'white'
              },
              tooltip: false
            },
            {
              type: 'line',
              data: rightData,
              encode: {
                x: 'field',
                y: 'value',
                series: 'category',
                color: 'category'
              },
              scale: {
                y: {
                  key: 'right',
                  nice: true,
                  independent: true
                }
              },
              axis: {
                y: {
                  position: 'right'
                }
              },
              style: {
                connect: false
              }
            },
            {
              type: 'point',
              data: rightData,
              encode: {
                x: 'field',
                y: 'value',
                color: 'category'
              },
              scale: {
                y: {
                  key: 'right',
                  nice: true,
                  independent: true
                }
              },
              axis: {
                y: false
              },
              style: {
                stroke: 'white'
              },
              tooltip: false
            }
          ]
        }
      ],
      interaction: {
        // 双线组合图只保留 tooltip，不展示鼠标悬浮/点击选中态
        elementHighlight: false,
        elementSelect: false
      }
    }
    // 注入公共渲染器配置以响应 SVG 渲染开关
    const newChart = new MixG2Chart({ container, ...getG2Renderer() })
    const options = this.setupOptions(chart, initOptions, {
      chartObj: newChart,
      leftData,
      rightData
    })

    newChart.on('point:click', action)
    newChart.on('interval:click', action)
    bindPlotBackgroundClick(newChart, { markTypes: ['point'] })
    handleChartDashboardHidden(chart, options)
    newChart.options(options)
    new G2TooltipCarousel(newChart, chart, [...leftData, ...rightData]).start()
    return newChart
  }

  protected configBasicStyle(chart: Chart, options: G2Spec, context: Record<string, any>): G2Spec {
    const { basicStyle } = parseJson(chart.customAttr)
    const leftCat = []
    const { xAxisExt, extBubble, yAxis, yAxisExt } = chart
    const [leftLineMark, leftPointMark, lineMark, pointMark] = options.children[0].children
    if (!xAxisExt?.length) {
      leftCat.push(yAxis[0]?.chartShowName ?? yAxis[0]?.name)
    } else {
      const { leftData } = context
      leftData.forEach(d => d.category && !leftCat.includes(d.category) && leftCat.push(d.category))
    }
    const leftColorMap = leftCat.reduce((acc, cur, index) => {
      acc[cur] = hexColorToRGBA(
        basicStyle.colors[index % basicStyle.colors.length],
        basicStyle.alpha
      )
      return acc
    }, {})
    if (basicStyle.seriesColor?.length) {
      if (!xAxisExt?.length) {
        const ySeries = basicStyle.seriesColor.find(s => s.id === yAxis[0]?.id)
        if (ySeries) {
          leftColorMap[yAxis[0]?.chartShowName ?? yAxis[0]?.name] = hexColorToRGBA(
            ySeries.color,
            basicStyle.alpha
          )
        }
      } else {
        basicStyle.seriesColor.forEach(s => {
          if (leftColorMap[s.id]) {
            leftColorMap[s.id] = hexColorToRGBA(s.color, basicStyle.alpha)
          }
        })
      }
    }
    const leftRelations = []
    Object.entries(leftColorMap).forEach(([key, value]) => {
      leftRelations.push([key, value])
    })
    const leftRange = basicStyle.colors.map(c => hexColorToRGBA(c, basicStyle.alpha))
    const leftColorScale = {
      scale: {
        color: {
          key: 'color',
          type: 'ordinal',
          independent: true,
          range: leftRange,
          relations: leftRelations
        }
      }
    }
    merge(leftLineMark, leftColorScale)
    merge(leftPointMark, leftColorScale)
    merge(leftLineMark, {
      style: {
        lineWidth: basicStyle.leftLineWidth
      },
      encode: {
        shape: basicStyle.leftLineSmooth ? 'smooth' : 'line'
      }
    })
    merge(leftPointMark, {
      encode: {
        shape: basicStyle.leftLineSymbol,
        size: basicStyle.leftLineSymbolSize ? basicStyle.leftLineSymbolSize : 0.01
      },
      style: {
        opacity: basicStyle.leftLineSymbolSize === 0 ? 0 : 1,
        fillOpacity: basicStyle.leftLineSymbolSize === 0 ? 0 : 1,
        strokeOpacity: basicStyle.leftLineSymbolSize === 0 ? 0 : 1,
        lineWidth: basicStyle.leftLineSymbolSize === 0 ? 0 : 1
      }
    })
    const rightCat = []
    if (!extBubble?.length) {
      yAxisExt?.length && rightCat.push(yAxisExt[0]?.chartShowName ?? yAxisExt[0]?.name)
    } else {
      const { rightData } = context
      rightData.forEach(
        d => d.category && !rightCat.includes(d.category) && rightCat.push(d.category)
      )
    }
    const rightColorMap = rightCat.reduce((acc, cur, index) => {
      acc[cur] = hexColorToRGBA(
        basicStyle.subColors[index % basicStyle.subColors.length],
        basicStyle.subAlpha
      )
      return acc
    }, {})
    if (basicStyle.subSeriesColor?.length) {
      if (!extBubble?.length) {
        const yExtSeries = basicStyle.subSeriesColor.find(s => s.id === yAxisExt[0]?.id)
        if (yExtSeries) {
          rightColorMap[yAxisExt[0]?.chartShowName ?? yAxisExt[0]?.name] = hexColorToRGBA(
            yExtSeries.color,
            basicStyle.subAlpha
          )
        }
      } else {
        basicStyle.subSeriesColor.forEach(s => {
          if (rightColorMap[s.id]) {
            rightColorMap[s.id] = hexColorToRGBA(s.color, basicStyle.subAlpha)
          }
        })
      }
    }
    const rightRelations = []
    Object.entries(rightColorMap).forEach(entry => {
      rightRelations.push(entry)
    })
    const rightRange = basicStyle.subColors.map(c => hexColorToRGBA(c, basicStyle.subAlpha))
    const rightColorScale = {
      scale: {
        color: {
          type: 'ordinal',
          independent: true,
          domain: rightCat,
          range: rightRange,
          relations: rightRelations
        }
      }
    }
    merge(lineMark, rightColorScale)
    merge(pointMark, rightColorScale)
    merge(lineMark, {
      style: {
        lineWidth: basicStyle.lineWidth
      },
      encode: {
        shape: basicStyle.lineSmooth ? 'smooth' : 'line'
      }
    })
    merge(pointMark, {
      encode: {
        shape: basicStyle.lineSymbol,
        size: basicStyle.lineSymbolSize ? basicStyle.lineSymbolSize : 0.01
      },
      style: {
        opacity: basicStyle.lineSymbolSize === 0 ? 0 : 1,
        fillOpacity: basicStyle.lineSymbolSize === 0 ? 0 : 1,
        strokeOpacity: basicStyle.lineSymbolSize === 0 ? 0 : 1,
        lineWidth: basicStyle.lineSymbolSize === 0 ? 0 : 1
      }
    })
    return options
  }

  protected configLegend(chart: Chart, options: G2Spec): G2Spec {
    const [leftLineMark, _, lineMark] = options.children[0].children
    const leftRelations = leftLineMark.scale.color.relations
    const rightRelations = lineMark.scale.color.relations
    // 底部水平图例与柱线、堆叠柱线组合图保持相同的留白位置
    return configMixCustomLegend(chart, options, leftRelations, rightRelations, {
      alignBottom: true
    })
  }

  protected configLabel(chart: Chart, options: G2Spec): G2Spec {
    const { label } = parseJson(chart.customAttr)
    if (!label.show) {
      return options
    }
    const seriesMap = label.seriesLabelFormatter?.reduce((acc, cur) => {
      acc[cur.id] = cur
      return acc
    }, {})
    const labelOpt = {
      labels: [
        {
          text: d => {
            if (!label.seriesLabelFormatter?.length) {
              return d.value
            }
            const labelCfg = seriesMap?.[d.quotaList[0].id] as SeriesFormatter
            if (!labelCfg) {
              return d.value
            }
            if (!labelCfg.show) {
              return ''
            }
            return valueFormatter(d.value, labelCfg.formatterCfg)
          },
          style: {
            fillOpacity: 1,
            fontSize: d => {
              if (!label.seriesLabelFormatter?.length) {
                return 12
              }
              const labelCfg = seriesMap?.[d.quotaList[0].id] as SeriesFormatter
              if (!labelCfg) {
                return 12
              }
              if (!labelCfg.show) {
                return 0
              }
              return labelCfg.fontSize
            },
            fill: d => {
              if (!label.seriesLabelFormatter?.length) {
                return 'black'
              }
              const labelCfg = seriesMap?.[d.quotaList[0].id] as SeriesFormatter
              if (!labelCfg?.show) {
                return 'black'
              }
              return labelCfg.color
            },
            position: label.position === 'middle' ? 'inside' : label.position
          },
          textBaseline: {
            top: 'bottom',
            middle: 'middle',
            bottom: 'top'
          }[label.position],
          transform: getMixLabelTransform(label.fullDisplay),
          fontFamily: chart.fontFamily
        }
      ]
    }
    const [_, leftPointMark, __, pointMark] = options.children.find(c => c.key === 'chart').children
    if (!label.seriesLabelFormatter?.length) {
      defaultsDeep(leftPointMark, labelOpt)
      defaultsDeep(pointMark, labelOpt)
    } else {
      const showLeft = label.seriesLabelFormatter.some(c => c.id === chart.yAxis[0]?.id && c.show)
      const showRight = label.seriesLabelFormatter.some(
        c => c.id === chart.yAxisExt[0]?.id && c.show
      )
      if (showLeft) {
        defaultsDeep(leftPointMark, labelOpt)
      }
      if (showRight) {
        defaultsDeep(pointMark, labelOpt)
      }
    }
    return options
  }

  protected configTooltip(chart: Chart, options: G2Spec, context: Record<string, any>): G2Spec {
    const { tooltip } = parseJson(chart.customAttr)
    const [leftLineMark, _, lineMark] = options.children.find(c => c.key === 'chart').children
    if (!tooltip.show) {
      defaultsDeep(leftLineMark, { tooltip: false })
      defaultsDeep(lineMark, { tooltip: false })
      return options
    }
    const chartObj = context.chartObj as G2Chart
    const formatterMap = (tooltip.seriesTooltipFormatter || []).reduce((pre, next) => {
      pre[next.id] = next
      return pre
    }, {}) as Record<string, SeriesFormatter>
    const sourceTooltipItems = [
      ...(context.leftData || []).map(item => ({ ...item, left: true })),
      ...(context.rightData || [])
    ]
    const quotaIdOf = item => item?.quotaList?.[0]?.id
    const completeTooltipItems = (title, items = []) => {
      if (filterValidMixTooltipItems(items).length) {
        return items
      }
      // 双线 shared tooltip 未返回有效指标时，仅按当前维度读取左右轴源数据
      return sourceTooltipItems.filter(item => item.field === title)
    }
    const yAxis = chart.yAxis
    // 读取左右轴 color relations，保持 tooltip 系列顺序与图例一致
    const seriesIndexMap = getSeriesIndexMapByRelations([
      ...(leftLineMark.scale?.color?.relations || []),
      ...(lineMark.scale?.color?.relations || [])
    ])
    const tooltipOptions: G2Spec = {
      tooltip: d => d,
      interaction: {
        ...options.interaction,
        tooltip: {
          crosshairsLineDash: [4, 4],
          ...getTooltipCrosshairsStyle(chart),
          mount: createTooltipWrapper(chart),
          css: tooltipCss(tooltip),
          enterable: true,
          marker: false,
          render: (_, { title, items }) => {
            const titleHtml = TOOLTIP_TITLE_TPL.replace('{title}', title)
            items = completeTooltipItems(title, items)
            if (tooltip.seriesTooltipFormatter?.length) {
              items = items.filter(item => formatterMap[quotaIdOf(item)]?.show !== false)
            }
            items = filterValidMixTooltipItems(items)
            const result = []
            const view = chartObj.getContext().views.find(v => v.key === 'chart')
            items.forEach(item => {
              const formatterCfg =
                formatterMap[quotaIdOf(item)]?.formatterCfg ?? yAxis[0].formatterCfg
              const value = valueFormatter(item.value, formatterCfg)
              const name = item.category
              const colorScale = item.left ? view?.scale?.color : view?.scale?.color2
              const color = colorScale?.map?.(name) ?? item.color
              // 记录 tooltip 项所属维度分组，供后续分组标题和排序使用
              result.push({
                value,
                color,
                name,
                groupName: getMixTooltipGroupName(chart, item),
                groupIndex: getMixTooltipGroupIndex(chart, item)
              })
            })
            // 混合图 tooltip 先按维度分组，再按图例系列顺序排列
            sortMixTooltipItems(result, seriesIndexMap)
            const itemsHtml = renderGroupedTooltipItems(
              result,
              item => item.groupName,
              item => {
                const marker = item.color
                const label = item.name
                const value = item.value
                return TOOLTIP_ITEM_TPL.replace('{marker}', marker)
                  .replace('{label}', label)
                  .replace('{value}', value)
              }
            )
            const listHtml = `<ul class="g2-tooltip-list" style="${tooltipMaxHeight(
              chart
            )}margin: 0px; list-style-type: none; padding: 0px;">${itemsHtml}</ul>`
            return `${titleHtml}${listHtml}`
          }
        }
      }
    }
    defaultsDeep(lineMark, tooltipOptions)
    defaultsDeep(leftLineMark, { tooltip: d => d })
    return options
  }

  protected configXAxis(chart: Chart, options: G2Spec, context: Record<string, any>): G2Spec {
    const { xAxis } = parseJson(chart.customStyle)
    const view = options.children.find(c => c.key === 'chart')
    // 固定 x 轴顺序
    const { leftData: xAxisData } = context
    const xAxisSort = xAxisData.map(d => d.field)
    defaultsDeep(view, {
      scale: {
        x: {
          range: [0, 1],
          compare: (a, b) => {
            return xAxisSort.indexOf(a) - xAxisSort.indexOf(b)
          }
        }
      }
    })
    if (!xAxis.show) {
      const axisHide = {
        axis: {
          x: false
        }
      }
      defaultsDeep(view, axisHide)
      return options
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
          title: xAxis.nameShow === false || isEmpty(xAxis.name) ? false : xAxis.name,
          titleFontSize: xAxis.fontSize,
          titleFill: xAxis.color,
          ...this.getAxisLineStyle(chart, xAxis),
          lineLineDash,
          label: xAxis.axisLabel.show,
          labelOpacity: 1,
          labelFill: xAxis.axisLabel.color,
          labelFillOpacity: 1,
          labelFontSize: xAxis.axisLabel.fontSize,
          grid: xAxis.splitLine.show,
          gridStroke: xAxis.splitLine.lineStyle.color,
          gridStrokeOpacity: 1,
          gridLineWidth: xAxis.splitLine.lineStyle.width,
          gridLineDash,
          ...this.getAxisLabelStyle(xAxis)
        }
      }
    }
    defaultsDeep(view, axisStyle)
    return options
  }

  protected configYAxis(chart: Chart, options: G2Spec): G2Spec {
    const { xAxis, yAxis, yAxisExt } = parseJson(chart.customStyle)
    const [leftLineMark, leftPointMark, lineMark, pointMark] = options.children.find(
      c => c.key === 'chart'
    ).children
    if (!yAxis.show) {
      leftLineMark.axis.y = false
      lineMark.axis.y = false
      return options
    }
    const overlapGridFilter = this.getOverlapGridFilter(xAxis)
    const yAxisOption = { ...this.getAxis(chart, yAxis), ...overlapGridFilter }
    const yAxisExtOption = { ...this.getAxis(chart, yAxisExt), ...overlapGridFilter }
    merge(leftLineMark, {
      axis: {
        y: {
          ...yAxisOption,
          position: 'left'
        }
      }
    })
    merge(lineMark, {
      axis: {
        y: {
          ...yAxisExtOption,
          position: 'right'
        }
      }
    })
    // 自动轴下显式同步 line 和 point 的 y 轴域，避免独立 scale 导致点线错位
    const leftDomain = this.getYAxisDomain(leftLineMark.data?.value || [])
    if (leftDomain) {
      const scaleYLeft = {
        ...leftLineMark.scale.y,
        domain: leftDomain
      }
      leftLineMark.scale.y = scaleYLeft
      leftPointMark.scale.y = scaleYLeft
    }
    const rightDomain = this.getYAxisDomain(lineMark.data || [])
    if (rightDomain) {
      const scaleYRight = {
        ...lineMark.scale.y,
        domain: rightDomain
      }
      lineMark.scale.y = scaleYRight
      pointMark.scale.y = scaleYRight
    }
    if (yAxis.axisValue.auto === false) {
      const n = Math.max(2, yAxis.axisValue.splitCount)
      const scaleYLeft = {
        key: 'left',
        nice: false,
        independent: true,
        domain: [yAxis.axisValue.min, yAxis.axisValue.max]
      }
      leftLineMark.scale.y = scaleYLeft
      leftPointMark.scale.y = scaleYLeft
      merge(leftLineMark, {
        axis: {
          y: {
            tickCount: n,
            tickMethod: (min, max, count) => {
              const step = (max - min) / (count - 1)
              const ticks = []
              for (let i = 0; i < count; i++) {
                ticks.push(min + i * step)
              }
              return ticks
            }
          }
        }
      })
    }
    if (yAxisExt.axisValue.auto === false) {
      const n = Math.max(2, yAxisExt.axisValue.splitCount)
      const scaleYRight = {
        key: 'right',
        nice: false,
        independent: true,
        domain: [yAxisExt.axisValue.min, yAxisExt.axisValue.max]
      }
      lineMark.scale.y = scaleYRight
      pointMark.scale.y = scaleYRight
      merge(lineMark, {
        axis: {
          y: {
            tickCount: n,
            tickMethod: (min, max, count) => {
              const step = (max - min) / (count - 1)
              const ticks = []
              for (let i = 0; i < count; i++) {
                ticks.push(min + i * step)
              }
              return ticks
            }
          }
        }
      })
    }
    return options
  }

  protected configAssistLine(chart: Chart, options: G2Spec): G2Spec {
    const { assistLineCfg } = parseJson(chart.senior)
    if (!assistLineCfg.enable || !assistLineCfg.assistLine?.length) {
      return options
    }
    const splitLineData = [[], []]
    const splitDynamicFields = [[], []]
    assistLineCfg.assistLine?.forEach(item => {
      const axisIndex = getAssistLineAxisIndex(item.yAxisType)
      const lineData = splitLineData[axisIndex]
      const dynamicFields = splitDynamicFields[axisIndex]
      // 固定值
      if (item.field === '0') {
        lineData.push({ ...item, value: parseFloat(item.value) })
      }
      // 动态值
      if (item.field === '1') {
        dynamicFields.push(item.fieldId)
      }
    })
    const assistLineData = [
      ...(chart.data.left.dynamicAssistLines ?? []),
      ...(chart.data.right.dynamicAssistLines ?? [])
    ]
    assistLineData.forEach(d => {
      const axisIndex = getAssistLineAxisIndex(d.yAxisType)
      const fields = splitDynamicFields[axisIndex]
      if (fields.includes(d.fieldId)) {
        splitLineData[axisIndex].push({ ...d, value: parseFloat(d.value) })
      }
    })
    const { yAxis, yAxisExt } = parseJson(chart.customStyle)
    const yAxisFormatterCfg = yAxis.axisLabelFormatter ?? DEFAULT_YAXIS_STYLE.axisLabelFormatter
    const yAxisExtFormatterCfg =
      yAxisExt.axisLabelFormatter ?? DEFAULT_YAXIS_STYLE.axisLabelFormatter
    const view = options.children.find(c => c.key === 'chart')
    const [leftLineMark, , rightLineMark] = view.children
    splitLineData.forEach((lineData, index) => {
      const assistLineScaleY = index === 0 ? leftLineMark.scale?.y : rightLineMark.scale?.y
      // 辅助线只在对应轴当前可见轴域内显示，超出轴域时连同标签一起隐藏
      const [domainStart, domainEnd] = assistLineScaleY?.domain || []
      const domainMin = Number(domainStart)
      const domainMax = Number(domainEnd)
      const hasDomain = Number.isFinite(domainMin) && Number.isFinite(domainMax)
      const visibleLineData = lineData.filter(item => {
        const value = Number(item.value)
        if (!Number.isFinite(value)) {
          return false
        }
        if (!hasDomain) {
          return true
        }
        return value >= Math.min(domainMin, domainMax) && value <= Math.max(domainMin, domainMax)
      })
      if (visibleLineData.length) {
        const assistLineMark: G2Spec = {
          type: 'lineY',
          encode: { y: 'value' },
          scale: {
            y: {
              ...assistLineScaleY,
              key: index === 0 ? 'left' : 'right'
            }
          },
          axis: {
            y: false
          },
          data: visibleLineData,
          style: {
            stroke: d => d.color,
            lineDash: d =>
              d.lineType === 'solid' ? [] : d.lineType === 'dashed' ? [10, 8] : [1, 2],
            opacity: 1
          },
          labels: [
            {
              text: d => {
                const value = valueFormatter(
                  parseFloat(d.value),
                  index === 0 ? yAxisFormatterCfg : yAxisExtFormatterCfg
                )
                return d.name ? `${d.name}: ${value}` : value
              },
              style: {
                fontSize: d => parseInt(d.fontSize),
                fill: d => d.color,
                fillOpacity: 1
              },
              textBaseline: 'bottom',
              position: index === 0 ? 'left' : 'right',
              transform: [{ type: 'overlapHide' }, { type: 'exceedAdjust' }],
              fontFamily: chart.fontFamily
            }
          ],
          tooltip: false,
          // 无 color 通道且关闭图例，图例筛选不会再触达辅助线
          legend: false
        }
        view.children.push(assistLineMark)
      }
    })
    return options
  }

  public setupDefaultOptions(chart: ChartObj): ChartObj {
    const { customAttr, senior } = chart
    if (
      senior.functionCfg.emptyDataStrategy == undefined ||
      senior.functionCfg.emptyDataStrategy === 'ignoreData'
    ) {
      senior.functionCfg.emptyDataStrategy = 'breakLine'
    }
    return chart
  }

  public setupSeriesColor(chart: ChartObj, data?: any[]): ChartBasicStyle['seriesColor'] {
    const result: ChartBasicStyle['seriesColor'] = []
    const seriesSet = new Set<string>()
    const colors = chart.customAttr.basicStyle.colors
    const { yAxis, xAxisExt } = chart
    if (xAxisExt?.length) {
      data?.forEach(d => {
        if (d.value === null || d.category === null || seriesSet.has(d.category)) {
          return
        }
        seriesSet.add(d.category)
        result.push({
          id: d.category,
          name: d.category,
          color: colors[(seriesSet.size - 1) % colors.length]
        })
      })
    } else {
      yAxis?.forEach(axis => {
        if (seriesSet.has(axis.id)) {
          return
        }
        seriesSet.add(axis.id)
        result.push({
          id: axis.id,
          name: axis.chartShowName ?? axis.name,
          color: colors[(seriesSet.size - 1) % colors.length]
        })
      })
    }
    return result
  }

  public setupSubSeriesColor(chart: ChartObj, data?: any[]): ChartBasicStyle['seriesColor'] {
    const result: ChartBasicStyle['seriesColor'] = []
    const seriesSet = new Set<string>()
    const colors = chart.customAttr.basicStyle.subColors ?? CHART_MIX_DEFAULT_BASIC_STYLE.subColors
    const { yAxisExt, extBubble } = chart
    if (extBubble?.length) {
      data?.forEach(d => {
        if (d.value === null || d.category === null || seriesSet.has(d.category)) {
          return
        }
        seriesSet.add(d.category)
        result.push({
          id: d.category,
          name: d.category,
          color: colors[(seriesSet.size - 1) % colors.length]
        })
      })
    } else {
      yAxisExt?.forEach(axis => {
        if (seriesSet.has(axis.id)) {
          return
        }
        seriesSet.add(axis.id)
        result.push({
          id: axis.id,
          name: axis.chartShowName ?? axis.name,
          color: colors[(seriesSet.size - 1) % colors.length]
        })
      })
    }
    return result
  }

  protected configEmptyDataStrategy(chart: Chart, options: G2Spec): G2Spec {
    const { functionCfg } = parseJson(chart.senior)
    const { emptyDataStrategy } = functionCfg
    const [leftLineMark, _, rightLineMark] = options.children[0].children
    const leftData = leftLineMark.data?.value || []
    const rightData = rightLineMark.data || []
    const leftMultiDimension = chart.xAxisExt?.length > 0
    const rightMultiDimension = chart.extBubble?.length > 0
    switch (emptyDataStrategy) {
      case 'breakLine': {
        if (leftMultiDimension) {
          handleBreakLineMultiDimension(leftData)
          merge(leftLineMark, { style: { connect: false } })
        }
        if (rightMultiDimension) {
          handleBreakLineMultiDimension(rightData)
          merge(rightLineMark, { style: { connect: false } })
        }
        break
      }
      case 'setZero': {
        if (leftMultiDimension) {
          // 多维度置0
          handleSetZeroMultiDimension(leftData)
        } else {
          // 单维度置0
          handleSetZeroSingleDimension(leftData)
        }
        if (rightMultiDimension) {
          // 多维度置0
          handleSetZeroMultiDimension(rightData)
        } else {
          // 单维度置0
          handleSetZeroSingleDimension(rightData)
        }
        break
      }
    }
    return options
  }
  protected setupOptions(chart: Chart, options: G2Spec, context: Record<string, any>): G2Spec {
    return flow(
      this.configEmptyDataStrategy,
      this.configBasicStyle,
      this.configLegend,
      this.configLabel,
      this.configTooltip,
      this.configXAxis,
      this.configYAxis,
      this.configAssistLine
    )(chart, options, context, this)
  }

  constructor(name = 'chart-mix-dual-line') {
    super(name, [])
  }
}

export const CHART_MIX_DEFAULT_BASIC_STYLE = {
  ...DEFAULT_BASIC_STYLE,
  subAlpha: 100,
  subColorScheme: 'fast',
  subSeriesColor: [],
  subColors: [
    '#fae800',
    '#00c039',
    '#0482dc',
    '#bb9581',
    '#ff7701',
    '#9c5ec3',
    '#00ccdf',
    '#00c039',
    '#ff7701'
  ],
  leftLineWidth: 2,
  leftLineSymbol: 'circle',
  leftLineSymbolSize: 4,
  leftLineSmooth: true
}
