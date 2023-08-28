import {
  G2PlotChartView,
  G2PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/g2plot'
import { ScatterOptions, Scatter as G2Scatter } from '@antv/g2plot/esm/plots/scatter'
import { flow, parseJson } from '../../../util'
import { singleDimensionTooltipFormatter, valueFormatter } from '../../../formatter'
import { getPadding } from '../../common/common_antv'
import { Datum } from '@antv/g2plot/esm/types/common'
import { useI18n } from '@/hooks/web/useI18n'

const { t } = useI18n()
/**
 * 散点图
 */
export class Scatter extends G2PlotChartView<ScatterOptions, G2Scatter> {
  properties: EditorProperty[] = [
    'background-overall-component',
    'basic-style-selector',
    'x-axis-selector',
    'y-axis-selector',
    'title-selector',
    'label-selector',
    'tooltip-selector',
    'legend-selector'
  ]
  propertyInner: EditorPropertyInner = {
    'basic-style-selector': ['colors', 'alpha', 'scatterSymbol', 'scatterSymbolSize'],
    'label-selector': ['fontSize', 'color'],
    'tooltip-selector': ['fontSize', 'color', 'backgroundColor'],
    'x-axis-selector': [
      'vPosition',
      'name',
      'color',
      'fontSize',
      'axisLine',
      'splitLine',
      'axisForm',
      'axisLabel'
    ],
    'y-axis-selector': [
      'vPosition',
      'name',
      'color',
      'fontSize',
      'axisValue',
      'axisLine',
      'splitLine',
      'axisForm',
      'axisLabel',
      'axisLabelFormatter'
    ],
    'title-selector': [
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
  axis: AxisType[] = ['xAxis', 'yAxis', 'extBubble', 'filter', 'drill', 'extLabel', 'extTooltip']
  axisConfig: AxisConfig = {
    ...this['axisConfig'],
    extBubble: {
      name: `${t('chart.bubble_size')} / ${t('chart.quota')}`,
      type: 'q',
      limit: 1
    }
  }
  public drawChart(drawOptions: G2PlotDrawOptions<G2Scatter>) {
    const { chart, container, action } = drawOptions
    if (!chart.data?.data) {
      return
    }
    const data = chart.data.data
    const baseOptions: ScatterOptions = {
      data: data,
      xField: 'field',
      yField: 'value',
      colorField: 'category',
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
            start: [{ trigger: 'point:mousemove', action: 'tooltip:show' }],
            end: [{ trigger: 'point:mouseleave', action: 'tooltip:hide' }]
          }
        }
      ]
    }
    const options = this.setupOptions(chart, baseOptions)
    const newChart = new G2Scatter(container, options)
    newChart.on('point:click', action)
    return newChart
  }

  protected configTooltip(chart: Chart, options: ScatterOptions): ScatterOptions {
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

  protected configBasicStyle(chart: Chart, options: ScatterOptions): ScatterOptions {
    const customAttr = parseJson(chart.customAttr)
    const basicStyle = customAttr.basicStyle
    if (chart.extBubble?.length) {
      return {
        ...options,
        size: [5, 30],
        sizeField: 'popSize',
        shape: basicStyle.scatterSymbol
      }
    }
    return {
      ...options,
      size: basicStyle.scatterSymbolSize,
      shape: basicStyle.scatterSymbol
    }
  }

  protected configYAxis(chart: Chart, options: ScatterOptions): ScatterOptions {
    const tmpOptions = super.configYAxis(chart, options)
    if (!tmpOptions.yAxis) {
      return tmpOptions
    }
    const yAxis = parseJson(chart.customStyle).yAxis
    if (tmpOptions.yAxis.label) {
      tmpOptions.yAxis.label.formatter = value => {
        return valueFormatter(value, yAxis.axisLabelFormatter)
      }
    }
    const axisValue = yAxis.axisValue
    if (!axisValue?.auto) {
      const axis = {
        yAxis: {
          ...tmpOptions.yAxis,
          min: axisValue.min,
          max: axisValue.max,
          minLimit: axisValue.min,
          maxLimit: axisValue.max,
          tickCount: axisValue.splitCount
        }
      }
      return { ...tmpOptions, ...axis }
    }
    return tmpOptions
  }

  protected setupOptions(chart: Chart, options: ScatterOptions) {
    return flow(
      this.configTheme,
      this.configLabel,
      this.configTooltip,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configAnalyse,
      this.configSlider,
      this.configBasicStyle
    )(chart, options)
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    return this.setupVerticalAxis(chart)
  }

  constructor() {
    super('scatter', [])
  }
}
