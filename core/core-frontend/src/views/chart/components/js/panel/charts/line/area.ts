import {
  G2PlotChartView,
  G2PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/g2plot'
import { Area as G2Area, AreaOptions } from '@antv/g2plot/esm/plots/area'
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
  LINE_AXIS_TYPE,
  LINE_EDITOR_PROPERTY,
  LINE_EDITOR_PROPERTY_INNER
} from '@/views/chart/components/js/panel/charts/line/common'
import { IntervalGeometryLabelPosition } from '@antv/g2/lib/interface'
import { Label } from '@antv/g2plot/lib/types/label'
import { Datum } from '@antv/g2plot/esm/types/common'

const DEFAULT_DATA = []
export class Area extends G2PlotChartView<AreaOptions, G2Area> {
  properties = LINE_EDITOR_PROPERTY
  propertyInner = {
    ...LINE_EDITOR_PROPERTY_INNER,
    'basic-style-selector': [...LINE_EDITOR_PROPERTY_INNER['basic-style-selector'], 'gradient']
  }
  axis: AxisType[] = [...LINE_AXIS_TYPE]
  baseOptions: AreaOptions = {
    data: [],
    xField: 'field',
    yField: 'value',
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
          start: [{ trigger: 'point:mousemove', action: 'tooltip:show' }],
          end: [{ trigger: 'point:mouseleave', action: 'tooltip:hide' }]
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

  drawChart(drawOptions: G2PlotDrawOptions<G2Area>): G2Area {
    const { chart, container, action } = drawOptions
    if (chart?.data) {
      // data
      const data = cloneDeep(chart.data.data)
      const initOptions: AreaOptions = {
        ...this.baseOptions,
        data,
        appendPadding: getPadding(chart)
      }
      // options
      const options = this.setupOptions(chart, initOptions)
      // 处理空值
      if (chart.senior) {
        let emptyDataStrategy = parseJson(chart.senior)?.functionCfg?.emptyDataStrategy
        if (!emptyDataStrategy) {
          emptyDataStrategy = 'breakLine'
        }
        handleEmptyDataStrategy(emptyDataStrategy, chart, data, options)
      }

      // 开始渲染
      const newChart = new G2Area(container, options)

      newChart.on('point:click', action)

      return newChart
    }
  }

  protected configLabel(chart: Chart, options: AreaOptions): AreaOptions {
    const baseptions = super.configLabel(chart, options)
    if (!baseptions.label) {
      return baseptions
    }
    const label = {
      ...options.label,
      offsetY: -8,
      autoRotate: undefined
    }
    return { ...baseptions, label }
  }

  protected configTooltip(chart: Chart, options: AreaOptions): AreaOptions {
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

  protected configBasicStyle(chart: Chart, options: AreaOptions): AreaOptions {
    // size
    const customAttr: DeepPartial<ChartAttr> = parseJson(chart.customAttr)
    const s: DeepPartial<ChartBasicStyle> = JSON.parse(JSON.stringify(customAttr.basicStyle))
    const smooth = s.lineSmooth
    const point = {
      size: s.lineSymbolSize,
      shape: s.lineSymbol
    }
    const line = {
      style: {
        lineWidth: s.lineWidth
      }
    }
    // custom color
    const { colors, alpha } = customAttr.basicStyle
    const areaColors = [...colors, ...colors]
    let areaStyle
    if (customAttr.basicStyle.gradient) {
      areaStyle = () => {
        const ele = areaColors.shift()
        if (ele) {
          return {
            fill: setGradientColor(hexColorToRGBA(ele, alpha), true, 270)
          }
        }
      }
    }
    return {
      ...options,
      smooth,
      line,
      point,
      areaStyle
    }
  }

  protected configYAxis(chart: Chart, options: AreaOptions): AreaOptions {
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

  protected setupOptions(chart: Chart, options: AreaOptions): AreaOptions {
    return flow(
      this.configTheme,
      this.configLabel,
      this.configTooltip,
      this.configBasicStyle,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configSlider,
      this.configAnalyse
    )(chart, options)
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    return this.setupVerticalAxis(chart)
  }

  constructor(name = 'area') {
    super(name, DEFAULT_DATA)
  }
}

/**
 * 堆叠面积图
 */
export class StackArea extends Area {
  protected configLabel(chart: Chart, options: AreaOptions): AreaOptions {
    const customAttr = parseJson(chart.customAttr)
    const labelAttr = customAttr.label
    if (!labelAttr?.show) {
      return {
        ...options,
        label: false
      }
    }
    const { xAxisExt, yAxis } = chart
    const label: Label = {
      position: labelAttr.position as IntervalGeometryLabelPosition,
      offsetY: -8,
      style: {
        fill: labelAttr.color,
        fontSize: labelAttr.fontSize
      },
      formatter: function (param: Datum) {
        let res = param.value
        let f
        if (xAxisExt?.length > 0) {
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
        return res
      }
    }
    return { ...options, label }
  }

  protected configTooltip(chart: Chart, options: AreaOptions): AreaOptions {
    const customAttr: DeepPartial<ChartAttr> = parseJson(chart.customAttr)
    const tooltipAttr = customAttr.tooltip
    if (!tooltipAttr.show) {
      return {
        ...options,
        tooltip: false
      }
    }
    const tooltip = {
      formatter: function (param: Datum) {
        let res
        const obj = { name: param.category, value: param.value }
        const xAxisExt = chart.xAxisExt
        const yAxis = chart.yAxis
        for (let i = 0; i < yAxis.length; i++) {
          const f = yAxis[i]
          if (f.name === param.category || (yAxis.length && xAxisExt.length)) {
            if (f.formatterCfg) {
              res = valueFormatter(param.value, f.formatterCfg)
            } else {
              res = valueFormatter(param.value, formatterItem)
            }
            break
          }
        }
        obj.value = res ?? ''
        return obj
      }
    }
    return { ...options, tooltip }
  }

  constructor() {
    super('area-stack')
    this.baseOptions = {
      ...this.baseOptions,
      isStack: true
    }
    this.axis.push('extStack')
  }
}
