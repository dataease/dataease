import {
  configPlotTooltipEvent,
  getLabel,
  getLegend,
  getPadding,
  getTheme,
  getTooltip
} from '@/views/chart/chart/common/common_antv'

import { Pie, Rose } from '@antv/g2plot'
import { antVCustomColor } from '@/views/chart/chart/util'
import { configTopN } from '@/views/chart/chart/common/common_antv'
import { deepCopy } from '@/components/canvas/utils/utils'

export function basePieOptionAntV(container, chart, action) {
  // theme
  const theme = getTheme(chart)
  // attr
  const label = getLabel(chart)
  const tooltip = getTooltip(chart)
  // style
  const legend = getLegend(chart)
  // data
  const data = chart.data.data
  // groupData
  let hasXaxisExt = false
  try {
    const xaxisExtData = JSON.parse(chart.xaxisExt)
    hasXaxisExt = Array.isArray(xaxisExtData) && xaxisExtData.length > 0
  } catch (e) {
    console.error('Failed to parse xaxisExt:', e)
  }

  const sanitizeField = (str) => str?.replace('\n', ' ') ?? ''

  const groupData = hasXaxisExt
    ? data.reduce((acc, item) => {
        const type = item.dimensionList?.[0]?.value ?? null
        if (!type) return acc

        item.name = sanitizeField(item.name)
        item.field = sanitizeField(item.field)

        const lastGroup = acc[acc.length - 1]
        if (!lastGroup || lastGroup.field !== type) {
          acc.push({
            ...deepCopy(item),
            field: type,
            name: type,
            dimensionList: [item.dimensionList[0]]
          })
        } else {
          lastGroup.value += item.value
        }
        return acc
      }, [])
    : data
  // options
  const options = {
    theme: theme,
    data: groupData,
    angleField: 'value',
    colorField: 'field',
    appendPadding: getPadding(chart),
    label: label,
    tooltip: tooltip,
    legend: legend,
    animation: false,
    autoFit: false,
    pieStyle: {
      lineWidth: 0
    },
    statistic: {
      title: false,
      content: {
        style: {
          whiteSpace: 'pre-wrap',
          overflow: 'hidden',
          textOverflow: 'ellipsis'
        },
        content: ''
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
      options.radius = parseFloat(parseInt(s.pieOuterRadius) / 100)
      options.innerRadius = parseFloat(parseInt(s.pieInnerRadius) / 100)
    }
  }
  // custom color
  options.color = antVCustomColor(chart)
  // topN
  configTopN(data, chart)
  const plot = new Pie(container, options)

  // handle click
  let showDetail = false
  function handleItemClick(evt) {
    const field = evt?.data?.data?.field
    if (!field || !hasXaxisExt || showDetail) return

    // 过滤出当前分类下的所有子项
    const filteredData = data.filter(datum => {
      const type = datum.dimensionList?.[0]?.value
      return type === field
    })

    plot.chart.changeData(filteredData)
    showDetail = true
  }
  // click 
  plot.on('interval:click', (evt) => {
    handleItemClick(evt)
    action(evt)
  })
  // dbclick
  plot.on('interval:dblclick', () => {
    if (hasXaxisExt) {
      plot.chart.changeData(groupData)
      showDetail = false
    }
  })
  // 处理 tooltip 被其他视图遮挡
  configPlotTooltipEvent(chart, plot)
  return plot
}

export function basePieRoseOptionAntV(container, chart, action) {
  // theme
  const theme = getTheme(chart)
  // attr
  const label = getLabel(chart)
  const tooltip = getTooltip(chart)
  // style
  const legend = getLegend(chart)
  // data
  const data = chart.data.data
  // options
  const options = {
    theme: theme,
    data: data,
    xField: 'field',
    yField: 'value',
    seriesField: 'field',
    appendPadding: getPadding(chart),
    label: label,
    tooltip: tooltip,
    legend: legend,
    statistic: {
      title: false,
      content: {
        style: {
          whiteSpace: 'pre-wrap',
          overflow: 'hidden',
          textOverflow: 'ellipsis'
        },
        content: ''
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
      }
    ]
  }
  // size
  let customAttr = {}
  if (chart.customAttr) {
    customAttr = JSON.parse(chart.customAttr)
    if (customAttr.size) {
      const s = JSON.parse(JSON.stringify(customAttr.size))
      options.radius = parseFloat(parseInt(s.pieOuterRadius) / 100)
      options.innerRadius = parseFloat(parseInt(s.pieInnerRadius) / 100)
    }
  }
  // custom color
  options.color = antVCustomColor(chart)

  const plot = new Rose(container, options)

  plot.on('interval:click', action)
  // 处理 tooltip 被其他视图遮挡
  configPlotTooltipEvent(chart, plot)
  return plot
}
