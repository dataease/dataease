import { Line, Area } from '@antv/g2plot'
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
  configPlotTooltipEvent,
  configPlotTrendLine
} from '@/views/chart/chart/common/common_antv'
import { antVCustomColor, handleEmptyDataStrategy } from '@/views/chart/chart/util'
import _ from 'lodash'

export function baseLineOptionAntV(container, chart, action) {
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
    meta: {
      category: {
        type: 'cat'
      }
    },
    point: {},
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
          start: [{ trigger: 'point:mousemove', action: 'tooltip:show' }],
          end: [{ trigger: 'point:mouseleave', action: 'tooltip:hide' }]
        }
      },
      {
        type: 'active-region', cfg: {
          start: [{ trigger: 'element:mousemove', action: 'active-region:show' }],
          end: [{ trigger: 'element:mouseleave', action: 'active-region:hide' }]
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
      options.smooth = s.lineSmooth
      options.point = {
        size: parseInt(s.lineSymbolSize),
        shape: s.lineSymbol
      }
      options.lineStyle = {
        lineWidth: parseInt(s.lineWidth)
      }
    }
  }
  // forecast
  if (chart.data?.forecastData?.length) {
    const { forecastData } = chart.data
    const templateData = data?.[data.length - 1]
    forecastData.forEach(item => {
      data.push({
        ...templateData,
        field: item.dimension,
        name: item.dimension,
        value: item.quota,
        forecast: true
      })
    })
    analyse.push({
      type: 'region',
      start: (xScale) => {
        if (forecastData.length > 1) {
          return [forecastData[0].dimension, 'min']
        }
        const ratio = xScale.ticks ? 1 / xScale.ticks.length : 1
        const x = xScale.scale(forecastData[0].dimension) - ratio / 2
        return [`${x * 100}%`, '0%']
      },
      end: (xScale) => {
        if (forecastData.length > 1) {
          return [forecastData[forecastData.length - 1].dimension, 'max']
        }
        const ratio = xScale.ticks ? 1 / xScale.ticks.length : 1
        const x = xScale.scale(forecastData[forecastData.length - 1].dimension) + ratio / 2
        return [`${x * 100}%`, '100%']
      }
    })
  }
  // custom color
  options.color = antVCustomColor(chart)
  // 处理空值
  if (chart.senior) {
    let emptyDataStrategy = JSON.parse(chart.senior)?.functionCfg?.emptyDataStrategy
    if (!emptyDataStrategy) {
      emptyDataStrategy = 'breakLine'
    }
    handleEmptyDataStrategy(emptyDataStrategy, chart, data, options)
  }
  const plot = new Line(container, options)

  plot.on('point:click', action)
  // 趋势线
  configPlotTrendLine(chart, plot)
  // 处理 tooltip 被其他视图遮挡
  configPlotTooltipEvent(chart, plot)
  return plot
}

export function baseAreaOptionAntV(container, chart, action, isStack) {
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
    point: {},
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
    isStack: isStack,
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
          start: [{ trigger: 'point:mousemove', action: 'tooltip:show' }],
          end: [{ trigger: 'point:mouseleave', action: 'tooltip:hide' }]
        }
      },
      {
        type: 'active-region', cfg: {
          start: [{ trigger: 'element:mousemove', action: 'active-region:show' }],
          end: [{ trigger: 'element:mouseleave', action: 'active-region:hide' }]
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
      options.smooth = s.lineSmooth
      options.point = {
        size: parseInt(s.lineSymbolSize),
        shape: s.lineSymbol
      }
      options.line = {
        style: {
          lineWidth: parseInt(s.lineWidth)
        }
      }
    }
  }
  // custom color
  options.color = antVCustomColor(chart)
  const areaColors = [...options.color, ...options.color]
  if (customAttr.color.gradient) {
    options.areaStyle = () => {
      const ele = areaColors.shift()
      if (ele) {
        return {
          fill: setGradientColor(ele, customAttr.color.gradient, 270)
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

  const plot = new Area(container, options)

  plot.on('point:click', action)
  // 处理 tooltip 被其他视图遮挡
  configPlotTooltipEvent(chart, plot)
  return plot
}
