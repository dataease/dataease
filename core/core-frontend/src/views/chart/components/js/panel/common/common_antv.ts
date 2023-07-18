import { hexColorToRGBA, parseJson } from '../../util'
import {
  DEFAULT_XAXIS_STYLE,
  DEFAULT_YAXIS_STYLE
} from '@/views/chart/components/editor/util/chart'
import { valueFormatter } from '@/views/chart/components/editor/util/formatter'
import { Datum } from '@antv/g2plot'
import { formatterItem } from '@/views/chart/components/js/formatter'
import { AreaOptions, LabelOptions } from '@antv/l7plot'
import { TooltipOptions } from '@antv/l7plot/dist/lib/types/tooltip'
import { FeatureCollection } from '@antv/l7plot/dist/esm/plots/choropleth/types'

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
  let customAttr
  if (chart.customAttr) {
    customAttr = chart.customAttr
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
    customStyle = chart.customStyle
    // bg
    if (customStyle.background) {
      bgColor = hexColorToRGBA(customStyle.background.color, customStyle.background.alpha)
    }
    // legend
    if (customStyle.legend) {
      const l = customStyle.legend
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
            background: tooltipBackgroundColor
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
  // TODO 搬到堆叠的图里面自行配置
  // 堆叠柱状图需要取消 offset，因为在顶部类别占比较低的时候有可能会把标签挤出去
  // 并且视觉上也比较不舒服
  // if (equalsAny(chart.type, 'percentage-bar-stack', 'bar-group-stack')) {
  //   theme.innerLabels.offset = 0
  // }
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
          style: {
            fill: l.color,
            fontSize: l.fontSize
          },
          formatter: function (param: Datum) {
            const yAxis = chart.yAxis
            let res = param.value
            for (let i = 0; i < yAxis.length; i++) {
              const f = yAxis[i]
              if (f.name === param.category) {
                let formatterCfg = formatterItem
                if (f.formatterCfg) {
                  formatterCfg = f.formatterCfg
                }
                res = valueFormatter(param.value, formatterCfg)
                break
              }
            }
            return res
          }
        }
        // TODO 格式化函数搬到各个视图自行实现
        // label value formatter
        // if (chart.type && chart.type !== 'waterfall') {
        //   label.formatter = function (param) {
        //     let yAxis, extStack, xaxisExt
        //     let res = param.value
        //     try {
        //       yAxis = JSON.parse(chart.yaxis)
        //     } catch (e) {
        //       yAxis = JSON.parse(JSON.stringify(chart.yaxis))
        //     }
        //     try {
        //       extStack = JSON.parse(chart.extStack)
        //     } catch (e) {
        //       extStack = JSON.parse(JSON.stringify(chart.extStack))
        //     }
        //     try {
        //       xaxisExt = JSON.parse(chart.xaxisExt)
        //     } catch (e) {
        //       xaxisExt = JSON.parse(JSON.stringify(chart.xaxisExt))
        //     }
        //
        //     if (
        //       equalsAny(
        //         chart.type,
        //         'line',
        //         'bar-stack',
        //         'line-stack',
        //         'bar-stack-horizontal',
        //         'percentage-bar-stack',
        //         'percentage-bar-stack-horizontal'
        //       )
        //     ) {
        //       let f
        //       if (extStack?.length > 0 || xaxisExt?.length > 0) {
        //         f = yAxis[0]
        //       } else {
        //         for (let i = 0; i < yAxis.length; i++) {
        //           if (yAxis[i].name === param.category) {
        //             f = yAxis[i]
        //             break
        //           }
        //         }
        //       }
        //       if (!f) {
        //         return res
        //       }
        //       if (!f.formatterCfg) {
        //         f.formatterCfg = formatterItem
        //       }
        //       // 百分比堆叠柱状图保留小数处理
        //       if (chart.type.includes('percentage')) {
        //         if (!param.value) {
        //           return
        //         }
        //         f.formatterCfg.type = 'percent'
        //         f.formatterCfg.decimalCount = l.reserveDecimalCount
        //         f.formatterCfg.thousandSeparator = false
        //       }
        //       res = valueFormatter(param.value, f.formatterCfg)
        //     } else if (equalsAny(chart.type, 'bar-group')) {
        //       const f = yAxis[0]
        //       if (f.formatterCfg) {
        //         res = valueFormatter(param.value, f.formatterCfg)
        //       } else {
        //         res = valueFormatter(param.value, formatterItem)
        //       }
        //     } else if (equalsAny(chart.type, 'bar-group-stack')) {
        //       const f = yAxis[0]
        //       let formatterCfg = formatterItem
        //       if (f.formatterCfg) {
        //         formatterCfg = f.formatterCfg
        //       }
        //       const labelContent = l.labelContent ?? ['quota']
        //       const contentItems = []
        //       if (labelContent.includes('group')) {
        //         contentItems.push(param.group)
        //       }
        //       if (labelContent.includes('stack')) {
        //         contentItems.push(param.category)
        //       }
        //       if (labelContent.includes('quota')) {
        //         contentItems.push(valueFormatter(param.value, formatterCfg))
        //       }
        //       res = contentItems.join('\n')
        //     } else {
        //       for (let i = 0; i < yAxis.length; i++) {
        //         const f = yAxis[i]
        //         if (f.name === param.category) {
        //           let formatterCfg = formatterItem
        //           if (f.formatterCfg) {
        //             formatterCfg = f.formatterCfg
        //           }
        //           // 饼图和环形图格式优化
        //           if (equalsAny(chart.type, 'pie', 'pie-donut')) {
        //             // 这边默认值取指标是为了兼容存量的视图
        //             const labelContent = l.labelContent ?? ['quota']
        //             const contentItems = []
        //             if (labelContent.includes('dimension')) {
        //               contentItems.push(param.field)
        //             }
        //             if (labelContent.includes('quota')) {
        //               contentItems.push(valueFormatter(param.value, formatterCfg))
        //             }
        //             if (labelContent.includes('proportion')) {
        //               const percentage = `${(Math.round(param.percent * 10000) / 100).toFixed(
        //                 l.reserveDecimalCount
        //               )}%`
        //               if (labelContent.length === 3) {
        //                 contentItems.push(`(${percentage})`)
        //               } else {
        //                 contentItems.push(percentage)
        //               }
        //             }
        //             res = contentItems.join(' ')
        //           } else if (equalsAny(chart.type, 'pie-rose', 'pie-donut-rose')) {
        //             const quotaValue = valueFormatter(param.value, formatterCfg)
        //             res = [param.field, quotaValue].join(' ')
        //           } else {
        //             res = valueFormatter(param.value, formatterCfg)
        //           }
        //           break
        //         }
        //       }
        //     }
        //     return res
        //   }
        // }
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
            return { value: param.value ?? '' }
          }
        }
        // tooltip value formatter
        // if (chart.type && chart.type !== 'waterfall') {
        //   tooltip.formatter = function (param) {
        //     let yAxis, extStack
        //     let res = param.value
        //     try {
        //       yAxis = JSON.parse(chart.yaxis)
        //     } catch (e) {
        //       yAxis = JSON.parse(JSON.stringify(chart.yaxis))
        //     }
        //     try {
        //       extStack = JSON.parse(chart.extStack)
        //     } catch (e) {
        //       extStack = JSON.parse(JSON.stringify(chart.extStack))
        //     }
        //
        //     let obj
        //     if (
        //       equalsAny(
        //         chart.type,
        //         'bar-stack',
        //         'line-stack',
        //         'bar-stack-horizontal',
        //         'percentage-bar-stack',
        //         'percentage-bar-stack-horizontal'
        //       )
        //     ) {
        //       let f
        //       if (extStack && extStack.length > 0) {
        //         obj = { name: param.category, value: param.value }
        //         f = yAxis[0]
        //       } else {
        //         obj = { name: param.category, value: param.value }
        //         for (let i = 0; i < yAxis.length; i++) {
        //           if (yAxis[i].name === param.category) {
        //             f = yAxis[i]
        //             break
        //           }
        //         }
        //       }
        //       if (!f) {
        //         return res
        //       }
        //       if (!f.formatterCfg) {
        //         f.formatterCfg = formatterItem
        //       }
        //       if (chart.type.includes('percentage')) {
        //         if (!param.value) {
        //           obj.value = 0
        //           return obj
        //         }
        //         // 保留小数位数和标签保持一致，这边拿一下标签的配置
        //         const l = JSON.parse(JSON.stringify(customAttr.label))
        //         f.formatterCfg.type = 'percent'
        //         f.formatterCfg.decimalCount = l.reserveDecimalCount
        //         f.formatterCfg.thousandSeparator = false
        //       }
        //       res = valueFormatter(param.value, f.formatterCfg)
        //     } else if (chart.type === 'word-cloud') {
        //       obj = { name: param.text, value: param.value }
        //       for (let i = 0; i < yAxis.length; i++) {
        //         const f = yAxis[i]
        //         if (f.formatterCfg) {
        //           res = valueFormatter(param.value, f.formatterCfg)
        //         } else {
        //           res = valueFormatter(param.value, formatterItem)
        //         }
        //       }
        //     } else if (chart.type.includes('treemap')) {
        //       obj = { name: param.name, value: param.value }
        //       for (let i = 0; i < yAxis.length; i++) {
        //         const f = yAxis[i]
        //         if (f.formatterCfg) {
        //           res = valueFormatter(param.value, f.formatterCfg)
        //         } else {
        //           res = valueFormatter(param.value, formatterItem)
        //         }
        //       }
        //     } else if (includesAny(chart.type, 'pie', 'funnel')) {
        //       obj = { name: param.field, value: param.value }
        //       for (let i = 0; i < yAxis.length; i++) {
        //         const f = yAxis[i]
        //         if (f.formatterCfg) {
        //           res = valueFormatter(param.value, f.formatterCfg)
        //         } else {
        //           res = valueFormatter(param.value, formatterItem)
        //         }
        //       }
        //     } else if (
        //       includesAny(chart.type, 'bar', 'scatter', 'radar', 'area') &&
        //       !chart.type.includes('group')
        //     ) {
        //       obj = { name: param.category, value: param.value }
        //       for (let i = 0; i < yAxis.length; i++) {
        //         const f = yAxis[i]
        //         if (f.name === param.category) {
        //           if (f.formatterCfg) {
        //             res = valueFormatter(param.value, f.formatterCfg)
        //           } else {
        //             res = valueFormatter(param.value, formatterItem)
        //           }
        //           break
        //         }
        //       }
        //     } else if (chart.type === 'line') {
        //       obj = { name: param.category, value: param.value }
        //       const xAxisExt = JSON.parse(chart.xaxisExt)
        //       for (let i = 0; i < yAxis.length; i++) {
        //         const f = yAxis[i]
        //         if (f.name === param.category || (yAxis.length && xAxisExt.length)) {
        //           if (f.formatterCfg) {
        //             res = valueFormatter(param.value, f.formatterCfg)
        //           } else {
        //             res = valueFormatter(param.value, formatterItem)
        //           }
        //           break
        //         }
        //       }
        //     } else if (chart.type.includes('group')) {
        //       if (chart.type === 'bar-group') {
        //         obj = { name: param.category, value: param.value }
        //       } else {
        //         let name = ''
        //         if (param.group) {
        //           name = param.name + '-'
        //         }
        //         if (param.category) {
        //           name += param.category
        //         }
        //         obj = { name: name, value: param.value }
        //       }
        //       for (let i = 0; i < yAxis.length; i++) {
        //         const f = yAxis[i]
        //         if (f.formatterCfg) {
        //           res = valueFormatter(param.value, f.formatterCfg)
        //         } else {
        //           res = valueFormatter(param.value, formatterItem)
        //         }
        //       }
        //     } else {
        //       res = param.value
        //     }
        //     obj.value = res === null ? '' : res
        //     return obj
        //   }
        // }
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
                  fill: a.nameTextStyle.color,
                  fontSize: a.nameTextStyle.fontSize
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
        const label = a.axisLabel.show
          ? {
              rotate: (a.axisLabel.rotate * Math.PI) / 180,
              style: {
                fill: a.axisLabel.color,
                fontSize: parseInt(a.axisLabel.fontSize)
              }
              // formatter: function (value) {
              //   if (chart.type.includes('horizontal')) {
              //     if (!a.axisLabelFormatter) {
              //       return valueFormatter(value, formatterItem)
              //     } else {
              //       return valueFormatter(value, a.axisLabelFormatter)
              //     }
              //   } else {
              //     return value
              //   }
              // }
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

        // // 轴值设置
        // delete axis.minLimit
        // delete axis.maxLimit
        // delete axis.tickCount
        // const axisValue = a.axisValue
        // if (chart.type.includes('horizontal')) {
        //   if (axisValue && !axisValue.auto) {
        //     axisValue.min && (axis.minLimit = parseFloat(axisValue.min))
        //     axisValue.max && (axis.maxLimit = parseFloat(axisValue.max))
        //     axisValue.splitCount && (axis.tickCount = parseFloat(axisValue.splitCount))
        //   }
        // }
      } else {
        axis = false
      }
    }
  }
  return axis
}
// yAxis
export function getYAxis(chart: Chart) {
  let axis = {}
  let customStyle: CustomStyle
  if (chart.customStyle) {
    customStyle = parseJson(chart.customStyle)
    // legend
    if (customStyle.yAxis) {
      const a = JSON.parse(JSON.stringify(customStyle.yAxis))
      if (a.show) {
        const title =
          a.name && a.name !== ''
            ? {
                text: a.name,
                style: {
                  fill: a.nameTextStyle.color,
                  fontSize: a.nameTextStyle.fontSize
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
        const axisCfg = a.axisLine ? a.axisLine : DEFAULT_YAXIS_STYLE.axisLine
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
        const label = a.axisLabel.show
          ? {
              rotate: (a.axisLabel.rotate * Math.PI) / 180,
              style: {
                fill: a.axisLabel.color,
                fontSize: parseInt(a.axisLabel.fontSize)
              }
              // formatter: function (value) {
              //   if (chart.type === 'waterfall') {
              //     return value
              //   } else {
              //     if (!chart.type.includes('horizontal')) {
              //       if (!a.axisLabelFormatter) {
              //         return valueFormatter(value, formatterItem)
              //       } else {
              //         return valueFormatter(value, a.axisLabelFormatter)
              //       }
              //     } else {
              //       return value
              //     }
              //   }
              // }
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

        // 轴值设置
        // delete axis.minLimit
        // delete axis.maxLimit
        // delete axis.tickCount
        // const axisValue = a.axisValue
        // if (!chart.type.includes('horizontal')) {
        //   if (axisValue && !axisValue.auto) {
        //     axisValue.min && (axis.minLimit = parseFloat(axisValue.min))
        //     axisValue.max && (axis.maxLimit = parseFloat(axisValue.max))
        //     axisValue.splitCount && (axis.tickCount = parseFloat(axisValue.splitCount))
        //   }
        // }
      } else {
        axis = false
      }
    }
  }
  return axis
}
// // yAxisExt
// export function getYAxisExt(chart) {
//   let axis = {}
//   let customStyle
//   if (chart.customStyle) {
//     customStyle = JSON.parse(chart.customStyle)
//     // legend
//     if (customStyle.yAxisExt) {
//       const a = JSON.parse(JSON.stringify(customStyle.yAxisExt))
//       if (a.show) {
//         const title =
//           a.name && a.name !== ''
//             ? {
//                 text: a.name,
//                 style: {
//                   fill: a.nameTextStyle.color,
//                   fontSize: parseInt(a.nameTextStyle.fontSize)
//                 },
//                 spacing: 8
//               }
//             : null
//         const grid = a.splitLine.show
//           ? {
//               line: {
//                 style: {
//                   stroke: a.splitLine.lineStyle.color,
//                   lineWidth: parseInt(a.splitLine.lineStyle.width)
//                 }
//               }
//             }
//           : null
//         const axisCfg = a.axisLine ? a.axisLine : DEFAULT_YAXIS_EXT_STYLE.axisLine
//         const axisLine = axisCfg.show
//           ? {
//               style: {
//                 stroke: axisCfg.lineStyle.color,
//                 lineWidth: parseInt(axisCfg.lineStyle.width)
//               }
//             }
//           : null
//         const tickLine = axisCfg.show
//           ? {
//               style: {
//                 stroke: axisCfg.lineStyle.color
//               }
//             }
//           : null
//         const label = a.axisLabel.show
//           ? {
//               rotate: (parseInt(a.axisLabel.rotate) * Math.PI) / 180,
//               style: {
//                 fill: a.axisLabel.color,
//                 fontSize: parseInt(a.axisLabel.fontSize)
//               },
//               formatter: function (value) {
//                 if (chart.type === 'waterfall') {
//                   return value
//                 } else {
//                   if (!chart.type.includes('horizontal')) {
//                     if (!a.axisLabelFormatter) {
//                       return valueFormatter(value, formatterItem)
//                     } else {
//                       return valueFormatter(value, a.axisLabelFormatter)
//                     }
//                   } else {
//                     return value
//                   }
//                 }
//               }
//             }
//           : null
//
//         axis = {
//           position: transAxisPosition(chart, a),
//           title: title,
//           grid: grid,
//           label: label,
//           line: axisLine,
//           tickLine: tickLine
//         }
//
//         // 轴值设置
//         delete axis.minLimit
//         delete axis.maxLimit
//         delete axis.tickCount
//         const axisValue = a.axisValue
//         if (!chart.type.includes('horizontal')) {
//           if (axisValue && !axisValue.auto) {
//             axisValue.min && (axis.minLimit = parseFloat(axisValue.min))
//             axisValue.max && (axis.maxLimit = parseFloat(axisValue.max))
//             axisValue.splitCount && (axis.tickCount = parseFloat(axisValue.splitCount))
//           }
//         }
//       } else {
//         axis = false
//       }
//     }
//   }
//   return axis
// }
//
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
    const dynamicLineArr = senior.assistLine.filter(ele => ele.field === '1')
    const l = chart.data.dynamicAssistLines
    const dynamicLines = []
    for (let i = 0; i < l.length; i++) {
      for (let j = 0; j < dynamicLineArr.length; j++) {
        if (l[i].fieldId === dynamicLineArr[j].fieldId) {
          dynamicLines.push(l[i])
        }
      }
    }
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
    const dynamicLines = chart.data.dynamicAssistLines
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

export function setGradientColor(rawColor, show = false, angle = 0) {
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
  return {
    showComponent: tooltip.show,
    domStyles: {
      'l7plot-tooltip': {
        'background-color': tooltip.backgroundColor,
        'font-size': `${tooltip.textStyle.fontSize}px`
      },
      'l7plot-tooltip__name': {
        color: tooltip.textStyle.color
      },
      'l7plot-tooltip__value': {
        color: tooltip.textStyle.color
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
