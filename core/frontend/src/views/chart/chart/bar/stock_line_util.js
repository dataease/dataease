import {getXAxis, getYAxis} from '@/views/chart/chart/common/common_antv'
import { valueFormatter } from '@/views/chart/chart/formatter'
import {cloneDeep} from "lodash";
import {handleEmptyDataStrategy} from "@/views/chart/chart/util";

/**
 * 计算收盘价平均值
 * @param data
 * @param dayCount
 * @param chart
 */
export const calculateMovingAverage = (data, dayCount, chart) => {
  const xAxis = JSON.parse(chart.xaxis)
  const yAxis = JSON.parse(chart.yaxis)
  // 时间字段
  const xAxisDataeaseName = xAxis[0].dataeaseName
  // 收盘价字段
  const yAxisDataeaseName = yAxis[1].dataeaseName
  const result = []
  for (let i = 0; i < data.length; i++) {
    if (i < dayCount) {
      result.push({
        [xAxisDataeaseName]: data[i][xAxisDataeaseName],
        value: null
      })
    } else {
      const sum = data
        .slice(i - dayCount + 1, i + 1)
        .reduce((sum, item) => sum + item[yAxisDataeaseName], 0)
      result.push({
        [xAxisDataeaseName]: data[i][xAxisDataeaseName],
        value: parseFloat((sum / dayCount).toFixed(3))
      })
    }
  }
  return result
}

/**
 * 获取数据集合中对象属性值的最大最小值
 * @param data
 */
export const calculateMinMax = data => {
  return data.reduce(
    (acc, current) => {
      // 获取 current 对象的所有属性值
      const values = Object.values(current)
      // 过滤出数字值
      const numericValues = values.filter(value => typeof value === 'number') ?? []
      // 找到 current 对象的数字属性值中的最大值和最小值
      // 如果存在数字值，则计算当前对象的最大值和最小值
      if (numericValues.length > 0) {
        const currentMax = Math.max(...numericValues)
        const currentMin = Math.min(...numericValues)
        // 更新全局最大值和最小值
        acc.maxValue = Math.max(acc.maxValue, currentMax)
        acc.minValue = Math.min(acc.minValue, currentMin)
      }
      return acc
    },
    { maxValue: Number.NEGATIVE_INFINITY, minValue: Number.POSITIVE_INFINITY }
  )
}

/**
 * 注册图表事件
 * @param data
 * @param plot
 * @param averagesLineData
 */
export const registerEvent = (data, plot, averagesLineData) => {
  // 监听图例点击事件，显示隐藏
  let risingVisible = true
  plot.on('legend-item:click', evt => {
    const { value } = evt.target.get('delegateObject').item
    if (value === 'k') {
      risingVisible = !risingVisible
      plot.chart.geometries.forEach(geom => {
        if (geom.type === 'schema') {
          geom.changeVisible(risingVisible)
        }
      })
    } else {
      const lines = plot.chart.geometries.filter(item => item.type === 'line')
      const points = plot.chart.geometries.filter(item => item.type === 'point')
      let lineIndex = 0
      for (const key of averagesLineData.keys()) {
        lineIndex++
        if (key === value) {
          lines[lineIndex - 1].changeVisible(!lines[lineIndex - 1].visible)
          points[lineIndex - 1].changeVisible(!points[lineIndex - 1].visible)
        }
      }
    }
  })
  // 监听图表渲染事件
  plot.on('afterrender', e => {
    let first = false
    if (plot.chart.options.slider.start === 0.5 && plot.chart.options.slider.end === 1) {
      first = true
    }
    if (e.view?.options?.scales) {
      const startIndex = Math.floor(0.5 * data.length)
      const endIndex = Math.ceil(1 * data.length)
      const filteredData = data.slice(startIndex, endIndex)
      const { maxValue, minValue } = calculateMinMax(
        first ? filteredData : e.view.filteredData
      )
      const a = e.view.options.scales
      Object.keys(a).forEach(item => {
        if (a[item].max) {
          a[item].max = maxValue
        }
        if (a[item].min) {
          a[item].min = minValue
        }
      })
    }
  })
  // 监听图例组点击事件，设置缩放
  plot.on('legend-item-group:click', e => {
    if (e.view?.options?.scales) {
      const { maxValue, minValue } = calculateMinMax(e.view.filteredData)
      const a = e.view.options.scales
      Object.keys(a).forEach(item => {
        if (a[item].max) {
          a[item].max = maxValue
        }
        if (a[item].min) {
          a[item].min = minValue
        }
      })
    }
  })
  // 监听滑块事件，设置缩放
  plot.on('slider:valuechanged', e => {
    const start = e.gEvent.currentTarget.cfg.component.cfg.start
    const end = e.gEvent.currentTarget.cfg.component.cfg.end
    plot.chart.options.slider.start = start
    plot.chart.options.slider.end = end
    const startIndex = Math.floor(start * data.length)
    const endIndex = Math.ceil(end * data.length)
    const filteredData = data.slice(startIndex, endIndex)
    const { maxValue, minValue } = calculateMinMax(filteredData)
    const a = e.view.options.scales
    Object.keys(a).forEach(item => {
      if (a[item].max) {
        a[item].max = maxValue
      }
      if (a[item].min) {
        a[item].min = minValue
      }
    })
  })
}

export const configBasicStyle = (chart, options) => {
  // size
  const customAttr = JSON.parse(chart.customAttr)
  const s = JSON.parse(JSON.stringify(customAttr.size))
  const smooth = s.lineSmooth
  const point = {
    size: s.lineSymbolSize,
    shape: s.lineSymbol
  }
  const lineStyle = {
    lineWidth: s.lineWidth
  }
  const plots = []
  options.plots.forEach(item => {
    if (item.type === 'line') {
      plots.push({ ...item, options: { ...item.options, smooth, point, lineStyle } })
    }
    if (item.type === 'stock') {
      plots.push({ ...item })
    }
  })
  return {
    ...options,
    plots
  }
}

export const configTooltip = (chart, options)=> {
  const tooltipAttr = JSON.parse(chart.customAttr).tooltip
  const newPlots = []
  const linePlotList = options.plots.filter(item => item.type === 'line')
  linePlotList.forEach(item => {
    newPlots.push(item)
  })
  const stockPlot = options.plots.filter(item => item.type === 'stock')[0]
  if (!tooltipAttr.show) {
    const stockOption = {
      ...stockPlot.options,
      tooltip: {
        showContent: false
      }
    }
    newPlots.push({ ...stockPlot, options: stockOption })
    return {
      ...options,
      plots: newPlots
    }
  }

  const showFiled = chart.data.fields
  const yAxis = cloneDeep(JSON.parse(chart.yaxis))
  const customTooltipItems = originalItems => {
    const formattedItems = originalItems.map(item => {
      const fieldObj = showFiled.find(q => q.dataeaseName === item.name)
      const displayName = fieldObj?.chartShowName || fieldObj?.name || item.name
      const formattedName = displayName.startsWith('ma') ? displayName.toUpperCase() : displayName
      if(!yAxis[0].formatterCfg){
        yAxis[0].formatterCfg = {
          type: 'value', // auto,value,percent
          unit: 1, // 换算单位
          suffix: '', // 单位后缀
          decimalCount: 3, // 小数位数
          thousandSeparator: true// 千分符
        }
      }
      if(yAxis[0].formatterCfg.type === 'auto'){
        yAxis[0].formatterCfg.type = 'value'
        yAxis[0].formatterCfg.decimalCount = 3
      }
      const formattedValue = valueFormatter(item.value, yAxis[0].formatterCfg)
      return {
        ...item,
        name: formattedName,
        value: formattedValue,
        color: item.color
      }
    })

    const hasKLine = formattedItems.some(item => !item.name.startsWith('MA'))
    const kLines = formattedItems.filter(item => !item.name.startsWith('MA'))
    return hasKLine
      ? [
        { name: '日K', value: '', marker: true, color: kLines[0]?.color },
        ...kLines,
        ...formattedItems.filter(item => item.name.startsWith('MA'))
      ]
      : formattedItems
  }
  const formatTooltipItem = (item) => {
    const size = item.name.startsWith('MA') || !item.value ? 10 : 5
    const markerMarginRight = item.name.startsWith('MA') || !item.value ? 5 : 9
    const markerMarginLeft = item.name.startsWith('MA') || !item.value ? 0 : 2
    return `
        <li style="display: flex; align-items: center; margin-bottom: 10px;">
          <div>
            <span
              style="
                background-color: ${item.color};
                width: ${size}px;
                height: ${size}px;
                border-radius: 50%;
                display: inline-block;
                margin-right: ${markerMarginRight}px;
                margin-left: ${markerMarginLeft}px;
              "></span>
          </div>
          <div style="display: flex; justify-content: space-between; width: 100%;">
            <span style="margin-right: 15px;">${item.name}</span>
            <span>${item.name.startsWith('MA') && item.value === '0' ? '-' : item.value}</span>
          </div>
        </li>
      `
  }
  const generateCustomTooltipContent = (title, items) => {
    return `
        <div style="padding: 10px 0;">
          <div style="margin-bottom: 10px;">${title}</div>
          <ul style="list-style: none; padding: 0;">
            ${items.map(formatTooltipItem).join('')}
          </ul>
        </div>
      `
  }
  const stockOption = {
    ...stockPlot.options,
    tooltip: {
      showMarkers: true,
      showCrosshairs: true,
      showNil: true,
      crosshairs: {
        follow: true,
        text: (axisType, value, data) => {
          if (axisType === 'y') {
            return { content: value ? value.toFixed(0) : value }
          }
          return { content: data[0].title, position: 'end' }
        }
      },
      showContent: true,
      customItems: customTooltipItems,
      customContent: generateCustomTooltipContent
    }
  }
  newPlots.push({ ...stockPlot, options: stockOption })
  return {
    ...options,
    plots: newPlots
  }
}

export const configXAxis = (chart, options) => {
  const xAxisOptions = getXAxis(chart)
  if (!xAxisOptions) {
    options.plots.forEach(item => {
      if(item.type === 'stock'){
        item.options.xAxis = false
      }
    })
    return options
  }
  const newPlots = []
  const linePlotList = options.plots.filter(item => item.type === 'line')

  const stockPlot = options.plots.filter(item => item.type === 'stock')[0]
  const newStockPlot = {
    ...stockPlot,
    options: {
      ...stockPlot.options,
      xAxis: xAxisOptions
        ? {
          ...stockPlot.options['xAxis'],
          ...xAxisOptions
        }
        : {
          label: false,
          line: null
        }
    }
  }
  newPlots.push(newStockPlot)
  linePlotList.forEach(item => {
    newPlots.push(item)
  })
  return {
    ...options,
    plots: newPlots
  }
}

export const configYAxis = (chart, options) => {
  const yAxisOptions = getYAxis(chart)
  if (!yAxisOptions) {
    options.plots.forEach(item => {
      if(item.type === 'stock'){
        item.options.yAxis = false
      }
    })
    return options
  }
  const yAxis = JSON.parse(chart.yaxis)
  const newPlots = []
  const linePlotList = options.plots.filter(item => item.type === 'line')

  const stockPlot = options.plots.filter(item => item.type === 'stock')[0]
  let label = false
  if (yAxisOptions.label) {
    label = {
      ...yAxisOptions.label
    }
  }
  const newStockPlot = {
    ...stockPlot,
    options: {
      ...stockPlot.options,
      yAxis: label
        ? {
          ...stockPlot.options['yAxis'],
          ...yAxisOptions,
          label
        }
        : {
          label,
          grid: null,
          line: null
        }
    }
  }
  newPlots.push(newStockPlot)
  linePlotList.forEach(item => {
    newPlots.push(item)
  })
  return {
    ...options,
    plots: newPlots
  }
}

export const customConfigEmptyDataStrategy = (chart, options) => {
  const { data } = options
  if (!data?.length) {
    return options
  }
  const strategy = JSON.parse(chart.senior)?.functionCfg?.emptyDataStrategy
  if (strategy === 'ignoreData') {
    for (let i = data.length - 1; i >= 0; i--) {
      const item = data[i]
      Object.keys(item).forEach(key => {
        if (key.startsWith('C_') && item[key] === null) {
          data.splice(i, 1)
        }
      })
    }
  }
  const updateValues = (strategy, data) => {
    data.forEach(obj => {
      Object.keys(obj).forEach(key => {
        if (key.startsWith('C_') && obj[key] === null) {
          obj[key] = strategy === 'breakLine' ? null : 0
        }
      })
    })
  }
  if (strategy === 'breakLine' || strategy === 'setZero') {
    updateValues(strategy, data)
  }
  return options
}
