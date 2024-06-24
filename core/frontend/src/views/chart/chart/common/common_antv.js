import { hexColorToRGBA } from '@/views/chart/chart/util'
import { formatterItem, valueFormatter } from '@/views/chart/chart/formatter'
import {
  DEFAULT_COLOR_CASE,
  DEFAULT_XAXIS_STYLE,
  DEFAULT_YAXIS_EXT_STYLE,
  DEFAULT_YAXIS_STYLE
} from '@/views/chart/chart/chart'
import { equalsAny, includesAny } from '@/utils/StringUtils'
import {
  regressionExp,
  regressionPoly,
  regressionLinear,
  regressionLoess,
  regressionLog,
  regressionPow,
  regressionQuad
} from 'd3-regression/dist/d3-regression.esm'
import substitute from '@antv/util/esm/substitute'
import createDom from '@antv/dom-util/esm/create-dom'
import { assign } from 'lodash-es'

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
  let bgColor, labelFontsize, labelColor, tooltipColor, tooltipFontsize, tooltipBackgroundColor, legendColor, legendFontsize
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
      tooltipBackgroundColor = t.backgroundColor
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
            'z-index': 3000,
            position: 'fixed'
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
  // 堆叠柱状图需要取消 offset，因为在顶部类别占比较低的时候有可能会把标签挤出去
  // 并且视觉上也比较不舒服
  if (equalsAny(chart.type, 'percentage-bar-stack', 'bar-group-stack')) {
    theme.innerLabels.offset = 0
  }
  return theme
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
        if (equalsAny(chart.type, 'pie', 'pie-donut')) {
          label = {
            type: l.position,
            autoRotate: false
          }
          if (l.position === 'outer') {
            label.type = 'spider'
            label.layout = [
              { type: 'limit-in-plot' }
            ]
          }
        } else if (chart.type.includes('line') || chart.type.includes('area')) {
          label = {
            position: l.position,
            layout: [
              { type: 'hide-overlap' },
              { type: 'limit-in-plot' }
            ]
          }
        } else if (equalsAny(chart.type, 'pie-rose', 'pie-donut-rose')) {
          label = {
            autoRotate: true
          }
          if (l.position === 'inner') {
            label.offset = -10
          }
        } else if (chart.type.includes('bar')) {
          label = {
            layout: [{ type: 'limit-in-plot' }],
            position: l.position
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
        // label value formatter
        if (chart.type && chart.type !== 'waterfall') {
          if (chart.type === 'bar-time-range') {
            label.content = ''
          }
          label.formatter = function(param) {
            let xAxis, yAxis, extStack, xaxisExt
            let res = param.value
            try {
              xAxis = JSON.parse(chart.xaxis)
            } catch (e) {
              xAxis = JSON.parse(JSON.stringify(chart.xaxis))
            }
            try {
              yAxis = JSON.parse(chart.yaxis)
            } catch (e) {
              yAxis = JSON.parse(JSON.stringify(chart.yaxis))
            }
            try {
              extStack = JSON.parse(chart.extStack)
            } catch (e) {
              extStack = JSON.parse(JSON.stringify(chart.extStack))
            }
            try {
              xaxisExt = JSON.parse(chart.xaxisExt)
            } catch (e) {
              xaxisExt = JSON.parse(JSON.stringify(chart.xaxisExt))
            }

            if (equalsAny(chart.type, 'line', 'bar-stack', 'line-stack',
              'bar-stack-horizontal', 'percentage-bar-stack', 'percentage-bar-stack-horizontal')) {
              let f
              if (extStack?.length > 0 || xaxisExt?.length > 0) {
                f = yAxis[0]
              } else {
                for (let i = 0; i < yAxis.length; i++) {
                  if (yAxis[i].name === param.category) {
                    f = yAxis[i]
                    break
                  }
                }
              }
              if (!f) {
                return res
              }
              if (!f.formatterCfg) {
                f.formatterCfg = formatterItem
              }
              // 百分比堆叠柱状图保留小数处理
              if (chart.type.includes('percentage')) {
                if (!param.value) {
                  return
                }
                f.formatterCfg.type = 'percent'
                f.formatterCfg.decimalCount = l.reserveDecimalCount
                f.formatterCfg.thousandSeparator = false
              }
              res = valueFormatter(param.value, f.formatterCfg)
            } else if (chart.type === 'bidirectional-bar') {
              let yaxis = yAxis[0]
              if (param['series-field-key'] === 'extValue') {
                yaxis = JSON.parse(chart.yaxisExt)[0]
              }
              const value = param[param['series-field-key']]
              if (yaxis.formatterCfg) {
                res = valueFormatter(value, yaxis.formatterCfg)
              } else {
                res = valueFormatter(value, formatterItem)
              }
            } else if (equalsAny(chart.type, 'bar-group')) {
              const f = yAxis[0]
              if (f.formatterCfg) {
                res = valueFormatter(param.value, f.formatterCfg)
              } else {
                res = valueFormatter(param.value, formatterItem)
              }
            } else if (equalsAny(chart.type, 'bar-group-stack')) {
              const f = yAxis[0]
              let formatterCfg = formatterItem
              if (f.formatterCfg) {
                formatterCfg = f.formatterCfg
              }
              const labelContent = l.labelContent ?? ['quota']
              const contentItems = []
              if (labelContent.includes('group')) {
                contentItems.push(param.group)
              }
              if (labelContent.includes('stack')) {
                contentItems.push(param.category)
              }
              if (labelContent.includes('quota')) {
                contentItems.push(valueFormatter(param.value, formatterCfg))
              }
              res = contentItems.join('\n')
            } else if (chart.type === 'scatter' && xAxis && xAxis.length > 0 && xAxis[0].groupType === 'q') {
              // 针对散点图
              res = param.field
            } else if (chart.type === 'bar-time-range') {
              if (l.showGap) {
                res = param.gap
              } else {
                res = param.values[0] + ' ~ ' + param.values[1]
              }
            } else {
              for (let i = 0; i < yAxis.length; i++) {
                const f = yAxis[i]
                let formatterCfg = formatterItem
                if (f.formatterCfg) {
                  formatterCfg = f.formatterCfg
                }

                if (chart.type === 'scatter' && xAxis && xAxis.length > 0 && xAxis[0].groupType === 'q') {
                  // 针对横轴为指标的散点图
                  if (f.name === param.group) {
                    res = valueFormatter(param.value, formatterCfg)
                  }
                } else {
                  if (f.name === param.category) {
                  // 饼图和环形图格式优化
                    if (equalsAny(chart.type, 'pie', 'pie-donut')) {
                    // 这边默认值取指标是为了兼容存量的视图
                      const labelContent = l.labelContent ?? ['quota']
                      const contentItems = []
                      if (labelContent.includes('dimension')) {
                        contentItems.push(param.field)
                      }
                      if (labelContent.includes('quota')) {
                        contentItems.push(valueFormatter(param.value, formatterCfg))
                      }
                      if (labelContent.includes('proportion')) {
                        const percentage = `${(Math.round(param.percent * 10000) / 100).toFixed(l.reserveDecimalCount)}%`
                        if (labelContent.length === 3) {
                          contentItems.push(`(${percentage})`)
                        } else {
                          contentItems.push(percentage)
                        }
                      }
                      res = contentItems.join(' ')
                    } else if (equalsAny(chart.type, 'pie-rose', 'pie-donut-rose')) {
                      const quotaValue = valueFormatter(param.value, formatterCfg)
                      res = [param.field, quotaValue].join(' ')
                    } else {
                      res = valueFormatter(param.value, formatterCfg)
                    }
                    break
                  }
                }
              }
            }
            return res
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
export function getTooltip(chart) {
  let tooltip = {}
  let customAttr = {}
  if (chart.customAttr) {
    customAttr = JSON.parse(chart.customAttr)
    // tooltip
    if (customAttr.tooltip) {
      const t = JSON.parse(JSON.stringify(customAttr.tooltip))
      if (t.show) {
        tooltip = {
          container: getTooltipContainer(`tooltip-${chart.id}`),
          itemTpl: TOOLTIP_TPL,
          enterable: true
        }
        let xAxis, yAxis, extStack, xAxisExt

        try {
          xAxis = JSON.parse(chart.xaxis)
        } catch (e) {
          xAxis = JSON.parse(JSON.stringify(chart.xaxis))
        }
        try {
          xAxisExt = JSON.parse(chart.xaxisExt)
        } catch (e) {
          xAxisExt = JSON.parse(JSON.stringify(chart.xaxisExt))
        }
        try {
          yAxis = JSON.parse(chart.yaxis)
        } catch (e) {
          yAxis = JSON.parse(JSON.stringify(chart.yaxis))
        }
        try {
          extStack = JSON.parse(chart.extStack)
        } catch (e) {
          extStack = JSON.parse(JSON.stringify(chart.extStack))
        }

        // tooltip value formatter
        if (chart.type && chart.type !== 'waterfall') {
          if (chart.type === 'bar-group-stack') {
            tooltip.fields = []
          } else if (chart.type === 'bar-time-range') {
            tooltip.fields = ['gap', 'category', 'values', 'group', 'field']
          }

          tooltip.formatter = function(param) {
            let res = param.value

            let obj
            if (equalsAny(chart.type, 'bar-stack', 'line-stack',
              'bar-stack-horizontal', 'percentage-bar-stack', 'percentage-bar-stack-horizontal')) {
              let f
              if (extStack && extStack.length > 0) {
                obj = { name: param.category, value: param.value }
                f = yAxis[0]
              } else {
                obj = { name: param.category, value: param.value }
                for (let i = 0; i < yAxis.length; i++) {
                  if (yAxis[i].name === param.category) {
                    f = yAxis[i]
                    break
                  }
                }
              }
              if (!f) {
                return res
              }
              if (!f.formatterCfg) {
                f.formatterCfg = formatterItem
              }
              if (chart.type.includes('percentage')) {
                if (!param.value) {
                  obj.value = 0
                  return obj
                }
                // 保留小数位数和标签保持一致，这边拿一下标签的配置
                const l = JSON.parse(JSON.stringify(customAttr.label))
                f.formatterCfg.type = 'percent'
                f.formatterCfg.decimalCount = l.reserveDecimalCount
                f.formatterCfg.thousandSeparator = false
              }
              res = valueFormatter(param.value, f.formatterCfg)
            } else if (chart.type === 'word-cloud') {
              obj = { name: param.text, value: param.value }
              for (let i = 0; i < yAxis.length; i++) {
                const f = yAxis[i]
                if (f.formatterCfg) {
                  res = valueFormatter(param.value, f.formatterCfg)
                } else {
                  res = valueFormatter(param.value, formatterItem)
                }
              }
            } else if (chart.type === 'bidirectional-bar') {
              let yaxis = yAxis[0]
              if (param['series-field-key'] === 'extValue') {
                yaxis = JSON.parse(chart.yaxisExt)[0]
              }
              obj = { name: yaxis.name, value: param[param['series-field-key']] }
              if (yaxis.formatterCfg) {
                res = valueFormatter(obj.value, yaxis.formatterCfg)
              } else {
                res = valueFormatter(obj.value, formatterItem)
              }
            } else if (chart.type.includes('treemap')) {
              obj = { name: param.name, value: param.value }
              for (let i = 0; i < yAxis.length; i++) {
                const f = yAxis[i]
                if (f.formatterCfg) {
                  res = valueFormatter(param.value, f.formatterCfg)
                } else {
                  res = valueFormatter(param.value, formatterItem)
                }
              }
            } else if (includesAny(chart.type, 'pie', 'funnel')) {
              obj = { name: param.field, value: param.value }
              for (let i = 0; i < yAxis.length; i++) {
                const f = yAxis[i]
                if (f.formatterCfg) {
                  res = valueFormatter(param.value, f.formatterCfg)
                } else {
                  res = valueFormatter(param.value, formatterItem)
                }
              }
            } else if (includesAny(chart.type, 'bar', 'scatter', 'radar', 'area') && !chart.type.includes('group') && chart.type !== 'bar-time-range') {
              obj = { name: param.category, value: param.value }
              for (let i = 0; i < yAxis.length; i++) {
                const f = yAxis[i]
                if (f.name === param.category) {
                  if (f.formatterCfg) {
                    res = valueFormatter(param.value, f.formatterCfg)
                  } else {
                    res = valueFormatter(param.value, formatterItem)
                  }
                  break
                }
              }
            } else if (chart.type === 'line') {
              obj = { name: param.category, value: param.value }
              const xAxisExt = JSON.parse(chart.xaxisExt)
              for (let i = 0; i < yAxis.length; i++) {
                const f = yAxis[i]
                if (f.name === param.category || (yAxis.length && xAxisExt.length)) {
                  if (f.formatterCfg) {
                    res = valueFormatter(param.value, f.formatterCfg)
                  } else {
                    res = valueFormatter(param.value, formatterItem)
                  }
                  break
                }
              }
            } else if (chart.type.includes('group')) {
              if (chart.type === 'bar-group') {
                obj = { name: param.category, value: param.value }
              } else {
                let name = ''
                if (param.group) {
                  name = param.group + '-'
                }
                if (param.category) {
                  name += param.category
                }
                obj = { name: name, value: param.value }
              }
              for (let i = 0; i < yAxis.length; i++) {
                const f = yAxis[i]
                if (f.formatterCfg) {
                  res = valueFormatter(param.value, f.formatterCfg)
                } else {
                  res = valueFormatter(param.value, formatterItem)
                }
              }
            } else if (chart.type === 'bar-time-range') {
              obj = { values: param.values, name: param.category }
              res = param.values[0] + ' ~ ' + param.values[1]
              if (t.showGap) {
                res = res + ' (' + param.gap + ')'
              }
            } else {
              res = param.value
            }
            obj.value = res === null ? '' : res
            return obj
          }
          //
          if (chart.type === 'scatter' && xAxis && xAxis.length > 0 && xAxis[0].groupType === 'q') {
            tooltip.fields = ['x', 'category', 'value', 'group', 'field']
            tooltip.showTitle = true
            tooltip.title = 'category'
            let subGroupTpl = ''
            if (xAxisExt?.length) {
              subGroupTpl = '<div>' +
                '<span class="g2-tooltip-name">{subGroupName}</span>:' +
                '<span class="g2-tooltip-value">{subGroupValue}</span>' +
                '</div>'
            }
            tooltip.itemTpl = '<li class="g2-tooltip-list-item">' +
              subGroupTpl +
              '<div>' +
              '<span class="g2-tooltip-name">{name}</span>:' +
              '<span class="g2-tooltip-value">{x}</span>' +
              '</div>' +
              '<div>' +
              '<span class="g2-tooltip-name">{group}</span>:' +
              '<span class="g2-tooltip-value">{value}</span>' +
              '</div>' +
              '</li>'
            tooltip.customItems = items => {
              items?.forEach(item => {
                item.title = item.data.category
                item.name = xAxis[0]?.name

                item.group = item.data.group
                item.subGroupValue = item.data.field
                item.subGroupName = xAxisExt?.[0]?.name

                const fx = xAxis[0]
                if (fx.formatterCfg) {
                  item.x = valueFormatter(item.data.x, fx.formatterCfg)
                } else {
                  item.x = valueFormatter(item.data.x, formatterItem)
                }

                for (let i = 0; i < yAxis.length; i++) {
                  const f = yAxis[i]
                  if (f.name === item.group) {
                    if (f.formatterCfg) {
                      item.value = valueFormatter(item.data.value, f.formatterCfg)
                    } else {
                      item.value = valueFormatter(item.data.value, formatterItem)
                    }
                    break
                  }
                }
              })
              return items
            }
          }
        }
      } else {
        // 百分比堆叠柱状图隐藏 tooltip 设置 show 为 false 或者直接设置 tooltip 为 false 都无效，会变成分组显示，
        // 需要将容器(container)或者内容框(showContent)设置为 false 或者 null 才可以隐藏
        if (chart.type.includes('percentage')) {
          tooltip.showContent = false
        } else {
          tooltip = false
        }
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
          },
          radio: false, // 柱状图图例的聚焦功能，默认先关掉
          itemName: {
            formatter: (text, item, index) => {
              if (chart.type !== 'bidirectional-bar') {
                return text
              }
              const yaxis = JSON.parse(chart.yaxis)[0]
              const yaxisExt = JSON.parse(chart.yaxisExt)[0]
              if (index === 0) {
                return yaxis.name
              }
              return yaxisExt.name
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
        const gridCfg = a.splitLine ? a.splitLine : DEFAULT_XAXIS_STYLE.splitLine
        if (!gridCfg.dashStyle) {
          gridCfg.dashStyle = DEFAULT_XAXIS_STYLE.splitLine.dashStyle
        }
        const grid = a.splitLine.show ? {
          line: {
            style: {
              stroke: a.splitLine.lineStyle.color,
              lineWidth: parseInt(a.splitLine.lineStyle.width),
              lineDash: gridCfg.enableDash ? [gridCfg.dashStyle.width, gridCfg.dashStyle.offset] : undefined
            }
          }
        } : null
        const axisCfg = a.axisLine ? a.axisLine : DEFAULT_XAXIS_STYLE.axisLine
        const axisLine = axisCfg.show ? {
          style: {
            stroke: axisCfg.lineStyle.color,
            lineWidth: parseInt(axisCfg.lineStyle.width)
          }
        } : null
        const tickLine = axisCfg.show ? {
          style: {
            stroke: axisCfg.lineStyle.color
          }
        } : null
        const rotate = parseInt(a.axisLabel.rotate)
        const label = a.axisLabel.show ? {
          rotate: rotate * Math.PI / 180,
          style: {
            textAlign: rotate > 20 ? 'start' : rotate < -20 ? 'end' : 'center',
            fill: a.axisLabel.color,
            fontSize: parseInt(a.axisLabel.fontSize)
          },
          formatter: function(value) {
            if (chart.type.includes('horizontal')) {
              if (!a.axisLabelFormatter) {
                return valueFormatter(value, formatterItem)
              } else {
                return valueFormatter(value, a.axisLabelFormatter)
              }
            } else {
              return value
            }
          }
        } : null

        const position = transAxisPosition(chart, a)

        if (a.axisLabel.show && chart.type === 'bidirectional-bar') {
          label.rotate = 0
          label.style.textAlign = 'start'
        }
        if (a.axisLabel.show && chart.type === 'bar-time-range') {
          label.rotate = 0
          if (position === 'top') {
            label.style.textAlign = 'start'
          } else {
            label.style.textAlign = 'end'
          }
        }

        axis = {
          position: position,
          title: title,
          grid: grid,
          label: label,
          line: axisLine,
          tickLine: tickLine
        }

        // 轴值设置
        delete axis.minLimit
        delete axis.maxLimit
        delete axis.tickCount
        const axisValue = a.axisValue
        if (chart.type.includes('horizontal') || chart.type === 'bar-time-range') {
          if (axisValue && !axisValue.auto) {
            const yAxisSeriesMaxList = []
            const maxList = []
            chart.data.data.forEach(ele => {
              maxList.push(ele.value)
            })
            yAxisSeriesMaxList.push(Math.max.apply(null, maxList))
            if (yAxisSeriesMaxList.length > 0 && !isNaN(axisValue.max)) {
              const max = Math.max.apply(null, yAxisSeriesMaxList)
              if (max <= parseFloat(axisValue.max)) {
                axisValue.max && (axis.maxLimit = axis.max = parseFloat(axisValue.max))
              }
            }

            const yAxisSeriesMinList = []
            const minList = []
            chart.data.data.forEach(ele => {
              minList.push(ele.value)
            })
            yAxisSeriesMinList.push(Math.min.apply(null, minList))
            if (yAxisSeriesMinList.length > 0 && !isNaN(axisValue.min)) {
              const min = Math.min.apply(null, yAxisSeriesMinList)
              if (min >= parseFloat(axisValue.min)) {
                axisValue.min && (axis.minLimit = axis.min = parseFloat(axisValue.min))
              }
            }
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
        const gridCfg = a.splitLine ? a.splitLine : DEFAULT_YAXIS_STYLE.splitLine
        if (!gridCfg.dashStyle) {
          gridCfg.dashStyle = DEFAULT_YAXIS_STYLE.splitLine.dashStyle
        }
        const grid = a.splitLine.show ? {
          line: {
            style: {
              stroke: a.splitLine.lineStyle.color,
              lineWidth: parseInt(a.splitLine.lineStyle.width),
              lineDash: gridCfg.enableDash ? [gridCfg.dashStyle.width, gridCfg.dashStyle.offset] : undefined
            }
          }
        } : null
        const axisCfg = a.axisLine ? a.axisLine : DEFAULT_YAXIS_STYLE.axisLine
        const axisLine = axisCfg.show ? {
          style: {
            stroke: axisCfg.lineStyle.color,
            lineWidth: parseInt(axisCfg.lineStyle.width)
          }
        } : null
        const tickLine = axisCfg.show ? {
          style: {
            stroke: axisCfg.lineStyle.color
          }
        } : null
        const label = a.axisLabel.show ? {
          rotate: parseInt(a.axisLabel.rotate) * Math.PI / 180,
          style: {
            fill: a.axisLabel.color,
            fontSize: parseInt(a.axisLabel.fontSize)
          },
          formatter: function(value) {
            if (chart.type === 'waterfall') {
              return value
            } else {
              if (!(chart.type.includes('horizontal') || chart.type === 'bar-time-range')) {
                if (!a.axisLabelFormatter) {
                  return valueFormatter(value, formatterItem)
                } else {
                  return valueFormatter(value, a.axisLabelFormatter)
                }
              } else {
                const { lengthLimit } = a.axisLabel
                if (!lengthLimit || value?.length <= lengthLimit) {
                  return value
                }
                return value?.slice(0, lengthLimit) + '...'
              }
            }
          }
        } : null

        axis = {
          position: transAxisPosition(chart, a),
          title: title,
          grid: grid,
          label: label,
          line: axisLine,
          tickLine: tickLine
        }

        // 轴值设置
        delete axis.minLimit
        delete axis.maxLimit
        delete axis.tickCount
        const axisValue = a.axisValue
        if (!chart.type.includes('horizontal') || chart.type === 'bar-time-range') {
          if (axisValue && !axisValue.auto) {
            const yAxisSeriesMaxList = []
            const maxList = []
            chart.data.data.forEach(ele => {
              maxList.push(ele.value)
            })
            yAxisSeriesMaxList.push(Math.max.apply(null, maxList))
            if (yAxisSeriesMaxList.length > 0 && !isNaN(axisValue.max)) {
              const max = Math.max.apply(null, yAxisSeriesMaxList)
              if (max <= parseFloat(axisValue.max)) {
                axisValue.max && (axis.maxLimit = axis.max = parseFloat(axisValue.max))
              }
            }

            const yAxisSeriesMinList = []
            const minList = []
            chart.data.data.forEach(ele => {
              minList.push(ele.value)
            })
            yAxisSeriesMinList.push(Math.min.apply(null, minList))
            if (yAxisSeriesMinList.length > 0 && !isNaN(axisValue.min)) {
              const min = Math.min.apply(null, yAxisSeriesMinList)
              if (min >= parseFloat(axisValue.min)) {
                axisValue.min && (axis.minLimit = axis.min = parseFloat(axisValue.min))
              }
            }
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
        const gridCfg = a.splitLine ? a.splitLine : DEFAULT_YAXIS_EXT_STYLE.splitLine
        if (!gridCfg.dashStyle) {
          gridCfg.dashStyle = DEFAULT_YAXIS_EXT_STYLE.splitLine.dashStyle
        }
        const grid = a.splitLine.show ? {
          line: {
            style: {
              stroke: a.splitLine.lineStyle.color,
              lineWidth: parseInt(a.splitLine.lineStyle.width),
              lineDash: gridCfg.enableDash ? [gridCfg.dashStyle.width, gridCfg.dashStyle.offset] : undefined
            }
          }
        } : null
        const axisCfg = a.axisLine ? a.axisLine : DEFAULT_YAXIS_EXT_STYLE.axisLine
        const axisLine = axisCfg.show ? {
          style: {
            stroke: axisCfg.lineStyle.color,
            lineWidth: parseInt(axisCfg.lineStyle.width)
          }
        } : null
        const tickLine = axisCfg.show ? {
          style: {
            stroke: axisCfg.lineStyle.color
          }
        } : null
        const label = a.axisLabel.show ? {
          rotate: parseInt(a.axisLabel.rotate) * Math.PI / 180,
          style: {
            fill: a.axisLabel.color,
            fontSize: parseInt(a.axisLabel.fontSize)
          },
          formatter: function(value) {
            if (chart.type === 'waterfall') {
              return value
            } else {
              if (!chart.type.includes('horizontal')) {
                if (!a.axisLabelFormatter) {
                  return valueFormatter(value, formatterItem)
                } else {
                  return valueFormatter(value, a.axisLabelFormatter)
                }
              } else {
                return value
              }
            }
          }
        } : null

        axis = {
          position: transAxisPosition(chart, a),
          title: title,
          grid: grid,
          label: label,
          line: axisLine,
          tickLine: tickLine
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
        return 'left'
      case 'bottom':
        return 'right'
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
  if (chart.senior && chart.type && (chart.type.includes('bar') || chart.type.includes('line') || chart.type.includes('mix') || chart.type.includes('area'))) {
    senior = JSON.parse(chart.senior)
    if (senior.functionCfg) {
      if (senior.functionCfg.sliderShow) {
        cfg = {
          start: parseInt(senior.functionCfg.sliderRange[0]) / 100,
          end: parseInt(senior.functionCfg.sliderRange[1]) / 100
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
            'fill': senior.functionCfg.sliderFillBg,
            'fillOpacity': 0.5
          }
        }
        if (senior.functionCfg.sliderTextClolor) {
          cfg.textStyle = {
            'fill': senior.functionCfg.sliderTextClolor
          }
          cfg.handlerStyle = {
            'fill': senior.functionCfg.sliderTextClolor,
            'fillOpacity': 0.5,
            'highLightFill': senior.functionCfg.sliderTextClolor
          }
        }
      }
    }
  }
  return cfg
}

export function getAnalyse(chart) {
  let senior = {}
  const assistLine = []
  if (chart.senior && chart.type && (chart.type.includes('bar') || chart.type.includes('line') || chart.type.includes('mix') || chart.type.includes('area') || chart.type.includes('scatter'))) {
    senior = JSON.parse(chart.senior)
    if (senior.assistLine && senior.assistLine.length > 0) {
      const customStyle = JSON.parse(chart.customStyle)
      let xAxisPosition, yAxisPosition, axisFormatterCfg
      if (customStyle.xAxis) {
        const a = JSON.parse(JSON.stringify(customStyle.xAxis))
        xAxisPosition = transAxisPosition(chart, a)
        if (chart.type.includes('horizontal')) {
          axisFormatterCfg = a.axisLabelFormatter ? a.axisLabelFormatter : DEFAULT_XAXIS_STYLE.axisLabelFormatter
        }
      }
      if (customStyle.yAxis) {
        const a = JSON.parse(JSON.stringify(customStyle.yAxis))
        yAxisPosition = transAxisPosition(chart, a)
        if (!chart.type.includes('horizontal')) {
          axisFormatterCfg = a.axisLabelFormatter ? a.axisLabelFormatter : DEFAULT_YAXIS_STYLE.axisLabelFormatter
        }
      }

      const fixedLines = senior.assistLine.filter(ele => ele.field === '0')
      const dynamicLines = chart.data.dynamicAssistData
      const lines = fixedLines.concat(dynamicLines)

      lines.forEach(ele => {
        if (ele) {
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
          if (!chart.type.includes('horizontal')) {
            assistLine.push({
              type: 'text',
              position: [yAxisPosition === 'left' ? 'start' : 'end', value],
              content: content,
              offsetY: -2,
              offsetX: yAxisPosition === 'left' ? 2 : -10 * (content.length - 2),
              style: {
                textBaseline: 'bottom',
                fill: ele.color,
                fontSize: ele.fontSize ? parseInt(ele.fontSize) : 10
              }
            })
          } else {
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
                fontSize: ele.fontSize ? parseInt(ele.fontSize) : 10
              }
            })
          }
        }
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

export function setGradientColor(rawColor, show = false, angle = 0) {
  const item = rawColor.split(',')
  item.splice(3, 1, '0.3)')
  return show ? `l(${angle}) 0:${item.join(',')} 1:${rawColor}` : rawColor
}

export function getMeta(chart) {
  let meta
  if (chart.type === 'bidirectional-bar') {
    const xAxis = JSON.parse(chart.xaxis)
    if (xAxis?.length === 1 && xAxis[0].deType === 1) {
      const values = chart.data.data.map(item => item.field)
      meta = {
        field: {
          type: 'cat',
          values: values.reverse()
        }
      }
    }
  }
  return meta
}
export function getTooltipContainer(id) {
  const curDom = document.getElementById(id)
  if (curDom) {
    curDom.remove()
  }
  const g2Tooltip = document.createElement('div')
  g2Tooltip.setAttribute('id', id)
  g2Tooltip.classList.add('g2-tooltip')
  // 最多半屏，鼠标移入可滚动
  g2Tooltip.style.maxHeight = '50%'
  g2Tooltip.style.overflowY = 'scroll'
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
    document.body.appendChild(g2Tooltip)
  }
  return g2Tooltip
}
export function configPlotTooltipEvent(chart, plot) {
  const customAttr = JSON.parse(chart.customAttr)
  const t = JSON.parse(JSON.stringify(customAttr.tooltip))
  if (!t.show) {
    return
  }
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
  plot.on('tooltip:show', () => {
    const tooltipCtl = plot.chart.getController('tooltip')
    if (!tooltipCtl) {
      return
    }
    const event = plot.chart.interactions.tooltip?.context?.event
    if (tooltipCtl.tooltip) {
      // 处理视图放大后再关闭 tooltip 的 dom 被清除
      const container = tooltipCtl.tooltip.cfg.container
      container.style.display = 'block'
      const dom = document.getElementById(container.id)
      if (!dom) {
        const full = document.getElementsByClassName('fullscreen')
        if (full.length) {
          full.item(0).appendChild(container)
        } else {
          document.body.appendChild(container)
        }
      }
    }
    plot.chart.getOptions().tooltip.follow = false
    tooltipCtl.title = Math.random().toString()
    plot.chart.getTheme().components.tooltip.x = event.clientX
    plot.chart.getTheme().components.tooltip.y = event.clientY
  })
  // https://github.com/antvis/G2/blob/master/src/chart/controller/tooltip.ts#hideTooltip
  plot.on('plot:mouseleave', () => {
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
}

export const TOOLTIP_TPL = '<li class="g2-tooltip-list-item" data-index={index}>' +
                                    '<span class="g2-tooltip-marker" style="background:{color}"></span>' +
                                    '<span class="g2-tooltip-name">{name}</span>:' +
                                    '<span class="g2-tooltip-value">{value}</span>' +
                                  '</li>'
export const configTopN = (data, chart) => {
  if (!data?.length) {
    return
  }
  const color = JSON.parse(chart.customAttr).color
  if (!color.calcTopN || data.length <= color.topN) {
    return
  }
  data.sort((a, b) => b.value - a.value)
  const otherItems = data.splice(color.topN)
  const initOtherItem = {
    ...data[0],
    field: color.topNLabel ?? DEFAULT_COLOR_CASE.topNLabel,
    name: color.topNLabel ?? DEFAULT_COLOR_CASE.topNLabel,
    value: 0
  }
  otherItems.reduce((p, n) => {
    p.value += n.value ?? 0
    return p
  }, initOtherItem)
  data.push(initOtherItem)
}
const REGRESSION_ALGO_MAP = {
  poly: regressionPoly,
  linear: regressionLinear,
  exp: regressionExp,
  log: regressionLog,
  quad: regressionQuad,
  pow: regressionPow,
  loess: regressionLoess
}
const AXIS_LABEL_TOOLTIP_STYLE = {
  transition: 'left 0.4s cubic-bezier(0.23, 1, 0.32, 1) 0s, top 0.4s cubic-bezier(0.23, 1, 0.32, 1) 0s',
  backgroundColor: 'rgb(255, 255, 255)',
  boxShadow: 'rgb(174, 174, 174) 0px 0px 10px',
  borderRadius: '3px',
  padding: '8px 12px',
  opacity: '0.95',
  position: 'absolute',
  visibility: 'visible'
}
const AXIS_LABEL_TOOLTIP_TPL = '<div class="g2-axis-label-tooltip">' +
  '<div class="g2-tooltip-title">{title}</div>' +
  '</div>'
export function configPlotTrendLine(chart, plot) {
  const senior = JSON.parse(chart.senior)
  if (!senior?.trendLine?.length || !chart.data?.data?.length) {
    return
  }
  const originData = chart.data.data
  const originFieldDataMap = {}
  originData.forEach(item => {
    if (item.quotaList?.length) {
      const quota = item.quotaList[0]
      if (!originFieldDataMap[quota.id]) {
        originFieldDataMap[quota.id] = []
      }
      originFieldDataMap[quota.id].push(item.value)
    }
  })
  const trendResultData = {}
  const totalData = []
  const trendLineMap = senior.trendLine.reduce((p, n, i) => {
    const fieldData = originFieldDataMap[n.fieldId]
    if (!fieldData?.length) {
      return p
    }
    const regAlgo = REGRESSION_ALGO_MAP[n.algoType]()
      .x((_, i) => i + 1)
      .y(d => d)
    const result = regAlgo(fieldData)
    const trendId = `${n.fieldId}|${i}`
    trendResultData[trendId] = result
    result.forEach(item => {
      totalData.push({ trendId, index: item[0], value: item[1], color: n.color, field: n.fieldId })
    })
    p[trendId] = n
    return p
  }, {})
  if (!totalData.length) {
    return
  }
  const regLine = plot.chart.createView()
  plot.once('afterrender', () => {
    for (const trendId in trendResultData) {
      const trendLine = trendLineMap[trendId]
      const trendData = trendResultData[trendId]
      regLine.annotation().text({
        content: trendLine.name,
        position: [0, trendData[0][1]],
        style: {
          textBaseline: 'bottom',
          fill: trendLine.color,
          fontSize: trendLine.fontSize ?? 20,
          fontWeight: 300
        },
        offsetY: 10
      })
    }
    regLine.axis(false)
    regLine.data(totalData)
    regLine.line()
      .position('index*value')
      .color('color', color => color)
      .style('trendId', trendId => {
        const trend = trendLineMap[trendId]
        return {
          stroke: trend?.color ?? 'grey',
          lineDash: trend?.lineType ? getLineDash(trend.lineType) : [0, 0]
        }
      })
      .tooltip(false)
    regLine.render()
  })
}
export function configAxisLabelLengthLimit(chart, plot) {
  const customStyle = JSON.parse(chart.customStyle)
  const { lengthLimit, fontSize, color, show } = customStyle.yAxis.axisLabel
  if (!lengthLimit || !show) {
    return
  }
  plot.on('axis-label:mouseenter', (e) => {
    const field = e.target.cfg.delegateObject.component.cfg.field
    // 先只处理竖轴
    if (field !== 'field') {
      return
    }
    const realContent = e.target.attrs.text
    if (realContent.length < lengthLimit || !(realContent?.slice(-3) === '...')) {
      return
    }
    const { x, y } = e
    const parentNode = e.event.target.parentNode
    let labelTooltipDom = parentNode.getElementsByClassName('g2-axis-label-tooltip')?.[0]
    if (!labelTooltipDom) {
      const title = e.target.cfg.delegateObject.item.name
      const domStr = substitute(AXIS_LABEL_TOOLTIP_TPL, { title })
      labelTooltipDom = createDom(domStr)
      assign(labelTooltipDom.style, AXIS_LABEL_TOOLTIP_STYLE)
      parentNode.appendChild(labelTooltipDom)
    } else {
      labelTooltipDom.getElementsByClassName('g2-tooltip-title')[0].innerHTML = e.target.cfg.delegateObject.item.name
      labelTooltipDom.style.visibility = 'visible'
    }
    const { height, width } = parentNode.getBoundingClientRect()
    const { offsetHeight, offsetWidth } = labelTooltipDom
    if (offsetHeight > height || offsetWidth > width) {
      labelTooltipDom.style.left = labelTooltipDom.style.top = `0px`
      return
    }
    const initPosition = { left: x + 10, top: y + 15 }
    if (initPosition.left + offsetWidth > width) {
      initPosition.left = width - offsetWidth - 10
    }
    if (initPosition.top + offsetHeight > height) {
      initPosition.top -= (offsetHeight + 15)
    }
    labelTooltipDom.style.left = `${initPosition.left}px`
    labelTooltipDom.style.top = `${initPosition.top}px`
    labelTooltipDom.style.color = color
    labelTooltipDom.style.fontSize = `${fontSize}px`
  })
  plot.on('axis-label:mouseleave', (e) => {
    const field = e.target.cfg.delegateObject.component.cfg.field
    // 先只处理竖轴
    if (field !== 'field') {
      return
    }
    const realContent = e.target.attrs.text
    if (realContent.length < lengthLimit || !(realContent?.slice(-3) === '...')) {
      return
    }
    const parentNode = e.event.target.parentNode
    const labelTooltipDom = parentNode.getElementsByClassName('g2-axis-label-tooltip')?.[0]
    if (labelTooltipDom) {
      labelTooltipDom.style.visibility = 'hidden'
    }
  })
}
