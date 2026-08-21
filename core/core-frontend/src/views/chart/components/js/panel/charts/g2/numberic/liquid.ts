import { flow, hexColorToRGBA, parseJson } from '@/views/chart/components/js/util'
import { DEFAULT_MISC } from '@/views/chart/components/editor/util/chart'
import { formatterItem, valueFormatter } from '@/views/chart/components/js/formatter'
import { useI18n } from '@/hooks/web/useI18n'
import { G2ChartView, G2DrawOptions } from '../../../types/impl/g2'
import { Chart as G2Chart, G2Spec } from '@antv/g2'
import { defaultsDeep } from 'lodash-es'
import {
  getG2Renderer,
  handleChartDashboardHidden
} from '@/views/chart/components/js/panel/common/common_antv'

const { t } = useI18n()
const DEFAULT_LIQUID_DATA = []
/**
 * 水波图
 */
export class Liquid extends G2ChartView {
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
    'label-selector': ['fontSize', 'color', 'showQuota', 'showProportion'],
    'misc-selector': [
      'liquidShape',
      'liquidSize',
      'liquidMaxType',
      'liquidMaxField',
      'liquidShowBorder',
      'liquidBorderWidth',
      'liquidBorderDistance'
    ],
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

  async drawChart(drawOptions: G2DrawOptions<G2Chart>): Promise<G2Chart> {
    const { chart, container, action } = drawOptions
    if (!chart.data?.series || !chart.yAxis.length) {
      return
    }
    const initOptions: G2Spec = {
      type: 'liquid',
      autoFit: true,
      tooltip: false,
      interaction: {
        tooltip: false
      }
    }
    const context = {
      container
    }
    const options = this.setupOptions(chart, initOptions, context)
    const newChart = new G2Chart({ container, ...getG2Renderer() })
    handleChartDashboardHidden(chart, options)
    newChart.options(options)
    newChart.on('afterrender', () => {
      action({
        from: 'liquid',
        data: {
          type: 'liquid',
          max: chart.data?.series[0]?.data[0]
        }
      })
    })
    // 处理空数据, 只要有一个指标是空数据，就不显示图表
    const hasNoneData = chart.data?.series.some(s => !s.data?.[0])
    this.configEmptyDataStyle(hasNoneData ? [] : [1], container, newChart)
    if (hasNoneData) {
      return
    }
    return newChart
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

  protected configMisc(chart: Chart, options: G2Spec, context: Record<string, any>): G2Spec {
    const customAttr = parseJson(chart.customAttr)
    let value = 0
    if (chart.data.series.length > 0) {
      value = chart.data.series[0].data[0]
    }
    let max, radius, shape
    if (customAttr.misc) {
      const misc = customAttr.misc
      const defaultLiquidMax = chart.data?.series[chart.data?.series.length - 1]?.data[0]
      if (misc.liquidMaxType === 'dynamic') {
        max = defaultLiquidMax
      } else {
        max = misc.liquidMax ? misc.liquidMax : defaultLiquidMax
      }
      radius = (misc.liquidSize ? misc.liquidSize : DEFAULT_MISC.liquidSize) / 100
      shape = misc.liquidShape ?? DEFAULT_MISC.liquidShape
    }
    const { container } = context
    const dom = document.getElementById(container)
    const lessLength = Math.min(dom.clientWidth, dom.clientHeight)
    if (lessLength) {
      const margin = (lessLength * (1 - radius)) / 2
      options.margin = margin
    }
    const tmpOptions = {
      data: value / max,
      style: {
        shape,
        waveLength: lessLength ? lessLength * radius * 0.3 : 128,
        // G2 水波图默认绘制边框，关闭时需显式将宽度设为 0
        outlineBorder: customAttr.misc?.liquidShowBorder
          ? customAttr.misc.liquidBorderWidth ?? DEFAULT_MISC.liquidBorderWidth
          : 0,
        outlineDistance: customAttr.misc?.liquidShowBorder
          ? customAttr.misc.liquidBorderDistance ?? DEFAULT_MISC.liquidBorderDistance
          : 0
      }
    }
    defaultsDeep(options, tmpOptions)
    return options
  }

  protected configLabel(chart: Chart, options: G2Spec): G2Spec {
    const customAttr = parseJson(chart.customAttr)
    const originVal = options.data
    // 数值过大图表会异常，大于 1 无意义
    if (originVal > 1) {
      options = {
        ...options,
        data: 1
      }
    }
    const labelStyle: Record<string, any> = {
      contentText: ''
    }
    if (!customAttr.label?.show) {
      defaultsDeep(options, { style: labelStyle })
      return options
    }
    const label = customAttr.label
    const showQuota = label.showQuota === true
    const showProportion = label.showProportion ?? true
    // 水波图在中心文本中组合指标值与占比，支持两个配置项独立切换
    const quotaText = showQuota
      ? valueFormatter(
          chart.data.series[0].data[0],
          label.quotaLabelFormatter ?? label.labelFormatter ?? formatterItem
        )
      : ''
    const proportionText = showProportion
      ? `${(originVal * 100).toFixed(label.reserveDecimalCount ?? 2)}%`
      : ''
    labelStyle.contentFontSize = label.fontSize
    labelStyle.contentText = [quotaText, proportionText].filter(Boolean).join('\n')
    labelStyle.contentFill = label.color
    defaultsDeep(options, { style: labelStyle })
    return options
  }

  protected configThreshold(chart: Chart, options: G2Spec): G2Spec {
    const senior = parseJson(chart.senior)
    if (senior?.threshold?.enable) {
      const { liquidThreshold } = senior?.threshold
      if (liquidThreshold) {
        const { category10: colors } = options.theme
        const thresholdArr = liquidThreshold.split(',')
        let index = 0
        thresholdArr.forEach((v, i) => {
          if (options.data > parseFloat(v) / 100) {
            index = i + 1
          }
        })
        options.theme.color = colors[index % colors.length]
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

  protected setupOptions(chart: Chart, options: G2Spec, context: Record<string, any>): G2Spec {
    return flow(this.configTheme, this.configMisc, this.configLabel, this.configThreshold)(
      chart,
      options,
      context,
      this
    )
  }
  constructor() {
    super('liquid', DEFAULT_LIQUID_DATA)
  }
}
