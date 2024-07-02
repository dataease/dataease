import {
  G2PlotChartView,
  G2PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/g2plot'
import { Bar, BarOptions } from '@antv/g2plot/esm/plots/bar'
import { getPadding, setGradientColor } from '@/views/chart/components/js/panel/common/common_antv'
import { cloneDeep, find } from 'lodash-es'
import { flow, hexColorToRGBA, parseJson } from '@/views/chart/components/js/util'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import {
  BAR_AXIS_TYPE,
  BAR_RANGE_EDITOR_PROPERTY,
  BAR_EDITOR_PROPERTY_INNER
} from '@/views/chart/components/js/panel/charts/bar/common'
import { Datum } from '@antv/g2plot/esm/types/common'
import { useI18n } from '@/hooks/web/useI18n'

const { t } = useI18n()
const DEFAULT_DATA = []

/**
 * 区间条形图
 */
export class RangeBar extends G2PlotChartView<BarOptions, Bar> {
  axisConfig = {
    ...this['axisConfig'],
    yAxis: {
      name: `${t('chart.drag_block_value_start')} / ${t('chart.time_dimension_or_quota')}`,
      limit: 1,
      type: 'q'
    },
    yAxisExt: {
      name: `${t('chart.drag_block_value_end')} / ${t('chart.time_dimension_or_quota')}`,
      limit: 1,
      type: 'q'
    }
  }
  properties = BAR_RANGE_EDITOR_PROPERTY
  propertyInner = {
    ...BAR_EDITOR_PROPERTY_INNER,
    'label-selector': ['hPosition', 'color', 'fontSize', 'labelFormatter', 'showGap'],
    'tooltip-selector': [
      'fontSize',
      'color',
      'backgroundColor',
      'tooltipFormatter',
      'showGap',
      'show'
    ],
    'x-axis-selector': [...BAR_EDITOR_PROPERTY_INNER['x-axis-selector'], 'axisLabelFormatter'],
    'y-axis-selector': [
      'name',
      'color',
      'fontSize',
      'axisLine',
      'splitLine',
      'axisForm',
      'axisLabel',
      'position'
    ]
  }
  axis: AxisType[] = [...BAR_AXIS_TYPE, 'yAxisExt']
  protected baseOptions: BarOptions = {
    data: [],
    xField: 'values',
    yField: 'field',
    colorField: 'category',
    isGroup: true,
    interactions: [
      {
        type: 'legend-active',
        cfg: {
          start: [{ trigger: 'legend-item:mouseenter', action: ['element-active:reset'] }],
          end: [{ trigger: 'legend-item:mouseleave', action: ['element-active:reset'] }]
        }
      },
      {
        type: 'legend-filter',
        cfg: {
          start: [
            {
              trigger: 'legend-item:click',
              action: [
                'list-unchecked:toggle',
                'data-filter:filter',
                'element-active:reset',
                'element-highlight:reset'
              ]
            }
          ]
        }
      },
      {
        type: 'tooltip',
        cfg: {
          start: [{ trigger: 'interval:mousemove', action: 'tooltip:show' }],
          end: [{ trigger: 'interval:mouseleave', action: 'tooltip:hide' }]
        }
      },
      {
        type: 'active-region',
        cfg: {
          start: [{ trigger: 'interval:mousemove', action: 'active-region:show' }],
          end: [{ trigger: 'interval:mouseleave', action: 'active-region:hide' }]
        }
      }
    ]
  }

  drawChart(drawOptions: G2PlotDrawOptions<Bar>): Bar {
    const { chart, container, action } = drawOptions
    if (!chart.data?.data?.length) {
      return
    }
    // data
    const data: Array<any> = cloneDeep(chart.data.data)

    data.forEach(d => {
      d.tempId = (Math.random() * 10000000).toString()
    })

    const ifAggregate = !!chart.aggregate

    const isDate = !!chart.data.isDate

    const minTime = chart.data.minTime
    const maxTime = chart.data.maxTime

    const minNumber = chart.data.min
    const maxNumber = chart.data.max

    // options
    const initOptions: BarOptions = {
      ...this.baseOptions,
      appendPadding: getPadding(chart),
      data,
      seriesField: isDate ? (ifAggregate ? 'category' : undefined) : 'category',
      isGroup: isDate ? !ifAggregate : false,
      isStack: isDate ? !ifAggregate : false,
      meta: isDate
        ? {
            values: {
              type: 'time',
              min: minTime,
              max: maxTime,
              mask: 'YYYY-MM-DD HH:mm:ss'
            },
            tempId: {
              key: true
            }
          }
        : {
            values: {
              min: minNumber,
              max: maxNumber,
              mask: 'YYYY-MM-DD HH:mm:ss'
            },
            tempId: {
              key: true
            }
          }
    }

    const options = this.setupOptions(chart, initOptions)

    // 开始渲染
    const newChart = new Bar(container, options)

    newChart.on('interval:click', action)

    return newChart
  }

  protected configXAxis(chart: Chart, options: BarOptions): BarOptions {
    const tmpOptions = super.configXAxis(chart, options)
    if (!tmpOptions.xAxis) {
      return tmpOptions
    }
    const xAxis = parseJson(chart.customStyle).xAxis
    const axisValue = xAxis.axisValue
    const isDate = !!chart.data.isDate
    if (tmpOptions.xAxis.label) {
      tmpOptions.xAxis.label.formatter = value => {
        if (isDate) {
          return value
        }
        return valueFormatter(value, xAxis.axisLabelFormatter)
      }
    }
    if (tmpOptions.xAxis.position === 'top') {
      tmpOptions.xAxis.position = 'left'
    }
    if (tmpOptions.xAxis.position === 'bottom') {
      tmpOptions.xAxis.position = 'right'
    }
    if (!axisValue?.auto) {
      const axis = {
        xAxis: {
          ...tmpOptions.xAxis,
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

  protected configTooltip(chart: Chart, options: BarOptions): BarOptions {
    const isDate = !!chart.data.isDate
    let tooltip
    let customAttr: DeepPartial<ChartAttr>
    if (chart.customAttr) {
      customAttr = parseJson(chart.customAttr)
      // tooltip
      if (customAttr.tooltip) {
        const t = JSON.parse(JSON.stringify(customAttr.tooltip))
        if (t.show) {
          tooltip = {
            fields: ['values', 'field', 'gap'],
            formatter: function (param: Datum) {
              let res
              if (isDate) {
                res = param.values[0] + ' ~ ' + param.values[1]
                if (t.showGap) {
                  res = res + ' (' + param.gap + ')'
                }
              } else {
                res =
                  valueFormatter(param.values[0], t.tooltipFormatter) +
                  ' ~ ' +
                  valueFormatter(param.values[1], t.tooltipFormatter)
                if (t.showGap) {
                  res = res + ' (' + valueFormatter(param.gap, t.tooltipFormatter) + ')'
                }
              }
              return { value: res, values: param.values, name: param.field }
            }
          }
        } else {
          tooltip = false
        }
      }
    }
    return { ...options, tooltip }
  }

  protected configBasicStyle(chart: Chart, options: BarOptions): BarOptions {
    const isDate = !!chart.data.isDate
    const ifAggregate = !!chart.aggregate
    const basicStyle = parseJson(chart.customAttr).basicStyle

    if (isDate && !ifAggregate) {
      const customColors = []
      const groups = []
      for (let i = 0; i < chart.data.data.length; i++) {
        const name = chart.data.data[i].field
        if (groups.indexOf(name) < 0) {
          groups.push(name)
        }
      }
      for (let i = 0; i < groups.length; i++) {
        const s = groups[i]
        customColors.push({
          name: s,
          color: basicStyle.colors[i % basicStyle.colors.length],
          isCustom: false
        })
      }
      const color = obj => {
        const colorObj = find(customColors, o => {
          return o.name === obj.field
        })
        if (colorObj === undefined) {
          return undefined
        }
        const color = hexColorToRGBA(colorObj.color, basicStyle.alpha)
        if (basicStyle.gradient) {
          return setGradientColor(color, true)
        } else {
          return color
        }
      }

      options = {
        ...options,
        color
      }
    } else {
      if (basicStyle.gradient) {
        let color = basicStyle.colors
        color = color.map(ele => {
          const tmp = hexColorToRGBA(ele, basicStyle.alpha)
          return setGradientColor(tmp, true)
        })
        options = {
          ...options,
          color
        }
      }
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

  setupDefaultOptions(chart: ChartObj): ChartObj {
    const { customAttr, senior } = chart
    const { label } = customAttr
    if (!['left', 'middle', 'right'].includes(label.position)) {
      label.position = 'middle'
    }
    senior.functionCfg.emptyDataStrategy = 'ignoreData'
    return chart
  }

  protected configLabel(chart: Chart, options: BarOptions): BarOptions {
    const isDate = !!chart.data.isDate
    const ifAggregate = !!chart.aggregate

    const tmpOptions = super.configLabel(chart, options)
    if (!tmpOptions.label) {
      return {
        ...tmpOptions,
        label: false
      }
    }
    const labelAttr = parseJson(chart.customAttr).label

    if (isDate && !ifAggregate) {
      if (!tmpOptions.label.layout) {
        tmpOptions.label.layout = []
      }
      tmpOptions.label.layout.push({ type: 'interval-hide-overlap' })
      tmpOptions.label.layout.push({ type: 'limit-in-plot', cfg: { action: 'hide' } })
    }

    const label = {
      fields: [],
      ...tmpOptions.label,
      formatter: (param: Datum) => {
        let res
        if (isDate) {
          if (labelAttr.showGap) {
            res = param.gap
          } else {
            res = param.values[0] + ' ~ ' + param.values[1]
          }
        } else {
          if (labelAttr.showGap) {
            res = valueFormatter(param.gap, labelAttr.labelFormatter)
          } else {
            res =
              valueFormatter(param.values[0], labelAttr.labelFormatter) +
              ' ~ ' +
              valueFormatter(param.values[1], labelAttr.labelFormatter)
          }
        }
        return res
      }
    }
    return {
      ...tmpOptions,
      label
    }
  }

  protected configYAxis(chart: Chart, options: BarOptions): BarOptions {
    const tmpOptions = super.configYAxis(chart, options)
    if (!tmpOptions.yAxis) {
      return tmpOptions
    }
    if (tmpOptions.yAxis.position === 'left') {
      tmpOptions.yAxis.position = 'bottom'
    }
    if (tmpOptions.yAxis.position === 'right') {
      tmpOptions.yAxis.position = 'top'
    }
    return tmpOptions
  }

  protected setupOptions(chart: Chart, options: BarOptions): BarOptions {
    return flow(
      this.configTheme,
      this.configBasicStyle,
      this.configLabel,
      this.configTooltip,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configSlider,
      this.configEmptyDataStrategy
    )(chart, options)
  }

  constructor(name = 'bar-range') {
    super(name, DEFAULT_DATA)
  }
}
