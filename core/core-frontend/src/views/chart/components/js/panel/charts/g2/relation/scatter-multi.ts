import { G2ChartView, G2DrawOptions } from '../../../types/impl/g2'
import { flow, hexColorToRGBA, parseJson } from '@/views/chart/components/js/util'
import {
  getG2Renderer,
  handleChartDashboardHidden,
  TOOLTIP_ITEM_TPL,
  TOOLTIP_TITLE_TPL
} from '../../../common/common_antv'
import { useI18n } from '@/hooks/web/useI18n'
import { defaultsDeep, isEmpty, toString } from 'lodash-es'
import { Chart as G2Chart, G2Spec } from '@antv/g2'
import { valueFormatter } from '../../../../formatter'
import { createTooltipWrapper } from '../bar/barUtil'

const { t } = useI18n()
const DEFAULT_LIGHTNESS_RANGE = [0.25, 1]
const DEFAULT_BUBBLE_SIZE_RANGE = [5, 30]
const LABEL_TEXT_FIELD = '__multiScatterLabelText'

const safeToNumber = (value: unknown): number | undefined => {
  if (value === null || value === undefined || value === '') {
    return undefined
  }
  const num = Number(value)
  return Number.isFinite(num) ? num : undefined
}

const normalizeRatio = (value: number, min: number, max: number) => {
  if (!Number.isFinite(value) || !Number.isFinite(min) || !Number.isFinite(max) || max === min) {
    return 0.5
  }
  return Math.max(0, Math.min(1, (value - min) / (max - min)))
}

const getFieldName = (field?: Axis) => {
  if (!field) {
    return ''
  }
  return isEmpty(field.chartShowName) ? field.name : field.chartShowName
}

const isCssColor = (value: unknown) => {
  if (typeof value !== 'string') {
    return false
  }
  return /^(#|rgb\(|rgba\(|hsl\(|hsla\()/i.test(value) || ['grey', 'gray'].includes(value)
}

const estimateLineTextWidth = (text: string, fontSize: number) => {
  return Array.from(text).reduce((width, char) => {
    if (/\s/.test(char)) {
      return width + fontSize * 0.35
    }
    if (/[^\x00-\xff]/.test(char)) {
      return width + fontSize
    }
    return width + fontSize * 0.6
  }, 0)
}

const isTextInCircle = (text: string, fontSize: number, radius: number) => {
  if (!text || radius <= 0) {
    return false
  }
  const lines = text.split('\n')
  const lineHeight = fontSize * 1.2
  const halfWidth = Math.max(...lines.map(line => estimateLineTextWidth(line, fontSize))) / 2
  const halfHeight = ((lines.length - 1) * lineHeight + fontSize) / 2
  return halfWidth * halfWidth + halfHeight * halfHeight <= radius * radius
}

const getMultilineTextBounds = (text: string, fontSize: number) => {
  const lines = text.split('\n')
  const lineHeight = fontSize * 1.2
  return {
    width: Math.max(...lines.map(line => estimateLineTextWidth(line, fontSize))),
    height: (lines.length - 1) * lineHeight + fontSize
  }
}

const getContainerSize = (container: unknown) => {
  const el =
    typeof container === 'string'
      ? document.getElementById(container)
      : container instanceof HTMLElement
      ? container
      : undefined
  return {
    containerWidth: el?.clientWidth || 0,
    containerHeight: el?.clientHeight || 0
  }
}

const isRectOverlap = (
  a: { left: number; right: number; top: number; bottom: number },
  b: { left: number; right: number; top: number; bottom: number }
) => {
  return !(a.right <= b.left || a.left >= b.right || a.bottom <= b.top || a.top >= b.bottom)
}

const formatLabelValue = (value: unknown, formatter?: Record<string, any>) => {
  if (!formatter || value === null || value === undefined || value === '') {
    return toString(value)
  }
  const formatterType = formatter.type
  if (['auto', 'value', 'percent'].includes(formatterType) && safeToNumber(value) === undefined) {
    return toString(value)
  }
  return toString(valueFormatter(value, formatter))
}

/**
 * 多维散点图
 */
export class MultiScatter extends G2ChartView {
  properties: EditorProperty[] = [
    'background-overall-component',
    'border-style',
    'basic-style-selector',
    'x-axis-selector',
    'y-axis-selector',
    'title-selector',
    'label-selector',
    'tooltip-selector',
    'legend-selector',
    'jump-set',
    'linkage'
  ]
  propertyInner: EditorPropertyInner = {
    'basic-style-selector': [
      'colors',
      'alpha',
      'scatterSymbol',
      'scatterSymbolSize',
      'seriesColor'
    ],
    'label-selector': ['fontSize', 'color', 'labelFormatter'],
    'tooltip-selector': ['fontSize', 'color', 'backgroundColor', 'seriesTooltipFormatter', 'show'],
    'x-axis-selector': [
      'position',
      'name',
      'color',
      'fontSize',
      'axisLine',
      'splitLine',
      'axisForm',
      'axisLabel',
      'axisLabelFormatter'
    ],
    'y-axis-selector': [
      'position',
      'name',
      'color',
      'fontSize',
      'axisValue',
      'axisLine',
      'splitLine',
      'axisForm',
      'axisLabel',
      'axisLabelFormatter'
    ],
    'title-selector': [
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
  axis: AxisType[] = [
    'extColor',
    'xAxis',
    'yAxis',
    'extBubble',
    'yAxisExt',
    'filter',
    'extLabel',
    'extTooltip'
  ]
  axisConfig: AxisConfig = {
    extColor: {
      name: `${t('chart.color')} / ${t('chart.dimension')}`,
      type: 'd',
      limit: 1,
      allowEmpty: false
    },
    xAxis: {
      name: `${t('chart.x_axis')} / ${t('chart.time_dimension_or_quota')}`,
      limit: 1,
      allowEmpty: false
    },
    yAxis: {
      ...this['axisConfig'].yAxis,
      name: `${t('chart.y_axis')} / ${t('chart.quota')}`,
      limit: 1,
      allowEmpty: false
    },
    yAxisExt: {
      name: `${t('chart.lightness')} / ${t('chart.quota')}`,
      type: 'q',
      limit: 1,
      allowEmpty: true
    },
    extBubble: {
      name: `${t('chart.radar_size')} / ${t('chart.quota')}`,
      type: 'q',
      limit: 1,
      allowEmpty: true
    }
  }

  async drawChart(drawOptions: G2DrawOptions<G2Chart>): Promise<G2Chart> {
    const { chart, container, action } = drawOptions
    if (!chart.data?.data) {
      return
    }
    const scatterContext = this.buildScatterDataContext(
      chart,
      chart.data.data as Record<string, unknown>[]
    )
    Object.assign(scatterContext, getContainerSize(container))
    const baseOptions: G2Spec = {
      type: 'point',
      data: {
        value: scatterContext.data
      },
      autoFit: true,
      encode: {
        x: scatterContext.xField,
        y: scatterContext.yField,
        color: scatterContext.colorField
      },
      scale: {
        [scatterContext.xField]: scatterContext.isTimeX
          ? {
              type: 'point'
            }
          : {
              nice: true
            },
        [scatterContext.yField]: {
          nice: true
        }
      },
      legend: {
        size: false
      }
    }
    const options: G2Spec = this.setupOptions(chart, baseOptions, scatterContext)
    const newChart = new G2Chart({ container, ...getG2Renderer() })
    handleChartDashboardHidden(chart, options)
    newChart.options(options)
    newChart.on('point:click', action)
    if ((options as any).labels) {
      newChart.on('label:click', e => {
        action({
          x: e.x,
          y: e.y,
          data: {
            data: e.data.data
          }
        })
      })
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
    return {
      ...options,
      theme: {
        color: colors[0],
        category10: colors,
        category20: colors,
        view: {
          viewFill: bgColor
        }
      }
    }
  }

  protected configColor(chart: Chart, options: G2Spec): G2Spec {
    const basicStyle = parseJson(chart.customAttr).basicStyle
    const { seriesColor } = basicStyle
    if (!seriesColor?.length) {
      return options
    }
    const relations = seriesColor.map(item => [
      item.id,
      hexColorToRGBA(item.color, basicStyle.alpha)
    ])
    return defaultsDeep(options, {
      scale: {
        color: {
          relations
        }
      }
    })
  }

  protected configBasicStyle(chart: Chart, options: G2Spec, context: Record<string, any>): G2Spec {
    const basicStyle = parseJson(chart.customAttr).basicStyle
    const data = (context?.data || []) as Record<string, unknown>[]
    const hasBubbleMetric = data.some(item => Number.isFinite(Number(item.popSize)))
    const lightValues = data
      .map(item => safeToNumber(item.lightness))
      .filter((v): v is number => Number.isFinite(v))
    const hasLightnessMetric = lightValues.length > 0
    const sizeOptions = {
      encode: {
        shape: {
          type: 'constant',
          value: basicStyle.scatterSymbol
        }
      }
    }
    defaultsDeep(options, {
      style: {
        stroke: 'transparent',
        strokeOpacity: 0,
        lineWidth: 0
      }
    })
    if (chart.extBubble?.length || hasBubbleMetric) {
      defaultsDeep(options, {
        encode: {
          size: 'popSize'
        },
        scale: {
          size: {
            range: DEFAULT_BUBBLE_SIZE_RANGE
          }
        }
      })
    } else {
      defaultsDeep(options, {
        encode: {
          size: {
            type: 'constant',
            value: basicStyle.scatterSymbolSize
          }
        }
      })
    }
    defaultsDeep(options, sizeOptions)
    if (hasLightnessMetric) {
      const min = Math.min(...lightValues)
      const max = Math.max(...lightValues)
      const [opacityMin, opacityMax] = DEFAULT_LIGHTNESS_RANGE
      defaultsDeep(options, {
        style: {
          fillOpacity: datum => {
            const lightness = safeToNumber(datum.lightness)
            const ratio = lightness !== undefined ? normalizeRatio(lightness, min, max) : 0.5
            return opacityMin + ratio * (opacityMax - opacityMin)
          },
          strokeOpacity: 0,
          shadowBlur: 10
        }
      })
    }
    return options
  }

  protected configLabel(chart: Chart, options: G2Spec, context: Record<string, any>): G2Spec {
    const { label } = parseJson(chart.customAttr)
    if (!label.show) {
      return options
    }
    const basicStyle = parseJson(chart.customAttr).basicStyle
    const labelFontSize = safeToNumber(label.fontSize) ?? 12
    const labelLineHeight = labelFontSize * 1.2
    const xAxisFormatter = chart.xAxis?.[0]?.formatterCfg
    const yAxisFormatter = chart.yAxis?.[0]?.formatterCfg
    const labelFormatter = label.labelFormatter
    const data = (context?.data || []) as Record<string, unknown>[]
    const popSizes = data
      .map(item => safeToNumber(item.popSize))
      .filter((v): v is number => Number.isFinite(v))
    const minPopSize = popSizes.length ? Math.min(...popSizes) : 0
    const maxPopSize = popSizes.length ? Math.max(...popSizes) : 0
    const hasBubbleMetric = chart.extBubble?.length || popSizes.length > 0
    const getBubbleRadius = (datum: Record<string, unknown>) => {
      if (!hasBubbleMetric) {
        return safeToNumber(basicStyle.scatterSymbolSize) ?? 0
      }
      const popSize = safeToNumber(datum.popSize)
      const ratio = popSize !== undefined ? normalizeRatio(popSize, minPopSize, maxPopSize) : 0.5
      const [sizeMin, sizeMax] = DEFAULT_BUBBLE_SIZE_RANGE
      return sizeMin + ratio * (sizeMax - sizeMin)
    }
    const buildLabelText = (datum: Record<string, unknown>) => {
      const xValue = datum.xLabel ?? datum.x
      const yValue = datum.y ?? datum.value
      return `${formatLabelValue(xValue, labelFormatter ?? xAxisFormatter)}\n${formatLabelValue(
        yValue,
        labelFormatter ?? yAxisFormatter
      )}`
    }
    if (!label.fullDisplay) {
      const width = Number(context?.containerWidth) || 300
      const height = Number(context?.containerHeight) || 200
      const plotLeft = 60
      const plotTop = 35
      const plotWidth = Math.max(1, width - 120)
      const plotHeight = Math.max(1, height - 90)
      const xCategories = Array.from(new Set(data.map(item => toString(item.x))))
      const xValues = data
        .map(item => safeToNumber(item.x))
        .filter((v): v is number => Number.isFinite(v))
      const yValues = data
        .map(item => safeToNumber(item.y))
        .filter((v): v is number => Number.isFinite(v))
      const minX = xValues.length ? Math.min(...xValues) : 0
      const maxX = xValues.length ? Math.max(...xValues) : 0
      const minY = yValues.length ? Math.min(...yValues) : 0
      const maxY = yValues.length ? Math.max(...yValues) : 0
      const acceptedRects: { left: number; right: number; top: number; bottom: number }[] = []
      const getPointPosition = (datum: Record<string, unknown>) => {
        let xRatio: number
        if (context?.isTimeX) {
          const index = xCategories.indexOf(toString(datum.x))
          xRatio = xCategories.length > 1 ? index / (xCategories.length - 1) : 0.5
        } else {
          xRatio = normalizeRatio(safeToNumber(datum.x) ?? 0, minX, maxX)
        }
        const yRatio = normalizeRatio(safeToNumber(datum.y) ?? 0, minY, maxY)
        return {
          x: plotLeft + xRatio * plotWidth,
          y: plotTop + (1 - yRatio) * plotHeight
        }
      }
      data.forEach(item => {
        const text = buildLabelText(item)
        if (!isTextInCircle(text, labelFontSize, getBubbleRadius(item))) {
          item[LABEL_TEXT_FIELD] = ''
          return
        }
        const { width: textWidth, height: textHeight } = getMultilineTextBounds(text, labelFontSize)
        const position = getPointPosition(item)
        const rect = {
          left: position.x - textWidth / 2,
          right: position.x + textWidth / 2,
          top: position.y - textHeight / 2,
          bottom: position.y + textHeight / 2
        }
        if (acceptedRects.some(accepted => isRectOverlap(rect, accepted))) {
          item[LABEL_TEXT_FIELD] = ''
          return
        }
        acceptedRects.push(rect)
        item[LABEL_TEXT_FIELD] = text
      })
    }
    return defaultsDeep(options, {
      labels: [
        {
          text: d => {
            if (label.fullDisplay) {
              return buildLabelText(d)
            }
            return d[LABEL_TEXT_FIELD] || ''
          },
          position: 'inside',
          style: {
            fill: label.color,
            fontSize: label.fontSize,
            lineHeight: labelLineHeight,
            textAlign: 'center',
            textBaseline: 'middle',
            fillOpacity: 1,
            pointerEvents: 'none'
          },
          transform: label.fullDisplay ? [] : [{ type: 'overlapHide' }]
        }
      ]
    })
  }

  protected configTooltip(chart: Chart, options: G2Spec, context: Record<string, any>): G2Spec {
    const customAttr: DeepPartial<ChartAttr> = parseJson(chart.customAttr)
    const tooltipAttr = customAttr.tooltip
    if (!tooltipAttr.show) {
      return { ...options, tooltip: false }
    }
    const xAxisField = chart.xAxis?.[0]
    const isTimeX =
      !!context?.isTimeX ||
      (xAxisField &&
        (xAxisField.groupType === 'd' || (xAxisField.deType != null && xAxisField.deType === 1)))
    const formatterMap = tooltipAttr.seriesTooltipFormatter
      ?.filter(i => i.show)
      .reduce((pre, next) => {
        pre[next.seriesId] = next
        pre[next.id] = next
        return pre
      }, {}) as Record<string, SeriesFormatter>
    const tooltipOptions: G2Spec = {
      tooltip: d => d,
      interaction: {
        tooltip: {
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
          render: (_, { items }) => {
            const head = (items as unknown as Record<string, any>[])[0] || {}
            const title = head.category ?? head.field ?? ''
            const titleHtml = TOOLTIP_TITLE_TPL.replace('{title}', title)
            const result = []
            const markerColor =
              head.__markerColor || (isCssColor(head.color) ? head.color : '#999999')
            const xValue = head.xLabel ?? head.x
            if (xAxisField) {
              const formatter =
                formatterMap?.[`${xAxisField.id}-xAxis`] ??
                formatterMap?.[xAxisField.id] ??
                xAxisField
              result.push({
                color: markerColor,
                name: getFieldName(xAxisField),
                value:
                  isTimeX || typeof xValue === 'string'
                    ? toString(xValue)
                    : formatLabelValue(xValue, formatter.formatterCfg)
              })
            }
            if (chart.yAxis?.[0]) {
              const formatter = formatterMap?.[`${chart.yAxis[0].id}-yAxis`] ?? chart.yAxis[0]
              result.push({
                color: markerColor,
                name: getFieldName(chart.yAxis[0]),
                value: valueFormatter(head.y ?? head.value, formatter.formatterCfg)
              })
            }
            if (chart.extBubble?.[0]) {
              const formatter =
                formatterMap?.[`${chart.extBubble[0].id}-extBubble`] ?? chart.extBubble[0]
              result.push({
                color: markerColor,
                name: getFieldName(chart.extBubble[0]),
                value: valueFormatter(head.popSize, formatter.formatterCfg)
              })
            }
            if (chart.yAxisExt?.[0]) {
              const formatter =
                formatterMap?.[`${chart.yAxisExt[0].id}-yAxisExt`] ?? chart.yAxisExt[0]
              result.push({
                color: markerColor,
                name: getFieldName(chart.yAxisExt[0]),
                value: valueFormatter(head.lightness, formatter.formatterCfg)
              })
            }
            head.dynamicTooltipValue?.forEach(item => {
              const formatter = formatterMap?.[item.fieldId]
              if (formatter) {
                result.push({
                  color: 'grey',
                  name: isEmpty(formatter.chartShowName) ? formatter.name : formatter.chartShowName,
                  value: formatLabelValue(item.stringValue ?? item.value, formatter.formatterCfg)
                })
              }
            })
            const itemsHtml = result
              .map(item => {
                return TOOLTIP_ITEM_TPL.replace('{marker}', item.color)
                  .replace('{label}', item.name)
                  .replace('{value}', item.value)
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

  protected configXAxis(chart: Chart, options: G2Spec): G2Spec {
    const { xAxis } = parseJson(chart.customStyle)
    if (!xAxis.show) {
      return defaultsDeep(options, { axis: { x: false } })
    }
    return defaultsDeep(options, {
      axis: {
        x: {
          ...this.getAxisStyle(chart, xAxis),
          labelFormatter: d => formatLabelValue(d, xAxis.axisLabelFormatter)
        }
      }
    })
  }

  protected configYAxis(chart: Chart, options: G2Spec): G2Spec {
    const { xAxis, yAxis } = parseJson(chart.customStyle)
    if (!yAxis.show) {
      return defaultsDeep(options, { axis: { y: false } })
    }
    const axisOption = {
      axis: {
        y: {
          ...this.getAxisStyle(chart, yAxis),
          dataeaseAxisTitleSafeMargin: true,
          ...this.getOverlapGridFilter(xAxis),
          labelFormatter: d => valueFormatter(d, yAxis.axisLabelFormatter)
        }
      }
    }
    if (!yAxis.axisValue.auto) {
      defaultsDeep(axisOption, {
        scale: {
          y: {
            nice: false,
            domainMin: yAxis.axisValue.min,
            domainMax: yAxis.axisValue.max,
            tickCount: yAxis.axisValue.splitCount < 2 ? 2 : yAxis.axisValue.splitCount,
            tickMethod: (min, max, count) => {
              const n = Math.max(2, count)
              const step = (max - min) / (n - 1)
              const ticks = []
              for (let i = 0; i < n; i++) {
                ticks.push(min + step * i)
              }
              return ticks
            }
          }
        }
      })
      const result = defaultsDeep(options, axisOption)
      if (result.scale?.y) {
        result.scale.y.nice = false
      }
      return result
    }
    return defaultsDeep(options, axisOption)
  }

  protected configLegend(chart: Chart, options: G2Spec): G2Spec {
    const { legend } = parseJson(chart.customStyle)
    if (!legend.show) {
      return { ...options, legend: false }
    }
    const baseLegend = this.getLegend(chart, 2)
    return defaultsDeep(options, {
      legend: {
        color: {
          // 与柱状图、折线图复用统一的图例标记和分页器尺寸
          ...baseLegend
        }
      }
    })
  }

  protected setupOptions(chart: Chart, options: G2Spec, context: Record<string, any>): G2Spec {
    return flow(
      this.configTheme,
      this.configColor,
      this.configBasicStyle,
      this.configLabel,
      this.configTooltip,
      this.configXAxis,
      this.configYAxis,
      this.configLegend
    )(chart, options, context, this)
  }

  public setupSeriesColor(chart: ChartObj, data?: any[]): ChartBasicStyle['seriesColor'] {
    const result: ChartBasicStyle['seriesColor'] = []
    const seriesSet = new Set<string>()
    const colors = chart.customAttr.basicStyle.colors
    data?.forEach(item => {
      const colorValue = item.category ?? item.genre ?? item.field ?? item.color
      if (colorValue === null || colorValue === undefined || seriesSet.has(toString(colorValue))) {
        return
      }
      const id = toString(colorValue)
      seriesSet.add(id)
      result.push({
        id,
        name: id,
        color: colors[(seriesSet.size - 1) % colors.length]
      })
    })
    return result
  }

  private getAxisStyle(chart: Chart, axisStyle) {
    let lineLineDash = undefined
    if (axisStyle.axisLine.lineStyle.style === 'dashed') {
      lineLineDash = [10, 8]
    }
    if (axisStyle.axisLine.lineStyle.style === 'dotted') {
      lineLineDash = [1, 2]
    }
    let gridLineDash = [0, 0]
    if (axisStyle.splitLine.lineStyle.style === 'dashed') {
      gridLineDash = [10, 8]
    }
    if (axisStyle.splitLine.lineStyle.style === 'dotted') {
      gridLineDash = [1, 2]
    }
    return {
      position: axisStyle.position,
      title: axisStyle.nameShow === false ? false : axisStyle.name,
      titleFontSize: axisStyle.fontSize,
      titleFill: axisStyle.color,
      ...this.getAxisLineStyle(chart, axisStyle),
      lineLineDash,
      label: axisStyle.axisLabel.show,
      labelFill: axisStyle.axisLabel.color,
      labelFillOpacity: 1,
      labelFontSize: axisStyle.axisLabel.fontSize,
      grid: axisStyle.splitLine.show,
      gridStroke: axisStyle.splitLine.lineStyle.color,
      gridStrokeOpacity: 1,
      gridLineWidth: axisStyle.splitLine.lineStyle.width,
      gridLineDash,
      ...this.getAxisLabelStyle(axisStyle)
    }
  }

  private buildScatterDataContext(chart: Chart, sourceData: Record<string, unknown>[]) {
    const hasTimeX = sourceData.some(row => row.xLabel != null)
    const basicStyle = parseJson(chart.customAttr)?.basicStyle
    const palette = (basicStyle?.colors || []).map(color => hexColorToRGBA(color, basicStyle.alpha))
    const seriesColorMap = (basicStyle?.seriesColor || []).reduce((pre, next) => {
      pre[next.id] = hexColorToRGBA(next.color, basicStyle.alpha)
      return pre
    }, {})
    const colorDomain: string[] = []
    const getMarkerColor = (color: string) => {
      if (isCssColor(color)) {
        return color
      }
      if (seriesColorMap[color]) {
        return seriesColorMap[color]
      }
      let index = colorDomain.indexOf(color)
      if (index === -1) {
        colorDomain.push(color)
        index = colorDomain.length - 1
      }
      return palette[index % palette.length] || '#999999'
    }
    const multiData = sourceData
      .map(row => {
        let x: number | string | undefined
        if (hasTimeX) {
          x = row.xLabel != null ? String(row.xLabel) : undefined
        } else {
          x = safeToNumber(row.x ?? row.xAxis ?? row.value)
        }
        const y = safeToNumber(row.y ?? row.yAxis ?? row.value)
        if (x === undefined || !Number.isFinite(y)) {
          return undefined
        }
        const color = String(row.category ?? row.genre ?? row.field ?? row.color ?? 'default')
        const popSize = safeToNumber(row.popSize ?? row.extBubble ?? row.size)
        const lightness = safeToNumber(row.lightness ?? row.extColor)
        const markerColor = getMarkerColor(color)
        return {
          ...row,
          x,
          y,
          color,
          __markerColor: markerColor,
          popSize,
          lightness
        }
      })
      .filter(item => !!item)
    return {
      data: multiData,
      xField: 'x',
      yField: 'y',
      colorField: 'color',
      isTimeX: hasTimeX
    }
  }

  constructor() {
    super('multi-scatter', [])
  }
}
