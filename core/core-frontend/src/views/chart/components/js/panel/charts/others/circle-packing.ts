import {
  G2PlotChartView,
  G2PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/g2plot'
import type {
  CirclePacking as G2CirclePacking,
  CirclePackingOptions
} from '@antv/g2plot/esm/plots/circle-packing'
import { flow, parseJson } from '@/views/chart/components/js/util'
import { getPadding } from '@/views/chart/components/js/panel/common/common_antv'
import { useI18n } from '@/hooks/web/useI18n'
import type { Datum } from '@antv/g2plot/esm/types/common'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { cloneDeep } from 'lodash-es'

const { t } = useI18n()
const DEFAULT_DATA = []
/**
 * 圆形填充图
 */
export class CirclePacking extends G2PlotChartView<CirclePackingOptions, G2CirclePacking> {
  properties: EditorProperty[] = [
    'basic-style-selector',
    'background-overall-component',
    'border-style',
    'label-selector',
    'legend-selector',
    'title-selector',
    'tooltip-selector',
    'jump-set',
    'linkage'
  ]
  propertyInner: EditorPropertyInner = {
    'background-overall-component': ['all'],
    'border-style': ['all'],
    'basic-style-selector': ['colors', 'alpha', 'circleBorderStyle'],
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
    'function-cfg': ['emptyDataStrategy'],
    'label-selector': ['color', 'fontSize'],
    'legend-selector': ['icon', 'orient', 'fontSize', 'color', 'hPosition', 'vPosition'],
    'tooltip-selector': ['color', 'fontSize', 'backgroundColor', 'tooltipFormatter', 'show']
  }
  axis: AxisType[] = ['xAxis', 'yAxis', 'filter', 'drill']
  axisConfig: AxisConfig = {
    xAxis: {
      name: `${t('chart.circle_packing_name')} / ${t('chart.dimension')}`,
      type: 'd',
      limit: 1
    },
    yAxis: {
      name: `${t('chart.circle_packing_value')} / ${t('chart.quota')}`,
      type: 'q',
      limit: 1
    }
  }
  async drawChart(drawOptions: G2PlotDrawOptions<G2CirclePacking>): Promise<G2CirclePacking> {
    const { chart, container, action } = drawOptions
    if (chart?.data?.data?.length) {
      // data
      const data = chart.data.data
      const { yAxis } = chart
      const ySort = yAxis[0]?.sort ?? 'none'
      const sort = {
        sort: (a, b) =>
          ySort === 'asc' ? a.value - b.value : ySort === 'desc' ? b.value - a.value : 0
      }
      // 将数据转为圆形填充图数据格式
      const getCirclePackingData = () => {
        const result = [{ name: t('commons.all'), children: [] }]
        const addNode = (nodes, item) => {
          const node = { ...item, name: item.name, children: [] }
          nodes.push(node)
        }
        data.forEach(item => addNode(result[0].children, item))
        return result[0]
      }
      // options
      const initOptions: CirclePackingOptions = {
        data: getCirclePackingData(),
        appendPadding: getPadding(chart),
        hierarchyConfig: {
          ...(ySort === 'none' ? {} : sort)
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
          }
        ]
      }
      const options = this.setupOptions(chart, initOptions)
      const { CirclePacking: G2CirclePacking } = await import(
        '@antv/g2plot/esm/plots/circle-packing'
      )
      const newChart = new G2CirclePacking(container, options)
      newChart.on('element:click', param => {
        const pointData = param?.data?.data
        if (pointData?.name === t('commons.all')) {
          return
        }
        const actionParams = {
          x: param.x,
          y: param.y,
          data: {
            data: {
              ...pointData
            }
          }
        }
        action(actionParams)
      })
      return newChart
    }
  }

  protected configBasicStyle(chart: Chart, options: CirclePackingOptions): CirclePackingOptions {
    // size
    const customAttr: DeepPartial<ChartAttr> = parseJson(chart.customAttr)
    const s = JSON.parse(JSON.stringify(customAttr.basicStyle))
    // 圆形边框样式
    const pointStyle = {
      stroke: s.circleBorderColor,
      lineWidth: s.circleBorderWidth ?? 0
    }
    const padding = s.circlePadding
    return {
      ...options,
      hierarchyConfig: {
        ...options.hierarchyConfig,
        padding: padding / 100 ?? 0
      },
      pointStyle
    }
  }

  protected configLabel(chart: Chart, options: CirclePackingOptions): CirclePackingOptions {
    const tmpOptions = super.configLabel(chart, options)
    if (!tmpOptions.label) {
      return {
        ...tmpOptions,
        label: false
      }
    }
    const { label: labelAttr } = parseJson(chart.customAttr)
    const label = {
      ...tmpOptions.label,
      textAlign: 'center',
      offsetY: 5,
      layout: labelAttr.fullDisplay ? [{ type: 'limit-in-plot' }] : tmpOptions.label.layout,
      formatter: (d: Datum) => {
        return d.children.length === 0 ? d.name : ''
      }
    }
    return {
      ...tmpOptions,
      label
    }
  }

  protected configTooltip(chart: Chart, options: CirclePackingOptions): CirclePackingOptions {
    const temOptions = super.configTooltip(chart, options)
    if (!temOptions.tooltip) {
      return temOptions
    }
    const tooltipAttr = parseJson(chart.customAttr).tooltip
    return {
      ...temOptions,
      tooltip: {
        ...temOptions,
        fields: ['name', 'value'],
        formatter: d => {
          let value = d.value
          if (tooltipAttr.tooltipFormatter) {
            value = valueFormatter(value, tooltipAttr.tooltipFormatter)
          }
          return { name: d.name, value }
        }
      }
    }
  }
  configEmptyDataStrategy(chart: Chart, options: CirclePackingOptions): CirclePackingOptions {
    const { functionCfg } = parseJson(chart.senior)
    const emptyDataStrategy = functionCfg.emptyDataStrategy
    const setChildren = children => {
      if (emptyDataStrategy === 'ignoreData') {
        for (let i = children.length - 1; i >= 0; i--) {
          let isNotNullChildren = []
          if (children[i].children?.length) {
            isNotNullChildren = children[i].children.filter(item => item.value !== null)
          }
          if (children[i].children?.length && isNotNullChildren.length) {
            setChildren(children[i].children)
          }
          if (children[i]?.hasOwnProperty('value') && children[i].value === null) {
            children.splice(i, 1)
          }
          if (!children[i]?.hasOwnProperty('value') && isNotNullChildren.length === 0) {
            children.splice(i, 1)
          }
        }
      } else {
        for (let i = children.length - 1; i >= 0; i--) {
          let isNotNullChildren = []
          if (children[i].children?.length) {
            isNotNullChildren = children[i].children.filter(item => item.value !== null)
            if (!isNotNullChildren.length) {
              children[i].children = []
              continue
            }
          }
          setChildren(children[i].children)
        }
      }
    }
    const data = cloneDeep(options.data.children)
    setChildren(data)
    options.data.children = data
    return options
  }
  setupDefaultOptions(chart: ChartObj): ChartObj {
    const { customAttr, customStyle, senior } = chart
    const { label, basicStyle } = customAttr
    const { legend } = customStyle
    senior.functionCfg.emptyDataStrategy = 'ignoreData'
    customAttr.label = {
      ...label,
      show: true
    }
    legend.show = false
    basicStyle.circleBorderWidth = 0
    basicStyle.circleBorderColor = '#fff'
    basicStyle.circlePadding = 0
    return chart
  }
  protected setupOptions(chart: Chart, options: CirclePackingOptions): CirclePackingOptions {
    return flow(
      this.configTheme,
      this.configEmptyDataStrategy,
      this.configBasicStyle,
      this.configLabel,
      this.configTooltip,
      this.configLegend
    )(chart, options)
  }

  constructor() {
    super('circle-packing', DEFAULT_DATA)
  }
}
