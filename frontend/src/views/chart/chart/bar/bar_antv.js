import { Column, Bar } from '@antv/g2plot'
import {
  getTheme,
  getLabel,
  getTooltip,
  getLegend,
  getXAxis,
  getYAxis,
  getPadding,
  getSlider,
  getAnalyse
} from '@/views/chart/chart/common/common_antv'

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
  const data = chart.data.datas
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
        type: 'element-active', cfg: {
          start: [{ trigger: 'element:mouseenter', action: ['element-highlight:highlight', 'element-active:reset', 'cursor:pointer'] }],
          end: [{ trigger: 'element:mouseleave', action: ['element-highlight:reset', 'element-active:reset', 'cursor:default'] }]
        }
      },
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
  const data = chart.data.datas
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
        type: 'element-active', cfg: {
          start: [{ trigger: 'element:mouseenter', action: ['element-highlight:highlight', 'element-active:reset', 'cursor:pointer'] }],
          end: [{ trigger: 'element:mouseleave', action: ['element-highlight:reset', 'element-active:reset', 'cursor:default'] }]
        }
      },
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

  // 开始渲染
  if (plot) {
    plot.destroy()
  }
  plot = new Bar(container, options)

  plot.off('interval:click')
  plot.on('interval:click', action)

  return plot
}
