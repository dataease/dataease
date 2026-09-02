import { flow, hexColorToRGBA, parseJson } from '@/views/chart/components/js/util'
import {
  DEFAULT_LABEL,
  DEFAULT_MISC,
  DEFAULT_THRESHOLD,
  getScaleValue
} from '@/views/chart/components/editor/util/chart'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import {
  getG2Renderer,
  handleChartDashboardHidden,
  setGradientColor
} from '@/views/chart/components/js/panel/common/common_antv'
import { useI18n } from '@/hooks/web/useI18n'
import { defaultsDeep } from 'lodash-es'
import { G2Spec, Chart as G2Chart } from '@antv/g2'
import { G2ChartView, G2DrawOptions } from '../../../types/impl/g2'
import { RuntimeOptions } from '@antv/g2/lib/api/runtime'

const { t } = useI18n()

const DEFAULT_DATA = []
const GAUGE_PADDING = { top: 0, right: 10, bottom: 15, left: 10 }
const G2_GAUGE_OUTER_RADIUS = 1.15
const V2_GAUGE_RADIUS = 0.95
const GAUGE_ANGLE_EPSILON = 1e-10
const GAUGE_TEXT_PIN_GAP = 12
const GAUGE_TEXT_LABEL_GAP = 4

type GaugeLayout = {
  insetLeft: number
  insetRight: number
  insetTop: number
  insetBottom: number
  centerY: number
  polarRadius: number
  arcCenterY: number
}

const normalizeGaugeAngles = (
  startDegree: number,
  endDegree: number,
  startFallback: number,
  endFallback: number
) => {
  const start = Number.isFinite(Number(startDegree)) ? Number(startDegree) : startFallback
  let end = Number.isFinite(Number(endDegree)) ? Number(endDegree) : endFallback
  // 结束角不大于起始角时沿顺时针补足一圈，等角度按整圆处理
  while (end <= start) {
    end += 360
  }
  return {
    startAngle: (start * Math.PI) / 180,
    endAngle: (end * Math.PI) / 180
  }
}

const getGaugeUnitBox = (startAngle: number, endAngle: number) => {
  if (Math.abs(endAngle - startAngle) >= Math.PI * 2 - GAUGE_ANGLE_EPSILON) {
    return { minX: -1, maxX: 1, minY: -1, maxY: 1 }
  }
  const xs = [0, Math.cos(startAngle), Math.cos(endAngle)]
  const ys = [0, Math.sin(startAngle), Math.sin(endAngle)]
  for (
    let angle = Math.min(startAngle, endAngle);
    angle < Math.max(startAngle, endAngle);
    angle += Math.PI / 18
  ) {
    xs.push(Math.cos(angle))
    ys.push(Math.sin(angle))
  }
  return {
    minX: Math.min(...xs),
    maxX: Math.max(...xs),
    minY: Math.min(...ys),
    maxY: Math.max(...ys)
  }
}

const getGaugeLayout = (
  containerWidth: number,
  containerHeight: number,
  startAngle: number,
  endAngle: number
): GaugeLayout | undefined => {
  if (!Number.isFinite(containerWidth) || !Number.isFinite(containerHeight)) {
    return
  }
  const width = containerWidth - GAUGE_PADDING.left - GAUGE_PADDING.right
  const height = containerHeight - GAUGE_PADDING.top - GAUGE_PADDING.bottom
  if (width <= 0 || height <= 0) {
    return
  }
  const box = getGaugeUnitBox(startAngle, endAngle)
  const boxWidth = box.maxX - box.minX
  const boxHeight = box.maxY - box.minY
  if (boxWidth <= 0 || boxHeight <= 0) {
    return
  }
  const left = Math.abs(box.minX) / boxWidth
  const top = Math.abs(box.minY) / boxHeight
  const viewCenterX = width / 2
  const viewCenterY = height / 2
  let maxRadius: number
  let centerX: number
  let centerY: number
  if (height / boxHeight > width / boxWidth) {
    maxRadius = width / boxWidth
    centerX = viewCenterX - (0.5 - left) * width
    centerY = viewCenterY - (0.5 - top) * maxRadius * boxHeight
  } else {
    maxRadius = height / boxHeight
    centerX = viewCenterX - (0.5 - left) * maxRadius * boxWidth
    centerY = viewCenterY - (0.5 - top) * height
  }
  const polarRadius = maxRadius * V2_GAUGE_RADIUS
  const coordinateRadius = polarRadius / G2_GAUGE_OUTER_RADIUS
  return {
    insetLeft: centerX - coordinateRadius,
    insetRight: width - centerX - coordinateRadius,
    insetTop: centerY - coordinateRadius,
    insetBottom: height - centerY - coordinateRadius,
    centerY,
    polarRadius,
    arcCenterY: (box.minY + box.maxY) / 2
  }
}
const getGaugeTextLayout = (
  gaugeLayout: GaugeLayout | undefined,
  containerHeight: number,
  primaryFontSize: number,
  proportionFontSize: number
) => {
  const hasPrimary = primaryFontSize > 0
  const hasProportion = proportionFontSize > 0
  const labelGap = hasPrimary && hasProportion ? GAUGE_TEXT_LABEL_GAP : 0
  // 两行文本按各自半字号计算基线间距，避免大字号占比覆盖指标
  const proportionOffsetY =
    hasPrimary && hasProportion ? primaryFontSize / 2 + labelGap + proportionFontSize / 2 : 0
  const topExtent = hasPrimary ? primaryFontSize / 2 : proportionFontSize / 2
  const bottomExtent = hasProportion
    ? proportionOffsetY + proportionFontSize / 2
    : primaryFontSize / 2
  const blockHeight = Math.max(topExtent + bottomExtent, 16)
  if (!gaugeLayout || !Number.isFinite(containerHeight) || containerHeight <= 0) {
    return { textY: '60%', proportionOffsetY }
  }
  const centerY = GAUGE_PADDING.top + gaugeLayout.centerY
  const minGroupCenterY = GAUGE_PADDING.top + blockHeight / 2
  const maxGroupCenterY = containerHeight - GAUGE_PADDING.bottom - blockHeight / 2
  // 文本块固定在圆心与圆弧之间，缩放时只做连续位移和边界约束
  const direction = gaugeLayout.arcCenterY <= 0 ? -1 : 1
  const preferredGroupCenterY = centerY + direction * (GAUGE_TEXT_PIN_GAP + blockHeight / 2)
  const groupCenterY =
    minGroupCenterY <= maxGroupCenterY
      ? Math.max(minGroupCenterY, Math.min(preferredGroupCenterY, maxGroupCenterY))
      : containerHeight / 2
  const textY = groupCenterY - (bottomExtent - topExtent) / 2
  return { textY: `${(textY / containerHeight) * 100}%`, proportionOffsetY }
}
const clampGaugePercent = (percent: number) => {
  // 对齐 G2Plot 仪表盘行为，避免超出 0-1 后角度变化被成倍放大
  return Number.isFinite(percent) ? Math.max(0, Math.min(percent, 1)) : 0
}
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
    const containerDom = document.getElementById(container)
    const options = this.setupOptions(chart, initOptions, {
      scale,
      containerWidth: containerDom?.offsetWidth,
      containerHeight: containerDom?.offsetHeight
    })
    const newChart = new G2Chart({ container, ...getG2Renderer() })
    handleChartDashboardHidden(chart, options)
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
      const gaugeAngles = normalizeGaugeAngles(
        misc.gaugeStartAngle,
        misc.gaugeEndAngle,
        DEFAULT_MISC.gaugeStartAngle,
        DEFAULT_MISC.gaugeEndAngle
      )
      startAngle = gaugeAngles.startAngle
      endAngle = gaugeAngles.endAngle
      context.min = min
      context.max = max
    }
    const gaugeLayout = getGaugeLayout(
      context.containerWidth,
      context.containerHeight,
      startAngle,
      endAngle
    )
    context.gaugeLayout = gaugeLayout
    context.gaugeAngleSpan = endAngle - startAngle
    const percent = clampGaugePercent(
      (parseFloat(data) - parseFloat(min)) / (parseFloat(max) - parseFloat(min))
    )
    const tmp: G2Spec = {
      data: {
        value: {
          percent
        }
      },
      coordinate: {
        startAngle,
        endAngle
      },
      ...(gaugeLayout && {
        margin: 0,
        paddingTop: GAUGE_PADDING.top,
        paddingRight: GAUGE_PADDING.right,
        paddingBottom: GAUGE_PADDING.bottom,
        paddingLeft: GAUGE_PADDING.left,
        insetLeft: gaugeLayout.insetLeft,
        insetRight: gaugeLayout.insetRight,
        insetTop: gaugeLayout.insetTop,
        insetBottom: gaugeLayout.insetBottom
      })
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
    if (!label.show) {
      defaultsDeep(options, {
        style: {
          textContent: () => ''
        }
      })
      return options
    }
    const labelFormatter = label.labelFormatter ?? DEFAULT_LABEL.labelFormatter
    const proportionFormatter = label.proportionSeriesFormatter
    const primaryFontSize = label.childrenShow ? Number(label.fontSize) || 0 : 0
    const proportionFontSize = proportionFormatter.show
      ? Number(proportionFormatter.fontSize) || 0
      : 0
    const { textY, proportionOffsetY } = getGaugeTextLayout(
      context?.gaugeLayout,
      Number(context?.containerHeight),
      primaryFontSize,
      proportionFontSize
    )
    if (label.childrenShow) {
      const labelTitleOption = {
        style: {
          textFontSize: label.fontSize,
          textFill: label.color,
          textY,
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
      defaultsDeep(options, labelTitleOption)
    } else {
      defaultsDeep(options, {
        style: {
          textContent: () => ''
        }
      })
    }
    if (proportionFormatter.show) {
      const { min, max } = context
      // 组合视图必须继承仪表盘布局，否则 G2 会按坐标轴重新留白并缩小圆弧
      const gaugeViewLayout = {
        margin: options.margin,
        paddingTop: options.paddingTop,
        paddingRight: options.paddingRight,
        paddingBottom: options.paddingBottom,
        paddingLeft: options.paddingLeft,
        insetTop: options.insetTop,
        insetRight: options.insetRight,
        insetBottom: options.insetBottom,
        insetLeft: options.insetLeft
      }
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
          y: textY,
          dy: proportionOffsetY,
          fontSize: proportionFormatter.fontSize,
          fill: proportionFormatter.color,
          textAlign: 'center',
          textBaseline: 'middle'
        },
        tooltip: false
      }
      options = {
        type: 'view',
        autoFit: true,
        ...gaugeViewLayout,
        children: [options, labelProportionOption]
      }
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
    const customStyle = parseJson(chart.customStyle)
    const axisLabelColor = customStyle.yAxis?.axisLabel?.color ?? customAttr.label.color
    const { min, max } = context
    const isFullCircle = context.gaugeAngleSpan >= Math.PI * 2 - GAUGE_ANGLE_EPSILON
    const labelFormatter = customAttr.label.labelFormatter ?? DEFAULT_LABEL.labelFormatter
    const axisOption = {
      axis: {
        y: {
          labelFill: axisLabelColor,
          labelOpacity: 1,
          labelFillOpacity: 1,
          tickStroke: axisLabelColor,
          tickOpacity: 1,
          tickStrokeOpacity: 1,
          lineStroke: axisLabelColor,
          lineStrokeOpacity: 1,
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
          // 弧形轴不会自动注入避让策略，只保留主刻度后再进行碰撞隐藏
          labelFilter: (_v, id, ticks) => {
            return (
              gaugeAxisLine !== false && id % 5 === 0 && !(isFullCircle && id === ticks.length - 1)
            )
          },
          labelOverlap: [{ type: 'hide', keepHeader: true, keepTail: true, margin: [2, 4, 2, 4] }],
          labelFormatter: v => {
            if (gaugeAxisLine === false) {
              return ' '
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
