import { Chart as G2Bullet } from '@antv/g2'
import { G2ChartView, G2DrawOptions } from '@/views/chart/components/js/panel/types/impl/g2'
import {
  BAR_AXIS_TYPE,
  BAR_EDITOR_PROPERTY,
  BAR_EDITOR_PROPERTY_INNER
} from '@/views/chart/components/js/panel/charts/g2/bar/common'
import { useI18n } from '@/hooks/web/useI18n'
import { flow, parseJson } from '@/views/chart/components/js/util'
import { RuntimeOptions } from '@antv/g2/lib/api/runtime'
import { formatterItem, valueFormatter } from '@/views/chart/components/js/formatter'
import {
  configAxisLengthLimit,
  formatAxisLabelWithLengthLimit,
  getG2Renderer,
  getLineDash,
  handleChartDashboardHidden,
  TOOLTIP_ITEM_TPL,
  TOOLTIP_TITLE_TPL
} from '@/views/chart/components/js/panel/common/common_antv'
import {
  createTooltipWrapper,
  listenerTooltipShow,
  tooltipCss,
  tooltipMaxHeight
} from '@/views/chart/components/js/panel/charts/g2/bar/barUtil'
import { isEmpty } from 'lodash-es'

const { t } = useI18n()

const BULLET_MEASURE_KEY = '__de_bullet_measure__'
const BULLET_TARGET_KEY = '__de_bullet_target__'
const BULLET_DYNAMIC_RANGE_KEY = '__de_bullet_range_dynamic__'
const getFixedRangeKey = (index: number) => `__de_bullet_range_${index}__`

/**
 * 子弹图
 */
export class BulletGraph extends G2ChartView<RuntimeOptions, G2Bullet> {
  constructor() {
    super('bullet-graph', [])
  }

  axis: AxisType[] = [...BAR_AXIS_TYPE, 'yAxisExt', 'extBubble']
  axisConfig = {
    ...this['axisConfig'],
    xAxis: { name: `${t('chart.form_type')} / ${t('chart.dimension')}`, type: 'd', limit: 1 },
    yAxis: { name: `${t('chart.progress_current')} / ${t('chart.quota')}`, type: 'q', limit: 1 },
    yAxisExt: { name: `${t('chart.progress_target')} / ${t('chart.quota')}`, type: 'q', limit: 1 },
    extBubble: {
      name: `${t('chart.range_bg')} / ${t('chart.quota')}`,
      type: 'q',
      allowEmpty: true,
      limit: 1
    }
  }
  properties: EditorProperty[] = [
    ...BAR_EDITOR_PROPERTY.filter(
      item => !['function-cfg', 'assist-line', 'threshold'].includes(item)
    ),
    'bullet-graph-selector'
  ]
  propertyInner = {
    'basic-style-selector': ['radiusColumnBar', 'layout'],
    'label-selector': ['hPosition', 'vPosition', 'fontSize', 'color', 'labelFormatter'],
    'tooltip-selector': ['fontSize', 'color', 'backgroundColor', 'seriesTooltipFormatter', 'show'],
    'x-axis-selector': [
      ...BAR_EDITOR_PROPERTY_INNER['x-axis-selector'].filter(item => item != 'position'),
      'showLengthLimit'
    ],
    'y-axis-selector': [
      ...BAR_EDITOR_PROPERTY_INNER['y-axis-selector'].filter(
        item => item !== 'axisValue' && item !== 'position'
      ),
      'axisLabelFormatter'
    ],
    'legend-selector': ['showRange', 'orient', 'fontSize', 'color', 'hPosition', 'vPosition']
  }

  async drawChart(drawOption: G2DrawOptions<G2Bullet>): Promise<G2Bullet> {
    const { chart, container, action } = drawOption
    if (!chart.data?.data?.length) return
    const result = mergeBulletData(chart)
    // 处理自定义区间
    const { bullet } = parseJson(chart.customAttr).misc
    if (bullet.bar.ranges.showType === 'fixed') {
      const customRange = bullet.bar.ranges.fixedRange?.map(item => item.fixedRangeValue) || [0]
      result.forEach(item => (item.ranges = customRange))
    } else {
      result.forEach(item => (item.ranges = item.originalRanges))
    }
    // 处理自定义目标值
    if (bullet.bar.target.showType === 'fixed') {
      const customTarget = bullet.bar.target.value || 0
      result.forEach(item => {
        item.target = customTarget
        item.tooltipTarget = customTarget
      })
    } else {
      result.forEach(item => (item.target = item.originalTarget))
    }
    const initOptions = {
      container,
      autoFit: true,
      ...getG2Renderer(),
      data: result
    }
    const options = this.setupOptions(chart, initOptions)
    let newChart = null
    const { Chart: BulletClass } = await import('@antv/g2')
    handleChartDashboardHidden(chart, options)
    newChart = new BulletClass(options)
    newChart.on('element:click', ev => {
      const pointData = ev?.data?.data
      const dimensionList = options.data.find(item => item.title === pointData.title)?.dimensionList
      const actionParams = {
        ...ev,
        data: {
          data: {
            ...pointData,
            dimensionList
          }
        }
      }
      action(actionParams)
    })
    listenerTooltipShow(newChart, chart)
    configAxisLengthLimit(chart, newChart, 'xAxis')
    return newChart
  }

  protected configBasicStyle(chart: Chart, options: RuntimeOptions): RuntimeOptions {
    const basicStyle = parseJson(chart.customAttr).basicStyle
    const { radiusColumnBar, columnBarRightAngleRadius, layout } = basicStyle
    // 获取所有 interval 类型的子元素
    const intervals = options.children?.filter(item => item.type === 'interval') || []
    const intervalCount = intervals.length
    // 如果需要设置圆角，计算圆角值
    const radiusValue =
      radiusColumnBar === 'roundAngle' || radiusColumnBar === 'topRoundAngle'
        ? columnBarRightAngleRadius
        : 0
    // 更新每个 interval 的样式
    intervals.forEach((item, index) => {
      const updateStyle = (styleUpdates: Record<string, number>) => {
        item.style = { ...item.style, ...styleUpdates }
      }
      // 根据 radiusColumnBar 的值和索引位置，设置不同的圆角样式
      if (radiusColumnBar === 'topRoundAngle') {
        // 顶部圆角
        if (index === intervalCount - 2) {
          // 倒数第二个柱子（固定区间背景的最后一个）
          updateStyle({ radiusTopLeft: radiusValue, radiusTopRight: radiusValue })
        }
        if (index === intervalCount - 1) {
          // 最后一个柱子（实际值）
          updateStyle({ radiusTopLeft: radiusValue, radiusTopRight: radiusValue })
        }
      } else if (radiusColumnBar === 'roundAngle') {
        // 四角圆角
        if (index === 0) {
          // 第一个柱子
          updateStyle({ radiusBottomLeft: radiusValue, radiusBottomRight: radiusValue })
        }
        if (index === intervalCount - 2) {
          // 倒数第二个柱子（固定区间背景的最后一个）
          updateStyle({ radiusTopLeft: radiusValue, radiusTopRight: radiusValue })
        }
        if (index === intervalCount - 1) {
          // 最后一个柱子
          updateStyle({ radiusBottomLeft: radiusValue, radiusBottomRight: radiusValue })
          updateStyle({ radiusTopLeft: radiusValue, radiusTopRight: radiusValue })
        }
      }
    })
    // 如果布局是水平的，转置坐标系
    if (layout === 'horizontal') {
      options.coordinate = { transform: [{ type: 'transpose' }] }
    }
    return options
  }

  protected configMisc(chart: Chart, options: RuntimeOptions): RuntimeOptions {
    const { basicStyle, tooltip } = parseJson(chart.customAttr)
    const customStyleLegend = parseJson(chart.customStyle).legend
    const { bullet } = parseJson(chart.customAttr).misc
    const isDynamic = bullet.bar.ranges.showType === 'dynamic'
    const showRangeLegend = customStyleLegend?.showRange
    const rangeLegendKeys: string[] = []
    const rangeLegendLabelMap: Record<string, string> = {}
    const hoverState = () => ({
      active: {
        backgroundPointerEvents: 'none'
      }
    })
    // 背景颜色，固定区间背景时，按大小降序
    const rangeColor = isDynamic
      ? chart.extBubble?.length
        ? bullet.bar.ranges.fill
        : []
      : bullet.bar.ranges.fixedRange
          ?.sort((a, b) => (a.fixedRangeValue ?? 0) - (b.fixedRangeValue ?? 0))
          .map(item => item.fill) || []
    const childrens = []
    // 固定区间背景
    const ranges = bullet.bar.ranges.fixedRange || []
    ranges.sort((a, b) => (a.fixedRangeValue ?? 0) - (b.fixedRangeValue ?? 0))
    if (showRangeLegend && !isDynamic) {
      ranges.forEach((item, index) => {
        const key = getFixedRangeKey(index)
        rangeLegendKeys.push(key)
        rangeLegendLabelMap[key] = item.name ?? ''
      })
    }
    ranges.forEach((item, index) => {
      // 用于配置区间边界， 存储当前区间的上一个区间
      const prev = ranges[index - 1]
      const range = {
        type: 'interval',
        encode: {
          x: 'title',
          y: [prev ? prev.fixedRangeValue : 0, item.fixedRangeValue],
          ...(showRangeLegend ? { color: () => getFixedRangeKey(index) } : {})
        },
        interaction: {
          legendFilter: false
        },
        style: {
          maxWidth: bullet.bar.ranges.size,
          ...(showRangeLegend ? {} : { fill: item.fill })
        },
        state: hoverState(),
        tooltip: false
      }
      childrens.push(range)
    })
    if (isDynamic) {
      childrens.length = 0
      if (chart.extBubble?.length) {
        const rangeName = chart.extBubble[0]?.chartShowName || chart.extBubble[0]?.name
        if (showRangeLegend) {
          rangeLegendKeys.push(BULLET_DYNAMIC_RANGE_KEY)
          rangeLegendLabelMap[BULLET_DYNAMIC_RANGE_KEY] = rangeName
        }
        childrens.push({
          type: 'interval',
          encode: {
            x: 'title',
            y: 'ranges',
            ...(showRangeLegend ? { color: () => BULLET_DYNAMIC_RANGE_KEY } : {})
          },
          interaction: {
            legendFilter: false
          },
          style: {
            maxWidth: bullet.bar.ranges.size,
            ...(showRangeLegend ? {} : { fill: [].concat(bullet.bar.ranges.fill)[0] })
          },
          state: hoverState(),
          tooltip: false
        })
      }
    }
    // 实际值与目标值
    const measures = {
      // 标识实际值 mark，联动恢复时只对该层绘制选中描边
      key: BULLET_MEASURE_KEY,
      type: 'interval',
      encode: {
        x: 'title',
        y: 'measures',
        color: () => BULLET_MEASURE_KEY,
        shape: 'rect'
      },
      interaction: {
        legendFilter: false
      },
      style: {
        maxWidth: bullet.bar.measures.size
      },
      state: hoverState(),
      tooltip: tooltip.show
        ? {
            title: d => d.title,
            items: [{ channel: 'y' }]
          }
        : false
    }
    const target = {
      type: 'point',
      encode: {
        x: 'title',
        y: 'target',
        color: () => BULLET_TARGET_KEY,
        shape: basicStyle.layout === 'horizontal' ? 'line' : 'hyphen',
        size: bullet.bar.target.size
      },
      interaction: {
        legendFilter: false
      },
      state: hoverState(),
      tooltip: tooltip.show
        ? {
            title: false,
            items: [{ channel: 'y' }]
          }
        : false
    }
    childrens.push(measures)
    childrens.push(target)
    if (tooltip.show) {
      // 空数据 line 仅用于启用 series tooltip，让分类带空白区域按最近维度命中
      childrens.push({
        type: 'line',
        data: [],
        encode: {
          x: 'title',
          y: 'measures'
        },
        tooltip: {
          title: d => d.title,
          items: [{ channel: 'y' }]
        }
      })
    }
    options = {
      ...options,
      interaction: {
        ...options.interaction,
        // 与基础柱状图一致，按最近维度绘制分类带悬浮背景
        elementHighlight: {
          background: true,
          region: true,
          single: true
        }
      },
      scale: {
        color: {
          domain: [
            ...(showRangeLegend ? rangeLegendKeys : []),
            BULLET_TARGET_KEY,
            BULLET_MEASURE_KEY
          ],
          range: [
            ...(showRangeLegend ? [].concat(rangeColor) : []),
            ...[].concat(bullet.bar.target.fill),
            ...[].concat(bullet.bar.measures.fill)
          ]
        }
      },
      children: childrens
    }
    return options
  }

  protected configAxis(chart: Chart, options: RuntimeOptions): RuntimeOptions {
    return { ...options, axis: { x: this.configXAxis(chart), y: this.configYAxis(chart) } }
  }

  protected configXAxis(chart: Chart): any {
    const horizontal = parseJson(chart.customAttr).basicStyle.layout === 'horizontal'
    return this.getAxisConfig(chart, 'xAxis', horizontal ? 'left' : 'bottom')
  }

  protected configYAxis(chart: Chart): any {
    const horizontal = parseJson(chart.customAttr).basicStyle.layout === 'horizontal'
    return this.getAxisConfig(chart, 'yAxis', horizontal ? 'bottom' : 'left')
  }

  protected getAxisConfig(chart: Chart, axisType: string, position: string) {
    const customStyle = parseJson(chart.customStyle)
    const axis = JSON.parse(JSON.stringify(customStyle[axisType]))
    if (customStyle[axisType] && axis.show) {
      // 轴线与刻度线统一使用公共规则
      const lineAndTick = {
        ...this.getAxisLineStyle(chart, axis),
        lineLineDash: getLineDash(axis.axisLine.lineStyle.style),
        lineStrokeOpacity: 1
      }
      // 网格线
      const grid = {
        grid: axis.splitLine.show,
        gridLineWidth: axis.splitLine.lineStyle.width,
        gridStroke: axis.splitLine.lineStyle.color,
        gridStrokeOpacity: 1,
        gridLineDash: getLineDash(axis.splitLine.lineStyle.style)
      }
      // 标签（刻度值）
      const label = {
        label: axis.axisLabel.show,
        labelFontSize: axis.axisLabel.fontSize,
        labelFill: axis.axisLabel.color,
        labelOpacity: 1,
        labelFormatter: value => {
          if (axisType === 'yAxis') {
            return valueFormatter(value, axis.axisLabelFormatter)
          }
          if (axisType === 'xAxis') {
            return formatAxisLabelWithLengthLimit(value, axis.axisLabel.lengthLimit)
          }
          return value
        }
      }
      const x = {
        // 标题
        title: axis.nameShow && axis.name ? axis.name : false,
        // 左轴标题复用条形图的安全留白，避免贴近轴标签或画布边界
        ...(position === 'left' ? { dataeaseAxisTitleSafeMargin: true } : {}),
        titleFontSize: axis.fontSize,
        titleFill: axis.color,
        // 轴线与刻度线
        ...lineAndTick,
        // 网格线
        ...grid,
        // 刻度值
        ...label,
        ...this.getAxisLabelStyle({ ...axis, position })
      }
      return x
    }
    return false
  }

  protected configLegend(chart: Chart, options: RuntimeOptions): RuntimeOptions {
    const tmpOptions = { legend: this.getLegend(chart) }
    if (!tmpOptions.legend) {
      return { ...options, legend: false }
    }
    const { bullet } = parseJson(chart.customAttr).misc
    const { ranges } = bullet.bar
    const targetName =
      chart.yAxisExt[0]?.chartShowName || bullet.bar.target.name || chart.yAxisExt[0]?.name
    const measureName =
      chart.yAxis[0]?.chartShowName || bullet.bar.measures.name || chart.yAxis[0]?.name
    const rangeLegendLabelMap: Record<string, string> = {}
    const showRangeLegend = parseJson(chart.customStyle).legend?.showRange
    const getLegendKey = d => (typeof d === 'string' ? d : d?.id ?? d?.name ?? '')
    if (showRangeLegend) {
      if (bullet.bar.ranges.showType === 'dynamic') {
        const rangeName = chart.extBubble?.[0]?.chartShowName || chart.extBubble?.[0]?.name
        if (rangeName) {
          rangeLegendLabelMap[BULLET_DYNAMIC_RANGE_KEY] = rangeName
        }
      } else {
        ;(ranges.fixedRange || [])
          .sort((a, b) => (a.fixedRangeValue ?? 0) - (b.fixedRangeValue ?? 0))
          .forEach((item, index) => {
            rangeLegendLabelMap[getFixedRangeKey(index)] = item.name
          })
      }
    }
    const baseLegend = tmpOptions.legend ? (tmpOptions.legend as any) : {}
    const tmpLegend = {
      color: {
        ...baseLegend,
        itemMarkerSize: ranges.symbolSize,
        itemMarkerLineWidth: 2,
        itemMarker: d => {
          const key = getLegendKey(d)
          if (key === BULLET_TARGET_KEY) {
            return 'line'
          }
          return ranges.symbol
        },
        itemLabelText: d => {
          const key = getLegendKey(d)
          return (
            rangeLegendLabelMap[key] ||
            (key === BULLET_TARGET_KEY
              ? targetName
              : key === BULLET_MEASURE_KEY
              ? measureName
              : String(key))
          )
        }
      }
    }
    return { ...options, legend: tmpLegend }
  }

  protected configLabel(chart: Chart, options: RuntimeOptions): RuntimeOptions {
    const customAttr = parseJson(chart.customAttr)
    const { label: l } = customAttr
    // 如果没有启用标签，直接返回
    if (!l || !l.show) {
      return options
    }
    const basicStyle = parseJson(chart.customAttr).basicStyle
    const { layout } = basicStyle
    const labelPosition = getBulletLabelPosition(l.position, layout)
    const position = {
      position: labelPosition,
      textAlign: 'left',
      dy: labelPosition === 'top' ? -10 : 0,
      dx: 0
    }
    if (layout !== 'horizontal') {
      position.textAlign = 'center'
    }
    // contrastReverse 标签颜色在图形背景上对比度低的情况下，从指定色板选择一个对比度最优的颜色
    // overlapDodgeY 对位置碰撞的标签在 y 方向上进行调整，防止标签重叠
    // exceedAdjust 自动对标签做溢出检测和矫正，即当标签超出视图区域时，会对标签自动做反方向的位移
    // overlapHide 对位置碰撞的标签进行隐藏，默认保留前一个，隐藏后一个
    const transform = {
      transform: [{ type: 'exceedAdjust' }, { type: 'overlapHide' }]
    }
    // 配置标签样式
    const label = {
      text: 'measures',
      pointerEvents: 'none',
      fill: l.color,
      // 标签颜色按配置原值渲染，避免叠加 G2 默认透明度
      fillOpacity: 1,
      fontSize: l.fontSize,
      ...position,
      formatter: d => valueFormatter(d, l.labelFormatter),
      ...(l.fullDisplay ? {} : transform)
    }
    // 将标签配置应用到实际值条
    options.children?.forEach(item => {
      if (item.type === 'interval' && item.encode?.y === 'measures') {
        item.labels = [label]
      }
    })
    return options
  }

  private buildTooltipFormatterMap(tooltipAttr: any): Record<string, any> {
    const formatterMap: Record<string, any> = {}
    tooltipAttr.seriesTooltipFormatter
      ?.filter(
        i => i.show && ['-yAxis', '-yAxisExt', 'extBubble'].some(k => i.seriesId.includes(k))
      )
      .forEach(next => {
        switch (next.axisType) {
          case 'yAxis':
            formatterMap.measures = next
            break
          case 'yAxisExt':
            formatterMap.target = next
            break
          case 'extBubble':
            formatterMap.ranges = next
            break
          default:
            break
        }
      })
    return formatterMap
  }

  private formatTooltipValue(value: unknown, formatterCfg?: any): string {
    const safeFormatterCfg = formatterCfg ? { ...formatterCfg } : { ...formatterItem }
    if (value === null || value === undefined) {
      return ''
    }
    if (typeof value === 'string') {
      const trimmedValue = value.trim()
      if (!trimmedValue) {
        return ''
      }
      const numericValue = Number(trimmedValue)
      if (!Number.isFinite(numericValue)) {
        return ''
      }
      return valueFormatter(numericValue, safeFormatterCfg)
    }
    if (typeof value === 'number') {
      if (!Number.isFinite(value)) {
        return ''
      }
      return valueFormatter(value, safeFormatterCfg)
    }
    const normalizedValue = Number(value)
    if (!Number.isFinite(normalizedValue)) {
      return ''
    }
    return valueFormatter(normalizedValue, safeFormatterCfg)
  }

  private getTooltipMetricValue(chartData: any, field: string): any {
    if (field === 'measures') {
      return chartData?.tooltipMeasures ?? chartData?.measures
    }
    if (field === 'target') {
      return chartData?.tooltipTarget ?? chartData?.target
    }
    if (field === 'ranges') {
      return chartData?.tooltipRanges ?? chartData?.ranges
    }
    return chartData?.[field]
  }

  private buildTooltipRows(params: {
    chart: Chart
    options: RuntimeOptions
    tooltipAttr: any
    bullet: any
    formatterMap: Record<string, any>
    titleValue: string
  }): any[] {
    const { chart, options, tooltipAttr, bullet, formatterMap, titleValue } = params
    const hasSeriesFormatter = !!tooltipAttr.seriesTooltipFormatter?.length
    const isDynamic = bullet.bar.ranges.showType === 'dynamic'
    const rangeFormatter = chart.extBubble?.[0]
    const chartData = options.data?.find(item => item.title === titleValue)
    const showRangeLegend = parseJson(chart.customStyle).legend?.showRange
    const result = []

    const axisFormatterMap = {
      measures: chart.yAxis?.[0],
      target: chart.yAxisExt?.[0],
      ranges: chart.extBubble?.[0]
    }
    const visibleKeys = hasSeriesFormatter ? Object.keys(formatterMap) : ['measures', 'target']

    visibleKeys.forEach(field => {
      const formatter = formatterMap?.[field] ?? axisFormatterMap[field]
      if (!formatter) {
        return
      }

      let name = isEmpty(formatter.chartShowName) ? formatter.name : formatter.chartShowName
      let value = this.formatTooltipValue(
        this.getTooltipMetricValue(chartData, field),
        formatter.formatterCfg
      )
      let color = bullet.bar[field]?.fill ?? 'grey'

      if (field === 'ranges') {
        if (!showRangeLegend) {
          return
        }
        if (!isDynamic && rangeFormatter) {
          name = isEmpty(rangeFormatter.chartShowName)
            ? rangeFormatter.name
            : rangeFormatter.chartShowName
          value = this.formatTooltipValue(
            chartData?.tooltipMinRanges?.[0] ?? chartData?.minRanges?.[0],
            rangeFormatter.formatterCfg
          )
          color = 'grey'
        } else {
          return
        }
      }

      result.push({ color, name, value })
    })

    if (!hasSeriesFormatter) {
      return result
    }

    if (!showRangeLegend) {
      return result
    }

    const ranges = chartData?.ranges ?? []
    const rangeFormatterCfg = formatterMap['ranges']?.formatterCfg ?? rangeFormatter?.formatterCfg
    const shouldShowRanges = isDynamic ? Boolean(formatterMap['ranges']) : showRangeLegend
    if (shouldShowRanges) {
      ranges.forEach((range, index) => {
        const value = isDynamic
          ? this.formatTooltipValue(
              chartData?.tooltipMinRanges?.[0] ?? chartData?.minRanges?.[0],
              rangeFormatterCfg
            )
          : this.formatTooltipValue(range, rangeFormatterCfg)
        let name = ''
        let color: string | string[] = 'grey'
        if (isDynamic && rangeFormatter) {
          name = isEmpty(rangeFormatter.chartShowName)
            ? rangeFormatter.name
            : rangeFormatter.chartShowName
          color = bullet.bar.ranges.fill
        } else {
          const customRange = bullet.bar.ranges.fixedRange?.[index]
          name = customRange?.name
            ? customRange.name
            : isEmpty(rangeFormatter?.chartShowName)
            ? rangeFormatter?.name
            : rangeFormatter?.chartShowName
          color = customRange?.fill ?? 'grey'
        }
        result.push({ color, name, value })
      })
    }

    const dynamicTooltipValue =
      chart.data?.data?.find(d => d.field === titleValue)?.dynamicTooltipValue || []
    if (dynamicTooltipValue.length > 0) {
      dynamicTooltipValue.forEach(dy => {
        const formatter = tooltipAttr.seriesTooltipFormatter?.find(i => i.id === dy.fieldId)
        if (!formatter) {
          return
        }
        const value = this.formatTooltipValue(dy.value, formatter.formatterCfg)
        const name = isEmpty(formatter.chartShowName) ? formatter.name : formatter.chartShowName
        result.push({ color: 'grey', name, value })
      })
    }

    return result
  }

  private buildTooltipHtml(chart: Chart, titleValue: string, items: any[]): string {
    const titleHtml = TOOLTIP_TITLE_TPL.replace('{title}', titleValue)
    const itemsHtml = items
      .map(item => {
        const marker = [].concat(item.color as any)?.[0] ?? 'grey'
        return TOOLTIP_ITEM_TPL.replace('{marker}', marker)
          .replace('{label}', item.name ?? '')
          .replace('{value}', item.value ?? '')
      })
      .join('')
    const listHtml = `<ul class="g2-tooltip-list" style="${tooltipMaxHeight(
      chart
    )}margin: 0px; list-style-type: none; padding: 0px;">${itemsHtml}</ul>`
    return `${titleHtml}${listHtml}`
  }

  protected configTooltip(chart: Chart, options: RuntimeOptions): RuntimeOptions {
    const customAttr: DeepPartial<ChartAttr> = parseJson(chart.customAttr)
    const tooltipAttr = customAttr.tooltip
    const { bullet } = parseJson(chart.customAttr).misc
    if (!tooltipAttr.show) {
      return { ...options, tooltip: false }
    }
    const formatterMap = this.buildTooltipFormatterMap(tooltipAttr)

    return {
      ...options,
      tooltip: d => d,
      interaction: {
        ...options.interaction,
        tooltip: {
          mount: createTooltipWrapper(chart),
          css: tooltipCss(tooltipAttr),
          shared: true,
          series: true,
          enterable: true,
          render: (_, { title, items: originalItems }) => {
            if (!originalItems?.length) {
              return ''
            }
            const head: any = originalItems[0]
            const titleValue = (title as any) || head.title || head.data?.title || ''
            const rows = this.buildTooltipRows({
              chart,
              options,
              tooltipAttr,
              bullet,
              formatterMap,
              titleValue
            })
            rows.sort((a, b) => (a.color === 'grey' ? 1 : b.color === 'grey' ? -1 : 0))
            return this.buildTooltipHtml(chart, titleValue, rows)
          }
        }
      }
    }
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    chart.customAttr.label.position = 'middle'
    chart.customStyle.yAxis.splitLine.show = false
    chart.customAttr.misc.bullet.bar.ranges.symbolSize = 8
    chart.customAttr.misc.bullet.bar.target.symbolSize = 8
    chart.customAttr.misc.bullet.bar.measures.symbolSize = 8
    chart.customAttr.misc.bullet.bar.target.size = 8
    chart.customAttr.misc.bullet.bar.measures.symbol = 'square'
    chart.customAttr.misc.bullet.bar.ranges.symbol = 'square'
    return super.setupDefaultOptions(chart)
  }
  protected setupOptions(chart: Chart, options: RuntimeOptions): RuntimeOptions {
    return flow(
      this.configMisc,
      this.configBasicStyle,
      this.configAxis,
      this.configLabel,
      this.configLegend,
      this.configTooltip
    )(chart, options, {}, this)
  }
}

/**
 * 组装子弹图数据
 * @param chart
 */
function mergeBulletData(chart): any[] {
  // 先根据维度分组，再根据指标字段组装成子弹图的格式
  const groupedData = chart.data.data.reduce((acc, item) => {
    const field = item.field
    if (!acc[field]) {
      acc[field] = []
    }
    acc[field].push(item)
    return acc
  }, {})
  const result = []
  // 组装子弹图数据，每个维度对应一个子弹图
  Object.keys(groupedData).forEach(field => {
    const items = groupedData[field]
    // 初始化子弹图条目结构
    const entry = {
      title: field,
      // 保留公共联动匹配所需的维度语义，子弹图本身不区分系列维度
      name: field,
      category: 'NO_DATA',
      ranges: [],
      measures: [],
      target: [],
      hasValidMeasure: false,
      hasValidTarget: false,
      hasValidRange: false,
      dimensionList: items[0].dimensionList,
      quotaList: []
    }

    // 防止指标相同时无数据有可能会导致数据不一致
    items.forEach(item => {
      const quotaId = item.quotaList[0]?.id
      const rawValue = item.value
      let normalizedValue = NaN
      if (typeof rawValue === 'number') {
        normalizedValue = rawValue
      } else if (typeof rawValue === 'string') {
        const trimmedValue = rawValue.trim()
        normalizedValue = trimmedValue ? Number(trimmedValue) : NaN
      } else if (rawValue !== null && rawValue !== undefined) {
        normalizedValue = Number(rawValue)
      }
      const hasValidNumber = Number.isFinite(normalizedValue)
      const v = hasValidNumber ? normalizedValue : 0
      if (quotaId === chart.yAxis[0]?.id) {
        entry.measures.push(v)
        entry.hasValidMeasure = entry.hasValidMeasure || hasValidNumber
      }
      if (quotaId === chart.yAxisExt[0]?.id) {
        entry.target.push(v)
        entry.hasValidTarget = entry.hasValidTarget || hasValidNumber
      }
      if (quotaId === chart.extBubble[0]?.id) {
        entry.ranges.push(v)
        entry.hasValidRange = entry.hasValidRange || hasValidNumber
      }
      entry.quotaList.push(item.quotaList[0])
    })
    // 对数据进行累加
    const ranges = chart.extBubble[0]?.id
      ? [].concat(entry.ranges?.reduce((acc, curr) => acc + curr, 0))
      : []
    const target = [].concat(entry.target?.reduce((acc, curr) => acc + curr, 0))
    const measures = [].concat(entry.measures?.reduce((acc, curr) => acc + curr, 0))
    const bulletData = {
      ...entry,
      measures: measures,
      target: target,
      ranges: ranges,
      tooltipMeasures: entry.hasValidMeasure ? measures[0] : '',
      tooltipTarget: entry.hasValidTarget ? target[0] : '',
      tooltipRanges: entry.hasValidRange ? ranges[0] : '',
      quotaList: [...entry.quotaList],
      minRanges: ranges,
      tooltipMinRanges: entry.hasValidRange ? ranges : [''],
      originalRanges: ranges,
      originalTarget: target
    }
    result.push(bulletData)
  })
  return result
}

function getBulletLabelPosition(position: string, layout: string): string {
  if (position === 'middle') {
    return 'inside'
  }
  if (layout === 'horizontal') {
    if (position === 'top') {
      return 'right'
    }
    if (position === 'bottom') {
      return 'left'
    }
    return position
  }
  if (position === 'left') {
    return 'bottom'
  }
  if (position === 'right') {
    return 'top'
  }
  return position
}
