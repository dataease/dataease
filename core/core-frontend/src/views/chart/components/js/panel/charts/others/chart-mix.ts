import {
  G2PlotChartView,
  G2PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/g2plot'
import { DualAxes, DualAxesOptions } from '@antv/g2plot/esm/plots/dual-axes'
import {
  getAnalyse,
  getLabel,
  getPadding,
  getYAxis,
  getYAxisExt,
  setGradientColor
} from '../../common/common_antv'
import { flow, hexColorToRGBA, parseJson } from '@/views/chart/components/js/util'
import { cloneDeep, isEmpty, defaultTo, map, filter } from 'lodash-es'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import {
  CHART_MIX_AXIS_TYPE,
  CHART_MIX_EDITOR_PROPERTY,
  CHART_MIX_EDITOR_PROPERTY_INNER
} from './chart-mix-common'
import { Datum } from '@antv/g2plot/esm/types/common'
import { useI18n } from '@/hooks/web/useI18n'
import { DEFAULT_LABEL } from '@/views/chart/components/editor/util/chart'

const { t } = useI18n()
const DEFAULT_DATA = []

/**
 * 柱线混合图
 */
export class ColumnLineMix extends G2PlotChartView<DualAxesOptions, DualAxes> {
  properties = CHART_MIX_EDITOR_PROPERTY
  propertyInner = {
    ...CHART_MIX_EDITOR_PROPERTY_INNER,
    'label-selector': ['vPosition', 'seriesLabelFormatter'],
    'tooltip-selector': [
      ...CHART_MIX_EDITOR_PROPERTY_INNER['tooltip-selector'],
      'seriesTooltipFormatter'
    ]
  }
  axis: AxisType[] = [...CHART_MIX_AXIS_TYPE, 'yAxisExt']
  axisConfig = {
    ...this['axisConfig'],
    yAxis: {
      name: `${t('chart.drag_block_value_axis_left')} / ${t('chart.quota')}`,
      limit: 1,
      type: 'q'
    },
    yAxisExt: {
      name: `${t('chart.drag_block_value_axis_right')} / ${t('chart.quota')}`,
      limit: 1,
      type: 'q'
    }
  }
  drawChart(drawOptions: G2PlotDrawOptions<DualAxes>): DualAxes {
    const { chart, action, container } = drawOptions
    if (!chart.data.data?.length) {
      return
    }
    const data = cloneDeep(chart.data.data)

    const data1Type = data[0]?.type === 'bar' ? 'column' : data[0]?.type
    const data2Type = data[1]?.type === 'bar' ? 'column' : data[1]?.type

    const data1 = defaultTo(data[0]?.data, [])
    const data2 = map(defaultTo(data[1]?.data, []), d => {
      return {
        ...d,
        valueExt: d.value
      }
    })
    // custom color
    const customAttr = parseJson(chart.customAttr)
    let color = customAttr.basicStyle.colors

    color = color.map(ele => {
      const tmp = hexColorToRGBA(ele, customAttr.basicStyle.alpha)
      if (customAttr.basicStyle.gradient) {
        return setGradientColor(tmp, true, 270)
      } else {
        return tmp
      }
    })

    // options
    const initOptions: DualAxesOptions = {
      data: [data1, data2],
      xField: 'field',
      yField: ['value', 'valueExt'], //这里不能设置成一样的
      appendPadding: getPadding(chart),
      geometryOptions: [
        {
          geometry: data1Type,
          color: color[0]
        },
        {
          geometry: data2Type,
          color: color[1]
        }
      ],
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
          type: 'active-region',
          cfg: {
            start: [{ trigger: 'element:mousemove', action: 'active-region:show' }],
            end: [{ trigger: 'element:mouseleave', action: 'active-region:hide' }]
          }
        }
      ]
    }
    const options = this.setupOptions(chart, initOptions)

    // 开始渲染
    const newChart = new DualAxes(container, options)

    newChart.on('point:click', action)
    newChart.on('interval:click', action)

    return newChart
  }

  protected configLabel(chart: Chart, options: DualAxesOptions): DualAxesOptions {
    const tempLabel = getLabel(chart)
    const tmpOption = { ...options }
    if (!tempLabel) {
      if (tmpOption.geometryOptions) {
        tmpOption.geometryOptions[0].label = false
        tmpOption.geometryOptions[1].label = false
      }
      return tmpOption
    }

    const labelAttr = parseJson(chart.customAttr).label
    const formatterMap = labelAttr.seriesLabelFormatter?.reduce((pre, next) => {
      pre[next.id] = next
      return pre
    }, {})
    tempLabel.style.fill = DEFAULT_LABEL.color
    const label = {
      fields: [],
      ...tempLabel,
      offsetY: -8,
      formatter: (data: Datum) => {
        if (!labelAttr.seriesLabelFormatter?.length) {
          return data.value
        }
        const labelCfg = formatterMap?.[data.quotaList[0].id] as SeriesFormatter
        if (!labelCfg) {
          return data.value
        }
        if (!labelCfg.show) {
          return
        }
        const value = valueFormatter(data.value, labelCfg.formatterCfg)
        const group = new G2PlotChartView.engine.Group({})
        group.addShape({
          type: 'text',
          attrs: {
            x: 0,
            y: 0,
            text: value,
            textAlign: 'start',
            textBaseline: 'top',
            fontSize: labelCfg.fontSize,
            fill: labelCfg.color
          }
        })
        return group
      }
    }
    if (tmpOption.geometryOptions) {
      tmpOption.geometryOptions[0].label = label
      tmpOption.geometryOptions[1].label = label
    }
    return tmpOption
  }

  protected configBasicStyle(chart: Chart, options: DualAxesOptions): DualAxesOptions {
    // size
    const customAttr: DeepPartial<ChartAttr> = parseJson(chart.customAttr)
    const s = JSON.parse(JSON.stringify(customAttr.basicStyle))
    const smooth = s.lineSmooth
    const point = {
      size: s.lineSymbolSize,
      shape: s.lineSymbol
    }
    const lineStyle = {
      lineWidth: s.lineWidth
    }
    const tempOption = {
      ...options,
      smooth,
      point,
      lineStyle
    }
    if (tempOption.geometryOptions) {
      tempOption.geometryOptions[0].smooth = smooth
      tempOption.geometryOptions[0].point = point
      tempOption.geometryOptions[0].lineStyle = lineStyle

      tempOption.geometryOptions[1].smooth = smooth
      tempOption.geometryOptions[1].point = point
      tempOption.geometryOptions[1].lineStyle = lineStyle
    }
    return tempOption
  }

  protected configCustomColors(chart: Chart, options: DualAxesOptions): DualAxesOptions {
    const basicStyle = parseJson(chart.customAttr).basicStyle
    const color = basicStyle.colors.map(item => hexColorToRGBA(item, basicStyle.alpha))
    return {
      ...options,
      color
    }
  }

  protected configYAxis(chart: Chart, options: DualAxesOptions): DualAxesOptions {
    const yAxis = getYAxis(chart)
    const yAxisExt = getYAxisExt(chart)

    const tempOption = {
      ...options
    }

    tempOption.yAxis = {}
    if (!yAxis) {
      //左右轴都要隐藏
      tempOption.yAxis.value = false
    } else {
      tempOption.yAxis.value = undefined
      yAxis.position = 'left'

      const yAxisTmp = parseJson(chart.customStyle).yAxis
      if (yAxis.label) {
        yAxis.label.style.textAlign = 'end'
        yAxis.label.formatter = value => {
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
    }

    if (!yAxisExt) {
      //左右轴都要隐藏
      tempOption.yAxis.valueExt = false
    } else {
      tempOption.yAxis.valueExt = undefined
      yAxisExt.position = 'right'

      const yAxisExtTmp = parseJson(chart.customStyle).yAxisExt
      if (yAxisExt.label) {
        yAxisExt.label.style.textAlign = 'start'
        yAxisExt.label.formatter = value => {
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
    }

    return tempOption
  }

  protected configTooltip(chart: Chart, options: DualAxesOptions): DualAxesOptions {
    const customAttr: DeepPartial<ChartAttr> = parseJson(chart.customAttr)
    const tooltipAttr = customAttr.tooltip
    if (!tooltipAttr.show) {
      return {
        ...options,
        tooltip: false
      }
    }
    const xAxisExt = chart.xAxisExt
    const formatterMap = tooltipAttr.seriesTooltipFormatter
      ?.filter(i => i.show)
      .reduce((pre, next) => {
        pre[next.id] = next
        return pre
      }, {}) as Record<string, SeriesFormatter>
    const tooltip: DualAxesOptions['tooltip'] = {
      shared: true,
      showTitle: true,
      customItems(originalItems) {
        if (!tooltipAttr.seriesTooltipFormatter?.length) {
          return originalItems
        }
        const head = originalItems[0]
        // 非原始数据
        if (!head.data.quotaList) {
          return originalItems
        }
        const result = []
        originalItems
          .filter(item => formatterMap[item.data.quotaList[0].id])
          .forEach(item => {
            const formatter = formatterMap[item.data.quotaList[0].id]
            const value = valueFormatter(parseFloat(item.value as string), formatter.formatterCfg)
            let name = isEmpty(formatter.chartShowName) ? formatter.name : formatter.chartShowName
            if (xAxisExt?.length > 0) {
              name = item.data.category
            }
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

  protected configLegend(chart: Chart, options: DualAxesOptions): DualAxesOptions {
    const o = super.configLegend(chart, options)
    if (o.legend) {
      const data = chart.data.data
      o.legend.itemName = {
        formatter: (text: string, item: any, index: number) => {
          let name = undefined
          if (index === 0 && text === 'value') {
            name = data[0]?.name
          } else if (index === 1 && text === 'valueExt') {
            name = data[1]?.name
          }
          if (name === undefined) {
            return text
          } else {
            return name
          }
        }
      }
    }
    return o
  }

  protected configAnalyse(chart: Chart, options: DualAxesOptions): DualAxesOptions {
    const list = getAnalyse(chart)
    const annotations = {
      value: filter(list, l => l.yAxisType === 'left'),
      valueExt: filter(list, l => l.yAxisType === 'right')
    }
    return { ...options, annotations }
  }

  protected setupOptions(chart: Chart, options: DualAxesOptions): DualAxesOptions {
    return flow(
      this.configTheme,
      this.configLabel,
      this.configTooltip,
      this.configBasicStyle,
      this.configCustomColors,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configSlider,
      this.configAnalyse,
      this.configEmptyDataStrategy
    )(chart, options)
  }

  constructor(name = 'chart-mix') {
    super(name, DEFAULT_DATA)
  }
}
