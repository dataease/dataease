import { Column, ColumnOptions } from '@antv/g2plot/esm/plots/column'
import { cloneDeep } from 'lodash-es'
import {
  G2PlotChartView,
  G2PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/g2plot'
import { flow, hexColorToRGBA, parseJson } from '@/views/chart/components/js/util'
import { Datum } from '@antv/g2plot'
import {
  singleDimensionTooltipFormatter,
  valueFormatter
} from '@/views/chart/components/js/formatter'
import {
  BAR_AXIS_TYPE,
  BAR_EDITOR_PROPERTY,
  BAR_EDITOR_PROPERTY_INNER
} from '@/views/chart/components/js/panel/charts/bar/common'
import { getPadding, setGradientColor } from '@/views/chart/components/js/panel/common/common_antv'
import { flow as flowLeft } from 'lodash-es'
import { useI18n } from '@/hooks/web/useI18n'

const { t } = useI18n()
const DEFAULT_DATA: any[] = []
/**
 * 柱状图
 */
export class Bar extends G2PlotChartView<ColumnOptions, Column> {
  properties = BAR_EDITOR_PROPERTY
  propertyInner = {
    ...BAR_EDITOR_PROPERTY_INNER,
    'x-axis-selector': [...BAR_EDITOR_PROPERTY_INNER['x-axis-selector'], 'vPosition'],
    'y-axis-selector': [...BAR_EDITOR_PROPERTY_INNER['y-axis-selector'], 'vPosition'],
    'label-selector': ['vPosition', 'seriesLabelFormatter'],
    'tooltip-selector': ['fontSize', 'color', 'backgroundColor', 'seriesTooltipFormatter']
  }
  protected baseOptions: ColumnOptions = {
    xField: 'field',
    yField: 'value',
    seriesField: 'category',
    isGroup: true,
    data: [],
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
        type: 'tooltip',
        cfg: {
          start: [{ trigger: 'interval:mousemove', action: 'tooltip:show' }],
          end: [{ trigger: 'interval:mouseleave', action: 'tooltip:hide' }]
        }
      },
      {
        type: 'active-region',
        cfg: {
          start: [{ trigger: 'interval:mousemove', action: 'active-region:show' }],
          end: [{ trigger: 'interval:mouseleave', action: 'active-region:hide' }]
        }
      }
    ]
  }

  axis: AxisType[] = [...BAR_AXIS_TYPE]
  axisConfig = {
    ...this['axisConfig'],
    yAxis: {
      name: `${t('chart.drag_block_value_axis')} / ${t('chart.quota')}`,
      type: 'q'
    }
  }

  drawChart(drawOptions: G2PlotDrawOptions<Column>): Column {
    const { chart, container, action } = drawOptions
    if (!chart?.data?.data?.length) {
      return
    }
    const data = cloneDeep(drawOptions.chart.data?.data)
    const initOptions: ColumnOptions = {
      ...this.baseOptions,
      appendPadding: getPadding(chart),
      data
    }
    const options: ColumnOptions = this.setupOptions(chart, initOptions)

    const newChart = new Column(container, options)
    newChart.on('interval:click', action)

    return newChart
  }

  protected configLabel(chart: Chart, options: ColumnOptions): ColumnOptions {
    const tmpOptions = super.configLabel(chart, options)
    if (!tmpOptions.label) {
      return {
        ...tmpOptions,
        label: false
      }
    }
    const labelAttr = parseJson(chart.customAttr).label
    const formatterMap = labelAttr.seriesLabelFormatter?.reduce((pre, next) => {
      pre[next.id] = next
      return pre
    }, {})
    const label = {
      fields: [],
      ...tmpOptions.label,
      formatter: (data: Datum) => {
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
        const value = valueFormatter(data.value, labelCfg.formatterCfg)
        const group = new G2PlotChartView.engine.Group({})
        group.addShape({
          type: 'text',
          attrs: {
            x: 0,
            y: 0,
            text: value,
            textAlign: 'start',
            textBaseline: 'top',
            fontSize: labelCfg.fontSize,
            fill: labelCfg.color
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

  protected configTooltip(chart: Chart, options: ColumnOptions): ColumnOptions {
    let tooltip
    const customAttr: DeepPartial<ChartAttr> = parseJson(chart.customAttr)
    if (customAttr.tooltip) {
      const tooltipAttr = customAttr.tooltip
      if (tooltipAttr.show) {
        tooltip = {
          formatter: function (param: Datum) {
            return singleDimensionTooltipFormatter(param, chart)
          }
        }
      } else {
        tooltip = false
      }
    }
    return { ...options, tooltip }
  }

  protected configBasicStyle(chart: Chart, options: ColumnOptions): ColumnOptions {
    const basicStyle = parseJson(chart.customAttr).basicStyle
    if (basicStyle.gradient) {
      let color = basicStyle.colors
      color = color.map(ele => {
        const tmp = hexColorToRGBA(ele, basicStyle.alpha)
        return setGradientColor(tmp, true, 270)
      })
      options = {
        ...options,
        color
      }
    }
    return options
  }

  protected configYAxis(chart: Chart, options: ColumnOptions): ColumnOptions {
    const tmpOptions = super.configYAxis(chart, options)
    if (!tmpOptions.yAxis) {
      return tmpOptions
    }
    const yAxis = parseJson(chart.customStyle).yAxis
    const axisValue = yAxis.axisValue
    if (tmpOptions.yAxis.label) {
      tmpOptions.yAxis.label.formatter = value => {
        return valueFormatter(value, yAxis.axisLabelFormatter)
      }
    }
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

  protected setupOptions(chart: Chart, options: ColumnOptions): ColumnOptions {
    return flow(
      this.configTheme,
      this.configBasicStyle,
      this.configLabel,
      this.configTooltip,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configSlider,
      this.configAnalyse,
      this.configEmptyDataStrategy
    )(chart, options)
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    return flowLeft(this.setupVerticalAxis, this.setupVerticalLabel)(chart)
  }

  constructor(name = 'bar', defaultData = DEFAULT_DATA) {
    super(name, defaultData)
  }
}

/**
 * 堆叠柱状图
 */
export class StackBar extends Bar {
  propertyInner = {
    ...this['propertyInner'],
    'label-selector': [...BAR_EDITOR_PROPERTY_INNER['label-selector'], 'vPosition']
  }
  protected configLabel(chart: Chart, options: ColumnOptions): ColumnOptions {
    const baseOptions = super.configLabel(chart, options)
    if (!baseOptions.label) {
      return baseOptions
    }
    const { label: labelAttr } = parseJson(chart.customAttr)
    const label = {
      ...baseOptions.label,
      formatter: function (param: Datum) {
        return valueFormatter(param.value, labelAttr.labelFormatter)
      }
    }
    return {
      ...baseOptions,
      label
    }
  }

  protected configTooltip(chart: Chart, options: ColumnOptions): ColumnOptions {
    const tooltipAttr = parseJson(chart.customAttr).tooltip
    if (!tooltipAttr.show) {
      return {
        ...options,
        tooltip: false
      }
    }
    const tooltip = {
      formatter: (param: Datum) => {
        const obj = { name: param.category, value: param.value }
        const res = valueFormatter(param.value, tooltipAttr.tooltipFormatter)
        obj.value = res ?? ''
        return obj
      }
    }
    return {
      ...options,
      tooltip
    }
  }
  constructor(name = 'bar-stack') {
    super(name)
    this.baseOptions = {
      ...this.baseOptions,
      isStack: true,
      isGroup: false
    }
    this.axis = [...this.axis, 'extStack']
  }
}

/**
 * 分组柱状图
 */
export class GroupBar extends StackBar {
  axisConfig = {
    ...this['axisConfig'],
    yAxis: {
      name: `${t('chart.drag_block_value_axis')} / ${t('chart.quota')}`,
      type: 'q',
      limit: 1
    }
  }

  constructor(name = 'bar-group') {
    super(name)
    this.baseOptions = {
      ...this.baseOptions,
      isGroup: true,
      isStack: false
    }
    this.axis = [...BAR_AXIS_TYPE, 'xAxisExt']
  }
}

/**
 * 分组堆叠柱状图
 */
export class GroupStackBar extends StackBar {
  protected configTheme(chart: Chart, options: ColumnOptions): ColumnOptions {
    const baseOptions = super.configTheme(chart, options)
    const baseTheme = baseOptions.theme as object
    const theme = {
      ...baseTheme,
      innerLabels: {
        offset: 0
      }
    }
    return {
      ...options,
      theme
    }
  }

  protected configTooltip(chart: Chart, options: ColumnOptions): ColumnOptions {
    const tooltipAttr = parseJson(chart.customAttr).tooltip
    if (!tooltipAttr.show) {
      return {
        ...options,
        tooltip: false
      }
    }
    const tooltip = {
      fields: [],
      formatter: (param: Datum) => {
        const obj = { name: `${param.category} - ${param.group}`, value: param.value }
        obj.value = valueFormatter(param.value, tooltipAttr.tooltipFormatter)
        return obj
      }
    }
    return {
      ...options,
      tooltip
    }
  }
  constructor(name = 'bar-group-stack') {
    super(name)
    this.baseOptions = {
      ...this.baseOptions,
      isGroup: true,
      groupField: 'group'
    }
    this.axis = [...this.axis, 'extStack']
  }
}

/**
 * 百分比堆叠柱状图
 */
export class PercentageStackBar extends GroupStackBar {
  propertyInner = {
    ...this['propertyInner'],
    'label-selector': ['color', 'fontSize', 'vPosition', 'reserveDecimalCount'],
    'tooltip-selector': ['color', 'fontSize']
  }
  protected configLabel(chart: Chart, options: ColumnOptions): ColumnOptions {
    const baseOptions = super.configLabel(chart, options)
    if (!baseOptions.label) {
      return baseOptions
    }
    const { customAttr } = chart
    const l = parseJson(customAttr).label
    const label = {
      ...baseOptions.label,
      formatter: function (param: Datum) {
        if (!param.value) {
          return '0%'
        }
        return (Math.round(param.value * 10000) / 100).toFixed(l.reserveDecimalCount) + '%'
      }
    }
    return {
      ...baseOptions,
      label
    }
  }

  protected configTooltip(chart: Chart, options: ColumnOptions): ColumnOptions {
    const tooltipAttr = parseJson(chart.customAttr).tooltip
    if (!tooltipAttr.show) {
      return {
        ...options,
        tooltip: {
          showContent: false
        }
      }
    }
    const { customAttr } = chart
    const l = parseJson(customAttr).label
    const tooltip = {
      formatter: (param: Datum) => {
        const obj = { name: param.category, value: param.value }
        obj.value = (Math.round(param.value * 10000) / 100).toFixed(l.reserveDecimalCount) + '%'
        return obj
      }
    }
    return {
      ...options,
      tooltip
    }
  }
  protected setupOptions(chart: Chart, options: ColumnOptions): ColumnOptions {
    return flow(
      this.configTheme,
      this.configBasicStyle,
      this.configLabel,
      this.configTooltip,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configSlider,
      this.configEmptyDataStrategy
    )(chart, options)
  }
  constructor() {
    super('percentage-bar-stack')
    this.baseOptions = {
      ...this.baseOptions,
      isStack: true,
      isPercent: true,
      isGroup: false,
      groupField: undefined
    }
    this.properties = this.properties.filter(item => item !== 'assist-line')
    this.axis = [...BAR_AXIS_TYPE, 'extStack']
  }
}
