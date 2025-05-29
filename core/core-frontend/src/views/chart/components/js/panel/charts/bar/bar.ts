import type { Column, ColumnOptions } from '@antv/g2plot/esm/plots/column'
import { cloneDeep, each, groupBy, isEmpty } from 'lodash-es'
import {
  G2PlotChartView,
  G2PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/g2plot'
import {
  flow,
  hexColorToRGBA,
  parseJson,
  setUpGroupSeriesColor,
  setUpStackSeriesColor
} from '@/views/chart/components/js/util'
import type { Datum } from '@antv/g2plot'
import { formatterItem, valueFormatter } from '@/views/chart/components/js/formatter'
import {
  BAR_AXIS_TYPE,
  BAR_EDITOR_PROPERTY,
  BAR_EDITOR_PROPERTY_INNER
} from '@/views/chart/components/js/panel/charts/bar/common'
import {
  configPlotTooltipEvent,
  configRoundAngle,
  getLabel,
  getPadding,
  getTooltipContainer,
  setGradientColor,
  TOOLTIP_TPL
} from '@/views/chart/components/js/panel/common/common_antv'
import { useI18n } from '@/hooks/web/useI18n'
import { DEFAULT_BASIC_STYLE, DEFAULT_LABEL } from '@/views/chart/components/editor/util/chart'
import { clearExtremum, extremumEvt } from '@/views/chart/components/js/extremumUitl'
import { Group } from '@antv/g-canvas'

const { t } = useI18n()
const DEFAULT_DATA: any[] = []
/**
 * 柱状图
 */
export class Bar extends G2PlotChartView<ColumnOptions, Column> {
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
  protected baseOptions: ColumnOptions = {
    xField: 'field',
    yField: 'value',
    seriesField: 'category',
    isGroup: true,
    data: []
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

  async drawChart(drawOptions: G2PlotDrawOptions<Column>): Promise<Column> {
    const { chart, container, action } = drawOptions
    chart.container = container
    if (!chart?.data?.data?.length) {
      clearExtremum(chart)
      return
    }
    const isGroup = 'bar-group' === this.name && chart.xAxisExt?.length > 0
    const isStack =
      ['bar-stack', 'bar-group-stack'].includes(this.name) && chart.extStack?.length > 0
    const data = cloneDeep(drawOptions.chart.data?.data)
    const initOptions: ColumnOptions = {
      ...this.baseOptions,
      appendPadding: getPadding(chart),
      data
    }
    const options: ColumnOptions = this.setupOptions(chart, initOptions)
    let newChart = null
    const { Column: ColumnClass } = await import('@antv/g2plot/esm/plots/column')
    newChart = new ColumnClass(container, options)
    newChart.on('interval:click', action)
    extremumEvt(newChart, chart, options, container)
    configPlotTooltipEvent(chart, newChart)
    return newChart
  }

  protected configLabel(chart: Chart, options: ColumnOptions): ColumnOptions {
    const tmpOptions = super.configLabel(chart, options)
    if (!tmpOptions.label) {
      return {
        ...tmpOptions,
        label: false
      }
    }
    const { label: labelAttr } = parseJson(chart.customAttr)
    const formatterMap = labelAttr.seriesLabelFormatter?.reduce((pre, next) => {
      pre[next.id] = next
      return pre
    }, {})
    // 默认是灰色
    tmpOptions.label.style.fill = DEFAULT_LABEL.color
    const label = {
      fields: [],
      ...tmpOptions.label,
      formatter: (data: Datum) => {
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
          return
        }
        const value = valueFormatter(data.value, labelCfg.formatterCfg)
        const group = new Group({})
        group.addShape({
          type: 'text',
          attrs: {
            x: 0,
            y: 0,
            text: value,
            textAlign: 'start',
            textBaseline: 'top',
            fontSize: labelCfg.fontSize,
            fontFamily: chart.fontFamily,
            fill: labelCfg.color
          }
        })
        return group
      },
      position: data => {
        if (data.value < 0) {
          if (tmpOptions.label?.position === 'top') {
            return 'bottom'
          }
          if (tmpOptions.label?.position === 'bottom') {
            return 'top'
          }
        }
        return tmpOptions.label?.position
      }
    }
    return {
      ...tmpOptions,
      label
    }
  }

  protected configTooltip(chart: Chart, options: ColumnOptions): ColumnOptions {
    return super.configMultiSeriesTooltip(chart, options)
  }

  protected configBasicStyle(chart: Chart, options: ColumnOptions): ColumnOptions {
    const basicStyle = parseJson(chart.customAttr).basicStyle
    if (basicStyle.gradient) {
      let color = basicStyle.colors
      color = color.map(ele => {
        const tmp = hexColorToRGBA(ele, basicStyle.alpha)
        return setGradientColor(tmp, true, 270)
      })
      options = {
        ...options,
        color
      }
    }
    options = {
      ...options,
      ...configRoundAngle(chart, 'columnStyle')
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
      options.columnWidthRatio = columnWidthRatio
    }

    return options
  }

  protected configYAxis(chart: Chart, options: ColumnOptions): ColumnOptions {
    const tmpOptions = super.configYAxis(chart, options)
    if (!tmpOptions.yAxis) {
      return tmpOptions
    }
    const yAxis = parseJson(chart.customStyle).yAxis
    const axisValue = yAxis.axisValue
    if (tmpOptions.yAxis.label) {
      tmpOptions.yAxis.label.formatter = value => {
        return valueFormatter(value, yAxis.axisLabelFormatter)
      }
    }
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
      // 根据axis的最小值，过滤options中的data数据，过滤掉小于最小值的数据
      const { data } = options
      const newData = data.filter(item => item.value >= axisValue.min)
      return { ...tmpOptions, data: newData, ...axis }
    }
    return tmpOptions
  }

  protected setupOptions(chart: Chart, options: ColumnOptions): ColumnOptions {
    return flow(
      this.addConditionsStyleColorToData,
      this.configTheme,
      this.configEmptyDataStrategy,
      this.configColor,
      this.configBasicStyle,
      this.configLabel,
      this.configTooltip,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configSlider,
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

/**
 * 堆叠柱状图
 */
export class StackBar extends Bar {
  properties = BAR_EDITOR_PROPERTY.filter(ele => ele !== 'threshold')
  propertyInner = {
    ...this['propertyInner'],
    'label-selector': [
      ...BAR_EDITOR_PROPERTY_INNER['label-selector'],
      'vPosition',
      'showTotal',
      'totalColor',
      'totalFontSize',
      'totalFormatter',
      'showStackQuota'
    ],
    'tooltip-selector': [
      'fontSize',
      'color',
      'backgroundColor',
      'tooltipFormatter',
      'show',
      'carousel'
    ]
  }
  protected configLabel(chart: Chart, options: ColumnOptions): ColumnOptions {
    let label = getLabel(chart)
    if (!label) {
      return options
    }
    options = { ...options, label }
    const { label: labelAttr } = parseJson(chart.customAttr)
    if (labelAttr.showStackQuota || labelAttr.showStackQuota === undefined) {
      label.style.fill = labelAttr.color
      label = {
        ...label,
        formatter: function (param: Datum) {
          return valueFormatter(param.value, labelAttr.labelFormatter)
        }
      }
    } else {
      label = false
    }
    if (labelAttr.showTotal) {
      const formatterCfg = labelAttr.labelFormatter ?? formatterItem
      each(groupBy(options.data, 'field'), (values, key) => {
        const total = values.reduce((a, b) => a + b.value, 0)
        const value = valueFormatter(total, formatterCfg)
        if (!options.annotations) {
          options = {
            ...options,
            annotations: []
          }
        }
        options.annotations.push({
          type: 'text',
          position: [key, total],
          content: `${value}`,
          style: {
            textAlign: 'center',
            fontSize: labelAttr.fontSize,
            fill: labelAttr.color
          },
          offsetY: -(parseInt(labelAttr.fontSize as unknown as string) / 2)
        })
      })
    }
    return {
      ...options,
      label
    }
  }

  protected configTooltip(chart: Chart, options: ColumnOptions): ColumnOptions {
    const tooltipAttr = parseJson(chart.customAttr).tooltip
    if (!tooltipAttr.show) {
      return {
        ...options,
        tooltip: false
      }
    }
    const tooltip = {
      formatter: (param: Datum) => {
        const name = isEmpty(param.category) ? param.field : param.category
        const obj = { name, value: param.value }
        const res = valueFormatter(param.value, tooltipAttr.tooltipFormatter)
        obj.value = res ?? ''
        return obj
      },
      container: getTooltipContainer(`tooltip-${chart.id}`),
      itemTpl: TOOLTIP_TPL,
      enterable: true
    }
    return {
      ...options,
      tooltip
    }
  }

  protected configColor(chart: Chart, options: ColumnOptions): ColumnOptions {
    return this.configStackColor(chart, options)
  }

  protected configData(chart: Chart, options: ColumnOptions): ColumnOptions {
    const { xAxis, extStack, yAxis } = chart
    const mainSort = xAxis.some(axis => axis.sort !== 'none')
    const subSort = extStack.some(axis => axis.sort !== 'none')
    if (mainSort || subSort) {
      return options
    }
    const quotaSort = yAxis?.[0].sort !== 'none'
    if (!quotaSort || !extStack.length || !yAxis.length) {
      return options
    }
    const { data } = options
    const mainAxisValueMap = data.reduce((p, n) => {
      p[n.field] = p[n.field] ? p[n.field] + n.value : n.value || 0
      return p
    }, {})
    const sort = yAxis[0].sort
    data.sort((p, n) => {
      if (sort === 'asc') {
        return mainAxisValueMap[p.field] - mainAxisValueMap[n.field]
      } else {
        return mainAxisValueMap[n.field] - mainAxisValueMap[p.field]
      }
    })
    return options
  }

  public setupSeriesColor(chart: ChartObj, data?: any[]): ChartBasicStyle['seriesColor'] {
    return setUpStackSeriesColor(chart, data)
  }

  protected setupOptions(chart: Chart, options: ColumnOptions): ColumnOptions {
    return flow(
      this.configTheme,
      this.configEmptyDataStrategy,
      this.configData,
      this.configColor,
      this.configBasicStyle,
      this.configLabel,
      this.configTooltip,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configSlider,
      this.configAnalyse
    )(chart, options, {}, this)
  }

  constructor(name = 'bar-stack') {
    super(name)
    this.baseOptions = {
      ...this.baseOptions,
      isStack: true,
      isGroup: false,
      meta: {
        category: {
          type: 'cat'
        }
      }
    }
    this.axis = [...this.axis, 'extStack']
  }
}

/**
 * 分组柱状图
 */
export class GroupBar extends StackBar {
  properties = BAR_EDITOR_PROPERTY
  propertyInner = {
    ...this['propertyInner'],
    'label-selector': [...BAR_EDITOR_PROPERTY_INNER['label-selector'], 'vPosition', 'showExtremum']
  }
  axisConfig = {
    ...this['axisConfig'],
    yAxis: {
      name: `${t('chart.drag_block_value_axis')} / ${t('chart.quota')}`,
      type: 'q',
      limit: 1
    }
  }

  async drawChart(drawOptions: G2PlotDrawOptions<Column>): Promise<Column> {
    const plot = await super.drawChart(drawOptions)
    if (!plot) {
      return plot
    }
    const { chart } = drawOptions
    const { xAxis, xAxisExt, yAxis } = chart
    let innerSort = !!(xAxis.length && xAxisExt.length && yAxis.length)
    if (innerSort && yAxis[0].sort === 'none') {
      innerSort = false
    }
    if (innerSort && xAxisExt[0].sort !== 'none') {
      const sortPriority = chart.sortPriority ?? []
      const yAxisIndex = sortPriority?.findIndex(e => e.id === yAxis[0].id)
      const xAxisExtIndex = sortPriority?.findIndex(e => e.id === xAxisExt[0].id)
      if (xAxisExtIndex <= yAxisIndex) {
        innerSort = false
      }
    }
    if (!innerSort) {
      return plot
    }
    plot.chart.once('beforepaint', () => {
      const geo = plot.chart.geometries[0]
      const originMapping = geo.beforeMapping.bind(geo)
      geo.beforeMapping = originData => {
        const values = geo.getXScale().values
        const valueMap = values.reduce((p, n) => {
          if (!p?.[n]) {
            p[n] = {
              fieldArr: [],
              indexArr: [],
              dataArr: []
            }
          }
          originData.forEach((arr, arrIndex) => {
            arr.forEach((item, index) => {
              if (item._origin.field === n) {
                p[n].fieldArr.push(item.field)
                p[n].indexArr.push([arrIndex, index])
                p[n].dataArr.push(item)
              }
            })
          })
          return p
        }, {})
        values.forEach(v => {
          const item = valueMap[v]
          item.dataArr.sort((a, b) => {
            if (yAxis[0].sort === 'asc') {
              return a.value - b.value
            }
            if (yAxis[0].sort === 'desc') {
              return b.value - a.value
            }
            return 0
          })
          item.indexArr.forEach((index, i) => {
            item.dataArr[i].field = item.fieldArr[i]
            originData[index[0]][index[1]] = item.dataArr[i]
          })
        })
        return originMapping(originData)
      }
    })
    return plot
  }

  protected configLabel(chart: Chart, options: ColumnOptions): ColumnOptions {
    const tmpLabel = getLabel(chart)
    if (!tmpLabel) {
      return options
    }
    const baseOptions = { ...options, label: tmpLabel }
    const { label: labelAttr } = parseJson(chart.customAttr)
    baseOptions.label.style.fill = labelAttr.color
    const label = {
      ...baseOptions.label,
      formatter: function (param: Datum) {
        if (param.EXTREME) {
          return ''
        }
        const value = valueFormatter(param.value, labelAttr.labelFormatter)
        return labelAttr.childrenShow ? value : null
      }
    }
    return {
      ...baseOptions,
      label
    }
  }

  protected configColor(chart: Chart, options: ColumnOptions): ColumnOptions {
    return this.configGroupColor(chart, options)
  }

  public setupSeriesColor(chart: ChartObj, data?: any[]): ChartBasicStyle['seriesColor'] {
    return setUpGroupSeriesColor(chart, data)
  }

  protected setupOptions(chart: Chart, options: ColumnOptions): ColumnOptions {
    return flow(
      this.addConditionsStyleColorToData,
      this.configTheme,
      this.configEmptyDataStrategy,
      this.configColor,
      this.configBasicStyle,
      this.configLabel,
      this.configTooltip,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configSlider,
      this.configAnalyse,
      this.configBarConditions
    )(chart, options, {}, this)
  }

  constructor(name = 'bar-group') {
    super(name)
    this.baseOptions = {
      ...this.baseOptions,
      marginRatio: 0,
      isGroup: true,
      isStack: false,
      meta: {
        category: {
          type: 'cat'
        }
      }
    }
    this.axis = [...BAR_AXIS_TYPE, 'xAxisExt']
  }
}

/**
 * 分组堆叠柱状图
 */
export class GroupStackBar extends StackBar {
  propertyInner = {
    ...this['propertyInner'],
    'label-selector': [...BAR_EDITOR_PROPERTY_INNER['label-selector'], 'vPosition']
  }
  protected configTheme(chart: Chart, options: ColumnOptions): ColumnOptions {
    const baseOptions = super.configTheme(chart, options)
    const baseTheme = baseOptions.theme as object
    const theme = {
      ...baseTheme,
      innerLabels: {
        offset: 0
      }
    }
    return {
      ...options,
      theme
    }
  }

  protected configLabel(chart: Chart, options: ColumnOptions): ColumnOptions {
    const tmpLabel = getLabel(chart)
    if (!tmpLabel) {
      return options
    }
    const baseOptions = { ...options, label: tmpLabel }
    const { label: labelAttr } = parseJson(chart.customAttr)
    baseOptions.label.style.fill = labelAttr.color
    const label = {
      ...baseOptions.label,
      formatter: function (param: Datum) {
        return valueFormatter(param.value, labelAttr.labelFormatter)
      }
    }
    return {
      ...baseOptions,
      label
    }
  }

  protected configTooltip(chart: Chart, options: ColumnOptions): ColumnOptions {
    const tooltipAttr = parseJson(chart.customAttr).tooltip
    if (!tooltipAttr.show) {
      return {
        ...options,
        tooltip: false
      }
    }
    const tooltip = {
      fields: [],
      formatter: (param: Datum) => {
        const obj = { name: `${param.category} - ${param.group}`, value: param.value }
        obj.value = valueFormatter(param.value, tooltipAttr.tooltipFormatter)
        return obj
      },
      container: getTooltipContainer(`tooltip-${chart.id}`),
      itemTpl: TOOLTIP_TPL,
      enterable: true
    }
    return {
      ...options,
      tooltip
    }
  }

  protected setupOptions(chart: Chart, options: ColumnOptions): ColumnOptions {
    return flow(
      this.configTheme,
      this.configEmptyDataStrategy,
      this.configColor,
      this.configBasicStyle,
      this.configLabel,
      this.configTooltip,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configSlider,
      this.configAnalyse
    )(chart, options, {}, this)
  }

  constructor(name = 'bar-group-stack') {
    super(name)
    this.baseOptions = {
      ...this.baseOptions,
      isGroup: true,
      groupField: 'group'
    }
    this.axis = [...this.axis, 'xAxisExt', 'extStack']
  }
}

/**
 * 百分比堆叠柱状图
 */
export class PercentageStackBar extends GroupStackBar {
  propertyInner = {
    ...this['propertyInner'],
    'label-selector': ['color', 'fontSize', 'vPosition', 'reserveDecimalCount'],
    'tooltip-selector': ['color', 'fontSize', 'backgroundColor', 'show', 'carousel']
  }
  protected configLabel(chart: Chart, options: ColumnOptions): ColumnOptions {
    const baseOptions = super.configLabel(chart, options)
    if (!baseOptions.label) {
      return baseOptions
    }
    const { customAttr } = chart
    const l = parseJson(customAttr).label
    const label = {
      ...baseOptions.label,
      formatter: function (param: Datum) {
        if (!param.value) {
          return '0%'
        }
        return (Math.round(param.value * 10000) / 100).toFixed(l.reserveDecimalCount) + '%'
      }
    }
    return {
      ...baseOptions,
      label
    }
  }

  protected configTooltip(chart: Chart, options: ColumnOptions): ColumnOptions {
    const tooltipAttr = parseJson(chart.customAttr).tooltip
    if (!tooltipAttr.show) {
      return {
        ...options,
        tooltip: {
          showContent: false
        }
      }
    }
    const { customAttr } = chart
    const l = parseJson(customAttr).label
    const tooltip = {
      formatter: (param: Datum) => {
        const obj = { name: param.category, value: param.value }
        obj.value = (Math.round(param.value * 10000) / 100).toFixed(l.reserveDecimalCount) + '%'
        return obj
      },
      container: getTooltipContainer(`tooltip-${chart.id}`),
      itemTpl: TOOLTIP_TPL,
      enterable: true
    }
    return {
      ...options,
      tooltip
    }
  }
  protected setupOptions(chart: Chart, options: ColumnOptions): ColumnOptions {
    return flow(
      this.configTheme,
      this.configEmptyDataStrategy,
      this.configColor,
      this.configBasicStyle,
      this.configLabel,
      this.configTooltip,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configSlider,
      this.configAnalyse
    )(chart, options, {}, this)
  }
  constructor() {
    super('percentage-bar-stack')
    this.baseOptions = {
      ...this.baseOptions,
      isStack: true,
      isPercent: true,
      isGroup: false,
      groupField: undefined,
      meta: {
        category: {
          type: 'cat'
        }
      }
    }
    this.axis = [...BAR_AXIS_TYPE, 'extStack']
  }
}
