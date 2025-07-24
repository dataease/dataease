import {
  G2PlotChartView,
  G2PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/g2plot'
import type { Line as G2Line, LineOptions } from '@antv/g2plot/esm/plots/line'
import {
  configPlotTooltipEvent,
  getPadding,
  getTooltipContainer,
  TOOLTIP_TPL
} from '../../common/common_antv'
import {
  convertToAlphaColor,
  flow,
  getLineConditions,
  getLineLabelColorByCondition,
  hexColorToRGBA,
  isAlphaColor,
  parseJson,
  setUpGroupSeriesColor
} from '@/views/chart/components/js/util'
import { cloneDeep, defaults, isEmpty } from 'lodash-es'
import {
  calcNiceMinValue,
  listenYAxisNiceMinEvents,
  valueFormatter
} from '@/views/chart/components/js/formatter'
import {
  LINE_AXIS_TYPE,
  LINE_EDITOR_PROPERTY,
  LINE_EDITOR_PROPERTY_INNER
} from '@/views/chart/components/js/panel/charts/line/common'
import type { Datum } from '@antv/g2plot/esm/types/common'
import { useI18n } from '@/hooks/web/useI18n'
import { DEFAULT_LABEL, DEFAULT_LEGEND_STYLE } from '@/views/chart/components/editor/util/chart'
import { clearExtremum, extremumEvt } from '@/views/chart/components/js/extremumUitl'
import { Group } from '@antv/g-canvas'

const { t } = useI18n()
const DEFAULT_DATA = []

/**
 * 折线图
 */
export class Line extends G2PlotChartView<LineOptions, G2Line> {
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

  async drawChart(drawOptions: G2PlotDrawOptions<G2Line>): Promise<G2Line> {
    const { chart, action, container } = drawOptions
    chart.container = container
    if (!chart.data?.data?.length) {
      clearExtremum(chart)
      return
    }
    const data = cloneDeep(chart.data.data)
    // custom color
    const customAttr = parseJson(chart.customAttr)
    const color = customAttr.basicStyle.colors
    // options
    const initOptions: LineOptions = {
      data,
      xField: 'field',
      yField: 'value',
      seriesField: 'category',
      appendPadding: getPadding(chart),
      color,
      interactions: [
        {
          type: 'legend-active',
          cfg: {
            start: [{ trigger: 'legend-item:mouseenter', action: ['element-active:reset'] }],
            end: [{ trigger: 'legend-item:mouseleave', action: ['element-active:reset'] }]
          }
        },
        {
          type: 'legend-filter',
          cfg: {
            start: [
              {
                trigger: 'legend-item:click',
                action: [
                  'list-unchecked:toggle',
                  'data-filter:filter',
                  'element-active:reset',
                  'element-highlight:reset'
                ]
              }
            ]
          }
        },
        {
          type: 'active-region',
          cfg: {
            start: [{ trigger: 'element:mousemove', action: 'active-region:show' }],
            end: [{ trigger: 'element:mouseleave', action: 'active-region:hide' }]
          }
        }
      ]
    }
    const options = this.setupOptions(chart, initOptions)
    const { Line: G2Line } = await import('@antv/g2plot/esm/plots/line')
    // 开始渲染
    const newChart = new G2Line(container, options)

    newChart.on('point:click', action)
    extremumEvt(newChart, chart, options, container)
    configPlotTooltipEvent(chart, newChart)
    listenYAxisNiceMinEvents(chart, newChart)
    return newChart
  }

  protected configLabel(chart: Chart, options: LineOptions): LineOptions {
    const tmpOptions = super.configLabel(chart, options)
    if (!tmpOptions.label) {
      return {
        ...tmpOptions,
        label: false
      }
    }
    const { label: labelAttr, basicStyle } = parseJson(chart.customAttr)
    const conditions = getLineConditions(chart)
    const formatterMap = labelAttr.seriesLabelFormatter?.reduce((pre, next) => {
      pre[next.id] = next
      return pre
    }, {})
    tmpOptions.label.style.fill = DEFAULT_LABEL.color
    const label = {
      fields: [],
      ...tmpOptions.label,
      layout: labelAttr.fullDisplay ? [{ type: 'limit-in-plot' }] : tmpOptions.label.layout,
      formatter: (data: Datum) => {
        if (data.EXTREME) {
          return ''
        }
        if (!labelAttr.seriesLabelFormatter?.length) {
          return data.value
        }
        const labelCfg = formatterMap?.[data.quotaList[0].id] as SeriesFormatter
        if (!labelCfg) {
          return data.value
        }
        if (!labelCfg.show) {
          return
        }
        const position =
          labelCfg.position === 'top'
            ? -2 - basicStyle.lineSymbolSize
            : 10 + basicStyle.lineSymbolSize
        const value = valueFormatter(data.value, labelCfg.formatterCfg)
        const color =
          getLineLabelColorByCondition(conditions, data.value, data.quotaList[0].id) ||
          labelCfg.color
        const group = new Group({})
        group.addShape({
          type: 'text',
          attrs: {
            x: 0,
            y: position,
            text: value,
            textAlign: 'start',
            textBaseline: 'top',
            fontSize: labelCfg.fontSize,
            fontFamily: chart.fontFamily,
            fill: color
          }
        })
        return group
      }
    }
    return {
      ...tmpOptions,
      label
    }
  }

  protected configBasicStyle(chart: Chart, options: LineOptions): LineOptions {
    // size
    const customAttr: DeepPartial<ChartAttr> = parseJson(chart.customAttr)
    const s = JSON.parse(JSON.stringify(customAttr.basicStyle))
    const smooth = s.lineSmooth
    const point = {
      size: s.lineSymbolSize,
      shape: s.lineSymbol
    }
    const lineStyle = {
      lineWidth: s.lineWidth
    }
    return {
      ...options,
      smooth,
      point,
      lineStyle
    }
  }

  protected configCustomColors(chart: Chart, options: LineOptions): LineOptions {
    const basicStyle = parseJson(chart.customAttr).basicStyle
    const color = basicStyle.colors.map(item => hexColorToRGBA(item, basicStyle.alpha))
    return {
      ...options,
      color
    }
  }

  protected configYAxis(chart: Chart, options: LineOptions): LineOptions {
    const tmpOptions = super.configYAxis(chart, options)
    if (!tmpOptions.yAxis) {
      return tmpOptions
    }
    const yAxis = parseJson(chart.customStyle).yAxis
    if (tmpOptions.yAxis.label) {
      tmpOptions.yAxis.label.formatter = value => {
        return valueFormatter(value, yAxis.axisLabelFormatter)
      }
    }
    const axisValue = yAxis.axisValue
    if (!axisValue?.auto) {
      const axis = {
        yAxis: {
          ...tmpOptions.yAxis,
          min: axisValue.min,
          max: axisValue.max,
          minLimit: axisValue.min,
          maxLimit: axisValue.max,
          tickCount: axisValue.splitCount
        }
      }
      return { ...tmpOptions, ...axis }
    }
    if (axisValue?.auto) {
      return calcNiceMinValue(chart, options, tmpOptions)
    }
    return tmpOptions
  }

  protected configTooltip(chart: Chart, options: LineOptions): LineOptions {
    const customAttr: DeepPartial<ChartAttr> = parseJson(chart.customAttr)
    const tooltipAttr = customAttr.tooltip
    if (!tooltipAttr.show) {
      return {
        ...options,
        tooltip: false
      }
    }
    const xAxisExt = chart.xAxisExt
    const formatterMap = tooltipAttr.seriesTooltipFormatter
      ?.filter(i => i.show)
      .reduce((pre, next) => {
        pre[next.id] = next
        return pre
      }, {}) as Record<string, SeriesFormatter>
    const tooltip: LineOptions['tooltip'] = {
      showTitle: true,
      customItems(originalItems) {
        if (!tooltipAttr.seriesTooltipFormatter?.length) {
          return originalItems
        }
        const head = originalItems[0]
        // 非原始数据
        if (!head.data.quotaList) {
          return originalItems
        }
        const result = []
        originalItems
          .filter(item => formatterMap[item.data.quotaList[0].id])
          .forEach(item => {
            const formatter = formatterMap[item.data.quotaList[0].id]
            const value = valueFormatter(parseFloat(item.value as string), formatter.formatterCfg)
            let name = isEmpty(formatter.chartShowName) ? formatter.name : formatter.chartShowName
            if (xAxisExt?.length > 0) {
              name = item.data.category
            }
            result.push({ ...item, name, value })
          })
        head.data.dynamicTooltipValue?.forEach(item => {
          const formatter = formatterMap[item.fieldId]
          if (formatter) {
            const value = valueFormatter(parseFloat(item.value), formatter.formatterCfg)
            const name = isEmpty(formatter.chartShowName) ? formatter.name : formatter.chartShowName
            result.push({ color: 'grey', name, value })
          }
        })
        return result
      },
      container: getTooltipContainer(`tooltip-${chart.id}`),
      itemTpl: TOOLTIP_TPL,
      enterable: true
    }
    return {
      ...options,
      tooltip
    }
  }

  public setupSeriesColor(chart: ChartObj, data?: any[]): ChartBasicStyle['seriesColor'] {
    return setUpGroupSeriesColor(chart, data)
  }

  protected configLegend(chart: Chart, options: LineOptions): LineOptions {
    const optionTmp = super.configLegend(chart, options)
    if (!optionTmp.legend) {
      return optionTmp
    }
    const xAxisExt = chart.xAxisExt[0]
    if (xAxisExt?.customSort?.length > 0) {
      // 图例自定义排序
      const sort = xAxisExt.customSort ?? []
      if (sort?.length) {
        // 用值域限定排序，有可能出现新数据但是未出现在图表上，所以这边要遍历一下子维度，加到后面，让新数据显示出来
        const data = optionTmp.data
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
        optionTmp.meta = {
          ...optionTmp.meta,
          category: {
            type: 'cat',
            values
          }
        }
      }
    }

    const customStyle = parseJson(chart.customStyle)
    let size
    if (customStyle && customStyle.legend) {
      size = defaults(JSON.parse(JSON.stringify(customStyle.legend)), DEFAULT_LEGEND_STYLE).size
    } else {
      size = DEFAULT_LEGEND_STYLE.size
    }

    optionTmp.legend.marker.style = style => {
      return {
        r: size,
        fill: style.stroke
      }
    }
    const { sort, customSort, icon } = customStyle.legend
    if (sort && sort !== 'none' && chart.xAxisExt.length) {
      const customAttr = parseJson(chart.customAttr)
      const { basicStyle } = customAttr
      const seriesMap =
        basicStyle.seriesColor?.reduce((p, n) => {
          p[n.id] = n
          return p
        }, {}) || {}
      const dupCheck = new Set()
      const items = optionTmp.data?.reduce((arr, item) => {
        if (!dupCheck.has(item.category)) {
          const fill =
            seriesMap[item.category]?.color ??
            optionTmp.color[dupCheck.size % optionTmp.color.length]
          dupCheck.add(item.category)
          arr.push({
            name: item.category,
            value: item.category,
            marker: {
              symbol: icon,
              style: {
                r: size,
                fill: isAlphaColor(fill) ? fill : convertToAlphaColor(fill, basicStyle.alpha)
              }
            }
          })
        }
        return arr
      }, [])
      if (sort !== 'custom') {
        items.sort((a, b) => {
          return sort !== 'desc' ? a.name.localeCompare(b.name) : b.name.localeCompare(a.name)
        })
      } else {
        const tmp = []
        ;(customSort || []).forEach(item => {
          const index = items.findIndex(i => i.name === item)
          if (index !== -1) {
            tmp.push(items[index])
            items.splice(index, 1)
          }
        })
        items.unshift(...tmp)
      }
      optionTmp.legend.items = items
      if (xAxisExt?.customSort?.length > 0) {
        delete optionTmp.meta?.category.values
      }
    }
    return optionTmp
  }

  protected setupOptions(chart: Chart, options: LineOptions): LineOptions {
    return flow(
      this.configTheme,
      this.configEmptyDataStrategy,
      this.configGroupColor,
      this.configLabel,
      this.configTooltip,
      this.configBasicStyle,
      this.configCustomColors,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configSlider,
      this.configAnalyse,
      this.configConditions
    )(chart, options)
  }

  constructor(name = 'line') {
    super(name, DEFAULT_DATA)
  }
}
