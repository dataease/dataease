import type { FunnelOptions, Funnel as G2Funnel } from '@antv/g2plot/esm/plots/funnel'
import { G2PlotChartView, G2PlotDrawOptions } from '../../types/impl/g2plot'
import { flow, parseJson, setUpSingleDimensionSeriesColor } from '@/views/chart/components/js/util'
import { getPadding } from '../../common/common_antv'
import { useI18n } from '@/hooks/web/useI18n'
import { Datum } from '@antv/g2plot/esm/types/common'
import { valueFormatter } from '@/views/chart/components/js/formatter'

const { t } = useI18n()

/**
 * 漏斗图
 */
export class Funnel extends G2PlotChartView<FunnelOptions, G2Funnel> {
  properties: EditorProperty[] = [
    'background-overall-component',
    'basic-style-selector',
    'label-selector',
    'tooltip-selector',
    'title-selector',
    'legend-selector',
    'jump-set',
    'linkage'
  ]
  propertyInner: EditorPropertyInner = {
    'background-overall-component': ['all'],
    'basic-style-selector': ['colors', 'alpha', 'seriesColor'],
    'label-selector': ['fontSize', 'color', 'hPosition', 'showQuota', 'conversionTag'],
    'tooltip-selector': ['color', 'fontSize', 'backgroundColor', 'seriesTooltipFormatter', 'show'],
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
  axisConfig: AxisConfig = {
    xAxis: {
      name: `${t('chart.drag_block_funnel_split')} / ${t('chart.dimension')}`,
      type: 'd'
    },
    yAxis: {
      name: `${t('chart.drag_block_funnel_width')} / ${t('chart.quota')}`,
      type: 'q',
      limit: 1
    }
  }

  async drawChart(drawOptions: G2PlotDrawOptions<G2Funnel>): Promise<G2Funnel> {
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
      ],
      meta: {
        field: {
          type: 'cat'
        }
      }
    }
    const options = this.setupOptions(chart, baseOptions)
    const { Funnel: G2Funnel } = await import('@antv/g2plot/esm/plots/funnel')
    const newChart = new G2Funnel(container, options)
    newChart.on('interval:click', action)
    return newChart
  }

  protected configLabel(chart: Chart, options: FunnelOptions): FunnelOptions {
    let label
    let conversionTag
    let customAttr: DeepPartial<ChartAttr>
    if (chart.customAttr) {
      customAttr = parseJson(chart.customAttr)
      const showQuota = customAttr.label.showQuota
      const l = customAttr.label
      if (customAttr.label?.show) {
        // label
        if (showQuota) {
          label = {
            position: l.position,
            layout: [{ type: 'limit-in-canvas' }],
            style: {
              fill: l.color,
              fontSize: l.fontSize
            },
            formatter: function (param: Datum) {
              return valueFormatter(param.value, l.quotaLabelFormatter)
            }
          }
          const position = label.position
          if (position === 'right') {
            label.offsetX = -40
          }
        }
        // 转化率
        const conversionTagAtt = parseJson(chart.customAttr).label.conversionTag
        if (conversionTagAtt && conversionTagAtt.show) {
          conversionTag = {
            style: {
              fill: l.color,
              fontSize: l.fontSize
            },
            formatter: datum => {
              const rate = (
                (datum['$$conversion$$'][1] / datum['$$conversion$$'][0]) *
                100
              ).toFixed(conversionTagAtt.precision)
              return `${conversionTagAtt.text ?? ''}${rate}%`
            }
          }
        }
      }
      return {
        ...options,
        label,
        conversionTag,
        maxSize: conversionTag ? 0.8 : 1
      }
    }
    return options
  }

  public setupSeriesColor(chart: ChartObj, data?: any[]): ChartBasicStyle['seriesColor'] {
    return setUpSingleDimensionSeriesColor(chart, data)
  }
  protected setupOptions(chart: Chart, options: FunnelOptions): FunnelOptions {
    return flow(
      this.configTheme,
      this.configSingleDimensionColor,
      this.configLabel,
      this.configMultiSeriesTooltip,
      this.configLegend
    )(chart, options)
  }
  setupDefaultOptions(chart: ChartObj): ChartObj {
    const { customAttr, customStyle } = chart
    const { label } = customAttr
    if (!['left', 'middle', 'right'].includes(label.position)) {
      label.position = 'middle'
    }
    customAttr.label = {
      ...label,
      show: true,
      showQuota: true,
      conversionTag: {
        show: false,
        precision: 2,
        text: '转化率'
      }
    }
    const { legend } = customStyle
    legend.show = false
    return chart
  }

  constructor() {
    super('funnel', [])
  }
}
