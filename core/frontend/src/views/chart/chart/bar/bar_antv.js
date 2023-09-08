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
  getMeta
} from '@/views/chart/chart/common/common_antv'
import { antVCustomColor, handleEmptyDataStrategy } from '@/views/chart/chart/util'
import _ from 'lodash'

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
  const data = _.cloneDeep(chart.data.data)
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
  const data = _.cloneDeep(chart.data.data)
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
  const data = _.cloneDeep(chart.data.data)
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

  return plot
}
