import {
  G2PlotChartView,
  G2PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/g2plot'
import type { Gauge as G2Gauge, GaugeOptions } from '@antv/g2plot/esm/plots/gauge'
import { flow, parseJson } from '@/views/chart/components/js/util'
import {
  DEFAULT_LABEL,
  DEFAULT_MISC,
  DEFAULT_THRESHOLD,
  getScaleValue
} from '@/views/chart/components/editor/util/chart'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { setGradientColor } from '@/views/chart/components/js/panel/common/common_antv'
import { useI18n } from '@/hooks/web/useI18n'
import { merge } from 'lodash-es'

const { t } = useI18n()

const DEFAULT_DATA = []
// 刻度值向表盘内侧偏移，给刻度线和外圈预留空间
const GAUGE_LABEL_OFFSET = -20
// 指标值与占比文本之间保留的视觉间距
const GAUGE_STATISTIC_GAP = 4

/** 根据渲染后的极坐标半径计算中心统计文本需要下移的距离 */
const getGaugeStatisticOffset = (gauge: G2Gauge, options: GaugeOptions) => {
  const coordinate = gauge.chart.views.find(view => view.id === 'indicator-view')?.getCoordinate()
  const statistic = options.statistic?.title || options.statistic?.content
  if (!coordinate || !statistic) {
    return 0
  }

  // 只有仪表盘弧线跨越正上方时，顶部刻度值才可能挤占中心统计文本空间
  let topAngle = -Math.PI / 2
  while (topAngle < coordinate.startAngle) {
    topAngle += Math.PI * 2
  }
  if (topAngle > coordinate.endAngle) {
    return 0
  }

  const statisticFontSize = parseFloat(`${(statistic.style as any)?.fontSize}`) || 0
  const axisFontSize =
    parseFloat(`${options.axis && (options.axis.label?.style as any)?.fontSize}`) || 12
  // 通过弧线最高点与两端较低边界之间的可用高度估算统计文本安全区
  const endpointY = Math.max(Math.sin(coordinate.startAngle), Math.sin(coordinate.endAngle))
  // 保留刻度标签与刻度线的间距，小半径时仅下移中心统计值
  return Math.max(
    0,
    Math.ceil(
      -(1 + endpointY) * coordinate.getRadius() -
        GAUGE_LABEL_OFFSET +
        statisticFontSize +
        axisFontSize / 2 +
        4 -
        (statistic.offsetY || 0)
    )
  )
}

export class Gauge extends G2PlotChartView<GaugeOptions, G2Gauge> {
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

  async drawChart(drawOptions: G2PlotDrawOptions<G2Gauge>): Promise<G2Gauge> {
    const { chart, container, scale, action } = drawOptions
    if (!chart.data?.series || !chart.yAxis.length) {
      return
    }
    // options
    const initOptions: GaugeOptions = {
      percent: 0,
      appendPadding: [0, 10, 15, 10],
      axis: {
        tickInterval: 0.2,
        label: {
          // G2Plot 2.x CircleAxis 仅在存在 verticalLimitLength 时执行该隐藏策略
          autoHide: true,
          offset: GAUGE_LABEL_OFFSET,
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
    const { Gauge: G2Gauge } = await import('@antv/g2plot/esm/plots/gauge')
    const newChart = new G2Gauge(container, options)
    let statisticOffset = 0
    newChart.on('afterrender', () => {
      // 极坐标半径在首次渲染及容器 resize 后才可靠，此处按实际尺寸重新计算
      const nextOffset = getGaugeStatisticOffset(newChart, options)
      if (nextOffset !== statisticOffset) {
        statisticOffset = nextOffset
        const { title, content } = options.statistic || {}
        // 只在偏移变化时更新，避免 update 再次触发 afterrender 后形成循环
        newChart.update({
          statistic: {
            title: title && { offsetY: (title.offsetY || 0) + nextOffset },
            content: content && { offsetY: (content.offsetY || 0) + nextOffset }
          }
        })
        return
      }
      action({
        from: 'gauge',
        data: {
          type: 'gauge',
          max: chart.data?.series[0]?.data[0]
        }
      })
    })
    const hasNoneData = chart.data?.series.some(
      s => s.data?.[0] === undefined || s.data?.[0] === null
    )
    this.configEmptyDataStyle(hasNoneData ? [] : [1], container, newChart)
    if (hasNoneData) {
      return
    }
    return newChart
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
    let labelTitle: GaugeOptions['statistic']['title'] = false
    let labelContent: GaugeOptions['statistic']['content'] = false
    const label = customAttr.label
    const labelFormatter = label.labelFormatter ?? DEFAULT_LABEL.labelFormatter
    if (label.show && label.childrenShow) {
      labelTitle = {
        style: {
          fontSize: `${label.fontSize}px`,
          color: label.color
        },
        formatter: function () {
          return valueFormatter(data, labelFormatter)
        }
      } as GaugeOptions['statistic']['title']
    }
    const { min, max } = context
    if (label.show && label.proportionSeriesFormatter.show) {
      const proportionFormatter = label.proportionSeriesFormatter
      labelContent = {
        // G2Plot 已根据占比字号定位文本，此处只追加缩放后的视觉行间距
        offsetY:
          proportionFormatter.fontSize + getScaleValue(GAUGE_STATISTIC_GAP, context?.scale ?? 1),
        style: {
          fontSize: `${proportionFormatter.fontSize}px`,
          color: proportionFormatter.color
        },
        formatter: function () {
          const proportionValue = ((parseFloat(data) - min) / (max - min)) * 100
          return (
            t('chart.proportion') +
            '： ' +
            proportionValue.toFixed(proportionFormatter.formatterCfg.decimalCount) +
            '%'
          )
        }
      } as GaugeOptions['statistic']['content']
    }
    const statistic = {
      title: labelTitle,
      content: labelContent
    }
    const { gaugeAxisLine, gaugePercentLabel } = customAttr.basicStyle
    const tmp = {
      axis: {
        label: {
          formatter: v => {
            if (gaugeAxisLine === false) {
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
