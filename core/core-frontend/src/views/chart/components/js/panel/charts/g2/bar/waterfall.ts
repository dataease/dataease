import { Chart as G2Column } from '@antv/g2'
import { G2DrawOptions } from '@/views/chart/components/js/panel/types/impl/g2'
import { useI18n } from '@/hooks/web/useI18n'
import { flow, hexColorToRGBA, parseJson } from '@/views/chart/components/js/util'
import {
  listenerTooltipShow,
  handleEmptyDataStrategy,
  ViewSpec
} from '@/views/chart/components/js/panel/charts/g2/bar/barUtil'
import {
  configAxisLengthLimit,
  formatAxisLabelWithLengthLimit,
  getG2Renderer,
  handleChartDashboardHidden,
  setGradientColor
} from '@/views/chart/components/js/panel/common/common_antv'
import { Bar } from '@/views/chart/components/js/panel/charts/g2/bar/bar'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { DEFAULT_BASIC_STYLE } from '@/views/chart/components/editor/util/chart'

const { t } = useI18n()

/**
 * 瀑布图
 */
export class Waterfall extends Bar {
  properties: EditorProperty[] = [
    'background-overall-component',
    'border-style',
    'basic-style-selector',
    'label-selector',
    'tooltip-selector',
    'title-selector',
    'legend-selector',
    'function-cfg',
    'x-axis-selector',
    'y-axis-selector',
    'threshold',
    'jump-set',
    'linkage'
  ]
  propertyInner = {
    ...this['propertyInner'],
    'background-overall-component': ['all'],
    'border-style': ['all'],
    'basic-style-selector': ['colors', 'alpha', 'gradient', 'columnWidthRatio'],
    'label-selector': ['fontSize', 'color', 'vPosition', 'labelFormatter'],
    'tooltip-selector': ['fontSize', 'color', 'backgroundColor', 'seriesTooltipFormatter', 'show'],
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
    'legend-selector': ['icon', 'orient', 'fontSize', 'color', 'hPosition', 'vPosition'],
    'function-cfg': ['emptyDataStrategy'],
    'x-axis-selector': [
      'position',
      'name',
      'color',
      'fontSize',
      'axisLine',
      'splitLine',
      'axisForm',
      'axisLabel',
      'showLengthLimit'
    ],
    'y-axis-selector': [
      'position',
      'name',
      'color',
      'fontSize',
      'axisValue',
      'splitLine',
      'axisForm',
      'axisLabel',
      'axisLabelFormatter',
      'showLengthLimit',
      'axisLine'
    ],
    threshold: ['lineThreshold']
  }
  axis: AxisType[] = ['xAxis', 'yAxis', 'filter', 'drill', 'extLabel', 'extTooltip']
  axisConfig: AxisConfig = {
    xAxis: {
      name: `${t('chart.drag_block_type_axis')} / ${t('chart.dimension')}`,
      type: 'd'
    },
    yAxis: {
      name: `${t('chart.drag_block_value_axis')} / ${t('chart.quota')}`,
      type: 'q',
      limit: 1
    }
  }
  protected intervalOptions = {
    type: 'interval',
    encode: {
      x: 'field',
      y: 'value'
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
      },
      tooltip: {
        shared: true
      },
      legendFilter: false
    },
    data: [],
    tooltip: false
  } as ViewSpec

  async drawChart(drawOptions: G2DrawOptions<G2Column>): Promise<G2Column> {
    const { chart, container, action } = drawOptions
    if (!chart.data?.data) {
      return
    }
    const data = chart.data.data
    const intervalData = this.transformData(
      data,
      this.intervalOptions.encode.x,
      this.intervalOptions.encode.y,
      { label: t('chart.waterfall_total') }
    )
    // 处理连接线的数据
    const linkData = data => {
      return data.reduce((r, d, idx) => {
        if (idx > 0) {
          return r.concat({
            x1: data[idx - 1]?.field,
            x2: d.field,
            value: d.field === t('chart.waterfall_total') ? d.end : d.start
          })
        }
        return r
      }, [])
    }
    const initOptions: ViewSpec = {
      type: 'view',
      children: [
        {
          ...this.intervalOptions,
          encode: {
            x: 'field',
            y: ['start', 'end'],
            color: () => [t('chart.increase'), t('chart.decrease'), t('chart.waterfall_total')]
          },
          transform: [],
          data: intervalData
        },
        {
          type: 'link',
          tooltip: false,
          data: { value: intervalData, transform: [{ type: 'custom', callback: linkData }] },
          encode: {
            x: ['x1', 'x2'],
            y: 'value'
          },
          style: {
            stroke: '#8c8c8c',
            lineWidth: 1,
            lineDash: [4, 2]
          }
        }
      ]
    }
    const options = this.setupOptions(chart, initOptions)
    const newChart = new G2Column({ container, autoFit: true, ...getG2Renderer() })
    handleChartDashboardHidden(chart, options)
    newChart.options(options)
    newChart.on('interval:click', action)
    listenerTooltipShow(newChart, chart)
    this.configLengthLimitTooltip(chart, newChart)
    return newChart
  }

  protected configLengthLimitTooltip(chart: Chart, chartObj: G2Column): void {
    configAxisLengthLimit(chart, chartObj, 'xAxis')
    configAxisLengthLimit(chart, chartObj, 'yAxis')
  }

  protected getAxisConfig(chart: Chart, axisType: string): any {
    const axisConfig = super.getAxisConfig(chart, axisType)
    if (!axisConfig || axisType !== 'yAxis') {
      return axisConfig
    }
    const { yAxis } = parseJson(chart.customStyle)
    const originLabelFormatter = axisConfig.labelFormatter
    return {
      ...axisConfig,
      labelFormatter: value => {
        const label =
          typeof originLabelFormatter === 'function' ? originLabelFormatter(value) : value
        return formatAxisLabelWithLengthLimit(label, yAxis.axisLabel.lengthLimit)
      }
    }
  }

  protected supportAxisLengthLimit(axisType: string): boolean {
    return ['xAxis', 'yAxis'].includes(axisType)
  }

  protected configBasicStyle(chart: Chart, options: ViewSpec): ViewSpec {
    const { children } = options
    const customAttr = parseJson(chart.customAttr)
    const basicStyle = customAttr?.basicStyle || {}
    const colors = (basicStyle.colors || []).map(ele =>
      basicStyle.gradient
        ? setGradientColor(hexColorToRGBA(ele, basicStyle.alpha), true, 270)
        : hexColorToRGBA(ele, basicStyle.alpha)
    )

    const scale = {
      color: { range: colors },
      y: { nice: true },
      x: { paddingInner: -0.005 }
    }

    let style = {
      radius:
        basicStyle.radiusColumnBar === 'roundAngle' ? basicStyle.columnBarRightAngleRadius : 0,
      ...(basicStyle.radiusColumnBar === 'topRoundAngle' && {
        radiusTopLeft: basicStyle.columnBarRightAngleRadius,
        radiusTopRight: basicStyle.columnBarRightAngleRadius
      })
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
      } as any
    }

    return {
      ...options,
      children: [
        {
          ...children[0],
          scale,
          style: {
            ...style,
            fill: d => {
              if (d.isTotal) return colors[2]
              return d.difference > 0 ? colors[0] : colors[1]
            }
          }
        },
        ...children.slice(1)
      ]
    }
  }

  protected configLabel(chart: Chart, options: ViewSpec): ViewSpec {
    const customAttr = parseJson(chart.customAttr)
    const { label: labelAttr } = customAttr
    if (!labelAttr.show) return options

    const { children } = options
    const position = {
      position: labelAttr.position === 'middle' ? 'inside' : labelAttr.position,
      textAlign: 'center',
      dy: labelAttr.position === 'top' ? -10 : 0,
      dx: 0
    }
    const transform = labelAttr.fullDisplay
      ? {}
      : { transform: [{ type: 'exceedAdjust' }, { type: 'overlapHide' }] }

    const label = {
      text: 'value',
      fillOpacity: 1,
      pointerEvents: 'none',
      fill: labelAttr.color,
      fontSize: labelAttr.fontSize,
      ...position,
      formatter: (value, _data) => valueFormatter(value, labelAttr.labelFormatter),
      ...transform
    }
    return {
      ...options,
      children: [
        {
          ...children[0],
          labels: [label]
        },
        ...children.slice(1)
      ]
    }
  }

  protected configEmptyDataStrategy(chart: Chart, options: ViewSpec): ViewSpec {
    handleEmptyDataStrategy(chart, options)
    return options
  }

  protected configBarConditions(chart: Chart, options: ViewSpec): ViewSpec {
    const { threshold } = parseJson(chart.senior)
    if (!threshold.enable) return options
    const colors = options.children[0].scale.color.range
    const overThreshold = data => {
      data.forEach(item => {
        item['conditionColor'] = []
        const quotaList = item.quotaList.map(q => q.id) ?? []
        quotaList.forEach(q => {
          const cColor = this.getColorByConditions([].concat(q), item['value'], chart)
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
        if (d.isTotal) return colors[2]
        if (d.conditionColor?.[0]) {
          return d.conditionColor[0]
        }
        return d.difference > 0 ? colors[0] : colors[1]
      }
    }
    return options
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

  /**
   * 统一将数据处理成[start, end]
   * @param data
   * @param xField
   * @param yField
   * @param newYField
   * @param total
   * @protected
   */
  protected processData(
    data: Record<string, any>[],
    xField: string,
    yField: string,
    newYField: string,
    total?: false | { label?: string }
  ) {
    const newData = [] as Record<string, any>[]
    let r = 0
    data.forEach(d => {
      const value = this.getNumberValue(d[yField])
      const current = value ?? 0
      newData.push({ ...d, [yField]: value, [newYField]: [r, r + current] })
      r += current
    })
    if (newData.length && total) {
      const sum = newData[newData.length - 1][newYField][1]
      newData.push({
        ...data[0],
        dynamicTooltipValue: this.getTotalDynamicTooltipValue(data),
        [xField]: total.label,
        [yField]: sum,
        [newYField]: [0, sum]
      })
    }
    return newData
  }

  protected getNumberValue(value: any): number | null {
    if (value === null || value === undefined) {
      return null
    }
    if (typeof value === 'string' && value.trim() === '') {
      return null
    }
    const numberValue = Number(value)
    return Number.isFinite(numberValue) ? numberValue : null
  }

  protected getTotalDynamicTooltipValue(data: Record<string, any>[]): Record<string, any>[] {
    const dynamicTooltipValueMap = new Map<string, Record<string, any> & { hasValue: boolean }>()
    data.forEach(d => {
      d.dynamicTooltipValue?.forEach(item => {
        const key = `${item.fieldId}`
        const value = this.getNumberValue(item.value)
        const current = dynamicTooltipValueMap.get(key) || {
          ...item,
          value: null,
          hasValue: false
        }
        if (value !== null) {
          current.value = (current.hasValue ? current.value : 0) + value
          current.hasValue = true
        }
        dynamicTooltipValueMap.set(key, current)
      })
    })
    return Array.from(dynamicTooltipValueMap.values()).map(({ hasValue, ...item }) => ({
      ...item,
      value: hasValue ? item.value : null
    }))
  }

  /**
   * 处理为 瀑布图 数据
   * @param data
   * @param xField
   * @param yField
   * @param total
   * @protected
   */
  protected transformData(
    data: Record<string, any>[],
    xField: string,
    yField: string,
    total?: { label?: string }
  ) {
    const newYField = 'bounds'
    const processed = this.processData(data, xField, yField, newYField, total)
    return processed.map((d, dIdx) => {
      return {
        ...d,
        ['start']: d[newYField][0],
        ['end']: d[newYField][1],
        ['difference']: d[newYField][1] - d[newYField][0],
        ['isTotal']: dIdx === data.length
      }
    })
  }

  protected setupOptions(chart: Chart, options: ViewSpec): ViewSpec {
    return flow(
      this.configTheme,
      this.configEmptyDataStrategy,
      this.configBasicStyle,
      this.configLabel,
      this.configTooltip,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configBarConditions
    )(chart, options, {}, this)
  }

  constructor(name = 'waterfall') {
    super(name, [])
  }
}
