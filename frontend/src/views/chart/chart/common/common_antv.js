import { hexColorToRGBA } from '@/views/chart/chart/util'

export function getPadding(chart) {
  if (chart.drill) {
    return [0, 10, 26, 10]
  } else {
    return [0, 10, 14, 10]
  }
}
// color,label,tooltip,axis,legend,background
export function getTheme(chart) {
  const colors = []
  let bgColor, labelFontsize, labelColor, tooltipColor, tooltipFontsize, legendColor, legendFontsize
  let customAttr = {}
  if (chart.customAttr) {
    customAttr = JSON.parse(chart.customAttr)
    // color
    if (customAttr.color) {
      const c = JSON.parse(JSON.stringify(customAttr.color))
      c.colors.forEach(ele => {
        colors.push(hexColorToRGBA(ele, c.alpha))
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
      tooltipColor = t.textStyle.color
      tooltipFontsize = t.textStyle.fontSize
    }
  }

  let customStyle
  if (chart.customStyle) {
    customStyle = JSON.parse(chart.customStyle)
    // bg
    if (customStyle.background) {
      bgColor = hexColorToRGBA(customStyle.background.color, customStyle.background.alpha)
    }
    // legend
    if (customStyle.legend) {
      const l = JSON.parse(JSON.stringify(customStyle.legend))
      legendColor = l.textStyle.color
      legendFontsize = l.textStyle.fontSize
    }
  }

  return {
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
            fontSize: tooltipFontsize + 'px'
          }
        }
      },
      legend: {
        common: {
          itemName: {
            style: {
              fill: legendColor,
              fontSize: parseInt(legendFontsize)
            }
          }
        }
      }
    }
  }
}
// 通用label
export function getLabel(chart) {
  let label = {}
  let customAttr = {}
  if (chart.customAttr) {
    customAttr = JSON.parse(chart.customAttr)
    // label
    if (customAttr.label) {
      const l = JSON.parse(JSON.stringify(customAttr.label))
      if (l.show) {
        if (chart.type === 'pie') {
          label = {
            type: l.position,
            autoRotate: false
          }
        } else if (chart.type.includes('line')) {
          label = {
            position: l.position,
            offsetY: -8
          }
        } else {
          label = {
            position: l.position
          }
        }
        label.style = {
          fill: l.color,
          fontSize: parseInt(l.fontSize)
        }
      } else {
        label = false
      }
    }
  }
  return label
}
// 通用tooltip
export function getTooltip(chart) {
  let tooltip = {}
  let customAttr = {}
  if (chart.customAttr) {
    customAttr = JSON.parse(chart.customAttr)
    // tooltip
    if (customAttr.tooltip) {
      const t = JSON.parse(JSON.stringify(customAttr.tooltip))
      if (t.show) {
        tooltip = {}
      } else {
        tooltip = false
      }
    }
  }
  return tooltip
}
// 通用legend
export function getLegend(chart) {
  let legend = {}
  let customStyle
  if (chart.customStyle) {
    customStyle = JSON.parse(chart.customStyle)
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
export function getXAxis(chart) {
  let axis = {}
  let customStyle
  if (chart.customStyle) {
    customStyle = JSON.parse(chart.customStyle)
    // legend
    if (customStyle.xAxis) {
      const a = JSON.parse(JSON.stringify(customStyle.xAxis))
      if (a.show) {
        const title = (a.name && a.name !== '') ? {
          text: a.name,
          style: {
            fill: a.nameTextStyle.color,
            fontSize: parseInt(a.nameTextStyle.fontSize)
          },
          spacing: 8
        } : null
        const grid = a.splitLine.show ? {
          line: {
            style: {
              stroke: a.splitLine.lineStyle.color,
              lineWidth: parseInt(a.splitLine.lineStyle.width)
            }
          }
        } : null
        const label = a.axisLabel.show ? {
          rotate: parseInt(a.axisLabel.rotate) * Math.PI / 180,
          style: {
            fill: a.axisLabel.color,
            fontSize: parseInt(a.axisLabel.fontSize)
          }
        } : null

        axis = {
          position: transAxisPosition(chart, a),
          title: title,
          grid: grid,
          label: label
        }

        // 轴值设置
        delete axis.minLimit
        delete axis.maxLimit
        delete axis.tickCount
        const axisValue = a.axisValue
        if (chart.type.includes('horizontal')) {
          if (axisValue && !axisValue.auto) {
            axisValue.min && (axis.minLimit = parseFloat(axisValue.min))
            axisValue.max && (axis.maxLimit = parseFloat(axisValue.max))
            axisValue.splitCount && (axis.tickCount = parseFloat(axisValue.splitCount))
          }
        }
      } else {
        axis = false
      }
    }
  }
  return axis
}
// yAxis
export function getYAxis(chart) {
  let axis = {}
  let customStyle
  if (chart.customStyle) {
    customStyle = JSON.parse(chart.customStyle)
    // legend
    if (customStyle.yAxis) {
      const a = JSON.parse(JSON.stringify(customStyle.yAxis))
      if (a.show) {
        const title = (a.name && a.name !== '') ? {
          text: a.name,
          style: {
            fill: a.nameTextStyle.color,
            fontSize: parseInt(a.nameTextStyle.fontSize)
          },
          spacing: 8
        } : null
        const grid = a.splitLine.show ? {
          line: {
            style: {
              stroke: a.splitLine.lineStyle.color,
              lineWidth: parseInt(a.splitLine.lineStyle.width)
            }
          }
        } : null
        const label = a.axisLabel.show ? {
          rotate: parseInt(a.axisLabel.rotate) * Math.PI / 180,
          style: {
            fill: a.axisLabel.color,
            fontSize: parseInt(a.axisLabel.fontSize)
          }
        } : null

        axis = {
          position: transAxisPosition(chart, a),
          title: title,
          grid: grid,
          label: label
        }

        // 轴值设置
        delete axis.minLimit
        delete axis.maxLimit
        delete axis.tickCount
        const axisValue = a.axisValue
        if (!chart.type.includes('horizontal')) {
          if (axisValue && !axisValue.auto) {
            axisValue.min && (axis.minLimit = parseFloat(axisValue.min))
            axisValue.max && (axis.maxLimit = parseFloat(axisValue.max))
            axisValue.splitCount && (axis.tickCount = parseFloat(axisValue.splitCount))
          }
        }
      } else {
        axis = false
      }
    }
  }
  return axis
}
// yAxisExt
export function getYAxisExt(chart) {
  let axis = {}
  let customStyle
  if (chart.customStyle) {
    customStyle = JSON.parse(chart.customStyle)
    // legend
    if (customStyle.yAxisExt) {
      const a = JSON.parse(JSON.stringify(customStyle.yAxisExt))
      if (a.show) {
        const title = (a.name && a.name !== '') ? {
          text: a.name,
          style: {
            fill: a.nameTextStyle.color,
            fontSize: parseInt(a.nameTextStyle.fontSize)
          },
          spacing: 8
        } : null
        const grid = a.splitLine.show ? {
          line: {
            style: {
              stroke: a.splitLine.lineStyle.color,
              lineWidth: parseInt(a.splitLine.lineStyle.width)
            }
          }
        } : null
        const label = a.axisLabel.show ? {
          rotate: parseInt(a.axisLabel.rotate) * Math.PI / 180,
          style: {
            fill: a.axisLabel.color,
            fontSize: parseInt(a.axisLabel.fontSize)
          }
        } : null

        axis = {
          position: transAxisPosition(chart, a),
          title: title,
          grid: grid,
          label: label
        }

        // 轴值设置
        delete axis.minLimit
        delete axis.maxLimit
        delete axis.tickCount
        const axisValue = a.axisValue
        if (!chart.type.includes('horizontal')) {
          if (axisValue && !axisValue.auto) {
            axisValue.min && (axis.minLimit = parseFloat(axisValue.min))
            axisValue.max && (axis.maxLimit = parseFloat(axisValue.max))
            axisValue.splitCount && (axis.tickCount = parseFloat(axisValue.splitCount))
          }
        }
      } else {
        axis = false
      }
    }
  }
  return axis
}

function transAxisPosition(chart, axis) {
  if (chart.type.includes('horizontal')) {
    switch (axis.position) {
      case 'top':
        return 'right'
      case 'bottom':
        return 'left'
      case 'left':
        return 'bottom'
      case 'right':
        return 'top'
      default:
        return axis.position
    }
  } else {
    return axis.position
  }
}

export function getSlider(chart) {
  let senior = {}
  let cfg = false
  if (chart.senior && chart.type && (chart.type.includes('bar') || chart.type.includes('line') || chart.type.includes('mix'))) {
    senior = JSON.parse(chart.senior)
    if (senior.functionCfg) {
      if (senior.functionCfg.sliderShow) {
        cfg = {
          start: parseInt(senior.functionCfg.sliderRange[0]) / 100,
          end: parseInt(senior.functionCfg.sliderRange[1]) / 100
        }
      }
    }
  }
  return cfg
}

export function getAnalyse(chart) {
  let senior = {}
  const assistLine = []
  if (chart.senior && chart.type && (chart.type.includes('bar') || chart.type.includes('line') || chart.type.includes('mix'))) {
    senior = JSON.parse(chart.senior)
    if (senior.assistLine && senior.assistLine.length > 0) {
      senior.assistLine.forEach(ele => {
        assistLine.push({
          type: 'line',
          start: ['start', parseFloat(ele.value)],
          end: ['end', parseFloat(ele.value)],
          style: {
            stroke: ele.color,
            lineDash: getLineDash(ele.lineType)
          }
        })
        assistLine.push({
          type: 'text',
          position: ['start', parseFloat(ele.value)],
          content: parseFloat(ele.value),
          offsetY: -2,
          offsetX: 2,
          style: {
            textBaseline: 'bottom',
            fill: ele.color,
            fontSize: 10
          }
        })
      })
    }
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
