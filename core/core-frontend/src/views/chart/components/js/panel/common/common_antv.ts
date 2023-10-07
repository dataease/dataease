import { hexColorToRGBA, parseJson } from '../../util'
import {
  DEFAULT_XAXIS_STYLE,
  DEFAULT_YAXIS_STYLE
} from '@/views/chart/components/editor/util/chart'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { AreaOptions, LabelOptions } from '@antv/l7plot'
import { TooltipOptions } from '@antv/l7plot/dist/lib/types/tooltip'
import { FeatureCollection } from '@antv/l7plot/dist/esm/plots/choropleth/types'
import { Datum } from '@antv/g2plot/esm/types/common'

export function getPadding(chart: Chart): number[] {
  if (chart.drill) {
    return [0, 10, 26, 10]
  } else {
    return [0, 10, 14, 10]
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
            background: tooltipBackgroundColor
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
              offsetY = -16
            } else {
              offsetY = -4
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
              offsetY = -22
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
            symbol: legendSymbol
          },
          radio: false
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
                  lineWidth: a.splitLine.lineStyle.width
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
        const rotate = a.axisLabel.rotate
        const label = a.axisLabel.show
          ? {
              rotate: (rotate * Math.PI) / 180,
              style: {
                fill: a.axisLabel.color,
                fontSize: a.axisLabel.fontSize,
                textAlign: rotate > 20 ? 'start' : rotate < -20 ? 'end' : 'center'
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
            lineWidth: yAxis.splitLine.lineStyle.width
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
  const label = yAxis.axisLabel.show
    ? {
        rotate: (yAxis.axisLabel.rotate * Math.PI) / 180,
        style: {
          fill: yAxis.axisLabel.color,
          fontSize: yAxis.axisLabel.fontSize
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
  if (senior.assistLine?.length > 0) {
    const customStyle = parseJson(chart.customStyle)
    let yAxisPosition, axisFormatterCfg
    if (customStyle.yAxis) {
      const a = JSON.parse(JSON.stringify(customStyle.yAxis))
      yAxisPosition = a.position
      axisFormatterCfg = a.axisLabelFormatter
        ? a.axisLabelFormatter
        : DEFAULT_YAXIS_STYLE.axisLabelFormatter
    }

    const fixedLines = senior.assistLine.filter(ele => ele.field === '0')
    const dynamicLineFields = senior.assistLine
      .filter(ele => ele.field === '1')
      .map(item => item.fieldId)
    const dynamicLines = chart.data.dynamicAssistLines?.filter(item =>
      dynamicLineFields?.includes(item.fieldId)
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
        position: [yAxisPosition === 'left' ? 'start' : 'end', value],
        content: content,
        offsetY: -2,
        offsetX: yAxisPosition === 'left' ? 2 : -10 * (content.length - 2),
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
  if (senior.assistLine?.length > 0) {
    const customStyle = parseJson(chart.customStyle)
    let xAxisPosition, axisFormatterCfg
    if (customStyle.xAxis) {
      const a = JSON.parse(JSON.stringify(customStyle.xAxis))
      xAxisPosition = transAxisPosition(a.position)
      axisFormatterCfg = a.axisLabelFormatter
        ? a.axisLabelFormatter
        : DEFAULT_XAXIS_STYLE.axisLabelFormatter
    }

    const fixedLines = senior.assistLine.filter(ele => ele.field === '0')
    const dynamicLineFields = senior.assistLine
      .filter(ele => ele.field === '1')
      .map(item => item.fieldId)
    const dynamicLines = chart.data.dynamicAssistLines?.filter(item =>
      dynamicLineFields?.includes(item.fieldId)
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

function getLineDash(type) {
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
