import { Box as G2Box, BoxOptions } from '@antv/g2plot/esm/plots/box'
import { OUTLIERS_VIEW_ID } from '@antv/g2plot/esm/plots/box/constant'
import {
  G2PlotChartView,
  G2PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/g2plot'
import {
  configPlotTooltipEvent,
  getPadding,
  getTooltipContainer
} from '@/views/chart/components/js/panel/common/common_antv'
import { BAR_EDITOR_PROPERTY_INNER } from '@/views/chart/components/js/panel/charts/bar/common'
import { cloneDeep } from 'lodash-es'
import {
  flow,
  hexColorToRGBA,
  parseJson,
  setUpGroupSeriesColor
} from '@/views/chart/components/js/util'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { useI18n } from '@/hooks/web/useI18n'
import { getItemsOfView } from '@antv/g2/lib/interaction/action/active-region'

const { t } = useI18n()
const DEFAULT_DATA = []
const OUTLIER_VALUES_FIELD = 'boxPlotOutlierValues'
// 分组头保持 G2 原有标记大小，仅缩小统计明细标记，不参与箱线图统计计算
const DETAIL_TOOLTIP_HEADER_MARKER_SIZE = 8
const DETAIL_TOOLTIP_ITEM_MARKER_SIZE = 4
const MAX_TOOLTIP_OUTLIER_VALUES = 10
// 使用 itemTpl 保留 DataEase 的唯一 tooltip 容器，避免 customContent 在连续悬浮时替换节点并留下残影
const BOX_PLOT_TOOLTIP_ITEM_TPL =
  '<li class="g2-tooltip-list-item" data-index={index} ' +
  'style="margin-top:{itemMarginTop}px;white-space:nowrap">' +
  // 不使用 g2-tooltip-marker 类，防止 G2 将父项和明细项统一覆盖为 8px
  '<span class="box-plot-tooltip-marker" ' +
  'style="background:{color};width:{markerSize}px;height:{markerSize}px;' +
  'border-radius:50%;display:inline-block;flex:0 0 auto;' +
  'margin-top:{markerMarginTop}px;' +
  'margin-left:{markerMarginLeft}px;margin-right:{markerMarginRight}px"></span>' +
  '<span class="g2-tooltip-name" style="font-weight:{nameFontWeight};white-space:nowrap">' +
  '{name}{nameSuffix}</span>' +
  '<span class="g2-tooltip-value" style="white-space:nowrap">{value}</span>' +
  '</li>'

type DataEaseBoxOptions = BoxOptions & {
  outlierColorMode?: ChartBasicStyle['outlierColorMode']
  outlierColor?: string
  outlierSize?: number
}

class DataEaseBox extends G2Box {
  protected execAdaptor(): void {
    super.execAdaptor()
    if (this.options.legend === false) {
      this.chart.legend(false)
    }
    const groupField = this.options.groupField
    const outliersView = this.chart.views.find(view => view.id === OUTLIERS_VIEW_ID)
    const geometry = outliersView?.geometries?.[0]
    if (!outliersView || !geometry) {
      return
    }
    const sourceData = this.options.data ?? []
    const dataEaseOptions = this.options as DataEaseBoxOptions
    const categoryValues = [...new Set(sourceData.map(datum => datum[this.options.xField]))]
    // 异常点子视图只包含有异常值的类别，必须复用主图完整类别域，否则单个类别会被画到绘图区中央
    outliersView.scale(this.options.xField, { type: 'cat', values: categoryValues })
    // 点大小属于 geometry 映射而不是 ShapeStyle，需要在 G2Plot 创建异常点子视图后设置
    geometry.size(dataEaseOptions.outlierSize ?? 4)
    if (dataEaseOptions.outlierColorMode === 'custom' && dataEaseOptions.outlierColor) {
      geometry.color(dataEaseOptions.outlierColor)
    } else if (!groupField) {
      // 无分组时显式复用箱体颜色，避免异常点退回 G2 默认色
      const color = Array.isArray(this.options.color) ? this.options.color[0] : this.options.color
      if (typeof color === 'string') {
        geometry.color(color)
      }
    }
    if (!groupField) {
      return
    }
    const groupValues = [...new Set(sourceData.map(datum => datum[groupField]))]
    // 分组尺度也要与主箱体保持同一顺序，确保 dodge 后的异常点落在对应分组箱体中心
    outliersView.scale(groupField, { type: 'cat', values: groupValues })
    // G2Plot 2.4 的异常点子视图不会继承分组 dodge，需与箱体使用相同分组映射
    if (dataEaseOptions.outlierColorMode === 'custom' && dataEaseOptions.outlierColor) {
      geometry.color(dataEaseOptions.outlierColor)
    } else if (this.options.color) {
      geometry.color(groupField, this.options.color)
    } else {
      geometry.color(groupField)
    }
    geometry.adjust('dodge')
    outliersView.legend(false)
  }
}

export class BoxPlot extends G2PlotChartView<BoxOptions, G2Box> {
  properties: EditorProperty[] = [
    'background-overall-component',
    'border-style',
    'basic-style-selector',
    'tooltip-selector',
    'x-axis-selector',
    'y-axis-selector',
    'title-selector',
    'legend-selector',
    'jump-set',
    'linkage'
  ]

  propertyInner: EditorPropertyInner = {
    ...BAR_EDITOR_PROPERTY_INNER,
    'basic-style-selector': [
      'colors',
      'alpha',
      'seriesColor',
      'showOutliers',
      'outlierColorMode',
      'outlierColor',
      'outlierSize'
    ],
    'tooltip-selector': [
      'fontSize',
      'color',
      'backgroundColor',
      'tooltipFormatter',
      'showBoxPlotDetails',
      'show'
    ],
    'x-axis-selector': [...BAR_EDITOR_PROPERTY_INNER['x-axis-selector'], 'showLengthLimit'],
    'y-axis-selector': [...BAR_EDITOR_PROPERTY_INNER['y-axis-selector'], 'axisLabelFormatter'],
    'legend-selector': BAR_EDITOR_PROPERTY_INNER['legend-selector']
  }

  axis: AxisType[] = ['xAxis', 'xAxisExt', 'yAxis', 'filter', 'drill']

  axisConfig: AxisConfig = {
    xAxis: {
      name: `${t('chart.drag_block_type_axis')} / ${t('chart.dimension')}`,
      type: 'd',
      limit: 1,
      allowEmpty: false,
      tooltip: t('chart.box_plot_category_tip')
    },
    xAxisExt: {
      name: `${t('chart.chart_group')} / ${t('chart.dimension')}`,
      type: 'd',
      limit: 1,
      allowEmpty: true,
      tooltip: t('chart.box_plot_group_tip')
    },
    yAxis: {
      name: `${t('chart.drag_block_value_axis')} / ${t('chart.quota')}`,
      type: 'q',
      limit: 1,
      allowEmpty: false,
      tooltip: t('chart.box_plot_value_tip')
    }
  }

  async drawChart(drawOptions: G2PlotDrawOptions<G2Box>): Promise<G2Box> {
    const { chart, container, action } = drawOptions
    chart.container = container
    if (!chart?.data?.data?.length) {
      return undefined as unknown as G2Box
    }
    const data = cloneDeep(chart.data.data).map(datum => ({
      ...datum,
      // G2Plot 会在异常点子视图中把 outliers 数组替换成当前单值，单独保留完整列表供统一 tooltip 使用
      [OUTLIER_VALUES_FIELD]: Array.isArray(datum.outliers) ? [...datum.outliers] : []
    }))
    // 子类别字段存在但全部为空时按无分组绘制，避免 G2Plot 为无内容图例保留布局空间
    const hasGroup =
      !!chart.xAxisExt?.length &&
      data.some(
        datum =>
          datum.category !== null &&
          datum.category !== undefined &&
          String(datum.category).trim() !== ''
      )
    const initOptions: BoxOptions = {
      appendPadding: getPadding(chart),
      data,
      xField: 'field',
      yField: ['low', 'q1', 'median', 'q3', 'high'],
      groupField: hasGroup ? 'category' : undefined,
      outliersField: 'outliers',
      meta: {
        field: { type: 'cat' },
        ...(hasGroup ? { category: { type: 'cat' } } : {})
      }
    }
    const options = this.setupOptions(chart, initOptions)
    const plot = new DataEaseBox(container, options)

    const normalizeAction = (event, datum = event?.data?.data) => {
      if (!datum) {
        return
      }
      action?.({
        ...event,
        // G2 的坐标是原型 getter，对象展开不会复制，需显式传给联动菜单定位逻辑
        x: event.x,
        y: event.y,
        data: {
          data: {
            ...datum,
            value: Array.isArray(datum.outliers) ? datum.median : datum.outliers ?? datum.median
          }
        }
      })
    }
    plot.on('schema:click', normalizeAction)
    plot.on('point:click', normalizeAction)
    if (options.tooltip) {
      plot.on('plot:click', event => {
        if (event.target?.cfg?.renderer !== 'canvas') {
          return
        }
        const view = event.view
        const activeRegion = view?.backgroundGroup?.cfg?.children?.find(
          item => item.cfg.name === 'active-region'
        )
        if (!activeRegion?.cfg.visible) {
          return
        }
        // 维度背景没有图形 datum，通过当前 active-region 对应的 tooltip 项恢复点击数据
        const items = getItemsOfView(
          view,
          { x: event.x, y: event.y },
          view.getController('tooltip').getTooltipCfg()
        )
        const datum = items?.[0]?.data
        if (datum?.field) {
          normalizeAction(event, datum)
        }
      })
    }
    configPlotTooltipEvent(chart, plot as any)
    return plot
  }

  protected configColor(chart: Chart, options: BoxOptions): BoxOptions {
    return options.groupField
      ? this.configGroupColor(chart, options)
      : super.configColor(chart, options)
  }

  protected configBasicStyle(chart: Chart, options: BoxOptions): BoxOptions {
    const customAttr = parseJson(chart.customAttr)
    const basicStyle = customAttr.basicStyle
    const stroke = basicStyle.themeContrastColor ?? customAttr.label?.color ?? '#000000'
    const configuredColors = Array.isArray(options.color)
      ? options.color
      : typeof options.color === 'string'
      ? [options.color]
      : basicStyle.colors.map(color => hexColorToRGBA(color, basicStyle.alpha))
    const groupValues = [...new Set((options.data ?? []).map(datum => datum[options.groupField]))]
    // G2Plot 默认把系列色用于箱体描边，这里改为系列色填充并使用主题反色绘制全部统计线
    const boxStyle = datum => {
      const groupIndex = options.groupField
        ? groupValues.findIndex(value => value === datum[options.groupField])
        : 0
      const colorIndex = Math.max(groupIndex, 0) % configuredColors.length
      return {
        fill: configuredColors[colorIndex],
        stroke,
        lineWidth: 1
      }
    }
    return {
      ...options,
      outlierColorMode: basicStyle.outlierColorMode,
      outlierColor: basicStyle.outlierColor,
      outlierSize: basicStyle.outlierSize,
      // 隐藏异常点只影响展示，四分位数、须线和异常值数量的统计口径保持不变
      outliersField: basicStyle.showOutliers === false ? undefined : options.outliersField,
      boxStyle,
      outliersStyle: {
        lineWidth: 1
      }
    } as DataEaseBoxOptions
  }

  protected configTooltip(chart: Chart, options: BoxOptions): BoxOptions {
    const tooltipAttr = parseJson(chart.customAttr).tooltip
    if (!tooltipAttr?.show) {
      return { ...options, tooltip: false }
    }
    const groupAxis = options.groupField ? chart.xAxisExt?.[0] : undefined
    const valueAxis = chart.yAxis?.[0]
    const metricName = valueAxis?.chartShowName || valueAxis?.name || t('chart.quota')
    const showDetails = tooltipAttr.showBoxPlotDetails === true
    const tooltipFontSize = Number(tooltipAttr.fontSize) || 12
    const labelMap = {
      low: t('chart.box_plot_low'),
      q1: t('chart.box_plot_q1'),
      median: t('chart.box_plot_median'),
      quartileRange: t('chart.box_plot_quartile_range'),
      q3: t('chart.box_plot_q3'),
      high: t('chart.box_plot_high'),
      outliers: t('chart.box_plot_outlier'),
      outlierCount: t('chart.box_plot_outlier_count')
    }
    const summaryFields = ['low', 'q1', 'median', 'q3', 'high']
    return {
      ...options,
      tooltip: {
        fields: [...summaryFields, 'count', 'outlierCount'],
        // 始终按类别共享提示，使退化为横线的箱体也能通过整段类别背景稳定触发
        shared: true,
        // 类别维度值使用 G2Plot 标题展示，避免在内容区重复显示字段名称
        showTitle: true,
        // DataEase 使用自定义 tooltip 容器，跟随鼠标可避免固定方位产生页面级坐标偏移
        follow: true,
        showMarkers: false,
        customItems: items => {
          const uniqueItems = []
          const summaryKeys = new Set<string>()
          items.forEach(item => {
            const datum = item?.data
            if (!datum) {
              return
            }
            // schema 原始字段和多个异常点可能产生重复 item，每个类别与分组只保留一份统计摘要
            const key = JSON.stringify([datum.field, datum.category ?? null])
            if (!summaryKeys.has(key)) {
              summaryKeys.add(key)
              uniqueItems.push(item)
            }
          })
          if (!uniqueItems.length) {
            return []
          }
          const formatMetricValue = value =>
            valueFormatter(Number(value), tooltipAttr.tooltipFormatter)
          const createItem = (
            sourceItem,
            name,
            value,
            formatValue = true,
            extraProperties = {}
          ) => ({
            ...sourceItem,
            name,
            value: formatValue ? formatMetricValue(value) : String(value),
            ...extraProperties
          })
          const buildSummaryItems = sourceItem => {
            const datum = sourceItem.data
            const outlierValues = Array.isArray(datum[OUTLIER_VALUES_FIELD])
              ? datum[OUTLIER_VALUES_FIELD]
              : []
            const outlierCount = datum.outlierCount ?? outlierValues.length
            const formattedOutlierValues = outlierValues
              .slice(0, MAX_TOOLTIP_OUTLIER_VALUES)
              .map(formatMetricValue)
            const remainingOutlierCount = outlierValues.length - formattedOutlierValues.length
            if (remainingOutlierCount > 0) {
              formattedOutlierValues.push(
                t('chart.box_plot_more_outliers', { count: remainingOutlierCount })
              )
            }
            const outlierItem = outlierValues.length
              ? createItem(sourceItem, labelMap.outliers, formattedOutlierValues.join(', '), false)
              : undefined

            // 分组场景用子类别值作为父项；无分组时用指标名作为父项
            const headerName =
              groupAxis && datum.category !== null && datum.category !== undefined
                ? datum.category
                : metricName
            const sampleCount = t('chart.box_plot_samples', { count: datum.count })
            // 样本量提升到分组头，填补无分组标题空白并避免在统计明细中重复展示
            const headerValue = groupAxis ? `${metricName} · ${sampleCount}` : sampleCount
            const headerItem = createItem(sourceItem, headerName, headerValue, false, {
              boxPlotHeader: true
            })

            // 简洁模式保留核心分布摘要和异常值明细，便于直接定位异常数据
            if (!showDetails) {
              return [
                headerItem,
                createItem(sourceItem, labelMap.median, datum.median),
                createItem(
                  sourceItem,
                  labelMap.quartileRange,
                  `${formatMetricValue(datum.q1)} – ${formatMetricValue(datum.q3)}`,
                  false
                ),
                createItem(sourceItem, labelMap.outlierCount, outlierCount, false),
                ...(outlierItem ? [outlierItem] : [])
              ]
            }

            return [
              headerItem,
              ...summaryFields.map(field => createItem(sourceItem, labelMap[field], datum[field])),
              createItem(sourceItem, labelMap.outlierCount, outlierCount, false),
              ...(outlierItem ? [outlierItem] : [])
            ]
          }

          const resultItems = uniqueItems.flatMap(buildSummaryItems)
          let headerIndex = 0
          return resultItems.map(item => {
            const isHeader = item.boxPlotHeader === true
            const currentHeaderIndex = isHeader ? headerIndex++ : headerIndex
            const markerSize = isHeader
              ? DETAIL_TOOLTIP_HEADER_MARKER_SIZE
              : DETAIL_TOOLTIP_ITEM_MARKER_SIZE
            return {
              ...item,
              markerSize,
              // 与基础图表一致，按当前字号和实际标记尺寸动态计算垂直间距
              markerMarginTop: Math.max((tooltipFontSize - markerSize) / 2, 0),
              markerMarginLeft: isHeader ? 0 : 2,
              markerMarginRight: isHeader ? 5 : 9,
              itemMarginTop: isHeader && currentHeaderIndex > 0 ? 12 : 0,
              nameFontWeight: isHeader ? 500 : 400,
              nameSuffix: isHeader ? '' : ':'
            }
          })
        },
        container: getTooltipContainer(`tooltip-${chart.id}`, chart.container),
        itemTpl: BOX_PLOT_TOOLTIP_ITEM_TPL,
        // 公共事件会在提示显示后冻结位置，允许移入提示框查看较长的详情内容
        enterable: true
      }
    }
  }

  protected configYAxis(chart: Chart, options: BoxOptions): BoxOptions {
    const result = super.configYAxis(chart, options)
    if (!result.yAxis) {
      return result
    }
    const yAxis = parseJson(chart.customStyle).yAxis
    if (result.yAxis.label) {
      result.yAxis.label.formatter = value =>
        valueFormatter(Number(value), yAxis?.axisLabelFormatter)
    }
    const axisValue = yAxis.axisValue
    if (!axisValue?.auto) {
      const manualScale = {
        min: axisValue.min,
        max: axisValue.max,
        minLimit: axisValue.min,
        maxLimit: axisValue.max
      }
      return {
        ...result,
        yAxis: {
          ...result.yAxis,
          ...manualScale,
          tickCount: axisValue.splitCount
        },
        meta: {
          ...result.meta,
          // 异常点使用独立 scale，同步相同边界才能避免它把手动范围重新扩回数据范围
          outliers: {
            ...result.meta?.outliers,
            ...manualScale
          }
        }
      }
    }
    return result
  }

  protected setupOptions(chart: Chart, options: BoxOptions): BoxOptions {
    return flow(
      this.configTheme,
      this.configColor,
      this.configBasicStyle,
      this.configTooltip,
      this.configLegend,
      this.configXAxis,
      this.configYAxis
    )(chart, options, {}, this)
  }

  public setupSeriesColor(chart: ChartObj, data?: any[]): ChartBasicStyle['seriesColor'] {
    return setUpGroupSeriesColor(chart, data)
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    chart.customAttr.tooltip.showBoxPlotDetails ??= true
    return chart
  }

  constructor() {
    super('box-plot', DEFAULT_DATA)
  }
}
