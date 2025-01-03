import type { RadarOptions, Radar as G2Radar } from '@antv/g2plot/esm/plots/radar'
import { G2PlotChartView, G2PlotDrawOptions } from '../../types/impl/g2plot'
import { flow, parseJson } from '../../../util'
import { configPlotTooltipEvent } from '../../common/common_antv'
import { valueFormatter } from '../../../formatter'
import type { Datum } from '@antv/g2plot/esm/types/common'
import { useI18n } from '@/hooks/web/useI18n'
import { DEFAULT_LABEL, DEFAULT_LEGEND_STYLE } from '@/views/chart/components/editor/util/chart'
import { Group } from '@antv/g-canvas'
import { defaults } from 'lodash-es'

const { t } = useI18n()

export class Radar extends G2PlotChartView<RadarOptions, G2Radar> {
  properties: EditorProperty[] = [
    'background-overall-component',
    'border-style',
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
    'basic-style-selector': [
      'colors',
      'alpha',
      'radarShape',
      'seriesColor',
      'radarShowPoint',
      'radarPointSize',
      'radarAreaColor'
    ],
    'label-selector': ['seriesLabelFormatter'],
    'tooltip-selector': ['color', 'fontSize', 'backgroundColor', 'seriesTooltipFormatter', 'show'],
    'misc-style-selector': ['showName', 'color', 'fontSize', 'axisColor', 'axisValue'],
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

  async drawChart(drawOptions: G2PlotDrawOptions<G2Radar>): Promise<G2Radar> {
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
      appendPadding: [10, 10, 10, 10],
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
    const { Radar: G2Radar } = await import('@antv/g2plot/esm/plots/radar')
    const newChart = new G2Radar(container, options)
    newChart.on('point:click', action)
    if (options.label) {
      newChart.on('label:click', e => {
        action({
          x: e.x,
          y: e.y,
          data: {
            data: e.target.attrs.data
          }
        })
      })
    }
    configPlotTooltipEvent(chart, newChart)
    return newChart
  }

  protected configBasicStyle(chart: Chart, options: RadarOptions): RadarOptions {
    const { radarShowPoint, radarPointSize, radarAreaColor } = parseJson(
      chart.customAttr
    ).basicStyle
    const tempOptions: RadarOptions = {}

    if (radarShowPoint) {
      tempOptions['point'] = { shape: 'circle', size: radarPointSize, style: { fill: null } }
    }
    if (radarAreaColor) {
      tempOptions['area'] = {}
    }

    return { ...options, ...tempOptions }
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
        const group = new Group({})
        group.addShape({
          type: 'text',
          attrs: {
            data,
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
    const axisValue = misc.axisValue
    if (!axisValue?.auto) {
      const axisYAxis = {
        ...yAxis,
        min: axisValue.min,
        max: axisValue.max,
        minLimit: axisValue.min,
        maxLimit: axisValue.max,
        tickCount: axisValue.splitCount
      }
      return {
        ...options,
        xAxis,
        yAxis: axisYAxis
      }
    }
    return {
      ...options,
      xAxis,
      yAxis
    }
  }

  protected configLegend(chart: Chart, options: RadarOptions): RadarOptions {
    const optionTmp = super.configLegend(chart, options)
    if (!optionTmp.legend) {
      return optionTmp
    }
    const customStyle = parseJson(chart.customStyle)
    let size
    if (customStyle && customStyle.legend) {
      size = defaults(JSON.parse(JSON.stringify(customStyle.legend)), DEFAULT_LEGEND_STYLE).size
    } else {
      size = DEFAULT_LEGEND_STYLE.size
    }
    optionTmp.legend.marker.style = style => {
      return {
        r: size,
        fill: style.stroke
      }
    }
    return optionTmp
  }

  protected setupOptions(chart: Chart, options: RadarOptions): RadarOptions {
    return flow(
      this.configTheme,
      this.configColor,
      this.configLabel,
      this.configLegend,
      this.configMultiSeriesTooltip,
      this.configAxis,
      this.configBasicStyle
    )(chart, options)
  }

  constructor() {
    super('radar', [])
  }
}
