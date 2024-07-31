import {
  G2PlotChartView,
  G2PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/g2plot'
import { cloneDeep, defaultTo, isEmpty, map } from 'lodash-es'
import {
  getPadding,
  getYAxis,
  getYAxisExt,
  setGradientColor
} from '@/views/chart/components/js/panel/common/common_antv'
import {
  BidirectionalBar as G2BidirectionalBar,
  BidirectionalBarOptions
} from '@antv/g2plot/esm/plots/bidirectional-bar'
import { flow, hexColorToRGBA, parseJson } from '@/views/chart/components/js/util'
import { useI18n } from '@/hooks/web/useI18n'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { Options } from '@antv/g2plot/esm'
const { t } = useI18n()
/**
 * 对称柱状图
 */

export class BidirectionalHorizontalBar extends G2PlotChartView<
  BidirectionalBarOptions,
  G2BidirectionalBar
> {
  axisConfig = {
    ...this['axisConfig'],
    xAxis: {
      name: `${t('chart.drag_block_type_axis')} / ${t('chart.dimension')}`,
      type: 'd',
      limit: 1
    },
    yAxis: {
      name: `${t('chart.drag_block_value_axis')} / ${t('chart.quota')}`,
      type: 'q',
      limit: 1
    },
    yAxisExt: {
      name: `${t('chart.drag_block_value_axis_ext')} / ${t('chart.quota')}`,
      type: 'q',
      limit: 1
    }
  }
  axis: AxisType[] = ['xAxis', 'yAxis', 'yAxisExt', 'filter', 'drill', 'extLabel', 'extTooltip']
  properties: EditorProperty[] = [
    'background-overall-component',
    'basic-style-selector',
    'x-axis-selector',
    'dual-y-axis-selector',
    'title-selector',
    'legend-selector',
    'label-selector',
    'tooltip-selector',
    'function-cfg',
    'jump-set',
    'linkage'
  ]
  propertyInner = {
    'background-overall-component': ['all'],
    'basic-style-selector': ['colors', 'alpha', 'gradient', 'layout'],
    'x-axis-selector': ['position', 'axisLabel', 'axisLine', 'splitLine'],
    'y-axis-selector': [
      'name',
      'position',
      'color',
      'fontSize',
      'axisLabel',
      'axisLine',
      'splitLine',
      'axisValue',
      'axisLabelFormatter'
    ],
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
    'label-selector': ['hPosition', 'vPosition', 'seriesLabelFormatter'],
    'tooltip-selector': ['fontSize', 'color', 'backgroundColor', 'seriesTooltipFormatter', 'show']
  }

  selectorSpec: EditorSelectorSpec = {
    ...this['selectorSpec'],
    'dual-y-axis-selector': {
      title: `${t('chart.xAxis')}`
    },
    'x-axis-selector': {
      title: `${t('chart.yAxis')}`
    }
  }

  drawChart(drawOptions: G2PlotDrawOptions<G2BidirectionalBar>): G2BidirectionalBar {
    const { chart, container, action } = drawOptions
    if (!chart.data?.data?.length) {
      return
    }
    // data
    const data = cloneDeep(chart.data.data)
    const data1 = defaultTo(data[0]?.data, [])
    const data2 = map(defaultTo(data[1]?.data, []), d => {
      return {
        ...d,
        category: d.field,
        value: data1.find(item => item.field === d.field)?.value,
        valueExt: d.value
      }
    })
    // options
    const initOptions: BidirectionalBarOptions = {
      xField: 'field',
      data: data2,
      xAxis: {
        label: {
          style: {}
        },
        position: 'bottom'
      },
      interactions: [{ type: 'active-region' }],
      yField: ['value', 'valueExt'],
      appendPadding: getPadding(chart),
      meta: {
        field: {
          type: 'cat'
        }
      }
    }
    const customOptions = this.setupOptions(chart, initOptions)
    const options = {
      ...customOptions
    }
    const xAxis = chart.xAxis
    if (xAxis?.length === 1 && xAxis[0].deType === 1) {
      const values = data2.map(item => item.field)
      options.meta = {
        field: {
          type: 'cat',
          values: values.reverse()
        }
      }
    }
    // 开始渲染
    const newChart = new G2BidirectionalBar(container, options)

    newChart.on('interval:click', action)
    newChart.on('element:click', ev => {
      const sourceData = newChart.options.data.filter(
        item =>
          item.field === ev.data.data.field &&
          item[ev.data.data['series-field-key']] === ev.data.data[ev.data.data['series-field-key']]
      )
      ev.data.data = {
        ...ev.data.data,
        ...sourceData[0]
      }
    })

    return newChart
  }

  protected configBasicStyle(
    chart: Chart,
    options: BidirectionalBarOptions
  ): BidirectionalBarOptions {
    const basicStyle = parseJson(chart.customAttr).basicStyle
    if (basicStyle.gradient) {
      const color = basicStyle.colors?.map((ele, index) => {
        const tmp = hexColorToRGBA(ele, basicStyle.alpha)
        let angle = 180 - index * 180
        // 垂直固定角度
        if (basicStyle.layout === 'vertical') {
          if (index === 0) {
            angle = 280
          }
          if (index === 1) {
            angle = 90
          }
        }
        return setGradientColor(tmp, true, angle)
      })
      options = {
        ...options,
        color
      }
    }
    options = {
      ...options,
      layout: basicStyle.layout
    }
    if (basicStyle.radiusColumnBar === 'roundAngle') {
      const barStyle = {
        radius: [
          basicStyle.columnBarRightAngleRadius,
          basicStyle.columnBarRightAngleRadius,
          basicStyle.columnBarRightAngleRadius,
          basicStyle.columnBarRightAngleRadius
        ]
      }
      options = {
        ...options,
        barStyle
      }
    }
    return options
  }

  protected configXAxis(chart: Chart, options: BidirectionalBarOptions): BidirectionalBarOptions {
    const tmpOptions = super.configXAxis(chart, options)
    if (!tmpOptions.xAxis) {
      return tmpOptions
    }
    if (tmpOptions.xAxis.label) {
      delete tmpOptions.xAxis.label.style.textAlign
    }
    return tmpOptions
  }

  protected configTooltip(chart: Chart, options: BidirectionalBarOptions): BidirectionalBarOptions {
    const customAttr: DeepPartial<ChartAttr> = parseJson(chart.customAttr)
    const tooltipAttr = customAttr.tooltip
    if (!tooltipAttr.show) {
      return {
        ...options,
        tooltip: false
      }
    }
    const yAxis = cloneDeep(chart.yAxis)
    const yAxisExt = cloneDeep(chart.yAxisExt)
    const formatterMap = tooltipAttr.seriesTooltipFormatter
      ?.filter(i => i.show)
      .reduce((pre, next) => {
        pre[next.seriesId] = next
        return pre
      }, {}) as Record<string, SeriesFormatter>
    const optionsData = cloneDeep(options.data)
    const yaxisObj = item => {
      const param = item.data
      let yaxis = yAxis[0]
      let axisType = 'yAxis'
      if (param['series-field-key'] === 'valueExt') {
        yaxis = yAxisExt[0]
        axisType = 'yAxisExt'
      }
      return {
        id: yaxis.id,
        name: yaxis.name,
        axisType: axisType,
        value: param[param['series-field-key']]
      }
    }
    const tooltip: BidirectionalBarOptions['tooltip'] = {
      shared: true,
      showTitle: true,
      customItems(originalItems) {
        if (!tooltipAttr.seriesTooltipFormatter?.length) {
          return originalItems
        }
        const result = []
        originalItems
          .filter(item => {
            const obj = yaxisObj(item)
            return formatterMap[obj.id + '-' + obj.axisType]
          })
          .forEach(item => {
            const obj = yaxisObj(item)
            const formatter = formatterMap[obj.id + '-' + obj.axisType]
            const value = valueFormatter(parseFloat(item.value as string), formatter.formatterCfg)
            const name = isEmpty(formatter.chartShowName) ? formatter.name : formatter.chartShowName
            result.push({ ...item, name, value })
          })
        const dynamicTooltipValue = optionsData.find(
          d => d.field === originalItems[0]['title']
        )?.dynamicTooltipValue
        if (dynamicTooltipValue.length > 0) {
          dynamicTooltipValue.forEach(dy => {
            const q = tooltipAttr.seriesTooltipFormatter.filter(i => i.id === dy.fieldId)
            if (q && q.length > 0) {
              const value = valueFormatter(parseFloat(dy.value as string), q[0].formatterCfg)
              const name = isEmpty(q[0].chartShowName) ? q[0].name : q[0].chartShowName
              result.push({ color: 'grey', name, value })
            }
          })
        }
        return result
      }
    }
    return {
      ...options,
      tooltip
    }
  }

  protected configLegend(chart: Chart, options: BidirectionalBarOptions): BidirectionalBarOptions {
    const o = super.configLegend(chart, options)
    if (o.legend) {
      o.legend.itemName = {
        formatter: (_text: string, _item: any, index: number) => {
          const yaxis = chart.yAxis[0]
          const yaxisExt = chart.yAxisExt[0]
          if (index === 0) {
            return yaxis.chartShowName ? yaxis.chartShowName : yaxis.name
          }
          return yaxisExt.chartShowName ? yaxisExt.chartShowName : yaxisExt.name
        }
      }
    }
    return o
  }

  protected configYAxis(chart: Chart, options: BidirectionalBarOptions): BidirectionalBarOptions {
    const yAxis = getYAxis(chart)
    let yAxisExt = getYAxisExt(chart)

    const tempOption = {
      ...options
    }

    if (!yAxis) {
      //左右轴都要隐藏
      yAxisExt = false
      tempOption['yAxis'] = {
        value: false,
        valueExt: false
      }
    } else {
      tempOption['yAxis'] = {
        value: undefined,
        valueExt: undefined
      }
    }
    const layoutHorizontal = options.layout === 'horizontal'
    // 处理横轴标题方向不对
    if (yAxis && yAxis['title'] && layoutHorizontal) {
      yAxis['title'].autoRotate = false
    }
    const yAxisTmp = parseJson(chart.customStyle).yAxis
    if (yAxis['label']) {
      yAxis['label'].formatter = value => {
        return valueFormatter(value, yAxisTmp.axisLabelFormatter)
      }
    }
    const axisValue = yAxisTmp.axisValue
    if (!axisValue?.auto) {
      tempOption.yAxis.value = {
        ...yAxis,
        min: axisValue.min,
        max: axisValue.max,
        minLimit: axisValue.min,
        maxLimit: axisValue.max,
        tickCount: axisValue.splitCount
      }
    } else {
      tempOption.yAxis.value = yAxis
    }

    const yAxisExtTmp = parseJson(chart.customStyle).yAxisExt
    if (yAxisExt['label']) {
      yAxisExt['label'].formatter = value => {
        return valueFormatter(value, yAxisExtTmp.axisLabelFormatter)
      }
    }
    const axisExtValue = yAxisExtTmp.axisValue
    if (!axisExtValue?.auto) {
      tempOption.yAxis.valueExt = {
        ...yAxisExt,
        min: axisExtValue.min,
        max: axisExtValue.max,
        minLimit: axisExtValue.min,
        maxLimit: axisExtValue.max,
        tickCount: axisExtValue.splitCount
      }
    } else {
      tempOption.yAxis.valueExt = yAxisExt
    }

    return tempOption
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    chart.customStyle.yAxis = {
      ...chart.customStyle.yAxis,
      position: 'left'
    }
    chart.customStyle.yAxisExt = {
      ...chart.customStyle.yAxisExt,
      position: 'left',
      splitLine: chart.customStyle.yAxis.splitLine
    }
    chart.customAttr.label = {
      ...chart.customAttr.label,
      position: 'right'
    }
    chart.customAttr.basicStyle.layout = 'horizontal'
    return chart
  }

  protected configLabel(chart: Chart, options: BidirectionalBarOptions): BidirectionalBarOptions {
    let label
    const yAxis = chart.yAxis
    const yAxisExt = chart.yAxisExt
    const labelAttr = parseJson(chart.customAttr).label
    const formatterMap = labelAttr.seriesLabelFormatter?.reduce((pre, next) => {
      pre[next.id] = next
      return pre
    }, {})
    let customAttr: DeepPartial<ChartAttr>
    const layoutHorizontal = options.layout === 'horizontal'
    if (chart.customAttr) {
      customAttr = parseJson(chart.customAttr)
      // label
      if (customAttr.label) {
        const l = customAttr.label
        if (l.show) {
          label = {
            position: l.position,
            layout: [{ type: 'limit-in-canvas' }],
            style: {
              fill: l.color,
              fontSize: l.fontSize
            },
            formatter: param => {
              let yaxis = yAxis[0]
              let res = param.value
              if (param['series-field-key'] === 'valueExt') {
                yaxis = yAxisExt[0]
              }
              const value = param[param['series-field-key']]
              const labelCfg = formatterMap?.[yaxis.id] as SeriesFormatter
              if (yaxis.formatterCfg) {
                res = valueFormatter(value, yaxis.formatterCfg)
              }
              if (!labelCfg) {
                return res
              }
              if (!labelCfg.show) {
                return
              }
              if (labelCfg) {
                res = valueFormatter(value, labelCfg.formatterCfg)
              } else {
                res = valueFormatter(value, l.labelFormatter)
              }
              const group = new G2PlotChartView.engine.Group({})
              const isValue = param['series-field-key'] === 'value'
              const textAlign = isValue && layoutHorizontal ? 'end' : 'start'
              const isMiddle = label.position === 'middle'
              group.addShape({
                type: 'text',
                attrs: {
                  x:
                    isValue && layoutHorizontal && !isMiddle
                      ? -6
                      : !isValue && layoutHorizontal && !isMiddle
                      ? 6
                      : 0,
                  y:
                    isValue && !layoutHorizontal && !isMiddle
                      ? -8
                      : !isValue && !layoutHorizontal && !isMiddle
                      ? 8
                      : 0,
                  text: res,
                  textAlign: label.position === 'middle' ? 'start' : textAlign,
                  textBaseline: 'top',
                  fontSize: labelCfg.fontSize,
                  fill: labelCfg.color
                }
              })
              return group
            }
          }
        } else {
          label = false
        }
      }
    }
    if (!layoutHorizontal) {
      if (label.position === 'left') {
        label.position = 'bottom'
      }
      if (label.position === 'right') {
        label.position = 'top'
      }
    }
    return { ...options, label }
  }

  protected configEmptyDataStrategy(
    chart: Chart,
    options: BidirectionalBarOptions
  ): BidirectionalBarOptions {
    const { data } = options as unknown as Options
    if (!data?.length) {
      return options
    }
    const strategy = parseJson(chart.senior).functionCfg.emptyDataStrategy
    if (strategy === 'ignoreData') {
      const emptyFields = data
        .filter(obj => obj['value'] === null || obj['valueExt'] === null)
        .map(obj => obj['field'])
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
    const updateValues = (strategy: 'breakLine' | 'setZero', data: any[]) => {
      data.forEach(obj => {
        if (obj['value'] === null) {
          obj['value'] = strategy === 'breakLine' ? null : 0
        }
        if (obj['valueExt'] === null) {
          obj['valueExt'] = strategy === 'breakLine' ? null : 0
        }
      })
    }
    if (strategy === 'breakLine' || strategy === 'setZero') {
      updateValues(strategy, data)
    }
    return options
  }

  protected setupOptions(chart: Chart, options: BidirectionalBarOptions) {
    return flow(
      this.configTheme,
      this.configBasicStyle,
      this.configLabel,
      this.configTooltip,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configAnalyse,
      this.configSlider,
      this.configEmptyDataStrategy
    )(chart, options)
  }

  constructor() {
    super('bidirectional-bar', [])
  }
}
