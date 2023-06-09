import { G2PlotChartView, G2PlotDrawOptions } from '@/views/chart/components/js/panel/types'
import { Bar, BarOptions, Datum } from '@antv/g2plot'
import {
  getPadding,
  setGradientColor,
  transAxisPosition
} from '@/views/chart/components/js/panel/common/common_antv'
import _ from 'lodash'
import {
  antVCustomColor,
  flow,
  handleEmptyDataStrategy,
  parseJson
} from '@/views/chart/components/js/util'
import { singleDimensionTooltipFormatter } from '@/views/chart/components/js/formatter'

const DEFAULT_DATA = []

export class HorizontalBar extends G2PlotChartView<BarOptions, Bar> {
  drawChart(drawOptions: G2PlotDrawOptions<Bar>): Bar {
    const chart = drawOptions.chart
    // size
    let customAttr: DeepPartial<ChartAttr>
    let barGap = undefined
    if (chart.customAttr) {
      customAttr = parseJson(chart.customAttr)
      if (customAttr.size) {
        const s = parseJson(customAttr).size
        if (!s.barDefault) {
          barGap = s.barGap
        }
      }
    }
    // data
    const data = _.cloneDeep(chart.data.data)
    // custom color
    let color = antVCustomColor(chart)
    if (customAttr.color.gradient) {
      color = color.map(ele => {
        return setGradientColor(ele, customAttr.color.gradient)
      })
    }

    // options
    const initOptions: BarOptions = {
      data: data,
      xField: 'value',
      yField: 'field',
      seriesField: 'category',
      appendPadding: getPadding(chart),
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

    const options = this.setupOptions(chart, initOptions)
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
      let emptyDataStrategy = parseJson(chart.senior)?.functionCfg?.emptyDataStrategy
      if (!emptyDataStrategy) {
        emptyDataStrategy = 'breakLine'
      }
      handleEmptyDataStrategy(emptyDataStrategy, chart, data, options)
    }

    // 开始渲染
    if (drawOptions.chartObj) {
      drawOptions.chartObj.destroy()
    }
    drawOptions.chartObj = new Bar(drawOptions.container, options)

    drawOptions.chartObj.off('interval:click')
    drawOptions.chartObj.on('interval:click', drawOptions.action)

    return drawOptions.chartObj
  }

  protected configTooltip(chart: Chart, options: BarOptions): BarOptions {
    let tooltip
    const customAttr: DeepPartial<ChartAttr> = parseJson(chart.customAttr)
    if (customAttr.tooltip) {
      const tooltipAttr = customAttr.tooltip
      if (tooltipAttr.show) {
        tooltip = {
          formatter: function (param: Datum) {
            return singleDimensionTooltipFormatter(param, chart)
          }
        }
      } else {
        tooltip = false
      }
    }
    return { ...options, tooltip }
  }
  protected configCustomXAxis(chart: Chart, options: BarOptions): BarOptions {
    if (options.xAxis) {
      const axisValue = parseJson(chart.customStyle).xAxis.axisValue
      if (!axisValue?.auto) {
        const axis = {
          position: transAxisPosition(options.xAxis.position),
          xAxis: {
            min: axisValue.min,
            max: axisValue.max,
            tickCount: axisValue.splitCount
          }
        }
        return { ...options, ...axis }
      }
    }
    return options
  }
  constructor() {
    super('bar-horizontal', DEFAULT_DATA)
  }

  protected setupOptions(chart: Chart, options: BarOptions): BarOptions {
    return flow(
      this.configTheme,
      this.configLabel,
      this.configTooltip,
      this.configLegend,
      this.configXAxis,
      this.configCustomXAxis,
      this.configYAxis,
      this.configSlider,
      this.configAnalyse
    )(chart, options)
  }
}
