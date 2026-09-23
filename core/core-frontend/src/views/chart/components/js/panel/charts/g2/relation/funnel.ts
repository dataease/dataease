import { G2ChartView, G2DrawOptions } from '../../../types/impl/g2'
import {
  flow,
  hexColorToRGBA,
  parseJson,
  setUpSingleDimensionSeriesColor
} from '@/views/chart/components/js/util'
import {
  getG2Renderer,
  handleChartDashboardHidden,
  TOOLTIP_ITEM_TPL,
  TOOLTIP_TITLE_TPL
} from '../../../common/common_antv'
import { useI18n } from '@/hooks/web/useI18n'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { defaultsDeep, isEmpty } from 'lodash-es'
import { Chart as G2Chart, G2Spec } from '@antv/g2'
import { createTooltipWrapper } from '../bar/barUtil'

const { t } = useI18n()

/**
 * 漏斗图
 */
export class Funnel extends G2ChartView {
  properties: EditorProperty[] = [
    'background-overall-component',
    'border-style',
    'basic-style-selector',
    'label-selector',
    'tooltip-selector',
    'title-selector',
    'legend-selector',
    'jump-set',
    'linkage'
  ]
  propertyInner: EditorPropertyInner = {
    'background-overall-component': ['all'],
    'border-style': ['all'],
    'basic-style-selector': ['colors', 'alpha', 'seriesColor'],
    'label-selector': ['fontSize', 'color', 'hPosition', 'showQuota', 'conversionTag'],
    'tooltip-selector': ['color', 'fontSize', 'backgroundColor', 'seriesTooltipFormatter', 'show'],
    'title-selector': [
      'show',
      'title',
      'fontSize',
      'color',
      'hPosition',
      'isItalic',
      'isBolder',
      'remarkShow',
      'fontFamily',
      'letterSpace',
      'fontShadow'
    ],
    'legend-selector': ['icon', 'orient', 'color', 'fontSize', 'hPosition', 'vPosition']
  }
  axis: AxisType[] = ['xAxis', 'yAxis', 'filter', 'drill', 'extLabel', 'extTooltip']
  axisConfig: AxisConfig = {
    xAxis: {
      name: `${t('chart.drag_block_funnel_split')} / ${t('chart.dimension')}`,
      type: 'd'
    },
    yAxis: {
      name: `${t('chart.drag_block_funnel_width')} / ${t('chart.quota')}`,
      type: 'q',
      limit: 1
    }
  }

  async drawChart(drawOptions: G2DrawOptions<G2Chart>): Promise<G2Chart> {
    const { chart, container, action } = drawOptions
    if (!chart.data?.data) {
      return
    }
    const data = chart.data.data
    const baseOptions: G2Spec = {
      type: 'interval',
      autoFit: true,
      data,
      encode: { x: 'field', y: 'value', color: 'field', shape: 'funnel' },
      transform: [{ type: 'symmetryY' }],
      scale: { x: { paddingOuter: 0, paddingInner: 0 } },
      coordinate: { transform: [{ type: 'transpose' }] },
      axis: false,
      labels: []
    }
    const options = this.setupOptions(chart, baseOptions)
    const newChart = new G2Chart({ container, ...getG2Renderer() })
    handleChartDashboardHidden(chart, options)
    newChart.options(options)
    let labelFrame: number | undefined
    newChart.on('afterrender', () => {
      if (labelFrame !== undefined) cancelAnimationFrame(labelFrame)
      labelFrame = requestAnimationFrame(() => {
        labelFrame = undefined
        const canvas = newChart.getContext().canvas
        if (!canvas) return
        const { width, height } = canvas.getConfig()
        hideFunnelLabelOverlaps(
          canvas.document.querySelectorAll('.label'),
          Number(width),
          Number(height),
          parseJson(chart.customAttr).label.fullDisplay === true
        )
      })
    })
    newChart.on('afterdestroy', () => {
      if (labelFrame !== undefined) cancelAnimationFrame(labelFrame)
    })
    newChart.on('interval:click', action)
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
    return { ...options, theme }
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

  protected configLabel(chart: Chart, options: G2Spec): G2Spec {
    const { label } = parseJson(chart.customAttr)
    if (!label.show) {
      return options
    }
    // 数据标签颜色按主题配置原值渲染，避免叠加 G2 默认透明度
    if (label.showQuota) {
      options.labels.push({
        text: d => {
          return valueFormatter(d.value, label.quotaLabelFormatter)
        },
        position: label.position === 'middle' ? 'inside' : label.position,
        fontSize: label.fontSize,
        fill: label.color,
        fillOpacity: 1
      })
    }
    if (label.conversionTag?.show) {
      const conversionTagArr = [
        {
          text: (_, i, data) => {
            if (i === 0) {
              return ''
            }
            const pre = data[i - 1].value
            const next = data[i].value
            const rate = `${((next / pre) * 100).toFixed(label.conversionTag.precision)}%`
            return (label.conversionTag.text ?? '转换率 ') + rate
          },
          position: 'top-right',
          textAlign: 'left',
          textBaseline: 'middle',
          dx: 60,
          connector: true,
          connectorStroke: '#aaa',
          fontSize: label.fontSize,
          fill: label.color,
          fillOpacity: 1
        }
      ]
      options.labels.push(...conversionTagArr)
      options.paddingRight = 120
    }
    // 所有标签统一避让，转换率文字和原生引导线作为一个图形共同调整、隐藏。
    options.labelTransform =
      label.fullDisplay === true ? [] : [{ type: 'exceedAdjust' }, { type: 'overlapHide' }]
    return options
  }

  protected configTooltip(chart: Chart, options: G2Spec): G2Spec {
    const customAttr: DeepPartial<ChartAttr> = parseJson(chart.customAttr)
    const tooltipAttr = customAttr.tooltip
    const yAxis = chart.yAxis
    if (!tooltipAttr.show) {
      return {
        ...options,
        tooltip: false
      }
    }
    const formatterMap = tooltipAttr.seriesTooltipFormatter
      ?.filter(i => i.show)
      .reduce((pre, next) => {
        pre[next.id] = next
        return pre
      }, {}) as Record<string, SeriesFormatter>
    const tooltipOptions: G2Spec = {
      tooltip: d => d,
      interaction: {
        tooltip: {
          crosshairsLineDash: [4, 4],
          mount: createTooltipWrapper(chart),
          css: {
            '.g2-tooltip': {
              background: tooltipAttr.backgroundColor
            },
            '.g2-tooltip-title': {
              color: tooltipAttr.color,
              'font-size': `${tooltipAttr.fontSize}px`
            },
            '.g2-tooltip-list-item-name-label': {
              color: tooltipAttr.color,
              'font-size': `${tooltipAttr.fontSize}px`
            },
            '.g2-tooltip-list-item-value': {
              color: tooltipAttr.color,
              'font-size': `${tooltipAttr.fontSize}px`
            }
          },
          render: (e, { title, items: originalItems }) => {
            const titleHtml = TOOLTIP_TITLE_TPL.replace('{title}', title)
            let tooltipItems = originalItems
            if (tooltipAttr.seriesTooltipFormatter?.length) {
              tooltipItems = originalItems.filter(item => formatterMap[item.quotaList[0].id])
            }
            const result = []
            const head = originalItems[0]
            tooltipItems.forEach(item => {
              const formatter = formatterMap[item.quotaList[0].id] ?? yAxis[0]
              const value = valueFormatter(item.value, formatter.formatterCfg)
              const name = isEmpty(formatter.chartShowName)
                ? formatter.name
                : formatter.chartShowName
              result.push({ ...item, name, value })
            })
            head.dynamicTooltipValue?.forEach(item => {
              const formatter = formatterMap[item.fieldId]
              if (formatter) {
                const value = valueFormatter(parseFloat(item.value), formatter.formatterCfg)
                const name = isEmpty(formatter.chartShowName)
                  ? formatter.name
                  : formatter.chartShowName
                result.push({ color: 'grey', name, value })
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
    defaultsDeep(options, tooltipOptions)
    return options
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

  public setupSeriesColor(chart: ChartObj, data?: any[]): ChartBasicStyle['seriesColor'] {
    return setUpSingleDimensionSeriesColor(chart, data)
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    const { customAttr, customStyle } = chart
    const { label } = customAttr
    if (!['left', 'middle', 'right'].includes(label.position)) {
      label.position = 'middle'
    }
    customAttr.label = {
      ...label,
      show: true,
      showQuota: true,
      conversionTag: {
        show: false,
        precision: 2,
        text: t('chart.conversion_rate')
      }
    }
    const { legend } = customStyle
    legend.show = false
    return chart
  }

  protected setupOptions(chart: Chart, options: G2Spec): G2Spec {
    return flow(
      this.configTheme,
      this.configColor,
      this.configLabel,
      this.configTooltip,
      this.configLegend
    )(chart, options, {}, this)
  }

  constructor() {
    super('funnel', [])
  }
}

// 在真实文字完成布局后检查，避免初始化阶段的空边界让避让失效。
export function hideFunnelLabelOverlaps(
  labels: any[],
  width: number,
  height: number,
  fullDisplay = false
) {
  const occupied: number[][] = []
  labels.forEach(label => {
    const text = label.querySelector('text')
    const bounds = text?.getBounds()
    // 首层转换率为空，仍可能生成引导线；空文字必须连同整个标签组隐藏。
    if (
      !String(text?.style.text ?? '').trim() ||
      !bounds ||
      bounds.max[0] <= bounds.min[0] ||
      bounds.max[1] <= bounds.min[1]
    ) {
      setFunnelLabelVisibility(label, 'hidden')
      return
    }
    const box = [bounds.min[0], bounds.min[1], bounds.max[0], bounds.max[1]]
    if (fullDisplay) {
      // 全量模式不隐藏标签，只将越界文字移回画布。同步补偿引导线锚点。
      const dx = box[0] < 2 ? 2 - box[0] : Math.min(0, width - 2 - box[2])
      const dy = box[1] < 2 ? 2 - box[1] : Math.min(0, height - 2 - box[3])
      if (dx || dy) {
        if (label.style.connector && label.style.connectorPoints?.length) {
          const points = label.style.connectorPoints.map(point => [...point])
          points[0][0] -= dx
          points[0][1] -= dy
          label.style.connectorPoints = points
        }
        label.style.x = Number(label.style.x || 0) + dx
        label.style.y = Number(label.style.y || 0) + dy
      }
      setFunnelLabelVisibility(label, 'visible')
      return
    }
    const outside = box[0] < 0 || box[1] < 0 || box[2] > width || box[3] > height
    const overlap = occupied.some(
      other =>
        box[0] < other[2] + 2 &&
        box[2] + 2 > other[0] &&
        box[1] < other[3] + 2 &&
        box[3] + 2 > other[1]
    )
    setFunnelLabelVisibility(label, outside || overlap ? 'hidden' : 'visible')
    if (!outside && !overlap) occupied.push(box)
  })
}

// G 的子图形可以显式设置 visibility，必须递归同步，不能仅隐藏父组。
function setFunnelLabelVisibility(label: any, visibility: 'visible' | 'hidden') {
  label.style.visibility = visibility
  label.children?.forEach(child => setFunnelLabelVisibility(child, visibility))
}
