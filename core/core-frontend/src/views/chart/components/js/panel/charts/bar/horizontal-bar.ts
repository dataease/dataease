import {
  G2PlotChartView,
  G2PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/g2plot'
import { Bar, BarOptions } from '@antv/g2plot/esm/plots/bar'
import { getPadding, setGradientColor } from '@/views/chart/components/js/panel/common/common_antv'
import { cloneDeep } from 'lodash-es'
import {
  flow,
  handleEmptyDataStrategy,
  hexColorToRGBA,
  parseJson
} from '@/views/chart/components/js/util'
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
import { Datum } from '@antv/g2plot/esm/types/common'

const DEFAULT_DATA = []

/**
 * 条形图
 */
export class HorizontalBar extends G2PlotChartView<BarOptions, Bar> {
  properties = BAR_EDITOR_PROPERTY
  propertyInner = {
    ...BAR_EDITOR_PROPERTY_INNER,
    'x-axis-selector': [...BAR_EDITOR_PROPERTY_INNER['y-axis-selector'], 'hPosition'],
    'y-axis-selector': [...BAR_EDITOR_PROPERTY_INNER['x-axis-selector'], 'hPosition'],
    'label-selector': [...BAR_EDITOR_PROPERTY_INNER['label-selector'], 'hPosition']
  }
  axis: AxisType[] = [...BAR_AXIS_TYPE]
  protected baseOptions: BarOptions = {
    data: [],
    xField: 'value',
    yField: 'field',
    seriesField: 'category',
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

  drawChart(drawOptions: G2PlotDrawOptions<Bar>): Bar {
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

    // 开始渲染
    const newChart = new Bar(container, options)

    newChart.on('interval:click', action)

    return newChart
  }

  protected configTooltip(chart: Chart, options: BarOptions): BarOptions {
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
      return { ...tmpOptions, ...axis }
    }
    return tmpOptions
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
    return options
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    const { customStyle, customAttr } = chart
    const { xAxis, yAxis } = customStyle
    const { label } = customAttr
    if (!['left', 'right'].includes(xAxis.position)) {
      xAxis.position = 'right'
    }
    if (!['top', 'bottom'].includes(yAxis.position)) {
      yAxis.position = 'bottom'
    }
    if (!['left', 'middle', 'right'].includes(label.position)) {
      label.position = 'middle'
    }
    return chart
  }

  protected setupOptions(chart: Chart, options: BarOptions): BarOptions {
    return flow(
      this.configTheme,
      this.configBasicStyle,
      this.configLabel,
      this.configTooltip,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configSlider,
      this.configAnalyseHorizontal,
      this.configEmptyDataStrategy
    )(chart, options)
  }

  constructor(name = 'bar-horizontal') {
    super(name, DEFAULT_DATA)
  }
}

/**
 * 堆叠条形图
 */
export class HorizontalStackBar extends HorizontalBar {
  protected configLabel(chart: Chart, options: BarOptions): BarOptions {
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

  protected configTooltip(chart: Chart, options: BarOptions): BarOptions {
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

  constructor(name = 'bar-stack-horizontal') {
    super(name)
    this.baseOptions = {
      ...this.baseOptions,
      isStack: true
    }
    this.axis = [...this.axis, 'extStack']
  }
}

/**
 * 百分比堆叠条形图
 */
export class HorizontalPercentageStackBar extends HorizontalStackBar {
  protected configLabel(chart: Chart, options: BarOptions): BarOptions {
    const baseOptions = super.configLabel(chart, options)
    if (!baseOptions.label) {
      return baseOptions
    }
    const { extStack, xAxisExt, yAxis, customAttr } = chart
    const l = parseJson(customAttr).label
    const label = {
      ...baseOptions.label,
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
    super('percentage-bar-stack-horizontal')
    this.baseOptions = {
      ...this.baseOptions,
      isPercent: true
    }
  }
}
