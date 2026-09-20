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

function sortSeriesItems<T extends { name: string }>(
  items: T[],
  sort?: Axis['sort'] | ChartLegendStyle['sort'],
  customSort: string[] = []
): T[] {
  const result = [...items]
  if (sort === 'asc' || sort === 'desc') {
    result.sort((a, b) => {
      return sort === 'desc' ? b.name.localeCompare(a.name) : a.name.localeCompare(b.name)
    })
  } else if (sort === 'custom' || sort === 'custom_sort') {
    const sortedItems: T[] = []
    for (const name of customSort ?? []) {
      const index = result.findIndex(item => item.name === name)
      if (index !== -1) {
        sortedItems.push(result[index])
        result.splice(index, 1)
      }
    }
    // 未配置到自定义排序中的新类别保留在末尾。
    result.unshift(...sortedItems)
  }
  return result
}

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
      meta: {
        category: {
          type: 'cat'
        }
      },
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
    const xAxisExt = chart.xAxisExt?.[0]
    if (!xAxisExt) {
      const groupOptions = super.configGroupColor(chart, options)
      const color =
        groupOptions.color ?? basicStyle.colors.map(item => hexColorToRGBA(item, basicStyle.alpha))
      return { ...groupOptions, color }
    }

    const categories = new Set<string>()
    options.data?.forEach(item => {
      if (item.category !== null && item.category !== undefined) {
        categories.add(item.category)
      }
    })
    const series = sortSeriesItems(
      [...categories].map(name => ({ name })),
      xAxisExt.sort,
      xAxisExt.customSort
    )
    const seriesColors = new Map(basicStyle.seriesColor?.map(item => [item.id, item.color]))
    const categoryColors = new Map<string, string>()
    // 子类别排序决定系列值域和默认配色；手动设置的类别颜色仍按类别匹配。
    series.forEach((item, index) => {
      const fill =
        seriesColors.get(item.name) ?? basicStyle.colors[index % basicStyle.colors.length]
      const color = isAlphaColor(fill) ? fill : hexColorToRGBA(fill, basicStyle.alpha)
      categoryColors.set(item.name, color)
    })
    // 绘图按类别取色，图库生成的默认图例和 Tooltip 也会沿用这份颜色映射。
    const defaultColor = categoryColors.values().next().value ?? 'rgba(0,0,0,0)'
    const color = ({ category }: Datum) => categoryColors.get(category) ?? defaultColor
    return {
      ...options,
      meta: {
        ...options.meta,
        category: {
          ...options.meta?.category,
          type: 'cat',
          values: series.map(item => item.name)
        }
      },
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
            // 保留图库映射后的 color，格式化名称和数值时不重新分配颜色。
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
      container: getTooltipContainer(`tooltip-${chart.id}`, chart.container),
      itemTpl: TOOLTIP_TPL,
      enterable: true
    }
    return {
      ...options,
      tooltip
    }
  }

  public setupSeriesColor(chart: ChartObj, data?: any[]): ChartBasicStyle['seriesColor'] {
    const items = setUpGroupSeriesColor(chart, data)
    const xAxisExt = chart.xAxisExt?.[0]
    if (!xAxisExt) {
      return items
    }
    // 配色面板与折线使用相同的子类别顺序，不受图例排序影响。
    const series = sortSeriesItems(items, xAxisExt.sort, xAxisExt.customSort)
    const colors = chart.customAttr.basicStyle.colors
    return series.map((item, index) => ({ ...item, color: colors[index % colors.length] }))
  }

  protected configLegend(chart: Chart, options: LineOptions): LineOptions {
    const optionTmp = super.configLegend(chart, options)
    if (!optionTmp.legend) {
      return optionTmp
    }
    const xAxisExt = chart.xAxisExt?.[0]
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
    const color = optionTmp.color
    if (sort && sort !== 'none' && xAxisExt && typeof color === 'function') {
      // 先沿用折线的类别与颜色映射，再单独调整图例顺序，不能改写系列值域。
      const categories = optionTmp.meta.category.values
      const items = categories.map(category => ({
        name: category,
        value: category,
        marker: {
          symbol: icon,
          style: {
            r: size,
            fill: color({ category })
          }
        }
      }))
      optionTmp.legend.items = sortSeriesItems(items, sort, customSort)
    }
    return optionTmp
  }

  protected setupOptions(chart: Chart, options: LineOptions): LineOptions {
    return flow(
      this.configTheme,
      this.configEmptyDataStrategy,
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
