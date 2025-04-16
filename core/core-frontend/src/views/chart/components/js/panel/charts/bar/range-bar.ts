import {
  G2PlotChartView,
  G2PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/g2plot'
import type { Bar, BarOptions } from '@antv/g2plot/esm/plots/bar'
import {
  configAxisLabelLengthLimit,
  configPlotTooltipEvent,
  configRoundAngle,
  getPadding,
  getTooltipContainer,
  setGradientColor,
  TOOLTIP_TPL
} from '@/views/chart/components/js/panel/common/common_antv'
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
import { DEFAULT_BASIC_STYLE } from '@/views/chart/components/editor/util/chart'
import { Group } from '@antv/g-canvas'

const { t } = useI18n()
const DEFAULT_DATA = []

/**
 * 区间条形图
 */
export class RangeBar extends G2PlotChartView<BarOptions, Bar> {
  axisConfig = {
    xAxis: {
      name: `${t('chart.drag_block_type_axis')} / ${t('chart.dimension')}`,
      type: 'd'
    },
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
  properties = BAR_RANGE_EDITOR_PROPERTY.filter(p => p !== 'threshold')
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
      'position',
      'showLengthLimit'
    ]
  }
  axis: AxisType[] = [...BAR_AXIS_TYPE, 'yAxisExt']
  protected baseOptions: BarOptions = {
    data: [],
    xField: 'values',
    yField: 'field',
    colorField: 'category',
    isGroup: true
  }

  async drawChart(drawOptions: G2PlotDrawOptions<Bar>): Promise<Bar> {
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

    const axis = chart.yAxis ?? chart.yAxisExt ?? []
    let dateFormat: string
    const dateSplit = axis[0]?.datePattern === 'date_split' ? '/' : '-'
    switch (axis[0]?.dateStyle) {
      case 'y':
        dateFormat = 'YYYY'
        break
      case 'y_M':
        dateFormat = 'YYYY' + dateSplit + 'MM'
        break
      case 'y_M_d':
        dateFormat = 'YYYY' + dateSplit + 'MM' + dateSplit + 'DD'
        break
      // case 'H_m_s':
      //   dateFormat = 'HH:mm:ss'
      //   break
      case 'y_M_d_H':
        dateFormat = 'YYYY' + dateSplit + 'MM' + dateSplit + 'DD' + ' HH'
        break
      case 'y_M_d_H_m':
        dateFormat = 'YYYY' + dateSplit + 'MM' + dateSplit + 'DD' + ' HH:mm'
        break
      case 'y_M_d_H_m_s':
        dateFormat = 'YYYY' + dateSplit + 'MM' + dateSplit + 'DD' + ' HH:mm:ss'
        break
      default:
        dateFormat = 'YYYY-MM-dd HH:mm:ss'
    }

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
              mask: dateFormat
            },
            tempId: {
              key: true
            }
          }
        : {
            values: {
              min: minNumber,
              max: maxNumber,
              mask: dateFormat
            },
            tempId: {
              key: true
            }
          }
    }

    const options = this.setupOptions(chart, initOptions)

    const { Bar: BarClass } = await import('@antv/g2plot/esm/plots/bar')
    // 开始渲染
    const newChart = new BarClass(container, options)

    newChart.on('interval:click', action)
    if (options.label) {
      newChart.on('label:click', e => {
        action({
          x: e.x,
          y: e.y,
          data: {
            data: e.target.attrs.data
          }
        })
      })
    }
    configPlotTooltipEvent(chart, newChart)
    configAxisLabelLengthLimit(chart, newChart)
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
            },
            container: getTooltipContainer(`tooltip-${chart.id}`),
            itemTpl: TOOLTIP_TPL,
            enterable: true
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
        const group = new Group({})
        group.addShape({
          type: 'text',
          attrs: {
            x: 0,
            y: 0,
            data: param,
            text: res,
            textAlign: 'start',
            textBaseline: 'top',
            fontSize: labelAttr.fontSize,
            fontFamily: chart.fontFamily,
            fill: labelAttr.color
          }
        })
        return group
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
