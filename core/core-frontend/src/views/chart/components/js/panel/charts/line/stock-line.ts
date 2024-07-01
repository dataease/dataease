import {
  G2PlotChartView,
  G2PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/g2plot'
import { Mix, MixOptions } from '@antv/g2plot/esm/plots/mix'
import { flow, hexColorToRGBA, parseJson } from '@/views/chart/components/js/util'
import { LINE_EDITOR_PROPERTY_INNER } from '@/views/chart/components/js/panel/charts/line/common'
import { useI18n } from '@/hooks/web/useI18n'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { Options } from '@antv/g2plot/esm'

const { t } = useI18n()
const DEFAULT_DATA = []
/**
 * K线图
 */
export class StockLine extends G2PlotChartView<MixOptions, Mix> {
  properties: EditorProperty[] = [
    'background-overall-component',
    'basic-style-selector',
    'x-axis-selector',
    'y-axis-selector',
    'title-selector',
    'tooltip-selector',
    'function-cfg',
    'jump-set',
    'linkage'
  ]
  propertyInner = {
    ...LINE_EDITOR_PROPERTY_INNER,
    'function-cfg': ['emptyDataStrategy'],
    'y-axis-selector': [
      'name',
      'color',
      'fontSize',
      'position',
      'axisLabel',
      'axisLine',
      'splitLine',
      'axisLabelFormatter'
    ]
  }
  axis: AxisType[] = ['xAxis', 'yAxis', 'filter', 'extLabel', 'extTooltip']
  axisConfig = {
    ...this['axisConfig'],
    xAxis: {
      name: `日期 / ${t('chart.dimension')}`,
      limit: 1,
      type: 'd'
    },
    yAxis: {
      name: `开盘价-收盘价-最低价-最高价 / ${t('chart.quota')}`,
      limit: 4,
      type: 'q'
    }
  }

  /**
   * 计算收盘价平均值
   * @param data
   * @param dayCount
   * @param chart
   */
  calculateMovingAverage = (data, dayCount, chart) => {
    const xAxis = chart.xAxis
    const yAxis = chart.yAxis
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
          value: parseFloat((sum / dayCount).toFixed(2))
        })
      }
    }
    return result
  }

  /**
   * 获取数据集合中对象属性值的最大最小值
   * @param data
   */
  calculateMinMax = data => {
    return data.reduce(
      (acc, current) => {
        // 获取 current 对象的所有属性值
        const values = Object.values(current)
        // 过滤出数字值
        const numericValues: any[] = values.filter(value => typeof value === 'number')
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
  registerEvent = (data, plot, averagesLineData) => {
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
        const { maxValue, minValue } = this.calculateMinMax(
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
        const { maxValue, minValue } = this.calculateMinMax(e.view.filteredData)
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
      const { maxValue, minValue } = this.calculateMinMax(filteredData)
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

  drawChart(drawOptions: G2PlotDrawOptions<Mix>): Mix {
    const { chart, action, container } = drawOptions
    if (!chart.data.data?.length) {
      return
    }
    const xAxis = chart.xAxis
    const yAxis = chart.yAxis
    if (yAxis.length != 4) {
      return
    }
    const basicStyle = parseJson(chart.customAttr).basicStyle
    const colors = []
    const alpha = basicStyle.alpha
    basicStyle.colors.forEach(ele => {
      colors.push(hexColorToRGBA(ele, alpha))
    })
    const data = parseJson(chart.data?.tableRow)

    // 时间字段
    const xAxisDataeaseName = xAxis[0].dataeaseName
    const averages = [5, 10, 20, 60, 120, 180]
    const legendItems: any[] = [
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
              ['M', x, y + width + 2],
              ['L', x, x - width - 1]
            ]
          },
          style: { fill: 'red', stroke: 'red', lineWidth: 2 }
        }
      }
    ]
    // 计算均线数据
    const averagesLineData = new Map()
    averages.forEach(item => {
      averagesLineData.set('ma' + item, this.calculateMovingAverage(data, item, chart))
    })

    // 将均线数据设置到主数据中
    data.forEach((item: any) => {
      const date = item[xAxisDataeaseName]
      for (const [key, value] of averagesLineData) {
        item[key] = value.find(m => m[xAxisDataeaseName] === date)?.value
      }
    })

    const averageLines: any[] = []
    let index = 0
    const start = 0.5
    const end = 1
    const startIndex = Math.floor(start * data.length)
    const endIndex = Math.ceil(end * data.length)
    const filteredData = data.slice(startIndex, endIndex)
    const { maxValue, minValue } = this.calculateMinMax(filteredData)
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
    const option = this.setupOptions(chart, {
      data,
      slider: {
        start: 0.5,
        end: 1
      },
      plots: [
        {
          type: 'stock',
          top: true,

          options: {
            stockStyle: {
              stroke: 'black',
              lineWidth: 0.5
            },
            yAxis: {
              label: {},
              position: 'left',
              min: minValue,
              max: maxValue
            },
            xField: xAxisDataeaseName,
            yField: [
              yAxis[0].dataeaseName,
              yAxis[1].dataeaseName,
              yAxis[2].dataeaseName,
              yAxis[3].dataeaseName
            ],
            legend: {
              position: 'top',
              custom: true,
              items: legendItems
            }
          }
        },
        ...averageLines
      ]
    })
    const plot = new Mix(container, option)
    this.registerEvent(data, plot, averagesLineData)
    plot.on('schema:click', evt => {
      const selectSchema = evt.data.data[xAxisDataeaseName]
      const paramData = parseJson(chart.data?.data)
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

  protected configBasicStyle(chart: Chart, options: MixOptions): MixOptions {
    // size
    const customAttr: DeepPartial<ChartAttr> = parseJson(chart.customAttr)
    const s = JSON.parse(JSON.stringify(customAttr.basicStyle))
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
      if (item.type === 'stock') {
        plots.push({ ...item })
      }
      if (item.type === 'line') {
        plots.push({ ...item, options: { ...item.options, smooth, point, lineStyle } })
      }
    })
    return {
      ...options,
      plots
    }
  }

  protected configTooltip(chart: Chart, options: MixOptions): MixOptions {
    const tooltipAttr = parseJson(chart.customAttr).tooltip
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
    const customTooltipItems = originalItems => {
      const formattedItems = originalItems.map(item => {
        const fieldObj = showFiled.find(q => q.dataeaseName === item.name)
        const displayName = fieldObj?.chartShowName || fieldObj?.name || item.name
        const formattedName = displayName.startsWith('ma') ? displayName.toUpperCase() : displayName
        const formattedValue = valueFormatter(item.value, tooltipAttr.tooltipFormatter)

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
    const formatTooltipItem = (item: any) => {
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
    const generateCustomTooltipContent = (title: string, items: Array<any>) => {
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
          follow: false
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

  protected configXAxis(chart: Chart, options: MixOptions): MixOptions {
    const xAxisOptions = super.configXAxis(chart, options)
    if (!xAxisOptions) {
      return options
    }
    const newPlots = []
    const linePlotList = options.plots.filter(item => item.type === 'line')

    const stockPlot = options.plots.filter(item => item.type === 'stock')[0]
    const newStockPlot = {
      ...stockPlot,
      options: {
        ...stockPlot.options,
        xAxis: xAxisOptions['xAxis']
          ? {
              ...stockPlot.options['xAxis'],
              ...xAxisOptions['xAxis']
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
  protected configYAxis(chart: Chart, options: MixOptions): MixOptions {
    const yAxisOptions = super.configYAxis(chart, options)
    if (!yAxisOptions) {
      return options
    }
    const yAxis = parseJson(chart.customStyle).yAxis
    const newPlots = []
    const linePlotList = options.plots.filter(item => item.type === 'line')

    const stockPlot = options.plots.filter(item => item.type === 'stock')[0]
    let label = false
    if (yAxisOptions['yAxis'].label) {
      label = {
        ...yAxisOptions['yAxis'].label,
        formatter: value => {
          return valueFormatter(value, yAxis.axisLabelFormatter)
        }
      }
    }
    const newStockPlot = {
      ...stockPlot,
      options: {
        ...stockPlot.options,
        yAxis: label
          ? {
              ...stockPlot.options['yAxis'],
              ...yAxisOptions['yAxis'],
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

  protected customConfigEmptyDataStrategy(chart: Chart, options: MixOptions): MixOptions {
    const { data } = options as unknown as Options
    if (!data?.length) {
      return options
    }
    const strategy = parseJson(chart.senior).functionCfg.emptyDataStrategy
    if (strategy === 'ignoreData') {
      for (let i = data.length - 1; i >= 0; i--) {
        const item = data[i]
        Object.keys(item).forEach(key => {
          if (key.startsWith('f_') && item[key] === null) {
            data.splice(i, 1)
          }
        })
      }
    }
    const updateValues = (strategy: 'breakLine' | 'setZero', data: any[]) => {
      data.forEach(obj => {
        Object.keys(obj).forEach(key => {
          if (obj[key] === null) {
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

  protected setupOptions(chart: Chart, options: MixOptions): MixOptions {
    return flow(
      this.configTheme,
      this.configBasicStyle,
      this.configXAxis,
      this.configYAxis,
      this.configTooltip,
      this.customConfigEmptyDataStrategy
    )(chart, options)
  }

  constructor(name = 'stock-line') {
    super(name, DEFAULT_DATA)
  }
}
