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
  getHorizontalBarAxisSafeLabelStyle,
  handleBarBreakLineNullData,
  handleEmptyDataStrategy,
  ViewSpec
} from '@/views/chart/components/js/panel/charts/g2/bar/barUtil'
import { useI18n } from '@/hooks/web/useI18n'
import { Bar } from '@/views/chart/components/js/panel/charts/g2/bar/bar'
import {
  configAxisLengthLimit,
  configDimensionSlider,
  formatAxisLabelWithLengthLimit,
  getLineDash,
  setGradientColor
} from '@/views/chart/components/js/panel/common/common_antv'
import { valueFormatter } from '@/views/chart/components/js/formatter'
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
    // 横向堆叠同样按整根条的外轮廓圆角处理，避免只有右端圆角
    style = {
      ...style,
      ...this.getStackOuterRadiusStyle(basicStyle)
    }
    const columnWidthRatio = this.getColumnWidthRatio(basicStyle)
    const columnPadding = this.getColumnPadding(columnWidthRatio)
    if (columnWidthRatio) {
      style = {
        ...style,
        columnWidthRatio: this.getStyleColumnWidthRatio(columnPadding)
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
    // 横向条形图同样通过 x band 控制分类宽度，需要同步外层和 dodgeX 组内间距
    children[0].scale.x = {
      ...(children[0].scale.x || {}),
      padding: columnPadding,
      paddingInner: columnPadding
    }
    children[0].transform = this.configDodgePadding(children[0].transform, columnPadding)
    children[0].scale.color.range = colors
    children[0].scale.y.nice = true
    children[0].style = { ...children[0].style, ...style }
    return options
  }

  protected configLabel(chart: Chart, options: ViewSpec): ViewSpec {
    const tmpOptions = super.configLabel(chart, options)
    const { children } = tmpOptions
    if (children?.[0].labels?.length) {
      const { label: labelAttr } = parseJson(chart.customAttr) || {}
      const newLabel = {
        ...children[0].labels[0],
        textAlign: 'start',
        ...getHorizontalBarAxisSafeLabelStyle(chart, labelAttr.position)
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
          axis: {
            ...children[0].axis,
            y: xAxis
              ? { ...children[0].axis.y, ...xAxis, labelAutoHide: true, labelAutoRotate: false }
              : false
          }
        },
        ...children.slice(1)
      ]
    }
    const customStyle = parseJson(chart.customStyle)
    const xAxisAtt = JSON.parse(JSON.stringify(customStyle['xAxis']))
    if (!xAxisAtt.axisValue?.auto) {
      const child0 = tmpOptions.children[0]
      return {
        ...tmpOptions,
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
          ...tmpOptions.children.slice(1)
        ]
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
          axis: { ...children[0].axis, x: yAxis ? { ...children[0].axis.x, ...yAxis } : false }
        },
        ...children.slice(1)
      ]
    }
    return tmpOptions
  }

  protected configLengthLimitTooltip(chart: Chart, chartObj): void {
    configAxisLengthLimit(chart, chartObj, 'yAxis')
  }

  protected getAxisConfig(chart: Chart, axisType: string): any {
    const customStyle = parseJson(chart.customStyle)
    const axis = JSON.parse(JSON.stringify(customStyle[axisType]))
    if (customStyle[axisType] && axis.show) {
      // 轴线与刻度线统一使用公共规则
      const lineAndTick = {
        ...this.getAxisLineStyle(chart, axis),
        lineLineDash: getLineDash(axis.axisLine.lineStyle.style)
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
          const lengthLimit = axis.axisLabel.lengthLimit
          const valueText = value === null || value === undefined ? value : `${value}`
          if (axisType === 'yAxis' && lengthLimit && valueText?.length > lengthLimit) {
            return formatAxisLabelWithLengthLimit(valueText, lengthLimit)
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
        // 轴线与刻度线
        ...lineAndTick,
        // 网格线
        ...grid,
        // 刻度值
        ...label,
        labelAutoHide: true,
        labelAutoRotate: false,
        ...this.getAxisLabelStyle(axis)
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
    handleBarBreakLineNullData(chart, options)
    return options
  }

  protected configSlider(chart: Chart, options: ViewSpec): ViewSpec {
    const functionCfgItems = this.propertyInner?.['function-cfg']
    const hasSliderConfig = Array.isArray(functionCfgItems) && functionCfgItems.includes('slider')
    const { functionCfg } = parseJson(chart.senior)
    if (!hasSliderConfig || !functionCfg?.sliderShow) {
      return options
    }

    const lineMark = options.children[0]
    // 大数据标签载体必须和真实条形同步维度域，避免共享比例尺把拖动范围重新撑满
    configDimensionSlider(lineMark, lineMark.data ?? options.data, functionCfg, {
      interactionName: 'horizontalBarDimensionSliderFilter',
      syncChildren: true
    })
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
