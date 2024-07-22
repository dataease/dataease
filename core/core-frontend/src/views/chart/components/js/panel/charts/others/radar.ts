import { RadarOptions, Radar as G2Radar } from '@antv/g2plot/esm/plots/radar'
import { G2PlotChartView, G2PlotDrawOptions } from '../../types/impl/g2plot'
import { flow, parseJson } from '../../../util'
import { getPadding } from '../../common/common_antv'
import { valueFormatter } from '../../../formatter'
import { Datum } from '@antv/g2plot/esm/types/common'
import { useI18n } from '@/hooks/web/useI18n'
import { DEFAULT_LABEL } from '@/views/chart/components/editor/util/chart'

const { t } = useI18n()

export class Radar extends G2PlotChartView<RadarOptions, G2Radar> {
  properties: EditorProperty[] = [
    'background-overall-component',
    'basic-style-selector',
    'label-selector',
    'tooltip-selector',
    'title-selector',
    'legend-selector',
    'misc-style-selector',
    'jump-set',
    'linkage'
  ]
  propertyInner: EditorPropertyInner = {
    'basic-style-selector': ['colors', 'alpha', 'radarShape', 'seriesColor'],
    'label-selector': ['seriesLabelFormatter'],
    'tooltip-selector': ['color', 'fontSize', 'backgroundColor', 'seriesTooltipFormatter', 'show'],
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
  selectorSpec: EditorSelectorSpec = {
    ...this['selectorSpec'],
    'misc-style-selector': {
      title: `${t('chart.tooltip_axis')}`
    }
  }
  axis: AxisType[] = ['xAxis', 'yAxis', 'drill', 'filter', 'extLabel', 'extTooltip']
  axisConfig: AxisConfig = {
    xAxis: {
      name: `${t('chart.drag_block_radar_label')} / ${t('chart.dimension')}`,
      type: 'd'
    },
    yAxis: {
      name: `${t('chart.drag_block_radar_length')} / ${t('chart.quota')}`,
      type: 'q'
    }
  }

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

  protected configLabel(chart: Chart, options: RadarOptions): RadarOptions {
    const tmpOptions = super.configLabel(chart, options)
    if (!tmpOptions.label) {
      return {
        ...tmpOptions,
        label: false
      }
    }
    const labelAttr = parseJson(chart.customAttr).label
    const formatterMap = labelAttr.seriesLabelFormatter?.reduce((pre, next) => {
      pre[next.id] = next
      return pre
    }, {})
    tmpOptions.label.style.fill = DEFAULT_LABEL.color
    // 自动旋转和标签自定义有冲突
    const label = {
      fields: [],
      ...tmpOptions.label,
      autoRotate: false,
      autoHide: true,
      formatter: (data: Datum) => {
        if (!labelAttr.seriesLabelFormatter?.length) {
          return data.value
        }
        const labelCfg = formatterMap?.[data.quotaList[0].id] as SeriesFormatter
        if (!labelCfg) {
          return data.value
        }
        if (!labelCfg.show) {
          return
        }
        const value = valueFormatter(data.value, labelCfg.formatterCfg)
        const group = new G2PlotChartView.engine.Group({})
        group.addShape({
          type: 'text',
          attrs: {
            x: 0,
            y: 0,
            text: value,
            textAlign: 'start',
            textBaseline: 'top',
            fontSize: labelCfg.fontSize,
            fill: labelCfg.color
          }
        })
        return group
      }
    }
    return {
      ...tmpOptions,
      label
    }
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
          type: basicStyle.radarShape,
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
      this.configColor,
      this.configLabel,
      this.configLegend,
      this.configMultiSeriesTooltip,
      this.configAxis
    )(chart, options)
  }

  constructor() {
    super('radar', [])
  }
}
