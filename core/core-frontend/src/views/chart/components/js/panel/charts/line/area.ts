import { G2PlotChartView, G2PlotDrawOptions } from '@/views/chart/components/js/panel/types'
import { Area as G2Area, AreaOptions, Datum } from '@antv/g2plot'
import { getPadding, setGradientColor } from '@/views/chart/components/js/panel/common/common_antv'
import _ from 'lodash'
import {
  antVCustomColor,
  flow,
  handleEmptyDataStrategy,
  parseJson
} from '@/views/chart/components/js/util'
import { singleDimensionTooltipFormatter } from '@/views/chart/components/js/formatter'
const DEFAULT_DATA = []
export class Area extends G2PlotChartView<AreaOptions, G2Area> {
  drawChart(drawOptions: G2PlotDrawOptions<G2Area>): G2Area {
    const chart = drawOptions.chart
    if (!chart.data?.data?.length) {
      return
    }
    // data
    const data = _.cloneDeep(chart.data.data)
    // size
    let customAttr: DeepPartial<ChartAttr>
    let smooth, point, line
    if (chart.customAttr) {
      customAttr = parseJson(chart.customAttr)
      if (customAttr.size) {
        const s: DeepPartial<ChartSizeAttr> = JSON.parse(JSON.stringify(customAttr.size))
        smooth = s.lineSmooth
        point = {
          size: s.lineSymbolSize,
          shape: s.lineSymbol
        }
        line = {
          style: {
            lineWidth: s.lineWidth
          }
        }
      }
    }
    // custom color
    const color = antVCustomColor(chart)
    const areaColors = [...color, ...color]
    let areaStyle
    if (customAttr.color.gradient) {
      areaStyle = () => {
        const ele = areaColors.shift()
        if (ele) {
          return {
            fill: setGradientColor(ele, customAttr.color.gradient, 270)
          }
        }
      }
    }
    const initOptions: AreaOptions = {
      point,
      smooth,
      line,
      areaStyle,
      color,
      data: data,
      xField: 'field',
      yField: 'value',
      seriesField: 'category',
      appendPadding: getPadding(chart),
      // isStack: isStack,
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
            start: [{ trigger: 'point:mousemove', action: 'tooltip:show' }],
            end: [{ trigger: 'point:mouseleave', action: 'tooltip:hide' }]
          }
        },
        {
          type: 'active-region',
          cfg: {
            start: [{ trigger: 'element:mousemove', action: 'active-region:show' }],
            end: [{ trigger: 'element:mouseleave', action: 'active-region:hide' }]
          }
        }
      ]
    }

    // options
    const options = this.setupOptions(chart, initOptions)
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
    drawOptions.chartObj = new G2Area(drawOptions.container, options)

    drawOptions.chartObj.off('point:click')
    drawOptions.chartObj.on('point:click', drawOptions.action)

    return drawOptions.chartObj
  }

  protected configCustomLabel(_: Chart, options: AreaOptions): AreaOptions {
    if (options.label) {
      const label = {
        offsetY: -8,
        autoRotate: undefined
      }
      return { ...options, label }
    }
    return options
  }

  protected configTooltip(chart: Chart, options: AreaOptions): AreaOptions {
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

  protected setupOptions(chart: Chart, options: AreaOptions): AreaOptions {
    return flow(
      this.configTheme,
      this.configLabel,
      this.configTooltip,
      this.configCustomLabel,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configSlider,
      this.configAnalyse
    )(chart, options)
  }
  constructor() {
    super('area', DEFAULT_DATA)
  }
}
