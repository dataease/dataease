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
import { ViewSpec } from '@/views/chart/components/js/panel/charts/g2/bar/barUtil'
import { useI18n } from '@/hooks/web/useI18n'
import { Bar } from '@/views/chart/components/js/panel/charts/g2/bar/bar'
import { getLineDash, setGradientColor } from '@/views/chart/components/js/panel/common/common_antv'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { Chart } from '@antv/g2'
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
    }
    let columnWidthRatio
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
        columnWidthRatio
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
    const tmpOptions = {
      ...options,
      children: [
        {
          ...children[0],
          axis: { ...children[0].axis, y: { ...children[0].axis.y, ...xAxis } }
        },
        ...children.slice(1)
      ]
    }
    const customStyle = parseJson(chart.customStyle)
    const xAxisAtt = JSON.parse(JSON.stringify(customStyle['xAxis']))
    if (!xAxisAtt.axisValue?.auto) {
      return {
        ...tmpOptions,
        scale: {
          ...options.scale,
          y: {
            ...options.scale.y,
            nice: true,
            clamp: true,
            domain: [xAxisAtt.axisValue.min, xAxisAtt.axisValue.max],
            tickCount: xAxisAtt.axisValue.splitCount
          }
        }
      }
    }
    return tmpOptions
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
      // 刻度值旋转角度
      const rotate = axis.axisLabel.rotate
      const labelTransform = {
        type: 'rotate',
        optionalAngles: !rotate || rotate === 0 ? [] : [rotate],
        recoverWhenFailed: false
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
        ...(rotate === 0 ? {} : { transform: [labelTransform] })
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

  protected setupOptions(chart: Chart, options: ViewSpec): ViewSpec {
    return flow(
      this.configTheme,
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
