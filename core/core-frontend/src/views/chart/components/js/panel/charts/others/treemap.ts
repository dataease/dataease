import { TreemapOptions, Treemap as G2Treemap } from '@antv/g2plot/esm/plots/treemap'
import { G2PlotChartView, G2PlotDrawOptions } from '../../types/impl/g2plot'
import { flow, parseJson, setUpSingleDimensionSeriesColor } from '../../../util'
import { getPadding, getTooltipSeriesTotalMap } from '../../common/common_antv'
import { valueFormatter } from '../../../formatter'
import { Label } from '@antv/g2plot/lib/types/label'
import { Datum } from '@antv/g2plot/esm/types/common'
import { useI18n } from '@/hooks/web/useI18n'
import isEmpty from 'lodash-es/isEmpty'

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
    'tooltip-selector',
    'jump-set',
    'linkage'
  ]
  propertyInner: EditorPropertyInner = {
    'background-overall-component': ['all'],
    'basic-style-selector': ['colors', 'alpha', 'seriesColor'],
    'label-selector': ['fontSize', 'color', 'showDimension', 'showQuota', 'showProportion'],
    'legend-selector': ['icon', 'orient', 'fontSize', 'color', 'hPosition', 'vPosition'],
    'tooltip-selector': ['fontSize', 'color', 'backgroundColor', 'seriesTooltipFormatter', 'show'],
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
      name: `${t('chart.drag_block_treemap_label')} / ${t('chart.dimension')}`,
      type: 'd'
    },
    yAxis: {
      name: `${t('chart.drag_block_treemap_size')} / ${t('chart.quota')}`,
      limit: 1
    }
  }

  public drawChart(drawOptions: G2PlotDrawOptions<G2Treemap>): G2Treemap {
    const { chart, container, action } = drawOptions
    if (!chart.data?.data?.length) {
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
    const { tooltip: tooltipAttr, label } = parseJson(chart.customAttr)
    const { yAxis } = chart
    if (!tooltipAttr.show) {
      return {
        ...options,
        tooltip: false
      }
    }
    const reserveDecimalCount = label.reserveDecimalCount
    const seriesTotalMap = getTooltipSeriesTotalMap(options.data.children)
    const formatterMap = tooltipAttr.seriesTooltipFormatter
      ?.filter(i => i.show)
      .reduce((pre, next) => {
        pre[next.id] = next
        return pre
      }, {}) as Record<string, SeriesFormatter>
    const tooltip: TreemapOptions['tooltip'] = {
      showTitle: true,
      title: () => undefined,
      customItems(originalItems) {
        let tooltipItems = originalItems
        if (tooltipAttr.seriesTooltipFormatter?.length) {
          tooltipItems = originalItems.filter(item => formatterMap[item.data.quotaList[0].id])
        }
        const result = []
        const head = originalItems[0]
        tooltipItems.forEach(item => {
          const formatter = formatterMap[item.data.quotaList[0].id] ?? yAxis[0]
          const value = valueFormatter(parseFloat(item.value as string), formatter.formatterCfg)
          // sync with label
          const percent = (
            Math.round((item.data.value / item.data.path[1].value) * 10000) / 100
          ).toFixed(reserveDecimalCount)
          const name = isEmpty(formatter.chartShowName) ? formatter.name : formatter.chartShowName
          result.push({ ...item, name, value: `${value ?? ''} (${percent}%)` })
        })
        head.data.dynamicTooltipValue?.forEach(item => {
          const formatter = formatterMap[item.fieldId]
          if (formatter) {
            const total = seriesTotalMap[item.fieldId]
            // sync with label
            const percent = (Math.round((item.value / total) * 10000) / 100).toFixed(
              reserveDecimalCount
            )
            const value = valueFormatter(parseFloat(item.value), formatter.formatterCfg)
            const name = isEmpty(formatter.chartShowName) ? formatter.name : formatter.chartShowName
            result.push({ color: 'grey', name, value: `${value ?? ''} (${percent}%)` })
          }
        })
        return result
      }
    }
    return {
      ...options,
      tooltip
    }
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
        let res = param.value
        const contentItems = []
        if (labelAttr.showDimension) {
          contentItems.push(param.field)
        }
        if (labelAttr.showQuota) {
          contentItems.push(valueFormatter(param.value, labelAttr.quotaLabelFormatter))
        }
        if (labelAttr.showProportion) {
          const percentage = `${(((param.value / param.parent.value) * 10000) / 100).toFixed(
            labelAttr.reserveDecimalCount
          )}%`
          contentItems.push(percentage)
        }
        res = contentItems.join('\n')
        return res
      }
    }
    return { ...options, label }
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    const { customAttr, customStyle } = chart
    const { label } = customAttr
    customAttr.label = {
      ...label,
      show: true,
      showDimension: true,
      showProportion: true,
      reserveDecimalCount: 2
    }
    const { legend } = customStyle
    legend.show = false
    return chart
  }
  public setupSeriesColor(chart: ChartObj, data?: any[]): ChartBasicStyle['seriesColor'] {
    data?.sort((a, b) => b.value - a.value)
    return setUpSingleDimensionSeriesColor(chart, data)
  }
  protected configColor(chart: Chart, options: TreemapOptions): TreemapOptions {
    const data = options.data.children
    data.sort((a, b) => b.value - a.value)
    const tmpOptions = this.configSingleDimensionColor(chart, { ...options, data })
    return { ...options, color: tmpOptions.color }
  }
  protected setupOptions(chart: Chart, options: TreemapOptions): TreemapOptions {
    return flow(
      this.configTheme,
      this.configColor,
      this.configLabel,
      this.configTooltip,
      this.configLegend
    )(chart, options, {}, this)
  }

  constructor() {
    super('treemap', [])
  }
}
