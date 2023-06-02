import { G2PlotChartView, G2PlotDrawOptions } from '@/views/chart/components/js/panel/types'
import { Bar as G2Bar, BarOptions } from '@antv/g2plot'
import {
  getAnalyse,
  getLabel,
  getLegend,
  getPadding,
  getSlider,
  getTheme,
  getTooltip,
  getXAxis,
  getYAxis,
  setGradientColor
} from '@/views/chart/components/js/panel/common/common_antv'
import _ from 'lodash'
import { antVCustomColor, handleEmptyDataStrategy } from '@/views/chart/components/js/util'
const BAR_DEFAULT_DATA = []
export class Bar extends G2PlotChartView<BarOptions, G2Bar> {
  drawChart(drawOptions: G2PlotDrawOptions<G2Bar>): G2Bar {
    const chart = drawOptions.chart
    // size
    let customAttr: DeepPartial<ChartAttr> = {}
    let barGap = undefined
    if (chart.customAttr) {
      customAttr = JSON.parse(JSON.stringify(chart.customAttr))
      if (customAttr.size) {
        const s = JSON.parse(JSON.stringify(customAttr.size)) as DeepPartial<ChartSizeAttr>
        if (!s.barDefault) {
          barGap = s.barGap
        }
      }
    }
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
    // custom color
    let color = antVCustomColor(chart)
    if (customAttr.color.gradient) {
      color = color.map(ele => {
        return setGradientColor(ele, customAttr.color.gradient)
      })
    }

    // options
    const options: BarOptions = {
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
      color: color,
      marginRatio: barGap,
      interactions: [
        {
          type: 'legend-active',
          cfg: {
            start: [{ trigger: 'legend-item:mouseenter', action: ['element-active:reset'] }],
            end: [{ trigger: 'legend-item:mouseleave', action: ['element-active:reset'] }]
          }
        },
        {
          type: 'legend-filter',
          cfg: {
            start: [
              {
                trigger: 'legend-item:click',
                action: [
                  'list-unchecked:toggle',
                  'data-filter:filter',
                  'element-active:reset',
                  'element-highlight:reset'
                ]
              }
            ]
          }
        },
        {
          type: 'tooltip',
          cfg: {
            start: [{ trigger: 'interval:mousemove', action: 'tooltip:show' }],
            end: [{ trigger: 'interval:mouseleave', action: 'tooltip:hide' }]
          }
        },
        {
          type: 'active-region',
          cfg: {
            start: [{ trigger: 'interval:mousemove', action: 'active-region:show' }],
            end: [{ trigger: 'interval:mouseleave', action: 'active-region:hide' }]
          }
        }
      ]
    }

    // group
    // if (isGroup) {
    //   options.isGroup = true
    // } else {
    //   delete options.isGroup
    // }
    // // stack
    // if (isStack) {
    //   options.isStack = true
    // } else {
    //   delete options.isStack
    // }
    // options.isPercent = chart.type.includes('percentage')

    // 处理空值
    if (chart.senior) {
      let emptyDataStrategy = JSON.parse(JSON.stringify(chart.senior))?.functionCfg
        ?.emptyDataStrategy
      if (!emptyDataStrategy) {
        emptyDataStrategy = 'breakLine'
      }
      handleEmptyDataStrategy(emptyDataStrategy, chart, data, options)
    }

    // 开始渲染
    if (drawOptions.chartObj) {
      drawOptions.chartObj.destroy()
    }
    drawOptions.chartObj = new G2Bar(drawOptions.container, options)

    drawOptions.chartObj.off('interval:click')
    drawOptions.chartObj.on('interval:click', drawOptions.action)

    return drawOptions.chartObj
  }

  constructor() {
    super('bar', BAR_DEFAULT_DATA)
  }
}
