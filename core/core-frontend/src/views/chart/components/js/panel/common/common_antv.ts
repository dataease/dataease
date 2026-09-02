import { hexColorToRGBA, hexToRgba, measureText, parseJson } from '../../util'
import {
  DEFAULT_BASIC_STYLE,
  DEFAULT_LEGEND_STYLE,
  DEFAULT_XAXIS_STYLE,
  DEFAULT_YAXIS_EXT_STYLE,
  DEFAULT_YAXIS_STYLE
} from '@/views/chart/components/editor/util/chart'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { AreaOptions, LabelOptions } from '@antv/l7plot'
import { TooltipOptions } from '@antv/l7plot/dist/lib/types/tooltip'
import { FeatureCollection } from '@antv/l7plot/dist/esm/plots/choropleth/types'
import { add } from 'mathjs'
import isEmpty from 'lodash-es/isEmpty'
import type { LegendOptions } from '@antv/l7plot/dist/esm/types/legend'
import { CategoryLegendListItem } from '@antv/l7plot-component/dist/lib/types/legend'
import createDom from '@antv/dom-util/esm/create-dom'
import {
  CONTAINER_TPL,
  ITEM_TPL,
  LIST_CLASS
} from '@antv/l7plot-component/dist/esm/legend/category/constants'
import type { Plot as L7Plot, PlotOptions } from '@antv/l7plot/dist/esm'
import { Zoom } from '@antv/l7'
import { DOM } from '@antv/l7-utils'
import { Scene } from '@antv/l7-scene'
import { type IZoomControlOption } from '@antv/l7-component'
import { InteractionEvent, PositionType } from '@antv/l7-core'
import { centroid } from '@turf/centroid'
import { assign, defaults, filter, find, groupBy, map, uniq } from 'lodash-es'
import { useI18n } from '@/hooks/web/useI18n'
import { isMobile } from '@/utils/utils'
import { ChartEvent } from '@antv/g2'
import type { Chart as G2Chart } from '@antv/g2'
import { Text } from '@antv/g'
import { GaodeMap, TMap, TencentMap } from '@antv/l7-maps'
import {
  gaodeMapStyleOptions,
  qqMapStyleOptions,
  tdtMapStyleOptions
} from '@/views/chart/components/js/panel/charts/map/common'
import {
  getCustomOnlineMapScene,
  getCustomOnlineMapStyle,
  isCustomOnlineMapScene
} from '@/views/chart/components/js/panel/charts/map/custom-online-map'
import { CUSTOM_TILE_MAP_TYPE, type OnlineMapConfig } from '@/utils/onlineMap'
import G2TooltipCarousel from '@/views/chart/components/js/G2TooltipCarousel'
import { Renderer as SVGRenderer } from '@antv/g-svg'
import { Renderer as CanvasRenderer } from '@antv/g-canvas'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'

const { t: tI18n } = useI18n()

/**
 * 获取 G2 图表渲染器配置
 * 根据 dvMainStore 中的 enableSvgRenderer 配置决定是否使用 SVG 渲染器
 * @returns 渲染器配置对象，如果启用 SVG 则返回 { renderer: new SVGRenderer() }，否则返回空对象使用默认 Canvas 渲染
 */
export function getG2Renderer() {
  const dvMainStore = dvMainStoreWithOut()
  const enableSvgRenderer = dvMainStore?.canvasStyleData?.enableSvgRenderer
  return enableSvgRenderer ? { renderer: new SVGRenderer() } : {}
}

const G2_TOOLTIP_CAROUSEL_CHART_TYPES = {
  COLUMN: ['bar', 'bar-stack', 'bar-group', 'bar-group-stack', 'percentage-bar-stack'],
  LINE: ['line', 'area', 'area-stack'],
  MIX: ['chart-mix', 'chart-mix-group', 'chart-mix-stack', 'chart-mix-dual-line'],
  PIE: ['pie', 'pie-donut', 'pie-rose', 'pie-donut-rose']
}

const isColumn = (chartType: string) => G2_TOOLTIP_CAROUSEL_CHART_TYPES.COLUMN.includes(chartType)
const isPie = (chartType: string) => G2_TOOLTIP_CAROUSEL_CHART_TYPES.PIE.includes(chartType)
const isMix = (chartType: string) => G2_TOOLTIP_CAROUSEL_CHART_TYPES.MIX.includes(chartType)
const isSupport = (chartType: string) =>
  Object.values(G2_TOOLTIP_CAROUSEL_CHART_TYPES).some(category => category.includes(chartType))
const DARK_TOOLTIP_CROSSHAIRS_STYLE = {
  crosshairsStroke: '#FFFFFF',
  crosshairsStrokeOpacity: 0.45
}
const LIGHT_TOOLTIP_CROSSHAIRS_STYLE = {
  crosshairsStroke: '#000000',
  crosshairsStrokeOpacity: 0.45
}

export function getTooltipCrosshairsStyle(chart: Chart) {
  const chartContext = chart as Chart & { isDataV?: boolean; themes?: string }
  // 数据大屏默认深色背景，折线类 tooltip 定位线需要提高对比度
  return chartContext?.isDataV || chartContext?.themes === 'dark'
    ? { ...DARK_TOOLTIP_CROSSHAIRS_STYLE }
    : { ...LIGHT_TOOLTIP_CROSSHAIRS_STYLE }
}

export function getPadding(chart: Chart): number[] {
  if (chart.drill) {
    return [0, 10, 22, 10]
  } else {
    return [0, 10, 10, 10]
  }
}
// color,label,tooltip,axis,legend,background
export function getTheme(chart: Chart) {
  const colors = []
  let bgColor,
    labelFontsize,
    labelColor,
    tooltipColor,
    tooltipFontsize,
    tooltipBackgroundColor,
    legendColor,
    legendFontsize
  let customAttr: DeepPartial<ChartAttr>
  if (chart.customAttr) {
    customAttr = parseJson(chart.customAttr)
    // color
    if (customAttr.basicStyle) {
      const b = JSON.parse(JSON.stringify(customAttr.basicStyle))
      b.colors.forEach(ele => {
        colors.push(hexColorToRGBA(ele, b.alpha))
      })
    }
    // label
    if (customAttr.label) {
      const l = JSON.parse(JSON.stringify(customAttr.label))
      labelFontsize = l.fontSize
      labelColor = l.color
    }
    // tooltip
    if (customAttr.tooltip) {
      const t = JSON.parse(JSON.stringify(customAttr.tooltip))
      tooltipColor = t.color
      tooltipFontsize = t.fontSize
      tooltipBackgroundColor = t.backgroundColor
    }
  }

  let customStyle: DeepPartial<ChartStyle>
  if (chart.customStyle) {
    customStyle = parseJson(chart.customStyle)
    // bg
    if (customStyle.background) {
      bgColor = hexColorToRGBA(customStyle.background.color, customStyle.background.alpha)
    }
    // legend
    if (customStyle.legend) {
      const l = customStyle.legend
      legendColor = l.color
      legendFontsize = l.fontSize
    }
  }

  const theme = {
    styleSheet: {
      brandColor: colors[0],
      paletteQualitative10: colors,
      paletteQualitative20: colors,
      backgroundColor: bgColor
    },
    labels: {
      offset: 4,
      style: {
        fill: labelColor,
        fontSize: labelFontsize
      }
    },
    innerLabels: {
      offset: 4,
      style: {
        fill: labelColor,
        fontSize: labelFontsize
      }
    },
    pieLabels: {
      offset: 4,
      style: {
        fill: labelColor,
        fontSize: labelFontsize
      }
    },
    components: {
      tooltip: {
        domStyles: {
          'g2-tooltip': {
            color: tooltipColor,
            fontSize: tooltipFontsize + 'px',
            background: tooltipBackgroundColor,
            boxShadow: '0 4px 8px 0 rgba(0, 0, 0, 0.1)',
            'z-index': 2000,
            position: 'fixed'
          },
          'g2-tooltip-list-item': {
            display: 'flex',
            'align-items': 'flex-start',
            'justify-content': 'space-between',
            'line-height': tooltipFontsize + 'px'
          },
          'g2-tooltip-name': {
            display: 'inline-block',
            'line-height': tooltipFontsize + 'px'
          },
          'g2-tooltip-value': {
            flex: 1,
            display: 'inline-block',
            'text-align': 'end',
            'line-height': tooltipFontsize + 'px'
          },
          'g2-tooltip-marker': {
            'margin-top': (tooltipFontsize - 8) / 2 + 'px',
            'min-width': '8px',
            'min-height': '8px'
          }
        }
      },
      legend: {
        common: {
          itemName: {
            style: {
              fill: legendColor,
              fontSize: legendFontsize
            }
          }
        }
      }
    }
  }
  if (chart.fontFamily) {
    theme.styleSheet.fontFamily = chart.fontFamily
  }
  return theme
}
// 通用label
export function getLabel(chart: Chart) {
  let label
  let customAttr: DeepPartial<ChartAttr>
  if (chart.customAttr) {
    customAttr = parseJson(chart.customAttr)
    // label
    if (customAttr.label) {
      const l = customAttr.label
      if (l.show) {
        const layout = []
        if (!l.fullDisplay) {
          if (chart.type === 'bar-stack') {
            layout.push({ type: 'interval-hide-overlap' })
          } else if (
            chart.type.indexOf('-horizontal') > -1 ||
            [
              'bidirectional-bar',
              'progress-bar',
              'pie-donut',
              'radar',
              'waterfall',
              't-heatmap',
              'bar'
            ].includes(chart.type)
          ) {
            layout.push({ type: 'limit-in-canvas' })
            layout.push({ type: 'hide-overlap' })
          } else if (chart.type.includes('chart-mix')) {
            layout.push({ type: 'limit-in-canvas' })
            layout.push({ type: 'limit-in-plot' })
            layout.push({ type: 'hide-overlap' })
          } else {
            layout.push({ type: 'limit-in-plot' })
            layout.push({ type: 'hide-overlap' })
          }
        }
        label = {
          position: l.position,
          layout,
          style: {
            fill: l.color,
            fontSize: l.fontSize,
            fontFamily: chart.fontFamily
          },
          formatter: function (param: Datum) {
            return valueFormatter(param.value, l.labelFormatter)
          }
        }
      } else {
        label = false
      }
    }
  }
  return label
}
// 通用tooltip
export function getTooltip(chart: Chart) {
  let tooltip
  let customAttr: DeepPartial<ChartAttr>
  if (chart.customAttr) {
    customAttr = parseJson(chart.customAttr)
    // tooltip
    if (customAttr.tooltip) {
      const t = JSON.parse(JSON.stringify(customAttr.tooltip))
      if (t.show) {
        tooltip = {
          formatter: function (param: Datum) {
            const value = valueFormatter(param.value, t.tooltipFormatter)
            return { name: param.field, value }
          },
          container: getTooltipContainer(`tooltip-${chart.id}`),
          itemTpl: TOOLTIP_TPL,
          enterable: true
        }
      } else {
        tooltip = false
      }
    }
  }
  return tooltip
}

export function getMultiSeriesTooltip(chart: Chart) {
  const customAttr: DeepPartial<ChartAttr> = parseJson(chart.customAttr)
  const tooltipAttr = customAttr.tooltip
  if (!tooltipAttr.show) {
    return false
  }
  const formatterMap = tooltipAttr.seriesTooltipFormatter
    ?.filter(i => i.show)
    .reduce((pre, next) => {
      pre[next.id] = next
      return pre
    }, {}) as Record<string, SeriesFormatter>
  const tooltip: Tooltip = {
    showTitle: true,
    customItems(originalItems) {
      if (!tooltipAttr.seriesTooltipFormatter?.length) {
        return originalItems
      }
      const head = originalItems[0]
      // 非原始数据
      if (!head.data.quotaList) {
        return originalItems
      }
      const result = []
      originalItems
        .filter(item => formatterMap[item.data.quotaList[0].id])
        .forEach(item => {
          const formatter = formatterMap[item.data.quotaList[0].id]
          const value = valueFormatter(parseFloat(item.value as string), formatter.formatterCfg)
          const name = isEmpty(formatter.chartShowName) ? formatter.name : formatter.chartShowName
          const color = getTooltipItemConditionColor(item)
          result.push({ ...item, name, value, ...(color ? { color } : {}) })
        })
      head.data.dynamicTooltipValue?.forEach(item => {
        const formatter = formatterMap[item.fieldId]
        if (formatter) {
          const value = valueFormatter(parseFloat(item.value), formatter.formatterCfg)
          const name = isEmpty(formatter.chartShowName) ? formatter.name : formatter.chartShowName
          result.push({ color: 'grey', name, value })
        }
      })
      return result
    },
    container: getTooltipContainer(`tooltip-${chart.id}`),
    itemTpl: TOOLTIP_TPL,
    enterable: true
  }
  return tooltip
}
// 通用legend
export function getLegend(chart: Chart) {
  let legend = {}
  let customStyle: CustomStyle
  if (chart.customStyle) {
    customStyle = parseJson(chart.customStyle)
    // legend
    if (customStyle.legend) {
      const l = defaults(JSON.parse(JSON.stringify(customStyle.legend)), DEFAULT_LEGEND_STYLE)
      if (l.show) {
        let offsetX, offsetY, position
        const orient = l.orient
        const legendSymbol = l.icon
        // fix position
        if (l.hPosition === 'center') {
          position = l.vPosition === 'center' ? 'top' : l.vPosition
        } else if (l.vPosition === 'center') {
          position = l.hPosition === 'center' ? 'left' : l.hPosition
        } else {
          if (orient === 'horizontal') {
            position = l.vPosition + '-' + l.hPosition
          } else {
            position = l.hPosition + '-' + l.vPosition
          }
        }
        // fix offset
        if (orient === 'horizontal') {
          if (l.hPosition === 'left') {
            offsetX = 16
          } else if (l.hPosition === 'right') {
            offsetX = -16
          } else {
            offsetX = 0
          }
          if (l.vPosition === 'top') {
            offsetY = 0
          } else if (l.vPosition === 'bottom') {
            if (chart.drill) {
              offsetY = -12
            }
          } else {
            offsetY = 0
          }
        } else {
          if (l.hPosition === 'left') {
            offsetX = 10
          } else if (l.hPosition === 'right') {
            offsetX = -10
          } else {
            offsetX = 0
          }
          if (l.vPosition === 'top') {
            offsetY = 0
          } else if (l.vPosition === 'bottom') {
            if (chart.drill) {
              offsetY = -18
            } else {
              offsetY = -10
            }
          } else {
            offsetY = 0
          }
        }

        legend = {
          layout: orient,
          position: position,
          offsetX: offsetX,
          offsetY: offsetY,
          marker: {
            symbol: legendSymbol,
            style: {
              r: l.size
            }
          },
          itemName: {
            style: {
              fill: l.color,
              fontSize: l.fontSize
            }
          },
          itemHeight: (l.fontSize > l.size * 2 ? l.fontSize : l.size * 2) + 4,
          radio: false,
          pageNavigator: {
            marker: {
              style: {
                fill: 'rgba(0,0,0,0.65)',
                stroke: 'rgba(192,192,192,0.52)',
                size: l.size * 2
              }
            },
            text: {
              style: {
                fill: l.color,
                fontSize: l.fontSize
              }
            }
          }
        }
      } else {
        legend = false
      }
    }
  }
  return legend
}
// xAxis
export function getXAxis(chart: Chart) {
  let axis: Record<string, any> | boolean = {}
  let customStyle: CustomStyle
  if (chart.customStyle) {
    customStyle = parseJson(chart.customStyle)
    // legend
    if (customStyle.xAxis) {
      const a = JSON.parse(JSON.stringify(customStyle.xAxis))
      if (a.show) {
        const title =
          a.nameShow && a.name && a.name !== ''
            ? {
                text: a.name,
                style: {
                  fill: a.color,
                  fontSize: a.fontSize
                },
                spacing: 8
              }
            : null
        const grid = a.splitLine.show
          ? {
              line: {
                style: {
                  stroke: a.splitLine.lineStyle.color,
                  lineWidth: a.splitLine.lineStyle.width,
                  lineDash: getLineDash(a.splitLine.lineStyle.style)
                }
              }
            }
          : null
        const axisCfg = a.axisLine ? a.axisLine : DEFAULT_XAXIS_STYLE.axisLine
        const line = axisCfg.show
          ? {
              style: {
                stroke: axisCfg.lineStyle.color,
                lineWidth: axisCfg.lineStyle.width,
                lineDash: getLineDash(axisCfg.lineStyle.style)
              }
            }
          : null
        const tickLine = axisCfg.show
          ? {
              style: {
                stroke: axisCfg.lineStyle.color,
                lineWidth: axisCfg.lineStyle.width
              }
            }
          : null
        let textAlign = 'center'
        const rotate = a.axisLabel.rotate
        if (a.position === 'top') {
          textAlign = rotate > 20 ? 'end' : rotate < -20 ? 'start' : 'center'
        }
        if (a.position === 'bottom') {
          textAlign = rotate > 20 ? 'start' : rotate < -20 ? 'end' : 'center'
        }
        const label = a.axisLabel.show
          ? {
              rotate: (rotate * Math.PI) / 180,
              style: {
                fill: a.axisLabel.color,
                fontSize: a.axisLabel.fontSize,
                textAlign: textAlign,
                fontFamily: chart.fontFamily
              },
              formatter: value => {
                return chart.type === 'bidirectional-bar' && value.length > a.axisLabel.lengthLimit
                  ? value.substring(0, a.axisLabel.lengthLimit) + '...'
                  : value
              }
            }
          : null

        axis = {
          position: a.position,
          title,
          grid,
          label,
          line,
          tickLine
        }
      } else {
        axis = false
      }
    }
  }
  return axis
}
// yAxis
export function getYAxis(chart: Chart) {
  let axis: Record<string, any> | boolean = {}
  const yAxis = parseJson(chart.customStyle).yAxis
  if (!yAxis.show) {
    return false
  }
  const title =
    yAxis.nameShow && yAxis.name && yAxis.name !== ''
      ? {
          text: yAxis.name,
          style: {
            fill: yAxis.color,
            fontSize: yAxis.fontSize
          },
          spacing: 8
        }
      : null
  const grid = yAxis.splitLine.show
    ? {
        line: {
          style: {
            stroke: yAxis.splitLine.lineStyle.color,
            lineWidth: yAxis.splitLine.lineStyle.width,
            lineDash: getLineDash(yAxis.splitLine.lineStyle.style)
          }
        }
      }
    : null
  const axisCfg = yAxis.axisLine ? yAxis.axisLine : DEFAULT_YAXIS_STYLE.axisLine
  const line = axisCfg.show
    ? {
        style: {
          stroke: axisCfg.lineStyle.color,
          lineWidth: axisCfg.lineStyle.width,
          lineDash: getLineDash(axisCfg.lineStyle.style)
        }
      }
    : null
  const tickLine = axisCfg.show
    ? {
        style: {
          stroke: axisCfg.lineStyle.color,
          lineWidth: axisCfg.lineStyle.width
        }
      }
    : null
  const rotate = yAxis.axisLabel.rotate
  let textAlign = 'end'
  let textBaseline = 'middle'
  if (yAxis.position === 'right') {
    textAlign = 'start'
    if (Math.abs(rotate) > 75) {
      textAlign = 'center'
    }
    if (rotate > 75) {
      textBaseline = 'bottom'
    }
    if (rotate < -75) {
      textBaseline = 'top'
    }
  }
  if (yAxis.position === 'left') {
    if (Math.abs(rotate) > 75) {
      textAlign = 'center'
    }
    if (rotate > 75) {
      textBaseline = 'top'
    }
    if (rotate < -75) {
      textBaseline = 'bottom'
    }
  }
  const label = yAxis.axisLabel.show
    ? {
        rotate: (rotate * Math.PI) / 180,
        style: {
          fill: yAxis.axisLabel.color,
          fontSize: yAxis.axisLabel.fontSize,
          textBaseline,
          textAlign,
          fontFamily: chart.fontFamily
        },
        formatter: value => {
          return value.length > yAxis.axisLabel.lengthLimit
            ? value.substring(0, yAxis.axisLabel.lengthLimit) + '...'
            : value
        }
      }
    : null

  axis = {
    position: yAxis.position,
    title,
    grid,
    label,
    line,
    tickLine,
    nice: true
  }
  return axis
}

export function getYAxisExt(chart: Chart) {
  let axis: Record<string, any> | boolean = {}
  const yAxis = parseJson(chart.customStyle).yAxisExt
  if (!yAxis.show) {
    return false
  }
  const title =
    yAxis.nameShow && yAxis.name && yAxis.name !== ''
      ? {
          text: yAxis.name,
          style: {
            fill: yAxis.color,
            fontSize: yAxis.fontSize
          },
          spacing: 8
        }
      : null
  const grid = yAxis.splitLine.show
    ? {
        line: {
          style: {
            stroke: yAxis.splitLine.lineStyle.color,
            lineWidth: yAxis.splitLine.lineStyle.width,
            lineDash: getLineDash(yAxis.splitLine.lineStyle.style)
          }
        }
      }
    : null
  const axisCfg = yAxis.axisLine ? yAxis.axisLine : DEFAULT_YAXIS_STYLE.axisLine
  const line = axisCfg.show
    ? {
        style: {
          stroke: axisCfg.lineStyle.color,
          lineWidth: axisCfg.lineStyle.width,
          lineDash: getLineDash(axisCfg.lineStyle.style)
        }
      }
    : null
  const tickLine = axisCfg.show
    ? {
        style: {
          stroke: axisCfg.lineStyle.color,
          lineWidth: axisCfg.lineStyle.width
        }
      }
    : null
  const rotate = yAxis.axisLabel.rotate
  let textAlign = 'end'
  let textBaseline = 'middle'
  if (yAxis.position === 'right') {
    textAlign = 'start'
    if (Math.abs(rotate) > 75) {
      textAlign = 'center'
    }
    if (rotate > 75) {
      textBaseline = 'bottom'
    }
    if (rotate < -75) {
      textBaseline = 'top'
    }
  }
  if (yAxis.position === 'left') {
    if (Math.abs(rotate) > 75) {
      textAlign = 'center'
    }
    if (rotate > 75) {
      textBaseline = 'top'
    }
    if (rotate < -75) {
      textBaseline = 'bottom'
    }
  }
  const label = yAxis.axisLabel.show
    ? {
        rotate: (rotate * Math.PI) / 180,
        style: {
          fill: yAxis.axisLabel.color,
          fontSize: yAxis.axisLabel.fontSize,
          textBaseline,
          textAlign,
          fontFamily: chart.fontFamily
        }
      }
    : null

  axis = {
    position: yAxis.position,
    title,
    grid,
    label,
    line,
    tickLine,
    nice: true
  }
  return axis
}

export function getSlider(chart: Chart) {
  let cfg
  const senior = parseJson(chart.senior)
  if (senior.functionCfg) {
    if (senior.functionCfg.sliderShow) {
      cfg = {
        start: senior.functionCfg.sliderRange[0] / 100,
        end: senior.functionCfg.sliderRange[1] / 100
      }

      if (senior.functionCfg.sliderBg) {
        cfg.backgroundStyle = {
          fill: senior.functionCfg.sliderBg,
          stroke: senior.functionCfg.sliderBg,
          lineWidth: 1,
          strokeOpacity: 0.5
        }
      }
      if (senior.functionCfg.sliderFillBg) {
        cfg.foregroundStyle = {
          fill: senior.functionCfg.sliderFillBg,
          fillOpacity: 0.5
        }
      }
      if (senior.functionCfg.sliderTextColor) {
        cfg.textStyle = {
          fill: senior.functionCfg.sliderTextColor,
          fontFamily: chart.fontFamily
        }
        cfg.handlerStyle = {
          fill: senior.functionCfg.sliderTextColor,
          fillOpacity: 0.5,
          highLightFill: senior.functionCfg.sliderTextColor
        }
      }
    }
  }
  return cfg
}

// 缩略轴范围统一使用 0 到 1 的归一化值
type SliderValues = [number, number]
// 不同 G2 mark 结构差异较大，这里只声明缩略轴需要读写的字段
type DimensionSliderMark = {
  encode?: Record<string, any>
  scale?: Record<string, any>
  slider?: Record<string, any>
  interaction?: Record<string, any>
  animate?: Record<string, any>
  [key: string]: any
}
// 调用方按图表结构声明同步策略，兼容柱图、折线图和面积图
type DimensionSliderOptions = {
  interactionName?: string
  dimensionField?: string
  valueScale?: {
    field: string
    includeZero?: boolean
  }
  valueScaleDomain?: [number, number]
  stableKey?: boolean
  disableMorph?: boolean
  syncChildren?: boolean
  syncMarks?: DimensionSliderMark[]
  sliderMarkIndex?: number
  onSelectedDomainChange?: (domain: any[]) => void
}

// 补齐缩略轴透明度，避免暗色主题或重绘后控件显示发虚
const DIMENSION_SLIDER_STYLE = {
  trackOpacity: 1,
  selectionFillOpacity: 1,
  handleLabelFillOpacity: 1,
  handleIconFillOpacity: 1
}

// 字体颜色同步滑块手柄，缩略轴轨道背景另行保持透明
const getDimensionSliderTextColorStyle = (sliderTextColor?: string) =>
  sliderTextColor
    ? {
        handleLabelFill: sliderTextColor,
        handleIconFill: sliderTextColor
      }
    : {}

// 背景色作为底部缩略轴轨道描边，轨道填充保持透明
const getDimensionSliderTrackStyle = (sliderBg?: string) => ({
  trackFill: 'transparent',
  ...(sliderBg && {
    trackStroke: sliderBg,
    trackStrokeOpacity: 1,
    trackLineWidth: 1
  })
})

// 将面板百分比范围规整到 G2 slider 可识别的 0 到 1 区间
const normalizeSliderValues = (values?: number[]): SliderValues => {
  const [start = 0, end = 1] = values || []
  return [Math.max(0, Math.min(start, end)), Math.min(1, Math.max(start, end))]
}

const getSliderDomainKey = (value: unknown): string =>
  `${value instanceof Date ? value.getTime() : value}`

// 从数组数据或 G2 data.value 中提取离散维度域，并保留原始值顺序
const getSliderDimensionDomain = (data: any, field: string): any[] => {
  const sourceData = Array.isArray(data) ? data : Array.isArray(data?.value) ? data.value : []
  return Array.from(
    new Map(
      sourceData.map(item => {
        const value = item?.[field]
        return [getSliderDomainKey(value), value]
      })
    ).values()
  )
}

// 按维度预聚合数值范围，缩略轴拖动时不再重复扫描全量数据
const createSliderValueDomainGetter = (
  data: any,
  dimensionField: string,
  valueScale?: DimensionSliderOptions['valueScale']
) => {
  if (!valueScale?.field) {
    return undefined
  }
  const sourceData = Array.isArray(data) ? data : Array.isArray(data?.value) ? data.value : []
  const valueRanges = new Map<string, [number, number]>()
  sourceData.forEach(item => {
    const rawValue = item?.[valueScale.field]
    const values = Array.isArray(rawValue) ? rawValue : [rawValue]
    values.forEach(value => {
      if (value === null || value === undefined || value === '') {
        return
      }
      const numberValue = Number(value)
      if (!Number.isFinite(numberValue)) {
        return
      }
      const key = getSliderDomainKey(item?.[dimensionField])
      const range = valueRanges.get(key)
      if (range) {
        range[0] = Math.min(range[0], numberValue)
        range[1] = Math.max(range[1], numberValue)
      } else {
        valueRanges.set(key, [numberValue, numberValue])
      }
    })
  })
  return (selectedDomain: any[]): [number, number] | undefined => {
    let min = Number.POSITIVE_INFINITY
    let max = Number.NEGATIVE_INFINITY
    selectedDomain.forEach(value => {
      const range = valueRanges.get(getSliderDomainKey(value))
      if (!range) {
        return
      }
      min = Math.min(min, range[0])
      max = Math.max(max, range[1])
    })
    if (!Number.isFinite(min) || !Number.isFinite(max)) {
      return undefined
    }
    if (valueScale.includeZero) {
      min = Math.min(0, min)
      max = Math.max(0, max)
    }
    return [min, max]
  }
}

// 将滑块百分比范围换算为实际需要保留的维度值
const getSelectedSliderDomain = (domain: any[], values: SliderValues): any[] => {
  if (!domain.length) {
    return []
  }
  const lastIndex = domain.length - 1
  const startIndex = Math.max(0, Math.min(lastIndex, Math.floor(values[0] * lastIndex)))
  const endIndex = Math.max(startIndex, Math.min(lastIndex, Math.ceil(values[1] * lastIndex)))
  return domain.slice(startIndex, endIndex + 1)
}

// handle 标签展示过滤后维度域的首尾值，避免显示连续比例值
const getSliderLabelFormatter = (domain: any[], values: SliderValues) => (value: number) => {
  const label =
    Math.abs(Number(value) - values[0]) <= Math.abs(Number(value) - values[1])
      ? domain[0]
      : domain[domain.length - 1]
  return label === null || label === undefined ? '' : `${label}`
}

// 柱形图缩略轴过滤时保持元素 key 稳定，避免不同维度之间复用动画
const getSliderKey = (field: string) => (data: any) =>
  [
    data?.[field],
    data?.category,
    data?.group,
    data?.quotaList?.map((item: any) => item.id).join(',')
  ]
    .filter(item => item !== null && item !== undefined && item !== '')
    .join('-')

// 只更新 mark 的 x 域，供 line、area、point 等多个 mark 同步过滤
const patchDimensionDomainMark = (
  mark: DimensionSliderMark,
  domain: any[],
  options: DimensionSliderOptions = {}
) => ({
  ...mark,
  ...(options.stableKey && {
    encode: { ...mark.encode, key: getSliderKey(options.dimensionField || mark.encode?.x) }
  }),
  ...(options.disableMorph && {
    // 缩略轴按维度切换域，禁用默认 morph，避免柱形从旧维度纵向过渡
    animate: {
      ...mark.animate,
      update: { ...mark.animate?.update, type: null },
      exit: { ...mark.animate?.exit, type: null }
    }
  }),
  scale: {
    ...mark.scale,
    x: { ...mark.scale?.x, domain, nice: false },
    ...(options.valueScaleDomain && {
      y: { ...mark.scale?.y, domain: options.valueScaleDomain, nice: true }
    })
  }
})

// 更新承载 slider 的 mark，同时继承离散维度域和样式补丁
const patchDimensionSliderMark = (
  mark: DimensionSliderMark,
  domain: any[],
  values: SliderValues,
  options: DimensionSliderOptions = {}
) => {
  const formatter = getSliderLabelFormatter(domain, values)
  return {
    ...patchDimensionDomainMark(mark, domain, options),
    slider: {
      ...mark.slider,
      x: {
        ...mark.slider?.x,
        values,
        formatter,
        style: { ...mark.slider?.x?.style, ...DIMENSION_SLIDER_STYLE, formatter }
      }
    }
  }
}

// 自定义 slider 交互按离散维度域更新 mark，替代 G2 默认连续比例过滤
const dimensionSliderFilter = ({
  data,
  field,
  stableKey,
  disableMorph,
  syncChildren,
  sliderMarkIndex = 0,
  onSelectedDomainChange,
  getValueScaleDomain
}: any) => {
  const domain = getSliderDimensionDomain(data, field)
  return (target: any) => {
    const slider = Array.from(target.container.getElementsByClassName?.('slider') || []).find(
      (item: any) => item.attributes?.orientation === 'horizontal'
    ) as any
    if (!slider || !domain.length) {
      return
    }
    let pendingValues: number[] | undefined
    let frameId: number | undefined
    let updateRunning = false
    let applyAfterUpdate = false
    let destroyed = false
    const scheduleRender = () => {
      if (destroyed || updateRunning || frameId !== undefined) {
        return
      }
      frameId = requestAnimationFrame(() => void render())
    }
    const render = async () => {
      frameId = undefined
      if (destroyed || updateRunning || !pendingValues) {
        return
      }
      // valuechange 只记录最新范围，实际更新在同一帧内合并执行
      const values = normalizeSliderValues(pendingValues)
      pendingValues = undefined
      const selectedDomain = getSelectedSliderDomain(domain, values)
      onSelectedDomainChange?.(selectedDomain)
      const formatter = getSliderLabelFormatter(selectedDomain, values)
      const options = {
        dimensionField: field,
        stableKey,
        disableMorph,
        valueScaleDomain: getValueScaleDomain?.(selectedDomain)
      }
      const patchMarks = (marks?: DimensionSliderMark[]) =>
        marks?.map((mark, index) => {
          if (index === sliderMarkIndex) {
            return patchDimensionSliderMark(mark, selectedDomain, values, options)
          }
          return syncChildren ? patchDimensionDomainMark(mark, selectedDomain, options) : mark
        })
      Object.assign(slider.attributes || {}, { formatter }, DIMENSION_SLIDER_STYLE)
      if (typeof slider?.setValues === 'function') {
        slider.setValues(values)
      }
      target.setState(slider, (state: any) => ({
        ...state,
        marks: patchMarks(state.marks),
        children: patchMarks(state.children)
      }))
      updateRunning = true
      try {
        // G2 update 未完成时不启动新一轮完整更新，期间始终保留最新范围
        await target.update()
      } catch (error) {
        console.warn(error)
      } finally {
        updateRunning = false
        if (destroyed) {
          return
        }
        if (pendingValues) {
          if (applyAfterUpdate) {
            applyAfterUpdate = false
            void render()
          } else {
            scheduleRender()
          }
        } else {
          applyAfterUpdate = false
        }
      }
    }
    const onValueChange = (event: any) => {
      pendingValues = event.detail?.value || slider.attributes?.values
      // 拖动中按帧刷新，避免 pointermove 频繁触发 G2 重绘
      scheduleRender()
    }
    const apply = () => {
      if (frameId !== undefined) {
        cancelAnimationFrame(frameId)
        frameId = undefined
      }
      if (updateRunning) {
        applyAfterUpdate = true
        return
      }
      // pointerup 时立即落地最后一次范围，避免松手后缩略轴和图形不同步
      void render()
    }
    slider.addEventListener('valuechange', onValueChange)
    document.addEventListener('pointerup', apply)
    return () => {
      destroyed = true
      if (frameId !== undefined) {
        cancelAnimationFrame(frameId)
      }
      slider.removeEventListener('valuechange', onValueChange)
      document.removeEventListener('pointerup', apply)
    }
  }
}

// G2 将 touchstart 转为 pointerdown 时仍保留 TouchEvent，需在 Slider 读取坐标前转换为 Touch
export const installG2SliderTouchAdapter = (chart: G2Chart) => {
  const boundTargets = new WeakSet<object>()
  const bindSliders = () => {
    const { canvas } = chart.getContext()
    const sliders = Array.from(canvas?.document?.getElementsByClassName?.('slider') || []) as any[]
    sliders.forEach(slider => {
      const dragTargets = ['slider-handle', 'slider-selection', 'slider-brush-area'].flatMap(
        className => Array.from(slider.getElementsByClassName?.(className) || []) as any[]
      )
      dragTargets.forEach(target => {
        if (boundTargets.has(target)) {
          return
        }
        boundTargets.add(target)
        target.addEventListener(
          'pointerdown',
          event => {
            if (event.pointerType !== 'touch') {
              return
            }
            const touch = event.nativeEvent?.touches?.[0] || event.nativeEvent?.changedTouches?.[0]
            if (!touch) {
              return
            }
            event.nativeEvent = touch
          },
          { capture: true }
        )
      })
    })
  }
  chart.on(ChartEvent.AFTER_RENDER, bindSliders)
  bindSliders()
  return () => chart.off(ChartEvent.AFTER_RENDER, bindSliders)
}

// 分类维度缩略轴统一入口：按离散维度域过滤，避免 G2 默认比例过滤导致标签和数据错位
export const configDimensionSlider = (
  sliderMark: DimensionSliderMark,
  sourceData: any,
  functionCfg: any,
  options: DimensionSliderOptions = {}
) => {
  const dimensionField = options.dimensionField || sliderMark?.encode?.x
  if (!dimensionField) {
    return false
  }
  const values = normalizeSliderValues([
    (functionCfg.sliderRange?.[0] ?? 0) / 100,
    (functionCfg.sliderRange?.[1] ?? 100) / 100
  ])
  const domain = getSliderDimensionDomain(sourceData, dimensionField)
  if (!domain.length) {
    return false
  }

  const selectedDomain = getSelectedSliderDomain(domain, values)
  options.onSelectedDomainChange?.(selectedDomain)
  const getValueScaleDomain = createSliderValueDomainGetter(
    sourceData,
    dimensionField,
    options.valueScale
  )
  const dimensionOptions = {
    ...options,
    dimensionField,
    valueScaleDomain: getValueScaleDomain?.(selectedDomain)
  }
  // 初始渲染时先把主 mark 收敛到缩略轴默认范围
  Object.assign(
    sliderMark,
    patchDimensionSliderMark(sliderMark, selectedDomain, values, dimensionOptions)
  )
  // 折线图和面积图的其它 mark 共享同一个 x 域，防止线、点、面积错位
  options.syncMarks?.forEach(mark => {
    Object.assign(mark, patchDimensionDomainMark(mark, selectedDomain, dimensionOptions))
  })
  const sliderX = sliderMark.slider?.x || {}
  // 强制使用底部横向 slider，横向条形图也保持统一布局入口
  sliderMark.slider = {
    ...sliderMark.slider,
    x: {
      ...sliderX,
      type: 'sliderX',
      position: 'bottom',
      style: {
        ...sliderX.style,
        ...getDimensionSliderTrackStyle(functionCfg.sliderBg),
        selectionFill: functionCfg.sliderFillBg,
        ...getDimensionSliderTextColorStyle(functionCfg.sliderTextColor),
        handleLabelPointerEvents: 'none',
        sparklineLineStrokeOpacity: 0
      }
    }
  }
  // 关闭 G2 默认 sliderFilter，改用离散维度过滤以保证标签和数据一致
  sliderMark.interaction = {
    ...sliderMark.interaction,
    sliderFilter: false,
    [options.interactionName || 'dimensionSliderFilter']: {
      type: dimensionSliderFilter,
      field: dimensionField,
      data: sourceData,
      syncChildren: options.syncChildren,
      sliderMarkIndex: options.sliderMarkIndex,
      stableKey: options.stableKey,
      disableMorph: options.disableMorph,
      onSelectedDomainChange: options.onSelectedDomainChange,
      getValueScaleDomain
    }
  }
  return true
}

export function getAnalyse(chart: Chart) {
  const assistLine = []
  const senior = parseJson(chart.senior)
  if (!senior.assistLineCfg?.enable) {
    return assistLine
  }
  const assistLineArr = senior.assistLineCfg.assistLine
  if (assistLineArr?.length > 0) {
    const customStyle = parseJson(chart.customStyle)
    let yAxisPosition, axisFormatterCfg, yAxisExtPosition, axisExtFormatterCfg
    if (customStyle.yAxis) {
      const a = JSON.parse(JSON.stringify(customStyle.yAxis))
      yAxisPosition = a.position
      axisFormatterCfg = a.axisLabelFormatter
        ? a.axisLabelFormatter
        : DEFAULT_YAXIS_STYLE.axisLabelFormatter
    }
    if (customStyle.yAxisExt) {
      const a = JSON.parse(JSON.stringify(customStyle.yAxisExt))
      yAxisExtPosition = a.position
      axisExtFormatterCfg = a.axisLabelFormatter
        ? a.axisLabelFormatter
        : DEFAULT_YAXIS_EXT_STYLE.axisLabelFormatter
    }

    const fixedLines = assistLineArr.filter(ele => ele.field === '0')
    const dynamicLineFields = assistLineArr
      .filter(ele => ele.field === '1')
      .map(item => item.fieldId)
    const quotaFields = filter(chart.yAxis, ele => ele.summary !== '' && ele.id !== '-1')
    const quotaExtFields = filter(chart.yAxisExt, ele => ele.summary !== '' && ele.id !== '-1')
    const dynamicLines = chart.data.dynamicAssistLines?.filter(item => {
      return (
        dynamicLineFields?.includes(item.fieldId) &&
        (!!find(quotaFields, d => d.id === item.fieldId) ||
          (!!find(quotaExtFields, d => d.id === item.fieldId) && chart.type.includes('chart-mix')))
      )
    })
    const lines = fixedLines.concat(dynamicLines || [])
    lines.forEach(ele => {
      const value = parseFloat(ele.value)
      const content =
        ele.name +
        ' : ' +
        valueFormatter(value, ele.yAxisType === 'left' ? axisFormatterCfg : axisExtFormatterCfg)
      assistLine.push({
        type: 'line',
        yAxisType: ele.yAxisType,
        start: ['start', value],
        end: ['end', value],
        style: {
          stroke: ele.color,
          lineDash: getLineDash(ele.lineType)
        }
      })
      assistLine.push({
        type: 'text',
        yAxisType: ele.yAxisType,
        position: [
          (ele.yAxisType === 'left' ? yAxisPosition : yAxisExtPosition) === 'left'
            ? 'start'
            : 'end',
          value
        ],
        content: content,
        offsetY: -2,
        offsetX:
          (ele.yAxisType === 'left' ? yAxisPosition : yAxisExtPosition) === 'left'
            ? 2
            : -10 * (content.length - 2),
        style: {
          textBaseline: 'bottom',
          fill: ele.color,
          fontSize: ele.fontSize ? ele.fontSize : 10
        }
      })
    })
  }
  return assistLine
}

export function getAnalyseHorizontal(chart: Chart) {
  const assistLine = []
  const senior = parseJson(chart.senior)
  if (!senior.assistLineCfg?.enable) {
    return assistLine
  }
  const assistLineArr = senior.assistLineCfg.assistLine
  if (assistLineArr?.length > 0) {
    const customStyle = parseJson(chart.customStyle)
    let xAxisPosition, axisFormatterCfg
    if (customStyle.xAxis) {
      const a = JSON.parse(JSON.stringify(customStyle.xAxis))
      xAxisPosition = transAxisPosition(a.position)
      axisFormatterCfg = a.axisLabelFormatter
        ? a.axisLabelFormatter
        : DEFAULT_XAXIS_STYLE.axisLabelFormatter
    }

    const fixedLines = assistLineArr.filter(ele => ele.field === '0')
    const dynamicLineFields = assistLineArr
      .filter(ele => ele.field === '1')
      .map(item => item.fieldId)
    const quotaFields = filter(chart.yAxis, ele => ele.summary !== '' && ele.id !== '-1')
    const dynamicLines = chart.data.dynamicAssistLines?.filter(
      item =>
        dynamicLineFields?.includes(item.fieldId) && !!find(quotaFields, d => d.id === item.fieldId)
    )
    const lines = fixedLines.concat(dynamicLines || [])

    lines.forEach(ele => {
      const value = parseFloat(ele.value)
      const content = ele.name + ' : ' + valueFormatter(value, axisFormatterCfg)
      assistLine.push({
        type: 'line',
        start: ['start', value],
        end: ['end', value],
        style: {
          stroke: ele.color,
          lineDash: getLineDash(ele.lineType)
        }
      })
      assistLine.push({
        type: 'text',
        position: ['start', value],
        content: content,
        offsetY: 5,
        offsetX: 2,
        rotate: Math.PI / 2,
        style: {
          textBaseline: 'bottom',
          fill: ele.color,
          fontSize: ele.fontSize ? ele.fontSize : 10
        }
      })
    })
  }
  return assistLine
}

export function getLineDash(type) {
  switch (type) {
    case 'solid':
      return [0, 0]
    case 'dashed':
      return [10, 8]
    case 'dotted':
      return [2, 2]
    default:
      return [0, 0]
  }
}

/**
 * 将 RGBA 格式的颜色转换成 ANTV 支持的渐变色格式
 * @param rawColor 原始 RGBA 颜色
 * @param show
 * @param angle 渐变角度
 * @param start 起始值
 */
export function setGradientColor(rawColor: string, show = false, angle = 0, start = 0) {
  const item = rawColor.split(',')
  const alpha = parseFloat(item[3].replace(')', ''))
  const startAlpha = alpha * 0.3
  item.splice(3, 1, `${startAlpha})`)
  let color: string
  if (start == 0) {
    color = `l(${angle}) 0:${item.join(',')} 1:${rawColor}`
  } else if (start > 0) {
    color = `l(${angle}) 0:rgba(255,255,255,0) ${start}:${item.join(',')} 1:${rawColor}`
  } else {
    color = `l(${angle}) 0:rgba(255,255,255,0) 0.1:${item.join(',')} 1:${rawColor}`
  }
  return show ? color : rawColor
}

export function transAxisPosition(position: string): string {
  switch (position) {
    case 'top':
      return 'left'
    case 'bottom':
      return 'right'
    case 'left':
      return 'bottom'
    case 'right':
      return 'top'
    default:
      return position
  }
}

export function configL7Label(chart: Chart): false | LabelOptions {
  const customAttr = parseJson(chart.customAttr)
  const label = customAttr.label
  const style = {
    fill: label.color,
    fontSize: label.fontSize,
    textAllowOverlap: true,
    fontWeight: 'bold'
  }
  if (!label.fullDisplay) {
    style.textAllowOverlap = false
    style.padding = [2, 2]
  }
  if (chart.fontFamily) {
    style.fontFamily = chart.fontFamily
  }
  return {
    visible: label.show,
    style
  }
}

export function configL7Style(chart: Chart): AreaOptions['style'] {
  const customAttr = parseJson(chart.customAttr)
  return {
    stroke: customAttr.basicStyle.areaBorderColor
  }
}

export function configL7Tooltip(chart: Chart): TooltipOptions {
  const customAttr = parseJson(chart.customAttr)
  const tooltip = customAttr.tooltip
  const formatterMap = tooltip.seriesTooltipFormatter
    ?.filter(i => i.show)
    .reduce((pre, next) => {
      pre[next.id] = next
      return pre
    }, {}) as Record<string, SeriesFormatter>
  const container = document.getElementById(chart.container)
  if (container) {
    container.addEventListener('mousemove', event => {
      const rect = container.getBoundingClientRect()
      const mouseX = event.clientX - rect.left
      const mouseY = event.clientY - rect.top
      const tooltipElement = container.getElementsByClassName('l7plot-tooltip-container')
      for (let i = 0; i < tooltipElement?.length; i++) {
        const element = tooltipElement[i] as HTMLElement
        const isNearRightEdge = container.clientWidth - mouseX <= element.clientWidth
        const isNearBottomEdge = container.clientHeight - mouseY <= element.clientHeight
        let transform = ''
        if (isNearRightEdge) {
          transform += 'translateX(-120%) '
        }
        if (isNearBottomEdge) {
          transform += 'translateY(-100%) '
        }
        if (transform) {
          element.style.transform = transform.trim()
        }
      }
    })
  }
  return {
    customTitle(data) {
      return data.name
    },
    customItems(originalItem) {
      const result = []
      if (isEmpty(formatterMap)) {
        return result
      }
      const head = originalItem.properties
      if (!head) {
        return result
      }
      const formatter = formatterMap[head.quotaList?.[0]?.id]
      if (!isEmpty(formatter)) {
        const originValue = parseFloat(head.value as string)
        const value = valueFormatter(originValue, formatter.formatterCfg)
        const name = isEmpty(formatter.chartShowName) ? formatter.name : formatter.chartShowName
        result.push({ ...head, name, value: `${value ?? ''}` })
      }
      head.dynamicTooltipValue?.forEach(item => {
        const formatter = formatterMap[item.fieldId]
        if (formatter) {
          const value = valueFormatter(parseFloat(item.value), formatter.formatterCfg)
          const name = isEmpty(formatter.chartShowName) ? formatter.name : formatter.chartShowName
          result.push({ color: 'grey', name, value: `${value ?? ''}` })
        }
      })
      return result
    },
    showComponent: tooltip.show,
    domStyles: {
      'l7plot-tooltip': {
        'background-color': tooltip.backgroundColor,
        'font-size': `${tooltip.fontSize}px`,
        'line-height': 1.6,
        'font-family': chart.fontFamily ? chart.fontFamily : undefined
      },
      'l7plot-tooltip__name': {
        color: tooltip.color
      },
      'l7plot-tooltip__value': {
        color: tooltip.color
      },
      'l7plot-tooltip__title': {
        color: tooltip.color
      }
    }
  }
}

export function handleGeoJson(
  geoJson: FeatureCollection,
  nameMapping?: Record<string, string>,
  useGlobalAreaMapping = false
) {
  let mapping = nameMapping
  if (useGlobalAreaMapping && geoJson?.['deMapping']) {
    mapping = geoJson['deMapping']
  }
  geoJson?.features.forEach(item => {
    if (!item.properties['centroid']) {
      if (item.properties['center']) {
        item.properties['centroid'] = item.properties['center']
      } else {
        const tmp = centroid(item.geometry)
        item.properties['centroid'] = tmp.geometry.coordinates
      }
    }
    let name = item.properties['name']
    if (mapping?.[name]) {
      name = mapping[name]
      item.properties['name'] = name
    }
  })
}

export function getTooltipSeriesTotalMap(data: any[]): Record<string, number> {
  const result = {}
  data?.forEach(item => {
    item.dynamicTooltipValue?.forEach(ele => {
      if (!result[ele.fieldId]) {
        result[ele.fieldId] = 0
      }
      if (ele.value) {
        result[ele.fieldId] = add(result[ele.fieldId], ele.value)
      }
    })
  })
  return result
}
const LEGEND_SHAPE_STYLE_MAP = {
  circle: {
    borderRadius: '50%'
  },
  square: {},
  triangle: {
    borderLeft: '5px solid transparent',
    borderRight: '5px solid transparent',
    borderBottom: '10px solid var(--bgColor)',
    background: 'unset'
  },
  diamond: {
    transform: 'rotate(45deg)'
  }
}
export function configL7Legend(chart: Chart): LegendOptions | false {
  const { basicStyle } = parseJson(chart.customAttr)
  if (basicStyle.suspension === false && basicStyle.showZoom === undefined) {
    return false
  }
  const { legend } = parseJson(chart.customStyle)
  if (!legend.show) {
    return false
  }
  return {
    position: 'bottomleft',
    customContent: (_: string, items: CategoryLegendListItem[]) => {
      const showItems = items?.length > 30 ? items.slice(0, 30) : items
      if (showItems?.length) {
        const containerDom = createDom(CONTAINER_TPL) as HTMLElement
        const listDom = containerDom.getElementsByClassName(LIST_CLASS)[0] as HTMLElement
        showItems.forEach(item => {
          let value = '-'
          if (item.value !== '') {
            if (Array.isArray(item.value)) {
              item.value.forEach((v, i) => {
                item.value[i] = Number.isNaN(v) || v === 'NaN' ? 'NaN' : parseFloat(v).toFixed(0)
              })
              value = item.value.join('-')
            } else {
              const tmp = item.value as string
              value = Number.isNaN(tmp) || tmp === 'NaN' ? 'NaN' : parseFloat(tmp).toFixed(0)
            }
          }
          const substituteObj = { ...item, value }

          const domStr = substitute(ITEM_TPL, substituteObj)
          const itemDom = createDom(domStr)
          // 给 legend 形状用的
          itemDom.style.setProperty('--bgColor', item.color)
          listDom.appendChild(itemDom)
        })
        return listDom
      }
      return ''
    },
    domStyles: {
      'l7plot-legend__category-value': {
        fontSize: legend.fontSize + 'px',
        color: legend.color
      },
      'l7plot-legend__category-marker': {
        ...LEGEND_SHAPE_STYLE_MAP[legend.icon]
      }
    }
  }
}
const ZOOM_IN_BTN =
  '<svg t="1717484429999" fill="${fill}" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="9332" width="14px" height="14px"><path d="M47.653 439.326c-24.501 0-44.368 19.881-44.368 44.4v56.548c0 24.492 19.862 44.4 44.368 44.4h928.694c24.501 0 44.368-19.881 44.368-44.4v-56.548c0-24.497-19.862-44.4-44.368-44.4H47.653z" p-id="9333"></path><path d="M586.326 47.653c0-24.501-19.881-44.368-44.4-44.368h-56.548c-24.492 0-44.4 19.862-44.4 44.368v928.694c0 24.501 19.881 44.368 44.4 44.368h56.548c24.497 0 44.4-19.862 44.4-44.368V47.653z" p-id="9334"></path></svg>'
const RESET_BTN =
  '<svg t="1717487786436" fill="${fill}" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="18361" width="14px" height="14px"><path d="M127.594667 503.274667a383.573333 383.573333 0 0 1 112.426666-263.04 380.864 380.864 0 0 1 122.24-82.474667 382.421333 382.421333 0 0 1 149.632-30.165333c51.946667 0 102.250667 10.176 149.504 30.165333a381.610667 381.610667 0 0 1 122.133334 82.474667 385.152 385.152 0 0 1 31.082666 35.093333l-67.285333 52.501333a8.96 8.96 0 0 0 3.349333 15.765334l196.352 48.042666a8.96 8.96 0 0 0 11.050667-8.597333l0.896-202.154667c0-7.466667-8.597333-11.733333-14.421333-7.04l-63.018667 49.28C795.605333 113.173333 661.973333 42.666667 511.786667 42.666667 255.786667 42.666667 47.488 247.829333 42.666667 502.826667a8.96 8.96 0 0 0 8.96 9.173333h67.029333c4.906667 0 8.832-3.925333 8.96-8.725333z m844.8 8.725333h-67.050667a8.917333 8.917333 0 0 0-8.96 8.704 381.76 381.76 0 0 1-30.037333 140.8 382.336 382.336 0 0 1-82.346667 122.24 382.656 382.656 0 0 1-271.893333 112.64 382.421333 382.421333 0 0 1-271.765334-112.64 385.152 385.152 0 0 1-31.061333-35.072l67.264-52.522667a8.96 8.96 0 0 0-3.349333-15.765333l-196.330667-48.042667a8.96 8.96 0 0 0-11.050667 8.597334l-0.789333 202.261333c0 7.488 8.597333 11.733333 14.421333 7.04l63.018667-49.28C228.394667 910.826667 362.026667 981.333333 512.213333 981.333333 768.341333 981.333333 976.512 776.042667 981.333333 521.173333a8.96 8.96 0 0 0-8.96-9.173333z" p-id="18362"></path></svg>'
const ZOOM_OUT_BTN =
  '<svg t="1717486240292" fill="${fill}" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="13641" width="14px" height="14px"><path d="M935 423.3H89C40.2 423.3 0.3 463.2 0.3 512c0 48.8 39.9 88.7 88.7 88.7h846c48.8 0 88.7-39.9 88.7-88.7 0-48.8-39.9-88.7-88.7-88.7z" p-id="13642"></path></svg>'
export class CustomZoom extends Zoom {
  resetButtonGroup(container) {
    DOM.clearChildren(container)
    this['zoomInButton'] = this['createButton'](
      this.controlOption.zoomInText,
      this.controlOption.zoomInTitle,
      'l7-button-control',
      container,
      this.zoomIn
    )
    this['zoomResetButton'] = this['createButton'](
      this.controlOption['resetText'],
      'Reset',
      'l7-button-control',
      container,
      () => {
        if (this.mapsService.map?.deMapProvider == 'qq') {
          if (this.mapsService.map.deMapAutoFit) {
            this.mapsService.setZoomAndCenter(this.mapsService.map.deMapAutoZoom, [
              this.mapsService.map.deMapAutoLng,
              this.mapsService.map.deMapAutoLat
            ])
          } else {
            this.mapsService.setZoomAndCenter(
              this.controlOption['initZoom'],
              this.controlOption['center']
            )
          }
        } else {
          if (this.controlOption['bounds']) {
            this.mapsService.fitBounds(this.controlOption['bounds'], { animate: true })
          } else {
            this.mapsService.setZoomAndCenter(
              this.controlOption['initZoom'],
              this.controlOption['center']
            )
          }
        }
      }
    )
    if (this.controlOption.showZoom) {
      this['zoomNumDiv'] = this['createButton'](
        '0',
        '',
        'l7-button-control l7-control-zoom__number',
        container
      )
    }
    this['zoomOutButton'] = this['createButton'](
      this.controlOption.zoomOutText,
      this.controlOption.zoomOutTitle,
      'l7-button-control',
      container,
      this.zoomOut
    )
    const { buttonBackground } = this.controlOption as any
    const elements = [this['zoomResetButton'], this['zoomInButton'], this['zoomOutButton']]
    if (buttonBackground) {
      setStyle(elements, 'background', buttonBackground)
    }
    setStyle(elements, 'border-bottom', 'none')
    this['updateDisabled']()
  }
  public getDefault(option: Partial<IZoomControlOption>) {
    const { buttonColor } = option as any
    let zoomInText = ZOOM_IN_BTN
    let zoomOutText = ZOOM_OUT_BTN
    let resetText = RESET_BTN
    if (buttonColor) {
      zoomInText = zoomInText.replace('${fill}', buttonColor)
      zoomOutText = zoomOutText.replace('${fill}', buttonColor)
      resetText = resetText.replace('${fill}', buttonColor)
    }
    return {
      ...option,
      position: PositionType.BOTTOMRIGHT,
      name: 'zoom',
      zoomInText,
      zoomInTitle: 'Zoom in',
      zoomOutText,
      zoomOutTitle: 'Zoom out',
      resetText,
      showZoom: false
    } as IZoomControlOption
  }
}

class CustomTileZoom extends CustomZoom {
  resetButtonGroup(container) {
    DOM.clearChildren(container)
    const zoomIn = () => this.mapsService.zoomIn({ duration: 0 })
    const zoomOut = () => this.mapsService.zoomOut({ duration: 0 })
    const zoomReset = () => {
      if (this.controlOption['bounds']) {
        this.mapsService.fitBounds(this.controlOption['bounds'], {
          animate: false,
          duration: 0
        })
      } else {
        this.mapsService.map?.jumpTo({
          zoom: this.controlOption['initZoom'],
          center: this.controlOption['center']
        })
      }
    }
    this['zoomInButton'] = this['createButton'](
      this.controlOption.zoomInText,
      this.controlOption.zoomInTitle,
      'l7-button-control',
      container,
      zoomIn
    )
    this['zoomResetButton'] = this['createButton'](
      this.controlOption['resetText'],
      'Reset',
      'l7-button-control',
      container,
      zoomReset
    )
    if (this.controlOption.showZoom) {
      this['zoomNumDiv'] = this['createButton'](
        '0',
        '',
        'l7-button-control l7-control-zoom__number',
        container
      )
    }
    this['zoomOutButton'] = this['createButton'](
      this.controlOption.zoomOutText,
      this.controlOption.zoomOutTitle,
      'l7-button-control',
      container,
      zoomOut
    )
    const { buttonBackground } = this.controlOption as any
    const elements = [this['zoomResetButton'], this['zoomInButton'], this['zoomOutButton']]
    if (buttonBackground) {
      setStyle(elements, 'background', buttonBackground)
    }
    setStyle(elements, 'border-bottom', 'none')
    this['updateDisabled']()
  }
}

const ONLINE_MAP_INTERACTION_ENABLED = '__deOnlineMapInteractionEnabled'
const ONLINE_MAP_INTERACTION_TYPE = '__deOnlineMapInteractionType'
const ONLINE_MAP_INTERACTION_PENDING = '__deOnlineMapInteractionPending'

function applyOnlineMapInteraction(scene: Scene, mapType: string | undefined, enable: boolean) {
  const map = scene.map as any
  if (!map) {
    return
  }
  switch (mapType) {
    case CUSTOM_TILE_MAP_TYPE: {
      const method = enable ? 'enable' : 'disable'
      ;[
        'dragPan',
        'scrollZoom',
        'boxZoom',
        'doubleClickZoom',
        'dragRotate',
        'keyboard',
        'touchPitch',
        'touchZoomRotate'
      ].forEach(handler => map[handler]?.[method]?.())
      break
    }
    case 'tianditu': {
      const method = enable ? 'enable' : 'disable'
      map[`${method}Drag`]?.()
      map[`${method}ScrollWheelZoom`]?.()
      map[`${method}DoubleClickZoom`]?.()
      map[`${method}Keyboard`]?.()
      map[`${method}PinchToZoom`]?.()
      break
    }
    case 'qq':
      map.setDraggable?.(enable)
      map.setScrollable?.(enable)
      map.setDoubleClickZoom?.(enable)
      map.setTouchZoomable?.(enable)
      map.setPitchable?.(enable)
      map.setRotatable?.(enable)
      break
    default:
      map.setStatus?.({
        dragEnable: enable,
        keyboardEnable: enable,
        doubleClickZoom: enable,
        rotateEnable: enable,
        pitchEnable: enable,
        scrollWheel: enable,
        touchZoom: enable
      })
  }
}

function configOnlineMapInteraction(scene: Scene, mapType: string | undefined, enable: boolean) {
  const sceneState = scene as any
  sceneState[ONLINE_MAP_INTERACTION_ENABLED] = enable
  sceneState[ONLINE_MAP_INTERACTION_TYPE] = mapType
  const applyLatestStatus = () => {
    sceneState[ONLINE_MAP_INTERACTION_PENDING] = false
    applyOnlineMapInteraction(
      scene,
      sceneState[ONLINE_MAP_INTERACTION_TYPE],
      sceneState[ONLINE_MAP_INTERACTION_ENABLED]
    )
  }
  if (scene.loaded) {
    applyLatestStatus()
    return
  }
  if (!sceneState[ONLINE_MAP_INTERACTION_PENDING]) {
    // 沿用 v2 语义：隐藏缩放按钮时同步锁定地图交互
    sceneState[ONLINE_MAP_INTERACTION_PENDING] = true
    scene.once('loaded', applyLatestStatus)
  }
}

const L7_SCALED_INTERACTION_FLAG = '__deScaledInteractionPatched'
const L7_SCALED_INTERACTION_CHARTS = ['map', 'bubble-map']

function getScaledL7ContainerPoint(
  container: HTMLElement,
  clientX: number,
  clientY: number,
  includeClientOffset = true
) {
  const rect = container.getBoundingClientRect()
  const scaleX = rect.width && container.clientWidth ? rect.width / container.clientWidth : 1
  const scaleY = rect.height && container.clientHeight ? rect.height / container.clientHeight : 1
  const safeScaleX = Number.isFinite(scaleX) && scaleX > 0 ? scaleX : 1
  const safeScaleY = Number.isFinite(scaleY) && scaleY > 0 ? scaleY : 1
  return {
    x: (clientX - rect.left) / safeScaleX - (includeClientOffset ? container.clientLeft : 0),
    y: (clientY - rect.top) / safeScaleY - (includeClientOffset ? container.clientTop : 0)
  }
}

function rebindL7MouseMoveListener(interactionService: Record<string, any>, originalOnHover) {
  const container = interactionService.mapService?.getMapContainer?.() as HTMLElement
  if (!container || typeof originalOnHover !== 'function') {
    return
  }
  container.removeEventListener('mousemove', originalOnHover)
  container.addEventListener('mousemove', interactionService.onHover)
}

function getScaledL7HammerInteractionTarget(interactionService: Record<string, any>, target) {
  const { type, pointerType } = target
  let clientX
  let clientY
  if (pointerType === 'touch') {
    clientY = Math.floor(target.pointers[0].clientY)
    clientX = Math.floor(target.pointers[0].clientX)
  } else {
    clientY = Math.floor(target.srcEvent.y)
    clientX = Math.floor(target.srcEvent.x)
  }
  const mapContainer = interactionService.mapService?.getMapContainer?.() as HTMLElement
  if (mapContainer && typeof clientX === 'number' && typeof clientY === 'number') {
    const point = getScaledL7ContainerPoint(mapContainer, clientX, clientY, false)
    clientX = point.x
    clientY = point.y
  }
  const lngLat = interactionService.mapService?.containerToLngLat?.([clientX, clientY])
  return {
    x: clientX,
    y: clientY,
    lngLat,
    type,
    target: target.srcEvent
  }
}

function configL7ScaledInteraction(chart: Chart, scene?: Scene) {
  if (!L7_SCALED_INTERACTION_CHARTS.includes(chart?.type)) {
    return
  }
  const interactionService = (scene as any)?.interactionService as Record<string, any>
  if (!interactionService || interactionService[L7_SCALED_INTERACTION_FLAG]) {
    return
  }
  const originalOnHover = interactionService.onHover
  const originalInteractionEvent = interactionService.interactionEvent
  if (typeof originalOnHover !== 'function' || typeof originalInteractionEvent !== 'function') {
    return
  }
  interactionService[L7_SCALED_INTERACTION_FLAG] = true
  interactionService.interactionEvent = target => {
    if (['click', 'dblclick'].includes(target?.type)) {
      // 大屏编辑缩放后，L7 Hammer 点击坐标也需要还原到未缩放容器
      return getScaledL7HammerInteractionTarget(interactionService, target)
    }
    return originalInteractionEvent.call(interactionService, target)
  }
  interactionService.onHover = (event: MouseEvent & { type: string }) => {
    if (event.type !== 'mousemove') {
      originalOnHover(event)
      return
    }
    const { clientX, clientY } = event
    let x = clientX
    let y = clientY
    const type = event.type
    const mapContainer = interactionService.mapService?.getMapContainer?.() as HTMLElement
    if (!mapContainer || typeof clientX !== 'number' || typeof clientY !== 'number') {
      originalOnHover(event)
      return
    }
    // 大屏编辑缩放后，L7 hover picking 坐标需要还原到未缩放容器
    const point = getScaledL7ContainerPoint(mapContainer, clientX, clientY)
    x = point.x
    y = point.y
    const lngLat = interactionService.mapService?.containerToLngLat?.([x, y])
    interactionService.emit?.(InteractionEvent.Hover, {
      x,
      y,
      lngLat,
      type,
      target: event
    })
  }
  rebindL7MouseMoveListener(interactionService, originalOnHover)
}

export function configL7Zoom(chart: Chart, scene: Scene, mapKey?: OnlineMapConfig) {
  configL7ScaledInteraction(chart, scene)
  const { basicStyle } = parseJson(chart.customAttr)
  const zoomOption = scene?.getControlByName('zoom')
  if (zoomOption) {
    scene.removeControl(zoomOption)
  }
  const hideZoom = shouldHideZoom(basicStyle)
  configOnlineMapInteraction(scene, mapKey?.mapType, !hideZoom)
  if (hideZoom) {
    return
  }
  if (mapKey?.mapType === CUSTOM_TILE_MAP_TYPE) {
    configCustomTileZoom(chart, scene, basicStyle)
    return
  }
  if (!scene?.getControlByName('zoom')) {
    if (!scene.map) {
      scene.once('loaded', () => {
        switch (mapKey?.mapType) {
          case 'tianditu':
            //天地图
            {
              const initZoom = basicStyle.autoFit === false ? basicStyle.zoomLevel : scene.getZoom()
              const center =
                basicStyle.autoFit === false
                  ? [basicStyle.mapCenter.longitude, basicStyle.mapCenter.latitude]
                  : [scene.map.getCenter().getLng(), scene.map.getCenter().getLat()]
              const newZoomOptions = {
                initZoom: initZoom,
                center: center,
                buttonColor: basicStyle.zoomButtonColor,
                buttonBackground: basicStyle.zoomBackground
              } as any
              scene.addControl(new CustomZoom(newZoomOptions))
            }
            break
          case 'qq':
            {
              const initZoom = basicStyle.autoFit === false ? basicStyle.zoomLevel : scene.getZoom()
              const center =
                basicStyle.autoFit === false
                  ? [basicStyle.mapCenter.longitude, basicStyle.mapCenter.latitude]
                  : [scene.map.getCenter().lng, scene.map.getCenter().lat]
              const newZoomOptions = {
                initZoom: initZoom,
                center: center,
                buttonColor: basicStyle.zoomButtonColor,
                buttonBackground: basicStyle.zoomBackground
              } as any
              scene.addControl(new CustomZoom(newZoomOptions))
            }
            break
          default:
            scene.map.on('complete', () => {
              const initZoom = basicStyle.autoFit === false ? basicStyle.zoomLevel : scene.getZoom()
              const center =
                basicStyle.autoFit === false
                  ? [basicStyle.mapCenter.longitude, basicStyle.mapCenter.latitude]
                  : [scene.map.getCenter().lng, scene.map.getCenter().lat]
              const newZoomOptions = {
                initZoom: initZoom,
                center: center,
                buttonColor: basicStyle.zoomButtonColor,
                buttonBackground: basicStyle.zoomBackground
              } as any
              scene.addControl(new CustomZoom(newZoomOptions))
            })
        }
      })
    } else {
      const newZoomOptions = {
        buttonColor: basicStyle.zoomButtonColor,
        buttonBackground: basicStyle.zoomBackground
      } as any
      if (basicStyle.autoFit === false) {
        newZoomOptions.initZoom = basicStyle.zoomLevel
        newZoomOptions.center = [basicStyle.mapCenter.longitude, basicStyle.mapCenter.latitude]
      } else {
        const coordinates: [][] = []
        if (chart.type === 'flow-map') {
          const startAxis = chart.xAxis
          const endAxis = chart.xAxisExt
          if (startAxis?.length === 2) {
            chart.data?.tableRow?.forEach(row => {
              coordinates.push([row[startAxis[0].dataeaseName], row[startAxis[1].dataeaseName]])
            })
          }
          if (endAxis?.length === 2) {
            chart.data?.tableRow?.forEach(row => {
              coordinates.push([row[endAxis[0].dataeaseName], row[endAxis[1].dataeaseName]])
            })
          }
        } else {
          const axis = chart.xAxis
          if (axis?.length === 2) {
            chart.data?.tableRow?.forEach(row => {
              coordinates.push([row[axis[0].dataeaseName], row[axis[1].dataeaseName]])
            })
          }
        }
        newZoomOptions.bounds = calculateBounds(coordinates)
      }
      scene.addControl(new CustomZoom(newZoomOptions))
    }
  }
}

function configCustomTileZoom(chart: Chart, scene: Scene, basicStyle: ChartBasicStyle) {
  const addControl = () => {
    if (scene.getControlByName('zoom')) {
      return
    }
    const mapCenter = scene.map?.getCenter()
    const options = {
      initZoom: basicStyle.autoFit === false ? basicStyle.zoomLevel : scene.getZoom(),
      center:
        basicStyle.autoFit === false
          ? [basicStyle.mapCenter.longitude, basicStyle.mapCenter.latitude]
          : [mapCenter?.lng ?? 105, mapCenter?.lat ?? 35],
      buttonColor: basicStyle.zoomButtonColor,
      buttonBackground: basicStyle.zoomBackground
    } as any
    if (basicStyle.autoFit !== false) {
      const coordinates: number[][] = []
      const appendCoordinates = (axis: ChartViewField[]) => {
        if (axis?.length !== 2) {
          return
        }
        chart.data?.tableRow?.forEach(row => {
          coordinates.push([row[axis[0].dataeaseName], row[axis[1].dataeaseName]])
        })
      }
      appendCoordinates(chart.xAxis)
      if (chart.type === 'flow-map') {
        appendCoordinates(chart.xAxisExt)
      }
      if (coordinates.length) {
        options.bounds = calculateBounds(coordinates)
      }
    }
    // MapLibre 使用独立缩放控制，避免改变区域地图共享控件行为
    scene.addControl(new CustomTileZoom(options))
  }
  if (scene.loaded) {
    addControl()
  } else {
    scene.once('loaded', addControl)
  }
}
/**
 * 计算经纬度数据的边界点
 * @param coordinates 经纬度数组 [[lng, lat], [lng, lat], ...]
 * @returns {[[number, number], [number, number]]} 返回东北角和西南角的坐标
 */
export function calculateBounds(coordinates: number[][]): {
  northEast: [number, number]
  southWest: [number, number]
} {
  if (!coordinates || coordinates.length === 0) {
    return {
      northEast: [180, 90],
      southWest: [-180, -90]
    }
  }

  let maxLng = -180
  let minLng = 180
  let maxLat = -90
  let minLat = 90

  coordinates.forEach(([lng, lat]) => {
    maxLng = Math.max(maxLng, lng)
    minLng = Math.min(minLng, lng)
    maxLat = Math.max(maxLat, lat)
    minLat = Math.min(minLat, lat)
  })

  return [
    [maxLng, maxLat], // 东北角坐标
    [minLng, minLat] // 西南角坐标
  ]
}

function configL7PlotInteraction(scene: Scene, enable: boolean) {
  const applyInteraction = () => {
    const map = scene?.map as any
    const method = enable ? 'enable' : 'disable'
    // 沿用在线地图语义：隐藏缩放按钮时同步锁定地图交互
    ;[
      'zoomEnable',
      'dragEnable',
      'dragPan',
      'scrollZoom',
      'boxZoom',
      'doubleClickZoom',
      'dragRotate',
      'touchPitch',
      'touchZoomRotate'
    ].forEach(handler => map?.[handler]?.[method]?.())
  }
  if (scene?.loaded) {
    applyInteraction()
  } else {
    scene?.once('loaded', applyInteraction)
  }
}

export function configL7PlotZoom(chart: Chart, plot: L7Plot<PlotOptions>) {
  configL7ScaledInteraction(chart, plot?.scene as Scene)
  const { basicStyle } = parseJson(chart.customAttr)
  const hideZoom = shouldHideZoom(basicStyle)
  configL7PlotInteraction(plot?.scene as Scene, !hideZoom)
  if (hideZoom) {
    return
  }
  plot.once('loaded', () => {
    const zoomOptions = {
      initZoom: plot.scene.getZoom(),
      center: plot.scene.getCenter(),
      buttonColor: basicStyle.zoomButtonColor,
      buttonBackground: basicStyle.zoomBackground
    } as any
    plot.scene.addControl(new CustomZoom(zoomOptions))
  })
}

function setStyle(elements: HTMLElement[], styleProp: string, value) {
  elements.forEach(e => {
    e.style[styleProp] = value
  })
}

export function mapRendering(dom: HTMLElement | string) {
  if (typeof dom === 'string') {
    dom = document.getElementById(dom)
  }
  dom.classList.add('de-map-rendering')
}

export function qqMapRendered(scene?: Scene) {
  if (scene?.map && scene.map.deMapProvider === 'qq') {
    setTimeout(() => {
      if (scene.map) {
        scene.map.deMapAutoZoom = scene.map.getZoom()
        scene.map.deMapAutoLng = scene.map.getCenter().getLng()
        scene.map.deMapAutoLat = scene.map.getCenter().getLat()
      }
    }, 1000)
  }
}

export function mapRendered(dom: HTMLElement | string) {
  if (typeof dom === 'string') {
    dom = document.getElementById(dom)
  }
  dom.classList.add('de-map-rendered')
}

export function getMapCenter(basicStyle: ChartBasicStyle) {
  let center: [number, number]
  if (basicStyle.autoFit === false) {
    const longitude = basicStyle?.mapCenter?.longitude ?? DEFAULT_BASIC_STYLE.mapCenter.longitude
    const latitude = basicStyle?.mapCenter?.latitude ?? DEFAULT_BASIC_STYLE.mapCenter.latitude
    center = [longitude, latitude]
  } else {
    center = undefined
  }
  return center
}

export function getMapStyle(mapKey: OnlineMapConfig, basicStyle: ChartBasicStyle) {
  let mapStyle: any
  switch (mapKey.mapType) {
    case CUSTOM_TILE_MAP_TYPE:
      mapStyle = getCustomOnlineMapStyle(mapKey)
      break
    case 'tianditu':
      if (!find(tdtMapStyleOptions, s => s.value === basicStyle.mapStyle)) {
        mapStyle = 'normal'
      } else {
        mapStyle = basicStyle.mapStyle
      }
      break
    case 'qq':
      if (
        !find(qqMapStyleOptions, s => s.value === basicStyle.mapStyle) ||
        basicStyle.mapStyle === 'normal'
      ) {
        mapStyle = 'normal'
      } else {
        mapStyle = basicStyle.mapStyleUrl
      }
      break
    default:
      if (!find(gaodeMapStyleOptions, s => s.value === basicStyle.mapStyle)) {
        basicStyle.mapStyle = 'normal'
      }
      mapStyle = basicStyle.mapStyleUrl
      if (basicStyle.mapStyle !== 'custom') {
        mapStyle = `amap://styles/${basicStyle.mapStyle ? basicStyle.mapStyle : 'normal'}`
      }
      break
  }
  return mapStyle
}

export async function getMapScene(
  chart: Chart,
  scene: Scene,
  container: string,
  mapKey: OnlineMapConfig,
  basicStyle: ChartBasicStyle,
  miscStyle: ChartMiscAttr,
  mapStyle: any,
  center?: [number, number]
) {
  if (mapKey.mapType === CUSTOM_TILE_MAP_TYPE) {
    scene = await getCustomOnlineMapScene({
      scene,
      container,
      mapConfig: mapKey,
      basicStyle,
      miscStyle,
      mapStyle,
      center
    })
    mapRendering(container)
    if (scene.loaded) {
      mapRendered(container)
    } else {
      scene.once('loaded', () => mapRendered(container))
    }
    return scene
  }
  if (isCustomOnlineMapScene(scene)) {
    scene.destroy()
    scene = undefined
  }
  if (!scene) {
    scene = new Scene({
      id: container,
      logoVisible: false,
      map: getMapObject(mapKey, basicStyle, miscStyle, mapStyle, center)
    })
  } else {
    if (mapKey.mapType === 'tianditu') {
      scene.map?.checkResize()
    }
    if (scene.getLayers()?.length) {
      await scene.removeAllLayer()
      try {
        scene.setPitch(miscStyle.mapPitch)
      } catch (e) {}
      if (mapKey.mapType === 'tianditu') {
        if (mapStyle === 'normal') {
          scene.map?.removeStyle()
        } else {
          scene.setMapStyle(mapStyle)
        }
      } else {
        scene.setMapStyle(mapStyle)
      }

      scene.map.showLabel = !(basicStyle.showLabel === false)
      if (mapKey.mapType === 'qq') {
        scene.map.setBaseMap({
          //底图设置（参数为：VectorBaseMap对象）
          type: 'vector', //类型：失量底图
          features: basicStyle.showLabel === false ? ['base', 'building2d'] : undefined
          //仅渲染：道路及底面(base) + 2d建筑物(building2d)，以达到隐藏文字的效果
        })
      }
    }
    if (basicStyle.autoFit === false) {
      scene.setZoomAndCenter(basicStyle.zoomLevel, center)
      if (mapKey.mapType === 'qq') {
        scene.map.deMapAutoFit = false
        scene.map.deMapZoom = basicStyle.zoomLevel
        scene.map.deMapCenter = center
      }
    }
  }
  mapRendering(container)
  scene.once('loaded', () => {
    mapRendered(container)
    if (mapKey.mapType === 'qq') {
      scene.map.setBaseMap({
        //底图设置（参数为：VectorBaseMap对象）
        type: 'vector', //类型：失量底图
        features: basicStyle.showLabel === false ? ['base', 'building2d'] : undefined
        //仅渲染：道路及底面(base) + 2d建筑物(building2d)，以达到隐藏文字的效果
      })
      scene.setMapStyle(mapStyle)

      scene.map.deMapProvider = 'qq'
      scene.map.deMapAutoFit = !!basicStyle.autoFit
    }
    // 去除天地图自己的缩放按钮
    if (mapKey.mapType === 'tianditu') {
      if (mapStyle === 'normal') {
        scene.map?.removeStyle()
      } else {
        scene.setMapStyle(mapStyle)
      }

      const tdtControl = document.querySelector(
        `#component${chart.id} .tdt-control-zoom.tdt-bar.tdt-control`
      )
      if (tdtControl) {
        tdtControl.style.display = 'none'
      }
      const tdtControlOuter = document.querySelectorAll(
        `#wrapper-outer-id-${chart.id} .tdt-control-zoom.tdt-bar.tdt-control`
      )
      if (tdtControlOuter && tdtControlOuter.length > 0) {
        for (let i = 0; i < tdtControlOuter.length; i++) {
          tdtControlOuter[i].style.display = 'none'
        }
      }
      const tdtCopyrightControl = document.querySelector(
        `#component${chart.id} .tdt-control-copyright.tdt-control`
      )
      if (tdtCopyrightControl) {
        tdtCopyrightControl.style.display = 'none'
      }
      const tdtCopyrightControlOuter = document.querySelectorAll(
        `#wrapper-outer-id-${chart.id} .tdt-control-copyright.tdt-control`
      )
      if (tdtCopyrightControlOuter && tdtCopyrightControlOuter.length > 0) {
        for (let i = 0; i < tdtCopyrightControlOuter.length; i++) {
          tdtCopyrightControlOuter[i].style.display = 'none'
        }
      }
    }
  })
  return scene
}

export function getMapObject(
  mapKey: OnlineMapConfig,
  basicStyle: ChartBasicStyle,
  miscStyle: ChartMiscAttr,
  mapStyle: any,
  center?: [number, number]
) {
  switch (mapKey.mapType) {
    case 'tianditu':
      return new TMap({
        token: mapKey?.key ?? undefined,
        style: mapStyle, //不生效
        pitch: undefined, //不支持
        center,
        zoom: basicStyle.autoFit === false ? basicStyle.zoomLevel : undefined,
        showLabel: !(basicStyle.showLabel === false), //不支持
        WebGLParams: {
          preserveDrawingBuffer: true
        }
      })
    case 'qq':
      return new TencentMap({
        token: mapKey?.key ?? undefined,
        style: mapStyle,
        pitch: miscStyle.mapPitch,
        center,
        zoom: basicStyle.autoFit === false ? basicStyle.zoomLevel : 12,
        showLabel: !(basicStyle.showLabel === false),
        WebGLParams: {
          preserveDrawingBuffer: true
        }
      })
    default:
      return new GaodeMap({
        token: mapKey?.key ?? undefined,
        style: mapStyle,
        pitch: miscStyle.mapPitch,
        center,
        zoom: basicStyle.autoFit === false ? basicStyle.zoomLevel : undefined,
        showLabel: !(basicStyle.showLabel === false),
        WebGLParams: {
          preserveDrawingBuffer: true
        }
      })
  }
}
/**
 * 隐藏缩放控件
 * @param basicStyle
 */
function shouldHideZoom(basicStyle: any): boolean {
  return (
    (basicStyle.suspension === false && basicStyle.showZoom === undefined) ||
    basicStyle.showZoom === false
  )
}

const G2_TOOLTIP_WRAPPER = 'g2-tooltip-wrapper'
export function getTooltipContainer(id) {
  let wrapperDom = document.getElementById(G2_TOOLTIP_WRAPPER)
  if (!wrapperDom) {
    wrapperDom = document.createElement('div')
    wrapperDom.style.position = 'absolute'
    wrapperDom.style.zIndex = '9999'
    wrapperDom.id = G2_TOOLTIP_WRAPPER
    document.body.appendChild(wrapperDom)
  }
  const curDom = document.getElementById(id)
  if (curDom) {
    curDom.remove()
  }
  const g2Tooltip = document.createElement('div')
  g2Tooltip.setAttribute('id', id)
  g2Tooltip.classList.add('g2-tooltip')
  // 最多半屏，鼠标移入可滚动
  g2Tooltip.style.maxHeight = '50%'
  isMobile() ? (g2Tooltip.style.maxWidth = '50%') : (g2Tooltip.style.maxWidth = '25%')
  g2Tooltip.style.overflowY = 'auto'
  g2Tooltip.style.display = 'none'
  g2Tooltip.style.position = 'fixed'
  g2Tooltip.style.left = '0px'
  g2Tooltip.style.top = '0px'
  const g2TooltipTitle = document.createElement('div')
  g2TooltipTitle.classList.add('g2-tooltip-title')
  g2Tooltip.appendChild(g2TooltipTitle)

  const g2TooltipList = document.createElement('ul')
  g2TooltipList.classList.add('g2-tooltip-list')
  g2Tooltip.appendChild(g2TooltipList)
  const full = document.getElementsByClassName('fullscreen')
  if (full.length) {
    full.item(0).appendChild(g2Tooltip)
  } else {
    wrapperDom.appendChild(g2Tooltip)
  }
  return g2Tooltip
}

/**
 * 配置提示轮播
 * @param plot
 * @param chart
 */
function configCarouselTooltip(plot, chart) {
  const start = isSupport(chart.type) && !document.getElementById('multiplexingDrawer')
  if (start) {
    // 启用轮播
    plot.once('afterrender', () => {
      const sourceData = Array.isArray(plot?.options?.data) ? plot.options.data : []
      // 统一使用 G2TooltipCarousel 管理轮播，避免新旧 tooltip 轮播实现分叉
      new G2TooltipCarousel(plot?.chart || plot, chart, sourceData).start()
    })
  }
}
/**
 * 计算 Tooltip 的位置
 * @param {Chart} chart - 图表实例
 * @param {boolean} isCarousel - 是否为轮播模式
 * @param {object} tooltipCtl - Tooltip 控制器
 * @param {HTMLElement} chartElement - 图表元素
 * @param {Event} event - 事件对象
 * @param {boolean} enlargeElement - 放大弹窗
 * @returns {{x: number, y: number}} - 计算后的 x 和 y 坐标
 */
function calculateTooltipPosition(
  chart,
  isCarousel,
  tooltipCtl,
  chartElement,
  event,
  enlargeElement
) {
  // 辅助函数: 根据不同图表类型计算 Tooltip 的y位置
  const getTooltipY = () => {
    const top = Number(chartElement.getBoundingClientRect().top)
    if (isColumn(chart.type)) {
      return top + chartElement.getBoundingClientRect().height / 2
    }
    if (isMix(chart.type) || isPie(chart.type)) {
      return top + tooltipCtl.point.y
    }
    return top + tooltipCtl.point.y + 60
  }
  if (isCarousel) {
    return {
      x: tooltipCtl.point.x + Number(chartElement.getBoundingClientRect().left),
      y: getTooltipY()
    }
  } else {
    return { x: event.clientX, y: event.clientY }
  }
}
export function configPlotTooltipEvent<O extends PickOptions, P extends Plot<O>>(
  chart: Chart,
  plot: P
) {
  const { tooltip } = parseJson(chart.customAttr)
  if (!tooltip.show) {
    G2TooltipCarousel.destroyByContainer(chart.container)
    return
  }
  // 图表容器，用于计算 tooltip 的位置
  // 获取图表元素，优先顺序：放大 > 预览 > 公共连接页面 > 默认
  const chartElement =
    document.getElementById('container-viewDialog-' + chart.id + '-common') ||
    document.getElementById('container-preview-' + chart.id + '-common') ||
    document.getElementById('enlarge-inner-content-' + chart.id) ||
    document.getElementById('shape-id-' + chart.id)
  // 是否是放大弹窗
  const enlargeElement = chartElement?.id.includes('viewDialog')
  // 轮播时tooltip的zIndex
  const carousel_zIndex = enlargeElement ? '9999' : '1002'
  configCarouselTooltip(plot, chart)
  // 鼠标可移入, 移入之后保持显示, 移出之后隐藏
  plot.options.tooltip.container.addEventListener('mouseenter', e => {
    e.target.style.visibility = 'visible'
    e.target.style.display = 'block'
  })
  plot.options.tooltip.container.addEventListener('mouseleave', e => {
    e.target.style.visibility = 'hidden'
    e.target.style.display = 'none'
  })
  // 手动处理 tooltip 的显示和隐藏事件，需配合源码理解
  // https://github.com/antvis/G2/blob/master/src/chart/controller/tooltip.ts#showTooltip
  plot.on('tooltip:show', _d => {
    const tooltipCtl = plot.chart.getController('tooltip')
    if (!tooltipCtl) {
      return
    }
    // 处理 tooltip 与下拉菜单的显示冲突问题
    const viewTrackBarElement = document.getElementById('view-track-bar-' + chart.id)
    const event = plot.chart.interactions.tooltip?.context?.event
    // 是否时轮播模式
    const isCarousel =
      chart.customAttr?.tooltip?.carousel &&
      (!event || // 事件触发时，使用event的client坐标
        ['plot:leave', 'plot:mouseleave'].includes(event?.type) || //鼠标离开时，使用tooltipCtl.point
        ['pie', 'pie-rose', 'pie-donut'].includes(chart.type)) // 饼图时，使用tooltipCtl.point
    plot.options.tooltip.showMarkers = isCarousel ? true : false
    const wrapperDom = document.getElementById(G2_TOOLTIP_WRAPPER)
    wrapperDom.style.zIndex = isCarousel && wrapperDom ? carousel_zIndex : '9999'
    if (tooltipCtl.tooltip) {
      // 处理视图放大后再关闭 tooltip 的 dom 被清除
      const container = tooltipCtl.tooltip.cfg.container
      // 当下拉菜单不显示时，移除tooltip的hidden-tooltip样式
      if (viewTrackBarElement?.getAttribute('aria-expanded') === 'false') {
        container.classList.toggle('hidden-tooltip', false)
      }
      container.style.display = 'block'
      const dom = document.getElementById(container.id)
      if (!dom) {
        const full = document.getElementsByClassName('fullscreen')
        if (full.length) {
          full.item(0).appendChild(container)
        } else {
          const wrapperDom = document.getElementById(G2_TOOLTIP_WRAPPER)
          wrapperDom.appendChild(container)
        }
      }
    }
    plot.chart.getOptions().tooltip.follow = false
    tooltipCtl.title = Math.random().toString()
    // 当显示提示为事件触发时，使用event的client坐标，否则使用tooltipCtl.point 数据点的位置，在图表中，需要加上图表在绘制区的位置
    const { x, y } = calculateTooltipPosition(
      chart,
      isCarousel,
      tooltipCtl,
      chartElement,
      event,
      enlargeElement
    )
    plot.chart.getTheme().components.tooltip.x = x
    plot.chart.getTheme().components.tooltip.y = y
  })
  // https://github.com/antvis/G2/blob/master/src/chart/controller/tooltip.ts#hideTooltip
  plot.on('plot:leave', () => {
    const tooltipCtl = plot.chart.getController('tooltip')
    if (!tooltipCtl) {
      return
    }
    plot.chart.getOptions().tooltip.follow = true
    const container = tooltipCtl.tooltip?.cfg?.container
    if (container) {
      container.style.display = 'none'
    }
    tooltipCtl.hideTooltip()
  })
  // 移动端处理，关闭其他图表的提示
  plot.on('plot:touchstart', () => {
    const wrapperDom = document.getElementById(G2_TOOLTIP_WRAPPER)
    if (wrapperDom) {
      const tooltipCtl = plot.chart.getController('tooltip')
      if (!tooltipCtl) {
        return
      }
      const container = tooltipCtl.tooltip?.cfg.container
      for (const ele of wrapperDom.children) {
        if (!container || container.id !== ele.id) {
          ele.style.display = 'none'
        }
      }
    }
  })
  plot.on('tooltip:hidden', () => {
    const tooltipCtl = plot.chart.getController('tooltip')
    if (!tooltipCtl) {
      return
    }
    const container = tooltipCtl.tooltip?.cfg.container
    container && (container.style.display = 'none')
  })
}

export const TOOLTIP_TPL =
  '<li class="g2-tooltip-list-item" data-index={index}>' +
  '<span class="g2-tooltip-marker" style="background:{color}"></span>' +
  '<span class="g2-tooltip-name">{name}</span>:' +
  '<span class="g2-tooltip-value">{value}</span>' +
  '</li>'

export function getConditions(chart: Chart) {
  const { threshold } = parseJson(chart.senior)
  const annotations = []
  if (!threshold.enable || chart.type === 'area-stack' || chart.type === 'symbolic-map')
    return annotations
  const conditions = threshold.lineThreshold ?? []
  const yAxisIds = chart.yAxis.map(i => i.id)
  for (const field of conditions) {
    if (!yAxisIds.includes(field.fieldId)) {
      continue
    }
    for (const t of field.conditions) {
      const annotation = {
        type: 'regionFilter',
        start: ['start', 'median'],
        end: ['end', 'min'],
        color: t.color
      }
      // 加中线
      const annotationLine = {
        type: 'line',
        start: ['start', t.value],
        end: ['end', t.value],
        style: {
          stroke: t.color,
          lineDash: [2, 2]
        }
      }
      if (t.term === 'between') {
        annotation.start = ['start', parseFloat(t.min)]
        annotation.end = ['end', parseFloat(t.max)]
        annotationLine.start = ['start', parseFloat(t.min)]
        annotationLine.end = ['end', parseFloat(t.min)]
        annotations.push(JSON.parse(JSON.stringify(annotationLine)))
        annotationLine.start = ['start', parseFloat(t.max)]
        annotationLine.end = ['end', parseFloat(t.max)]
        annotations.push(annotationLine)
      } else if (['lt', 'le'].includes(t.term)) {
        annotation.start = ['start', t.value]
        annotation.end = ['end', 'min']
        annotations.push(annotationLine)
      } else if (['gt', 'ge'].includes(t.term)) {
        annotation.start = ['start', t.value]
        annotation.end = ['end', 'max']
        annotations.push(annotationLine)
      }
      annotations.push(annotation)
    }
  }
  return annotations
}
const AXIS_LABEL_TOOLTIP_STYLE = {
  transition:
    'left 0.4s cubic-bezier(0.23, 1, 0.32, 1) 0s, top 0.4s cubic-bezier(0.23, 1, 0.32, 1) 0s',
  backgroundColor: 'rgb(255, 255, 255)',
  boxShadow: 'rgb(174, 174, 174) 0px 0px 10px',
  borderRadius: '3px',
  padding: '8px 12px',
  opacity: '0.95',
  position: 'absolute',
  visibility: 'visible'
}
const AXIS_LABEL_TOOLTIP_TPL =
  '<div class="g2-axis-label-tooltip">' + '<div class="g2-tooltip-title">{title}</div>' + '</div>'
export function configAxisLabelLengthLimit(chart, plot, triggerObjName) {
  // 设置触发事件的名称，如果未传入，则默认为 'axis-label'
  const triggerName = triggerObjName || 'axis-label'

  // 判断是否是Y轴标题
  const isYaxisTitle = triggerName === 'axis-title'

  // 解析图表的自定义样式和属性
  const { customStyle, customAttr } = parseJson(chart)
  const { lengthLimit, fontSize, color, show } = customStyle.yAxis.axisLabel
  const { tooltip } = customAttr

  // 如果不是标题，判断没有设置长度限制、没有显示或Y轴不显示，或图表类型为双向条形图，则不执行后续操作
  if (
    !isYaxisTitle &&
    (!lengthLimit || !show || !customStyle.yAxis.show || chart.type === 'bidirectional-bar')
  )
    return

  // 鼠标进入事件
  plot.on(triggerName + ':mouseenter', e => {
    const field = e.target.cfg.delegateObject.component.cfg.field
    const position = e.target.cfg.delegateObject.component.cfg.position
    const isYaxis = position === 'left' || position === 'right'

    // 如果不是 'field' 或 'title'，且不是Y轴，直接返回
    if (field !== 'field' && field !== 'title' && !isYaxis) return

    // 获取轴标签的实际内容
    const realContent = e.target.attrs.text

    // 不是标题时，判断标签长度小于限制或已经省略（以'...'结尾），则不显示 tooltip
    if (
      isYaxisTitle ? false : realContent.length < lengthLimit || !(realContent.slice(-3) === '...')
    )
      return

    // 获取当前鼠标事件的坐标
    const { x, y } = e
    const parentNode = e.event.target.parentNode

    // 获取父节点中是否已有 tooltip
    let labelTooltipDom = parentNode.getElementsByClassName('g2-axis-label-tooltip')[0]

    // 获取轴的标题
    const title =
      e.target.cfg.delegateObject.item?.name ||
      e.target.cfg.delegateObject.axis.cfg.title.originalText

    // 如果没有 tooltip，创建新的 tooltip DOM 元素
    if (!labelTooltipDom) {
      const domStr = substitute(AXIS_LABEL_TOOLTIP_TPL, { title })
      labelTooltipDom = createDom(domStr)

      // 设置 tooltip 的样式
      AXIS_LABEL_TOOLTIP_STYLE.backgroundColor = tooltip.backgroundColor
      AXIS_LABEL_TOOLTIP_STYLE.boxShadow = `${tooltip.backgroundColor} 0px 0px 5px`
      AXIS_LABEL_TOOLTIP_STYLE.maxWidth = '200px'
      assign(labelTooltipDom.style, AXIS_LABEL_TOOLTIP_STYLE)

      // 将 tooltip 添加到父节点
      parentNode.appendChild(labelTooltipDom)
    } else {
      // 如果已有 tooltip，更新其标题并使其可见
      labelTooltipDom.getElementsByClassName('g2-tooltip-title')[0].innerHTML = title
      labelTooltipDom.style.visibility = 'visible'
    }

    // 获取父节点的尺寸和 tooltip 的尺寸
    const { height, width } = parentNode.getBoundingClientRect()
    const { offsetHeight, offsetWidth } = labelTooltipDom

    // 如果 tooltip 的尺寸超出了父节点的尺寸，则将其位置重置为 (0, 0)
    if (offsetHeight > height || offsetWidth > width) {
      labelTooltipDom.style.left = labelTooltipDom.style.top = '0px'
      return
    }

    // 计算 tooltip 的初始位置
    const initPosition = { left: x + 10, top: y + 15 }

    // 调整位置，避免 tooltip 超出边界
    if (initPosition.left + offsetWidth > width) initPosition.left = width - offsetWidth - 10
    if (initPosition.top + offsetHeight > height) initPosition.top -= offsetHeight + 15

    // 设置 tooltip 的位置和样式
    labelTooltipDom.style.left = `${initPosition.left}px`
    labelTooltipDom.style.top = `${initPosition.top}px`
    labelTooltipDom.style.color = color
    labelTooltipDom.style.fontSize = `${fontSize}px`
  })

  // 鼠标离开事件
  plot.on(triggerName + ':mouseleave', e => {
    const field = e.target.cfg.delegateObject.component.cfg.field
    const position = e.target.cfg.delegateObject.component.cfg.position
    const isYaxis = position === 'left' || position === 'right'

    // 如果不是 'field' 或 'title'，且不是Y轴，直接返回
    if (field !== 'field' && field !== 'title' && !isYaxis) return

    // 获取轴标签的实际内容
    const realContent = e.target.attrs.text

    // 如果标签长度小于限制或已经省略（以'...'结尾），则不显示 tooltip
    if (
      isYaxisTitle ? false : realContent.length < lengthLimit || !(realContent.slice(-3) === '...')
    )
      return

    // 获取父节点中的 tooltip
    const parentNode = e.event.target.parentNode
    const labelTooltipDom = parentNode.getElementsByClassName('g2-axis-label-tooltip')[0]

    // 如果 tooltip 存在，隐藏它
    if (labelTooltipDom) labelTooltipDom.style.visibility = 'hidden'
  })
}

export function configXAxisLengthLimit(
  chart: Chart,
  chartObj: any,
  formatOriginText?: (originText: string, event: any) => string
): void {
  configAxisLengthLimit(chart, chartObj, 'xAxis', formatOriginText)
}

type AxisTooltipOptions = {
  maxWidth?: string
  wordBreak?: string
  transition?: boolean
}

const getAxisTooltipDom = (
  tooltipId: string,
  tooltip: Record<string, any>,
  options: AxisTooltipOptions = {}
) => {
  let parentDom = document.getElementById('G2-TOOLTIP-WRAPPER')
  if (!parentDom) {
    parentDom = document.createElement('div')
    parentDom.id = 'G2-TOOLTIP-WRAPPER'
    parentDom.style.position = 'absolute'
    parentDom.style.pointerEvents = 'none'
    parentDom.style.zIndex = '9999'
    document.body.appendChild(parentDom)
  }

  let tooltipDom = document.getElementById(tooltipId)
  if (!tooltipDom) {
    tooltipDom = document.createElement('div')
    tooltipDom.id = tooltipId
    tooltipDom.className = 'g2-axis-label-tooltip'
    parentDom.appendChild(tooltipDom)
  }
  Object.assign(tooltipDom.style, {
    position: 'fixed',
    display: 'none',
    color: tooltip.color ?? '#333333',
    backgroundColor: tooltip.backgroundColor ?? '#ffffff',
    fontSize: `${tooltip.fontSize ?? 12}px`,
    padding: '4px 8px',
    borderRadius: '4px',
    maxWidth: options.maxWidth ?? '',
    wordBreak: options.wordBreak ?? '',
    cursor: 'default',
    pointerEvents: 'none',
    boxShadow: 'rgba(0, 0, 0, 0.1) 0px 4px 8px 0px',
    transition: options.transition
      ? 'left 0.4s cubic-bezier(0.23, 1, 0.32, 1), top 0.4s cubic-bezier(0.23, 1, 0.32, 1)'
      : ''
  })
  return tooltipDom
}

const hideAxisTooltip = (tooltipId: string) => {
  const tooltipDom = document.getElementById(tooltipId)
  if (tooltipDom) {
    tooltipDom.style.display = 'none'
  }
}

const showAxisTooltip = (tooltipDom: HTMLElement, text: string, event: any) => {
  tooltipDom.innerText = text
  tooltipDom.style.display = 'block'

  const { width, height } = tooltipDom.getBoundingClientRect()
  const clientX = event.client?.x ?? event.x ?? 0
  const clientY = event.client?.y ?? event.y ?? 0
  const gap = 10
  let left = clientX + gap
  let top = clientY + gap
  if (left + width > window.innerWidth) {
    left = Math.max(gap, clientX - width - gap)
  }
  if (top + height > window.innerHeight) {
    top = Math.max(gap, clientY - height - gap)
  }
  tooltipDom.style.left = `${left}px`
  tooltipDom.style.top = `${top}px`
}

const hasAxisTitleSafeMargin = (spec: any): boolean => {
  const axes = spec?.axis
  if (
    axes &&
    typeof axes === 'object' &&
    Object.values(axes).some((axis: any) => axis?.dataeaseAxisTitleSafeMargin === true)
  ) {
    return true
  }
  return Array.isArray(spec?.children) && spec.children.some(hasAxisTitleSafeMargin)
}

/**
 * 纵轴标题被 G2 截断时，悬浮展示完整标题。
 * 仅带 dataeaseAxisTitleSafeMargin 的左轴会在布局阶段产生截断，其它轴标题不受影响。
 */
export function configAxisTitleOverflowTooltip(chart: Chart, chartObj: any): void {
  if (!chartObj) {
    return
  }
  // 只有启用安全边距的目标图表才注册轴标题事件，避免给全部 G2 实例增加监听器
  if (!hasAxisTitleSafeMargin(chartObj.options?.())) {
    return
  }
  const { tooltip = {} } = parseJson(chart.customAttr)
  const tooltipId = `AXIS_TITLE_TIP-${chart.container || chart.id || 'default'}`
  const hideTooltip = () => hideAxisTooltip(tooltipId)

  chartObj.on(`axis-title:${ChartEvent.POINTER_MOVE}`, event => {
    const target = event.target
    const originalValue = target?.attributes?.dataeaseOriginalText
    if (originalValue === undefined || originalValue === null) {
      hideTooltip()
      return
    }
    const overflowing =
      typeof target?.isOverflowing === 'function'
        ? target.isOverflowing()
        : target?.parsedStyle?.isOverflowing === true
    if (!overflowing) {
      hideTooltip()
      return
    }

    const originalText = String(originalValue)
    if (!originalText) {
      hideTooltip()
      return
    }

    showAxisTooltip(
      getAxisTooltipDom(tooltipId, tooltip, {
        maxWidth: '200px',
        wordBreak: 'break-all'
      }),
      originalText,
      event
    )
  })

  chartObj.on(`axis-title:${ChartEvent.POINTER_OUT}`, hideTooltip)
}

export function configAxisLengthLimit(
  chart: Chart,
  chartObj: any,
  axisType = 'xAxis',
  formatOriginText?: (originText: string, event: any) => string
): void {
  const axis = parseJson(chart.customStyle)?.[axisType]
  if (!axis?.show || !axis.axisLabel?.show || !axis.axisLabel.lengthLimit) {
    return
  }
  let hideTimer: ReturnType<typeof setTimeout>
  const { tooltip = {} } = parseJson(chart.customAttr)
  const tooltipId = `AXIS_LABEL_TIP-${chart.container || chart.id || 'default'}-${axisType}`

  const getOriginText = (event: any) => {
    const originTextRaw = event.target?.attributes?.originValue
    if (originTextRaw === undefined || originTextRaw === null) {
      return ''
    }
    return String(
      (formatOriginText ? formatOriginText(String(originTextRaw), event) : originTextRaw) ?? ''
    )
  }

  const hideTooltip = () => hideAxisTooltip(tooltipId)

  chartObj?.on(`axis-label-item:${ChartEvent.POINTER_MOVE}`, event => {
    const showText = event.target?.attributes?.text
    if (!showText?.endsWith('...')) {
      hideTooltip()
      return
    }
    const originText = getOriginText(event)
    if (!originText || originText === showText) {
      hideTooltip()
      return
    }
    if (hideTimer) {
      clearTimeout(hideTimer)
    }
    showAxisTooltip(getAxisTooltipDom(tooltipId, tooltip, { transition: true }), originText, event)
  })

  chartObj?.on(`axis-label-item:${ChartEvent.POINTER_OUT}`, () => {
    hideTimer = setTimeout(hideTooltip, 200)
  })
}

export function formatAxisLabelWithLengthLimit(value: unknown, lengthLimit?: number) {
  const label = value === null || value === undefined ? '' : `${value}`
  if (lengthLimit && label.length > lengthLimit) {
    return new Text({
      style: {
        text: label.substring(0, lengthLimit) + '...',
        originValue: label
      }
    })
  }
  return label
}

/**
 * y轴标题截取
 * @param chart
 * @param plot
 */
export function configYaxisTitleLengthLimit(chart, plot) {
  // 监听图表渲染前事件
  plot.on('beforerender', ev => {
    // 获取图表的Y轴自定义样式
    const { yAxis } = parseJson(chart.customStyle)

    // 计算最大可用空间高度，80% 为最大高度比
    const maxHeightRatio =
      0.8 * (ev.view.canvas.cfg.height - (ev.view.canvas.cfg.height < 120 ? 60 : 30))

    // 计算Y轴标题的每行高度
    const titleHeight = measureText(
      chart,
      yAxis.name,
      { fontSize: yAxis.fontSize, fontFamily: chart.fontFamily },
      'height'
    )

    // 用于存储截取后的标题
    let wrappedTitle = ''

    // 循环截取标题内容，直到超过最大高度
    for (
      let charIndex = 0;
      charIndex < yAxis.name.length && (charIndex + 1) * titleHeight <= maxHeightRatio;
      charIndex++
    ) {
      wrappedTitle += yAxis.name[charIndex]
    }

    // 如果标题被截断，添加省略号
    if (yAxis.name.length > wrappedTitle.length) {
      wrappedTitle =
        wrappedTitle.length > 2
          ? wrappedTitle.slice(0, wrappedTitle.length - 2) + '...'
          : wrappedTitle + '...'
    }
    // 更新Y轴标题的原始文本和截断后的文本
    const { title } = ev.view.options.axes.yAxisExt
    if (title) {
      title.originalText = yAxis.name
      title.text = wrappedTitle
    }
  })
}

/**
 * 调整原始数据options.data
 * 添加conditionColor字段，用于保存符合条件的颜色
 * conditionColor 为数组，多个指标多个颜色，按照指标的顺序
 * @param chart
 * @param options
 */
export const addConditionsStyleColorToData = (chart: Chart, options) => {
  const { threshold } = parseJson(chart.senior)
  if (!threshold.enable) return options
  options.data.forEach(item => {
    item['conditionColor'] = []
    // 条形图的值字段是xField，柱形图的值字段是yField
    const valueField = chart.type === 'bar-horizontal' ? options.xField : options.yField
    // 对称条形图区分左右值，value、 valueExt,quotaList只有一个
    if (chart.type === 'bidirectional-bar') {
      valueField.forEach(value => {
        const quotaList = value === 'value' ? chart.yAxis : chart.yAxisExt
        const conditionColor = getColorByConditions([quotaList[0]?.id], item[value], chart)
        if (conditionColor) {
          item[item[options.xField] + '-' + value] = conditionColor
        }
      })
    } else if (item.quotaList?.length) {
      const quotaList = item.quotaList.map(q => q.id) ?? []
      quotaList.forEach((q, index) => {
        // 定义后，在 handleConditionsStyle 函数中使用
        let currentValue = item[valueField]
        if (chart.type === 'progress-bar') {
          currentValue = item['originalValue']
        }
        const cColor = getColorByConditions([q], currentValue, chart)
        if (cColor) {
          item.conditionColor.push(cColor)
        } else {
          item.conditionColor = undefined
        }
      })
    }
  })
  return options
}

/**
 * 辅助函数：获取颜色, 根据条件以及值计算
 * @param quotaList 指标列表
 * @param values 值
 */
const getColorByConditions = (quotaList: [], values: number | number[], chart) => {
  const { threshold } = parseJson(chart.senior)
  const { basicStyle } = parseJson(chart.customAttr)
  const currentValue = Array.isArray(values) ? values[1] - values[0] : values
  if (!currentValue) return undefined
  // 同样的指标只取最后一个
  const conditionMap = new Map()
  for (const condition of threshold.lineThreshold ?? []) {
    conditionMap.set(condition.fieldId, condition)
  }
  for (const condition of conditionMap.values()) {
    if (chart.type === 'progress-bar' && chart.yAxisExt?.[0]?.id !== quotaList[0]) continue
    if (!quotaList.includes(condition.fieldId) && chart.type !== 'waterfall') continue
    for (const tc of condition.conditions) {
      if (
        (tc.term === 'between' && currentValue >= tc.min && currentValue <= tc.max) ||
        (tc.term === 'lt' && currentValue < tc.value) ||
        (tc.term === 'le' && currentValue <= tc.value) ||
        (tc.term === 'gt' && currentValue > tc.value) ||
        (tc.term === 'ge' && currentValue >= tc.value)
      ) {
        let tmpColor = hexToRgba(tc.color, basicStyle.alpha)
        if (basicStyle.gradient) {
          let vhAngle = ['bar-horizontal', 'progress-bar'].includes(chart.type) ? 0 : 270
          if (chart.type === 'bidirectional-bar') {
            const yAxis = chart.yAxis.find(item => item.id === condition.fieldId)
            vhAngle = getBidirectionalAngle(basicStyle, yAxis ? 0 : 1)
          }
          tmpColor = setGradientColor(tmpColor, true, vhAngle)
        }
        return tmpColor
      }
    }
  }
}

/**
 * 处理柱条图的样式
 * 柱条的颜色
 * 提示marker的颜色
 * 注: 原始options中tooltip已经配置了customItems,这里将会忽略
 * @param chart
 * @param options
 */
export function handleConditionsStyle(chart: Chart, options) {
  const { threshold } = parseJson(chart.senior)
  if (!threshold.enable) return options
  const { basicStyle } = parseJson(chart.customAttr)
  // 该字段出处 addConditionsStyleColorToData
  const colorField = 'conditionColor'
  // 配置条件样式的颜色字段
  const rawFields = options.rawFields || []
  rawFields.push(colorField)
  // 辅助函数：配置柱条样式颜色，条形图为barStyle,柱形图为columnStyle
  const columnStyle = data => {
    return {
      ...(data[colorField]?.[0] ? { fill: data[colorField][0] } : {})
    }
  }
  let newColor = undefined
  if (chart.type === 'bidirectional-bar') {
    rawFields.push(options.xField)
    newColor = getBidirectionalBarColor(chart, basicStyle, options)
  } else if (chart.type === 'waterfall') {
    newColor = getWaterfallColor(basicStyle, chart)
  }
  const tmpOption = {
    ...options,
    rawFields,
    ...configRoundAngle(chart, 'columnStyle', columnStyle),
    ...configRoundAngle(chart, 'barStyle', columnStyle),
    tooltip: {
      ...options.tooltip,
      ...(options.tooltip['customItems']
        ? {}
        : {
            customItems: originalItems => {
              originalItems.forEach(item => {
                if (item.data?.[colorField]) {
                  item.color = item.data[colorField][0]
                }
              })
              return originalItems
            }
          })
    },
    ...(newColor ? { color: newColor } : {})
  }
  return tmpOption
}

/**
 * 配置瀑布图的color
 * 瀑布color,这个图表固定为基础样式中颜色的前三个颜色，第一个为增加，第二个为减少，第三个为总计
 * @param basicStyle
 * @param chart
 */
const getWaterfallColor = (basicStyle, chart) => {
  const waterfallBasicColors = getBasicColors(chart, basicStyle, 270)
  return data => {
    if (data['$$isTotal$$']) return waterfallBasicColors[2]
    const values = data['$$yField$$']
    const newColor = getColorByConditions([], values, chart)
    return newColor ?? (values[1] > values[0] ? waterfallBasicColors[0] : waterfallBasicColors[1])
  }
}

/**
 * 配置对称条形图的color
 * @param basicStyle
 * @param options
 */
const getBidirectionalBarColor = (chart, basicStyle, options) => {
  const basicColors = getBasicColors(chart, basicStyle, 270)
  return ref => {
    const obj = options.data.find(item => item[ref[options.xField] + '-' + ref['series-field-key']])
    if (obj) {
      return obj[ref[options.xField] + '-' + ref['series-field-key']]
    }
    return ref['series-field-key'] === 'value' ? basicColors[0] : basicColors[1]
  }
}

/**
 * 获取基础颜色
 * @param chart
 * @param basicStyle
 * @param angle
 */
const getBasicColors = (chart, basicStyle, angle) => {
  const baseColors = []
  basicStyle.colors?.forEach((color, index) => {
    if (chart.type === 'bidirectional-bar') {
      baseColors.push(
        setGradientColor(
          hexToRgba(color, basicStyle.alpha),
          true,
          getBidirectionalAngle(basicStyle, index)
        )
      )
    } else {
      baseColors.push(setGradientColor(hexToRgba(color, basicStyle.alpha), true, angle))
    }
  })
  return basicStyle.gradient ? baseColors : basicStyle.colors
}

/**
 * 获取对称条形图颜色的渐变角度
 * @param basicStyle
 * @param index
 */
const getBidirectionalAngle = (basicStyle, index) => {
  let vhAngle = 180 - index * 180
  if (basicStyle.layout === 'vertical') {
    vhAngle = index === 0 ? 280 : 90
  }
  return vhAngle
}

/**
 * tooltip验证条件样式中的颜色，有就使用，否则使用原始颜色
 * @param item
 */
export const getTooltipItemConditionColor = item => {
  let color = item.color
  if (item.data?.['conditionColor']) {
    color = item.data['conditionColor'][0]
  }
  return color
}

/**
 * 配置空数据样式
 * @param newChart
 * @param newData
 * @param container
 */
export const configEmptyDataStyle = (newData, container, newChart?, content?) => {
  /**
   * 辅助函数：移除空数据dom
   */
  const removeEmptyDom = () => {
    const emptyElement = document.getElementById(container + '_empty')
    if (emptyElement) {
      emptyElement.parentElement.removeChild(emptyElement)
    }
  }
  removeEmptyDom()
  if (newData?.length > 0) return
  if (!newData?.length) {
    const emptyDom = document.createElement('div')
    emptyDom.id = container + '_empty'
    emptyDom.textContent = content || tI18n('data_set.no_data')
    emptyDom.setAttribute(
      'style',
      `position: absolute;
        left: 50%;
        top: 50%;
        transform: translate(-50%, -50%);
        color: darkgray;
        textAlign: center;`
    )
    const parent = document.getElementById(container)
    parent.insertBefore(emptyDom, parent.firstChild)
    newChart?.destroy()
  }
}

export const numberToChineseUnderHundred = (num: number): string => {
  // 合法性检查
  if (num <= 0 || num > 99 || !Number.isInteger(num)) {
    throw new Error('请输入1-99之间的整数')
  }

  const digits = ['', '一', '二', '三', '四', '五', '六', '七', '八', '九']

  // 处理个位数
  if (num < 10) return digits[num]

  const tens = Math.floor(num / 10)
  const ones = num % 10

  // 处理整十
  if (ones === 0) {
    return tens === 1 ? '十' : digits[tens] + '十'
  }

  // 处理其他两位数
  return tens === 1 ? '十' + digits[ones] : digits[tens] + '十' + digits[ones]
}

/**
 * 配置柱条图的圆角
 * @param styleName
 * @param callBack 自定义其他属性函数
 */
export const configRoundAngle = (chart: Chart, styleName: string, callBack?: (datum) => {}) => {
  const { basicStyle } = parseJson(chart.customAttr)
  if (['roundAngle', 'topRoundAngle'].includes(basicStyle.radiusColumnBar)) {
    const radius = Array(2).fill(basicStyle.columnBarRightAngleRadius)
    const topRadius = [0, 0, ...radius]
    const bottomRadius = [...radius, 0, 0]
    const finalRadius = [...radius, ...radius]
    if (chart.type.includes('-stack')) {
      return {
        [styleName]: datum => {
          if (!datum.value) return { radius: [], ...(callBack ? callBack(datum) : {}) }
          return { radius: finalRadius, ...(callBack ? callBack(datum) : {}) }
        }
      }
    }
    const isTopRound = basicStyle.radiusColumnBar === 'topRoundAngle'
    // 对称条形图
    if (chart.type === 'bidirectional-bar') {
      const valueField = basicStyle.layout === 'vertical' ? 'valueExt' : 'value'
      return {
        [styleName]: datum => ({
          radius: datum[valueField] && isTopRound ? topRadius : isTopRound ? radius : finalRadius,
          ...(callBack ? callBack(datum) : {})
        })
      }
    }
    // 进度条
    if (chart.type === 'progress-bar') {
      return {
        [styleName]: datum => {
          return {
            radius: isTopRound ? bottomRadius : finalRadius,
            ...(callBack ? callBack(datum) : {})
          }
        }
      }
    }
    // 区间条形图
    if (chart.type === 'bar-range') {
      return {
        [styleName]: datum => {
          return {
            radius:
              datum?.values[0] < datum?.values[1]
                ? isTopRound
                  ? bottomRadius
                  : finalRadius
                : isTopRound
                ? topRadius
                : finalRadius,
            ...(callBack ? callBack(datum) : {})
          }
        }
      }
    }
    // 堆叠条形图、百分比条形图第一个和最后一个反转
    const isStackHorizontalBar = [
      'bar-stack-horizontal',
      'percentage-bar-stack-horizontal'
    ].includes(chart.type)
    // 配置柱条样式
    const style = datum => {
      if (isTopRound && datum.isFirst && datum.isLast) {
        return { radius, ...(callBack ? callBack(datum) : {}) }
      }
      if (!isTopRound && datum.isFirst && datum.isLast) {
        return { radius: finalRadius, ...(callBack ? callBack(datum) : {}) }
      }
      if (isStackHorizontalBar) {
        if (datum.isLast || (!isTopRound && datum.isFirst)) {
          return {
            radius: datum.isFirst ? topRadius : radius,
            ...(callBack ? callBack(datum) : {})
          }
        }
      } else if (datum.isFirst || (!isTopRound && datum.isLast)) {
        return {
          radius: datum.isLast ? topRadius : radius,
          ...(callBack ? callBack(datum) : {})
        }
      }
    }
    return {
      [styleName]: style
    }
  }
  return {
    [styleName]: datum => {
      return { ...(callBack ? callBack(datum) : {}) }
    }
  }
}

/**
 * 为圆角组装options.data，
 * 添加 isFirst 和 isLast 属性
 * @param data
 * @param isGroup
 * @param isStack
 */
export const assembleOptionsDataForRoundAngle = (
  data: Record<string, any>[],
  isGroup: boolean,
  isStack?: boolean
) => {
  // column数据分组
  const groupedByField = data.reduce((acc, item) => {
    let groupField = item.field
    if (isGroup || isStack) {
      groupField = `${item.field}-${isStack ? item.group : item.category}`
    }
    if (!acc[groupField]) {
      acc[groupField] = []
    }
    acc[groupField].push(item)
    return acc
  }, {})
  // 遍历每个分组，添加 isFirst 和 isLast 属性
  Object.values(groupedByField).forEach(group => {
    const firstItem = group[0]
    const lastItem = group[group.length - 1]
    firstItem.isFirst = true
    lastItem.isLast = true
  })
  // 将分组后的数据重新展开为一个数组
  return Object.values(groupedByField).flat()
}

interface ObjectType<T> {
  [key: string]: T
}

export function substitute<T>(str: string, o: ObjectType<T>) {
  if (!str || !o) {
    return str
  }
  return str.replace(/\\?\{([^{}]+)\}/g, (match, name): any => {
    if (match.charAt(0) === '\\') {
      return match.slice(1)
    }
    return o[name] === undefined ? '' : o[name]
  })
}

export const TOOLTIP_ITEM_TPL = `
        <li class="g2-tooltip-list-item" data-index="0" style="list-style-type: none; display: flex; line-height: 2em; align-items: center; justify-content: space-between; white-space: nowrap;">
          <span class="g2-tooltip-list-item-name" style="display: flex; align-items: center; max-width: 216px;">
            <span class="g2-tooltip-list-item-marker" style="background: {marker}; width: 8px; height: 8px; border-radius: 50%; display: inline-block; margin-right: 4px;"></span>
            <span class="g2-tooltip-list-item-name-label" title="value" style="flex: 1 1 0%; overflow: hidden; white-space: nowrap; text-overflow: ellipsis;">{label}</span>
          </span>
          <span class="g2-tooltip-list-item-value" style="display: inline-block; float: right; flex: 1 1 0%; text-align: right; min-width: 28px; margin-left: 30px; color: rgba(0, 0, 0, 0.85); overflow: hidden; white-space: nowrap; text-overflow: ellipsis;">{value}</span>
        </li>
`
export const TOOLTIP_TITLE_TPL = `<div class="g2-tooltip-title" style="color: rgba(0, 0, 0, 0.45); overflow: hidden; white-space: nowrap; text-overflow: ellipsis;">{title}</div>`

/**
 * 辅助函数：隐藏子组件的文本标签
 * 包含 图例、坐标轴标签、坐标轴标题、数据标签、缩略轴
 * @param child
 */
function hideChildrenLabels(child) {
  child.labels?.length && (child.labels = [])
  ;['x', 'y'].forEach(
    axis => child.axis?.[axis] && Object.assign(child.axis[axis], { label: false, title: false })
  )
  child.legend && (child.legend = false)
  child.slider && Object.assign(child.slider, { x: false, y: false })
  child.tooltip = false
}

/**
 * 处理图表隐藏时的图表配置项
 * 当隐藏图表示，对应的图表文本配置项也隐藏
 * 包括 图例、坐标轴标签、坐标轴标题、数据标签、缩略轴
 * @param chart
 * @param options
 */
export function handleChartDashboardHidden(chart: Chart, options) {
  if (!chart.dashboardHidden) return
  const { type } = chart
  const hasChildren = options.children && options.children.length > 0
  // 辅助函数：批量隐藏 legend 和 axis
  const hideLegendAndAxis = opt => {
    opt.legend = false
    opt.axis?.x && Object.assign(opt.axis.x, { label: false, title: false })
    opt.axis?.y && Object.assign(opt.axis.y, { label: false, title: false })
  }
  if (hasChildren && type !== 'gauge' && type !== 'liquid') {
    switch (type) {
      case 'stock-line':
        hideLegendAndAxis(options)
        options.children?.[1] && (options.children[1].slider = false)
        break
      case 'bullet-graph':
        hideLegendAndAxis(options)
        options.children?.[1] && (options.children[1].labels = [])
        break
      default:
        if (type.indexOf('-mix') > -1) {
          if (options.type === 'view') {
            hideLegendAndAxis(options)
            options.children?.forEach(hideChildrenLabels)
          } else {
            options.children.forEach(child => {
              if (child.type === 'view') {
                hideLegendAndAxis(child)
                child.children?.forEach(hideChildrenLabels)
              } else {
                child.scale ? (child.scale.color = false) : (child.color = false)
              }
            })
          }
        } else {
          if (options.type === 'view') {
            options.legend = false
            const radar = type === 'radar'
            const axisOpt = radar
              ? { labelFormatter: () => '', title: false }
              : { label: false, title: false }
            options.axis?.x && Object.assign(options.axis.x, axisOpt)
            options.axis?.y && Object.assign(options.axis.y, axisOpt)
          }
          options.children.forEach(child =>
            type === 'bidirectional-bar'
              ? child.children?.forEach(c => hideChildrenLabels(c.value || c))
              : hideChildrenLabels(child)
          )
        }
    }
  } else {
    switch (type) {
      case 'gauge':
        const setGaugeStyle = c => {
          c.style.text = () => ''
          c.style.textContent = () => ''
          c.axis?.y && (c.axis.y.labelFormatter = () => '')
        }
        hasChildren ? options.children.forEach(setGaugeStyle) : setGaugeStyle(options)
        break
      case 'liquid':
        options.style.contentText = ''
        break
      case 'treemap':
      case 'sankey':
      case 'circle-packing':
        options.style.labelText = () => ''
        break
      default:
        if (type === 'funnel') options.paddingRight = 0
        hideChildrenLabels(options)
    }
  }
}

/**
 * 将渐变色字符串（如 l(270) 0:rgba(255,69,0,0.3) 1:rgba(255,69,0,1)）转换为 CSS 的 linear-gradient 语法
 * @param str
 */
export function toLinearGradient(str: string): string {
  // 匹配角度和颜色点
  const match = str.match(/^l\((\d+)\)\s*(.*)$/)
  if (!match) return str
  const angle = match[1] === '270' ? '360' : '90'
  const stops = match[2]
    .split(/\s+/)
    .map(s => {
      const [offset, color] = s.split(':')
      return `${color} ${parseFloat(offset) * 100}%`
    })
    .join(', ')
  return `linear-gradient(${angle}deg, ${stops})`
}
