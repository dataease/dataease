import { flow, hexColorToRGBA, parseJson } from '@/views/chart/components/js/util'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { useI18n } from '@/hooks/web/useI18n'
import { cloneDeep, defaultsDeep } from 'lodash-es'
import { Chart as G2Chart, G2Spec } from '@antv/g2'
import { G2ChartView, G2DrawOptions } from '../../../types/impl/g2'
import { TOOLTIP_ITEM_TPL } from '../../../common/common_antv'

const { t } = useI18n()
const DEFAULT_DATA = []
/**
 * 圆形填充图
 */
export class CirclePacking extends G2ChartView {
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
  async drawChart(drawOptions: G2DrawOptions<G2Chart>): Promise<G2Chart> {
    const { chart, container, action } = drawOptions
    if (chart?.data?.data?.length) {
      // data
      const data = chart.data.data
      // options
      const initOptions: G2Spec = {
        type: 'pack',
        autoFit: true,
        data: {
          value: {
            field: t('commons.all'),
            children: data
          }
        },
        encode: { value: 'value', color: d => d.data.field },
        scale: {
          color: {
            type: 'ordinal'
          }
        }
      }
      const options = this.setupOptions(chart, initOptions)
      const newChart = new G2Chart({ container })
      newChart.options(options)
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

  protected configTheme(chart: Chart, options: G2Spec): G2Spec {
    const customAttr = parseJson(chart.customAttr)
    const colors: string[] = []
    if (customAttr.basicStyle) {
      const basicStyle = customAttr.basicStyle
      basicStyle.colors.forEach(ele => {
        colors.push(hexColorToRGBA(ele, basicStyle.alpha))
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

  protected configBasicStyle(chart: Chart, options: G2Spec): G2Spec {
    const { basicStyle } = parseJson(chart.customAttr)
    const styleOpt = {
      style: {
        stroke: basicStyle.circleBorderColor,
        lineWidth: basicStyle.circleBorderWidth ?? 0
      },
      layout: {
        padding: basicStyle.circlePadding ?? 0
      }
    }
    return defaultsDeep(options, styleOpt)
  }

  protected configLabel(chart: Chart, options: G2Spec): G2Spec {
    const { label } = parseJson(chart.customAttr)
    if (!label.show) {
      const labelHide = {
        style: {
          labelText: ''
        }
      }
      return defaultsDeep(options, labelHide)
    }
    const labelStyle = {
      style: {
        labelFill: label.color,
        labelFontSize: label.fontSize,
        labelText: d => {
          if (d.height) {
            return ''
          }
          return d.data.field
        },
        labelTransform: label.fullDisplay ? [] : [{ type: 'overflowHide' }]
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

    const tooltipMap = function (a) {
      return a
    }
    tooltipMap.title = undefined

    const tooltipOptions: G2Spec = {
      tooltip: tooltipMap,
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
            const value = valueFormatter(head.value, tooltip.tooltipFormatter)
            const itemsHtml = TOOLTIP_ITEM_TPL.replace('{marker}', head.color)
              .replace('{label}', head.data.field)
              .replace('{value}', value)
            const listHtml = `<ul class="g2-tooltip-list" style="margin: 0px; list-style-type: none; padding: 0px;">${itemsHtml}</ul>`
            return listHtml
          }
        }
      }
    }
    return defaultsDeep(options, tooltipOptions)
  }

  configEmptyDataStrategy(chart: Chart, options: G2Spec): G2Spec {
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
    const data = cloneDeep(options.data.value.children)
    setChildren(data)
    options.data.value.children = data
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

  protected configLegend(chart: Chart, options: G2Spec): G2Spec {
    const { legend } = parseJson(chart.customStyle)
    if (!legend.show) {
      return { ...options, legend: false }
    }
    const baseLegend = this.getLegend(chart)
    const tmpLegend = {
      style: {
        zIndex: d => -d.height
      },
      legend: {
        color: {
          ...baseLegend,
          itemMarkerSize: legend.size,
          itemMarker: legend.icon
        }
      }
    }
    defaultsDeep(options, tmpLegend)
    return options
  }

  protected setupOptions(chart: Chart, options: G2Spec): G2Spec {
    return flow(
      this.configTheme,
      this.configEmptyDataStrategy,
      this.configBasicStyle,
      this.configLabel,
      this.configTooltip,
      this.configLegend
    )(chart, options, {}, this)
  }

  constructor() {
    super('circle-packing', DEFAULT_DATA)
  }
}
