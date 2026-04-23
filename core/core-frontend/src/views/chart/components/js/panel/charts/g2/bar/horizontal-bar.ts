import {
  BAR_AXIS_TYPE,
  BAR_EDITOR_PROPERTY,
  BAR_EDITOR_PROPERTY_INNER
} from '@/views/chart/components/js/panel/charts/g2/bar/common'
import {
  flow,
  hexColorToRGBA,
  parseJson,
  setUpStackSeriesColor
} from '@/views/chart/components/js/util'
import {
  ViewSpec,
  handleEmptyDataStrategy
} from '@/views/chart/components/js/panel/charts/g2/bar/barUtil'
import { useI18n } from '@/hooks/web/useI18n'
import { Bar } from '@/views/chart/components/js/panel/charts/g2/bar/bar'
import { getLineDash, setGradientColor } from '@/views/chart/components/js/panel/common/common_antv'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { DEFAULT_BASIC_STYLE } from '@/views/chart/components/editor/util/chart'
import { defaultsDeep } from 'lodash-es'

const { t } = useI18n()
/**
 * 基础条形图
 */
export class HorizontalBar extends Bar {
  axisConfig = {
    ...this['axisConfig'],
    xAxis: {
      name: `${t('chart.drag_block_type_axis')} / ${t('chart.dimension')}`,
      type: 'd'
    },
    yAxis: {
      name: `${t('chart.drag_block_value_axis')} / ${t('chart.quota')}`,
      type: 'q'
    }
  }
  properties = BAR_EDITOR_PROPERTY
  propertyInner = {
    ...BAR_EDITOR_PROPERTY_INNER,
    'basic-style-selector': [...BAR_EDITOR_PROPERTY_INNER['basic-style-selector'], 'seriesColor'],
    'label-selector': ['hPosition', 'seriesLabelFormatter'],
    'tooltip-selector': ['fontSize', 'color', 'backgroundColor', 'seriesTooltipFormatter', 'show'],
    'x-axis-selector': [
      ...BAR_EDITOR_PROPERTY_INNER['x-axis-selector'],
      'axisLabelFormatter',
      'axisValue'
    ],
    'y-axis-selector': [
      'name',
      'color',
      'fontSize',
      'axisLine',
      'splitLine',
      'axisForm',
      'axisLabel',
      'position',
      'showLengthLimit'
    ]
  }
  axis: AxisType[] = [...BAR_AXIS_TYPE]

  protected configBasicStyle(chart: Chart, options: ViewSpec): ViewSpec {
    const { children } = options
    const { basicStyle = {} } = parseJson(chart.customAttr) || {}
    const colors = (basicStyle.colors || []).map(ele =>
      basicStyle.gradient
        ? setGradientColor(hexColorToRGBA(ele, basicStyle.alpha), true)
        : hexColorToRGBA(ele, basicStyle.alpha)
    )
    let style = {
      ...(basicStyle.radiusColumnBar === 'topRoundAngle' && {
        radiusTopLeft: basicStyle.columnBarRightAngleRadius,
        radiusTopRight: basicStyle.columnBarRightAngleRadius
      }),
      ...(basicStyle.radiusColumnBar === 'roundAngle' && {
        radius: basicStyle.columnBarRightAngleRadius
      }),
      ...(basicStyle.radiusColumnBar !== 'topRoundAngle' &&
        basicStyle.radiusColumnBar !== 'roundAngle' && { radius: 0 })
    } as any
    let columnWidthRatio: number | undefined
    const _v = basicStyle.columnWidthRatio ?? DEFAULT_BASIC_STYLE.columnWidthRatio
    if (_v >= 1 && _v <= 100) {
      columnWidthRatio = _v / 100.0
    } else if (_v < 1) {
      columnWidthRatio = 1 / 100.0
    } else if (_v > 100) {
      columnWidthRatio = 1
    }
    if (columnWidthRatio) {
      style = {
        ...style,
        columnWidthRatio: columnWidthRatio
      }
    }
    if (
      this.name === 'bar-stack-horizontal' ||
      this.name === 'progress-bar' ||
      this.name === 'bar-range'
    ) {
      children[0].scale.x = {
        type: 'band',
        paddingInner: 0.01
      }
    }
    children[0].scale.color.range = colors
    children[0].scale.y.nice = true
    children[0].style = { ...children[0].style, ...style }
    return options
  }

  protected configLabel(chart: Chart, options: ViewSpec): ViewSpec {
    const tmpOptions = super.configLabel(chart, options)
    const { children } = tmpOptions
    if (children?.[0].labels?.length) {
      const newLabel = {
        ...children[0].labels[0],
        textAlign: 'start'
      }
      return {
        ...tmpOptions,
        children: [{ ...children[0], labels: [newLabel] }, ...children.slice(1)]
      }
    }
    return tmpOptions
  }

  protected configYAxis(chart: Chart, options: ViewSpec): ViewSpec {
    const { children } = options
    const xAxis = this.getAxisConfig(chart, 'xAxis')
    // 让末端刻度标签有空间完整绘制
    const valueAxisExtra: Record<string, any> = xAxis
      ? {
          labelAutoHide: false,
          labelAutoRotate: false
        }
      : {}
    const tmpOptions = {
      ...options,
      children: [
        {
          ...children[0],
          axis: {
            ...children[0].axis,
            y: { ...children[0].axis.y, ...xAxis, ...valueAxisExtra }
          }
        },
        ...children.slice(1)
      ]
    }
    const customStyle = parseJson(chart.customStyle)
    const xAxisAtt = JSON.parse(JSON.stringify(customStyle['xAxis']))
    // 预留右侧画布空间，避免数值轴最后一个刻度标签被裁剪
    const appliedOptions = this.reserveValueAxisRightMargin(chart, tmpOptions, xAxisAtt, xAxis)
    if (!xAxisAtt.axisValue?.auto) {
      const child0 = appliedOptions.children[0]
      return {
        ...appliedOptions,
        children: [
          {
            ...child0,
            scale: {
              ...child0.scale,
              y: {
                nice: false,
                clamp: true,
                domain: [xAxisAtt.axisValue.min, xAxisAtt.axisValue.max],
                tickCount: xAxisAtt.axisValue.splitCount,
                tickMethod: (min, max, count) => {
                  const n = Math.max(2, count)
                  const step = (max - min) / (n - 1)
                  const ticks = []
                  for (let i = 0; i < n; i++) {
                    ticks.push(min + step * i)
                  }
                  return ticks
                }
              }
            }
          },
          ...appliedOptions.children.slice(1)
        ]
      }
    }
    return appliedOptions
  }

  /**
   * 基于当前数据与数值轴格式化，估算数值轴最长刻度标签的渲染
   * 并在 ViewSpec 顶层设置足够的 margin，避免末端刻度标签被画布裁剪
   */
  private reserveValueAxisRightMargin(
    chart: Chart,
    options: ViewSpec,
    xAxisAtt: any,
    valueAxisCfg: any
  ): ViewSpec {
    if (!valueAxisCfg || !xAxisAtt?.show || xAxisAtt?.axisLabel?.show === false) {
      return options
    }
    const data = (chart.data?.data ?? []) as Array<{ value?: number }>
    if (!data.length) {
      return options
    }

    let maxVal = -Infinity
    let minVal = Infinity
    for (const d of data) {
      const v = Number(d?.value)
      if (!Number.isFinite(v)) continue
      if (v > maxVal) maxVal = v
      if (v < minVal) minVal = v
    }
    if (!Number.isFinite(maxVal)) {
      return options
    }
    const axisValue = xAxisAtt?.axisValue
    if (axisValue && !axisValue.auto) {
      const fixedMax = Number(axisValue.max)
      const fixedMin = Number(axisValue.min)
      if (Number.isFinite(fixedMax)) maxVal = fixedMax
      if (Number.isFinite(fixedMin)) minVal = fixedMin
    }

    const formatter = xAxisAtt.axisLabelFormatter
    const maxStr = String(valueFormatter(maxVal, formatter) ?? maxVal)
    const minStr = String(valueFormatter(minVal, formatter) ?? minVal)
    const longestLen = Math.max(maxStr.length, minStr.length)

    // 字符宽估算
    const fontSize = Number(xAxisAtt?.axisLabel?.fontSize) || 12
    const labelW = longestLen * fontSize * 0.6

    // 旋转角度取绝对值
    const rotateDeg = Math.min(90, Math.abs(Number(xAxisAtt?.axisLabel?.rotate) || 0))

    // 默认居中对齐, 末端标签溢出 labelW/2
    // 锚点变为刻度线处左中点, 标签整体向右下延伸 labelW*cosθ
    const rightOverflow =
      rotateDeg === 0 ? labelW / 2 : labelW * Math.cos((rotateDeg * Math.PI) / 180)

    const buffer = 8
    const marginRight = Math.ceil(rightOverflow + buffer)

    const prev = options as any
    const next: Record<string, any> = {
      ...options,
      marginRight: Math.max(Number(prev.marginRight) || 0, marginRight)
    }

    if (xAxisAtt?.position === 'top') {
      const labelH = fontSize * 1.2
      const topOverflow = rotateDeg === 0 ? labelH : labelW * Math.sin((rotateDeg * Math.PI) / 180)
      next.marginTop = Math.max(Number(prev.marginTop) || 0, Math.ceil(topOverflow + buffer))
    }
    return next as ViewSpec
  }

  protected configXAxis(chart: Chart, options: ViewSpec): ViewSpec {
    const { children } = options
    const yAxis = this.getAxisConfig(chart, 'yAxis')
    const tmpOptions = {
      ...options,
      children: [
        {
          ...children[0],
          axis: { ...children[0].axis, x: { ...children[0].x, ...yAxis } }
        },
        ...children.slice(1)
      ]
    }
    return tmpOptions
  }

  protected getAxisConfig(chart: Chart, axisType: string): any {
    const customStyle = parseJson(chart.customStyle)
    const axis = JSON.parse(JSON.stringify(customStyle[axisType]))
    if (customStyle[axisType] && axis.show) {
      // 轴线
      const line = {
        line: axis.axisLine.show,
        lineStrokeOpacity: 1,
        lineLineWidth: axis.axisLine.lineStyle.width,
        lineStroke: axis.axisLine.lineStyle.color,
        lineLineDash: getLineDash(axis.axisLine.lineStyle.style)
      }
      // 刻度
      const tick = {
        tick: axis.axisLine.show,
        tickLineWidth: axis.axisLine.lineStyle.width,
        tickStroke: axis.axisLine.lineStyle.color,
        tickStrokeOpacity: 2
      }
      // 网格线
      const grid = {
        grid: axis.splitLine.show,
        gridLineWidth: axis.splitLine.lineStyle.width,
        gridStroke: axis.splitLine.lineStyle.color,
        gridStrokeOpacity: 1,
        gridLineDash: getLineDash(axis.splitLine.lineStyle.style)
      }
      // 标签（刻度值）
      const label = {
        label: axis.axisLabel.show,
        labelFontSize: axis.axisLabel.fontSize,
        labelFill: axis.axisLabel.color,
        labelOpacity: 1,
        labelFormatter: value => {
          if (axisType === 'xAxis') {
            return valueFormatter(value, axis.axisLabelFormatter)
          }
          return value
        }
      }
      const x = {
        position: axis.position,
        // 标题
        title: axis.nameShow && axis.name ? axis.name : false,
        titleFontSize: axis.fontSize,
        titleFill: axis.color,
        // 轴线
        ...line,
        // 刻度线
        ...tick,
        // 网格线
        ...grid,
        // 刻度值
        ...label,
        labelAutoHide: true,
        labelAutoRotate: false,
        labelTransform: `rotate(${Math.abs(axis.axisLabel.rotate || 0)})`
      }
      return x
    }
    return false
  }

  public setupSeriesColor(chart: ChartObj, data?: any[]): ChartBasicStyle['seriesColor'] {
    return setUpStackSeriesColor(chart, data)
  }

  protected configColor(chart: Chart, options: ViewSpec): ViewSpec {
    const { basicStyle } = parseJson(chart.customAttr)
    const { seriesColor } = basicStyle
    if (!seriesColor?.length) {
      return options
    }
    const { xAxis, yAxis, extStack } = chart
    if (!xAxis?.length || !yAxis?.length) {
      return options
    }
    const relations = []
    if (extStack?.length) {
      seriesColor.forEach(item => {
        let color = hexColorToRGBA(item.color, basicStyle.alpha)
        if (basicStyle.gradient) {
          color = setGradientColor(color, true)
        }
        relations.push([item.id, color])
      })
    } else {
      const colorMap = seriesColor.reduce((pre, next) => {
        pre[next.id] = next.color
        return pre
      }, {})
      yAxis.forEach(item => {
        if (colorMap[item.id]) {
          let color = hexColorToRGBA(colorMap[item.id], basicStyle.alpha)
          if (basicStyle.gradient) {
            color = setGradientColor(color, true)
          }
          relations.push([item.chartShowName ?? item.name, color])
        }
      })
    }
    if (relations.length) {
      const scaleOptions = {
        scale: {
          color: {
            relations
          }
        }
      }
      defaultsDeep(options, scaleOptions)
    }
    return options
  }

  protected configEmptyDataStrategy(chart: Chart, options: ViewSpec): ViewSpec {
    handleEmptyDataStrategy(chart, options)
    return options
  }

  protected setupOptions(chart: Chart, options: ViewSpec): ViewSpec {
    return flow(
      this.configTheme,
      this.configEmptyDataStrategy,
      this.configBasicStyle,
      this.configColor,
      this.configLabel,
      this.configTooltip,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configAnalyse,
      this.configBarConditions,
      this.configSlider
    )(chart, options, {}, this)
  }

  constructor(name = 'bar-horizontal') {
    super(name)
    Object.assign(this.intervalOptions, {
      coordinate: { transform: [{ type: 'transpose' }] },
      scale: {
        color: { range: [] },
        y: { nice: true },
        x: {
          type: 'band',
          paddingInner: -0.21
        }
      }
    })
  }
}
