import {
  G2PlotChartView,
  G2PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/g2plot'
import { Bar, BarOptions, Datum } from '@antv/g2plot'
import {
  getPadding,
  setGradientColor,
  transAxisPosition
} from '@/views/chart/components/js/panel/common/common_antv'
import { cloneDeep } from 'lodash-es'
import {
  flow,
  handleEmptyDataStrategy,
  hexColorToRGBA,
  parseJson
} from '@/views/chart/components/js/util'
import { singleDimensionTooltipFormatter } from '@/views/chart/components/js/formatter'
import {
  BAR_EDITOR_PROPERTY,
  BAR_EDITOR_PROPERTY_INNER
} from '@/views/chart/components/js/panel/charts/bar/common'
import { ColumnOptions } from '@antv/g2plot/lib/plots/column'

const DEFAULT_DATA = []

export class HorizontalBar extends G2PlotChartView<BarOptions, Bar> {
  properties = BAR_EDITOR_PROPERTY
  propertyInner = BAR_EDITOR_PROPERTY_INNER
  axis: AxisType[] = ['xAxis', 'yAxis', 'filter', 'drill', 'extLabel', 'extTooltip']
  drawChart(drawOptions: G2PlotDrawOptions<Bar>): Bar {
    const { chart, container, action } = drawOptions
    if (!chart.data?.data?.length) {
      return
    }
    // data
    const data = cloneDeep(chart.data.data)

    // options
    const initOptions: BarOptions = {
      data: data,
      xField: 'value',
      yField: 'field',
      seriesField: 'category',
      appendPadding: getPadding(chart),
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

    // 处理空值
    if (chart.senior) {
      let emptyDataStrategy = parseJson(chart.senior)?.functionCfg?.emptyDataStrategy
      if (!emptyDataStrategy) {
        emptyDataStrategy = 'breakLine'
      }
      handleEmptyDataStrategy(emptyDataStrategy, chart, data, options)
    }

    // 开始渲染
    const newChart = new Bar(container, options)

    newChart.on('interval:click', action)

    return newChart
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

  protected configBasicStyle(chart: Chart, options: ColumnOptions): ColumnOptions {
    const basicStyle = parseJson(chart.customAttr).basicStyle
    if (basicStyle.gradient) {
      let color = basicStyle.colors
      color = color.map(ele => {
        const tmp = hexColorToRGBA(ele, basicStyle.alpha)
        return setGradientColor(tmp, true)
      })
      options = {
        ...options,
        color
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
      this.configBasicStyle,
      this.configLabel,
      this.configTooltip,
      this.configLegend,
      this.configXAxis,
      this.configCustomXAxis,
      this.configYAxis,
      this.configSlider,
      this.configAnalyseHorizontal
    )(chart, options)
  }
}
