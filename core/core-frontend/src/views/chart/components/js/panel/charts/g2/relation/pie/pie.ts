import { G2ChartView, G2DrawOptions } from '../../../../types/impl/g2'
import {
  flow,
  hexColorToRGBA,
  parseJson,
  setUpSingleDimensionSeriesColor
} from '@/views/chart/components/js/util'
import {
  PIE_AXIS_CONFIG,
  PIE_AXIS_TYPE,
  PIE_EDITOR_PROPERTY,
  PIE_EDITOR_PROPERTY_INNER
} from '@/views/chart/components/js/panel/charts/g2plot/pie/common'
import { configSingleSectorScale, createCircularLabelLayout } from './common'
import { CircularLabelOverflow } from './text-overflow'
import {
  getG2Renderer,
  getTooltipSeriesTotalMap,
  handleChartDashboardHidden,
  TOOLTIP_ITEM_TPL,
  TOOLTIP_TITLE_TPL
} from '../../../../common/common_antv'
import { useI18n } from '@/hooks/web/useI18n'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { cloneDeep, defaultsDeep, isEmpty } from 'lodash-es'
import { Chart as G2Chart, G2Spec } from '@antv/g2'
import { Circle, Group, Text, type TextStyleProps } from '@antv/g'
import {
  CHART_FONT_FAMILY_MAP,
  DEFAULT_CENTER_CONTENT
} from '@/views/chart/components/editor/util/chart'
import G2TooltipCarousel from '@/views/chart/components/js/G2TooltipCarousel'
import { createTooltipWrapper, getThemeSelectedState, tooltipCss } from '../../bar/barUtil'

const { t } = useI18n()
const configCircularLabelLayout = createCircularLabelLayout(true)

export class Pie extends G2ChartView {
  axis: AxisType[] = PIE_AXIS_TYPE
  properties = PIE_EDITOR_PROPERTY
  propertyInner: EditorPropertyInner = {
    ...PIE_EDITOR_PROPERTY_INNER,
    'basic-style-selector': ['colors', 'alpha', 'radius', 'topN', 'seriesColor'],
    'tooltip-selector': [...PIE_EDITOR_PROPERTY_INNER['tooltip-selector'], 'carousel']
  }
  axisConfig = PIE_AXIS_CONFIG

  async drawChart(drawOptions: G2DrawOptions<G2Chart>): Promise<G2Chart> {
    const { chart, container, action } = drawOptions
    this.configEmptyDataStyle(chart.data?.data, container, null, t('chart.no_data_or_not_positive'))
    chart.container = container
    if (!chart.data?.data?.length) {
      return
    }
    // data
    const data = chart.data.data
    // custom color
    const customAttr = parseJson(chart.customAttr)
    // options
    const initOptions: G2Spec = {
      type: 'interval',
      autoFit: true,
      data: {
        value: data
      },
      transform: [{ type: 'stackY' }],
      encode: {
        color: 'field',
        y: 'value'
      },
      coordinate: {
        type: 'theta'
      },
      interaction: {
        elementSelect: {
          single: true
        },
        elementHighlight: true
      }
    }
    const total = data.reduce((pre, next) => pre + (next.value ?? 0), 0)
    const options = this.setupOptions(chart, initOptions, { total })
    const newChart = new G2Chart({ container, ...getG2Renderer() })
    handleChartDashboardHidden(chart, options)
    newChart.options(options)
    newChart.on('interval:click', d => {
      d.data?.data?.field !== customAttr.basicStyle.topNLabel && action(d)
    })
    new G2TooltipCarousel(newChart, chart, data).start()
    return newChart
  }

  protected configTheme(chart: Chart, options: G2Spec): G2Spec {
    const customAttr = parseJson(chart.customAttr)
    const colors: string[] = []
    if (customAttr.basicStyle) {
      const basicStyle = customAttr.basicStyle
      basicStyle.colors.forEach(ele => {
        colors.push(hexColorToRGBA(ele, basicStyle.alpha))
      })
    }
    const customStyle = parseJson(chart.customStyle)
    let bgColor
    if (customStyle.background) {
      bgColor = hexColorToRGBA(customStyle.background.color, customStyle.background.alpha)
    }
    const theme = {
      color: colors[0],
      category10: colors,
      category20: colors,
      view: {
        viewFill: bgColor
      }
    }
    const state = getThemeSelectedState(chart, options.state)
    return { ...options, theme, state }
  }

  protected configBasicStyle(chart: Chart, options: G2Spec): G2Spec {
    const customAttr = parseJson(chart.customAttr)
    const { basicStyle } = customAttr
    const data = options.data.value || []
    if (data?.length && basicStyle.calcTopN && data.length > basicStyle.topN) {
      data.sort((a, b) => b.value - a.value)
      const otherItems = data.splice(basicStyle.topN)
      const initOtherItem = {
        ...data[0],
        dynamicTooltipValue: [],
        field: basicStyle.topNLabel,
        name: basicStyle.topNLabel,
        value: 0
      }
      const dynamicTotalMap: Record<string, number> = {}
      otherItems.reduce((p, n) => {
        p.value += n.value ?? 0
        n.dynamicTooltipValue?.forEach(val => {
          dynamicTotalMap[val.fieldId] = (dynamicTotalMap[val.fieldId] || 0) + val.value
        })
        return p
      }, initOtherItem)
      for (const key in dynamicTotalMap) {
        initOtherItem.dynamicTooltipValue.push({
          fieldId: key,
          value: dynamicTotalMap[key]
        })
      }
      data.push(initOtherItem)
    }
    const singleSectorOptions = configSingleSectorScale(options, data)
    singleSectorOptions.coordinate.outerRadius = basicStyle.radius / 100
    return singleSectorOptions
  }

  protected configColor(chart: Chart, options: G2Spec): G2Spec {
    const { basicStyle } = parseJson(chart.customAttr)
    const { seriesColor } = basicStyle
    if (!seriesColor?.length) {
      return options
    }
    const { xAxis, yAxis } = chart
    if (xAxis?.length && yAxis?.length) {
      const relations = []
      seriesColor.forEach(item => {
        relations.push([item.id, hexColorToRGBA(item.color, basicStyle.alpha)])
      })
      const scaleOptions = {
        scale: {
          color: {
            relations
          }
        }
      }
      defaultsDeep(options, scaleOptions)
    }
    return options
  }

  protected configLabel(chart: Chart, options: G2Spec, context: Record<string, any>): G2Spec {
    const { label: labelAttr } = parseJson(chart.customAttr)
    if (!labelAttr?.show) {
      return options
    }
    const { total } = context
    const isInnerLabel = labelAttr.position === 'inner'
    const label = {
      // 普通外标签 spider 排布，全量显示时跳过自动隐藏
      transform: isInnerLabel
        ? [{ type: 'exceedAdjust' }]
        : [{ type: CircularLabelOverflow, fullDisplay: labelAttr.fullDisplay }],
      position: isInnerLabel ? 'inside' : labelAttr.fullDisplay ? 'outside' : 'spider',
      style: {
        fill: labelAttr.color,
        fontSize: labelAttr.fontSize,
        fillOpacity: 1
      },
      text: param => {
        let res = param.value
        const contentItems = []
        if (labelAttr.showDimension) {
          contentItems.push(param.field)
        }
        if (labelAttr.showQuota) {
          contentItems.push(valueFormatter(param.value, labelAttr.quotaLabelFormatter))
        }
        if (labelAttr.showProportion) {
          const percentage = `${(Math.round((param.value / total) * 10000) / 100).toFixed(
            labelAttr.reserveDecimalCount
          )}%`
          if (labelAttr.showDimension && labelAttr.showQuota) {
            contentItems.push(`(${percentage})`)
          } else {
            contentItems.push(percentage)
          }
        }
        res = contentItems.join(' ')
        return res
      },
      connectorStroke: (_data, _index, _dataList, { element }) => element.__data__.color
    }
    // 外部 spider 标签不做重叠隐藏，避免密集饼图只剩少量标签
    if (!labelAttr.fullDisplay && isInnerLabel) {
      label.transform.push({ type: 'overlapHide' })
    }
    return { ...options, labels: [label] }
  }

  protected configTooltip(chart: Chart, options: G2Spec, context: Record<string, any>): G2Spec {
    const { tooltip: tooltipAttr, label } = parseJson(chart.customAttr)
    const { yAxis } = chart
    if (!tooltipAttr.show) {
      return {
        ...options,
        tooltip: false
      }
    }
    const reserveDecimalCount = label.reserveDecimalCount
    const seriesTotalMap = getTooltipSeriesTotalMap(options.data.value)
    const formatterMap = tooltipAttr.seriesTooltipFormatter
      ?.filter(i => i.show)
      .reduce((pre, next) => {
        pre[next.id] = next
        return pre
      }, {}) as Record<string, SeriesFormatter>
    const { total } = context
    const tooltipOptions: G2Spec = {
      tooltip: d => d,
      interaction: {
        tooltip: {
          mount: createTooltipWrapper(chart),
          css: tooltipCss(tooltipAttr),
          render: (_, { items }) => {
            let tooltipItems = items
            if (tooltipAttr.seriesTooltipFormatter?.length) {
              tooltipItems = items.filter(item => formatterMap[item.quotaList[0].id])
            }
            const result = []
            const [head] = items
            const titleHtml = TOOLTIP_TITLE_TPL.replace('{title}', head.field)
            tooltipItems.forEach(item => {
              const formatter = formatterMap[item.quotaList[0].id] ?? yAxis[0]
              const value = valueFormatter(parseFloat(item.value as string), formatter.formatterCfg)
              // sync with label
              const percent = (Math.round(((item.value as number) / total) * 10000) / 100).toFixed(
                reserveDecimalCount
              )
              const name = isEmpty(formatter.chartShowName)
                ? formatter.name
                : formatter.chartShowName
              result.push({ ...item, name, value: `${value ?? ''} (${percent}%)` })
            })
            head.dynamicTooltipValue?.forEach(item => {
              const formatter = formatterMap[item.fieldId]
              if (formatter) {
                const total = seriesTotalMap[item.fieldId]
                // sync with label
                const percent = (Math.round((item.value / total) * 10000) / 100).toFixed(
                  reserveDecimalCount
                )
                const value = valueFormatter(parseFloat(item.value), formatter.formatterCfg)
                const name = isEmpty(formatter.chartShowName)
                  ? formatter.name
                  : formatter.chartShowName
                result.push({ color: 'grey', name, value: `${value ?? ''} (${percent}%)` })
              }
            })
            const itemsHtml = result
              .map(item => {
                const marker = item.color
                const label = item.name
                const value = item.value
                return TOOLTIP_ITEM_TPL.replace('{marker}', marker)
                  .replace('{label}', label)
                  .replace('{value}', value)
              })
              .join('')
            const listHtml = `<ul class="g2-tooltip-list" style="margin: 0px; list-style-type: none; padding: 0px;">${itemsHtml}</ul>`
            return `${titleHtml}${listHtml}`
          }
        }
      }
    }
    return defaultsDeep(options, tooltipOptions)
  }

  protected configLegend(chart: Chart, options: G2Spec): G2Spec {
    const { legend } = parseJson(chart.customStyle)
    if (!legend.show) {
      return { ...options, legend: false }
    }
    const baseLegend = this.getLegend(chart, 2)
    const tmpLegend = {
      legend: {
        color: {
          ...baseLegend
        }
      }
    }
    defaultsDeep(options, tmpLegend)
    return options
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    const { customAttr, customStyle } = chart
    const { label } = customAttr
    if (!['inner', 'outer'].includes(label.position)) {
      label.position = 'outer'
    }
    customAttr.label = {
      ...label,
      show: true,
      showDimension: true,
      showProportion: true,
      reserveDecimalCount: 2
    }
    const { legend } = customStyle
    legend.show = false
    return chart
  }

  public setupSeriesColor(chart: ChartObj, data?: any[]): ChartBasicStyle['seriesColor'] {
    data = cloneDeep(data)
    const { calcTopN, topN, topNLabel } = chart.customAttr.basicStyle
    if (data?.length && calcTopN && data.length > topN) {
      data.sort((a, b) => b.value - a.value)
      data.splice(topN)
      data.push({
        field: topNLabel,
        value: 0
      })
    }
    return setUpSingleDimensionSeriesColor(chart, data)
  }

  protected setupOptions(chart: Chart, options: G2Spec, context: Record<string, any>): G2Spec {
    return flow(
      this.configTheme,
      this.configBasicStyle,
      this.configColor,
      this.configLegend,
      this.configLabel,
      this.configTooltip,
      configCircularLabelLayout
    )(chart, options, context, this)
  }

  constructor(name = 'pie') {
    super(name, [])
  }
}

export class PieDonut extends Pie {
  properties: EditorProperty[] = [...PIE_EDITOR_PROPERTY, 'center-content-selector']
  propertyInner: EditorPropertyInner = {
    ...PIE_EDITOR_PROPERTY_INNER,
    'basic-style-selector': ['colors', 'alpha', 'radius', 'innerRadius', 'topN', 'seriesColor'],
    'tooltip-selector': [...PIE_EDITOR_PROPERTY_INNER['tooltip-selector'], 'carousel'],
    'center-content-selector': ['all']
  }

  async drawChart(drawOptions: G2DrawOptions<G2Chart>): Promise<G2Chart> {
    const { chart } = drawOptions
    const { centerContent, basicStyle } = parseJson(chart.customAttr)
    const config = defaultsDeep(cloneDeep(centerContent || {}), cloneDeep(DEFAULT_CENTER_CONTENT))
    // 父类会原地合并 TopN，中心统计必须先读取完整的分类指标值。
    const content = this.getCenterContent(chart, config)
    const instance = await super.drawChart(drawOptions)
    if (!instance || !config.show || basicStyle.innerRadius <= 0) {
      return instance
    }
    const customStyle = parseJson(chart.customStyle)
    let fontFamily = chart.fontFamily || customStyle.text?.fontFamily || 'sans-serif'
    fontFamily = CHART_FONT_FAMILY_MAP[fontFamily] || fontFamily
    let centerGroup: Group | undefined
    const clearCenterContent = () => {
      if (centerGroup) {
        centerGroup.style.clipPath?.destroy()
        centerGroup.destroy()
        centerGroup = undefined
      }
    }
    instance.on('afterrender', () => {
      // 重绘和自适应尺寸都重新定位，旧图元随组销毁，避免中心文本叠加。
      clearCenterContent()
      const { canvas, views } = instance.getContext()
      const view = views?.[0]
      const plot = canvas?.document.querySelector('.plot')
      if (!view || !plot) {
        return
      }
      const [x, y] = view.coordinate.getCenter()
      const [width, height] = view.coordinate.getSize()
      const radius = (Math.min(width, height) / 2) * (basicStyle.innerRadius / 100)
      if (radius <= 0) {
        return
      }
      const clip = new Circle({ style: { cx: 0, cy: 0, r: radius } })
      centerGroup = new Group({
        style: {
          transform: `translate(${x}, ${y})`,
          pointerEvents: 'none',
          zIndex: 1,
          clipPath: clip
        }
      })
      plot.appendChild(centerGroup)
      // G 的独立裁剪图元使用世界坐标，必须同步环心以及绘图区的平移。
      clip.setLocalTransform(centerGroup.getWorldTransform())
      this.drawCenterContent(centerGroup, config, content, fontFamily, radius)
    })
    instance.on('beforedestroy', clearCenterContent)
    return instance
  }

  private getCenterContent(chart: Chart, config: ChartCenterContentAttr): string {
    if (!config.show) {
      return ''
    }
    const data = chart.data?.data || []
    if (config.contentType === 'count') {
      // 分类数始终是原始分类条数，不参与指标单位换算和小数格式化。
      return String(data.length)
    }
    if (config.contentType === 'custom') {
      return config.content.slice(0, 50)
    }
    let total = 0
    let count = 0
    let max = -Infinity
    let min = Infinity
    data.forEach(item => {
      if (item.value === null || item.value === undefined) {
        return
      }
      const value = Number(item.value)
      if (!Number.isFinite(value)) {
        return
      }
      total += value
      count++
      max = Math.max(max, value)
      min = Math.min(min, value)
    })
    if (!count) {
      return ''
    }
    let value = total
    if (config.contentType === 'avg') {
      value = total / count
    } else if (config.contentType === 'max') {
      value = max
    } else if (config.contentType === 'min') {
      value = min
    }
    let formatter = config.formatter
    if (config.formatterMode === 'quota') {
      formatter = defaultsDeep(cloneDeep(chart.yAxis?.[0]?.formatterCfg || {}), config.formatter)
    }
    return String(valueFormatter(value, cloneDeep(formatter)))
  }

  private drawCenterContent(
    group: Group,
    config: ChartCenterContentAttr,
    content: string,
    fontFamily: string,
    radius: number
  ): void {
    const title = config.title.trim().slice(0, 50)
    const suffix = config.suffixEnable ? config.suffix : ''
    const hasContent = Boolean(content || suffix)
    const titleHeight = title ? config.titleStyle.fontSize * 1.2 : 0
    let contentHeight = 0
    if (content) {
      contentHeight = config.contentStyle.fontSize * 1.2
    }
    if (suffix) {
      contentHeight = Math.max(contentHeight, config.suffixStyle.fontSize * 1.2)
    }
    const gap = title && hasContent ? 4 : 0
    const totalHeight = titleHeight + contentHeight + gap
    let titleY = -totalHeight / 2 + titleHeight / 2
    let contentY = totalHeight / 2 - contentHeight / 2
    if (config.titlePosition === 'bottom') {
      titleY = totalHeight / 2 - titleHeight / 2
      contentY = -totalHeight / 2 + contentHeight / 2
    }
    // 按每行在内孔中的弦长限宽，长文本省略，圆形裁剪避免大字号覆盖环形扇区。
    const getLineWidth = (lineY: number, lineHeight: number) => {
      const edgeY = Math.abs(lineY) + lineHeight / 2
      return Math.max(1, 2 * Math.sqrt(Math.max(0, radius * radius - edgeY * edgeY)) - 8)
    }
    const textStyle = (style: ChartCenterContentTextStyle): Omit<TextStyleProps, 'text'> => ({
      fontFamily: CHART_FONT_FAMILY_MAP[style.fontFamily] || style.fontFamily || fontFamily,
      letterSpacing: style.letterSpace,
      fontSize: style.fontSize,
      fill: style.color,
      fontWeight: style.isBolder ? 'bold' : 'normal',
      fontStyle: style.isItalic ? 'italic' : 'normal',
      textAlign: 'left',
      textBaseline: 'middle',
      wordWrap: true,
      maxLines: 1,
      textOverflow: 'ellipsis',
      pointerEvents: 'none',
      shadowColor: style.fontShadow ? style.color : 'transparent',
      shadowBlur: style.fontShadow ? 4 : 0,
      shadowOffsetX: style.fontShadow ? 2 : 0,
      shadowOffsetY: style.fontShadow ? 2 : 0
    })
    if (title) {
      group.appendChild(
        new Text({
          style: {
            ...textStyle(config.titleStyle),
            text: title,
            x: 0,
            y: titleY,
            textAlign: 'center',
            wordWrapWidth: getLineWidth(titleY, titleHeight)
          }
        })
      )
    }
    const lineWidth = getLineWidth(contentY, contentHeight)
    // 内容与后缀共用底边，绘制后再补偿各自字形与字体基线之间的留白。
    const contentBottom = contentY + contentHeight / 2
    let suffixText: Text | undefined
    let suffixWidth = 0
    if (suffix) {
      suffixText = new Text({
        style: {
          ...textStyle(config.suffixStyle),
          text: suffix,
          y: contentBottom,
          textBaseline: 'ideographic',
          wordWrapWidth: lineWidth
        }
      })
      group.appendChild(suffixText)
      suffixWidth = suffixText.getBBox().width
    }
    let contentText: Text | undefined
    let contentWidth = 0
    if (content) {
      contentText = new Text({
        style: {
          ...textStyle(config.contentStyle),
          text: content,
          y: contentBottom,
          textBaseline: 'ideographic',
          wordWrapWidth: Math.max(1, lineWidth - suffixWidth)
        }
      })
      group.appendChild(contentText)
      contentWidth = contentText.getBBox().width
    }
    const lineStart = -(contentWidth + suffixWidth) / 2
    if (contentText) {
      contentText.style.x = lineStart
    }
    if (suffixText) {
      suffixText.style.x = lineStart + contentWidth
    }
    const measureContext = document.createElement('canvas').getContext('2d')
    if (measureContext) {
      // 使用原生表意基线避开 G 对 bottom 的模拟，并按实际字形底边对齐数字和中文。
      measureContext.textBaseline = 'ideographic'
      for (const text of [contentText, suffixText]) {
        const metrics = text?.parsedStyle.metrics
        if (!text || !metrics) {
          continue
        }
        measureContext.font = metrics.font
        const descent = measureContext.measureText(metrics.lines[0] || '').actualBoundingBoxDescent
        if (Number.isFinite(descent)) {
          text.style.y = contentBottom - descent
        }
      }
    }
  }

  protected configBasicStyle(chart: Chart, options: G2Spec): G2Spec {
    const tmp = super.configBasicStyle(chart, options)
    const { basicStyle } = parseJson(chart.customAttr)
    tmp.coordinate.innerRadius = basicStyle.innerRadius / 100
    return tmp
  }

  constructor() {
    super('pie-donut')
  }
}
