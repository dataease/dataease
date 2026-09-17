import { Chart as G2Chart } from '@antv/g2'
import { cloneDeep, defaultsDeep, escape } from 'lodash-es'
import { useI18n } from '@/hooks/web/useI18n'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import {
  flow,
  getColorFormAlphaColor,
  hexColorToRGBA,
  parseJson,
  setUpGroupSeriesColor
} from '@/views/chart/components/js/util'
import { Bar } from '@/views/chart/components/js/panel/charts/g2/bar/bar'
import { BAR_EDITOR_PROPERTY_INNER } from '@/views/chart/components/js/panel/charts/g2/bar/common'
import { G2DrawOptions } from '@/views/chart/components/js/panel/types/impl/g2'
import {
  getG2Renderer,
  handleChartDashboardHidden,
  TOOLTIP_TITLE_TPL
} from '@/views/chart/components/js/panel/common/common_antv'
import {
  bindPlotBackgroundClick,
  createTooltipWrapper,
  ChildSpec,
  tooltipCss,
  ViewSpec
} from '@/views/chart/components/js/panel/charts/g2/bar/barUtil'
import { DEFAULT_BASIC_STYLE, DEFAULT_TOOLTIP } from '@/views/chart/components/editor/util/chart'

const { t } = useI18n()
const DEFAULT_DATA = []
const MAX_TOOLTIP_OUTLIER_VALUES = 10
const DETAIL_TOOLTIP_HEADER_MARKER_SIZE = 8
const DETAIL_TOOLTIP_ITEM_MARKER_SIZE = 4
const BOX_SERIES_FIELD = '__boxPlotSeries'
const BOX_CATEGORY_FIELD = '__boxPlotCategory'
const BOX_SUMMARY_FIELD = '__boxPlotSummary'
const BOX_TOOLTIP_HIT_MARK_KEY = '__boxPlotTooltipHitMark'
const BOX_DIMENSION_HIT_MARK_KEY = '__boxPlotDimensionHitMark'
const BOX_DIMENSION_HIT_FIELD = '__boxPlotDimensionHit'
type BoxPlotSeriesColor = ChartBasicStyle['seriesColor'][number] & { boxPlotSeries?: true }

// 编码仅供 G2 分类域使用，原始维度仍用于联动，避免 null、空字符串和文字 NULL 混为一组
const dimensionKey = (value: unknown): string => JSON.stringify([value ?? null])
const dimensionLabel = (value: unknown): string =>
  value === null || value === undefined
    ? 'NULL'
    : value === ''
    ? t('chart.filter_empty')
    : value === 'NULL'
    ? '"NULL"'
    : String(value)
const keyLabel = (key: string): string => dimensionLabel(JSON.parse(key)[0])
const dimensionDomain = (values: unknown[], customSort = false): string[] => {
  const domain = Array.from(new Set(values.map(dimensionKey)))
  // 自定义排序已在查询层执行，不能再将用户指定位置的空类别移走
  if (customSort) {
    return domain
  }
  const isEmpty = (key: string) => {
    const value = JSON.parse(key)[0]
    return value === null || value === ''
  }
  // 只将空分类移到末尾，非空分类沿用查询结果的相对顺序
  return [...domain.filter(key => !isEmpty(key)), ...domain.filter(isEmpty)]
}

export const restoreBoxPlotSeriesColors = (
  seriesColors: ChartBasicStyle['seriesColor']
): ChartBasicStyle['seriesColor'] =>
  seriesColors.flatMap((item: BoxPlotSeriesColor) => {
    if (!item.boxPlotSeries) {
      return [item]
    }
    const value = JSON.parse(item.id)[0]
    // 公共配色列表不支持 NULL 项，不能把它转成文本键而覆盖真实类别的颜色
    if (value === null) {
      return []
    }
    return [{ id: value, name: String(value), color: item.color }]
  })

const normalizeCustomAttr = (customAttr: CustomAttr): ChartAttr => {
  const tooltip = cloneDeep(DEFAULT_TOOLTIP)
  tooltip.showBoxPlotDetails = true
  return defaultsDeep(cloneDeep(parseJson(customAttr) ?? {}), {
    basicStyle: cloneDeep(DEFAULT_BASIC_STYLE),
    tooltip
  }) as ChartAttr
}

export class BoxPlot extends Bar {
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

  propertyInner = {
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
    'label-selector': BAR_EDITOR_PROPERTY_INNER['label-selector'],
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

  async drawChart(drawOptions: G2DrawOptions<G2Chart>): Promise<G2Chart> {
    const { chart, container, action } = drawOptions
    // 历史图表或类型切换可能缺少样式配置，渲染前为箱线图补齐默认值
    chart.customAttr = normalizeCustomAttr(chart.customAttr)
    chart.container = container
    if (!chart?.data?.data?.length) {
      return undefined as unknown as G2Chart
    }

    const metricName = chart.yAxis?.[0]?.chartShowName || chart.yAxis?.[0]?.name || t('chart.quota')
    const sourceData = cloneDeep(chart.data.data)
    const hasGroup = !!chart.xAxisExt?.length
    const boxData = sourceData.map(datum => ({
      ...datum,
      [BOX_CATEGORY_FIELD]: dimensionKey(datum.field),
      [BOX_SERIES_FIELD]: hasGroup ? dimensionKey(datum.category) : metricName
    }))
    const seriesDomain = hasGroup
      ? dimensionDomain(
          sourceData.map(item => item.category),
          chart.xAxisExt[0].sort === 'custom_sort'
        )
      : [metricName]
    const categoryAxis =
      (chart.drill && chart.drillFields?.[chart.drillFilters?.length ?? 0]) || chart.xAxis?.[0]
    const categoryDomain = dimensionDomain(
      sourceData.map(item => item.field),
      categoryAxis?.sort === 'custom_sort'
    )
    const categoryOrder = new Map(categoryDomain.map((key, index) => [key, index]))
    // 追踪线的首尾坐标必须与显示顺序一致，空分类置后时也不能保留旧数据顺序
    boxData.sort(
      (left, right) =>
        categoryOrder.get(left[BOX_CATEGORY_FIELD]) - categoryOrder.get(right[BOX_CATEGORY_FIELD])
    )
    let visibleBoxData = boxData
    const layoutTransform = { type: 'dataeaseBoxPlotLayout' }
    const boxPlotLayout =
      () =>
      (index: number[], mark): [number[], any] => {
        // G2 图例会先插入 filter；每次重绘按其可见系列重算位置，颜色域保持完整
        const colorFilter = mark.transform?.find(
          item => item.type === 'filter' && item.color
        )?.color
        const selected = colorFilter?.value
        const visibleDomain = Array.isArray(selected)
          ? seriesDomain.filter(value => selected.includes(value))
          : seriesDomain
        if (mark.type === 'box') {
          visibleBoxData = index.map(i => mark.data[i])
        }
        return [
          index,
          {
            ...mark,
            scale: {
              ...mark.scale,
              series: { ...mark.scale?.series, domain: visibleDomain }
            }
          }
        ]
      }
    const outlierData = boxData.flatMap(datum =>
      (Array.isArray(datum.outliers) ? datum.outliers : []).map(outlier => ({
        ...datum,
        outlier,
        value: outlier,
        [BOX_SUMMARY_FIELD]: datum
      }))
    )

    // 后端已经给出五数统计，必须使用 box 标记直接绘制，不能使用会在前端重新计算分位数的 boxplot 标记。
    const boxMark: ChildSpec = {
      type: 'box',
      data: boxData,
      encode: {
        x: BOX_CATEGORY_FIELD,
        y: 'low',
        y1: 'q1',
        y2: 'median',
        y3: 'q3',
        y4: 'high',
        color: BOX_SERIES_FIELD,
        series: hasGroup ? BOX_SERIES_FIELD : undefined
      },
      scale: {
        x: { type: 'band' },
        series: { type: 'band', domain: seriesDomain },
        y: { nice: true }
      },
      transform: [layoutTransform],
      state: {
        active: { backgroundPointerEvents: 'none' },
        unselected: { opacity: 0.5 }
      },
      interaction: {
        // box 不参与 G2 的区域命中计算，由 tooltip 当前 datum 精确驱动维度背景
        elementHighlight: {
          background: true
        }
      }
    }
    // 异常点单独展开为 point 标记，数据规模由用户的结果展示配置控制
    const pointMark: ChildSpec = {
      type: 'point',
      data: outlierData,
      encode: {
        x: BOX_CATEGORY_FIELD,
        y: 'outlier',
        color: BOX_SERIES_FIELD,
        series: hasGroup ? BOX_SERIES_FIELD : undefined,
        size: 4
      },
      // G2 point 默认使用空心形状，异常点固定为实心圆
      style: {
        shape: 'point',
        fillOpacity: 1
      },
      transform: [layoutTransform, ...(hasGroup ? [{ type: 'dodgeX' }] : [])],
      // 异常点与箱体使用相同可见系列域，不能从稀疏异常点独立推导分组位置
      scale: {
        series: { type: 'band', domain: seriesDomain }
      }
    }
    const tooltipHitMark: ChildSpec = {
      key: BOX_TOOLTIP_HIT_MARK_KEY,
      type: 'line',
      data: boxData,
      encode: {
        x: BOX_CATEGORY_FIELD,
        y: 'median',
        series: BOX_SERIES_FIELD,
        // 追踪线虽然不可见，仍需沿用分组色标，tooltip marker 才能与箱体颜色一致。
        color: BOX_SERIES_FIELD
      },
      style: {
        strokeOpacity: 0,
        pointerEvents: 'none'
      },
      transform: [layoutTransform],
      animate: false
    }
    // 每个系列保留维度锚点，随图例筛选同步移除，避免无 color 字段的锚点被全部过滤
    const dimensionHitData = boxData.map(datum => ({
      field: datum.field,
      [BOX_CATEGORY_FIELD]: datum[BOX_CATEGORY_FIELD],
      [BOX_SERIES_FIELD]: datum[BOX_SERIES_FIELD],
      median: datum.median,
      [BOX_DIMENSION_HIT_FIELD]: true
    }))
    const dimensionHitMark: ChildSpec = {
      key: BOX_DIMENSION_HIT_MARK_KEY,
      type: 'point',
      data: dimensionHitData,
      encode: {
        x: BOX_CATEGORY_FIELD,
        y: 'median',
        color: BOX_SERIES_FIELD
      },
      // 锚点保持在维度中心，使 seriesTooltip 的最近 X 命中覆盖完整 Band
      style: {
        fillOpacity: 0,
        strokeOpacity: 0,
        pointerEvents: 'none'
      },
      animate: false
    }
    const baseOptions: ViewSpec = {
      type: 'view',
      data: boxData,
      autoFit: true,
      scale: {
        x: { type: 'band', domain: categoryDomain },
        color: { domain: seriesDomain }
      },
      // 透明 line 复用 seriesTooltip 的最近 X 命中，避免 tooltip 受 box 几何边界限制
      children: [boxMark, pointMark, tooltipHitMark, dimensionHitMark]
    }
    const options = this.setupOptions(chart, baseOptions)
    const newChart = new G2Chart({
      container,
      autoFit: true,
      ...getG2Renderer()
    })
    // G2 Chart 构造器会覆盖传入的 lib，布局转换必须注册到当前实例的实际运行上下文
    newChart.getContext().library['transform.dataeaseBoxPlotLayout'] = boxPlotLayout
    handleChartDashboardHidden(chart, options)
    newChart.options(options)

    const normalizeAction = event => {
      const datum = event?.data?.data
      if (!datum) {
        return
      }
      const summary = datum[BOX_SUMMARY_FIELD] ?? datum
      action?.({
        ...event,
        // 坐标由事件对象的原型提供，对象展开不会复制，需要显式保留供联动菜单定位。
        x: event.x,
        y: event.y,
        data: {
          data: {
            // 联动会补充日期范围和指标值，独立载荷避免修改箱体与异常点共享的数据
            ...cloneDeep(summary),
            value: datum.outlier ?? summary.median
          }
        }
      })
    }
    newChart.on('box:click', normalizeAction)
    newChart.on('point:click', normalizeAction)
    // 将维度背景点击映射到最近箱体，复用图形的联动和下钻事件
    bindPlotBackgroundClick(newChart, { markTypes: ['box'] })
    let highlightedField: string | undefined
    newChart.on('tooltip:show', event => {
      const datum = event?.data?.data
      const summary =
        datum?.[BOX_SUMMARY_FIELD] ??
        (datum?.field !== undefined
          ? datum
          : visibleBoxData.find(item => item[BOX_CATEGORY_FIELD] === datum?.x))
      const field = summary?.[BOX_CATEGORY_FIELD]
      if (field !== undefined && field !== highlightedField) {
        highlightedField = field
        // seriesTooltip 事件只返回 X 值，映射回箱体 datum 后再精确驱动维度背景
        newChart.emit('element:highlight', {
          nativeEvent: false,
          data: { data: summary }
        })
      }
    })
    newChart.on('tooltip:hide', () => {
      if (highlightedField === undefined) {
        return
      }
      highlightedField = undefined
      newChart.emit('element:unhighlight', { nativeEvent: false })
    })
    newChart.on('beforepaint', () => {
      // 图例筛选和缩放都会重新布局，清除旧命中位置对应的提示与维度背景
      newChart.emit('tooltip:hide', { nativeEvent: false })
      highlightedField = undefined
      newChart.emit('element:unhighlight', { nativeEvent: false })
    })
    this.configLengthLimitTooltip(chart, newChart)
    return newChart
  }

  protected configBasicStyle(chart: Chart, options: ViewSpec): ViewSpec {
    const basicStyle = parseJson(chart.customAttr).basicStyle
    const [boxMark, pointMark, ...interactionMarks] = options.children
    const strokeColor =
      basicStyle.themeContrastColor ?? parseJson(chart.customAttr).label?.color ?? '#000000'
    const stroke = hexColorToRGBA(getColorFormAlphaColor(strokeColor), basicStyle.alpha)
    const configuredPointColor =
      basicStyle.outlierColorMode === 'custom' && basicStyle.outlierColor
        ? // 兼容历史 rgba 或 8 位 hex，去除原有 alpha 后统一使用基础样式的不透明度
          hexColorToRGBA(getColorFormAlphaColor(basicStyle.outlierColor), basicStyle.alpha)
        : undefined
    const nextPointMark = {
      ...pointMark,
      encode: {
        ...pointMark.encode,
        size: Number(basicStyle.outlierSize) || 4
      },
      style: configuredPointColor
        ? { ...pointMark.style, fill: configuredPointColor, stroke: configuredPointColor }
        : pointMark.style
    }
    return {
      ...options,
      children: [
        {
          ...boxMark,
          state: {
            ...boxMark.state,
            active: { ...boxMark.state?.active, stroke },
            selected: { ...boxMark.state?.selected, stroke }
          },
          style: {
            ...boxMark.style,
            stroke,
            lineWidth: 1
          }
        },
        // 隐藏异常点只改变展示，不改变后端计算得到的四分位数和真实异常值数量。
        ...(basicStyle.showOutliers === false ? [] : [nextPointMark]),
        ...interactionMarks
      ]
    }
  }

  protected configTooltip(chart: Chart, options: ViewSpec): ViewSpec {
    const tooltipAttr = parseJson(chart.customAttr).tooltip
    if (!tooltipAttr?.show) {
      return {
        ...options,
        children: options.children.map(child => ({ ...child, tooltip: false }))
      }
    }
    const valueAxis = chart.yAxis?.[0]
    const metricName = valueAxis?.chartShowName || valueAxis?.name || t('chart.quota')
    const hasGroup = !!chart.xAxisExt?.length
    const groupAxis = hasGroup ? chart.xAxisExt[0] : undefined
    const showDetails = tooltipAttr.showBoxPlotDetails === true
    const tooltipFontSize = Number(tooltipAttr.fontSize) || 12
    // V2 tooltip 默认使用 20px 行高和 4px 行间距，字号变大时再同步扩展行高
    const tooltipSeriesIndex = new Map(
      (options.scale?.color?.domain ?? []).map((series, index) => [String(series), index])
    )
    const formatValue = value =>
      value === null || value === undefined
        ? ''
        : valueFormatter(Number(value), tooltipAttr.tooltipFormatter)
    const tooltipInteraction = {
      mount: createTooltipWrapper(chart),
      css: tooltipCss(tooltipAttr),
      enterable: true,
      shared: true,
      // 透明 line 仅用于扩大命中区域，不展示其默认十字辅助线
      crosshairs: false,
      crosshairsX: false,
      crosshairsY: false,
      marker: false,
      render: (_, { items }) => {
        const summaries: Array<{ sourceItem: any; sourceData: any }> = []
        const summaryKeys = new Set<string>()
        items?.forEach(sourceItem => {
          // G2 会把 tooltip 回调返回的 datum 平铺到 item，异常点再通过摘要字段回到所属箱体
          const sourceData = sourceItem?.[BOX_SUMMARY_FIELD] ?? sourceItem
          // 维度锚点只开启 Band 最近命中，不参与 tooltip 内容
          if (!sourceData || sourceData[BOX_DIMENSION_HIT_FIELD]) {
            return
          }
          const key = JSON.stringify([sourceData.field, sourceData.category ?? null])
          if (!summaryKeys.has(key)) {
            summaryKeys.add(key)
            summaries.push({ sourceItem, sourceData })
          }
        })
        if (!summaries.length) {
          return ''
        }
        // G2 按图形元素顺序返回共享 tooltip，显式按 color domain 与图例保持同序
        summaries.sort((left, right) => {
          const leftIndex = tooltipSeriesIndex.get(String(left.sourceData[BOX_SERIES_FIELD]))
          const rightIndex = tooltipSeriesIndex.get(String(right.sourceData[BOX_SERIES_FIELD]))
          return (leftIndex ?? Number.MAX_SAFE_INTEGER) - (rightIndex ?? Number.MAX_SAFE_INTEGER)
        })

        // 按 V2 结构区分系列头和统计明细，并在共享提示中去除箱体与异常点产生的重复项
        const itemsHtml = summaries
          .flatMap(({ sourceItem, sourceData }) => {
            const marker = String(sourceItem.color ?? '#5470C6')
            const sampleCount = t('chart.box_plot_samples', { count: sourceData.count })
            const headerLabel = groupAxis ? dimensionLabel(sourceData.category) : metricName
            const rows: Array<{
              label: string
              value: string
              header?: boolean
              marker: string
            }> = [
              {
                label: escape(String(headerLabel)),
                value: groupAxis ? `${metricName} · ${sampleCount}` : sampleCount,
                header: true,
                marker
              }
            ]
            if (showDetails) {
              rows.push(
                { label: t('chart.box_plot_low'), value: formatValue(sourceData.low), marker },
                { label: t('chart.box_plot_q1'), value: formatValue(sourceData.q1), marker },
                {
                  label: t('chart.box_plot_median'),
                  value: formatValue(sourceData.median),
                  marker
                },
                { label: t('chart.box_plot_q3'), value: formatValue(sourceData.q3), marker },
                { label: t('chart.box_plot_high'), value: formatValue(sourceData.high), marker }
              )
            } else {
              rows.push(
                {
                  label: t('chart.box_plot_median'),
                  value: formatValue(sourceData.median),
                  marker
                },
                {
                  label: t('chart.box_plot_quartile_range'),
                  value: `${formatValue(sourceData.q1)} – ${formatValue(sourceData.q3)}`,
                  marker
                }
              )
            }
            const outliers = Array.isArray(sourceData.outliers) ? sourceData.outliers : []
            const displayedOutliers = outliers.slice(0, MAX_TOOLTIP_OUTLIER_VALUES).map(formatValue)
            const hiddenOutlierCount = Math.max(
              0,
              Number(sourceData.outlierCount ?? outliers.length) - displayedOutliers.length
            )
            rows.push({
              label: t('chart.box_plot_outlier_count'),
              value: String(sourceData.outlierCount ?? outliers.length),
              marker
            })
            if (showDetails && (displayedOutliers.length || hiddenOutlierCount)) {
              if (hiddenOutlierCount) {
                displayedOutliers.push(
                  t('chart.box_plot_more_outliers', { count: hiddenOutlierCount })
                )
              }
              rows.push({
                label: t('chart.box_plot_outlier'),
                value: displayedOutliers.join(', '),
                marker
              })
            }
            return rows
          })
          .map((item, index) => {
            const isHeader = item.header === true
            const markerSize = isHeader
              ? DETAIL_TOOLTIP_HEADER_MARKER_SIZE
              : DETAIL_TOOLTIP_ITEM_MARKER_SIZE
            // 与公共 tooltip 对齐：使用自然行高，指标行间距 6px，后续分组标题前间距 4px
            const itemPaddingTop = index === 0 ? 0 : isHeader ? 4 : 6
            const markerMarginLeft = isHeader ? 0 : 2
            const markerMarginRight = isHeader ? 5 : 9
            const nameFontWeight = isHeader ? 500 : 400
            const nameSuffix = isHeader ? '' : ':'
            // 保留统计项的两列对齐，同时允许名称和值在公共 tooltip 上限内收缩省略
            return `<li class="box-plot-tooltip-row" data-index="${index}" style="display:contents">
              <span class="box-plot-tooltip-name" style="display:flex;align-items:center;min-width:0;max-width:216px;overflow:hidden;line-height:normal;padding-top:${itemPaddingTop}px;color:${tooltipAttr.color};font-size:${tooltipFontSize}px;white-space:nowrap">
                <span class="box-plot-tooltip-marker" style="background:${item.marker};width:${markerSize}px;height:${markerSize}px;border-radius:50%;display:inline-block;flex:0 0 auto;margin-left:${markerMarginLeft}px;margin-right:${markerMarginRight}px"></span>
                <span class="box-plot-tooltip-name-label" style="min-width:0;overflow:hidden;text-overflow:ellipsis;font-weight:${nameFontWeight};white-space:nowrap">${item.label}${nameSuffix}</span>
              </span>
              <span class="box-plot-tooltip-value" style="min-width:0;overflow:hidden;text-overflow:ellipsis;line-height:normal;padding-top:${itemPaddingTop}px;color:${tooltipAttr.color};font-size:${tooltipFontSize}px;text-align:right;white-space:nowrap">${item.value}</span>
            </li>`
          })
          .join('')
        const firstSummary = summaries[0].sourceData
        const titleHtml = TOOLTIP_TITLE_TPL.replace('{title}', () =>
          escape(dimensionLabel(firstSummary.field))
        )
        return `${titleHtml}<ul class="g2-tooltip-list box-plot-tooltip-list" style="display:grid;grid-template-columns:max-content minmax(0,1fr);column-gap:30px;row-gap:0;margin:0;list-style-type:none;padding:0;width:max-content">${itemsHtml}</ul>`
      }
    }
    return {
      ...options,
      children: options.children.map(child => {
        if (child.key === BOX_TOOLTIP_HIT_MARK_KEY) {
          return {
            ...child,
            tooltip: data => data,
            interaction: {
              ...child.interaction,
              tooltip: tooltipInteraction
            }
          }
        }
        if (child.key === BOX_DIMENSION_HIT_MARK_KEY) {
          return { ...child, tooltip: data => data }
        }
        return { ...child, tooltip: false }
      })
    }
  }

  protected configLegend(chart: Chart, options: ViewSpec): ViewSpec {
    const legend = this.getLegend(chart)
    if (legend && chart.xAxisExt?.length) {
      const colorLegend = (legend as any).color ?? {}
      const formatter = colorLegend.labelFormatter
      ;(legend as any).color = {
        ...colorLegend,
        labelFormatter: key => (formatter ? formatter(keyLabel(key)) : keyLabel(key))
      }
    }
    return {
      ...options,
      // 多个 mark 共用 color scale，必须同步 guide；任一子 mark 写入 false 都会关闭整张图的分组图例。
      children: options.children.map(child => (child.encode?.color ? { ...child, legend } : child))
    }
  }

  protected configColor(chart: Chart, options: ViewSpec): ViewSpec {
    if (!chart.xAxisExt?.length) {
      return super.configColor(chart, options)
    }
    const customAttr = parseJson(chart.customAttr)
    // 与面板使用同一份原始查询顺序，横轴布局重排不能改变默认系列颜色
    return super.configColor(
      {
        ...chart,
        customAttr: {
          ...customAttr,
          basicStyle: {
            ...customAttr.basicStyle,
            seriesColor: this.setupSeriesColor(chart as ChartObj, chart.data.data)
          }
        }
      },
      options
    )
  }

  protected configXAxis(chart: Chart, options: ViewSpec): ViewSpec {
    const result = super.configXAxis(chart, options)
    const axis = result.children[0].axis.x as any
    if (axis) {
      const formatter = axis.labelFormatter
      axis.labelFormatter = key => (formatter ? formatter(keyLabel(key)) : keyLabel(key))
    }
    return result
  }

  protected configYAxis(chart: Chart, options: ViewSpec): ViewSpec {
    const result = super.configYAxis(chart, options)
    const [boxMark, ...otherMarks] = result.children
    const yScale = boxMark?.scale?.y
    if (!yScale || !otherMarks.length) {
      return result
    }
    // 手动数值轴范围由父类写入箱体标记，这里同步给异常点，避免两个标记使用不同坐标域而错位。
    return {
      ...result,
      children: [
        boxMark,
        ...otherMarks.map(mark => ({
          ...mark,
          scale: {
            ...mark.scale,
            y: {
              ...mark.scale?.y,
              ...yScale
            }
          }
        }))
      ]
    }
  }

  protected setupOptions(chart: Chart, options: ViewSpec): ViewSpec {
    // 使用项目 flow 保留原始 chart 参数，避免继承的柱状图配置方法收到 G2 options
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
    if (!chart.xAxisExt?.length) {
      return setUpGroupSeriesColor(chart, data)
    }
    const { basicStyle } = parseJson(chart.customAttr)
    const savedColors = new Map<string, string>()
    const oldSeries: BoxPlotSeriesColor[] = basicStyle.seriesColor ?? []
    oldSeries.forEach(item => {
      // 格式标记区分历史原始类别和新编码，真实类别恰好是 "[null]" 时也不会串色
      savedColors.set(item.boxPlotSeries ? item.id : dimensionKey(item.id), item.color)
    })
    const colors = basicStyle.colors
    const domain = dimensionDomain(
      (data ?? []).map(item => item.category),
      chart.xAxisExt[0].sort === 'custom_sort'
    )
    return domain.map((id, index): BoxPlotSeriesColor => {
      // 所有类别按系列顺序循环取色，已保存的自定义颜色优先
      const colorIndex = index % colors.length
      return {
        id,
        name: keyLabel(id),
        color: savedColors.get(id) ?? colors[colorIndex],
        boxPlotSeries: true
      }
    })
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    chart.customAttr = normalizeCustomAttr(chart.customAttr)
    return super.setupDefaultOptions(chart)
  }

  constructor() {
    super('box-plot', DEFAULT_DATA)
  }
}
