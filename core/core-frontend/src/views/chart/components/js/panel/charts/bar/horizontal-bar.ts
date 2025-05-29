import {
  G2PlotChartView,
  G2PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/g2plot'
import type { Bar, BarOptions } from '@antv/g2plot/esm/plots/bar'
import {
  configAxisLabelLengthLimit,
  configPlotTooltipEvent,
  configRoundAngle,
  getPadding,
  getTooltipContainer,
  setGradientColor,
  TOOLTIP_TPL
} from '@/views/chart/components/js/panel/common/common_antv'
import { cloneDeep } from 'lodash-es'
import {
  flow,
  hexColorToRGBA,
  parseJson,
  setUpStackSeriesColor
} from '@/views/chart/components/js/util'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import {
  BAR_AXIS_TYPE,
  BAR_EDITOR_PROPERTY,
  BAR_EDITOR_PROPERTY_INNER
} from '@/views/chart/components/js/panel/charts/bar/common'
import type { Datum } from '@antv/g2plot/esm/types/common'
import { useI18n } from '@/hooks/web/useI18n'
import { DEFAULT_BASIC_STYLE, DEFAULT_LABEL } from '@/views/chart/components/editor/util/chart'
import { Group } from '@antv/g-canvas'

const { t } = useI18n()
const DEFAULT_DATA = []

/**
 * 条形图
 */
export class HorizontalBar extends G2PlotChartView<BarOptions, Bar> {
  axisConfig = {
    ...this['axisConfig'],
    xAxis: {
      name: `${t('chart.drag_block_type_axis')} / ${t('chart.dimension')}`,
      type: 'd'
    },
    yAxis: {
      name: `${t('chart.drag_block_value_axis')} / ${t('chart.quota')}`,
      type: 'q'
    }
  }
  properties = BAR_EDITOR_PROPERTY
  propertyInner = {
    ...BAR_EDITOR_PROPERTY_INNER,
    'basic-style-selector': [...BAR_EDITOR_PROPERTY_INNER['basic-style-selector'], 'seriesColor'],
    'label-selector': ['hPosition', 'seriesLabelFormatter'],
    'tooltip-selector': ['fontSize', 'color', 'backgroundColor', 'seriesTooltipFormatter', 'show'],
    'x-axis-selector': [
      ...BAR_EDITOR_PROPERTY_INNER['x-axis-selector'],
      'axisLabelFormatter',
      'axisValue'
    ],
    'y-axis-selector': [
      'name',
      'color',
      'fontSize',
      'axisLine',
      'splitLine',
      'axisForm',
      'axisLabel',
      'position',
      'showLengthLimit'
    ]
  }
  axis: AxisType[] = [...BAR_AXIS_TYPE]
  protected baseOptions: BarOptions = {
    data: [],
    xField: 'value',
    yField: 'field',
    seriesField: 'category',
    isGroup: true
  }

  async drawChart(drawOptions: G2PlotDrawOptions<Bar>): Promise<Bar> {
    const { chart, container, action } = drawOptions
    if (!chart.data?.data?.length) {
      return
    }
    // data
    const data = cloneDeep(chart.data.data)

    // options
    const initOptions: BarOptions = {
      ...this.baseOptions,
      appendPadding: getPadding(chart),
      data
    }

    const options = this.setupOptions(chart, initOptions)

    const { Bar } = await import('@antv/g2plot/esm/plots/bar')
    // 开始渲染
    const newChart = new Bar(container, options)

    newChart.on('interval:click', action)
    if (options.label) {
      newChart.on('label:click', e => {
        action({
          x: e.x,
          y: e.y,
          data: {
            data: e.target.attrs.data
          }
        })
      })
    }
    configPlotTooltipEvent(chart, newChart)
    configAxisLabelLengthLimit(chart, newChart)
    return newChart
  }

  protected configXAxis(chart: Chart, options: BarOptions): BarOptions {
    const tmpOptions = super.configXAxis(chart, options)
    if (!tmpOptions.xAxis) {
      return tmpOptions
    }
    const xAxis = parseJson(chart.customStyle).xAxis
    const axisValue = xAxis.axisValue
    if (tmpOptions.xAxis.label) {
      tmpOptions.xAxis.label.formatter = value => {
        return valueFormatter(value, xAxis.axisLabelFormatter)
      }
    }
    if (tmpOptions.xAxis.position === 'top') {
      tmpOptions.xAxis.position = 'left'
    }
    if (tmpOptions.xAxis.position === 'bottom') {
      tmpOptions.xAxis.position = 'right'
    }
    if (!axisValue?.auto) {
      const axis = {
        xAxis: {
          ...tmpOptions.xAxis,
          min: axisValue.min,
          max: axisValue.max,
          minLimit: axisValue.min,
          maxLimit: axisValue.max,
          tickCount: axisValue.splitCount
        }
      }
      // 根据axis的最小值，过滤options中的data数据，过滤掉小于最小值的数据
      const { data } = options
      const newData = data.filter(item => item.value >= axisValue.min)
      return { ...tmpOptions, data: newData, ...axis }
    }
    return tmpOptions
  }

  protected configTooltip(chart: Chart, options: BarOptions): BarOptions {
    return super.configMultiSeriesTooltip(chart, options)
  }

  protected configBasicStyle(chart: Chart, options: BarOptions): BarOptions {
    const basicStyle = parseJson(chart.customAttr).basicStyle
    if (basicStyle.gradient) {
      let color = basicStyle.colors
      color = color.map(ele => {
        const tmp = hexColorToRGBA(ele, basicStyle.alpha)
        return setGradientColor(tmp, true)
      })
      options = {
        ...options,
        color
      }
    }
    options = {
      ...options,
      ...configRoundAngle(chart, 'barStyle')
    }

    let barWidthRatio
    const _v = basicStyle.columnWidthRatio ?? DEFAULT_BASIC_STYLE.columnWidthRatio
    if (_v >= 1 && _v <= 100) {
      barWidthRatio = _v / 100.0
    } else if (_v < 1) {
      barWidthRatio = 1 / 100.0
    } else if (_v > 100) {
      barWidthRatio = 1
    }
    if (barWidthRatio) {
      options.barWidthRatio = barWidthRatio
    }

    return options
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    const { customAttr, senior } = chart
    const { label } = customAttr
    if (!['left', 'middle', 'right'].includes(label.position)) {
      label.position = 'middle'
    }
    senior.functionCfg.emptyDataStrategy = 'ignoreData'
    return chart
  }

  protected configLabel(chart: Chart, options: BarOptions): BarOptions {
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
    // 默认灰色
    tmpOptions.label.style.fill = DEFAULT_LABEL.color
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
        const group = new Group({})
        group.addShape({
          type: 'text',
          attrs: {
            x: 0,
            y: 0,
            data,
            text: value,
            textAlign: 'start',
            textBaseline: 'top',
            fontSize: labelCfg.fontSize,
            fontFamily: chart.fontFamily,
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

  protected configYAxis(chart: Chart, options: BarOptions): BarOptions {
    const tmpOptions = super.configYAxis(chart, options)
    if (!tmpOptions.yAxis) {
      return tmpOptions
    }
    if (tmpOptions.yAxis.position === 'left') {
      tmpOptions.yAxis.position = 'bottom'
    }
    if (tmpOptions.yAxis.position === 'right') {
      tmpOptions.yAxis.position = 'top'
    }
    return tmpOptions
  }

  protected setupOptions(chart: Chart, options: BarOptions): BarOptions {
    return flow(
      this.addConditionsStyleColorToData,
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
      this.configAnalyseHorizontal,
      this.configBarConditions
    )(chart, options, {}, this)
  }

  constructor(name = 'bar-horizontal') {
    super(name, DEFAULT_DATA)
  }
}

/**
 * 堆叠条形图
 */
export class HorizontalStackBar extends HorizontalBar {
  properties = BAR_EDITOR_PROPERTY.filter(ele => ele !== 'threshold')
  axisConfig = {
    ...this['axisConfig'],
    extStack: {
      name: `${t('chart.stack_item')} / ${t('chart.dimension')}`,
      type: 'd',
      limit: 1,
      allowEmpty: true
    }
  }
  propertyInner = {
    ...this['propertyInner'],
    'label-selector': ['color', 'fontSize', 'hPosition', 'labelFormatter'],
    'tooltip-selector': ['fontSize', 'color', 'backgroundColor', 'tooltipFormatter', 'show']
  }
  protected configLabel(chart: Chart, options: BarOptions): BarOptions {
    const baseOptions = super.configLabel(chart, options)
    if (!baseOptions.label) {
      return baseOptions
    }
    const { label: labelAttr } = parseJson(chart.customAttr)
    baseOptions.label.style.fill = labelAttr.color
    const label = {
      ...baseOptions.label,
      formatter: function (data: Datum) {
        const value = valueFormatter(data.value, labelAttr.labelFormatter)
        const group = new Group({})
        group.addShape({
          type: 'text',
          attrs: {
            x: 0,
            y: 0,
            data,
            text: value,
            textAlign: 'start',
            textBaseline: 'top',
            fontSize: labelAttr.fontSize,
            fontFamily: chart.fontFamily,
            fill: labelAttr.color
          }
        })
        return group
      }
    }
    return {
      ...baseOptions,
      label
    }
  }

  protected configTooltip(chart: Chart, options: BarOptions): BarOptions {
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
  protected configColor(chart: Chart, options: BarOptions): BarOptions {
    return this.configStackColor(chart, options)
  }
  public setupSeriesColor(chart: ChartObj, data?: any[]): ChartBasicStyle['seriesColor'] {
    return setUpStackSeriesColor(chart, data)
  }

  protected configData(chart: Chart, options: BarOptions): BarOptions {
    const { xAxis, extStack, yAxis } = chart
    const mainSort = xAxis.some(axis => axis.sort !== 'none')
    const subSort = extStack.some(axis => axis.sort !== 'none')
    if (mainSort || subSort) {
      return options
    }
    const quotaSort = yAxis?.[0]?.sort !== 'none'
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

  protected setupOptions(chart: Chart, options: BarOptions): BarOptions {
    return flow(
      this.configTheme,
      this.configEmptyDataStrategy,
      this.configData,
      this.configColor,
      this.configBasicStyle,
      this.configLabel,
      this.configTooltip,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configSlider,
      this.configAnalyseHorizontal
    )(chart, options, {}, this)
  }

  constructor(name = 'bar-stack-horizontal') {
    super(name)
    this.baseOptions = {
      ...this.baseOptions,
      isGroup: false,
      isStack: true,
      meta: {
        category: {
          type: 'cat'
        }
      }
    }
    this.axis = [...this.axis, 'extStack']
  }
}

/**
 * 百分比堆叠条形图
 */
export class HorizontalPercentageStackBar extends HorizontalStackBar {
  propertyInner = {
    ...this['propertyInner'],
    'label-selector': ['color', 'fontSize', 'hPosition', 'reserveDecimalCount'],
    'tooltip-selector': ['color', 'fontSize', 'backgroundColor', 'show']
  }
  protected configLabel(chart: Chart, options: BarOptions): BarOptions {
    const baseOptions = super.configLabel(chart, options)
    if (!baseOptions.label) {
      return baseOptions
    }
    const { customAttr } = chart
    const l = parseJson(customAttr).label
    const label = {
      ...baseOptions.label,
      formatter: function (data: Datum) {
        let value = data.value
        if (value) {
          value = (Math.round(value * 10000) / 100).toFixed(l.reserveDecimalCount) + '%'
        } else {
          value = '0%'
        }
        const group = new Group({})
        group.addShape({
          type: 'text',
          attrs: {
            x: 0,
            y: 0,
            data,
            text: value,
            textAlign: 'start',
            textBaseline: 'top',
            fontSize: l.fontSize,
            fontFamily: chart.fontFamily,
            fill: l.color
          }
        })
        return group
      }
    }
    return {
      ...baseOptions,
      label
    }
  }

  protected configTooltip(chart: Chart, options: BarOptions): BarOptions {
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
  protected setupOptions(chart: Chart, options: BarOptions): BarOptions {
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
      this.configAnalyseHorizontal
    )(chart, options, {}, this)
  }

  constructor() {
    super('percentage-bar-stack-horizontal')
    this.baseOptions = {
      ...this.baseOptions,
      isPercent: true
    }
  }
}
