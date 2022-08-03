import { Column, Bar } from '@antv/g2plot'
import { hexColorToRGBA } from '../util.js'
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

function sortDatas(data) {
  var sort1 = JSON.parse(data.xaxis)
  var sort2 = sort1[0].sort
  // console.log(sort1.sort)
  let isSort
  if (sort2 === undefined) {
    isSort = false
  } else if (sort2 === 'asc') {
    isSort = false
  } else if (sort2 === 'desc') {
    isSort = true
  } else {
    isSort = false
  }
  var datas1 = JSON.parse(JSON.stringify(data))
  var datas = datas1.data.datas
  const sorts = {
    '一': 1,
    '二': 2,
    '三': 3,
    '四': 4,
    '五': 5,
    '六': 6,
    '七': 7,
    '八': 8,
    '九': 9,
    '十': 10,
    '十一': 11,
    '十二': 12
  }
  if (sorts[datas[0].field.split('月')[0]]) {
    datas.map((item, index) => {
      item.id = sorts[item.field.split('月')[0]]
    })
    datas.sort((a, b) => {
      return isSort ? a.id - b.id : b.id - a.id
    })
    return datas
  }
  return datas.sort((a, b) => {
    return isSort ? b.field.split('月')[0] - a.field.split('月')[0] : a.field.split('月')[0] - b.field.split('月')[0]
  })
}
export function baseBarOptionAntV(plot, container, chart, action, isGroup, isStack, cstyle = {}) {
  console.log('bar_antv,chart',chart)
  // theme
  const theme = getTheme(chart)
  // attr
  const label = getLabel(chart)
  const tooltip = getTooltip(chart)
  // style
  const legend = getLegend(chart)
  const xAxis = getXAxis(chart,cstyle)
  const yAxis = getYAxis(chart,cstyle)
  // data
  const data = chart.data.datas
  // const data = sortDatas(chart)
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
    ],
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
  console.log('antv,bar,,,',options)
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

export function hBaseBarOptionAntV(plot, container, chart, action, isGroup, isStack, cstyle = {}) {
  // theme
  const theme = getTheme(chart)
  // attr
  const label = getLabel(chart)
  const tooltip = getTooltip(chart)
  // style
  const legend = getLegend(chart)
  const xAxis = getXAxis(chart,cstyle)
  const yAxis = getYAxis(chart,cstyle)
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
