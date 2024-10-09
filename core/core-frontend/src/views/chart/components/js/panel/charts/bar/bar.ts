import type { Column, ColumnOptions } from '@antv/g2plot/esm/plots/column'
import { cloneDeep, each, groupBy, isEmpty } from 'lodash-es'
import {
  G2PlotChartView,
  G2PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/g2plot'
import {
  flow,
  hexColorToRGBA,
  parseJson,
  setUpGroupSeriesColor,
  setUpStackSeriesColor
} from '@/views/chart/components/js/util'
import type { Datum } from '@antv/g2plot'
import { formatterItem, valueFormatter } from '@/views/chart/components/js/formatter'
import {
  BAR_AXIS_TYPE,
  BAR_EDITOR_PROPERTY,
  BAR_EDITOR_PROPERTY_INNER
} from '@/views/chart/components/js/panel/charts/bar/common'
import {
  getLabel,
  getPadding,
  setGradientColor
} from '@/views/chart/components/js/panel/common/common_antv'
import { useI18n } from '@/hooks/web/useI18n'
import { DEFAULT_LABEL } from '@/views/chart/components/editor/util/chart'
import { clearExtremum, extremumEvt } from '@/views/chart/components/js/extremumUitl'
import { Group } from '@antv/g-canvas'

const { t } = useI18n()
const DEFAULT_DATA: any[] = []
/**
 * 柱状图
 */
export class Bar extends G2PlotChartView<ColumnOptions, Column> {
  properties = BAR_EDITOR_PROPERTY
  propertyInner = {
    ...BAR_EDITOR_PROPERTY_INNER,
    'basic-style-selector': [...BAR_EDITOR_PROPERTY_INNER['basic-style-selector'], 'seriesColor'],
    'label-selector': ['vPosition', 'seriesLabelFormatter', 'showExtremum'],
    'tooltip-selector': ['fontSize', 'color', 'backgroundColor', 'seriesTooltipFormatter', 'show'],
    'y-axis-selector': [...BAR_EDITOR_PROPERTY_INNER['y-axis-selector'], 'axisLabelFormatter']
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

  async drawChart(drawOptions: G2PlotDrawOptions<Column>): Promise<Column> {
    const { chart, container, action } = drawOptions
    if (!chart?.data?.data?.length) {
      chart.container = container
      clearExtremum(chart)
      return
    }
    const data = cloneDeep(drawOptions.chart.data?.data)
    const initOptions: ColumnOptions = {
      ...this.baseOptions,
      appendPadding: getPadding(chart),
      data
    }
    const options: ColumnOptions = this.setupOptions(chart, initOptions)
    let newChart = null
    const { Column: ColumnClass } = await import('@antv/g2plot/esm/plots/column')
    newChart = new ColumnClass(container, options)
    newChart.on('interval:click', action)
    extremumEvt(newChart, chart, options, container)
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
    const { label: labelAttr } = parseJson(chart.customAttr)
    const formatterMap = labelAttr.seriesLabelFormatter?.reduce((pre, next) => {
      pre[next.id] = next
      return pre
    }, {})
    // 默认是灰色
    tmpOptions.label.style.fill = DEFAULT_LABEL.color
    const label = {
      fields: [],
      ...tmpOptions.label,
      formatter: (data: Datum, _point) => {
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
        const value = valueFormatter(data.value, labelCfg.formatterCfg)
        const group = new Group({})
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
    return super.configMultiSeriesTooltip(chart, options)
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
    if (basicStyle.radiusColumnBar === 'roundAngle') {
      const columnStyle = {
        radius: [
          basicStyle.columnBarRightAngleRadius,
          basicStyle.columnBarRightAngleRadius,
          basicStyle.columnBarRightAngleRadius,
          basicStyle.columnBarRightAngleRadius
        ]
      }
      options = {
        ...options,
        columnStyle
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
      this.configEmptyDataStrategy,
      this.configColor,
      this.configBasicStyle,
      this.configLabel,
      this.configTooltip,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configSlider,
      this.configAnalyse
    )(chart, options, {}, this)
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    chart.senior.functionCfg.emptyDataStrategy = 'ignoreData'
    return chart
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
    'label-selector': [
      ...BAR_EDITOR_PROPERTY_INNER['label-selector'],
      'vPosition',
      'showTotal',
      'totalColor',
      'totalFontSize',
      'totalFormatter',
      'showStackQuota'
    ],
    'tooltip-selector': ['fontSize', 'color', 'backgroundColor', 'tooltipFormatter', 'show']
  }
  protected configLabel(chart: Chart, options: ColumnOptions): ColumnOptions {
    let label = getLabel(chart)
    if (!label) {
      return options
    }
    options = { ...options, label }
    const { label: labelAttr } = parseJson(chart.customAttr)
    if (labelAttr.showStackQuota || labelAttr.showStackQuota === undefined) {
      label.style.fill = labelAttr.color
      label = {
        ...label,
        formatter: function (param: Datum) {
          return valueFormatter(param.value, labelAttr.labelFormatter)
        }
      }
    } else {
      label = false
    }
    if (labelAttr.showTotal) {
      const formatterCfg = labelAttr.labelFormatter ?? formatterItem
      each(groupBy(options.data, 'field'), (values, key) => {
        const total = values.reduce((a, b) => a + b.value, 0)
        const value = valueFormatter(total, formatterCfg)
        if (!options.annotations) {
          options = {
            ...options,
            annotations: []
          }
        }
        options.annotations.push({
          type: 'text',
          position: [key, total],
          content: `${value}`,
          style: {
            textAlign: 'center',
            fontSize: labelAttr.fontSize,
            fill: labelAttr.color
          },
          offsetY: -(parseInt(labelAttr.fontSize as unknown as string) / 2)
        })
      })
    }
    return {
      ...options,
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
        const name = isEmpty(param.category) ? param.field : param.category
        const obj = { name, value: param.value }
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

  protected configColor(chart: Chart, options: ColumnOptions): ColumnOptions {
    return this.configStackColor(chart, options)
  }

  protected configData(chart: Chart, options: ColumnOptions): ColumnOptions {
    const { xAxis, extStack, yAxis } = chart
    const mainSort = xAxis.some(axis => axis.sort !== 'none')
    const subSort = extStack.some(axis => axis.sort !== 'none')
    if (mainSort || subSort) {
      return options
    }
    const quotaSort = yAxis?.[0].sort !== 'none'
    if (!quotaSort || !extStack.length || !yAxis.length) {
      return options
    }
    const { data } = options
    const mainAxisValueMap = data.reduce((p, n) => {
      p[n.field] = p[n.field] ? p[n.field] + n.value : n.value || 0
      return p
    }, {})
    const sort = yAxis[0].sort
    data.sort((p, n) => {
      if (sort === 'asc') {
        return mainAxisValueMap[p.field] - mainAxisValueMap[n.field]
      } else {
        return mainAxisValueMap[n.field] - mainAxisValueMap[p.field]
      }
    })
    return options
  }

  public setupSeriesColor(chart: ChartObj, data?: any[]): ChartBasicStyle['seriesColor'] {
    return setUpStackSeriesColor(chart, data)
  }

  protected setupOptions(chart: Chart, options: ColumnOptions): ColumnOptions {
    return flow(
      this.configTheme,
      this.configEmptyDataStrategy,
      this.configColor,
      this.configBasicStyle,
      this.configLabel,
      this.configTooltip,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configSlider,
      this.configAnalyse,
      this.configData
    )(chart, options, {}, this)
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
  propertyInner = {
    ...this['propertyInner'],
    'label-selector': [...BAR_EDITOR_PROPERTY_INNER['label-selector'], 'vPosition', 'showExtremum']
  }
  axisConfig = {
    ...this['axisConfig'],
    yAxis: {
      name: `${t('chart.drag_block_value_axis')} / ${t('chart.quota')}`,
      type: 'q',
      limit: 1
    }
  }

  protected configLabel(chart: Chart, options: ColumnOptions): ColumnOptions {
    const baseOptions = super.configLabel(chart, options)
    if (!baseOptions.label) {
      return baseOptions
    }
    const { label: labelAttr } = parseJson(chart.customAttr)
    baseOptions.label.style.fill = labelAttr.color
    const label = {
      ...baseOptions.label,
      formatter: function (param: Datum, _point) {
        if (param.EXTREME) {
          return ''
        }
        const value = valueFormatter(param.value, labelAttr.labelFormatter)
        return labelAttr.childrenShow ? value : null
      }
    }
    return {
      ...baseOptions,
      label
    }
  }

  protected configColor(chart: Chart, options: ColumnOptions): ColumnOptions {
    return this.configGroupColor(chart, options)
  }

  public setupSeriesColor(chart: ChartObj, data?: any[]): ChartBasicStyle['seriesColor'] {
    return setUpGroupSeriesColor(chart, data)
  }

  protected setupOptions(chart: Chart, options: ColumnOptions): ColumnOptions {
    return flow(
      this.configTheme,
      this.configEmptyDataStrategy,
      this.configColor,
      this.configBasicStyle,
      this.configLabel,
      this.configTooltip,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configSlider,
      this.configAnalyse
    )(chart, options, {}, this)
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
  propertyInner = {
    ...this['propertyInner'],
    'label-selector': [...BAR_EDITOR_PROPERTY_INNER['label-selector'], 'vPosition']
  }
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

  protected configLabel(chart: Chart, options: ColumnOptions): ColumnOptions {
    const baseOptions = super.configLabel(chart, options)
    if (!baseOptions.label) {
      return baseOptions
    }
    const { label: labelAttr } = parseJson(chart.customAttr)
    baseOptions.label.style.fill = labelAttr.color
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

  protected setupOptions(chart: Chart, options: ColumnOptions): ColumnOptions {
    return flow(
      this.configTheme,
      this.configEmptyDataStrategy,
      this.configColor,
      this.configBasicStyle,
      this.configLabel,
      this.configTooltip,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configSlider,
      this.configAnalyse
    )(chart, options, {}, this)
  }

  constructor(name = 'bar-group-stack') {
    super(name)
    this.baseOptions = {
      ...this.baseOptions,
      isGroup: true,
      groupField: 'group'
    }
    this.axis = [...this.axis, 'xAxisExt', 'extStack']
  }
}

/**
 * 百分比堆叠柱状图
 */
export class PercentageStackBar extends GroupStackBar {
  propertyInner = {
    ...this['propertyInner'],
    'label-selector': ['color', 'fontSize', 'vPosition', 'reserveDecimalCount'],
    'tooltip-selector': ['color', 'fontSize', 'backgroundColor', 'show']
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
      this.configEmptyDataStrategy,
      this.configColor,
      this.configBasicStyle,
      this.configLabel,
      this.configTooltip,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configSlider
    )(chart, options, {}, this)
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
