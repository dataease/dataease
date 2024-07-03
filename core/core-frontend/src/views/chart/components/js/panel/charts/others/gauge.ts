import {
  G2PlotChartView,
  G2PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/g2plot'
import { Gauge as G2Gauge, GaugeOptions } from '@antv/g2plot/esm/plots/gauge'
import { flow, parseJson } from '@/views/chart/components/js/util'
import {
  DEFAULT_LABEL,
  DEFAULT_MISC,
  DEFAULT_THRESHOLD,
  getScaleValue
} from '@/views/chart/components/editor/util/chart'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { getPadding, setGradientColor } from '@/views/chart/components/js/panel/common/common_antv'
import { useI18n } from '@/hooks/web/useI18n'
import { merge } from 'lodash-es'

const { t } = useI18n()

const DEFAULT_DATA = []
export class Gauge extends G2PlotChartView<GaugeOptions, G2Gauge> {
  properties: EditorProperty[] = [
    'background-overall-component',
    'basic-style-selector',
    'label-selector',
    'misc-selector',
    'title-selector',
    'threshold'
  ]
  propertyInner: EditorPropertyInner = {
    'background-overall-component': ['all'],
    'basic-style-selector': ['colors', 'alpha', 'gradient', 'gaugeAxisLine', 'gaugePercentLabel'],
    'label-selector': ['fontSize', 'color', 'labelFormatter'],
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
    'misc-selector': [
      'gaugeMinType',
      'gaugeMinField',
      'gaugeMin',
      'gaugeMaxType',
      'gaugeMaxField',
      'gaugeMax',
      'gaugeStartAngle',
      'gaugeEndAngle'
    ],
    threshold: ['gaugeThreshold']
  }
  axis: AxisType[] = ['yAxis', 'filter']
  axisConfig: AxisConfig = {
    yAxis: {
      name: `${t('chart.drag_block_gauge_angel')} / ${t('chart.quota')}`,
      type: 'q',
      limit: 1
    }
  }

  drawChart(drawOptions: G2PlotDrawOptions<G2Gauge>): G2Gauge {
    const { chart, container, scale } = drawOptions
    if (!chart.data?.series) {
      return
    }
    // options
    const initOptions: GaugeOptions = {
      percent: 0,
      appendPadding: getPadding(chart),
      axis: {
        tickInterval: 0.2,
        label: {
          style: {
            fontSize: getScaleValue(12, scale) // 刻度值字体大小
          }
        },
        tickLine: {
          length: getScaleValue(12, scale) * -1, // 刻度线长度
          style: {
            lineWidth: getScaleValue(1, scale) // 刻度线宽度
          }
        },
        subTickLine: {
          count: 4, // 子刻度数
          length: getScaleValue(6, scale) * -1, // 子刻度线长度
          style: {
            lineWidth: getScaleValue(1, scale) // 子刻度线宽度
          }
        }
      }
    }
    const options = this.setupOptions(chart, initOptions, { scale })
    return new G2Gauge(container, options)
  }

  protected configMisc(
    chart: Chart,
    options: GaugeOptions,
    context: Record<string, any>
  ): GaugeOptions {
    const customAttr = parseJson(chart.customAttr)
    const data = chart.data.series[0].data[0]
    let min, max, startAngle, endAngle
    if (customAttr.misc) {
      const misc = customAttr.misc
      if (misc.gaugeMinType === 'dynamic' && misc.gaugeMaxType === 'dynamic') {
        min = chart.data?.series[chart.data?.series.length - 2]?.data[0]
        max = chart.data?.series[chart.data?.series.length - 1]?.data[0]
      } else if (misc.gaugeMinType !== 'dynamic' && misc.gaugeMaxType === 'dynamic') {
        min = misc.gaugeMin ? misc.gaugeMin : DEFAULT_MISC.gaugeMin
        max = chart.data?.series[chart.data?.series.length - 1]?.data[0]
      } else if (misc.gaugeMinType === 'dynamic' && misc.gaugeMaxType !== 'dynamic') {
        min = chart.data?.series[chart.data?.series.length - 1]?.data[0]
        max = misc.gaugeMax ? misc.gaugeMax : DEFAULT_MISC.gaugeMax
      } else {
        min = misc.gaugeMin ? misc.gaugeMin : DEFAULT_MISC.gaugeMin
        max = misc.gaugeMax ? misc.gaugeMax : DEFAULT_MISC.gaugeMax
      }
      startAngle = (misc.gaugeStartAngle * Math.PI) / 180
      endAngle = (misc.gaugeEndAngle * Math.PI) / 180
      context.min = min
      context.max = max
    }
    const percent = (parseFloat(data) - parseFloat(min)) / (parseFloat(max) - parseFloat(min))
    const tmp = {
      percent,
      startAngle,
      endAngle
    }
    return { ...options, ...tmp }
  }

  private configRange(
    chart: Chart,
    options: GaugeOptions,
    context: Record<string, any>
  ): GaugeOptions {
    const { scale } = context
    const range = [0]
    let index = 0
    let flag = false
    let hasThreshold = false
    const theme = options.theme as any

    if (chart.senior) {
      const senior = parseJson(chart.senior)
      const threshold = senior.threshold ?? DEFAULT_THRESHOLD
      if (threshold.enable && threshold.gaugeThreshold) {
        hasThreshold = true
        const arr = threshold.gaugeThreshold.split(',')
        for (let i = 0; i < arr.length; i++) {
          const ele = arr[i]
          const p = parseFloat(ele) / 100
          range.push(p)
          if (!flag && options.percent <= p) {
            flag = true
            index = i
          }
        }
        if (!flag) {
          index = arr.length
        }
      }
    }
    range.push(1)
    let rangOptions
    if (hasThreshold) {
      rangOptions = {
        range: {
          color: theme.styleSheet.paletteQualitative10,
          ticks: range
        },
        indicator: {
          pointer: {
            style: {
              stroke:
                theme.styleSheet.paletteQualitative10[
                  index % theme.styleSheet.paletteQualitative10.length
                ]
            }
          },
          pin: {
            style: {
              stroke:
                theme.styleSheet.paletteQualitative10[
                  index % theme.styleSheet.paletteQualitative10.length
                ],
              r: getScaleValue(10, scale)
            }
          }
        }
      }
    } else {
      rangOptions = {
        indicator: {
          pin: {
            style: {
              r: getScaleValue(10, scale)
            }
          }
        }
      }
    }
    const customAttr = parseJson(chart.customAttr)
    if (customAttr.basicStyle.gradient) {
      const colorList = (theme.styleSheet?.paletteQualitative10 || []).map(ele => {
        return setGradientColor(ele, true)
      })
      if (!rangOptions.range) {
        rangOptions.range = {
          color: colorList
        }
      } else {
        rangOptions.range.color = colorList
      }
    }
    return { ...options, ...rangOptions }
  }

  protected configLabel(
    chart: Chart,
    options: GaugeOptions,
    context?: Record<string, any>
  ): GaugeOptions {
    const customAttr = parseJson(chart.customAttr)
    const data = chart.data.series[0].data[0]
    let labelContent: GaugeOptions['statistic']['content'] = false
    const label = customAttr.label
    const labelFormatter = label.labelFormatter ?? DEFAULT_LABEL.labelFormatter
    if (label.show) {
      labelContent = {
        style: {
          fontSize: `${label.fontSize}px`,
          color: label.color
        },
        formatter: function () {
          let value
          if (labelFormatter.type === 'percent') {
            value = options.percent
          } else {
            value = data
          }
          return valueFormatter(value, labelFormatter)
        }
      } as GaugeOptions['statistic']['content']
    }
    const statistic = {
      content: labelContent
    }
    const { gaugeAxisLine, gaugePercentLabel } = customAttr.basicStyle
    const { min, max } = context
    const tmp = {
      axis: {
        label: {
          formatter: v => {
            if (gaugeAxisLine === false) {
              return ''
            }
            if (gaugePercentLabel === false) {
              const val = v === '0' ? min : v === '1' ? max : min + (max - min) * v
              return valueFormatter(val, labelFormatter)
            }
            return v === '0' ? v : v * 100 + '%'
          }
        }
      }
    }
    options = merge(options, tmp)
    return { ...options, statistic }
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    chart.customAttr.label = {
      ...chart.customAttr.label,
      show: true,
      labelFormatter: {
        type: 'value',
        thousandSeparator: true,
        decimalCount: 0,
        unit: 1
      }
    }
    return chart
  }

  protected setupOptions(
    chart: Chart,
    options: GaugeOptions,
    context: Record<string, any>
  ): GaugeOptions {
    return flow(
      this.configTheme,
      this.configMisc,
      this.configLabel,
      this.configRange
    )(chart, options, context)
  }
  constructor() {
    super('gauge', DEFAULT_DATA)
  }
}
