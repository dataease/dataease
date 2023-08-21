import { TreemapOptions, Treemap as G2Treemap } from '@antv/g2plot/esm/plots/treemap'
import { G2PlotChartView, G2PlotDrawOptions } from '../../types/impl/g2plot'
import { flow, parseJson } from '../../../util'
import { getPadding } from '../../common/common_antv'
import { formatterItem, singleDimensionTooltipFormatter, valueFormatter } from '../../../formatter'
import { Label } from '@antv/g2plot/lib/types/label'
import { Datum } from '@antv/g2plot/esm/types/common'
import { useI18n } from '@/hooks/web/useI18n'

const { t } = useI18n()

/**
 * 矩形树图
 */
export class Treemap extends G2PlotChartView<TreemapOptions, G2Treemap> {
  properties: EditorProperty[] = [
    'background-overall-component',
    'basic-style-selector',
    'title-selector',
    'legend-selector',
    'label-selector',
    'tooltip-selector'
  ]
  propertyInner: EditorPropertyInner = {
    'background-overall-component': ['all'],
    'basic-style-selector': ['colors', 'alpha'],
    'label-selector': ['fontSize', 'color'],
    'legend-selector': ['icon', 'orient', 'textStyle', 'hPosition', 'vPosition'],
    'tooltip-selector': ['fontSize', 'color', 'backgroundColor'],
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
    ]
  }
  axis: AxisType[] = ['xAxis', 'yAxis', 'filter', 'drill', 'extLabel', 'extTooltip']
  axisConfig: AxisConfig = {
    xAxis: {
      name: `${t('chart.drag_block_treemap_label')}/${t('chart.dimension')}`,
      type: 'd'
    },
    yAxis: {
      name: `${t('chart.drag_block_treemap_size')}/${t('chart.quota')}`,
      limit: 1
    }
  }

  public drawChart(drawOptions: G2PlotDrawOptions<G2Treemap>): G2Treemap {
    const { chart, container, action } = drawOptions
    if (!chart.data?.data) {
      return
    }
    const data = chart.data.data
    const baseOptions = {
      data: {
        name: 'root',
        children: data
      },
      colorField: 'name',
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
            start: [{ trigger: 'element:mousemove', action: 'tooltip:show' }],
            end: [{ trigger: 'element:mouseleave', action: 'tooltip:hide' }]
          }
        }
      ]
    }
    const options = this.setupOptions(chart, baseOptions)
    const newChart = new G2Treemap(container, options)
    newChart.on('polygon:click', action)
    return newChart
  }
  protected configTooltip(chart: Chart, options: TreemapOptions): TreemapOptions {
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
        return singleDimensionTooltipFormatter(param, chart, 'name')
      }
    }
    return { ...options, tooltip }
  }
  protected configLabel(chart: Chart, options: TreemapOptions): TreemapOptions {
    const customAttr: DeepPartial<ChartAttr> = parseJson(chart.customAttr)
    const labelAttr = customAttr.label
    if (!labelAttr.show) {
      return {
        ...options,
        label: false
      }
    }
    const label: Label = {
      style: {
        fill: labelAttr.color,
        fontSize: labelAttr.fontSize
      },
      formatter: function (param: Datum) {
        const yAxis = chart.yAxis
        let res = param.value
        for (let i = 0; i < yAxis.length; i++) {
          const f = yAxis[i]
          if (f.name === param.category) {
            let formatterCfg = formatterItem
            if (f.formatterCfg) {
              formatterCfg = f.formatterCfg
            }
            res = valueFormatter(param.value, formatterCfg)
            break
          }
        }
        return res
      }
    }
    return { ...options, label }
  }
  protected setupOptions(chart: Chart, options: TreemapOptions): TreemapOptions {
    return flow(
      this.configTheme,
      this.configLabel,
      this.configTooltip,
      this.configLegend
    )(chart, options)
  }

  constructor() {
    super('treemap', [])
  }
}
