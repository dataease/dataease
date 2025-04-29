import { flow, hexColorToRGBA, parseJson } from '@/views/chart/components/js/util'
import {
  DEFAULT_LABEL,
  DEFAULT_MISC,
  DEFAULT_THRESHOLD,
  getScaleValue
} from '@/views/chart/components/editor/util/chart'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { setGradientColor } from '@/views/chart/components/js/panel/common/common_antv'
import { useI18n } from '@/hooks/web/useI18n'
import { defaultsDeep } from 'lodash-es'
import { G2Spec, Chart as G2Chart } from '@antv/g2'
import { G2ChartView, G2DrawOptions } from '../../../types/impl/g2'
import { RuntimeOptions } from '@antv/g2/lib/api/runtime'

const { t } = useI18n()

const DEFAULT_DATA = []
export class Gauge extends G2ChartView {
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

  async drawChart(drawOptions: G2DrawOptions<G2Chart>): Promise<G2Chart> {
    const { chart, container, scale, action } = drawOptions
    if (!chart.data?.series || !chart.yAxis.length) {
      return
    }
    // options
    const initOptions: G2Spec = {
      type: 'gauge',
      autoFit: true,
      axis: {
        y: {
          labelDirection: 'positive',
          tickDirection: 'positive'
        }
      },
      legend: false,
      coordinate: {
        type: 'radial',
        innerRadius: 1,
        outerRadius: 1.15
      }
    }
    const options = this.setupOptions(chart, initOptions, { scale })
    const newChart = new G2Chart({ container })
    newChart.options(options)
    newChart.on('afterrender', () => {
      action({
        from: 'gauge',
        data: {
          type: 'gauge',
          max: chart.data?.series[0]?.data[0]
        }
      })
    })
    const hasNoneData = chart.data?.series.some(s => !s.data?.[0])
    this.configEmptyDataStyle(hasNoneData ? [] : [1], container, newChart)
    if (hasNoneData) {
      return
    }
    return newChart
  }

  protected configMisc(chart: Chart, options: G2Spec, context: Record<string, any>): G2Spec {
    const customAttr = parseJson(chart.customAttr)
    const data = chart.data.series[0].data[0]
    let min, max, startAngle, endAngle
    if (customAttr.misc) {
      const misc = customAttr.misc
      if (misc.gaugeMinType === 'dynamic' && misc.gaugeMaxType === 'dynamic') {
        min = chart.data?.series[chart.data?.series.length - 2]?.data[0]
        max = chart.data?.series[chart.data?.series.length - 1]?.data[0]
      } else if (misc.gaugeMinType !== 'dynamic' && misc.gaugeMaxType === 'dynamic') {
        min = misc.gaugeMin || misc.gaugeMin === 0 ? misc.gaugeMin : DEFAULT_MISC.gaugeMin
        max = chart.data?.series[chart.data?.series.length - 1]?.data[0]
      } else if (misc.gaugeMinType === 'dynamic' && misc.gaugeMaxType !== 'dynamic') {
        min = chart.data?.series[chart.data?.series.length - 1]?.data[0]
        max = misc.gaugeMax ? misc.gaugeMax : DEFAULT_MISC.gaugeMax
      } else {
        min = misc.gaugeMin || misc.gaugeMin === 0 ? misc.gaugeMin : DEFAULT_MISC.gaugeMin
        max = misc.gaugeMax
          ? misc.gaugeMax
          : chart.data?.series[chart.data?.series.length - 1]?.data[0]
      }
      startAngle = (misc.gaugeStartAngle * Math.PI) / 180
      endAngle = (misc.gaugeEndAngle * Math.PI) / 180
      context.min = min
      context.max = max
    }
    const percent = (parseFloat(data) - parseFloat(min)) / (parseFloat(max) - parseFloat(min))
    const tmp: G2Spec = {
      data: {
        value: {
          percent
        }
      },
      coordinate: {
        startAngle,
        endAngle
      }
    }
    defaultsDeep(options, tmp)
    return options
  }

  protected configTheme(chart: Chart, options: G2Spec): G2Spec {
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
      color: colors[0],
      category10: colors,
      category20: colors,
      view: {
        viewFill: bgColor
      }
    }
    return { ...options, theme }
  }

  private configRange(chart: Chart, options: G2Spec, context: Record<string, any>): G2Spec {
    const { scale } = context
    const thresholds = []
    let index = 0
    let flag = false
    let hasThreshold = false
    const theme = options.theme as any
    const customAttr = parseJson(chart.customAttr)
    if (customAttr.basicStyle.gradient) {
      const colorList = (theme.category10 || []).map(ele => {
        return setGradientColor(ele, true)
      })
      theme.category10 = colorList
    }
    if (chart.senior) {
      const senior = parseJson(chart.senior)
      const threshold = senior.threshold ?? DEFAULT_THRESHOLD
      if (threshold.enable && threshold.gaugeThreshold) {
        hasThreshold = true
        const arr = threshold.gaugeThreshold.split(',')
        for (let i = 0; i < arr.length; i++) {
          const ele = arr[i]
          const p = parseFloat(ele) / 100
          thresholds.push(p)
          if (!flag && options.data.value.percent <= p) {
            flag = true
            index = i
          }
        }
        if (!flag) {
          index = arr.length
        }
      }
    }
    thresholds.push(1)
    let rangOptions: G2Spec
    if (hasThreshold) {
      rangOptions = {
        data: {
          value: {
            thresholds
          }
        },
        scale: {
          color: {
            range: theme.category10.slice(0, thresholds.length)
          }
        },
        style: {
          pointerStroke: theme.category10[index % theme.category10.length],
          pinStroke: theme.category10[index % theme.category10.length],
          pinR: getScaleValue(10, scale)
        }
      }
    } else {
      rangOptions = {
        scale: {
          color: {
            range: theme.category10.slice(0, 1)
          }
        },
        style: {
          pointerStroke: theme.category10[0],
          pinStroke: theme.category10[0],
          pinR: getScaleValue(10, scale)
        }
      }
    }
    return defaultsDeep(options, rangOptions)
  }

  protected configLabel(chart: Chart, options: G2Spec, context?: Record<string, any>): G2Spec {
    const customAttr = parseJson(chart.customAttr)
    const data = chart.data.series[0].data[0]
    const label = customAttr.label
    const labelFormatter = label.labelFormatter ?? DEFAULT_LABEL.labelFormatter
    if (label.show) {
      let proportionOffsetY = 0
      if (label.childrenShow) {
        const labelTitleOption = {
          style: {
            textFontSize: label.fontSize,
            textFill: label.color,
            textContent: () => {
              let value
              if (labelFormatter.type === 'percent') {
                value = options.data.value.percent
              } else {
                value = data
              }
              return valueFormatter(value, labelFormatter)
            }
          }
        }
        proportionOffsetY = label.fontSize
        defaultsDeep(options, labelTitleOption)
      }
      if (label.proportionSeriesFormatter.show) {
        const { min, max } = context
        const proportionFormatter = label.proportionSeriesFormatter
        const labelProportionOption = {
          type: 'text',
          style: {
            text: () => {
              const proportionValue = ((parseFloat(data) - min) / (max - min)) * 100
              return (
                t('chart.proportion') +
                '： ' +
                proportionValue.toFixed(proportionFormatter.formatterCfg.decimalCount) +
                '%'
              )
            },
            x: '50%',
            y: '60%',
            dy: proportionOffsetY,
            fontSize: proportionFormatter.fontSize,
            fill: proportionFormatter.color,
            textAlign: 'center'
          },
          tooltip: false
        }
        options = {
          type: 'view',
          autoFit: true,
          children: [options, labelProportionOption]
        }
      }
    } else {
      defaultsDeep(options, {
        style: {
          textContent: ''
        }
      })
    }
    return options
  }

  protected configAxis(
    chart: Chart,
    options: RuntimeOptions,
    context: Record<string, any>
  ): G2Spec {
    const customAttr = parseJson(chart.customAttr)
    const { gaugeAxisLine, gaugePercentLabel } = customAttr.basicStyle
    const { min, max } = context
    const labelFormatter = customAttr.label.labelFormatter ?? DEFAULT_LABEL.labelFormatter
    const axisOption = {
      axis: {
        y: {
          tickLength: (_, id) => {
            if (id % 5 === 0) {
              return 15
            }
            return 10
          },
          tickCount: 25,
          tickMethod: (min, max, count) => {
            const ticks = []
            for (let i = 0; i <= count; i++) {
              ticks.push((min + ((max - min) * i) / count).toFixed(2))
            }
            return ticks
          },
          labelDirection: 'positive',
          tickDirection: 'positive',
          labelFormatter: (v, id) => {
            if (gaugeAxisLine === false) {
              return ' '
            }
            if (id % 5 !== 0) {
              return ''
            }
            if (gaugePercentLabel === false) {
              const resultV = v === '0' ? min : v === '1' ? max : min + (max - min) * v
              return labelFormatter.type === 'value'
                ? valueFormatter(resultV, labelFormatter)
                : resultV
            }
            return v === '0' ? v : v * 100 + '%'
          }
        }
      }
    }
    return defaultsDeep(options, axisOption)
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

  protected setupOptions(chart: Chart, options: G2Spec, context: Record<string, any>): G2Spec {
    return flow(
      this.configTheme,
      this.configMisc,
      this.configRange,
      this.configAxis,
      this.configLabel
    )(chart, options, context)
  }
  constructor() {
    super('gauge', DEFAULT_DATA)
  }
}
