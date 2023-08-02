import {
  G2PlotChartView,
  G2PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/g2plot'
import { Area as G2Area, AreaOptions, Datum } from '@antv/g2plot'
import { getPadding, setGradientColor } from '@/views/chart/components/js/panel/common/common_antv'
import { cloneDeep } from 'lodash-es'
import {
  flow,
  handleEmptyDataStrategy,
  parseJson
} from '@/views/chart/components/js/util'
import { singleDimensionTooltipFormatter } from '@/views/chart/components/js/formatter'
import {
  LINE_EDITOR_PROPERTY,
  LINE_EDITOR_PROPERTY_INNER
} from '@/views/chart/components/js/panel/charts/line/common'

const DEFAULT_DATA = []
export class Area extends G2PlotChartView<AreaOptions, G2Area> {
  properties = LINE_EDITOR_PROPERTY
  propertyInner = LINE_EDITOR_PROPERTY_INNER
  axis: AxisType[] = ['xAxis', 'yAxis', 'drill', 'filter']

  drawChart(drawOptions: G2PlotDrawOptions<G2Area>): G2Area {
    const { chart, container, action } = drawOptions
    if (chart?.data) {
      // data
      const data = cloneDeep(chart.data.data)
      const initOptions: AreaOptions = {
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
      const newChart = new G2Area(container, options)

      newChart.on('point:click', action)

      return newChart
    }
  }

  protected configCustomLabel(_: Chart, options: AreaOptions): AreaOptions {
    if (options.label) {
      const label = {
        ...options.label,
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

  protected configBasicStyle(chart: Chart, options: AreaOptions): AreaOptions {
    // size
    const customAttr: DeepPartial<ChartAttr> = parseJson(chart.customAttr)
    const s: DeepPartial<ChartBasicStyle> = JSON.parse(JSON.stringify(customAttr.basicStyle))
    const smooth = s.lineSmooth
    const point = {
      size: s.lineSymbolSize,
      shape: s.lineSymbol
    }
    const line = {
      style: {
        lineWidth: s.lineWidth
      }
    }
    // custom color
    const color = customAttr.basicStyle.colors
    const areaColors = [...color, ...color]
    let areaStyle
    if (customAttr.basicStyle.gradient) {
      areaStyle = () => {
        const ele = areaColors.shift()
        if (ele) {
          return {
            fill: setGradientColor(ele, true, 270)
          }
        }
      }
    }
    return {
      ...options,
      smooth,
      line,
      point,
      areaStyle
    }
  }

  protected setupOptions(chart: Chart, options: AreaOptions): AreaOptions {
    return flow(
      this.configTheme,
      this.configLabel,
      this.configTooltip,
      this.configBasicStyle,
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
