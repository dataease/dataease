import { Chart as G2Column } from '@antv/g2'
import { G2DrawOptions } from '@/views/chart/components/js/panel/types/impl/g2'
import { useI18n } from '@/hooks/web/useI18n'
import { flow, hexColorToRGBA, parseJson } from '@/views/chart/components/js/util'
import { ViewSpec, configTooltip } from '@/views/chart/components/js/panel/charts/g2/bar/barUtil'
import { setGradientColor } from '@/views/chart/components/js/panel/common/common_antv'
import { Bar } from '@/views/chart/components/js/panel/charts/g2/bar/bar'
import { valueFormatter } from '@/views/chart/components/js/formatter'

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
    'x-axis-selector',
    'y-axis-selector',
    'threshold'
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
    'x-axis-selector': [
      'position',
      'name',
      'color',
      'fontSize',
      'axisLine',
      'splitLine',
      'axisForm',
      'axisLabel'
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
    data: []
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
    const newChart = new G2Column({ container, autoFit: true })
    newChart.options(options)
    newChart.on('interval:click', action)
    configTooltip(newChart, chart)
    return newChart
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
      y: { nice: true }
    }

    const style = {
      radius:
        basicStyle.radiusColumnBar === 'roundAngle' ? basicStyle.columnBarRightAngleRadius : 0,
      ...(basicStyle.radiusColumnBar === 'topRoundAngle' && {
        radiusTopLeft: basicStyle.columnBarRightAngleRadius,
        radiusTopRight: basicStyle.columnBarRightAngleRadius
      })
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
    if (!labelAttr || !labelAttr.show) return options

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
        if (d.conditionColor) {
          return d.conditionColor
        }
        if (d.isTotal) return colors[2]
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
      const value = d[yField] ? d[yField] : null
      newData.push({ ...d, [newYField]: [r, r + value] })
      r += value
    })
    if (newData.length && total) {
      const sum = newData[newData.length - 1][newYField][1]
      newData.push({
        ...data[0],
        [xField]: total.label,
        [yField]: sum,
        [newYField]: [0, sum]
      })
    }
    return newData
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
