import { Box as G2Box, BoxOptions } from '@antv/g2plot/esm/plots/box'
import { OUTLIERS_VIEW_ID } from '@antv/g2plot/esm/plots/box/constant'
import {
  G2PlotChartView,
  G2PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/g2plot'
import {
  configPlotTooltipEvent,
  configXAxisLengthLimit,
  getPadding,
  getTooltipContainer
} from '@/views/chart/components/js/panel/common/common_antv'
import { BAR_EDITOR_PROPERTY_INNER } from '@/views/chart/components/js/panel/charts/bar/common'
import { cloneDeep } from 'lodash-es'
import { flow, hexColorToRGBA, parseJson } from '@/views/chart/components/js/util'
import {
  alignBoxPlotOutliers,
  BOX_CATEGORY_FIELD,
  BOX_GROUP_FIELD,
  BOX_ID_FIELD,
  boxPlotDomain,
  boxPlotKey,
  boxPlotOpaqueColor
} from '@/views/chart/components/js/panel/common/box_plot'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { useI18n } from '@/hooks/web/useI18n'
import { Action, registerAction, registerInteraction, View } from '@antv/g2'
import { getTooltipItems } from '@antv/g2/esm/util/tooltip'

const { t } = useI18n()
const DEFAULT_DATA = []
const OUTLIER_VALUES_FIELD = 'boxPlotOutlierValues'
// 分组头保持 G2 原有标记大小，仅缩小统计明细标记，不参与箱线图统计计算
const DETAIL_TOOLTIP_HEADER_MARKER_SIZE = 8
const DETAIL_TOOLTIP_ITEM_MARKER_SIZE = 4
const MAX_TOOLTIP_OUTLIER_VALUES = 10
const BOX_ACTIVE_REGION = 'dataease-box-active-region'
const ACTIVE_REGION_SHAPE_NAME = 'active-region'
const ACTIVE_REGION_STYLE = {
  fill: '#CCD6EC',
  opacity: 0.3
}

const configSingleCategoryTooltip = (view: View) => {
  const controller = view.getController('tooltip')
  const originalGetTooltipItems = controller.getTooltipItems.bind(controller)
  controller.getTooltipItems = point => {
    const xScale = view.getXScale()
    if (xScale?.values?.length !== 1 || !view.getCoordinate()?.isRect) {
      return originalGetTooltipItems(point)
    }
    const bounds = view.coordinateBBox
    if (
      view.getOptions().tooltip === false ||
      !bounds ||
      point.x < bounds.minX ||
      point.x > bounds.maxX ||
      point.y < bounds.minY ||
      point.y > bounds.maxY
    ) {
      return []
    }
    const schema = view.geometries.find(geometry => geometry.type === 'schema')
    if (!schema?.visible || schema.tooltipOption === false) return []
    const { title, showNil, reversed } = controller.getTooltipCfg()
    // 单主维度的整个绘图区共享当前可见箱体摘要，避开分类反查边界并排除异常点的单独命中
    const items = schema.elements.flatMap(element => {
      if (!element.visible || element.getData()[xScale.field] !== xScale.values[0]) return []
      const mappingData = element.getModel().mappingData
      if (!mappingData || Array.isArray(mappingData)) return []
      const item = getTooltipItems(mappingData, schema, title, showNil)[0]
      if (!item) return []
      return [
        {
          ...item,
          x: Array.isArray(mappingData.x) ? mappingData.x[mappingData.x.length - 1] : mappingData.x,
          y: Array.isArray(mappingData.y) ? mappingData.y[mappingData.y.length - 1] : mappingData.y
        }
      ]
    })
    return reversed ? items.reverse() : items
  }
}

// 命中箱体不依赖 tooltip，退化成横线的箱体也能按当前可见分组进行交互
const getBoxDatumAtPoint = (view, point) => {
  const root = view.getRootView()
  const bounds = root.coordinateBBox
  if (
    !bounds ||
    point.x < bounds.minX ||
    point.x > bounds.maxX ||
    point.y < bounds.minY ||
    point.y > bounds.maxY
  ) {
    return undefined
  }
  const coordinate = root.getCoordinate()
  const xScale = root.getXScale()
  if (!coordinate?.isRect || !xScale?.values?.length) return undefined
  const axis = coordinate.isTransposed ? 'y' : 'x'
  // 先按分类中心划定所属区域，隐藏后的空类别不能命中相邻类别的箱体
  let category
  let categoryDistance = Infinity
  xScale.values.forEach(value => {
    const center = coordinate.convert({ x: xScale.scale(value), y: 0 })[axis]
    const distance = Math.abs(point[axis] - center)
    if (distance < categoryDistance) {
      categoryDistance = distance
      category = value
    }
  })
  const schema = root.geometries.find(geometry => geometry.type === 'schema')
  let nearest
  let distance = Infinity
  schema?.elements.forEach(element => {
    if (!element.visible) return
    const datum = element.getData()
    if (datum[xScale.field] !== category) return
    const bbox = element.shape.getCanvasBBox()
    const center = coordinate.isTransposed
      ? (bbox.minY + bbox.maxY) / 2
      : (bbox.minX + bbox.maxX) / 2
    const currentDistance = Math.abs(point[axis] - center)
    if (currentDistance < distance) {
      distance = currentDistance
      nearest = datum
    }
  })
  return nearest
}
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

/** 将计算结果限制在绘图区边界内，避免背景因浮点误差发生越界 */
const clamp = (value: number, min: number, max: number) => Math.min(Math.max(value, min), max)

/** 根据主维度分类中心计算互不重叠的悬浮背景路径 */
const getCategoryRegionPath = (view, categoryValue) => {
  const coordinate = view.getCoordinate()
  const coordinateBBox = view.coordinateBBox
  const xScale = view.getXScale()
  const categoryValues = xScale?.values ?? []
  const categoryIndex = categoryValues.findIndex(value => value === categoryValue)
  if (!coordinate?.isRect || !coordinateBBox || categoryIndex < 0) {
    return undefined
  }

  /** 将分类值转换为当前坐标系中的画布中心位置 */
  const getCategoryCenter = value => {
    const point = coordinate.convert({ x: xScale.scale(value), y: 0 })
    return coordinate.isTransposed ? point.y : point.x
  }
  const center = getCategoryCenter(categoryValue)
  const firstCenter = getCategoryCenter(categoryValues[0])
  const lastCenter = getCategoryCenter(categoryValues[categoryValues.length - 1])
  const previousCenter =
    categoryIndex > 0 ? getCategoryCenter(categoryValues[categoryIndex - 1]) : undefined
  const nextCenter =
    categoryIndex < categoryValues.length - 1
      ? getCategoryCenter(categoryValues[categoryIndex + 1])
      : undefined
  if (
    ![center, firstCenter, lastCenter, previousCenter, nextCenter]
      .filter(value => value !== undefined)
      .every(Number.isFinite)
  ) {
    return undefined
  }

  const axisMin = coordinate.isTransposed ? coordinateBBox.minY : coordinateBBox.minX
  const axisMax = coordinate.isTransposed ? coordinateBBox.maxY : coordinateBBox.maxX
  const isAscending = firstCenter <= lastCenter
  const leadingEdge = isAscending ? axisMin : axisMax
  const trailingEdge = isAscending ? axisMax : axisMin
  // 相邻分类共享同一个中点边界，确保背景区间既不扩大也不交叉
  const firstBoundary = categoryIndex === 0 ? leadingEdge : (previousCenter + center) / 2
  const secondBoundary =
    categoryIndex === categoryValues.length - 1 ? trailingEdge : (center + nextCenter) / 2
  const regionMin = clamp(Math.min(firstBoundary, secondBoundary), axisMin, axisMax)
  const regionMax = clamp(Math.max(firstBoundary, secondBoundary), axisMin, axisMax)
  if (regionMax <= regionMin) {
    return undefined
  }

  if (coordinate.isTransposed) {
    return [
      ['M', coordinateBBox.minX, regionMin],
      ['L', coordinateBBox.maxX, regionMin],
      ['L', coordinateBBox.maxX, regionMax],
      ['L', coordinateBBox.minX, regionMax],
      ['Z']
    ]
  }
  return [
    ['M', regionMin, coordinateBBox.minY],
    ['L', regionMax, coordinateBBox.minY],
    ['L', regionMax, coordinateBBox.maxY],
    ['L', regionMin, coordinateBBox.maxY],
    ['Z']
  ]
}

/** 为箱线图绘制不依赖图形 CanvasBBox 的分类悬浮背景 */
class BoxActiveRegion extends Action {
  private regionPath: any

  /** 根据当前 Tooltip 命中的分类创建或更新悬浮背景 */
  public show() {
    const view = this.context.view
    const event = this.context.event
    const tooltipController = view.getController('tooltip')
    if (!tooltipController || view.getOptions().tooltip === false) {
      this.hide()
      return
    }

    const datum = getBoxDatumAtPoint(view, event)
    const xField = view.getXScale()?.field
    const path = datum ? getCategoryRegionPath(view, datum[xField]) : undefined
    if (!path) {
      this.hide()
      return
    }

    if (this.regionPath) {
      this.regionPath.attr('path', path)
      this.regionPath.show()
      return
    }
    this.regionPath = view.backgroundGroup.addShape({
      type: 'path',
      name: ACTIVE_REGION_SHAPE_NAME,
      capture: false,
      attrs: {
        ...ACTIVE_REGION_STYLE,
        path
      }
    })
  }

  /** 隐藏已创建的悬浮背景 */
  public hide() {
    this.regionPath?.hide()
  }

  /** 销毁悬浮背景并释放交互持有的图形引用 */
  public destroy() {
    this.regionPath?.remove(true)
    this.regionPath = undefined
    super.destroy()
  }
}

// 使用独立交互名称注册，避免覆盖其他图表使用的 G2 active-region
registerAction(BOX_ACTIVE_REGION, BoxActiveRegion)
registerInteraction(BOX_ACTIVE_REGION, {
  start: [{ trigger: 'plot:mousemove', action: `${BOX_ACTIVE_REGION}:show` }],
  end: [{ trigger: 'plot:mouseleave', action: `${BOX_ACTIVE_REGION}:hide` }]
})

type DataEaseBoxOptions = BoxOptions & {
  outlierColorMode?: ChartBasicStyle['outlierColorMode']
  outlierColor?: string
  outlierSize?: number
}

class DataEaseBox extends G2Box {
  // G2 4.x 在 beforepaint 前完成所有 Geometry 的分组计算，此时尚未生成图形及命中信息
  // 升级 G2/G2Plot 后需重新验证实际渲染及图例更新的调用顺序
  private alignOutliers = () => {
    const schema = this.chart.geometries.find(geometry => geometry.type === 'schema')
    const point = this.chart.views.find(view => view.id === OUTLIERS_VIEW_ID)?.geometries[0]
    if (!schema || !point) return
    // 将对 G2 内部调整结果的访问限制在适配层，避免绘制后移动 Shape 导致命中位置不一致
    type AdjustedGeometry = { beforeMappingData: Record<string, any>[][] }
    const boxes = schema as unknown as AdjustedGeometry
    const points = point as unknown as AdjustedGeometry
    points.beforeMappingData = alignBoxPlotOutliers(
      boxes.beforeMappingData ?? [],
      points.beforeMappingData ?? [],
      this.options.xField
    )
  }

  protected execAdaptor(): void {
    super.execAdaptor()
    this.chart.off('beforepaint', this.alignOutliers)
    this.chart.on('beforepaint', this.alignOutliers)
    if (this.options.legend === false) {
      this.chart.legend(false)
    }
    const groupField = this.options.groupField
    // Box 适配器没有将状态传给异常点子视图，两层都要使用同一套联动状态样式
    this.chart.geometries.forEach(geometry => geometry.state(this.options.state))
    const colors = this.options.color
    if (!groupField && Array.isArray(colors)) {
      this.chart.geometries.forEach(geometry => geometry.color(colors[0]))
    }
    const outliersView = this.chart.views.find(view => view.id === OUTLIERS_VIEW_ID)
    const geometry = outliersView?.geometries?.[0]
    if (!outliersView || !geometry) {
      return
    }
    const sourceData = this.options.data ?? []
    const dataEaseOptions = this.options as DataEaseBoxOptions
    const categoryValues = [...new Set(sourceData.map(datum => datum[this.options.xField]))]
    // 异常点子视图只包含有异常值的类别，必须复用主图完整类别域，否则单个类别会被画到绘图区中央
    this.chart.scale(this.options.xField, {
      ...this.options.meta?.[this.options.xField],
      type: 'cat',
      values: categoryValues
    })
    outliersView.scale(this.options.xField, {
      ...this.options.meta?.[this.options.xField],
      type: 'cat',
      values: categoryValues
    })
    geometry.state(this.options.state)
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
    // 分组尺度与主箱体保持同一顺序，确保颜色和图例过滤使用相同类别域
    outliersView.scale(groupField, {
      ...this.options.meta?.[groupField],
      type: 'cat',
      values: groupValues
    })
    // 自定义常量色也保留分组通道，避免丢失子类别对应的颜色尺度
    if (dataEaseOptions.outlierColorMode === 'custom' && dataEaseOptions.outlierColor) {
      geometry.color(groupField, () => dataEaseOptions.outlierColor)
    } else if (this.options.color) {
      geometry.color(groupField, this.options.color)
    } else {
      geometry.color(groupField)
    }
    // 异常点仅保留分组与颜色映射，横坐标由 alignOutliers 取自箱体
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
      [BOX_CATEGORY_FIELD]: boxPlotKey(datum.field),
      [BOX_GROUP_FIELD]: boxPlotKey(datum.category),
      [BOX_ID_FIELD]: JSON.stringify([datum.field ?? null, datum.category ?? null]),
      // G2Plot 会在异常点子视图中把 outliers 数组替换成当前单值，单独保留完整列表供统一 tooltip 使用
      [OUTLIER_VALUES_FIELD]: Array.isArray(datum.outliers) ? [...datum.outliers] : []
    }))
    const hasGroup = !!chart.xAxisExt?.length
    const categories = boxPlotDomain(
      data.map(datum => datum.field),
      t('chart.filter_empty')
    )
    const groups = boxPlotDomain(
      data.map(datum => datum.category),
      t('chart.filter_empty')
    )
    const initOptions: BoxOptions = {
      appendPadding: getPadding(chart),
      data,
      xField: BOX_CATEGORY_FIELD,
      yField: ['low', 'q1', 'median', 'q3', 'high'],
      groupField: hasGroup ? BOX_GROUP_FIELD : undefined,
      outliersField: 'outliers',
      interactions: [{ type: BOX_ACTIVE_REGION }],
      meta: {
        [BOX_CATEGORY_FIELD]: {
          type: 'cat',
          values: [...categories.keys()],
          formatter: key => categories.get(key)?.label ?? key
        },
        ...(hasGroup
          ? {
              [BOX_GROUP_FIELD]: {
                type: 'cat',
                values: [...groups.keys()],
                formatter: key => groups.get(key)?.label ?? key
              }
            }
          : {})
      }
    }
    const options = this.setupOptions(chart, initOptions)
    const plot = new DataEaseBox(container, options)
    configSingleCategoryTooltip(plot.chart)

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
            ...cloneDeep(datum),
            value: Array.isArray(datum.outliers) ? datum.median : datum.outliers ?? datum.median
          }
        }
      })
    }
    plot.on('schema:click', normalizeAction)
    plot.on('point:click', normalizeAction)
    plot.on('plot:click', event => {
      if (event.target?.cfg?.renderer !== 'canvas') {
        return
      }
      const datum = getBoxDatumAtPoint(plot.chart, event)
      if (datum) {
        normalizeAction(event, datum)
      }
    })
    configPlotTooltipEvent(chart, plot as any)
    // 与柱状图共用轴标签提示，轴刻度的 name 已由 meta 转为完整展示文本
    configXAxisLengthLimit(chart, plot)
    return plot
  }

  protected configColor(chart: Chart, options: BoxOptions): BoxOptions {
    const { basicStyle } = parseJson(chart.customAttr)
    if (!options.groupField) {
      const customColor = basicStyle.seriesColor?.find(item => item.id === chart.yAxis?.[0]?.id)
      return {
        ...options,
        color: [hexColorToRGBA(customColor?.color ?? basicStyle.colors[0], basicStyle.alpha)]
      }
    }
    const groups = boxPlotDomain(
      options.data.map(datum => datum.category),
      t('chart.filter_empty')
    )
    const color = [...groups].map(([key, group], index) => {
      const customColor =
        basicStyle.seriesColor?.find(item => item.id === `box-plot:${key}`) ??
        basicStyle.seriesColor?.find(item => item.id === group.value)
      return hexColorToRGBA(
        customColor?.color ?? basicStyle.colors[index % basicStyle.colors.length],
        basicStyle.alpha
      )
    })
    return { ...options, color }
  }

  protected configBasicStyle(chart: Chart, options: BoxOptions): BoxOptions {
    const customAttr = parseJson(chart.customAttr)
    const basicStyle = customAttr.basicStyle
    // 统计线与箱体填充共用整体透明度，兼容历史描边颜色中自带的 alpha
    const stroke = hexColorToRGBA(
      boxPlotOpaqueColor(basicStyle.themeContrastColor ?? customAttr.label?.color ?? '#000000'),
      basicStyle.alpha
    )
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
      outlierColor: basicStyle.outlierColor
        ? hexColorToRGBA(boxPlotOpaqueColor(basicStyle.outlierColor), basicStyle.alpha)
        : undefined,
      outlierSize: basicStyle.outlierSize,
      // 隐藏异常点只影响展示，四分位数、须线和异常值数量的统计口径保持不变
      outliersField: basicStyle.showOutliers === false ? undefined : options.outliersField,
      boxStyle,
      // 公共主题会为高亮和选中状态重设描边，箱线图在这些状态下也保留整体透明度
      state: {
        ...options.state,
        active: {
          ...options.state?.active,
          style: { ...options.state?.active?.style, stroke }
        },
        selected: {
          ...options.state?.selected,
          style: { ...options.state?.selected?.style, stroke }
        }
      },
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
            const headerName = groupAxis
              ? options.meta?.[options.groupField]?.formatter?.(datum[options.groupField]) ??
                datum.category
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
                createItem(sourceItem, labelMap.outlierCount, outlierCount, false)
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

  protected configLegend(chart: Chart, options: BoxOptions): BoxOptions {
    const result = super.configLegend(chart, options)
    if (result.legend) {
      const marker = result.legend.marker
      result.legend = {
        ...result.legend,
        // G2 的箱线图图例默认只有描边，复用该项系列色填充并保留用户设置的形状和大小
        marker: (name, index, item) => {
          const config = typeof marker === 'function' ? marker(name, index, item) : marker
          return {
            ...config,
            style: {
              ...config?.style,
              fill: item.style?.stroke ?? item.style?.fill
            }
          }
        }
      }
    }
    return result
  }

  protected configXAxis(chart: Chart, options: BoxOptions): BoxOptions {
    const result = super.configXAxis(chart, options)
    if (result.xAxis && result.xAxis.label) {
      const limit = parseJson(chart.customStyle).xAxis.axisLabel.lengthLimit
      // 长度限制只作用于轴标签，完整类别键继续用于分组、tooltip 和联动
      result.xAxis.label.formatter = value => {
        const text = String(value)
        return limit > 0 && text.length > limit ? `${text.slice(0, limit)}...` : text
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
    const { basicStyle } = chart.customAttr
    if (!chart.xAxisExt?.length) return super.setupSeriesColor(chart, data)
    return [
      ...boxPlotDomain(
        (data ?? []).map(datum => datum.category),
        t('chart.filter_empty')
      )
    ].map(([key, group], index) => ({
      id: `box-plot:${key}`,
      name: group.label,
      color:
        basicStyle.seriesColor?.find(item => item.id === group.value)?.color ??
        basicStyle.colors[index % basicStyle.colors.length]
    }))
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    chart.customAttr.tooltip.showBoxPlotDetails ??= true
    return chart
  }

  constructor() {
    super('box-plot', DEFAULT_DATA)
  }
}
