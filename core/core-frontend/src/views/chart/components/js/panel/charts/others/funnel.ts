import { FunnelOptions, Funnel as G2Funnel } from '@antv/g2plot/esm/plots/funnel'
import { G2PlotChartView, G2PlotDrawOptions } from '../../types/impl/g2plot'
import { flow, parseJson } from '@/views/chart/components/js/util'
import { getPadding } from '../../common/common_antv'
import { formatterItem, singleDimensionTooltipFormatter, valueFormatter } from '../../../formatter'
import { Tooltip } from '@antv/g2plot/esm/types/tooltip'
import { Datum } from '@antv/g2plot/esm/types/common'

/**
 * 漏斗图
 */
export class Funnel extends G2PlotChartView<FunnelOptions, G2Funnel> {
  public drawChart(drawOptions: G2PlotDrawOptions<G2Funnel>): G2Funnel {
    const { chart, container, action } = drawOptions
    if (!chart.data?.data) {
      return
    }
    const data = chart.data.data
    const baseOptions: FunnelOptions = {
      data,
      xField: 'field',
      yField: 'value',
      appendPadding: getPadding(chart),
      conversionTag: false,
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
        }
      ]
    }
    const options = this.setupOptions(chart, baseOptions)
    const newChart = new G2Funnel(container, options)
    newChart.on('interval:click', action)
    return newChart
  }

  protected configTooltip(chart: Chart, options: FunnelOptions): FunnelOptions {
    const customAttr: DeepPartial<ChartAttr> = parseJson(chart.customAttr)
    if (!customAttr.tooltip.show) {
      return {
        ...options,
        tooltip: false
      }
    }
    const yAxis = chart.yAxis
    const tooltip: Tooltip = {
      formatter: function (param: Datum) {
        const obj = { name: param.field, value: param.value }
        let res = param.value
        for (let i = 0; i < yAxis.length; i++) {
          const f = yAxis[i]
          if (f.formatterCfg) {
            res = valueFormatter(param.value, f.formatterCfg)
          } else {
            res = valueFormatter(param.value, formatterItem)
          }
        }
        obj.value = res ?? ''
        return obj
      }
    }
    return { ...options, tooltip }
  }

  protected setupOptions(chart: Chart, options: FunnelOptions): FunnelOptions {
    return flow(
      this.configTheme,
      this.configLabel,
      this.configTooltip,
      this.configLegend
    )(chart, options)
  }

  properties: EditorProperty[] = [
    'background-overall-component',
    'basic-style-selector',
    'label-selector',
    'tooltip-selector',
    'title-selector',
    'legend-selector'
  ]
  propertyInner: EditorPropertyInner = {
    'background-overall-component': ['all'],
    'label-selector': ['show', 'fontSize', 'color', 'hPosition'],
    'tooltip-selector': ['color', 'fontSize', 'backgroundColor'],
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
  axis: AxisType[] = ['xAxis', 'yAxis', 'filter', 'drill', 'extLabel', 'extTooltip']

  setupDefaultOptions(chart: ChartObj): ChartObj {
    const { customAttr } = chart
    const { label } = customAttr
    if (!['left', 'middle', 'right'].includes(label.position)) {
      label.position = 'middle'
    }
    return chart
  }

  constructor() {
    super('funnel', [])
  }
}
