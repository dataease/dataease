import type { WaterfallOptions, Waterfall as G2Waterfall } from '@antv/g2plot/esm/plots/waterfall'
import { G2PlotChartView, G2PlotDrawOptions } from '../../types/impl/g2plot'
import { flow, hexColorToRGBA, parseJson } from '../../../util'
import { valueFormatter } from '../../../formatter'
import {
  configAxisLabelLengthLimit,
  configPlotTooltipEvent,
  configXAxisLengthLimit,
  getPadding,
  getTooltipContainer,
  getTooltipItemConditionColor,
  setGradientColor,
  TOOLTIP_TPL
} from '../../common/common_antv'
import { isEmpty } from 'lodash-es'
import { useI18n } from '@/hooks/web/useI18n'
import { DEFAULT_BASIC_STYLE } from '@/views/chart/components/editor/util/chart'
const { t } = useI18n()

function getWaterfallData(data: Record<string, any>[]): Record<string, any>[] {
  // 瀑布图数据处理，避免字符串数值参与累计时发生拼接
  return data.map(item => ({
    ...item,
    value: getWaterfallNumberValue(item.value)
  }))
}

function getWaterfallNumberValue(value: any): number | null {
  if (value === null || value === undefined) {
    return null
  }
  if (typeof value === 'string' && value.trim() === '') {
    return null
  }
  const numberValue = Number(value)
  return Number.isFinite(numberValue) ? numberValue : null
}

function limitAxisLabel(value: any, lengthLimit?: number): any {
  if (!lengthLimit || value === null || value === undefined) {
    return value
  }
  const text = String(value)
  // 轴标签格式化后再截断，避免覆盖样式面板长度限制
  return text.length > lengthLimit ? `${text.substring(0, lengthLimit)}...` : text
}

function getTotalDynamicTooltipValue(data: Record<string, any>[]): Record<string, any>[] {
  const dynamicTooltipValueMap = new Map<string, Record<string, any> & { hasValue: boolean }>()
  data.forEach(d => {
    d.dynamicTooltipValue?.forEach(item => {
      const key = `${item.fieldId}`
      const value = getWaterfallNumberValue(item.value)
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
 * 瀑布图
 */
export class Waterfall extends G2PlotChartView<WaterfallOptions, G2Waterfall> {
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
    'threshold',
    'jump-set',
    'linkage'
  ]
  propertyInner: EditorPropertyInner = {
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
  axisConfig = {
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
  async drawChart(drawOptions: G2PlotDrawOptions<G2Waterfall>): Promise<G2Waterfall> {
    const { chart, container, action } = drawOptions
    if (!chart.data?.data) {
      return
    }
    const data = getWaterfallData(chart.data.data)
    const baseOptions = {
      data,
      xField: 'field',
      yField: 'value',
      seriesField: 'category',
      appendPadding: getPadding(chart),
      meta: {
        field: {
          type: 'cat'
        }
      }
    }
    const options = this.setupOptions(chart, baseOptions)
    const { Waterfall: G2Waterfall } = await import('@antv/g2plot/esm/plots/waterfall')
    const newChart = new G2Waterfall(container, options)
    newChart.on('interval:click', action)
    configPlotTooltipEvent(chart, newChart)
    configXAxisLengthLimit(chart, newChart)
    configAxisLabelLengthLimit(chart, newChart)
    return newChart
  }

  protected configBasicStyle(chart: Chart, options: WaterfallOptions): WaterfallOptions {
    const customAttr = parseJson(chart.customAttr)
    const { colors, gradient, alpha } = customAttr.basicStyle
    const [risingColorRgba, fallingColorRgba, totalColorRgba] = colors

    let columnWidthRatio
    const _v = customAttr.basicStyle.columnWidthRatio ?? DEFAULT_BASIC_STYLE.columnWidthRatio
    if (_v >= 1 && _v <= 100) {
      columnWidthRatio = _v / 100.0
    } else if (_v < 1) {
      columnWidthRatio = 1 / 100.0
    } else if (_v > 100) {
      columnWidthRatio = 1
    }
    if (columnWidthRatio) {
      options.columnWidthRatio = columnWidthRatio
    }

    return {
      ...options,
      total: {
        label: t('chart.waterfall_total'),
        style: {
          fill: setGradientColor(hexColorToRGBA(totalColorRgba, alpha), gradient, 270)
        }
      },
      risingFill: setGradientColor(hexColorToRGBA(risingColorRgba, alpha), gradient, 270),
      fallingFill: setGradientColor(hexColorToRGBA(fallingColorRgba, alpha), gradient, 270)
    }
  }

  protected configXAxis(chart: Chart, options: WaterfallOptions): WaterfallOptions {
    const tmpOptions = super.configXAxis(chart, options)
    if (!tmpOptions.xAxis) {
      return tmpOptions
    }
    const xAxis = parseJson(chart.customStyle).xAxis
    if (tmpOptions.xAxis.label) {
      tmpOptions.xAxis.label.formatter = value => {
        return limitAxisLabel(value, xAxis.axisLabel.lengthLimit)
      }
    }
    return tmpOptions
  }

  protected configYAxis(chart: Chart, options: WaterfallOptions): WaterfallOptions {
    const tmpOptions = super.configYAxis(chart, options)
    if (!tmpOptions.yAxis) {
      return tmpOptions
    }
    const yAxis = parseJson(chart.customStyle).yAxis
    if (tmpOptions.yAxis.label) {
      tmpOptions.yAxis.label.formatter = value => {
        return limitAxisLabel(
          valueFormatter(value, yAxis.axisLabelFormatter),
          yAxis.axisLabel.lengthLimit
        )
      }
    }
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
    const totalMap = getTotalDynamicTooltipValue(options.data).reduce((pre, next) => {
      pre[next.fieldId] = next
      return pre
    }, {}) as Record<string, Record<string, any>>
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
            let tmpValue = totalMap[id]?.value
            let stringValue = totalMap[id]?.stringValue
            let color = 'grey'
            if (id === yAxis[0].id) {
              tmpValue = head.data.value
              stringValue = undefined
              color = head.color
            }
            const value =
              tmpValue !== null && tmpValue !== undefined
                ? valueFormatter(tmpValue, formatter.formatterCfg)
                : stringValue ?? ''
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
            const itemValue = getWaterfallNumberValue((item.value + '').replace(/,/g, ''))
            formatter.formatterCfg.type = 'value'
            const value =
              itemValue !== null ? valueFormatter(itemValue, formatter.formatterCfg) : ''
            const name = isEmpty(formatter.chartShowName) ? formatter.name : formatter.chartShowName
            result.push({ ...item, name, value })
          })
        head.data.dynamicTooltipValue?.forEach(item => {
          const formatter = formatterMap[item.fieldId]
          if (formatter) {
            const itemValue = getWaterfallNumberValue((item.value + '').replace(/,/g, ''))
            const value =
              itemValue !== null
                ? valueFormatter(itemValue, formatter.formatterCfg)
                : item.stringValue ?? ''
            const name = isEmpty(formatter.chartShowName) ? formatter.name : formatter.chartShowName
            result.push({ color: 'grey', name, value })
          }
        })
        result.forEach(item => {
          const color = getTooltipItemConditionColor(item)
          item.color = color
        })
        return result
      },
      container: getTooltipContainer(`tooltip-${chart.id}`, chart.container),
      itemTpl: TOOLTIP_TPL,
      enterable: true
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
            name: t('chart.increase'),
            value: '',
            marker: {
              style: {
                fill: setGradientColor(hexColorToRGBA(risingColorRgba, alpha), gradient, 270)
              }
            }
          },
          {
            name: t('chart.decrease'),
            value: '',
            marker: {
              style: {
                fill: setGradientColor(hexColorToRGBA(fallingColorRgba, alpha), gradient, 270)
              }
            }
          },
          {
            name: t('chart.waterfall_total'),
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
      this.addConditionsStyleColorToData,
      this.configTheme,
      this.configLegend,
      this.configBasicStyle,
      this.configLabel,
      this.configTooltip,
      this.configXAxis,
      this.configYAxis,
      this.configBarConditions
    )(chart, options)
  }

  constructor() {
    super('waterfall', [])
  }
}
