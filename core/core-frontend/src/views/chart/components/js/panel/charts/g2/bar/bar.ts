import { Chart as G2Column } from '@antv/g2'
import { G2ChartView, G2DrawOptions } from '@/views/chart/components/js/panel/types/impl/g2'
import {
  BAR_AXIS_TYPE,
  BAR_EDITOR_PROPERTY,
  BAR_EDITOR_PROPERTY_INNER
} from '@/views/chart/components/js/panel/charts/g2/bar/common'
import { useI18n } from '@/hooks/web/useI18n'
import { flow, hexColorToRGBA, hexToRgba, parseJson } from '@/views/chart/components/js/util'
import { cloneDeep, isEmpty } from 'lodash-es'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import {
  getLineDash,
  setGradientColor,
  TOOLTIP_ITEM_TPL,
  TOOLTIP_TITLE_TPL
} from '@/views/chart/components/js/panel/common/common_antv'
import {
  DEFAULT_YAXIS_EXT_STYLE,
  DEFAULT_YAXIS_STYLE
} from '@/views/chart/components/editor/util/chart'
import { filter, find } from 'lodash-es'
import {
  configTooltip,
  createTooltipWrapper,
  tooltipCss,
  Transform,
  ViewSpec
} from '@/views/chart/components/js/panel/charts/g2/bar/barUtil'

const { t } = useI18n()
const DEFAULT_DATA: any[] = []

/**
 * 柱状图
 */
export class Bar extends G2ChartView<ViewSpec, G2Column> {
  properties = BAR_EDITOR_PROPERTY
  propertyInner = {
    ...BAR_EDITOR_PROPERTY_INNER,
    'basic-style-selector': [...BAR_EDITOR_PROPERTY_INNER['basic-style-selector'], 'seriesColor'],
    'label-selector': ['vPosition', 'seriesLabelFormatter', 'showExtremum'],
    'tooltip-selector': [
      'fontSize',
      'color',
      'backgroundColor',
      'seriesTooltipFormatter',
      'show',
      'carousel'
    ],
    'y-axis-selector': [...BAR_EDITOR_PROPERTY_INNER['y-axis-selector'], 'axisLabelFormatter']
  }
  axis: AxisType[] = [...BAR_AXIS_TYPE]
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
  protected intervalOptions = {
    type: 'interval',
    encode: {
      x: 'field',
      y: 'value',
      color: 'category'
    },
    axis: {
      x: {
        title: false
      },
      y: {
        title: false
      }
    },
    interaction: {
      elementHighlight: {
        background: true
      }
    },
    transform: [{ type: 'dodgeX' } as Transform],
    data: []
  } as ViewSpec

  async drawChart(drawOptions: G2DrawOptions<G2Column>): Promise<G2Column> {
    const { chart, container, action } = drawOptions
    chart.container = container
    if (!chart?.data?.data?.length) {
      return
    }
    const data = cloneDeep(drawOptions.chart.data?.data)
    const initOptions: ViewSpec = {
      type: 'view',
      children: [
        {
          ...this.intervalOptions,
          transform: [].concat(this.intervalOptions.transform),
          data
        }
      ]
    }
    const options: ViewSpec = this.setupOptions(chart, initOptions)
    const newChart = new G2Column({ container, autoFit: true })
    newChart.options(options)
    newChart.on('interval:click', action)
    configTooltip(newChart, chart)
    return newChart
  }

  protected configLabel(chart: Chart, options: ViewSpec): ViewSpec {
    const customAttr = parseJson(chart.customAttr)
    const { label: l } = customAttr
    // 如果没有启用标签，直接返回
    if (!l || !l.show) {
      return options
    }
    const { children } = options
    const { label: labelAttr } = parseJson(chart.customAttr)
    const formatterMap = labelAttr.seriesLabelFormatter?.reduce((pre, next) => {
      pre[next.id] = next
      return pre
    }, {})
    const position = {
      position: l.position === 'middle' ? 'inside' : l.position,
      textAlign: 'center',
      dy: l.position === 'top' ? -10 : 0,
      dx: 0
    }
    const transform = {
      transform: [{ type: 'exceedAdjust' }, { type: 'overlapHide' }]
    }
    // 配置标签样式
    const newLabel = {
      text: 'value',
      fillOpacity: 1,
      fill: data => {
        const labelCfg = formatterMap?.[data.quotaList[0].id] as SeriesFormatter
        if (!labelCfg) {
          return l.color
        }
        return labelCfg.color
      },
      fontSize: data => {
        const labelCfg = formatterMap?.[data.quotaList[0].id] as SeriesFormatter
        if (!labelCfg) {
          return l.fontSize
        }
        return labelCfg.fontSize
      },
      ...position,
      formatter: (value, data) => {
        if (data.EXTREME) {
          return ''
        }
        if (!labelAttr.seriesLabelFormatter?.length) {
          return data.value
        }
        const labelCfg = formatterMap?.[data.quotaList[0].id] as SeriesFormatter
        if (!labelCfg) {
          return data.value
        }
        if (!labelCfg.show) {
          return ''
        }
        return valueFormatter(value, labelCfg.formatterCfg)
      },
      ...(l.fullDisplay ? { transform: [{ type: 'exceedAdjust' }] } : transform)
    }
    return {
      ...options,
      children: [
        {
          ...children[0],
          labels: [newLabel]
        },
        ...children.slice(1)
      ]
    }
  }

  protected configTooltip(chart: Chart, options: ViewSpec): ViewSpec {
    const customAttr: DeepPartial<ChartAttr> = parseJson(chart.customAttr)
    const tooltipAttr = customAttr.tooltip
    const yAxis = chart.yAxis
    if (!tooltipAttr.show) {
      options.children[0].tooltip = false
      return options
    }
    const formatterMap = tooltipAttr.seriesTooltipFormatter
      ?.filter(i => i.show)
      .reduce((pre, next) => {
        pre[next.id] = next
        return pre
      }, {}) as Record<string, SeriesFormatter>
    const tooltipOptions: ViewSpec = {
      tooltip: d => d,
      interaction: {
        tooltip: {
          mount: createTooltipWrapper(chart),
          css: tooltipCss(tooltipAttr),
          enterable: true,
          shared: true,
          bounding: {
            x: 0,
            y: 0
          },
          position: 'top-right',
          render: (_, { title, items: originalItems }) => {
            const titleHtml = TOOLTIP_TITLE_TPL.replace('{title}', title)
            let tooltipItems = originalItems
            if (tooltipAttr.seriesTooltipFormatter?.length) {
              tooltipItems = originalItems.filter(item => formatterMap[item.quotaList[0].id])
            }
            const result = []
            const head = originalItems[0]
            tooltipItems.forEach(item => {
              const formatter = formatterMap[item.quotaList[0].id] ?? yAxis[0]
              const value = valueFormatter(item.value, formatter.formatterCfg)
              const name = isEmpty(formatter.chartShowName)
                ? formatter.name
                : formatter.chartShowName
              result.push({ ...item, name, value })
            })
            head.dynamicTooltipValue?.forEach(item => {
              const formatter = formatterMap[item.fieldId]
              if (formatter) {
                const value = valueFormatter(parseFloat(item.value), formatter.formatterCfg)
                const name = isEmpty(formatter.chartShowName)
                  ? formatter.name
                  : formatter.chartShowName
                result.push({ color: 'grey', name, value })
              }
            })
            const itemsHtml = result
              .map(item => {
                const marker = item.color
                const label = item.name
                const value = item.value
                return TOOLTIP_ITEM_TPL.replace('{marker}', marker)
                  .replace('{label}', label)
                  .replace('{value}', value)
              })
              .join('')
            const listHtml = `<ul class="g2-tooltip-list" style="margin: 0px; list-style-type: none; padding: 0px;">${itemsHtml}</ul>`
            return `${titleHtml}${listHtml}`
          }
        }
      }
    }
    return {
      ...options,
      children: [
        {
          ...options.children[0],
          ...tooltipOptions
        },
        ...options.children.slice(1)
      ]
    }
  }

  protected configBasicStyle(chart: Chart, options: ViewSpec): ViewSpec {
    const { children } = options
    const customAttr = parseJson(chart.customAttr)
    const colors: string[] = []
    if (customAttr.basicStyle) {
      const basicStyle = customAttr.basicStyle
      basicStyle.colors.forEach(ele => {
        let color = hexColorToRGBA(ele, basicStyle.alpha)
        if (basicStyle.gradient) {
          color = setGradientColor(color, true, 270)
        }
        colors.push(color ? color : hexColorToRGBA(ele, basicStyle.alpha))
      })
    }
    const scale = {
      color: {
        range: colors
      },
      y: {
        nice: true
      }
    }
    const basicStyle = parseJson(chart.customAttr).basicStyle
    const { radiusColumnBar, columnBarRightAngleRadius } = basicStyle
    let style
    if (radiusColumnBar === 'topRoundAngle') {
      style = {
        radiusTopLeft: columnBarRightAngleRadius,
        radiusTopRight: columnBarRightAngleRadius
      }
    } else if (radiusColumnBar === 'roundAngle') {
      style = {
        radius: columnBarRightAngleRadius
      }
    } else {
      style = {
        radius: 0
      }
    }
    return {
      ...options,
      children: [
        {
          ...children[0],
          scale,
          style
        },
        ...children.slice(1)
      ]
    }
  }

  protected configLegend(chart: Chart, options: ViewSpec): ViewSpec {
    const { children } = options
    return {
      ...options,
      children: [
        {
          ...children[0],
          legend: this.getLegend(chart)
        },
        ...children.slice(1)
      ]
    }
  }

  protected getLegend = (chart: Chart) => {
    let legend = {}
    let customStyle: CustomStyle
    if (chart.customStyle) {
      customStyle = parseJson(chart.customStyle)
      // legend
      if (customStyle.legend) {
        const l = JSON.parse(JSON.stringify(customStyle.legend))
        if (l.show) {
          let position
          const orient = l.orient
          const legendSymbol = l.icon
          const legendSize = l.size
          const legendFontSize = l.fontSize
          const legendColor = l.color
          // position 图例布局
          // layoutJustifyContent 图例实例布局
          let layoutJustifyContent = 'center'
          // 根据图例方向和位置设置布局和位置
          if (orient === 'horizontal') {
            // 水平布局
            position = l.vPosition === 'center' ? 'bottom' : l.vPosition
            layoutJustifyContent =
              l.hPosition === 'left' && l.vPosition !== 'center'
                ? 'flex-start'
                : l.hPosition === 'right' && l.vPosition !== 'center'
                ? 'flex-end'
                : 'center'
          } else {
            // 垂直布局
            position = l.hPosition === 'center' ? 'left' : l.hPosition
            layoutJustifyContent =
              l.vPosition === 'top' && l.hPosition !== 'center'
                ? 'flex-start'
                : l.vPosition === 'bottom' && l.hPosition !== 'center'
                ? 'flex-end'
                : 'center'
          }
          legend = {
            color: {
              orientation: orient,
              position,
              layout: {
                justifyContent: layoutJustifyContent
              },
              itemMarker: legendSymbol,
              itemMarkerSize: legendSize,
              itemLabelFontSize: legendFontSize,
              itemLabelFill: legendColor,
              navPageNumFontSize: legendSize,
              navPageNumFill: legendColor,
              navButtonSize: legendSize,
              navOrientation:
                position === 'left' || position === 'right' ? 'vertical' : 'horizontal',
              maxRows: 1,
              navControllerSpacing: 20
            }
          }
        } else {
          legend = false
        }
      }
    }
    return legend
  }

  protected configXAxis(chart: Chart, options: ViewSpec): ViewSpec {
    const { children } = options
    const xAxis = this.getAxisConfig(chart, 'xAxis')
    return {
      ...options,
      children: [
        {
          ...children[0],
          axis: { ...children[0].axis, x: xAxis }
        },
        ...children.slice(1)
      ]
    }
  }

  protected configYAxis(chart: Chart, options: ViewSpec): ViewSpec {
    const { children } = options
    const yAxis = this.getAxisConfig(chart, 'yAxis')
    const tmpOptions = {
      ...options,
      children: [
        {
          ...children[0],
          axis: { ...children[0].axis, y: yAxis }
        },
        ...children.slice(1)
      ]
    }
    const customStyle = parseJson(chart.customStyle)
    const yAxisAtt = JSON.parse(JSON.stringify(customStyle['yAxis']))
    if (!yAxisAtt.axisValue?.auto) {
      return {
        ...tmpOptions,
        scale: {
          ...options.scale,
          y: {
            nice: true,
            clamp: true,
            domain: [yAxisAtt.axisValue.min, yAxisAtt.axisValue.max],
            tickCount: yAxisAtt.axisValue.splitCount
          }
        }
      }
    }
    return tmpOptions
  }

  protected configAnalyse(chart: Chart, options: ViewSpec): ViewSpec {
    const { children } = options
    return {
      ...options,
      children: [...children, ...this.getAssistLineStyle(chart)]
    }
  }
  protected getAssistLineStyle = (chart: Chart) => {
    const assistLine = []
    const senior = parseJson(chart.senior)
    if (!senior.assistLineCfg?.enable) {
      return assistLine
    }
    const assistLineArr = senior.assistLineCfg.assistLine
    if (assistLineArr?.length > 0) {
      const customStyle = parseJson(chart.customStyle)
      let axisFormatterCfg, axisExtFormatterCfg
      if (customStyle.yAxis) {
        const a = JSON.parse(JSON.stringify(customStyle.yAxis))
        axisFormatterCfg = a.axisLabelFormatter
          ? a.axisLabelFormatter
          : DEFAULT_YAXIS_STYLE.axisLabelFormatter
      }
      if (customStyle.yAxisExt) {
        const a = JSON.parse(JSON.stringify(customStyle.yAxisExt))
        axisExtFormatterCfg = a.axisLabelFormatter
          ? a.axisLabelFormatter
          : DEFAULT_YAXIS_EXT_STYLE.axisLabelFormatter
      }
      const fixedLines = assistLineArr.filter(ele => ele.field === '0')
      const dynamicLineFields = assistLineArr
        .filter(ele => ele.field === '1')
        .map(item => item.fieldId)
      const quotaFields = filter(chart.yAxis, ele => ele.summary !== '' && ele.id !== '-1')
      const quotaExtFields = filter(chart.yAxisExt, ele => ele.summary !== '' && ele.id !== '-1')
      const dynamicLines = chart.data.dynamicAssistLines?.filter(item => {
        return (
          dynamicLineFields?.includes(item.fieldId) &&
          (!!find(quotaFields, d => d.id === item.fieldId) ||
            (!!find(quotaExtFields, d => d.id === item.fieldId) &&
              chart.type.includes('chart-mix')))
        )
      })
      const lines = fixedLines.concat(dynamicLines || [])
      lines.forEach(item => {
        const value = parseFloat(item.value)
        const content =
          item.name +
          ' : ' +
          valueFormatter(value, item.yAxisType === 'left' ? axisFormatterCfg : axisExtFormatterCfg)
        assistLine.push({
          type: 'lineY',
          data: [value],
          style: {
            stroke: item.color,
            lineDash: getLineDash(item.lineType)
          },
          labels: [
            {
              text: content,
              position: 'left',
              textBaseline: 'bottom',
              fill: item.color,
              background: false,
              fontSize: item.fontSize ? item.fontSize : 10
            }
          ]
        })
      })
    }
    return assistLine
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
          if (axisType === 'yAxis') {
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

  protected configTheme(chart: Chart, options: ViewSpec): ViewSpec {
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

  protected configBarConditions(chart: Chart, options: ViewSpec): ViewSpec {
    const { threshold } = parseJson(chart.senior)
    if (!threshold.enable) return options
    const overThreshold = data => {
      data.forEach(item => {
        item['conditionColor'] = []
        const quotaList = item.quotaList.map(q => q.id) ?? []
        quotaList.forEach(q => {
          let currentValue = item['value']
          if (chart.type === 'progress-bar') {
            currentValue = item['progress']
          }
          const cColor = this.getColorByConditions([].concat(q), currentValue, chart)
          if (cColor) {
            item.conditionColor.push(cColor)
          } else {
            item.conditionColor = undefined
          }
        })
      })
      return data
    }
    options.children[0].data = {
      value: options.children[0].data,
      transform: [
        {
          type: 'custom',
          callback: data => overThreshold(data)
        }
      ]
    }
    options.children[0].style = {
      ...options.children[0].style,
      fill: d => {
        if (d.conditionColor) {
          return d.conditionColor
        }
      }
    }
    return options
  }

  protected getColorByConditions = (quotaList: any[], values: number | number[], chart) => {
    const { threshold } = parseJson(chart.senior)
    const { basicStyle } = parseJson(chart.customAttr)
    const currentValue = Array.isArray(values) ? values[1] - values[0] : values
    if (!currentValue) return undefined
    // 同样的指标只取最后一个
    const conditionMap = new Map()
    for (const condition of threshold.lineThreshold ?? []) {
      conditionMap.set(condition.fieldId, condition)
    }
    for (const condition of conditionMap.values()) {
      if (chart.type === 'progress-bar' && chart.yAxisExt?.[0]?.id !== quotaList?.[0]) continue
      if (!quotaList.includes(condition.fieldId) && chart.type !== 'waterfall') continue
      for (const tc of condition.conditions) {
        if (
          (tc.term === 'between' && currentValue >= tc.min && currentValue <= tc.max) ||
          (tc.term === 'lt' && currentValue < tc.value) ||
          (tc.term === 'le' && currentValue <= tc.value) ||
          (tc.term === 'gt' && currentValue > tc.value) ||
          (tc.term === 'ge' && currentValue >= tc.value)
        ) {
          let tmpColor = hexToRgba(tc.color, basicStyle.alpha)
          if (basicStyle.gradient) {
            const vhAngle = ['bar-horizontal', 'progress-bar'].includes(chart.type) ? 0 : 270
            tmpColor = setGradientColor(tmpColor, true, vhAngle)
          }
          return tmpColor
        }
      }
    }
  }

  protected setupOptions(chart: Chart, options: ViewSpec): ViewSpec {
    return flow(
      this.configTheme,
      this.configBasicStyle,
      this.configLabel,
      this.configTooltip,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configAnalyse,
      this.configBarConditions
    )(chart, options, {}, this)
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    chart.senior.functionCfg.emptyDataStrategy = 'ignoreData'
    return chart
  }

  constructor(name = 'bar', defaultData = DEFAULT_DATA) {
    super(name, defaultData)
  }
}
