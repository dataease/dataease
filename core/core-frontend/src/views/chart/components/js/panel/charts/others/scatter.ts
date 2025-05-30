import {
  G2PlotChartView,
  G2PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/g2plot'
import type { ScatterOptions, Scatter as G2Scatter } from '@antv/g2plot/esm/plots/scatter'
import { flow, parseJson } from '../../../util'
import { valueFormatter } from '../../../formatter'
import {
  configPlotTooltipEvent,
  getPadding,
  getTooltipContainer,
  TOOLTIP_TPL
} from '../../common/common_antv'
import { useI18n } from '@/hooks/web/useI18n'
import { defaults, isEmpty } from 'lodash-es'
import { DEFAULT_LEGEND_STYLE } from '@/views/chart/components/editor/util/chart'
import { type Datum } from '@antv/g2plot/esm'
import { Group } from '@antv/g-canvas'

const { t } = useI18n()
/**
 * 散点图
 */
export class Scatter extends G2PlotChartView<ScatterOptions, G2Scatter> {
  properties: EditorProperty[] = [
    'background-overall-component',
    'border-style',
    'basic-style-selector',
    'x-axis-selector',
    'y-axis-selector',
    'title-selector',
    'label-selector',
    'tooltip-selector',
    'legend-selector',
    'jump-set',
    'linkage'
  ]
  propertyInner: EditorPropertyInner = {
    'basic-style-selector': [
      'colors',
      'alpha',
      'scatterSymbol',
      'scatterSymbolSize',
      'seriesColor'
    ],
    'label-selector': ['fontSize', 'color', 'labelFormatter'],
    'tooltip-selector': ['fontSize', 'color', 'backgroundColor', 'seriesTooltipFormatter', 'show'],
    'x-axis-selector': [
      'position',
      'name',
      'color',
      'fontSize',
      'axisLine',
      'splitLine',
      'axisForm',
      'axisLabel'
    ],
    'y-axis-selector': [
      'position',
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
    xAxis: {
      name: `${t('chart.drag_block_type_axis')} / ${t('chart.dimension')}`,
      type: 'd'
    },
    yAxis: {
      ...this['axisConfig'].yAxis,
      limit: undefined,
      allowEmpty: false
    },
    extBubble: {
      name: `${t('chart.bubble_size')} / ${t('chart.quota')}`,
      type: 'q',
      limit: 1,
      allowEmpty: true
    }
  }
  async drawChart(drawOptions: G2PlotDrawOptions<G2Scatter>): Promise<G2Scatter> {
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
      meta: {
        field: {
          type: 'cat'
        }
      },
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
        }
      ]
    }
    const options = this.setupOptions(chart, baseOptions)
    const { Scatter: G2Scatter } = await import('@antv/g2plot/esm/plots/scatter')
    const newChart = new G2Scatter(container, options)
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

  protected configTooltip(chart: Chart, options: ScatterOptions): ScatterOptions {
    const customAttr: DeepPartial<ChartAttr> = parseJson(chart.customAttr)
    const tooltipAttr = customAttr.tooltip
    if (!tooltipAttr.show) {
      return {
        ...options,
        tooltip: false
      }
    }
    const formatterMap = tooltipAttr.seriesTooltipFormatter
      ?.filter(i => i.show)
      .reduce((pre, next) => {
        pre[next.seriesId] = next
        return pre
      }, {}) as Record<string, SeriesFormatter>
    const VALID_ITEMS = ['value', 'popSize']
    const tooltip: ScatterOptions['tooltip'] = {
      showTitle: true,
      customItems(originalItems) {
        if (!tooltipAttr.seriesTooltipFormatter?.length) {
          return originalItems
        }
        const head = originalItems[0]
        // 非原始数据
        if (!head.data.quotaList) {
          return originalItems
        }
        const result = []
        originalItems
          .filter(item => VALID_ITEMS.includes(item.name))
          .forEach(item => {
            let formatter = formatterMap[`${item.data.quotaList[0].id}-yAxis`]
            if (item.name === 'popSize') {
              formatter = formatterMap[`${item.data.quotaList[1].id}-extBubble`]
            }
            if (!formatter) {
              return
            }
            const value = valueFormatter(parseFloat(item.value as string), formatter.formatterCfg)
            const name = isEmpty(formatter.chartShowName) ? formatter.name : formatter.chartShowName
            result.push({ ...item, name, value })
          })
        head.data.dynamicTooltipValue?.forEach(item => {
          const formatter = formatterMap[item.fieldId]
          if (formatter) {
            const value = valueFormatter(parseFloat(item.value), formatter.formatterCfg)
            const name = isEmpty(formatter.chartShowName) ? formatter.name : formatter.chartShowName
            result.push({ color: 'grey', name, value })
          }
        })
        return result
      },
      container: getTooltipContainer(`tooltip-${chart.id}`),
      itemTpl: TOOLTIP_TPL,
      enterable: true
    }
    return {
      ...options,
      tooltip
    }
  }

  protected configLegend(chart: Chart, options: ScatterOptions): ScatterOptions {
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
        fill: style.fill
      }
    }
    return optionTmp
  }

  protected configLabel(chart: Chart, options: ScatterOptions): ScatterOptions {
    const tmpOption = super.configLabel(chart, options)
    if (!tmpOption.label) {
      return options
    }
    const { label: labelAttr } = parseJson(chart.customAttr)
    tmpOption.label.style.fill = labelAttr.color
    const label = {
      ...tmpOption.label,
      formatter: function (data: Datum) {
        const value = valueFormatter(data.value, labelAttr.labelFormatter)
        const group = new Group({})
        group.addShape({
          type: 'text',
          attrs: {
            x: 0,
            y: 0,
            data,
            text: value,
            textAlign: 'start',
            textBaseline: 'top',
            fontSize: labelAttr.fontSize,
            fontFamily: chart.fontFamily,
            fill: labelAttr.color
          }
        })
        return group
      }
    }
    return {
      ...tmpOption,
      label
    }
  }

  protected setupOptions(chart: Chart, options: ScatterOptions) {
    return flow(
      this.configTheme,
      this.configColor,
      this.configLabel,
      this.configTooltip,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configSlider,
      this.configBasicStyle
    )(chart, options)
  }

  constructor() {
    super('scatter', [])
  }
}
