import { flow, hexColorToRGBA, parseJson } from '@/views/chart/components/js/util'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { useI18n } from '@/hooks/web/useI18n'
import { defaultsDeep, isEmpty } from 'lodash-es'
import { DEFAULT_LABEL } from '@/views/chart/components/editor/util/chart'
import { Chart as G2Chart, G2Spec } from '@antv/g2'
import { G2ChartView, G2DrawOptions } from '../../../types/impl/g2'
import {
  getG2Renderer,
  handleChartDashboardHidden,
  TOOLTIP_ITEM_TPL,
  TOOLTIP_TITLE_TPL
} from '../../../common/common_antv'

const { t } = useI18n()

export class Radar extends G2ChartView {
  properties: EditorProperty[] = [
    'background-overall-component',
    'border-style',
    'basic-style-selector',
    'label-selector',
    'tooltip-selector',
    'title-selector',
    'legend-selector',
    'misc-style-selector',
    'jump-set',
    'linkage'
  ]
  propertyInner: EditorPropertyInner = {
    'basic-style-selector': [
      'colors',
      'alpha',
      'radarShape',
      'seriesColor',
      'radarShowPoint',
      'radarPointSize',
      'radarAreaColor'
    ],
    'label-selector': ['seriesLabelFormatter'],
    'tooltip-selector': ['color', 'fontSize', 'backgroundColor', 'seriesTooltipFormatter', 'show'],
    'misc-style-selector': ['showName', 'color', 'fontSize', 'axisColor', 'axisValue'],
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
  selectorSpec: EditorSelectorSpec = {
    ...this['selectorSpec'],
    'misc-style-selector': {
      title: `${t('chart.tooltip_axis')}`
    }
  }
  axis: AxisType[] = ['xAxis', 'yAxis', 'drill', 'filter', 'extLabel', 'extTooltip']
  axisConfig: AxisConfig = {
    xAxis: {
      name: `${t('chart.drag_block_radar_label')} / ${t('chart.dimension')}`,
      type: 'd'
    },
    yAxis: {
      name: `${t('chart.drag_block_radar_length')} / ${t('chart.quota')}`,
      type: 'q'
    }
  }

  async drawChart(drawOptions: G2DrawOptions<G2Chart>): Promise<G2Chart> {
    const { chart, container, action } = drawOptions
    if (!chart.data?.data) {
      return
    }
    const data = chart.data.data
    const fieldValues = Array.from(new Set(data.map(item => item.field)))
    const categoryValues = Array.from(new Set(data.map(item => item.category)))
    // 过滤空值但保留原始维度与系列顺序，避免雷达轴和颜色映射重排
    const validData = data.filter(item => {
      const value = item.value
      return value !== null && value !== undefined && !Number.isNaN(Number(value))
    })
    const baseOptions: G2Spec = {
      type: 'view',
      autoFit: true,
      data: validData,
      coordinate: { type: 'polar' },
      children: [{ zIndex: -1, type: 'line', style: { lineWidth: 2 } }],
      encode: {
        x: 'field',
        y: 'value',
        color: 'category'
      },
      scale: {
        x: {
          domain: fieldValues,
          padding: 0.5,
          align: 0
        },
        y: {
          nice: true
        },
        color: {
          domain: categoryValues
        }
      }
    }
    const options = this.setupOptions(chart, baseOptions)
    const newChart = new G2Chart({ container, ...getG2Renderer() })
    handleChartDashboardHidden(chart, options)
    newChart.options(options)
    const handleClick = e => {
      const pointData = e?.data?.data
      if (!pointData) {
        return
      }
      action({
        ...e,
        x: e.x,
        y: e.y,
        data: {
          data: pointData
        }
      })
    }
    newChart.on('point:click', handleClick)
    if (options.children[0].labels?.length) {
      newChart.on('label:click', handleClick)
    }
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
    const basicStyle = parseJson(chart.customAttr).basicStyle
    const { seriesColor } = basicStyle
    if (seriesColor?.length) {
      const { yAxis } = chart
      const seriesMap = seriesColor.reduce((p, n) => {
        p[n.id] = n
        return p
      }, {})
      const colorRelations = []
      yAxis?.forEach(axis => {
        const curAxisColor = seriesMap[axis.id]
        if (curAxisColor) {
          colorRelations.push([
            axis.name ?? axis.chartShowName,
            hexColorToRGBA(curAxisColor.color, basicStyle.alpha)
          ])
        }
      })
      if (colorRelations.length) {
        defaultsDeep(options, {
          scale: {
            color: {
              relations: colorRelations
            }
          }
        })
      }
    }
    return options
  }

  protected configBasicStyle(chart: Chart, options: G2Spec): G2Spec {
    const { radarShowPoint, radarPointSize, radarAreaColor } = parseJson(
      chart.customAttr
    ).basicStyle

    if (radarShowPoint) {
      options.children.push({
        // G2 标签层固定为 0，数据点保持略低层级，确保标签不会被 point 覆盖
        zIndex: -0.5,
        type: 'point',
        encode: {
          x: 'field',
          y: 'value',
          color: 'category',
          shape: 'point',
          size: radarPointSize
        },
        tooltip: false
      })
    }
    if (radarAreaColor) {
      const areaBaseline = Number(options.scale?.y?.domainMin)
      options.children.push({
        zIndex: -2,
        type: 'area',
        encode: {
          x: 'field',
          y: 'value',
          // 面积层默认以 0 为基线，自动轴最小值不是 0 时会画到坐标外
          y1: Number.isFinite(areaBaseline) ? { type: 'constant', value: areaBaseline } : undefined,
          color: 'category'
        },
        style: {
          opacity: 0.5
        },
        tooltip: false
      })
    }
    return options
  }

  protected configLabel(chart: Chart, options: G2Spec): G2Spec {
    const customAttr = parseJson(chart.customAttr)
    const labelAttr = customAttr.label
    if (!labelAttr.show) {
      return options
    }
    const { radarShowPoint, radarPointSize } = customAttr.basicStyle
    const fieldDomain = Array.isArray(options.scale?.x?.domain) ? options.scale.x.domain : []
    const pointOffset = radarShowPoint ? Math.max(8, Number(radarPointSize) + 4) : 8
    const fieldPositionMap = new Map(
      fieldDomain.map((field, index) => {
        const angle = (index / fieldDomain.length) * Math.PI * 2
        const dx = Math.sin(angle) * pointOffset
        const dy = -Math.cos(angle) * pointOffset
        return [
          field,
          {
            dx,
            dy,
            textAlign: Math.abs(dx) < 1 ? 'center' : dx > 0 ? 'start' : 'end',
            textBaseline: Math.abs(dy) < 1 ? 'middle' : dy > 0 ? 'top' : 'bottom'
          }
        ] as const
      })
    )
    const getLabelPosition = data => {
      const position = fieldPositionMap.get(data.field)
      if (position) {
        return position
      }
      return {
        dx: 0,
        dy: -pointOffset,
        textAlign: 'center',
        textBaseline: 'bottom'
      }
    }
    const formatterMap = labelAttr.seriesLabelFormatter?.reduce((pre, next) => {
      pre[next.id] = next
      return pre
    }, {})
    const lineMark = options.children[0]
    const label = {
      text: 'value',
      transform: labelAttr.fullDisplay === true ? [] : [{ type: 'overlapHide' }],
      style: {
        fill: data => {
          const color = DEFAULT_LABEL.color
          if (!labelAttr.seriesLabelFormatter?.length) {
            return color
          }
          const labelCfg = formatterMap?.[data.quotaList[0].id] as SeriesFormatter
          if (!labelCfg?.show) {
            return color
          }
          return labelCfg.color
        },
        fontSize: data => {
          const fontSize = DEFAULT_LABEL.fontSize
          if (!labelAttr.seriesLabelFormatter?.length) {
            return fontSize
          }
          const labelCfg = formatterMap?.[data.quotaList[0].id] as SeriesFormatter
          if (!labelCfg?.show) {
            return fontSize
          }
          return labelCfg.fontSize
        },
        // 极坐标折线标签默认与数据点重合，按雷达轴方向向圆外偏移
        dx: data => getLabelPosition(data).dx,
        dy: data => getLabelPosition(data).dy,
        textAlign: data => getLabelPosition(data).textAlign,
        textBaseline: data => getLabelPosition(data).textBaseline,
        opacity: 1
      },
      formatter: (value, data) => {
        if (!labelAttr.seriesLabelFormatter?.length) {
          return value
        }
        const labelCfg = formatterMap?.[data.quotaList[0].id] as SeriesFormatter
        if (!labelCfg) {
          return value
        }
        if (!labelCfg.show) {
          return ''
        }
        const result = valueFormatter(value, labelCfg.formatterCfg)
        return result
      }
    }
    lineMark.labels = [label]
    return options
  }

  protected configAxis(chart: Chart, options: G2Spec): G2Spec {
    const customAttr = parseJson(chart.customAttr)
    const customStyle = parseJson(chart.customStyle)
    const basicStyle = customAttr.basicStyle
    const misc = customStyle.misc
    const axis = {
      axis: {
        x: {
          grid: true,
          tick: true,
          tickLength: 10,
          tickStrokeOpacity: 0,
          label: misc.showName,
          labelFill: misc.color,
          labelFontSize: misc.fontSize,
          labelAlign: 'horizontal',
          labelOpacity: 1,
          gridLineDash: [0, 0],
          gridStroke: misc.axisColor,
          gridStrokeOpacity: 1,
          gridLineWidth: 1
        },
        y: {
          zIndex: -1,
          title: false,
          labelFilter: () => false,
          gridConnect: basicStyle.radarShape === 'polygon' ? 'line' : undefined,
          gridStroke: misc.axisColor,
          gridStrokeOpacity: 1,
          gridLineWidth: 1,
          gridLineDash: [0, 0]
        }
      }
    }
    defaultsDeep(options, axis)
    options.inset = misc.showName ? 24 : 8
    const axisValue = misc.axisValue
    const data = Array.isArray(options.data) ? options.data : []
    const dataRange = data.reduce(
      (range, item) => {
        const value = Number(item?.value)
        if (Number.isFinite(value)) {
          range.min = Math.min(range.min, value)
          range.max = Math.max(range.max, value)
        }
        return range
      },
      { min: Infinity, max: -Infinity }
    )
    const tickMethod = (min, max, count = 5) => {
      if (min === max) {
        return [min]
      }
      const splitCount = Math.max(1, count)
      const step = (max - min) / splitCount
      const ticks = []
      for (let i = 0; i <= splitCount; i++) {
        ticks.push(min + step * i)
      }
      return ticks
    }
    if (axisValue?.auto !== false) {
      if (Number.isFinite(dataRange.min) && Number.isFinite(dataRange.max)) {
        const dataMin = dataRange.min
        const dataMax = dataRange.max
        const splitCount = Math.max(1, Number(misc.splitNumber) || 5)
        const padding = (dataMax - dataMin || Math.abs(dataMax) || 1) / splitCount
        options.scale.y = {
          ...options.scale.y,
          // G2 的 nice 会同时扩展最小值，这里只固定最小值并给最大值留出一格空间
          nice: false,
          domainMin: dataMin,
          domainMax: dataMax + padding,
          tickCount: splitCount,
          tickMethod
        }
      }
    } else {
      const yScale = {
        scale: {
          y: {
            domainMin: axisValue.min,
            domainMax: axisValue.max,
            tickCount: axisValue.splitCount,
            tickMethod
          }
        }
      }
      defaultsDeep(options, yScale)
    }
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

  protected configTooltip(chart: Chart, options: G2Spec): G2Spec {
    const customAttr: DeepPartial<ChartAttr> = parseJson(chart.customAttr)
    const tooltipAttr = customAttr.tooltip
    const yAxis = chart.yAxis
    const lineMark = options.children[0]
    if (!tooltipAttr.show) {
      defaultsDeep(lineMark, { tooltip: false })
      return options
    }
    const formatterMap = tooltipAttr.seriesTooltipFormatter
      ?.filter(i => i.show)
      .reduce((pre, next) => {
        pre[next.id] = next
        return pre
      }, {}) as Record<string, SeriesFormatter>
    let g2TooltipWrapper = document.getElementById('G2-TOOLTIP-WRAPPER')
    if (!g2TooltipWrapper) {
      g2TooltipWrapper = document.createElement('div')
      g2TooltipWrapper.id = 'G2-TOOLTIP-WRAPPER'
      g2TooltipWrapper.style.position = 'absolute'
      g2TooltipWrapper.style.pointerEvents = 'none'
      g2TooltipWrapper.style.zIndex = '9999'
      document.body.appendChild(g2TooltipWrapper)
    }
    const tooltipOptions: G2Spec = {
      tooltip: d => d,
      interaction: {
        tooltip: {
          crosshairsLineDash: [4, 4],
          mount: g2TooltipWrapper,
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
    defaultsDeep(lineMark, tooltipOptions)
    return options
  }

  protected setupOptions(chart: Chart, options: G2Spec): G2Spec {
    return flow(
      this.configTheme,
      this.configColor,
      this.configLabel,
      this.configLegend,
      this.configTooltip,
      this.configAxis,
      this.configBasicStyle
    )(chart, options, {}, this)
  }

  constructor() {
    super('radar', [])
  }
}
