import { WaterfallOptions, Waterfall as G2Waterfall } from '@antv/g2plot/esm/plots/waterfall'
import { G2PlotChartView, G2PlotDrawOptions } from '../../types/impl/g2plot'
import { flow, hexColorToRGBA, parseJson } from '../../../util'
import { valueFormatter } from '../../../formatter'
import { getPadding, getTooltipSeriesTotalMap, setGradientColor } from '../../common/common_antv'
import { isEmpty } from 'lodash-es'

/**
 * 瀑布图
 */
export class Waterfall extends G2PlotChartView<WaterfallOptions, G2Waterfall> {
  properties: EditorProperty[] = [
    'background-overall-component',
    'basic-style-selector',
    'label-selector',
    'tooltip-selector',
    'title-selector',
    'legend-selector',
    'x-axis-selector',
    'y-axis-selector'
  ]
  propertyInner: EditorPropertyInner = {
    'background-overall-component': ['all'],
    'basic-style-selector': ['colors', 'alpha', 'gradient'],
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
      'axisLabelFormatter'
    ]
  }
  axis: AxisType[] = ['xAxis', 'yAxis', 'filter', 'drill', 'extLabel', 'extTooltip']
  public drawChart(drawOptions: G2PlotDrawOptions<G2Waterfall>): G2Waterfall {
    const { chart, container, action } = drawOptions
    if (!chart.data?.data) {
      return
    }
    const data = chart.data.data
    const baseOptions = {
      data,
      xField: 'field',
      yField: 'value',
      seriesField: 'category',
      appendPadding: getPadding(chart),
      interactions: [
        {
          type: 'tooltip',
          cfg: {
            start: [{ trigger: 'interval:mousemove', action: 'tooltip:show' }],
            end: [{ trigger: 'interval:mouseleave', action: 'tooltip:hide' }]
          }
        }
      ]
    }
    const options = this.setupOptions(chart, baseOptions)
    const newChart = new G2Waterfall(container, options)
    newChart.on('interval:click', action)
    return newChart
  }

  protected configMeta(chart: Chart, options: WaterfallOptions): WaterfallOptions {
    const yAxis = chart.yAxis
    const meta: WaterfallOptions['meta'] = {
      field: {
        type: 'cat'
      }
    }
    if (!yAxis?.length) {
      return {
        ...options,
        meta
      }
    }
    const f = yAxis[0]
    const yAxisStyle = parseJson(chart.customStyle).yAxis
    meta.value = {
      alias: f.name,
      formatter: (value: number) => {
        return valueFormatter(value, yAxisStyle.axisLabelFormatter)
      }
    }
    return {
      ...options,
      meta
    }
  }

  protected configBasicStyle(chart: Chart, options: WaterfallOptions): WaterfallOptions {
    const customAttr = parseJson(chart.customAttr)
    const { colors, gradient, alpha } = customAttr.basicStyle
    const [risingColorRgba, fallingColorRgba, totalColorRgba] = colors
    return {
      ...options,
      total: {
        label: '合计',
        style: {
          fill: setGradientColor(hexColorToRGBA(totalColorRgba, alpha), gradient, 270)
        }
      },
      risingFill: setGradientColor(hexColorToRGBA(risingColorRgba, alpha), gradient, 270),
      fallingFill: setGradientColor(hexColorToRGBA(fallingColorRgba, alpha), gradient, 270)
    }
  }

  protected configYAxis(chart: Chart, options: WaterfallOptions): WaterfallOptions {
    const tmpOptions = super.configYAxis(chart, options)
    if (!tmpOptions.yAxis) {
      return tmpOptions
    }
    const yAxis = parseJson(chart.customStyle).yAxis
    const axisValue = yAxis.axisValue
    if (!axisValue?.auto) {
      const axis = {
        yAxis: {
          ...tmpOptions.yAxis,
          min: axisValue.min,
          max: axisValue.max,
          minLimit: axisValue.min,
          maxLimit: axisValue.max,
          tickCount: axisValue.splitCount
        }
      }
      return { ...tmpOptions, ...axis }
    }
    return tmpOptions
  }

  protected configTooltip(chart: Chart, options: WaterfallOptions): WaterfallOptions {
    const customAttr: DeepPartial<ChartAttr> = parseJson(chart.customAttr)
    const tooltipAttr = customAttr.tooltip
    const yAxis = chart.yAxis
    if (!tooltipAttr.show) {
      return {
        ...options,
        tooltip: false
      }
    }
    const formatterMap = tooltipAttr.seriesTooltipFormatter
      ?.filter(i => i.show)
      .reduce((pre, next) => {
        pre[next.id] = next
        return pre
      }, {}) as Record<string, SeriesFormatter>
    const totalMap = getTooltipSeriesTotalMap(options.data)
    const tooltip: WaterfallOptions['tooltip'] = {
      showTitle: true,
      customItems(originalItems) {
        if (!tooltipAttr.seriesTooltipFormatter?.length) {
          return originalItems
        }
        const result = []
        const head = originalItems[0]
        // 汇总
        if (!head.data.quotaList) {
          Object.keys(formatterMap).forEach(id => {
            const formatter = formatterMap[id]
            let tmpValue = totalMap[id]
            let color = 'grey'
            if (id === yAxis[0].id) {
              tmpValue = head.data.value
              color = head.color
            }
            const value = valueFormatter(tmpValue, formatter.formatterCfg)
            const name = isEmpty(formatter.chartShowName) ? formatter.name : formatter.chartShowName
            if (id === yAxis[0].id) {
              result.unshift({ color, name, value })
              return
            }
            result.push({ color, name, value })
          })
          return result
        }
        originalItems
          .filter(item => formatterMap[item.data.quotaList[0].id])
          .forEach(item => {
            const formatter = formatterMap[item.data.quotaList[0].id]
            const value = valueFormatter(parseFloat(item.value as string), formatter.formatterCfg)
            const name = isEmpty(formatter.chartShowName) ? formatter.name : formatter.chartShowName
            result.push({ ...item, name, value })
          })
        head.data.dynamicTooltipValue?.forEach(item => {
          const formatter = formatterMap[item.fieldId]
          if (formatter) {
            const value = valueFormatter(parseFloat(item.value), formatter.formatterCfg)
            const name = isEmpty(formatter.chartShowName) ? formatter.name : formatter.chartShowName
            result.push({ color: 'grey', name, value })
          }
        })
        return result
      }
    }
    return {
      ...options,
      tooltip
    }
  }

  protected configLegend(chart: Chart, options: WaterfallOptions): WaterfallOptions {
    const tmp = super.configLegend(chart, options)
    if (!tmp.legend) {
      return tmp
    }
    const customAttr = parseJson(chart.customAttr)
    const { colors, gradient, alpha } = customAttr.basicStyle
    const [risingColorRgba, fallingColorRgba, totalColorRgba] = colors
    return {
      ...tmp,
      legend: {
        ...tmp.legend,
        items: [
          {
            name: '增加',
            value: '',
            marker: {
              style: {
                fill: setGradientColor(hexColorToRGBA(risingColorRgba, alpha), gradient, 270)
              }
            }
          },
          {
            name: '减少',
            value: '',
            marker: {
              style: {
                fill: setGradientColor(hexColorToRGBA(fallingColorRgba, alpha), gradient, 270)
              }
            }
          },
          {
            name: '合计',
            value: '',
            marker: {
              style: {
                fill: setGradientColor(hexColorToRGBA(totalColorRgba, alpha), gradient, 270)
              }
            }
          }
        ]
      }
    }
  }

  protected setupOptions(chart: Chart, options: WaterfallOptions): WaterfallOptions {
    return flow(
      this.configTheme,
      this.configLegend,
      this.configBasicStyle,
      this.configLabel,
      this.configTooltip,
      this.configXAxis,
      this.configYAxis,
      this.configMeta
    )(chart, options)
  }

  constructor() {
    super('waterfall', [])
  }
}
