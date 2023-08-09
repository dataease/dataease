import { RadarOptions, Radar as G2Radar, Datum } from '@antv/g2plot'
import { G2PlotChartView, G2PlotDrawOptions } from '../../types/impl/g2plot'
import { flow, parseJson } from '../../../util'
import { getPadding } from '../../common/common_antv'
import { singleDimensionTooltipFormatter } from '../../../formatter'

export class Radar extends G2PlotChartView<RadarOptions, G2Radar> {
  public drawChart(drawOptions: G2PlotDrawOptions<G2Radar>): G2Radar {
    const { chart, container, action } = drawOptions
    if (!chart.data?.data) {
      return
    }
    const data = chart.data.data
    const baseOptions: RadarOptions = {
      data,
      xField: 'field',
      yField: 'value',
      seriesField: 'category',
      appendPadding: getPadding(chart),
      point: {
        size: 4,
        shape: 'circle',
        style: {
          fill: null
        }
      },
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
            start: [{ trigger: 'point:mousemove', action: 'active-region:show' }],
            end: [{ trigger: 'point:mouseleave', action: 'active-region:hide' }]
          }
        }
      ]
    }
    const options = this.setupOptions(chart, baseOptions)
    const newChart = new G2Radar(container, options)
    newChart.on('point:click', action)
    return newChart
  }

  protected configTooltip(chart: Chart, options: RadarOptions): RadarOptions {
    const customAttr: DeepPartial<ChartAttr> = parseJson(chart.customAttr)
    const tooltipAttr = customAttr.tooltip
    if (!tooltipAttr.show) {
      return {
        ...options,
        tooltip: false
      }
    }
    const tooltip = {
      formatter: function (param: Datum) {
        return singleDimensionTooltipFormatter(param, chart)
      }
    }
    return { ...options, tooltip }
  }

  protected configAxis(chart: Chart, options: RadarOptions): RadarOptions {
    const customAttr = parseJson(chart.customAttr)
    const customStyle = parseJson(chart.customStyle)
    const basicStyle = customAttr.basicStyle
    const misc = customStyle.misc
    let label: any = {
      style: {
        fill: misc.color,
        fontSize: misc.fontSize
      }
    }
    if (!misc.showName) {
      label = false
    }
    const xAxis = {
      line: null,
      tickLine: null,
      label,
      grid: {
        line: {
          style: {
            stroke: misc.axisColor
          }
        }
      }
    }
    const yAxis = {
      label: null,
      line: null,
      tickLine: null,
      grid: {
        line: {
          type: 'line',
          style: {
            stroke: misc.axisColor
          }
        }
      }
    }
    return {
      ...options,
      xAxis,
      yAxis
    }
  }

  protected setupOptions(chart: Chart, options: RadarOptions): RadarOptions {
    return flow(
      this.configTheme,
      this.configLabel,
      this.configLegend,
      this.configTooltip,
      this.configAxis
    )(chart, options)
  }
  properties: EditorProperty[] = [
    'background-overall-component',
    'basic-style-selector',
    'label-selector',
    'tooltip-selector',
    'title-selector',
    'legend-selector',
    'misc-style-selector'
  ]
  propertyInner: EditorPropertyInner = {
    'basic-style-selector': ['colors', 'alpha', 'radarShape'],
    'label-selector': ['fontSize', 'color', 'vPosition'],
    'tooltip-selector': ['color', 'fontSize', 'backgroundColor'],
    'misc-style-selector': ['showName', 'color', 'fontSize', 'axisColor'],
    'title-selector': [
      'show',
      'title',
      'fontSize',
      'color',
      'hPosition',
      'isItalic',
      'isBolder',
      'remarkShow',
      'fontFamily',
      'letterSpace',
      'fontShadow'
    ],
    'legend-selector': ['icon', 'orient', 'color', 'fontSize', 'hPosition', 'vPosition']
  }
  axis: AxisType[] = ['xAxis', 'yAxis', 'drill', 'filter', 'extLabel', 'extTooltip']

  constructor() {
    super('radar', [])
  }
}
