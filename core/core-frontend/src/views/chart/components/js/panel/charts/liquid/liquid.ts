import {
  G2PlotChartView,
  G2PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/g2plot'
import { Liquid as G2Liquid, LiquidOptions } from '@antv/g2plot'
import { flow, hexColorToRGBA, parseJson } from '@/views/chart/components/js/util'
import { DEFAULT_LABEL, DEFAULT_SIZE } from '@/views/chart/components/editor/util/chart'
import { valueFormatter } from '@/views/chart/components/editor/util/formatter'

const DEFAULT_LIQUID_DATA = []
export class Liquid extends G2PlotChartView<LiquidOptions, G2Liquid> {
  drawChart(drawOptions: G2PlotDrawOptions<G2Liquid>): G2Liquid {
    const chart = drawOptions.chart
    if (chart?.data) {
      const initOptions: LiquidOptions = {
        percent: 0
      }
      const options = this.setupOptions(chart, initOptions)
      // 开始渲染
      if (drawOptions.chartObj) {
        drawOptions.chartObj.destroy()
      }
      drawOptions.chartObj = new G2Liquid(drawOptions.container, options)
      return drawOptions.chartObj
    }
  }

  protected configTheme(chart: Chart, options: LiquidOptions): LiquidOptions {
    const customAttr = parseJson(chart.customAttr)
    const colors: string[] = []
    if (customAttr.color) {
      customAttr.color.colors.forEach(ele => {
        colors.push(hexColorToRGBA(ele, customAttr.color.alpha))
      })
    }
    const customStyle = parseJson(chart.customStyle)
    let bgColor
    if (customStyle.background) {
      bgColor = hexColorToRGBA(customStyle.background.color, customStyle.background.alpha)
    }
    const theme = {
      styleSheet: {
        brandColor: colors[0],
        paletteQualitative10: colors,
        paletteQualitative20: colors,
        backgroundColor: bgColor
      }
    }
    return { ...options, theme }
  }

  protected configSize(chart: Chart, options: LiquidOptions): LiquidOptions {
    const customAttr = parseJson(chart.customAttr)
    let value = 0
    if (chart.data.series.length > 0) {
      value = chart.data.series[0].data[0]
    }
    let max, radius, shape
    if (customAttr.size) {
      const size = customAttr.size
      if (size.liquidMaxType === 'dynamic') {
        max = chart.data?.series[chart.data?.series.length - 1]?.data[0]
      } else {
        max = size.liquidMax ? size.liquidMax : DEFAULT_SIZE.liquidMax
      }
      radius = (size.liquidSize ? size.liquidSize : DEFAULT_SIZE.liquidSize) / 100
      shape = size.liquidShape ?? DEFAULT_SIZE.liquidShape
    }
    const size: LiquidOptions = {
      percent: value / max,
      radius: radius,
      shape: shape
    }
    return { ...options, ...size }
  }

  protected configLabel(chart: Chart, options: LiquidOptions): LiquidOptions {
    const customAttr = parseJson(chart.customAttr)
    let labelContent
    if (customAttr.label) {
      const label = customAttr.label
      const labelFormatter = label.gaugeLabelFormatter ?? DEFAULT_LABEL.gaugeLabelFormatter
      if (label.show) {
        labelContent = {
          style: () => ({
            fontSize: label.fontSize,
            color: label.color
          }),
          formatter: function (v) {
            const value = v.percent
            return valueFormatter(value, labelFormatter)
          }
        }
      } else {
        labelContent = false
      }
    }
    const statistic = {
      content: labelContent
    }
    return { ...options, statistic }
  }

  protected setupOptions(chart: Chart, options: LiquidOptions): LiquidOptions {
    return flow(this.configTheme, this.configSize, this.configLabel)(chart, options)
  }
  constructor() {
    super('liquid', DEFAULT_LIQUID_DATA)
  }
}
