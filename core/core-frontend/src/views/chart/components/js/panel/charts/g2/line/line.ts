import { G2ChartView, G2DrawOptions } from '../../../types/impl/g2'
import {
  flow,
  getLineConditions,
  getLineLabelColorByCondition,
  handleBreakLineMultiDimension,
  handleIgnoreData,
  handleSetZeroMultiDimension,
  handleSetZeroSingleDimension,
  hexColorToRGBA,
  parseJson,
  randomString,
  setUpGroupSeriesColor
} from '@/views/chart/components/js/util'
import { cloneDeep, defaultsDeep, isEmpty, merge } from 'lodash-es'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import {
  configLineConditionDataColor,
  configLineMarkConditionStyle,
  configPointConditionStyle,
  configYAxisSeriesLegendDomain,
  getLineConditionColorWithAlpha,
  getLineConditionLineYMarks,
  getLineTooltipSameDimensionItems,
  bindLineLegendState,
  LINE_AXIS_TYPE,
  LINE_CONDITION_VISIBLE_DOMAIN_KEY,
  LINE_EDITOR_PROPERTY,
  LINE_EDITOR_PROPERTY_INNER,
  sortTooltipItemsByYAxis
} from './common'
import { useI18n } from '@/hooks/web/useI18n'
import { Chart as G2Chart, G2Spec } from '@antv/g2'
import { DEFAULT_YAXIS_STYLE } from '@/views/chart/components/editor/util/chart'
import {
  configDimensionSlider,
  getG2Renderer,
  getTooltipCrosshairsStyle,
  handleChartDashboardHidden,
  TOOLTIP_ITEM_TPL,
  TOOLTIP_TITLE_TPL
} from '../../../common/common_antv'
import { extremumEvt, addExtremumText } from '@/views/chart/components/js/extremumUitl'
import G2TooltipCarousel from '@/views/chart/components/js/G2TooltipCarousel'
import {
  createTooltipWrapper,
  getStackTooltipGroupName,
  renderGroupedTooltipItems,
  tooltipCss,
  tooltipMaxHeight
} from '../bar/barUtil'

const { t } = useI18n()
const DEFAULT_DATA = []

/**
 * 折线图
 */
export class Line extends G2ChartView {
  properties = LINE_EDITOR_PROPERTY
  propertyInner = {
    ...LINE_EDITOR_PROPERTY_INNER,
    'basic-style-selector': [...LINE_EDITOR_PROPERTY_INNER['basic-style-selector'], 'seriesColor'],
    'label-selector': ['seriesLabelVPosition', 'seriesLabelFormatter', 'showExtremum'],
    'tooltip-selector': [
      ...LINE_EDITOR_PROPERTY_INNER['tooltip-selector'],
      'seriesTooltipFormatter',
      'carousel'
    ],
    'legend-selector': [...LINE_EDITOR_PROPERTY_INNER['legend-selector'], 'legendSort']
  }
  axis: AxisType[] = [...LINE_AXIS_TYPE, 'xAxisExt']
  axisConfig = {
    ...this['axisConfig'],
    xAxis: {
      name: `${t('chart.drag_block_type_axis')} / ${t('chart.dimension')}`,
      type: 'd'
    },
    xAxisExt: {
      name: `${t('chart.chart_group')} / ${t('chart.dimension')}`,
      type: 'd',
      limit: 1,
      allowEmpty: true
    },
    yAxis: {
      name: `${t('chart.drag_block_value_axis')} / ${t('chart.quota')}`,
      type: 'q'
    }
  }

  async drawChart(drawOptions: G2DrawOptions<G2Chart>): Promise<G2Chart> {
    const { chart, action, container, scale } = drawOptions
    chart.container = container
    if (!chart.data?.data?.length) {
      return
    }
    const data = cloneDeep(chart.data.data)
    // options
    const initOptions: G2Spec = {
      type: 'view',
      data: {
        value: data
      },
      autoFit: true,
      encode: {
        x: 'field',
        y: 'value',
        color: 'category'
      },
      scale: {
        x: {
          range: [0, 1]
        },
        y: {
          nice: true
        }
      },
      children: [
        {
          zIndex: -1,
          type: 'line',
          encode: { series: 'category' }
        },
        { type: 'point', tooltip: false, zIndex: 0 }
      ]
    }
    const newChart = new G2Chart({ container, ...getG2Renderer() })
    const legendState = bindLineLegendState(newChart)
    const options = this.setupOptions(chart, initOptions, { legendState })
    // 开始渲染
    handleChartDashboardHidden(chart, options)
    newChart.options(options)
    newChart.on('point:click', action)
    new G2TooltipCarousel(newChart, chart, data).start()
    extremumEvt(newChart, chart, options, container, scale)
    return newChart
  }

  protected configTheme(chart: Chart, options: G2Spec): G2Spec {
    const customAttr = parseJson(chart.customAttr)
    const colors: string[] = []
    if (customAttr.basicStyle) {
      const basicStyle = customAttr.basicStyle
      basicStyle.colors.forEach(ele => {
        colors.push(hexColorToRGBA(ele, basicStyle.alpha))
      })
    }
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

  protected configColor(chart: Chart, options: G2Spec): G2Spec {
    const { basicStyle } = parseJson(chart.customAttr)
    const { seriesColor } = basicStyle
    if (!seriesColor?.length) {
      return options
    }
    const { xAxis, xAxisExt, yAxis } = chart
    if (!xAxis?.length || !yAxis?.length) {
      return options
    }
    const relations = []
    if (xAxisExt?.length) {
      seriesColor.forEach(item => {
        relations.push([item.id, hexColorToRGBA(item.color, basicStyle.alpha)])
      })
    } else {
      const colorMap = seriesColor.reduce((pre, next) => {
        pre[next.id] = next.color
        return pre
      }, {})
      yAxis.forEach(item => {
        if (colorMap[item.id]) {
          relations.push([
            item.chartShowName ?? item.name,
            hexColorToRGBA(colorMap[item.id], basicStyle.alpha)
          ])
        }
      })
    }
    if (relations.length) {
      const scaleOptions = {
        scale: {
          color: {
            relations
          }
        }
      }
      defaultsDeep(options, scaleOptions)
    }
    return options
  }

  protected configLabel(chart: Chart, options: G2Spec): G2Spec {
    const { label: labelAttr, basicStyle } = parseJson(chart.customAttr)
    if (!labelAttr.show) {
      return options
    }
    const conditions = getLineConditions(chart)
    const formatterMap = labelAttr.seriesLabelFormatter?.reduce((pre, next) => {
      pre[next.id] = next
      return pre
    }, {})
    // 标签颜色同样叠加基础透明度，避免条件色和系列标签色出现透明度差异
    const getLabelColor = (color = '#000000') => {
      return getLineConditionColorWithAlpha(color, basicStyle.alpha)
    }
    const showExtremumIds = Object.keys(formatterMap).filter(id => formatterMap[id].showExtremum)
    if (showExtremumIds?.length > 0) {
      const { x: xField, y: yField, color: colorField } = options.encode
      addExtremumText(options.children, showExtremumIds, xField, yField, colorField)
    }
    const pointMark: G2Spec = options.children[1]
    const labelOpt = {
      labels: [
        {
          text: d => {
            if (d.value === null) {
              return ''
            }
            const isExtremumShown = d.extremum && showExtremumIds.includes(d.quotaList?.[0]?.id)
            if (isExtremumShown) {
              return ''
            }
            if (!labelAttr.seriesLabelFormatter?.length) {
              return d.value
            }
            const labelCfg = formatterMap?.[d.quotaList[0].id] as SeriesFormatter
            if (!labelCfg) {
              return d.value
            }
            if (!labelCfg.show) {
              return ''
            }
            return valueFormatter(d.value, labelCfg.formatterCfg)
          },
          style: {
            opacity: 1,
            // 标签需要保持自身填充透明度，避免被 mark 透明度影响条件色显示
            fillOpacity: 1,
            fontSize: d => {
              if (!labelAttr.seriesLabelFormatter?.length) {
                return 12
              }
              const labelCfg = formatterMap?.[d.quotaList[0].id] as SeriesFormatter
              if (!labelCfg) {
                return 12
              }
              if (d.extremum && showExtremumIds.includes(d.quotaList?.[0]?.id)) {
                return labelCfg.showExtremum ? labelCfg.fontSize : 0
              }
              if (!labelCfg.show) {
                return 0
              }
              return labelCfg.fontSize
            },
            fill: d => {
              if (!labelAttr.seriesLabelFormatter?.length) {
                return getLabelColor()
              }
              const labelCfg = formatterMap?.[d.quotaList[0].id] as SeriesFormatter
              if (d.extremum && showExtremumIds.includes(d.quotaList?.[0]?.id)) {
                return labelCfg?.showExtremum ? getLabelColor(labelCfg?.color) : getLabelColor()
              }
              if (!labelCfg?.show) {
                return getLabelColor()
              }
              // 条件样式优先于系列标签色，保持标签与折线颜色一致
              const conditionColor = getLineLabelColorByCondition(
                conditions,
                d.value,
                d.quotaList[0].id
              )
              return conditionColor
                ? getLineConditionColorWithAlpha(conditionColor, basicStyle.alpha)
                : getLabelColor(labelCfg.color)
            },
            position: d => {
              if (
                (d.extremum && showExtremumIds.includes(d.quotaList?.[0]?.id)) ||
                !labelAttr.seriesLabelFormatter?.length
              ) {
                return 'top'
              }
              const labelCfg = formatterMap?.[d.quotaList[0].id] as SeriesFormatter
              if (!labelCfg?.show) {
                return 'top'
              }
              return labelCfg.position
            }
          },
          textBaseline: d => {
            if (
              (d.extremum && showExtremumIds.includes(d.quotaList?.[0]?.id)) ||
              !labelAttr.seriesLabelFormatter?.length
            ) {
              return 'bottom'
            }
            const labelCfg = formatterMap?.[d.quotaList[0].id] as SeriesFormatter
            if (!labelCfg?.show) {
              return 'bottom'
            }
            return labelCfg.position === 'top' ? 'bottom' : 'top'
          },
          transform: labelAttr.fullDisplay
            ? [{ type: 'exceedAdjust', bounds: 'main' }]
            : [{ type: 'exceedAdjust', bounds: 'main' }, { type: 'overlapHide' }],
          fontFamily: chart.fontFamily
        }
      ]
    }
    defaultsDeep(pointMark, labelOpt)
    return options
  }

  protected configBasicStyle(chart: Chart, options: G2Spec): G2Spec {
    // size
    const { basicStyle } = parseJson(chart.customAttr)
    const [lineMark, pointMark] = options.children
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
        size: basicStyle.lineSymbolSize ? basicStyle.lineSymbolSize : 0.01
      },
      style: {
        opacity: basicStyle.lineSymbolSize === 0 ? 0 : 1,
        fillOpacity: basicStyle.lineSymbolSize === 0 ? 0 : 1,
        strokeOpacity: basicStyle.lineSymbolSize === 0 ? 0 : 1,
        lineWidth: 0
      },
      state: {
        // 禁止 hover/选中态给数据点补默认黑色描边
        active: { lineWidth: 0, strokeOpacity: 0 },
        selected: { lineWidth: 0, strokeOpacity: 0 }
      }
    }
    defaultsDeep(pointMark, pointStyleOpt)
    return options
  }

  /**
   * 给基础折线图追加条件样式，线和点共用同一套条件颜色
   */
  protected configConditions(chart: Chart, options: G2Spec): G2Spec {
    const { threshold } = parseJson(chart.senior)
    if (!threshold?.enable || !threshold?.lineThreshold?.length) {
      return options
    }
    const data = options.data?.value
    if (!data?.length) {
      return options
    }
    const conditions = getLineConditions(chart)
    if (!conditions.length) {
      return options
    }
    const { basicStyle } = parseJson(chart.customAttr)
    const [lineMark, pointMark] = options.children
    const conditionVisibleDomain = { field: options.encode?.x }
    Object.defineProperty(lineMark, LINE_CONDITION_VISIBLE_DOMAIN_KEY, {
      value: conditionVisibleDomain,
      configurable: true
    })
    // 先把条件色写入数据项，再分别驱动线段和点的样式
    configLineConditionDataColor(data, conditions, basicStyle.alpha)
    configLineMarkConditionStyle(
      chart,
      options,
      lineMark,
      conditions,
      basicStyle.alpha,
      conditionVisibleDomain
    )
    configPointConditionStyle(pointMark)
    // 辅助线作为条件分段的视觉参考，和条件色使用同一透明度
    options.children.push(...getLineConditionLineYMarks(chart, threshold, basicStyle.alpha))
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
          ...this.getAxisLineStyle(chart, xAxis),
          lineLineDash,
          label: xAxis.axisLabel.show,
          labelFill: xAxis.axisLabel.color,
          labelOpacity: 1,
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
          dataeaseAxisTitleSafeMargin: true,
          titleFontSize: yAxis.fontSize,
          titleFill: yAxis.color,
          ...this.getAxisLineStyle(chart, yAxis),
          lineLineDash,
          label: yAxis.axisLabel.show,
          labelFill: yAxis.axisLabel.color,
          labelOpacity: 1,
          labelFillOpacity: 1,
          labelFontSize: yAxis.axisLabel.fontSize,
          grid: yAxis.splitLine.show,
          gridStroke: yAxis.splitLine.lineStyle.color,
          gridStrokeOpacity: 1,
          gridLineWidth: yAxis.splitLine.lineStyle.width,
          gridLineDash,
          // 避免数值轴边界网格线覆盖维度轴轴线样式
          ...this.getOverlapGridFilter(xAxis),
          ...this.getAxisLabelStyle(yAxis),
          labelFormatter: d => {
            return valueFormatter(d, yAxis.axisLabelFormatter)
          }
        }
      }
    }
    if (!yAxis.axisValue.auto) {
      const scaleOpt = {
        scale: {
          y: {
            nice: false,
            domainMin: yAxis.axisValue.min,
            domainMax: yAxis.axisValue.max,
            tickCount: yAxis.axisValue.splitCount < 2 ? 2 : yAxis.axisValue.splitCount,
            tickMethod: (min, max, count) => {
              const n = Math.max(2, count)
              const step = (max - min) / (n - 1)
              const ticks = []
              for (let i = 0; i < n; i++) {
                ticks.push(min + step * i)
              }
              return ticks
            }
          }
        }
      }
      defaultsDeep(axisOption, scaleOpt)
      const result = defaultsDeep(options, axisOption)
      if (result.scale?.y) {
        result.scale.y.nice = false
      }
      return result
    }
    return defaultsDeep(options, axisOption)
  }

  public setupSeriesColor(chart: ChartObj, data?: any[]): ChartBasicStyle['seriesColor'] {
    return setUpGroupSeriesColor(chart, data)
  }

  protected configLegend(chart: Chart, options: G2Spec): G2Spec {
    const xAxisExt = chart.xAxisExt[0]
    if (xAxisExt?.customSort?.length > 0) {
      // 图例自定义排序
      const sort = xAxisExt.customSort ?? []
      if (sort?.length) {
        // 用值域限定排序，有可能出现新数据但是未出现在图表上，所以这边要遍历一下子维度，加到后面，让新数据显示出来
        const data = options.data.value
        const cats =
          data?.reduce((p, n) => {
            const cat = n['category']
            if (cat && !p.includes(cat)) {
              p.push(cat)
            }
            return p
          }, []) || []
        const values = sort.reduce((p, n) => {
          if (cats.includes(n)) {
            const index = cats.indexOf(n)
            if (index !== -1) {
              cats.splice(index, 1)
            }
            p.push(n)
          }
          return p
        }, [])
        cats.length > 0 && values.push(...cats)
        const scaleOpt = {
          scale: {
            color: {
              domain: values
            }
          }
        }
        defaultsDeep(options, scaleOpt)
      }
    }
    const { legend } = parseJson(chart.customStyle)
    if (!legend.show) {
      return { ...options, legend: false }
    }
    const baseLegend = this.getLegend(chart, 2)
    const tmpLegend = {
      legend: {
        color: {
          // 与基础柱状图复用同一分类图例尺寸，分页按钮和图例标记保持一致
          ...baseLegend
        }
      }
    }
    defaultsDeep(options, tmpLegend)

    const customStyle = parseJson(chart.customStyle)
    const { sort, customSort } = customStyle.legend
    if (sort && sort !== 'none' && chart.xAxisExt.length) {
      const domain = options.scale?.color?.domain || []
      if (!domain?.length) {
        options.data.value.forEach(item => {
          if (item.category && !domain.includes(item.category)) {
            domain.push(item.category)
          }
        })
      }
      if (sort !== 'custom') {
        domain.sort((a, b) => {
          return sort !== 'desc' ? a.localeCompare(b) : b.localeCompare(a)
        })
        const scaleOpt = {
          scale: {
            color: {
              domain
            }
          }
        }
        defaultsDeep(options, scaleOpt)
      } else {
        if (!customSort?.length) {
          return options
        }
        const tmp = []
        customSort.forEach(item => {
          if (domain.includes(item)) {
            const index = domain.indexOf(item)
            const val = domain.splice(index, 1)
            tmp.push(val[0])
          }
        })
        const scaleOpt = {
          scale: {
            color: {
              domain: [...tmp, ...domain]
            }
          }
        }
        defaultsDeep(options, scaleOpt)
      }
    }
    return configYAxisSeriesLegendDomain(chart, options)
  }

  protected configAssistLine(chart: Chart, options: G2Spec): G2Spec {
    const { assistLineCfg } = parseJson(chart.senior)
    if (!assistLineCfg.enable || !assistLineCfg.assistLine?.length) {
      return options
    }
    const lineData = []
    const { yAxis } = parseJson(chart.customStyle)
    const position = yAxis.position === 'left' ? 'left' : 'right'
    const axisFormatterCfg = yAxis.axisLabelFormatter ?? DEFAULT_YAXIS_STYLE.axisLabelFormatter
    const dynamicFields = []
    assistLineCfg.assistLine?.forEach(item => {
      // 固定值
      if (item.field === '0') {
        lineData.push(item)
      }
      // 动态值
      if (item.field === '1') {
        dynamicFields.push(item.fieldId)
      }
    })
    chart.data.dynamicAssistLines?.forEach(item => {
      if (dynamicFields.includes(item.fieldId)) {
        lineData.push({ ...item, value: parseFloat(item.value) })
      }
    })
    if (lineData.length) {
      const randomAssistColorScale = randomString(6)
      const assistLineMark: G2Spec = {
        type: 'lineY',
        zIndex: 0,
        encode: { y: 'value', color: () => randomAssistColorScale },
        scale: {
          color: {
            independent: true
          }
        },
        // 只关闭辅助线独立比例尺的图例，避免影响系列图例
        legend: false,
        data: lineData,
        style: {
          stroke: d => d.color,
          lineDash: d => (d.lineType === 'solid' ? [] : d.lineType === 'dashed' ? [10, 8] : [1, 2]),
          opacity: 1
        },
        labels: [
          {
            text: d => {
              const value = valueFormatter(parseFloat(d.value), axisFormatterCfg)
              return d.name ? `${d.name}: ${value}` : value
            },
            style: {
              fontSize: d => parseInt(d.fontSize),
              fill: d => d.color,
              fillOpacity: 1
            },
            position: position,
            textBaseline: 'bottom',
            transform: [{ type: 'overlapHide' }, { type: 'exceedAdjust' }],
            fontFamily: chart.fontFamily
          }
        ]
      }
      options.children.push(assistLineMark)
    }
    return options
  }

  protected configTooltip(chart: Chart, options: G2Spec, context: Record<string, any>): G2Spec {
    const customAttr: DeepPartial<ChartAttr> = parseJson(chart.customAttr)
    const tooltipAttr = customAttr.tooltip
    const lineMark = options.children[0]
    if (!tooltipAttr.show) {
      defaultsDeep(lineMark, { tooltip: false })
      return options
    }
    const formatterMap = tooltipAttr.seriesTooltipFormatter
      ?.filter(i => i.show)
      .reduce((pre, next) => {
        pre[next.id] = next
        return pre
      }, {}) as Record<string, SeriesFormatter>
    const yAxis = chart.yAxis
    const tooltipOptions: G2Spec = {
      tooltip: d => d,
      interaction: {
        ...lineMark.interaction,
        tooltip: {
          crosshairsLineDash: [4, 4],
          // 关闭 G2 tooltip 悬浮 marker，仅保留辅助线
          marker: false,
          ...getTooltipCrosshairsStyle(chart),
          mount: createTooltipWrapper(chart),
          css: tooltipCss(tooltipAttr),
          position: 'top-right',
          enterable: true,
          render: (e, { title, items: originalItems }) => {
            const titleHtml = TOOLTIP_TITLE_TPL.replace('{title}', title)
            // G2 折线默认只返回当前命中的线段，按维度补齐同一 x 下的系列项
            const fullItems = getLineTooltipSameDimensionItems(
              options,
              customAttr,
              title,
              originalItems,
              context.legendState?.visibleSeries
            )
            let tooltipItems = fullItems
            if (tooltipAttr.seriesTooltipFormatter?.length) {
              tooltipItems = fullItems.filter(item => formatterMap[item.quotaList[0].id])
            }
            const result = []
            const head = originalItems[0]
            sortTooltipItemsByYAxis(chart, tooltipItems).forEach(item => {
              if (item.value === null || item.value === undefined) {
                return
              }
              const formatter = formatterMap[item.quotaList[0].id] ?? yAxis[0]
              const value = valueFormatter(item.value, formatter.formatterCfg)
              result.push({ ...item, name: item.category, value })
            })
            head.dynamicTooltipValue?.forEach(item => {
              const formatter = formatterMap[item.fieldId]
              if (formatter) {
                const value = valueFormatter(parseFloat(item.value), formatter.formatterCfg)
                const name = isEmpty(formatter.chartShowName)
                  ? formatter.name
                  : formatter.chartShowName
                result.push({ color: 'grey', name, value })
              }
            })
            // tooltip 项按维度槽位分组，帮助区分多维度明细
            const itemsHtml = renderGroupedTooltipItems(
              result,
              item => getStackTooltipGroupName(chart, item),
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
    return options
  }

  protected configSlider(chart: Chart, options: G2Spec): G2Spec {
    const { functionCfg } = parseJson(chart.senior)
    if (!functionCfg?.sliderShow) {
      return options
    }
    const lineMark = options.children[0]
    const conditionVisibleDomain = lineMark[LINE_CONDITION_VISIBLE_DOMAIN_KEY]
    const yAxis = parseJson(chart.customStyle)?.yAxis
    const valueField = options.encode?.y
    // 折线图包含 line、point、辅助线等多个 mark，缩略轴切换维度域时需要同步 x 域
    configDimensionSlider(lineMark, options.data, functionCfg, {
      dimensionField: options.encode?.x,
      interactionName: 'lineDimensionSliderFilter',
      syncChildren: true,
      syncMarks: options.children.slice(1),
      ...(yAxis?.axisValue?.auto !== false &&
        typeof valueField === 'string' && {
          valueScale: { field: valueField }
        }),
      // 条件渐变需要和缩略轴可见维度域保持一致
      onSelectedDomainChange: domain => {
        if (conditionVisibleDomain) {
          conditionVisibleDomain.values = domain
        }
      }
    })
    return options
  }

  protected configEmptyDataStrategy(chart: Chart, options: G2Spec): G2Spec {
    const { functionCfg } = parseJson(chart.senior)
    const { emptyDataStrategy } = functionCfg
    const [lineMark] = options.children
    const data = options.data.value
    const multiDimension = chart.yAxis?.length > 1 || chart.xAxisExt?.length > 0
    switch (emptyDataStrategy) {
      case 'breakLine': {
        if (multiDimension) {
          handleBreakLineMultiDimension(data)
        }
        merge(lineMark, { style: { connect: false } })
        break
      }
      case 'ignoreData': {
        handleIgnoreData(data)
        break
      }
      case 'setZero': {
        if (multiDimension) {
          // 多维度置0
          handleSetZeroMultiDimension(data)
        } else {
          // 单维度置0
          handleSetZeroSingleDimension(data)
        }
        break
      }
    }
    return options
  }

  protected setupOptions(chart: Chart, options: G2Spec, context: Record<string, any>): G2Spec {
    return flow(
      this.configTheme,
      this.configEmptyDataStrategy,
      this.configColor,
      this.configLabel,
      this.configBasicStyle,
      this.configConditions,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configAssistLine,
      this.configTooltip,
      this.configSlider
    )(chart, options, context, this)
  }

  constructor(name = 'line') {
    super(name, DEFAULT_DATA)
  }
}
