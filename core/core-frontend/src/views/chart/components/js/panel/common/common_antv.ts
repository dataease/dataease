import { hexColorToRGBA, parseJson } from '../../util'
import {
  DEFAULT_XAXIS_STYLE,
  DEFAULT_YAXIS_EXT_STYLE,
  DEFAULT_YAXIS_STYLE
} from '@/views/chart/components/editor/util/chart'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { AreaOptions, LabelOptions } from '@antv/l7plot'
import { TooltipOptions } from '@antv/l7plot/dist/lib/types/tooltip'
import { FeatureCollection } from '@antv/l7plot/dist/esm/plots/choropleth/types'
import { Datum } from '@antv/g2plot/esm/types/common'
import { Tooltip } from '@antv/g2plot/esm'
import { add } from 'mathjs'
import isEmpty from 'lodash-es/isEmpty'
import _ from 'lodash'
import type { LegendOptions } from '@antv/l7plot/dist/esm/types/legend'
import { CategoryLegendListItem } from '@antv/l7plot-component/dist/lib/types/legend'
import createDom from '@antv/dom-util/esm/create-dom'
import {
  CONTAINER_TPL,
  ITEM_TPL,
  LIST_CLASS
} from '@antv/l7plot-component/dist/esm/legend/category/constants'
import substitute from '@antv/util/esm/substitute'
import type { Plot as L7Plot, PlotOptions } from '@antv/l7plot/dist/esm'
import { Zoom } from '@antv/l7'
import { createL7Icon } from '@antv/l7-component/es/utils/icon'
import { DOM } from '@antv/l7-utils'
import { Scene } from '@antv/l7-scene'

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
            boxShadow: '0 4px 8px 0 rgba(0, 0, 0, 0.1)'
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
        label = {
          position: l.position,
          layout: [{ type: 'limit-in-canvas' }],
          style: {
            fill: l.color,
            fontSize: l.fontSize
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
          }
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
          result.push({ ...item, name, value })
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
    }
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
      const l = JSON.parse(JSON.stringify(customStyle.legend))
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
              offsetY = -6
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
            symbol: legendSymbol
          },
          itemHeight: l.fontSize + 4,
          radio: false,
          pageNavigator: {
            marker: {
              style: {
                fill: 'rgba(0,0,0,0.65)',
                stroke: 'rgba(192,192,192,0.52)'
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
          a.name && a.name !== ''
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
                lineWidth: axisCfg.lineStyle.width
              }
            }
          : null
        const tickLine = axisCfg.show
          ? {
              style: {
                stroke: axisCfg.lineStyle.color
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
                textAlign: textAlign
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
    yAxis.name && yAxis.name !== ''
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
          lineWidth: axisCfg.lineStyle.width
        }
      }
    : null
  const tickLine = axisCfg.show
    ? {
        style: {
          stroke: axisCfg.lineStyle.color
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
          textAlign
        }
      }
    : null

  axis = {
    position: yAxis.position,
    title,
    grid,
    label,
    line,
    tickLine
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
    yAxis.name && yAxis.name !== ''
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
          lineWidth: axisCfg.lineStyle.width
        }
      }
    : null
  const tickLine = axisCfg.show
    ? {
        style: {
          stroke: axisCfg.lineStyle.color
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
          textAlign
        }
      }
    : null

  axis = {
    position: yAxis.position,
    title,
    grid,
    label,
    line,
    tickLine
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
          fill: senior.functionCfg.sliderTextColor
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
    const quotaFields = _.filter(chart.yAxis, ele => ele.summary !== '' && ele.id !== '-1')
    const quotaExtFields = _.filter(chart.yAxisExt, ele => ele.summary !== '' && ele.id !== '-1')
    const dynamicLines = chart.data.dynamicAssistLines?.filter(item => {
      return (
        dynamicLineFields?.includes(item.fieldId) &&
        (!!_.find(quotaFields, d => d.id === item.fieldId) ||
          (!!_.find(quotaExtFields, d => d.id === item.fieldId) && chart.type === 'chart-mix'))
      )
    })
    const lines = fixedLines.concat(dynamicLines)
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
    const quotaFields = _.filter(chart.yAxis, ele => ele.summary !== '' && ele.id !== '-1')
    const dynamicLines = chart.data.dynamicAssistLines?.filter(
      item =>
        dynamicLineFields?.includes(item.fieldId) &&
        !!_.find(quotaFields, d => d.id === item.fieldId)
    )
    const lines = fixedLines.concat(dynamicLines)

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
        position: [xAxisPosition === 'left' ? 'start' : 'end', value],
        content: content,
        offsetY: xAxisPosition === 'left' ? -2 : -10 * (content.length - 2),
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
 */
export function setGradientColor(rawColor: string, show = false, angle = 0) {
  const item = rawColor.split(',')
  item.splice(3, 1, '0.3)')
  return show ? `l(${angle}) 0:${item.join(',')} 1:${rawColor}` : rawColor
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
  return {
    visible: label.show,
    style: {
      fill: label.color,
      fontSize: label.fontSize
    }
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
  const { yAxis } = chart
  return {
    customTitle(data) {
      return data.name
    },
    items: [
      {
        field: 'value',
        alias: yAxis?.[0]?.chartShowName ?? yAxis?.[0]?.name,
        customValue: value => {
          return valueFormatter(value, tooltip.tooltipFormatter)
        }
      }
    ],
    showComponent: tooltip.show,
    domStyles: {
      'l7plot-tooltip': {
        'background-color': tooltip.backgroundColor,
        'font-size': `${tooltip.fontSize}px`
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

export function handleGeoJson(geoJson: FeatureCollection, nameMapping?: Record<string, string>) {
  geoJson.features.forEach(item => {
    if (!item.properties['centroid'] && item.properties['center']) {
      item.properties['centroid'] = item.properties['center']
    }
    let name = item.properties['name']
    if (nameMapping?.[name]) {
      name = nameMapping[name]
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
class CustomZoom extends Zoom {
  resetButtonGroup(container) {
    DOM.clearChildren(container)
    this['zoomInButton'] = this['createButton'](
      this.controlOption.zoomInText,
      this.controlOption.zoomInTitle,
      'l7-button-control',
      container,
      this.zoomIn
    )
    const resetBtnIconText = createL7Icon('l7-icon-round')
    this['zoomResetButton'] = this['createButton'](
      resetBtnIconText,
      'Reset',
      'l7-button-control',
      container,
      () => {
        this.mapsService.setZoomAndCenter(
          this.controlOption['initZoom'],
          this.controlOption['center']
        )
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
    const { buttonColor, buttonBackground } = this.controlOption as any
    if (buttonColor) {
      const elements = [
        this.controlOption.zoomInText,
        this.controlOption.zoomOutText,
        resetBtnIconText
      ] as HTMLElement[]
      setStyle(elements, 'fill', buttonColor)
    }
    const elements = [this['zoomResetButton'], this['zoomInButton'], this['zoomOutButton']]
    if (buttonBackground) {
      setStyle(elements, 'background', buttonBackground)
    }
    setStyle(elements, 'border-bottom', 'none')
    this['updateDisabled']()
  }
}
export function configL7Zoom(chart: Chart, plot: L7Plot<PlotOptions> | Scene) {
  const { basicStyle } = parseJson(chart.customAttr)
  if (
    (basicStyle.suspension === false && basicStyle.showZoom === undefined) ||
    basicStyle.showZoom === false
  ) {
    return
  }
  const plotScene = plot instanceof Scene ? plot : plot.scene
  plotScene.once('loaded', () => {
    const zoomOptions = {
      initZoom: plotScene.getZoom(),
      center: plotScene.getCenter(),
      buttonColor: basicStyle.zoomButtonColor,
      buttonBackground: basicStyle.zoomBackground
    } as any
    plotScene.addControl(new CustomZoom(zoomOptions))
  })
}

function setStyle(elements: HTMLElement[], styleProp: string, value) {
  elements.forEach(e => {
    e.style[styleProp] = value
  })
}
