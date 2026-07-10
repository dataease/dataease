import { flow, hexColorToRGBA, parseJson } from '@/views/chart/components/js/util'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { useI18n } from '@/hooks/web/useI18n'
import { cloneDeep, defaultsDeep } from 'lodash-es'
import { Chart as G2Chart, G2Spec } from '@antv/g2'
import { G2ChartView, G2DrawOptions } from '../../../types/impl/g2'
import { handleChartDashboardHidden, TOOLTIP_ITEM_TPL } from '../../../common/common_antv'

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
    'label-selector': [
      'color',
      'fontSize',
      'showDimension',
      'showQuota',
      'showProportion',
      'quotaLabelFormatter'
    ],
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
        },
        state: {
          selected: {
            stroke: 'black',
            lineWidth: 1
          },
          unselected: {
            opacity: 0.5
          }
        }
      }
      const options = this.setupOptions(chart, initOptions)
      const newChart = new G2Chart({ container })
      handleChartDashboardHidden(chart, options)
      newChart.options(options)
      const handlePointClick = param => {
        const pointData = param?.target?.__data__?.data?.data
        if (pointData?.name === t('commons.all')) {
          return
        }
        const actionParams = {
          ...param,
          data: {
            data: {
              ...pointData
            }
          }
        }
        action(actionParams)
      }
      // 标签层有时会拦截事件，这里同时监听 label 点击，确保下钻/联动可触发。
      ;['element:click', 'label:click'].forEach(eventName =>
        newChart.on(eventName, handlePointClick)
      )
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

    // 计算比例的总值
    const calculateTotal = (data: any[]): number => {
      let total = 0
      const traverse = (arr: any[]) => {
        arr?.forEach(item => {
          if (!item.children || !item.children.length) {
            total += item.value ?? 0
          } else {
            traverse(item.children)
          }
        })
      }
      traverse(data)
      return total
    }

    const total = calculateTotal(options.data.value.children)

    const getLabelContent = d => {
      const contentItems = []
      if (label.showDimension) {
        contentItems.push(d.data.field)
      }
      if (label.showQuota) {
        const formattedValue = valueFormatter(d.data.value, label.quotaLabelFormatter)
        contentItems.push(formattedValue)
      }
      if (label.showProportion && total > 0) {
        const percentage = `${(Math.round((d.data.value / total) * 10000) / 100).toFixed(
          label.reserveDecimalCount
        )}%`
        if (label.showDimension && label.showQuota) {
          contentItems.push(`(${percentage})`)
        } else {
          contentItems.push(percentage)
        }
      }
      return contentItems.length > 1 ? contentItems.join('\n') : contentItems.join(' ')
    }

    const canLabelFitInsideCircle = (d, text: string): boolean => {
      const radius = Number(d?.r)
      const diameterByRadius = radius > 0 ? radius * 2 : 0
      const width = Number(d?.width)
      const height = Number(d?.height)
      const diameterBySize = width > 0 && height > 0 ? Math.min(width, height) : 0
      const diameter = diameterByRadius || diameterBySize
      if (!diameter) {
        return true
      }
      const fontSize = Number(label.fontSize || 12)
      const lines = `${text ?? ''}`.split('\n').filter(line => line !== '')
      if (!lines.length) {
        return true
      }

      // 使用 canvas 实测文本宽度，避免字符数估算过于保守。
      const canvas = document.createElement('canvas')
      const ctx = canvas.getContext('2d')
      if (!ctx) {
        return true
      }
      ctx.font = `${fontSize}px sans-serif`

      const r = diameter / 2
      const lineHeight = fontSize * 1.05
      const textHeight = lines.length * lineHeight
      const verticalPadding = 0.98
      if (textHeight > diameter * verticalPadding) {
        return false
      }

      const widthPadding = 0.98
      for (let i = 0; i < lines.length; i++) {
        const lineWidth = ctx.measureText(lines[i]).width
        // 计算该行中心点在圆心坐标系中的 y 偏移，再求该高度上的最大弦长。
        const y = (i + 0.5) * lineHeight - textHeight / 2
        if (Math.abs(y) > r) {
          return false
        }
        const maxChordWidth = 2 * Math.sqrt(r * r - y * y)
        if (lineWidth > maxChordWidth * widthPadding) {
          return false
        }
      }
      return true
    }

    const labelStyle = {
      style: {
        labelFill: label.color,
        labelFontSize: label.fontSize,
        labelText: d => {
          if (d.height) {
            return ''
          }
          const labelContent = getLabelContent(d)
          if (!label.fullDisplay && !canLabelFitInsideCircle(d, labelContent)) {
            return ''
          }
          return labelContent
        },
        // 让鼠标事件尽量透传到圆形图元，避免 hover 标签时 tooltip 无法触发。
        labelPointerEvents: 'none',
        // 使用显式换行和自定义越界判断，避免 G2 再次折行/裁剪后只显示第一行。
        labelWordWrap: false,
        labelTransform: []
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
      show: true,
      showDimension: true,
      showQuota: true,
      showProportion: false,
      reserveDecimalCount: 2
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
