import {
  G2PlotChartView,
  G2PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/g2plot'
import { Sankey, SankeyOptions } from '@antv/g2plot/esm/plots/sankey'
import { getPadding, setGradientColor } from '@/views/chart/components/js/panel/common/common_antv'
import { cloneDeep, get } from 'lodash-es'
import { flow, hexColorToRGBA, parseJson } from '@/views/chart/components/js/util'
import { valueFormatter } from '@/views/chart/components/js/formatter'

import { Datum } from '@antv/g2plot/esm/types/common'
import { useI18n } from '@/hooks/web/useI18n'
import {
  SANKEY_AXIS_TYPE,
  SANKEY_EDITOR_PROPERTY,
  SANKEY_EDITOR_PROPERTY_INNER
} from '@/views/chart/components/js/panel/charts/others/sankey-common'

const { t } = useI18n()
const DEFAULT_DATA = []

/**
 * 区间条形图
 */
export class RangeBar extends G2PlotChartView<SankeyOptions, Sankey> {
  axisConfig = {
    ...this['axisConfig'],
    xAxis: {
      name: `${t('chart.drag_block_type_axis_start')} / ${t('chart.dimension')}`,
      limit: 1,
      type: 'd'
    },
    xAxisExt: {
      name: `${t('chart.drag_block_type_axis_end')} / ${t('chart.dimension')}`,
      limit: 1,
      type: 'd'
    },
    yAxis: {
      name: `${t('chart.chart_data')} / ${t('chart.quota')}`,
      limit: 1,
      type: 'q'
    }
  }
  properties = SANKEY_EDITOR_PROPERTY
  propertyInner = {
    ...SANKEY_EDITOR_PROPERTY_INNER,
    'label-selector': ['color', 'fontSize'],
    'tooltip-selector': ['fontSize', 'color', 'backgroundColor', 'tooltipFormatter', 'show']
  }
  axis: AxisType[] = [...SANKEY_AXIS_TYPE]
  protected baseOptions: SankeyOptions = {
    data: [],
    sourceField: 'source',
    targetField: 'target',
    weightField: 'value',
    rawFields: ['dimensionList', 'quotaList'],
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

  drawChart(drawOptions: G2PlotDrawOptions<Sankey>): Sankey {
    const { chart, container, action } = drawOptions
    if (!chart.data?.data?.length) {
      return
    }
    // data
    const data: Array<any> = cloneDeep(chart.data.data)

    data.forEach(d => {
      if (d.dimensionList) {
        if (d.dimensionList[0]) {
          d.source = d.dimensionList[0].value
        }
        if (d.dimensionList[1]) {
          d.target = d.dimensionList[1].value
        }
      }
    })

    // options
    const initOptions: SankeyOptions = {
      ...this.baseOptions,
      appendPadding: getPadding(chart),
      data,
      nodeSort: (a, b) => {
        // 这里是前端自己排序
        if (chart.yAxis && chart.yAxis[0]) {
          if (chart.yAxis[0].sort === 'asc') {
            return a.value - b.value
          } else if (chart.yAxis[0].sort === 'desc') {
            return b.value - a.value
          }
        }

        if (chart.xAxis && chart.xAxis[0] && a.sourceLinks.length > 0) {
          if (chart.xAxis[0].sort === 'custom_sort' && chart.xAxis[0].customSort) {
            return (
              chart.xAxis[0].customSort.indexOf(a.name) - chart.xAxis[0].customSort.indexOf(b.name)
            )
          } else if (chart.xAxis[0].sort === 'asc') {
            return a.name.localeCompare(b.name)
          } else if (chart.xAxis[0].sort === 'desc') {
            return b.name.localeCompare(a.name)
          }
        }
        if (chart.xAxisExt && chart.xAxisExt[0] && a.targetLinks.length > 0) {
          if (chart.xAxisExt[0].sort === 'custom_sort' && chart.xAxisExt[0].customSort) {
            return (
              chart.xAxisExt[0].customSort.indexOf(a.name) -
              chart.xAxisExt[0].customSort.indexOf(b.name)
            )
          } else if (chart.xAxisExt[0].sort === 'asc') {
            return a.name.localeCompare(b.name)
          } else if (chart.xAxisExt[0].sort === 'desc') {
            return b.name.localeCompare(a.name)
          }
        }

        return b.value - a.value
      }
    }

    const options = this.setupOptions(chart, initOptions)

    // 开始渲染
    const newChart = new Sankey(container, options)

    newChart.on('edge:click', action)

    return newChart
  }

  protected configTooltip(chart: Chart, options: SankeyOptions): SankeyOptions {
    let tooltip
    let customAttr: DeepPartial<ChartAttr>
    if (chart.customAttr) {
      customAttr = parseJson(chart.customAttr)
      // tooltip
      if (customAttr.tooltip) {
        const t = JSON.parse(JSON.stringify(customAttr.tooltip))
        if (t.show) {
          tooltip = {
            showTitle: false,
            showMarkers: false,
            shared: false,
            // 内置：node 不显示 tooltip，edge 显示 tooltip
            showContent: items => {
              return !get(items, [0, 'data', 'isNode'])
            },
            formatter: (datum: Datum) => {
              const { source, target, value } = datum
              return {
                name: source + ' -> ' + target,
                value: valueFormatter(value, t.tooltipFormatter)
              }
            }
          }
        } else {
          tooltip = false
        }
      }
    }
    return { ...options, tooltip }
  }

  protected configBasicStyle(chart: Chart, options: SankeyOptions): SankeyOptions {
    const basicStyle = parseJson(chart.customAttr).basicStyle

    let color = basicStyle.colors
    color = color.map(ele => {
      const tmp = hexColorToRGBA(ele, basicStyle.alpha)
      if (basicStyle.gradient) {
        return setGradientColor(tmp, true)
      } else {
        return tmp
      }
    })

    options = {
      ...options,
      color
    }
    return options
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    const { customAttr, senior } = chart
    const { label } = customAttr
    if (!['left', 'middle', 'right'].includes(label.position)) {
      label.position = 'middle'
    }
    senior.functionCfg.emptyDataStrategy = 'ignoreData'
    return chart
  }

  protected configLabel(chart: Chart, options: SankeyOptions): SankeyOptions {
    const labelAttr = parseJson(chart.customAttr).label
    if (labelAttr.show) {
      const label = {
        //...tmpOptions.label,
        formatter: ({ name }) => name,
        callback: (x: number[]) => {
          const isLast = x[1] === 1 // 最后一列靠边的节点
          return {
            style: {
              fill: labelAttr.color,
              fontSize: labelAttr.fontSize,
              textAlign: isLast ? 'end' : 'start'
            },
            offsetX: isLast ? -8 : 8
          }
        },
        layout: [{ type: 'hide-overlap' }, { type: 'limit-in-canvas' }]
      }
      return {
        ...options,
        label
      }
    } else {
      return {
        ...options,
        label: false
      }
    }
  }

  protected setupOptions(chart: Chart, options: SankeyOptions): SankeyOptions {
    return flow(
      this.configTheme,
      this.configBasicStyle,
      this.configLabel,
      this.configTooltip,
      this.configLegend,
      this.configSlider,
      this.configAnalyseHorizontal,
      this.configEmptyDataStrategy
    )(chart, options)
  }

  constructor(name = 'sankey') {
    super(name, DEFAULT_DATA)
  }
}
