import {
  G2PlotChartView,
  G2PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/g2plot'
import type { Liquid as G2Liquid, LiquidOptions } from '@antv/g2plot/esm/plots/liquid'
import { flow, hexColorToRGBA, parseJson } from '@/views/chart/components/js/util'
import { DEFAULT_MISC } from '@/views/chart/components/editor/util/chart'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { useI18n } from '@/hooks/web/useI18n'

const { t } = useI18n()
const DEFAULT_LIQUID_DATA = []
/**
 * 水波图
 */
export class Liquid extends G2PlotChartView<LiquidOptions, G2Liquid> {
  properties: EditorProperty[] = [
    'background-overall-component',
    'border-style',
    'basic-style-selector',
    'label-selector',
    'misc-selector',
    'title-selector',
    'threshold'
  ]
  propertyInner: EditorPropertyInner = {
    'background-overall-component': ['all'],
    'border-style': ['all'],
    'basic-style-selector': ['colors', 'alpha'],
    'label-selector': ['fontSize', 'color', 'labelFormatter'],
    'misc-selector': ['liquidShape', 'liquidSize', 'liquidMaxType', 'liquidMaxField'],
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
    threshold: ['liquidThreshold']
  }
  axis: AxisType[] = ['yAxis', 'filter']
  axisConfig: AxisConfig = {
    yAxis: {
      name: `${t('chart.drag_block_progress')} / ${t('chart.quota')}`,
      type: 'q',
      limit: 1
    }
  }

  async drawChart(drawOptions: G2PlotDrawOptions<G2Liquid>): Promise<G2Liquid> {
    const { chart, container } = drawOptions
    if (!chart.data?.series) {
      return
    }
    const initOptions: LiquidOptions = {
      percent: 0
    }
    const options = this.setupOptions(chart, initOptions)
    const { Liquid: G2Liquid } = await import('@antv/g2plot/esm/plots/liquid')
    // 开始渲染
    return new G2Liquid(container, options)
  }

  protected configTheme(chart: Chart, options: LiquidOptions): LiquidOptions {
    const customAttr = parseJson(chart.customAttr)
    const colors: string[] = []
    if (customAttr.basicStyle) {
      const basicStyle = customAttr.basicStyle
      basicStyle.colors.forEach(ele => {
        colors.push(hexColorToRGBA(ele, basicStyle.alpha))
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

  protected configMisc(chart: Chart, options: LiquidOptions): LiquidOptions {
    const customAttr = parseJson(chart.customAttr)
    let value = 0
    if (chart.data.series.length > 0) {
      value = chart.data.series[0].data[0]
    }
    let max, radius, shape
    if (customAttr.misc) {
      const misc = customAttr.misc
      if (misc.liquidMaxType === 'dynamic') {
        max = chart.data?.series[chart.data?.series.length - 1]?.data[0]
      } else {
        max = misc.liquidMax ? misc.liquidMax : DEFAULT_MISC.liquidMax
      }
      radius = (misc.liquidSize ? misc.liquidSize : DEFAULT_MISC.liquidSize) / 100
      shape = misc.liquidShape ?? DEFAULT_MISC.liquidShape
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
    const data = chart.data.series[0].data[0]
    const originVal = options.percent
    // 数值过大图表会异常，大于 1 无意义
    if (originVal > 1) {
      options = {
        ...options,
        percent: 1
      }
    }
    if (!customAttr.label?.show) {
      return {
        ...options,
        statistic: {
          content: false
        }
      }
    }
    const label = customAttr.label
    const labelFormatter = label.labelFormatter
    return {
      ...options,
      statistic: {
        content: {
          style: {
            fontSize: label.fontSize.toString() + 'px',
            color: label.color
          },
          formatter: () => {
            if (labelFormatter.type === 'value' || labelFormatter.type === 'auto') {
              return valueFormatter(data, labelFormatter);
            }

            return valueFormatter(options.percent, labelFormatter);
          }
        }
      }
    }
  }

  protected configThreshold(chart: Chart, options: LiquidOptions): LiquidOptions {
    const senior = parseJson(chart.senior)
    if (senior?.threshold?.enable) {
      const { liquidThreshold } = senior?.threshold
      if (liquidThreshold) {
        const { paletteQualitative10: colors } = (options.theme as any).styleSheet
        const liquidStyle = () => {
          const thresholdArr = liquidThreshold.split(',')
          let index = 0
          thresholdArr.forEach((v, i) => {
            if (options.percent > parseFloat(v) / 100) {
              index = i + 1
            }
          })
          return {
            fill: colors[index % colors.length],
            stroke: colors[index % colors.length]
          }
        }
        return { ...options, liquidStyle }
      }
    }
    return options
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    chart.customAttr.label = {
      ...chart.customAttr.label,
      fontSize: 12,
      show: true,
      labelFormatter: {
        type: 'percent',
        thousandSeparator: true,
        decimalCount: 2
      }
    }
    return chart
  }

  protected setupOptions(chart: Chart, options: LiquidOptions): LiquidOptions {
    return flow(
      this.configTheme,
      this.configMisc,
      this.configLabel,
      this.configThreshold
    )(chart, options)
  }
  constructor() {
    super('liquid', DEFAULT_LIQUID_DATA)
  }
}
