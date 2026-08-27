import {
  BAR_EDITOR_PROPERTY_INNER,
  BAR_RANGE_EDITOR_PROPERTY
} from '@/views/chart/components/js/panel/charts/g2/bar/common'
import { flow, parseJson } from '@/views/chart/components/js/util'
import {
  getHorizontalBarAxisSafeLabelStyle,
  listenerTooltipShow,
  createTooltipWrapper,
  tooltipCss,
  ViewSpec
} from '@/views/chart/components/js/panel/charts/g2/bar/barUtil'
import { useI18n } from '@/hooks/web/useI18n'
import { Chart as G2Column } from '@antv/g2'
import { HorizontalBar } from '@/views/chart/components/js/panel/charts/g2/bar/horizontal-bar'
import { G2DrawOptions } from '@/views/chart/components/js/panel/types/impl/g2'
import { cloneDeep, isEmpty } from 'lodash-es'
import dayjs from 'dayjs'
import {
  configAxisLengthLimit,
  getG2Renderer,
  handleChartDashboardHidden,
  TOOLTIP_ITEM_TPL,
  TOOLTIP_TITLE_TPL
} from '@/views/chart/components/js/panel/common/common_antv'
import { valueFormatter } from '@/views/chart/components/js/formatter'

const { t } = useI18n()
// 与父类方法参数保持一致，避免 flow 推导到 @antv/g2 的 Chart 类型后出现 TS2345。
type PanelChart = Parameters<HorizontalBar['setupOptions']>[0]
type RangeDateChart = {
  data: { isDate?: boolean }
  aggregate?: boolean
  yAxis?: Array<{ datePattern?: string; dateStyle?: string }>
}
type DateWithFormatter = Date & { format: (format: string) => string }
type RangeDateTickUnit = 'year' | 'month' | 'day' | 'hour' | 'minute' | 'second'

const RANGE_DATE_TICK_UNIT: Record<string, RangeDateTickUnit> = {
  y: 'year',
  y_M: 'month',
  y_M_d: 'day',
  M_d: 'day',
  H_m_s: 'second',
  y_M_d_H: 'hour',
  y_M_d_H_m: 'minute',
  y_M_d_H_m_s: 'second'
}

const asRangeDateChart = <T>(chart: T) => chart as T & RangeDateChart
const formatRangeDate = (date: Date, dateFormat: string) =>
  (date as DateWithFormatter).format(dateFormat)
const createRangeDateTickMethod =
  (dateStyle?: string) =>
  (min: Date, max: Date, count = 7) => {
    const start = dayjs(min)
    const end = dayjs(max)
    if (!start.isValid() || !end.isValid() || end.isBefore(start)) {
      return []
    }
    const unit = RANGE_DATE_TICK_UNIT[dateStyle] || 'second'
    const tickCount = Math.max(2, count || 7)
    const step = Math.max(1, Math.ceil(end.diff(start, unit, true) / tickCount))
    const ticks: Date[] = []
    // 按定义域起点生成自然时间刻度，避免同一日期的刻度与区间端点错位
    for (let index = 0; index <= tickCount + 1; index++) {
      const current = start.add(index * step, unit)
      if (current.isAfter(end)) break
      ticks.push(current.toDate())
    }
    if (ticks[ticks.length - 1]?.getTime() !== end.valueOf()) {
      ticks.push(end.toDate())
    }
    return ticks
  }
const normalizeRangeValue = (value: any, isDate: boolean) => {
  if (value === null || value === undefined || value === '') {
    return null
  }
  // 日期区间按本地时间解析，避免无时区日期被当成 UTC
  return isDate ? dayjs(value).valueOf() : value
}
const getRangeValues = (data: any) =>
  Array.isArray(data?.values) ? data.values : [data?.startValue, data?.endValue]

/**
 * 堆叠条形图
 */
export class RangeBar extends HorizontalBar {
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
  // 显式声明为父类同型，避免 TS 将 spread 结果推断成可选字段集合。
  propertyInner: HorizontalBar['propertyInner'] = {
    ...BAR_EDITOR_PROPERTY_INNER,
    // 显式补齐父类要求的必填项，避免 spread 后被推断为可选导致 TS2416。
    'basic-style-selector': BAR_EDITOR_PROPERTY_INNER['basic-style-selector'] || [],
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

  async drawChart(drawOptions: G2DrawOptions<G2Column>): Promise<G2Column> {
    const { chart, container, action } = drawOptions
    chart.container = container
    if (!chart?.data?.data?.length) {
      return
    }
    const rangeChart = asRangeDateChart(chart)
    let data = cloneDeep(drawOptions.chart.data?.data)
    const isDate = !!rangeChart.data.isDate
    const dateSplit = rangeChart.yAxis?.[0]?.datePattern === 'date_split' ? '/' : '-'
    const dateStyle = rangeChart.yAxis?.[0]?.dateStyle
    const dateFormat =
      {
        y: 'yyyy',
        y_M: `yyyy${dateSplit}MM`,
        y_M_d: `yyyy${dateSplit}MM${dateSplit}dd`,
        y_M_d_H: `yyyy${dateSplit}MM${dateSplit}dd hh`,
        y_M_d_H_m: `yyyy${dateSplit}MM${dateSplit}dd hh:mm`,
        y_M_d_H_m_s: `yyyy${dateSplit}MM${dateSplit}dd hh:mm:ss`
      }[dateStyle] || 'yyyy-MM-dd hh:mm:ss'
    data = cloneDeep(data).map(item => {
      // 时间区间转为时间戳绘制，避免 G2 在 Date range interval 中生成异常 points
      const values = Array.isArray(item.values)
        ? item.values.map(value => normalizeRangeValue(value, isDate))
        : []
      return {
        ...item,
        values,
        startValue: values[0],
        endValue: values[1],
        ...(isDate ? { dateFormat } : {})
      }
    })
    const initOptions: ViewSpec = {
      type: 'view',
      children: [
        {
          ...this.intervalOptions,
          encode: { ...this.intervalOptions.encode, y: 'startValue', y1: 'endValue' },
          transform: [].concat(this.intervalOptions.transform),
          scale: {
            x: {},
            color: {},
            ...(this.intervalOptions.scale || {}),
            y: {
              ...(this.intervalOptions.scale?.y || {}),
              ...(isDate
                ? {
                    type: 'time',
                    tickCount: 7,
                    tickMethod: createRangeDateTickMethod(dateStyle)
                  }
                : {}),
              mask: isDate ? dateFormat : undefined,
              labelFormatter: val => (isDate ? formatRangeDate(new Date(val), dateFormat) : val)
            },
            y1: { key: 'y' }
          },
          data
        }
      ]
    }
    const options: ViewSpec = this.setupOptions(chart, initOptions)
    const newChart = new G2Column({ container, autoFit: true, ...getG2Renderer() })
    handleChartDashboardHidden(chart, options)
    newChart.options(options)
    newChart.on('interval:click', action)
    listenerTooltipShow(newChart, chart)
    configAxisLengthLimit(chart, newChart, 'yAxis')
    return newChart
  }

  protected configYAxis(chart: PanelChart, options: ViewSpec): ViewSpec {
    const tmpOptions = super.configYAxis(chart, options)
    if (tmpOptions.children[0].axis?.y === false) {
      // 横轴关闭时保留父类生成的 false，避免只追加 formatter 又唤起默认轴
      return tmpOptions
    }
    const customStyle = parseJson(chart.customStyle)
    const axis = JSON.parse(JSON.stringify(customStyle['xAxis']))
    const rangeChart = asRangeDateChart(chart)
    const isDate = !!rangeChart.data.isDate
    const dateFormat = options.children[0].scale.y.mask
    const formatAxisValue = val => {
      if (isDate) {
        return formatRangeDate(new Date(val), dateFormat)
      }
      return valueFormatter(val, axis.axisLabelFormatter)
    }
    const formatAxisLabel = (val, index, ticks) => {
      return formatAxisValue(val)
    }
    tmpOptions.children[0].axis.y = {
      ...tmpOptions.children[0].axis.y,
      dataeaseAxisTitleSafeMargin: true,
      labelAutoHide: { keepHeader: true },
      labelFormatter: formatAxisLabel
    }
    return tmpOptions
  }

  protected configLegend(chart: PanelChart, options: ViewSpec): ViewSpec {
    const rangeChart = asRangeDateChart(chart)
    if (rangeChart.data.isDate && !rangeChart.aggregate) {
      const { children } = options
      // 对齐 V2：非聚合时间区间保留分类着色但不生成图例
      return {
        ...options,
        children: [{ ...children[0], legend: false }, ...children.slice(1)]
      }
    }
    return super.configLegend(chart, options)
  }

  protected configLabel(chart: PanelChart, options: ViewSpec): ViewSpec {
    const customAttr = parseJson(chart.customAttr)
    const { label: labelAttr } = customAttr
    if (!labelAttr.show) return options

    const { children } = options
    const position = {
      position: labelAttr.position === 'middle' ? 'inside' : labelAttr.position,
      textAlign: 'center',
      dy: labelAttr.position === 'top' ? -10 : 0,
      dx: labelAttr.position === 'middle' ? 0 : 15,
      ...getHorizontalBarAxisSafeLabelStyle(chart, labelAttr.position, 15)
    }
    const transform = {
      transform: [
        { type: 'exceedAdjust', bounds: 'main' },
        ...(labelAttr.fullDisplay ? [] : [{ type: 'overlapHide' }])
      ]
    }
    const rangeChart = asRangeDateChart(chart)
    const isDate = !!rangeChart.data.isDate
    const dateFormat = children[0].scale.y.mask
    const formatDateLabel = (dateVal: any) => {
      // 标签格式化兜底：空值或非法日期直接返回空串，避免显示 Invalid Date。
      if (dateVal === null || dateVal === undefined || dateVal === '') {
        return ''
      }
      const date = dateVal instanceof Date ? dateVal : new Date(dateVal)
      return Number.isNaN(date.getTime()) ? '' : formatRangeDate(date, dateFormat)
    }
    const formatRangeLabelValue = (value: any) =>
      isDate ? formatDateLabel(value) : valueFormatter(value, labelAttr.labelFormatter)
    const label = {
      // 区间条标签直接生成最终文本，避免 range interval 默认 value 绕过格式配置
      text: data => {
        if (labelAttr.showGap) {
          return formatRangeLabelValue(data.gap)
        }
        return getRangeValues(data).map(formatRangeLabelValue).join(' ~ ')
      },
      fillOpacity: 1,
      pointerEvents: 'none',
      fill: labelAttr.color,
      fontSize: labelAttr.fontSize,
      ...position,
      ...transform
    }
    children[0].labels = [label]
    return options
  }

  protected configTooltip(chart: PanelChart, options: ViewSpec): ViewSpec {
    const { tooltip } = parseJson(chart.customAttr)
    const { children } = options
    if (!tooltip.show) {
      children[0].tooltip = false
      return options
    }
    const rangeChart = asRangeDateChart(chart)
    const isDate = !!rangeChart.data.isDate
    const dateFormat = children[0].scale.y.mask
    const formatDateLabel = (dateVal: any) => {
      // tooltip 与标签保持一致的空值处理，避免时间区间展示异常文本。
      if (dateVal === null || dateVal === undefined || dateVal === '') {
        return ''
      }
      const date = dateVal instanceof Date ? dateVal : new Date(dateVal)
      return Number.isNaN(date.getTime()) ? '' : formatRangeDate(date, dateFormat)
    }
    const formatTooltipValue = (value: any) =>
      isDate ? formatDateLabel(value) : valueFormatter(value, tooltip.tooltipFormatter)
    const tooltipOptions: ViewSpec = {
      tooltip: {
        items: [
          (datum, index, data) => ({
            value: datum.values,
            original_data: data[index]
          })
        ]
      },
      interaction: {
        ...children[0].interaction,
        tooltip: {
          mount: createTooltipWrapper(chart),
          css: tooltipCss(tooltip),
          enterable: true,
          // 区间条是起止范围矩形，禁用 series tooltip 的 crosshair 点集计算
          series: false,
          shared: false,
          crosshairs: false,
          crosshairsX: false,
          crosshairsY: false,
          marker: false,
          bounding: { x: 0, y: 0 },
          position: 'top-right',
          render: (_, { title, items: originalItems }) => {
            const titleHtml = TOOLTIP_TITLE_TPL.replace('{title}', title)
            const itemsHtml = originalItems
              .map(item => {
                const values = Array.isArray(item.value) ? item.value : []
                // 保留“开始值 ~ 结束值”语义，空间不足时交给公共 tooltip 样式省略
                const value =
                  values.map(formatTooltipValue).join(' ~ ') +
                  (tooltip.showGap ? ` (${item.original_data.gap ?? ''})` : '')
                const name = isEmpty(item.original_data.category)
                  ? item.original_data.field
                  : item.original_data.category
                return TOOLTIP_ITEM_TPL.replace('{marker}', item.color)
                  .replace('{label}', name)
                  .replace('{value}', value)
              })
              .join('')
            const listHtml = `<ul class="g2-tooltip-list" style="margin: 0px; list-style-type: none; padding: 0px;">${itemsHtml}</ul>`
            return `${titleHtml}${listHtml}`
          }
        }
      }
    }
    return {
      ...options,
      children: [{ ...options.children[0], ...tooltipOptions }, ...options.children.slice(1)]
    }
  }

  protected configEmptyDataStrategy(chart: PanelChart, options: ViewSpec): ViewSpec {
    const markData = options.children?.[0]?.data
    // 兼容 data 既可能是数组也可能是 { value: [] } 的场景。
    const data = Array.isArray(markData) ? markData : markData?.value
    if (!Array.isArray(data) || !data.length) return options

    const isEmptyValue = (value: any) => {
      if (value === null || value === undefined || value === '') {
        return true
      }
      if (value instanceof Date) {
        return Number.isNaN(value.getTime())
      }
      if (typeof value === 'number') {
        return Number.isNaN(value)
      }
      return false
    }

    const rangeChart = asRangeDateChart(chart)
    const isDate = !!rangeChart.data.isDate
    const configuredStrategy = parseJson(chart.senior)?.functionCfg?.emptyDataStrategy
    // 日期区间不把空端点转换为 Unix 时间零点
    const strategy = isDate && configuredStrategy === 'setZero' ? 'breakLine' : configuredStrategy

    // RangeBar 的数值是 [start, end]，不能复用普通柱图单值逻辑。
    if (strategy === 'ignoreData') {
      // 任一端点为空时过滤整条，防止区间绘制不完整。
      const filteredData = data.filter(item => {
        if (!Array.isArray(item.values) || item.values.length < 2) {
          return false
        }
        return !item.values.some(v => isEmptyValue(v))
      })
      if (Array.isArray(markData)) {
        options.children[0].data = filteredData
      } else if (markData) {
        markData.value = filteredData
      }
      return options
    }

    if (strategy === 'setZero') {
      // 仅替换空端点为 0，非空值保持原始数据。
      data.forEach(item => {
        if (Array.isArray(item.values) && item.values.length >= 2) {
          item.values = item.values.map(v => (isEmptyValue(v) ? 0 : v))
          item.startValue = item.values[0]
          item.endValue = item.values[1]
          item.gap = Number(item.endValue) - Number(item.startValue)
        }
      })
    }

    return options
  }

  protected setupOptions(chart: PanelChart, options: ViewSpec): ViewSpec {
    return flow(
      this.configTheme,
      this.configEmptyDataStrategy,
      this.configBasicStyle,
      this.configLabel,
      this.configTooltip,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configSlider
    )(chart, options, {}, this)
  }

  constructor(name = 'bar-range') {
    super(name)
    Object.assign(this.intervalOptions, {
      transform: [],
      coordinate: { transform: [{ type: 'transpose' }] }
    })
    this.axis = [...this.axis, 'yAxisExt']
  }
}
