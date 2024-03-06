import { Column, Bar, BidirectionalBar } from '@antv/g2plot'
import {
  getTheme,
  getLabel,
  getTooltip,
  getLegend,
  getXAxis,
  getYAxis,
  getPadding,
  getSlider,
  getAnalyse,
  setGradientColor,
  getMeta,
  configPlotTooltipEvent
} from '@/views/chart/chart/common/common_antv'
import { antVCustomColor, getColors, handleEmptyDataStrategy, hexColorToRGBA } from '@/views/chart/chart/util'
import { cloneDeep, find } from 'lodash-es'

export function baseBarOptionAntV(plot, container, chart, action, isGroup, isStack) {
  // theme
  const theme = getTheme(chart)
  // attr
  const label = getLabel(chart)
  const tooltip = getTooltip(chart)
  // style
  const legend = getLegend(chart)
  const xAxis = getXAxis(chart)
  const yAxis = getYAxis(chart)
  // data
  const data = cloneDeep(chart.data.data)
  // config
  const slider = getSlider(chart)
  const analyse = getAnalyse(chart)
  // options
  const options = {
    theme: theme,
    data: data,
    xField: 'field',
    yField: 'value',
    seriesField: 'category',
    appendPadding: getPadding(chart),
    label: label,
    tooltip: tooltip,
    legend: legend,
    xAxis: xAxis,
    yAxis: yAxis,
    slider: slider,
    annotations: analyse,
    brush: {
      enabled: true,
      isStartEnable: (context) => {
        // 按住 shift 键，才能开启交互
        if (context.event.gEvent.originalEvent?.shiftKey) {
          return true
        }
        return false
      }
    },
    interactions: [
      {
        type: 'legend-active', cfg: {
          start: [{ trigger: 'legend-item:mouseenter', action: ['element-active:reset'] }],
          end: [{ trigger: 'legend-item:mouseleave', action: ['element-active:reset'] }]
        }
      },
      {
        type: 'legend-filter', cfg: {
          start: [{ trigger: 'legend-item:click', action: ['list-unchecked:toggle', 'data-filter:filter', 'element-active:reset', 'element-highlight:reset'] }]
        }
      },
      {
        type: 'tooltip', cfg: {
          start: [{ trigger: 'interval:mousemove', action: 'tooltip:show' }],
          end: [{ trigger: 'interval:mouseleave', action: 'tooltip:hide' }]
        }
      },
      {
        type: 'active-region', cfg: {
          start: [{ trigger: 'interval:mousemove', action: 'active-region:show' }],
          end: [{ trigger: 'interval:mouseleave', action: 'active-region:hide' }]
        }
      }
    ]
  }
  // size
  let customAttr = {}
  if (chart.customAttr) {
    customAttr = JSON.parse(chart.customAttr)
    if (customAttr.size) {
      const s = JSON.parse(JSON.stringify(customAttr.size))
      if (s.barDefault) {
        delete options.marginRatio
      } else {
        options.marginRatio = s.barGap
      }
    }
  }
  // group
  if (isGroup) {
    options.isGroup = true
  } else {
    delete options.isGroup
  }
  // stack
  if (isStack) {
    options.isStack = true
  } else {
    delete options.isStack
  }

  if (chart.type === 'bar-group-stack') {
    options.groupField = 'group'
  } else {
    delete options.groupField
  }
  // 目前只有百分比堆叠柱状图需要这个属性，先直接在这边判断而不作为参数传过来
  options.isPercent = chart.type === 'percentage-bar-stack'
  // custom color
  options.color = antVCustomColor(chart)
  if (customAttr.color.gradient) {
    options.color = options.color.map((ele) => {
      return setGradientColor(ele, customAttr.color.gradient, 270)
    })
  }
  // 处理空值
  if (chart.senior) {
    let emptyDataStrategy = JSON.parse(chart.senior)?.functionCfg?.emptyDataStrategy
    if (!emptyDataStrategy) {
      emptyDataStrategy = 'ignoreData'
    }
    handleEmptyDataStrategy(emptyDataStrategy, chart, data, options)
  }

  // 开始渲染
  if (plot) {
    plot.destroy()
  }
  plot = new Column(container, options)

  plot.off('interval:click')
  plot.on('interval:click', action)
  // 处理 tooltip 被其他视图遮挡
  configPlotTooltipEvent(chart, plot)
  return plot
}

export function hBaseBarOptionAntV(plot, container, chart, action, isGroup, isStack) {
  // theme
  const theme = getTheme(chart)
  // attr
  const label = getLabel(chart)
  const tooltip = getTooltip(chart)
  // style
  const legend = getLegend(chart)
  const xAxis = getXAxis(chart)
  const yAxis = getYAxis(chart)
  // data
  const data = cloneDeep(chart.data.data)
  // config
  const slider = getSlider(chart)
  const analyse = getAnalyse(chart)
  // options
  const options = {
    theme: theme,
    data: data,
    xField: 'value',
    yField: 'field',
    seriesField: 'category',
    appendPadding: getPadding(chart),
    label: label,
    tooltip: tooltip,
    legend: legend,
    xAxis: xAxis,
    yAxis: yAxis,
    slider: slider,
    annotations: analyse,
    brush: {
      enabled: true,
      isStartEnable: (context) => {
        // 按住 shift 键，才能开启交互
        if (context.event.gEvent.originalEvent?.shiftKey) {
          return true
        }
        return false
      }
    },
    interactions: [
      {
        type: 'legend-active', cfg: {
          start: [{ trigger: 'legend-item:mouseenter', action: ['element-active:reset'] }],
          end: [{ trigger: 'legend-item:mouseleave', action: ['element-active:reset'] }]
        }
      },
      {
        type: 'legend-filter', cfg: {
          start: [{ trigger: 'legend-item:click', action: ['list-unchecked:toggle', 'data-filter:filter', 'element-active:reset', 'element-highlight:reset'] }]
        }
      },
      {
        type: 'tooltip', cfg: {
          start: [{ trigger: 'interval:mousemove', action: 'tooltip:show' }],
          end: [{ trigger: 'interval:mouseleave', action: 'tooltip:hide' }]
        }
      },
      {
        type: 'active-region', cfg: {
          start: [{ trigger: 'interval:mousemove', action: 'active-region:show' }],
          end: [{ trigger: 'interval:mouseleave', action: 'active-region:hide' }]
        }
      }
    ]
  }
  // size
  let customAttr = {}
  if (chart.customAttr) {
    customAttr = JSON.parse(chart.customAttr)
    if (customAttr.size) {
      const s = JSON.parse(JSON.stringify(customAttr.size))
      if (s.barDefault) {
        delete options.marginRatio
      } else {
        options.marginRatio = s.barGap
      }
    }
  }
  // group
  if (isGroup) {
    options.isGroup = true
  } else {
    delete options.isGroup
  }
  // stack
  if (isStack) {
    options.isStack = true
  } else {
    delete options.isStack
  }
  options.isPercent = chart.type.includes('percentage')
  // custom color
  options.color = antVCustomColor(chart)
  if (customAttr.color.gradient) {
    options.color = options.color.map((ele) => {
      return setGradientColor(ele, customAttr.color.gradient)
    })
  }
  // 处理空值
  if (chart.senior) {
    let emptyDataStrategy = JSON.parse(chart.senior)?.functionCfg?.emptyDataStrategy
    if (!emptyDataStrategy) {
      emptyDataStrategy = 'breakLine'
    }
    handleEmptyDataStrategy(emptyDataStrategy, chart, data, options)
  }

  // 开始渲染
  if (plot) {
    plot.destroy()
  }
  plot = new Bar(container, options)

  plot.off('interval:click')
  plot.on('interval:click', action)
// 处理 tooltip 被其他视图遮挡
  configPlotTooltipEvent(chart, plot)
  return plot
}

export function timeRangeBarOptionAntV(plot, container, chart, action) {
  const ifAggregate = !!chart.aggregate

  // theme
  const theme = getTheme(chart)
  // attr
  const label = getLabel(chart)
  if (label && !ifAggregate) {
    label.layout = [
      { type: 'interval-hide-overlap' },
      { type: 'limit-in-plot', cfg: { action: 'hide' }}
    ]
  }

  const tooltip = getTooltip(chart)
  // style
  const legend = getLegend(chart)
  const yAxis = getXAxis(chart)
  const xAxis = getYAxis(chart)
  // data
  const data = cloneDeep(chart.data.data)

  const isDate = !!chart.data.isDate

  const minTime = chart.data.minTime
  const maxTime = chart.data.maxTime

  const minNumber = chart.data.min
  const maxNumber = chart.data.max

  // config
  const slider = getSlider(chart)
  const analyse = getAnalyse(chart)

  data.forEach(d => {
    d.tempId = (Math.random() * 10000000).toString()
  })

  // options
  const options = {
    theme: theme,
    data: data,
    xField: 'values',
    yField: 'field',
    colorFiled: 'category',
    appendPadding: getPadding(chart),
    label: label,
    tooltip: tooltip,
    legend: legend,
    xAxis: xAxis,
    yAxis: yAxis,
    slider: slider,
    annotations: analyse,
    isRange: true,
    brush: {
      enabled: true,
      isStartEnable: (context) => {
        // 按住 shift 键，才能开启交互
        if (context.event.gEvent.originalEvent?.shiftKey) {
          return true
        }
        return false
      }
    },
    interactions: [
      {
        type: 'legend-active', cfg: {
          start: [{ trigger: 'legend-item:mouseenter', action: ['element-active:reset'] }],
          end: [{ trigger: 'legend-item:mouseleave', action: ['element-active:reset'] }]
        }
      },
      {
        type: 'legend-filter', cfg: {
          start: [{ trigger: 'legend-item:click', action: ['list-unchecked:toggle', 'data-filter:filter', 'element-active:reset', 'element-highlight:reset'] }]
        }
      },
      {
        type: 'tooltip', cfg: {
          start: [{ trigger: 'interval:mousemove', action: 'tooltip:show' }],
          end: [{ trigger: 'interval:mouseleave', action: 'tooltip:hide' }]
        }
      },
      {
        type: 'active-region', cfg: {
          start: [{ trigger: 'interval:mousemove', action: 'active-region:show' }],
          end: [{ trigger: 'interval:mouseleave', action: 'active-region:hide' }]
        }
      }
    ]
  }

  if (ifAggregate) {
    options.seriesField = 'category'
    delete options.isGroup
    delete options.isStack
  } else {
    options.isGroup = true
    options.isStack = true
  }

  if (isDate) {
    options.meta = {
      values: {
        type: 'time',
        min: minTime,
        max: maxTime,
        mask: 'YYYY-MM-DD HH:mm:ss'
      },
      tempId: {
        key: true
      }
    }
  } else {
    options.meta = {
      values: {
        min: minNumber,
        max: maxNumber,
        mask: 'YYYY-MM-DD HH:mm:ss'
      },
      tempId: {
        key: true
      }
    }
  }

  // size
  let customAttr = {}
  if (chart.customAttr) {
    customAttr = JSON.parse(chart.customAttr)
    if (customAttr.size) {
      const s = JSON.parse(JSON.stringify(customAttr.size))
      if (s.barDefault) {
        delete options.marginRatio
      } else {
        options.marginRatio = s.barGap
      }
    }
  }

  options.isPercent = chart.type.includes('percentage')
  // custom color
  if (ifAggregate) {
    options.color = antVCustomColor(chart)
    if (customAttr.color.gradient) {
      options.color = options.color.map((ele) => {
        return setGradientColor(ele, customAttr.color.gradient)
      })
    }
  } else {
    if (chart.customAttr) {
      // color
      if (customAttr.color) {
        const c = JSON.parse(JSON.stringify(customAttr.color))
        const customColors = getColors(chart, c.colors, false)
        options.color = function(obj) {
          const colorObj = find(customColors, (o) => {
            return o.name === obj.field
          })
          if (colorObj === undefined) {
            return undefined
          }
          const color = hexColorToRGBA(colorObj.color, c.alpha)
          if (customAttr.color.gradient) {
            return setGradientColor(color, customAttr.color.gradient)
          } else {
            return color
          }
        }
      }
    }
  }

  // 处理空值
  if (chart.senior) {
    let emptyDataStrategy = JSON.parse(chart.senior)?.functionCfg?.emptyDataStrategy
    if (!emptyDataStrategy) {
      emptyDataStrategy = 'breakLine'
    }
    handleEmptyDataStrategy(emptyDataStrategy, chart, data, options)
  }

  // 开始渲染
  if (plot) {
    plot.destroy()
  }
  plot = new Bar(container, options)

  plot.off('interval:click')
  plot.on('interval:click', action)
// 处理 tooltip 被其他视图遮挡
  configPlotTooltipEvent(chart, plot)
  return plot
}

export function baseBidirectionalBarOptionAntV(plot, container, chart, action, isGroup, isStack) {
  // theme
  const theme = getTheme(chart)
  // attr
  const label = getLabel(chart)
  const tooltip = getTooltip(chart)
  // style
  const legend = getLegend(chart)
  const xAxis = getXAxis(chart)
  const yAxis = getYAxis(chart)
  // 处理横轴标题方向不对
  yAxis?.title && (yAxis.title.autoRotate = false)
  // data
  const data = cloneDeep(chart.data.data)
  // options
  const options = {
    theme: theme,
    data: data,
    xField: 'field',
    yField: ['value', 'extValue'],
    appendPadding: getPadding(chart),
    label: label,
    tooltip: tooltip,
    legend: legend,
    xAxis: xAxis,
    yAxis: {
      value: yAxis,
      extValue: yAxis
    },
    interactions: [
      {
        type: 'legend-active', cfg: {
          start: [{ trigger: 'legend-item:mouseenter', action: ['element-active:reset'] }],
          end: [{ trigger: 'legend-item:mouseleave', action: ['element-active:reset'] }]
        }
      },
      {
        type: 'legend-filter', cfg: {
          start: [{ trigger: 'legend-item:click', action: ['list-unchecked:toggle', 'data-filter:filter', 'element-active:reset', 'element-highlight:reset'] }]
        }
      },
      {
        type: 'active-region', cfg: {
          start: [{ trigger: 'interval:mousemove', action: 'active-region:show' }],
          end: [{ trigger: 'interval:mouseleave', action: 'active-region:hide' }]
        }
      }
    ]
  }
  // size
  let customAttr = {}
  if (chart.customAttr) {
    customAttr = JSON.parse(chart.customAttr)
    if (customAttr.size) {
      const s = JSON.parse(JSON.stringify(customAttr.size))
      if (s.barDefault) {
        delete options.marginRatio
      } else {
        options.marginRatio = s.barGap
      }
    }
  }
  // custom color
  options.color = antVCustomColor(chart)
  if (customAttr.color.gradient) {
    options.color = options.color.map((ele, index) => {
      return setGradientColor(ele, customAttr.color.gradient, 180 - index * 180)
    })
  }
  // 处理空值
  if (chart.senior) {
    let emptyDataStrategy = JSON.parse(chart.senior)?.functionCfg?.emptyDataStrategy
    if (!emptyDataStrategy) {
      emptyDataStrategy = 'breakLine'
    }
    handleEmptyDataStrategy(emptyDataStrategy, chart, data, options)
  }

  // meta，处理类别轴数据类型为时间时排序失效
  const meta = getMeta(chart)
  if (meta) {
    options.meta = meta
  }
  // 开始渲染
  if (plot) {
    plot.destroy()
  }
  plot = new BidirectionalBar(container, options)

  plot.off('interval:click')
  plot.on('interval:click', action)
// 处理 tooltip 被其他视图遮挡
  configPlotTooltipEvent(chart, plot)
  return plot
}
