import { Column, ColumnOptions } from '@antv/g2plot/lib/plots/column'
import { cloneDeep } from 'lodash-es'
import {
  G2PlotChartView,
  G2PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/g2plot'
import { flow, hexColorToRGBA, parseJson } from '@/views/chart/components/js/util'
import { Datum } from '@antv/g2plot'
import { singleDimensionTooltipFormatter } from '@/views/chart/components/js/formatter'
import {
  BAR_EDITOR_PROPERTY,
  BAR_EDITOR_PROPERTY_INNER
} from '@/views/chart/components/js/panel/charts/bar/common'
import { setGradientColor } from '@/views/chart/components/js/panel/common/common_antv'

const DEFAULT_DATA: any[] = []

/**
 * 柱状图
 */
export class Bar extends G2PlotChartView<ColumnOptions, Column> {
  properties = BAR_EDITOR_PROPERTY
  propertyInner = BAR_EDITOR_PROPERTY_INNER

  axis: AxisType[] = ['xAxis', 'yAxis', 'filter', 'drill', 'extLabel', 'extTooltip']

  drawChart(drawOptions: G2PlotDrawOptions<Column>): Column {
    const { chart } = drawOptions
    if (!chart?.data?.data?.length) {
      return
    }
    const data = cloneDeep(drawOptions.chart.data?.data)
    const initOptions: ColumnOptions = {
      xField: 'field',
      yField: 'value',
      seriesField: 'category',
      data: data,
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
    const options: ColumnOptions = this.setupOptions(drawOptions.chart, initOptions)

    if (drawOptions.chartObj) {
      drawOptions.chartObj.destroy()
    }
    drawOptions.chartObj = new Column(drawOptions.container, options)

    drawOptions.chartObj.off('interval:click')
    drawOptions.chartObj.on('interval:click', drawOptions.action)

    return drawOptions.chartObj
  }

  protected configTooltip(chart: Chart, options: ColumnOptions): ColumnOptions {
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

  protected configCustomYAxis(chart: Chart, options: ColumnOptions): ColumnOptions {
    if (options.yAxis) {
      const axisValue = parseJson(chart.customStyle).yAxis.axisValue
      if (!axisValue?.auto) {
        const yAxis = {
          ...options.yAxis,
          min: axisValue.min,
          max: axisValue.max,
          minLimit: axisValue.min,
          maxLimit: axisValue.max,
          tickCount: axisValue.splitCount
        }
        return { ...options, yAxis }
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
        return setGradientColor(tmp, true, 270)
      })
      options = {
        ...options,
        color
      }
    }
    return options
  }

  protected setupOptions(chart: Chart, options: ColumnOptions): ColumnOptions {
    return flow(
      this.configTheme,
      this.configBasicStyle,
      this.configLabel,
      this.configTooltip,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configCustomYAxis,
      this.configSlider,
      this.configAnalyse
    )(chart, options)
  }

  constructor() {
    super('bar', DEFAULT_DATA)
  }
}
