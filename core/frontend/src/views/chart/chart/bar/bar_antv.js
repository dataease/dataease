import {Column, Bar, BidirectionalBar, Mix} from '@antv/g2plot'
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
  configPlotTooltipEvent,
  configAxisLabelLengthLimit
} from '@/views/chart/chart/common/common_antv'
import { antVCustomColor, getColors, handleEmptyDataStrategy, hexColorToRGBA, handleStackSort } from '@/views/chart/chart/util'
import { cloneDeep, find, groupBy, each } from 'lodash-es'
import { formatterItem, valueFormatter } from '@/views/chart/chart/formatter'
import {
  calculateMinMax,
  calculateMovingAverage,
  configXAxis, configYAxis,
  configBasicStyle,
  configTooltip,
  registerEvent, customConfigEmptyDataStrategy
} from "@/views/chart/chart/bar/stock_line_util";

export function baseBarOptionAntV(container, chart, action, isGroup, isStack) {
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
      start: xScale => {
        const ratio = xScale.ticks ? 1 / xScale.ticks.length : 1
        const x = xScale.scale(forecastData[0].dimension) - ratio / 2
        return [`${x * 100}%`, '0%']
      },
      end: (xScale) => {
        const ratio = xScale.ticks ? 1 / xScale.ticks.length : 1
        const x = xScale.scale(forecastData[forecastData.length - 1].dimension) + ratio / 2
        return [`${x * 100}%`, '100%']
      }
    })
  }
  // total label
  if (chart.type === 'bar-stack' && customAttr.label.showTotal) {
    const yAxis = JSON.parse(chart.yaxis)
    const formatterCfg = yAxis?.[0]?.formatterCfg ?? formatterItem
    each(groupBy(data, 'field'), (values, key) => {
      const total = values.reduce((a, b) => a + b.value, 0)
      const value = valueFormatter(total, formatterCfg)
      analyse.push({
        type: 'text',
        position: [key, total],
        content: `${value}`,
        style: { textAlign: 'center', fontSize: customAttr.label.totalFontSize, fill: customAttr.label.totalColor },
        offsetY: -(parseInt(customAttr.label.totalFontSize) / 2)
      })
    })
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
  // 处理堆叠排序
  handleStackSort(chart, data)

  // 开始渲染
  const plot = new Column(container, options)

  plot.on('interval:click', action)
  // 处理 tooltip 被其他视图遮挡
  configPlotTooltipEvent(chart, plot)
  return plot
}

export function hBaseBarOptionAntV(container, chart, action, isGroup, isStack) {
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
  // 处理堆叠排序
  handleStackSort(chart, data)
  // 处理空值
  if (chart.senior) {
    let emptyDataStrategy = JSON.parse(chart.senior)?.functionCfg?.emptyDataStrategy
    if (!emptyDataStrategy) {
      emptyDataStrategy = 'breakLine'
    }
    handleEmptyDataStrategy(emptyDataStrategy, chart, data, options)
  }

  const plot = new Bar(container, options)

  plot.on('interval:click', action)
  // 处理 tooltip 被其他视图遮挡
  configPlotTooltipEvent(chart, plot)
  // 处理纵轴标签长度限制
  configAxisLabelLengthLimit(chart, plot)
  return plot
}

export function timeRangeBarOptionAntV(container, chart, action) {
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

  const plot = new Bar(container, options)

  plot.off('interval:click')
  plot.on('interval:click', action)
  // 处理 tooltip 被其他视图遮挡
  configPlotTooltipEvent(chart, plot)
  return plot
}

export function baseBidirectionalBarOptionAntV(container, chart, action, isGroup, isStack) {
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
  const plot = new BidirectionalBar(container, options)

  plot.on('interval:click', action)
  // 处理 tooltip 被其他视图遮挡
  configPlotTooltipEvent(chart, plot)
  return plot
}

export function stockLineOptionAntV(container, chart, action) {
  if (!chart.data?.data?.length) {
    return
  }
  const xAxis = JSON.parse(chart.xaxis)
  const yAxis = JSON.parse(chart.yaxis)
  if (yAxis.length !== 4) {
    return
  }
  const theme = getTheme(chart)
  const legend = getLegend(chart)
  const basicStyle = JSON.parse(chart.customAttr).color
  let colors = []
  const alpha = basicStyle.alpha
  basicStyle.colors.forEach(ele => {
    colors.push(hexColorToRGBA(ele, alpha))
  })
  // custom color
  colors = antVCustomColor(chart)
  const data = cloneDeep(chart.data?.tableRow ?? [])

  // 时间字段
  const xAxisDataeaseName = xAxis[0].dataeaseName
  const averages = [5, 10, 20, 60, 120, 180]
  const legendItems = [
    {
      name: '日K',
      value: 'k',
      marker: {
        symbol: (x, y, r) => {
          const width = r * 1
          const height = r
          return [
            // 矩形框
            ['M', x - width - 1 / 2, y - height / 2],
            ['L', x + width + 1 / 2, y - height / 2],
            ['L', x + width + 1 / 2, y + height / 2],
            ['L', x - width - 1 / 2, y + height / 2],
            ['Z'],
            // 中线
            ['M', x, y + 10 / 2],
            ['L', x, y - 10 / 2]
          ]
        },
        style: { fill: 'red', stroke: 'red', lineWidth: 2 }
      }
    }
  ]
  // 计算均线数据
  const averagesLineData = new Map()
  averages.forEach(item => {
    averagesLineData.set('ma' + item, calculateMovingAverage(data, item, chart))
  })

  // 将均线数据设置到主数据中
  data.forEach((item) => {
    const date = item[xAxisDataeaseName]
    for (const [key, value] of averagesLineData) {
      item[key] = value.find(m => m[xAxisDataeaseName] === date)?.value
    }
  })

  const averageLines = []
  let index = 0
  const start = 0.5
  const end = 1
  const startIndex = Math.floor(start * data.length)
  const endIndex = Math.ceil(end * data.length)
  const filteredData = data.slice(startIndex, endIndex)
  const { maxValue, minValue } = calculateMinMax(filteredData)
  for (const key of averagesLineData.keys()) {
    index++
    averageLines.push({
      type: 'line',
      top: true,
      options: {
        smooth: false,
        xField: xAxisDataeaseName,
        yField: key,
        color: colors[index - 1],
        xAxis: null,
        yAxis: {
          label: false,
          min: minValue,
          max: maxValue,
          grid: null,
          line: null
        },
        lineStyle: {
          lineWidth: 2
        }
      }
    })
    legendItems.push({
      name: key.toUpperCase(),
      value: key,
      marker: { symbol: 'hyphen', style: { stroke: colors[index - 1], lineWidth: 2 } }
    })
  }
  const axis =JSON.parse(chart.xaxis) ?? []
  let dateFormat
  const dateSplit = axis[0]?.datePattern === 'date_split' ? '/' : '-'
  switch (axis[0]?.dateStyle) {
    case 'y':
      dateFormat = 'YYYY'
      break
    case 'y_M':
      dateFormat = 'YYYY' + dateSplit + 'MM'
      break
    case 'y_M_d':
      dateFormat = 'YYYY' + dateSplit + 'MM' + dateSplit + 'DD'
      break
    case 'y_M_d_H':
      dateFormat = 'YYYY' + dateSplit + 'MM' + dateSplit + 'DD' + ' HH'
      break
    case 'y_M_d_H_m':
      dateFormat = 'YYYY' + dateSplit + 'MM' + dateSplit + 'DD' + ' HH:mm'
      break
    case 'y_M_d_H_m_s':
      dateFormat = 'YYYY' + dateSplit + 'MM' + dateSplit + 'DD' + ' HH:mm:ss'
      break
    default:
      dateFormat = 'YYYY-MM-dd HH:mm:ss'
  }
  let option =  {
    data,
    theme,
    appendPadding: getPadding(chart),
    slider: {
      start: 0.5,
      end: 1
    },
    plots: [
      ...averageLines,
      {
        type: 'stock',
        top: true,
        options: {
          meta: {
            [xAxisDataeaseName]: {
              mask: dateFormat
            }
          },
          stockStyle: {
            stroke: 'black',
            lineWidth: 0.5
          },
          xField: xAxisDataeaseName,
          yField: [
            yAxis[0].dataeaseName,
            yAxis[1].dataeaseName,
            yAxis[2].dataeaseName,
            yAxis[3].dataeaseName
          ],
          legend: !legend?false:{
            position: 'top',
            custom: true,
            items: legendItems
          },
          fallingFill: hexColorToRGBA('#ef5350', alpha),
          risingFill: hexColorToRGBA('#26a69a', alpha),
        }
      }
    ]
  }
  option = configBasicStyle(chart, option)
  option = configXAxis(chart, option)
  option = configYAxis(chart, option)
  option = configTooltip(chart, option)
  option = customConfigEmptyDataStrategy(chart,option)
  const plot = new Mix(container, option)
  registerEvent(data, plot, averagesLineData)
  plot.on('schema:click', evt => {
    const selectSchema = evt.data.data[xAxisDataeaseName]
    const paramData = cloneDeep(chart.data?.data ?? [])
    const selectData = paramData.filter(item => item.field === selectSchema)
    const quotaList = []
    selectData.forEach(item => {
      quotaList.push({ ...item.quotaList[0], value: item.value })
    })
    if (selectData.length) {
      const param = {
        x: evt.x,
        y: evt.y,
        data: {
          data: {
            ...evt.data.data,
            value: quotaList[0].value,
            name: selectSchema,
            dimensionList: selectData[0].dimensionList,
            quotaList: quotaList
          }
        }
      }
      action(param)
    }
  })
  return plot
}
