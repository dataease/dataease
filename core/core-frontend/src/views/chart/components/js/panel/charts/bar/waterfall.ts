import { WaterfallOptions, Waterfall as G2Waterfall } from '@antv/g2plot/esm/plots/waterfall'
import { G2PlotChartView, G2PlotDrawOptions } from '../../types/impl/g2plot'
import { flow, hexColorToRGBA, parseJson } from '../../../util'
import { valueFormatter } from '../../../formatter'
import { getPadding, setGradientColor } from '../../common/common_antv'
import { flow as flowLeft } from 'lodash-es'

/**
 * 瀑布图
 */
export class Waterfall extends G2PlotChartView<WaterfallOptions, G2Waterfall> {
  properties: EditorProperty[] = [
    'background-overall-component',
    'basic-style-selector',
    'label-selector',
    'tooltip-selector',
    'title-selector',
    'legend-selector',
    'x-axis-selector',
    'y-axis-selector'
  ]
  propertyInner: EditorPropertyInner = {
    'background-overall-component': ['all'],
    'basic-style-selector': ['colors', 'alpha', 'gradient'],
    'label-selector': ['fontSize', 'color', 'vPosition', 'labelFormatter'],
    'tooltip-selector': ['fontSize', 'color', 'backgroundColor', 'seriesTooltipFormatter'],
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
    'legend-selector': ['icon', 'orient', 'fontSize', 'color', 'hPosition', 'vPosition'],
    'x-axis-selector': [
      'vPosition',
      'name',
      'color',
      'fontSize',
      'axisLine',
      'splitLine',
      'axisForm',
      'axisLabel'
    ],
    'y-axis-selector': [
      'vPosition',
      'name',
      'color',
      'fontSize',
      'axisValue',
      'splitLine',
      'axisForm',
      'axisLabel',
      'axisLabelFormatter'
    ]
  }
  axis: AxisType[] = ['xAxis', 'yAxis', 'filter', 'drill', 'extLabel', 'extTooltip']
  public drawChart(drawOptions: G2PlotDrawOptions<G2Waterfall>): G2Waterfall {
    const { chart, container, action } = drawOptions
    if (!chart.data?.data) {
      return
    }
    const data = chart.data.data
    const baseOptions = {
      data,
      xField: 'field',
      yField: 'value',
      seriesField: 'category',
      appendPadding: getPadding(chart),
      interactions: [
        {
          type: 'tooltip',
          cfg: {
            start: [{ trigger: 'interval:mousemove', action: 'tooltip:show' }],
            end: [{ trigger: 'interval:mouseleave', action: 'tooltip:hide' }]
          }
        }
      ]
    }
    const options = this.setupOptions(chart, baseOptions)
    const newChart = new G2Waterfall(container, options)
    newChart.on('interval:click', action)
    return newChart
  }

  protected configMeta(chart: Chart, options: WaterfallOptions): WaterfallOptions {
    const yAxis = chart.yAxis
    const meta: WaterfallOptions['meta'] = {
      field: {
        type: 'cat'
      }
    }
    if (!yAxis?.length) {
      return {
        ...options,
        meta
      }
    }
    const f = yAxis[0]
    const yAxisStyle = parseJson(chart.customStyle).yAxis
    meta.value = {
      alias: f.name,
      formatter: (value: number) => {
        return valueFormatter(value, yAxisStyle.axisLabelFormatter)
      }
    }
    return {
      ...options,
      meta
    }
  }

  protected configBasicStyle(chart: Chart, options: WaterfallOptions): WaterfallOptions {
    const customAttr = parseJson(chart.customAttr)
    const { colors, gradient, alpha } = customAttr.basicStyle
    const [risingColorRgba, fallingColorRgba, totalColorRgba] = colors
    return {
      ...options,
      total: {
        label: '合计',
        style: {
          fill: setGradientColor(hexColorToRGBA(totalColorRgba, alpha), gradient, 270)
        }
      },
      legend: {
        items: [
          {
            name: '增加',
            value: '',
            marker: {
              style: {
                fill: setGradientColor(hexColorToRGBA(risingColorRgba, alpha), gradient, 270)
              }
            }
          },
          {
            name: '减少',
            value: '',
            marker: {
              style: {
                fill: setGradientColor(hexColorToRGBA(fallingColorRgba, alpha), gradient, 270)
              }
            }
          },
          {
            name: '合计',
            value: '',
            marker: {
              style: {
                fill: setGradientColor(hexColorToRGBA(totalColorRgba, alpha), gradient, 270)
              }
            }
          }
        ]
      },
      risingFill: setGradientColor(hexColorToRGBA(risingColorRgba, alpha), gradient, 270),
      fallingFill: setGradientColor(hexColorToRGBA(fallingColorRgba, alpha), gradient, 270)
    }
  }

  protected configYAxis(chart: Chart, options: WaterfallOptions): WaterfallOptions {
    const tmpOptions = super.configYAxis(chart, options)
    if (!tmpOptions.yAxis) {
      return tmpOptions
    }
    const yAxis = parseJson(chart.customStyle).yAxis
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

  protected setupOptions(chart: Chart, options: WaterfallOptions): WaterfallOptions {
    return flow(
      this.configTheme,
      this.configBasicStyle,
      this.configLabel,
      this.configMultiSeriesTooltip,
      this.configXAxis,
      this.configYAxis,
      this.configMeta
    )(chart, options)
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    return flowLeft(this.setupVerticalAxis, this.setupVerticalAxis)(chart)
  }

  constructor() {
    super('waterfall', [])
  }
}
