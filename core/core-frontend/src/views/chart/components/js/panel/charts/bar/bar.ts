import type { Column, ColumnOptions } from '@antv/g2plot/esm/plots/column'
import { cloneDeep, defaults, each, groupBy, isEmpty } from 'lodash-es'
import {
  G2PlotChartView,
  G2PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/g2plot'
import {
  convertToAlphaColor,
  flow,
  hexColorToRGBA,
  isAlphaColor,
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
import {
  DEFAULT_BASIC_STYLE,
  DEFAULT_LABEL,
  DEFAULT_LEGEND_STYLE
} from '@/views/chart/components/editor/util/chart'
import { clearExtremum, extremumEvt } from '@/views/chart/components/js/extremumUitl'
import { Group } from '@antv/g-canvas'
import { getItemsOfView } from '@antv/g2/lib/interaction/action/active-region'

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
    // 只处理柱状图，分组和堆叠的阴影部分没有子维度信息
    if (this.name === 'bar' && options.tooltip) {
      newChart.on('plot:click', e => {
        if (e.target?.cfg?.renderer !== 'canvas') {
          return
        }
        const activeRegion = e.view.backgroundGroup.cfg.children.find(
          i => i.cfg.name === 'active-region'
        )
        if (activeRegion?.cfg.visible) {
          const items = getItemsOfView(
            e.view,
            { x: e.x, y: e.y },
            e.view.getController('tooltip').getTooltipCfg()
          )
          if (items?.length) {
            const datum = items[0].data
            if (datum && datum.field) {
              action({
                x: e.x,
                y: e.y,
                data: {
                  data: datum
                }
              })
            }
          }
        }
      })
    }
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
  properties: EditorProperty[] = BAR_EDITOR_PROPERTY.filter(ele => ele !== 'threshold')
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
    ],
    'legend-selector': [...BAR_EDITOR_PROPERTY_INNER['legend-selector'], 'legendSort']
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

  protected configSortedLegend(chart: Chart, options: ColumnOptions): ColumnOptions {
    const optionTmp = super.configLegend(chart, options)
    if (!optionTmp.legend) {
      return optionTmp
    }
    const extStack = chart.extStack[0]
    if (extStack?.customSort?.length > 0) {
      // 图例自定义排序
      const sort = extStack.customSort ?? []
      if (sort?.length) {
        // 用值域限定排序，有可能出现新数据但是未出现在图表上，所以这边要遍历一下子维度，加到后面，让新数据显示出来
        const data = optionTmp.data
        const cats =
          data?.reduce((p, n) => {
            const cat = n['category']
            if (cat && !p.includes(cat)) {
              p.push(cat)
            }
            return p
          }, []) || []
        const values = sort.reduce((p, n) => {
          if (cats.includes(n)) {
            const index = cats.indexOf(n)
            if (index !== -1) {
              cats.splice(index, 1)
            }
            p.push(n)
          }
          return p
        }, [])
        cats.length > 0 && values.push(...cats)
        optionTmp.meta = {
          ...optionTmp.meta,
          category: {
            type: 'cat',
            values
          }
        }
      }
    }

    const customStyle = parseJson(chart.customStyle)
    let size
    if (customStyle && customStyle.legend) {
      size = defaults(JSON.parse(JSON.stringify(customStyle.legend)), DEFAULT_LEGEND_STYLE).size
    } else {
      size = DEFAULT_LEGEND_STYLE.size
    }

    optionTmp.legend.marker.style = style => {
      return {
        r: size,
        fill: style.fill
      }
    }
    const { sort, customSort, icon } = customStyle.legend
    if (sort && sort !== 'none' && chart.extStack.length) {
      const customAttr = parseJson(chart.customAttr)
      const { basicStyle } = customAttr
      const seriesMap =
        basicStyle.seriesColor?.reduce((p, n) => {
          p[n.id] = n
          return p
        }, {}) || {}
      const dupCheck = new Set()
      const colors = optionTmp.color ?? optionTmp.theme.styleSheet.paletteQualitative10
      const items = optionTmp.data?.reduce((arr, item) => {
        if (!dupCheck.has(item.category)) {
          const fill = seriesMap[item.category]?.color ?? colors[dupCheck.size % colors.length]
          dupCheck.add(item.category)
          arr.push({
            name: item.category,
            value: item.category,
            marker: {
              symbol: icon,
              style: {
                r: size,
                fill: isAlphaColor(fill) ? fill : convertToAlphaColor(fill, basicStyle.alpha)
              }
            }
          })
        }
        return arr
      }, [])
      if (sort !== 'custom') {
        items.sort((a, b) => {
          return sort !== 'desc' ? a.name.localeCompare(b.name) : b.name.localeCompare(a.name)
        })
      } else {
        const tmp = []
        ;(customSort || []).forEach(item => {
          const index = items.findIndex(i => i.name === item)
          if (index !== -1) {
            tmp.push(items[index])
            items.splice(index, 1)
          }
        })
        items.unshift(...tmp)
      }
      optionTmp.legend.items = items
      if (extStack?.customSort?.length > 0) {
        delete optionTmp.meta?.category.values
      }
    }
    return optionTmp
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
      this.configSortedLegend,
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
    'label-selector': [...BAR_EDITOR_PROPERTY_INNER['label-selector'], 'vPosition', 'showExtremum'],
    'legend-selector': BAR_EDITOR_PROPERTY_INNER['legend-selector']
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
    'label-selector': [...BAR_EDITOR_PROPERTY_INNER['label-selector'], 'vPosition'],
    'legend-selector': BAR_EDITOR_PROPERTY_INNER['legend-selector']
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
