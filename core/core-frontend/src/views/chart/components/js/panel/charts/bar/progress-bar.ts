import { G2PlotChartView, G2PlotDrawOptions } from '../../types/impl/g2plot'
import { flow, hexColorToRGBA, parseJson } from '../../../util'
import {
  configAxisLabelLengthLimit,
  configPlotTooltipEvent,
  configRoundAngle,
  getTooltipContainer,
  setGradientColor,
  TOOLTIP_TPL
} from '../../common/common_antv'
import { useI18n } from '@/hooks/web/useI18n'
import type { Bar as G2Progress, BarOptions } from '@antv/g2plot/esm/plots/bar'
import {
  BAR_AXIS_TYPE,
  BAR_EDITOR_PROPERTY_INNER
} from '@/views/chart/components/js/panel/charts/bar/common'
import { cloneDeep, defaultTo } from 'lodash-es'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { Options } from '@antv/g2plot/esm'
import { DEFAULT_BASIC_STYLE } from '@/views/chart/components/editor/util/chart'

const { t } = useI18n()

export class ProgressBar extends G2PlotChartView<BarOptions, G2Progress> {
  axisConfig = {
    xAxis: {
      name: `${t('chart.form_type')} / ${t('chart.dimension')}`,
      type: 'd',
      limit: 1
    },
    yAxis: {
      name: `${t('chart.progress_target')} / ${t('chart.quota')}`,
      type: 'q',
      limit: 1
    },
    yAxisExt: {
      name: `${t('chart.progress_current')} / ${t('chart.quota')}`,
      type: 'q',
      limit: 1
    }
  }
  properties: EditorProperty[] = [
    'background-overall-component',
    'border-style',
    'basic-style-selector',
    'label-selector',
    'tooltip-selector',
    'y-axis-selector',
    'title-selector',
    'function-cfg',
    'jump-set',
    'linkage',
    'threshold'
  ]
  propertyInner = {
    ...BAR_EDITOR_PROPERTY_INNER,
    'legend-selector': null,
    'background-overall-component': ['all'],
    'border-style': ['all'],
    'basic-style-selector': ['colors', 'alpha', 'gradient', 'radiusColumnBar', 'columnWidthRatio'],
    'label-selector': ['hPosition', 'color', 'fontSize', 'showQuota', 'showProportion'],
    'tooltip-selector': ['fontSize', 'color', 'backgroundColor', 'tooltipFormatter', 'show'],
    'y-axis-selector': [
      'name',
      'color',
      'fontSize',
      'axisForm',
      'axisLabel',
      // 'position',
      'showLengthLimit'
    ],
    'function-cfg': ['emptyDataStrategy'],
    threshold: ['lineThreshold']
  }
  axis: AxisType[] = [...BAR_AXIS_TYPE, 'yAxisExt']
  protected baseOptions: BarOptions = {
    data: [],
    xField: 'progress',
    yField: 'title',
    seriesField: 'type',
    isGroup: false,
    isPercent: true,
    isStack: true,
    xAxis: false,
    appendPadding: [0, 0, 10, 0]
  }

  async drawChart(drawOptions: G2PlotDrawOptions<G2Progress>): Promise<G2Progress> {
    const { chart, container, action } = drawOptions
    if (!chart.data?.data?.length) {
      return
    }
    const getCompletionRate = (target: number, current: number) => {
      if (target === 0) {
        return 100
      }
      // 目标为正 当前为负
      if (target > 0 && current < 0) {
        return 0
      }
      // 目标为负 当前为正 正向
      if ((target < 0 && current > 0) || (target < 0 && current === 0)) {
        return (2 - current / target) * 100
      }
      // 目标与当前都为正
      if (target > 0 && current > 0) {
        return (current / target) * 100
      }
      // 目标与当前都为负 负向小于0为0
      if (target < 0 && current < 0) {
        const completionRate = (2 - current / target) * 100
        return Number(Math.max(completionRate, 0).toFixed(2))
      }
      return 0
    }
    // data
    const sourceData: Array<any> = cloneDeep(chart.data.data)
    const data1 = defaultTo(sourceData[0]?.data, [])
    const data2 = defaultTo(sourceData[1]?.data, [])
    const currentData = data2.map(item => {
      const progress = getCompletionRate(data1.find(i => i.field === item.field)?.value, item.value)
      return {
        ...item,
        type: 'current',
        title: item.field,
        id: item.quotaList[0].id,
        originalValue: item.value,
        originalProgress: progress,
        progress: progress >= 100 ? 100 : progress
      }
    })
    const targetData = data1.map(item => {
      const progress = 100 - currentData.find(i => i.title === item.field)?.progress
      return {
        ...item,
        type: 'target',
        title: item.field,
        id: item.quotaList[0].id,
        originalValue: item.value,
        progress: progress
      }
    })
    // options
    const initOptions: BarOptions = {
      ...this.baseOptions,
      data: currentData.concat(targetData).flat()
    }
    const options = this.setupOptions(chart, initOptions)

    const { Bar: G2Progress } = await import('@antv/g2plot/esm/plots/bar')
    // 开始渲染
    const newChart = new G2Progress(container, options)

    newChart.on('interval:click', action)
    configPlotTooltipEvent(chart, newChart)
    configAxisLabelLengthLimit(chart, newChart)
    return newChart
  }
  protected configBasicStyle(chart: Chart, options: BarOptions): BarOptions {
    const basicStyle = parseJson(chart.customAttr).basicStyle
    let color1 = basicStyle.colors?.map((ele, index) => {
      if (index === 1) {
        return hexColorToRGBA(ele, basicStyle.alpha > 10 ? 10 : basicStyle.alpha)
      } else {
        return hexColorToRGBA(ele, basicStyle.alpha)
      }
    })
    if (basicStyle.gradient) {
      // eslint-disable-next-line
      color1 = color1.map((ele, _index) => {
        return setGradientColor(ele, true, 0)
      })
    }
    options = {
      ...options,
      color: datum => {
        if (datum.type === 'target') {
          return 'rgba(0, 0, 0, 0)'
        }
        return color1[0]
      },
      barBackground: {
        style: {
          fill: color1[1]
        }
      }
    }
    options = {
      ...options,
      ...configRoundAngle(chart, 'barStyle')
    }

    let barWidthRatio
    const _v = basicStyle.columnWidthRatio ?? DEFAULT_BASIC_STYLE.columnWidthRatio
    if (_v >= 1 && _v <= 100) {
      barWidthRatio = _v / 100.0
    } else if (_v < 1) {
      barWidthRatio = 1 / 100.0
    } else if (_v > 100) {
      barWidthRatio = 1
    }
    if (barWidthRatio) {
      options.barWidthRatio = barWidthRatio
    }

    return options
  }
  protected configTooltip(chart: Chart, options: BarOptions): BarOptions {
    const tooltipAttr = parseJson(chart.customAttr).tooltip
    if (!tooltipAttr.show) {
      return {
        ...options,
        tooltip: {
          showContent: false
        }
      }
    }
    const yAxis = cloneDeep(chart.yAxis)[0]
    const yAxisExt = cloneDeep(chart.yAxisExt)[0]
    return {
      ...options,
      tooltip: {
        showContent: true,
        domStyles: {
          'g2-tooltip-marker': null
        },
        customItems(originalItems) {
          const result = []
          originalItems.forEach(item => {
            if (item.data) {
              const value = valueFormatter(item.data.value, tooltipAttr.tooltipFormatter)
              if (item.data.id === yAxis.id) {
                result.push({
                  ...item,
                  marker: false,
                  name: yAxis.chartShowName ? yAxis.chartShowName : yAxis.name,
                  value: value
                })
              }
              if (item.data.id === yAxisExt.id) {
                result.push({
                  ...item,
                  marker: false,
                  name: yAxisExt.chartShowName ? yAxisExt.chartShowName : yAxisExt.name,
                  value: value
                })
              }
            }
          })
          return result.length == 0 ? originalItems : result
        },
        container: getTooltipContainer(`tooltip-${chart.id}`),
        itemTpl: TOOLTIP_TPL,
        enterable: true
      }
    }
  }

  protected configLabel(chart: Chart, options: BarOptions): BarOptions {
    const baseOptions = super.configLabel(chart, options)
    if (!baseOptions.label) return baseOptions
    if (!baseOptions.label.layout?.[0]) {
      baseOptions.label.layout = [{ type: 'limit-in-canvas' }]
    }
    const { label: labelAttr } = parseJson(chart.customAttr)
    baseOptions.label.style.fill = labelAttr.color
    const label = {
      ...baseOptions.label,
      content: item => {
        if (item.type === 'target') return ''
        let text = ''
        if (labelAttr.showQuota) text += valueFormatter(item.value, labelAttr.quotaLabelFormatter)
        if (labelAttr.showProportion) {
          let proportion = item.originalProgress.toFixed(labelAttr.reserveDecimalCount) + '%'
          if (labelAttr.showQuota) {
            proportion = ` (${proportion}) `
          }
          text += proportion
        }
        return text
      }
    }
    if (label.position === 'top') label.position = 'right'
    return { ...baseOptions, label }
  }
  protected configYAxis(chart: Chart, options: BarOptions): BarOptions {
    const baseOption = super.configYAxis(chart, options)
    if (!baseOption.yAxis) {
      return baseOption
    }
    baseOption.yAxis.position = 'bottom'
    const yAxis = parseJson(chart.customStyle).yAxis
    if (yAxis.axisLabel.show) {
      const rotate = yAxis.axisLabel.rotate
      let textAlign = 'end'
      let textBaseline = 'middle'
      if (Math.abs(rotate) > 75) {
        textAlign = 'center'
      }
      if (rotate > 75) {
        textBaseline = 'top'
      }
      if (rotate < -75) {
        textBaseline = 'bottom'
      }
      baseOption.yAxis.label.style.textBaseline = textBaseline
      baseOption.yAxis.label.style.textAlign = textAlign
    }

    /*if (baseOption.yAxis.position === 'left') {
      baseOption.yAxis.position = 'bottom'
    }
    if (baseOption.yAxis.position === 'right') {
      baseOption.yAxis.position = 'top'
    }*/
    return baseOption
  }
  setupDefaultOptions(chart: ChartObj): ChartObj {
    chart.customStyle.yAxis = {
      ...chart.customStyle.yAxis,
      position: 'left',
      axisLine: {
        show: false,
        lineStyle: chart.customStyle.yAxis.axisLine.lineStyle
      },
      splitLine: {
        show: false,
        lineStyle: chart.customStyle.yAxis.axisLine.lineStyle
      }
    }
    chart.customStyle.legend.show = false
    chart.customAttr.label.show = true
    chart.customAttr.label.position = 'right'
    chart.customAttr.label.showQuota = false
    chart.customAttr.label.showProportion = true
    return chart
  }

  protected configLegend(chart: Chart, options: BarOptions): BarOptions {
    const o = super.configLegend(chart, options)
    return {
      ...o,
      legend: false
    }
  }

  protected configEmptyDataStrategy(chart: Chart, options: BarOptions): BarOptions {
    const { data } = options as unknown as Options
    if (!data?.length) {
      return options
    }
    const strategy = parseJson(chart.senior).functionCfg.emptyDataStrategy
    if (strategy === 'ignoreData') {
      const emptyFields = data.filter(obj => obj['value'] === null).map(obj => obj['field'])
      return {
        ...options,
        data: data.filter(obj => {
          if (emptyFields.includes(obj['field'])) {
            return false
          }
          return true
        })
      }
    }
    if (strategy === 'breakLine') {
      data.forEach(obj => {
        if (obj['value'] === null) {
          obj['value'] = null
        }
      })
    }
    if (strategy === 'setZero') {
      data.forEach(obj => {
        if (obj['value'] === null) {
          obj['value'] = 0
        }
      })
    }
    return options
  }

  protected setupOptions(chart: Chart, options: BarOptions): BarOptions {
    return flow(
      this.addConditionsStyleColorToData,
      this.configTheme,
      this.configBasicStyle,
      this.configLabel,
      this.configTooltip,
      this.configLegend,
      this.configYAxis,
      this.configEmptyDataStrategy,
      this.configBarConditions
    )(chart, options)
  }

  constructor() {
    super('progress-bar', [])
  }
}
