import {
  G2PlotChartView,
  G2PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/g2plot'
import type { ScatterOptions, Scatter as G2Scatter } from '@antv/g2plot/esm/plots/scatter'
import { flow, parseJson, setUpSingleDimensionSeriesColor } from '../../../util'
import { valueFormatter } from '../../../formatter'
import {
  configPlotTooltipEvent,
  getPadding,
  getTooltipContainer,
  TOOLTIP_TPL
} from '../../common/common_antv'
import { useI18n } from '@/hooks/web/useI18n'
import { defaults, isEmpty } from 'lodash-es'
import { DEFAULT_LEGEND_STYLE } from '@/views/chart/components/editor/util/chart'
import { type Datum } from '@antv/g2plot/esm'

const { t } = useI18n()

const DEFAULT_LIGHTNESS_RANGE: [number, number] = [0.2, 1]

function normalizeRatio(val: number, min: number, max: number): number {
  if (max === min) {
    return 0.5
  }
  return (val - min) / (max - min)
}

function safeToNumber(value: unknown): number | undefined {
  const result = Number(value)
  return Number.isFinite(result) ? result : undefined
}

function getScatterTooltipFormatter(
  itemName: string,
  quotaList: Array<{ id: string }>,
  formatterMap: Record<string, SeriesFormatter>
) {
  const fieldIndexMap = {
    x: 0,
    y: Math.min(1, quotaList.length - 1),
    value: Math.min(1, quotaList.length - 1),
    popSize: Math.min(2, quotaList.length - 1),
    lightness: Math.min(3, quotaList.length - 1)
  }
  const fieldSuffixMap = {
    x: 'xAxis',
    y: 'yAxis',
    value: 'yAxis',
    popSize: 'extBubble',
    lightness: 'yAxisExt'
  }
  const index = fieldIndexMap[itemName]
  if (index === undefined || index < 0) {
    return undefined
  }
  const quota = quotaList[index]
  if (!quota) {
    return undefined
  }
  const suffix = fieldSuffixMap[itemName]
  return formatterMap[`${quota.id}-${suffix}`] ?? formatterMap[quota.id]
}

/**
 * 多维散点图
 */
export class MultiScatter extends G2PlotChartView<ScatterOptions, G2Scatter> {
  properties: EditorProperty[] = [
    'background-overall-component',
    'border-style',
    'basic-style-selector',
    'x-axis-selector',
    'y-axis-selector',
    'title-selector',
    'label-selector',
    'tooltip-selector',
    'legend-selector',
    'jump-set',
    'linkage'
  ]
  propertyInner: EditorPropertyInner = {
    'basic-style-selector': [
      'colors',
      'alpha',
      'scatterSymbol',
      'scatterSymbolSize',
      'seriesColor'
    ],
    'label-selector': ['fontSize', 'color', 'labelFormatter'],
    'tooltip-selector': ['fontSize', 'color', 'backgroundColor', 'seriesTooltipFormatter', 'show'],
    'x-axis-selector': [
      'position',
      'name',
      'color',
      'fontSize',
      'axisLine',
      'splitLine',
      'axisForm',
      'axisLabel'
    ],
    'y-axis-selector': [
      'position',
      'name',
      'color',
      'fontSize',
      'axisValue',
      'axisLine',
      'splitLine',
      'axisForm',
      'axisLabel',
      'axisLabelFormatter'
    ],
    'title-selector': [
      'title',
      'fontSize',
      'color',
      'hPosition',
      'isItalic',
      'isBolder',
      'remarkShow',
      'fontFamily',
      'letterSpace',
      'fontShadow'
    ],
    'legend-selector': ['icon', 'orient', 'color', 'fontSize', 'hPosition', 'vPosition']
  }
  axis: AxisType[] = [
    'xAxis',
    'yAxis',
    'yAxisExt',
    'extColor',
    'extBubble',
    'extLabel',
    'extTooltip'
  ]
  axisConfig: AxisConfig = {
    extColor: {
      name: `${t('chart.color')} / ${t('chart.dimension')}`,
      type: 'd',
      limit: 1,
      allowEmpty: false
    },
    xAxis: {
      name: `${t('chart.x_axis')} / ${t('chart.time_dimension_or_quota')}`,
      limit: 1,
      allowEmpty: false
    },
    yAxis: {
      ...this['axisConfig'].yAxis,
      type: 'q',
      name: `${t('chart.y_axis')} / ${t('chart.quota')}`,
      limit: 1,
      allowEmpty: false
    },
    yAxisExt: {
      ...this['axisConfig'].yAxis,
      type: 'q',
      name: `${t('chart.lightness')} / ${t('chart.quota')}`,
      limit: 1,
      allowEmpty: true
    },
    extBubble: {
      name: `${t('chart.radar_size')} / ${t('chart.quota')}`,
      type: 'q',
      limit: 1,
      allowEmpty: true
    }
  }
  async drawChart(drawOptions: G2PlotDrawOptions<G2Scatter>): Promise<G2Scatter> {
    const { chart, container, action } = drawOptions
    if (!chart.data?.data) {
      return
    }
    const sourceData = chart.data.data
    const scatterContext = this.buildScatterDataContext(sourceData)
    const meta: Record<string, any> = {
      [scatterContext.colorField]: {
        type: 'cat'
      }
    }
    // 时间维度 x 轴使用 cat 类型，保持类别顺序（使用 cat 而非 timeCat，兼容各种日期格式）
    if (scatterContext.isTimeX) {
      meta[scatterContext.xField] = { type: 'cat' }
    }
    const baseOptions: ScatterOptions = {
      data: scatterContext.data,
      xField: scatterContext.xField,
      yField: scatterContext.yField,
      colorField: scatterContext.colorField,
      rawFields: ['lightness'],
      meta,
      appendPadding: getPadding(chart),
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
        }
      ]
    }
    const options = this.setupOptions(chart, baseOptions)
    const { Scatter: G2Scatter } = await import('@antv/g2plot/esm/plots/scatter')
    const newChart = new G2Scatter(container, options)
    newChart.on('point:click', e => {
      action(e)
    })
    if (options.label) {
      newChart.on('label:click', e => {
        action({
          x: e.x,
          y: e.y,
          data: {
            data: e.data.data
          }
        })
      })
    }
    configPlotTooltipEvent(chart, newChart as unknown as any)
    return newChart
  }

  /**
   * 自定义颜色按颜色维度的值映射
   */
  protected configColor(chart: Chart, options: ScatterOptions): ScatterOptions {
    if (!chart.extColor?.length) {
      return options
    }
    return this.configSingleDimensionColor(chart, options)
  }

  /**
   * 为配色方案的自定义面板提供颜色维度的值列表
   */
  public setupSeriesColor(chart: ChartObj, data?: any[]): ChartBasicStyle['seriesColor'] {
    if (!chart.extColor?.length) {
      return []
    }
    return setUpSingleDimensionSeriesColor(chart, data)
  }

  protected configBasicStyle(chart: Chart, options: ScatterOptions): ScatterOptions {
    const customAttr = parseJson(chart.customAttr)
    const basicStyle = customAttr.basicStyle
    const data = (options.data || []) as Record<string, unknown>[]
    const hasBubbleMetric = data.some(item => Number.isFinite(Number(item.popSize)))
    const hasLightnessMetric = data.some(item => Number.isFinite(Number(item.lightness)))
    let nextOptions: ScatterOptions
    if (chart.extBubble?.length || hasBubbleMetric) {
      nextOptions = {
        ...options,
        size: [5, 30],
        sizeField: 'popSize',
        shape: basicStyle.scatterSymbol
      }
    } else {
      nextOptions = {
        ...options,
        size: basicStyle.scatterSymbolSize,
        shape: basicStyle.scatterSymbol
      }
    }

    if (!hasLightnessMetric) {
      return nextOptions
    }

    const lightValues = data
      .map(item => safeToNumber(item.lightness))
      .filter((v): v is number => Number.isFinite(v))
    const min = Math.min(...lightValues)
    const max = Math.max(...lightValues)
    const [opacityMin, opacityMax] = DEFAULT_LIGHTNESS_RANGE
    return {
      ...nextOptions,
      pointStyle: (datum: Record<string, unknown>) => {
        const lv = safeToNumber(datum.lightness ?? 1)
        const ratio = lv !== undefined ? normalizeRatio(lv, min, max) : 0.5
        const opacity = opacityMin + ratio * (opacityMax - opacityMin)
        return {
          stroke: 0,
          shadowBlur: 10,
          fillOpacity: Number(opacity ?? 1)
        }
      }
    }
  }

  protected configYAxis(chart: Chart, options: ScatterOptions): ScatterOptions {
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
    return tmpOptions
  }

  protected configTooltip(chart: Chart, options: ScatterOptions): ScatterOptions {
    const customAttr: DeepPartial<ChartAttr> = parseJson(chart.customAttr)
    const tooltipAttr = customAttr.tooltip
    if (!tooltipAttr.show) {
      return {
        ...options,
        tooltip: false
      }
    }
    // 判断 x 轴是否为日期格式字段
    const xAxisField = chart.xAxis?.[0]
    const isTimeX =
      xAxisField &&
      (xAxisField.groupType === 'd' || (xAxisField.deType != null && xAxisField.deType === 1))
    // 日期 x 轴的字段名称
    const xFieldName = xAxisField
      ? isEmpty(xAxisField.chartShowName)
        ? xAxisField.name
        : xAxisField.chartShowName
      : ''

    const formatterMap = tooltipAttr.seriesTooltipFormatter
      ?.filter(i => i.show)
      .reduce((pre, next) => {
        pre[next.seriesId] = next
        return pre
      }, {}) as Record<string, SeriesFormatter>
    // 构建的字段列表
    const manualFields = ['x', 'y', 'popSize', 'lightness'] as const
    const tooltip: ScatterOptions['tooltip'] = {
      showTitle: true,
      title: (_title, datum) => {
        // 使用颜色维度(extColor)的值作为 tooltip 标题
        return String(datum?.color ?? datum?.category ?? _title ?? '')
      },
      customItems: originalItems => {
        if (!tooltipAttr.seriesTooltipFormatter?.length) {
          return originalItems
        }
        const head = originalItems[0]
        if (!head?.data?.quotaList) {
          return originalItems
        }
        const datum = head.data
        const pointColor = head.color
        const result = []
        // 当 x 轴是日期格式时，始终作为第一项显示在 tooltip 中，
        // 不需要在提示中配置显示与否，显示格式与横轴 label 一致
        if (isTimeX) {
          const xValue = datum.x
          if (xValue != null) {
            const allFormatterMap = tooltipAttr.seriesTooltipFormatter?.reduce((pre, next) => {
              pre[next.seriesId] = next
              return pre
            }, {}) as Record<string, SeriesFormatter>
            const xFormatter = getScatterTooltipFormatter('x', datum.quotaList, allFormatterMap)
            const name = xFormatter
              ? isEmpty(xFormatter.chartShowName)
                ? xFormatter.name
                : xFormatter.chartShowName
              : xFieldName
            result.push({ color: pointColor, name, value: String(xValue), marker: true })
          }
        }

        // 按固定顺序遍历字段，从 datum 中直接取值
        manualFields.forEach(fieldKey => {
          if (fieldKey === 'x' && isTimeX) return
          const rawValue = datum[fieldKey]
          if (rawValue === undefined || rawValue === null) {
            return
          }
          const formatter = getScatterTooltipFormatter(fieldKey, datum.quotaList, formatterMap)
          if (!formatter) {
            return
          }
          const isTimeStr = fieldKey === 'x' && typeof rawValue === 'string'
          const value = isTimeStr
            ? String(rawValue)
            : valueFormatter(parseFloat(String(rawValue)), formatter.formatterCfg)
          const name = isEmpty(formatter.chartShowName) ? formatter.name : formatter.chartShowName
          result.push({ color: pointColor, name, value, marker: true })
        })
        datum.dynamicTooltipValue?.forEach(item => {
          const formatter = formatterMap[item.fieldId]
          if (formatter) {
            const value = valueFormatter(parseFloat(item.value), formatter.formatterCfg)
            const name = isEmpty(formatter.chartShowName) ? formatter.name : formatter.chartShowName
            result.push({ color: 'grey', name, value, marker: true })
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

  protected configLegend(chart: Chart, options: ScatterOptions): ScatterOptions {
    const optionTmp = super.configLegend(chart, options)
    if (!optionTmp.legend) {
      return optionTmp
    }
    const customStyle = parseJson(chart.customStyle)
    let size
    if (customStyle && customStyle.legend) {
      size = defaults(JSON.parse(JSON.stringify(customStyle.legend)), DEFAULT_LEGEND_STYLE).size
    } else {
      size = DEFAULT_LEGEND_STYLE.size
    }
    ;(optionTmp.legend.marker as Record<string, any>).style = style => {
      return {
        r: size,
        fill: style.fill
      }
    }
    return optionTmp
  }

  protected configLabel(chart: Chart, options: ScatterOptions): ScatterOptions {
    const tmpOption = super.configLabel(chart, options)
    if (!tmpOption.label) {
      return options
    }
    const { label: labelAttr } = parseJson(chart.customAttr)
    const layout = []
    if (!labelAttr.fullDisplay) {
      layout.push({ type: 'hide-overlap' })
      layout.push({ type: 'limit-in-shape' })
    }
    const label = {
      ...tmpOption.label,
      offset: 0,
      fontSize: labelAttr.fontSize,
      fontFamily: chart.fontFamily,
      fill: labelAttr.color,
      layout,
      formatter: function (data: Datum) {
        const datum = data as Record<string, unknown>
        const yValue = datum.y ?? data.value
        const xValue = datum.x
        const yText = valueFormatter(yValue as number, labelAttr.labelFormatter)
        const xText =
          typeof xValue === 'string'
            ? xValue
            : valueFormatter(xValue as number, labelAttr.labelFormatter)
        return yText + '\n' + xText
      }
    }
    return {
      ...tmpOption,
      label
    }
  }
  setupDefaultOptions(chart: ChartObj): ChartObj {
    chart.customStyle.yAxis.splitLine = {
      ...chart.customStyle.yAxis.splitLine,
      show: false
    }
    chart.customStyle.yAxis.axisLine = {
      ...chart.customStyle.yAxis.axisLine,
      show: true
    }
    return chart
  }

  protected setupOptions(chart: Chart, options: ScatterOptions) {
    return flow(
      this.configTheme,
      this.configColor,
      this.configLabel,
      this.configTooltip,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configSlider,
      this.configBasicStyle
    )(chart, options, {}, this)
  }

  private buildScatterDataContext(sourceData: Record<string, unknown>[]) {
    const hasTimeX = sourceData.some(row => row.xLabel != null)

    const multiData = sourceData
      .map(row => {
        let x: number | string | undefined
        if (hasTimeX) {
          x = row.xLabel != null ? String(row.xLabel) : undefined
        } else {
          x = safeToNumber(row.x ?? row.xAxis ?? row.value)
        }
        const y = safeToNumber(row.y ?? row.yAxis ?? row.value)
        if (x === undefined || !Number.isFinite(y)) {
          return undefined
        }
        const color = String(row.category ?? row.genre ?? row.field ?? row.color ?? 'default')
        const popSize = safeToNumber(row.popSize ?? row.extBubble ?? row.size)
        const lightness = safeToNumber(row.lightness ?? row.extColor)
        return {
          ...row,
          x,
          y,
          color,
          popSize,
          lightness
        }
      })
      .filter(item => !!item)

    if (!multiData.length) {
      return {
        data: sourceData,
        xField: 'field',
        yField: 'value',
        colorField: 'category',
        lightness: 'lightness',
        isTimeX: false
      }
    }

    return {
      data: multiData,
      xField: 'x',
      yField: 'y',
      colorField: 'color',
      isTimeX: hasTimeX
    }
  }

  constructor() {
    super('multi-scatter', [])
  }
}
