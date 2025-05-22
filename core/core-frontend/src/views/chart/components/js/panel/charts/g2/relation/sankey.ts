import { G2ChartView, G2DrawOptions } from '../../../types/impl/g2'
import { flow, hexColorToRGBA, parseJson } from '@/views/chart/components/js/util'
import { setGradientColor, TOOLTIP_ITEM_TPL } from '../../../common/common_antv'
import { useI18n } from '@/hooks/web/useI18n'
import { defaultsDeep } from 'lodash-es'
import { Chart as G2Chart, G2Spec } from '@antv/g2'
import { valueFormatter } from '../../../../formatter'

const { t } = useI18n()
const DEFAULT_DATA = []

/**
 * 桑基图
 */
export class G2ChartBar extends G2ChartView {
  axisConfig: AxisConfig = {
    xAxis: {
      name: `${t('chart.drag_block_type_axis_start')} / ${t('chart.dimension')}`,
      limit: 1,
      type: 'd'
    },
    xAxisExt: {
      name: `${t('chart.drag_block_type_axis_end')} / ${t('chart.dimension')}`,
      limit: 1,
      type: 'd',
      allowEmpty: true
    },
    yAxis: {
      name: `${t('chart.chart_data')} / ${t('chart.quota')}`,
      limit: 1,
      type: 'q'
    }
  }
  properties: EditorProperty[] = [
    'background-overall-component',
    'border-style',
    'basic-style-selector',
    'label-selector',
    'tooltip-selector',
    'title-selector',
    'jump-set',
    'linkage'
  ]
  propertyInner: EditorPropertyInner = {
    'label-selector': ['color', 'fontSize'],
    'tooltip-selector': ['fontSize', 'color', 'backgroundColor', 'tooltipFormatter', 'show'],
    'background-overall-component': ['all'],
    'border-style': ['all'],
    'basic-style-selector': ['colors', 'alpha', 'gradient'],
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
    'function-cfg': ['slider', 'emptyDataStrategy']
  }
  axis: AxisType[] = ['xAxis', 'xAxisExt', 'yAxis', 'filter', 'extLabel', 'extTooltip']

  async drawChart(drawOptions: G2DrawOptions<G2Chart>): Promise<G2Chart> {
    const { chart, container, action } = drawOptions
    if (!chart.data?.data?.length) {
      return
    }
    // data
    const data: Array<any> = chart.data.data

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

    const initOptions: G2Spec = {
      type: 'sankey',
      autoFit: true,
      data: {
        value: {
          links: data
        }
      },
      encode: {
        linkColor: {
          type: 'transform',
          value: d => d.source.key
        }
      },
      interaction: {
        elementHighlight: true
      },
      style: {
        nodeStrokeOpacity: 0
      },
      layout: {
        nodeSort: (a, b) => {
          // 这里是前端自己排序
          if (chart.yAxis?.[0]) {
            if (chart.yAxis[0].sort === 'asc') {
              return a.value - b.value
            } else if (chart.yAxis[0].sort === 'desc') {
              return b.value - a.value
            }
          }

          if (chart.xAxis?.[0] && a.sourceLinks.length > 0) {
            if (chart.xAxis[0].sort === 'custom_sort' && chart.xAxis[0].customSort) {
              return (
                chart.xAxis[0].customSort.indexOf(a.key) - chart.xAxis[0].customSort.indexOf(b.key)
              )
            } else if (chart.xAxis[0].sort === 'asc') {
              return a.key.localeCompare(b.key)
            } else if (chart.xAxis[0].sort === 'desc') {
              return b.key.localeCompare(a.key)
            }
          }
          if (chart.xAxisExt?.[0] && a.targetLinks.length > 0) {
            if (chart.xAxisExt[0].sort === 'custom_sort' && chart.xAxisExt[0].customSort) {
              return (
                chart.xAxisExt[0].customSort.indexOf(a.key) -
                chart.xAxisExt[0].customSort.indexOf(b.key)
              )
            } else if (chart.xAxisExt[0].sort === 'asc') {
              return a.key.localeCompare(b.key)
            } else if (chart.xAxisExt[0].sort === 'desc') {
              return b.key.localeCompare(a.key)
            }
          }

          return b.value - a.value
        }
      }
    }

    const options: G2Spec = this.setupOptions(chart, initOptions)
    const newChart = new G2Chart({ container })
    newChart.options(options)

    newChart.on('edge:click', action)

    return newChart
  }

  protected configTheme(chart: Chart, options: G2Spec): G2Spec {
    const customAttr = parseJson(chart.customAttr)
    const colors: string[] = []
    if (customAttr.basicStyle) {
      const basicStyle = customAttr.basicStyle
      basicStyle.colors.forEach(ele => {
        let color = hexColorToRGBA(ele, basicStyle.alpha)
        if (basicStyle.gradient) {
          color = setGradientColor(color, true)
        }
        colors.push(color)
      })
    }
    const customStyle = parseJson(chart.customStyle)
    let bgColor
    if (customStyle.background) {
      bgColor = hexColorToRGBA(customStyle.background.color, customStyle.background.alpha)
    }
    const theme = {
      color: colors[0],
      category10: colors,
      category20: colors,
      view: {
        viewFill: bgColor
      }
    }
    return { ...options, theme }
  }

  protected configLabel(chart: Chart, options: G2Spec): G2Spec {
    const { label } = parseJson(chart.customAttr)
    if (!label.show) {
      const hiedLabel = {
        style: {
          labelText: ''
        }
      }
      return defaultsDeep(options, hiedLabel)
    }
    const labelStyle = {
      style: {
        labelFill: label.color,
        labelFontSize: label.fontSize,
        labelFillOpacity: 1,
        labelTransform: label.fullDisplay ? [] : [{ type: 'overlapHide' }, { type: 'exceedAdjust' }]
      }
    }
    return defaultsDeep(options, labelStyle)
  }

  protected configTooltip(chart: Chart, options: G2Spec): G2Spec {
    const { tooltip } = parseJson(chart.customAttr)
    if (!tooltip.show) {
      return {
        ...options,
        tooltip: false
      }
    }
    let g2TooltipWrapper = document.getElementById('G2-TOOLTIP-WRAPPER')
    if (!g2TooltipWrapper) {
      g2TooltipWrapper = document.createElement('div')
      g2TooltipWrapper.id = 'G2-TOOLTIP-WRAPPER'
      g2TooltipWrapper.style.position = 'absolute'
      g2TooltipWrapper.style.pointerEvents = 'none'
      g2TooltipWrapper.style.zIndex = '9999'
      document.body.appendChild(g2TooltipWrapper)
    }

    const tooltipOptions: G2Spec = {
      tooltip: {
        linkItems: [d => d],
        nodeItems: [d => d]
      },
      interaction: {
        tooltip: {
          mount: g2TooltipWrapper,
          css: {
            '.g2-tooltip': {
              background: tooltip.backgroundColor
            },
            '.g2-tooltip-title': {
              color: tooltip.color,
              'font-size': `${tooltip.fontSize}px`
            },
            '.g2-tooltip-list-item-name-label': {
              color: tooltip.color,
              'font-size': `${tooltip.fontSize}px`
            },
            '.g2-tooltip-list-item-value': {
              color: tooltip.color,
              'font-size': `${tooltip.fontSize}px`
            }
          },
          render: (_, { items }) => {
            const head = items[0]
            let label = ''
            let value = ''
            const marker = head.color
            // 左边节点
            if (head.sourceLinks?.length) {
              label = head.key
              value = valueFormatter(head.value, tooltip.tooltipFormatter)
            }
            // 中间连线
            if (head.source) {
              label = head.source.key + ' -> ' + head.target.key
              value = valueFormatter(head.value, tooltip.tooltipFormatter)
            }
            //  右边节点
            if (head.targetLinks?.length) {
              label = head.key
              value = valueFormatter(head.value, tooltip.tooltipFormatter)
            }
            const itemsHtml = TOOLTIP_ITEM_TPL.replace('{marker}', marker)
              .replace('{label}', label)
              .replace('{value}', value)
            const listHtml = `<ul class="g2-tooltip-list" style="margin: 0px; list-style-type: none; padding: 0px;">${itemsHtml}</ul>`
            return listHtml
          }
        }
      }
    }
    return defaultsDeep(options, tooltipOptions)
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

  protected setupOptions(chart: Chart, options: G2Spec): G2Spec {
    return flow(this.configTheme, this.configLabel, this.configTooltip)(chart, options)
  }

  constructor(name = 'sankey') {
    super(name, DEFAULT_DATA)
  }
}
