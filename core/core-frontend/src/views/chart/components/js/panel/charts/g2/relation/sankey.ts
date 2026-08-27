import { G2ChartView, G2DrawOptions } from '../../../types/impl/g2'
import { flow, hexColorToRGBA, parseJson } from '@/views/chart/components/js/util'
import {
  getG2Renderer,
  handleChartDashboardHidden,
  setGradientColor,
  TOOLTIP_ITEM_TPL
} from '../../../common/common_antv'
import { useI18n } from '@/hooks/web/useI18n'
import { defaultsDeep } from 'lodash-es'
import { Chart as G2Chart, G2Spec } from '@antv/g2'
import { valueFormatter } from '../../../../formatter'
import { createTooltipWrapper } from '../bar/barUtil'

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

    const sourceNodeCount = new Set(
      data.map(d => d.source).filter(value => value !== undefined && value !== null)
    ).size
    const targetNodeCount = new Set(
      data.map(d => d.target).filter(value => value !== undefined && value !== null)
    ).size
    const maxNodeCount = Math.max(sourceNodeCount, targetNodeCount)
    // G2 默认间距在单层节点达到 51 个时会耗尽布局高度
    const nodePadding = maxNodeCount >= 51 ? 0.5 / (maxNodeCount - 1) : 0.02
    // 原逻辑固定先判断指标排序，指标启用升降序后会直接返回，导致更高优先级的维度排序无法执行
    // 将字段规则按 sortPriority 重排，确保排序冲突时优先采用用户设置的高优先级字段
    const sortPriority = new Map(
      (chart.sortPriority ?? []).map((field, index) => [field.id, index])
    )
    // 指标可比较两侧节点，起点和终点维度仅比较各自对应的左右节点层级
    const sortRules = [
      {
        field: chart.yAxis?.[0],
        defaultPriority: 0,
        applicable: () => true,
        compare: (a, b) => {
          if (chart.yAxis[0].sort === 'asc') {
            return a.value - b.value
          } else if (chart.yAxis[0].sort === 'desc') {
            return b.value - a.value
          }
          return 0
        }
      },
      {
        field: chart.xAxis?.[0],
        defaultPriority: 1,
        applicable: (a, b) => a.sourceLinks.length > 0 && b.sourceLinks.length > 0,
        compare: (a, b) => {
          if (chart.xAxis[0].sort === 'custom_sort' && chart.xAxis[0].customSort) {
            return (
              chart.xAxis[0].customSort.indexOf(a.key) - chart.xAxis[0].customSort.indexOf(b.key)
            )
          } else if (chart.xAxis[0].sort === 'asc') {
            return a.key.localeCompare(b.key)
          } else if (chart.xAxis[0].sort === 'desc') {
            return b.key.localeCompare(a.key)
          }
          return 0
        }
      },
      {
        field: chart.xAxisExt?.[0],
        defaultPriority: 2,
        applicable: (a, b) => a.targetLinks.length > 0 && b.targetLinks.length > 0,
        compare: (a, b) => {
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
          return 0
        }
      }
    ]
      .filter(rule => rule.field)
      .sort((a, b) => {
        const aPriority = sortPriority.get(a.field.id) ?? sortPriority.size + a.defaultPriority
        const bPriority = sortPriority.get(b.field.id) ?? sortPriority.size + b.defaultPriority
        return aPriority - bPriority
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
        nodeStrokeOpacity: 0,
        fillOpacity: 0.3
      },
      state: {
        active: {
          fillOpacity: 0.9
        }
      },
      layout: {
        nodePadding,
        nodeSort: (a, b) => {
          // 高优先级字段结果相同时继续比较下一级，保证多字段排序的级联语义
          for (const rule of sortRules) {
            if (rule.applicable(a, b)) {
              const result = rule.compare(a, b)
              if (result !== 0) {
                return result
              }
            }
          }
          return b.value - a.value
        }
      }
    }

    const options: G2Spec = this.setupOptions(chart, initOptions)
    const newChart = new G2Chart({ container, ...getG2Renderer() })
    handleChartDashboardHidden(chart, options)
    newChart.options(options)

    const templateData = chart.data.data[0]
    newChart.on('polygon:click', e => {
      if (!(e.data?.data?.source && e.data?.data?.target)) {
        return
      }
      const source = e.data.data.source.key
      const target = e.data.data.target.key
      const actionData = {
        data: {
          data: {
            // 记录实际点击连接的起止节点，避免使用第一条模板数据导致选中错位
            name: source,
            category: target,
            dimensionList: [{ id: templateData.dimensionList[0].id, value: source }],
            quotaList: [{ id: templateData.quotaList[0].id, value: e.data.data.value }]
          }
        },
        x: e.offset.x,
        y: e.offset.y
      }
      if (templateData.dimensionList[1]) {
        actionData.data.data.dimensionList.push({
          id: templateData.dimensionList[1].id,
          value: target
        })
      }
      action(actionData)
    })

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
    // 先调整越界位置再执行碰撞隐藏，避免重新显示已隐藏的标签
    const labelStyle = {
      style: {
        labelText: d => d.key || '',
        labelFill: label.color,
        labelFontSize: label.fontSize,
        labelFillOpacity: 1,
        labelTransform: label.fullDisplay ? [] : [{ type: 'exceedAdjust' }, { type: 'overlapHide' }]
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
    // 按起点汇总流出量，用于计算每条连线的占比
    const outTotal: Record<string, number> = {}
    chart.data.data.forEach(d => {
      outTotal[d.source] = (outTotal[d.source] || 0) + d.value
    })

    const tooltipOptions: G2Spec = {
      tooltip: {
        linkItems: [d => d],
        nodeItems: [d => d]
      },
      interaction: {
        tooltip: {
          mount: createTooltipWrapper(chart),
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
              if (tooltip.tooltipFormatter.showTotalPercent) {
                const sourceTotal = outTotal[head.source.key]
                if (sourceTotal) {
                  const { decimalCount = 2 } = tooltip.tooltipFormatter
                  const ratio = ((head.value / sourceTotal) * 100).toFixed(decimalCount)
                  value += ` (${ratio}%)`
                }
              }
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
