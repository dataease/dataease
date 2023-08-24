import { Column, ColumnOptions } from '@antv/g2plot/esm/plots/column'
import { cloneDeep } from 'lodash-es'
import {
  G2PlotChartView,
  G2PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/g2plot'
import { flow, hexColorToRGBA, parseJson } from '@/views/chart/components/js/util'
import { Datum } from '@antv/g2plot'
import {
  formatterItem,
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
    'label-selector': [...BAR_EDITOR_PROPERTY_INNER['label-selector'], 'vPosition']
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
      this.configAnalyse
    )(chart, options)
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    return flowLeft(this.setupVerticalAxis, this.setupVerticalAxis)(chart)
  }

  constructor(name = 'bar', defaultData = DEFAULT_DATA) {
    super(name, defaultData)
  }
}

/**
 * 堆叠柱状图
 */
export class StackBar extends Bar {
  protected configLabel(chart: Chart, options: ColumnOptions): ColumnOptions {
    const baseOptions = super.configLabel(chart, options)
    if (!baseOptions.label) {
      return baseOptions
    }
    const { extStack, xAxisExt, yAxis } = chart
    const label = {
      ...baseOptions.label,
      formatter: function (param: Datum) {
        const res = param.value
        let f
        if (extStack?.length > 0 || xAxisExt?.length > 0) {
          f = yAxis[0]
        } else {
          for (let i = 0; i < yAxis.length; i++) {
            if (yAxis[i].name === param.category) {
              f = yAxis[i]
              break
            }
          }
        }
        if (!f) {
          return res
        }
        if (!f.formatterCfg) {
          f.formatterCfg = formatterItem
        }
        return valueFormatter(param.value, f.formatterCfg)
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
    const { extStack, yAxis } = chart
    const tooltip = {
      formatter: (param: Datum) => {
        let res = param.value
        let f
        const obj = { name: param.category, value: param.value }
        if (extStack?.length > 0) {
          f = yAxis[0]
        } else {
          for (let i = 0; i < yAxis.length; i++) {
            if (yAxis[i].name === param.category) {
              f = yAxis[i]
              break
            }
          }
        }
        if (!f) {
          return res
        }
        if (!f.formatterCfg) {
          f.formatterCfg = formatterItem
        }
        res = valueFormatter(param.value, f.formatterCfg)
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
export class GroupBar extends Bar {
  protected configLabel(chart: Chart, options: ColumnOptions): ColumnOptions {
    const baseOptions = super.configLabel(chart, options)
    if (baseOptions.label) {
      const yAxis = chart.yAxis
      const label = {
        ...options.label,
        formatter: function (param: Datum) {
          const f = yAxis[0]
          let res = param.value
          if (f.formatterCfg) {
            res = valueFormatter(param.value, f.formatterCfg)
          } else {
            res = valueFormatter(param.value, formatterItem)
          }
          return res
        }
      }
      return {
        ...baseOptions,
        label
      }
    }
    return options
  }

  protected configTooltip(chart: Chart, options: ColumnOptions): ColumnOptions {
    const tooltipAttr = parseJson(chart.customAttr).tooltip
    if (!tooltipAttr.show) {
      return {
        ...options,
        tooltip: false
      }
    }
    const yAxis = chart.yAxis
    const tooltip = {
      formatter: (param: Datum) => {
        let res = param.value
        const obj = { name: param.category, value: param.value }
        for (let i = 0; i < yAxis.length; i++) {
          const f = yAxis[i]
          if (f.formatterCfg) {
            res = valueFormatter(param.value, f.formatterCfg)
          } else {
            res = valueFormatter(param.value, formatterItem)
          }
        }
        obj.value = res ?? ''
        return obj
      }
    }
    return {
      ...options,
      tooltip
    }
  }

  constructor(name = 'bar-group') {
    super(name)
    this.baseOptions = {
      ...this.baseOptions
    }
    this.axis = [...this.axis, 'xAxisExt']
  }
}

/**
 * 分组堆叠柱状图
 */
export class GroupStackBar extends Bar {
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
    if (baseOptions.label) {
      const yAxis = chart.yAxis
      const label = {
        ...options.label,
        formatter: function (param: Datum) {
          const f = yAxis[0]
          let res = param.value
          if (f.formatterCfg) {
            res = valueFormatter(param.value, f.formatterCfg)
          } else {
            res = valueFormatter(param.value, formatterItem)
          }
          return res
        }
      }
      return {
        ...baseOptions,
        label
      }
    }
    return options
  }

  protected configTooltip(chart: Chart, options: ColumnOptions): ColumnOptions {
    const tooltipAttr = parseJson(chart.customAttr).tooltip
    if (!tooltipAttr.show) {
      return {
        ...options,
        tooltip: false
      }
    }
    const yAxis = chart.yAxis
    const tooltip = {
      formatter: (param: Datum) => {
        let res = param.value
        const obj = { name: param.category, value: param.value }
        for (let i = 0; i < yAxis.length; i++) {
          const f = yAxis[i]
          if (f.formatterCfg) {
            res = valueFormatter(param.value, f.formatterCfg)
          } else {
            res = valueFormatter(param.value, formatterItem)
          }
        }
        obj.value = res ?? ''
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
      isStack: true,
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
  protected configLabel(chart: Chart, options: ColumnOptions): ColumnOptions {
    const baseOptions = super.configLabel(chart, options)
    if (!baseOptions.label) {
      return baseOptions
    }
    const { extStack, xAxisExt, yAxis, customAttr } = chart
    const l = parseJson(customAttr).label
    const label = {
      ...options.label,
      formatter: function (param: Datum) {
        let f
        const res = param.value
        if (extStack?.length > 0 || xAxisExt?.length > 0) {
          f = yAxis[0]
        } else {
          for (let i = 0; i < yAxis.length; i++) {
            if (yAxis[i].name === param.category) {
              f = yAxis[i]
              break
            }
          }
        }
        if (!f) {
          return res
        }
        if (!f.formatterCfg) {
          f.formatterCfg = formatterItem
        }
        if (!param.value) {
          return
        }
        f.formatterCfg.type = 'percent'
        f.formatterCfg.decimalCount = l.reserveDecimalCount
        f.formatterCfg.thousandSeparator = false
        return valueFormatter(param.value, f.formatterCfg)
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
    const { extStack, yAxis, customAttr } = chart
    const l = parseJson(customAttr).label
    const tooltip = {
      formatter: (param: Datum) => {
        let f
        let res = param.value
        const obj = { name: param.category, value: param.value }
        if (extStack?.length > 0) {
          f = yAxis[0]
        } else {
          for (let i = 0; i < yAxis.length; i++) {
            if (yAxis[i].name === param.category) {
              f = yAxis[i]
              break
            }
          }
        }
        if (!f) {
          return res
        }
        if (!f.formatterCfg) {
          f.formatterCfg = formatterItem
        }
        if (!param.value) {
          obj.value = 0
          return obj
        }
        // 保留小数位数和标签保持一致，这边拿一下标签的配置
        f.formatterCfg.type = 'percent'
        f.formatterCfg.decimalCount = l.reserveDecimalCount
        f.formatterCfg.thousandSeparator = false
        res = valueFormatter(param.value, f.formatterCfg)
        obj.value = res ?? ''
        return obj
      }
    }
    return {
      ...options,
      tooltip
    }
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
    this.axis = [...BAR_AXIS_TYPE, 'extStack']
  }
}
