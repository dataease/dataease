import {
  BAR_AXIS_TYPE,
  BAR_EDITOR_PROPERTY_INNER
} from '@/views/chart/components/js/panel/charts/g2/bar/common'
import { flow, parseJson } from '@/views/chart/components/js/util'
import { StackBar } from '@/views/chart/components/js/panel/charts/g2/bar/stack-bar'
import {
  configStackSeriesOrder,
  createTooltipWrapper,
  getStackSeriesIndexMap,
  getStackSeriesOrder,
  getStackTooltipGroupName,
  renderGroupedTooltipItems,
  sortStackTooltipItems,
  tooltipCss,
  tooltipMaxHeight,
  Transform,
  ViewSpec
} from '@/views/chart/components/js/panel/charts/g2/bar/barUtil'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import {
  toLinearGradient,
  TOOLTIP_ITEM_TPL,
  TOOLTIP_TITLE_TPL
} from '@/views/chart/components/js/panel/common/common_antv'
import { isEmpty } from 'lodash-es'

/**
 * 分组堆叠柱状图
 */
export class GroupStackBar extends StackBar {
  propertyInner = {
    ...this['propertyInner'],
    'label-selector': [...BAR_EDITOR_PROPERTY_INNER['label-selector'], 'vPosition']
  }

  protected isGroupMode(chart: Chart): boolean {
    return chart.xAxisExt?.length > 0 && !chart.extStack?.length
  }

  protected configGroupMode(chart: Chart, options: ViewSpec): ViewSpec {
    if (!this.isGroupMode(chart)) {
      return options
    }
    const { children } = options
    if (!children?.[0]) {
      return options
    }
    const encode = { ...children[0].encode }
    delete encode.series
    // 无堆叠项时按分组柱处理，避免子维度被 stackY 累加
    return {
      ...options,
      children: [
        {
          ...children[0],
          encode,
          transform: [{ type: 'dodgeX' } as Transform]
        },
        ...children.slice(1)
      ]
    }
  }

  protected configStackOrder(chart: Chart, options: ViewSpec): ViewSpec {
    if (this.isGroupMode(chart)) {
      return options
    }
    return configStackSeriesOrder(chart, options)
  }

  protected configLabel(chart: Chart, options: ViewSpec): ViewSpec {
    const customAttr = parseJson(chart.customAttr)
    const { label: labelAttr } = customAttr
    if (!labelAttr.show) return options

    const { children } = options
    const position = {
      position: labelAttr.position === 'middle' ? 'inside' : labelAttr.position,
      textAlign: 'center',
      dy: labelAttr.position === 'top' ? -10 : 0,
      dx: 0
    }
    const transform = labelAttr.fullDisplay
      ? {}
      : { transform: [{ type: 'exceedAdjust' }, { type: 'overlapHide' }] }

    const label = {
      text: 'value',
      fillOpacity: 1,
      fill: labelAttr.color,
      fontSize: labelAttr.fontSize,
      ...position,
      // 标签格式化只依赖 value，避免保留未使用的数据参数
      formatter: value => valueFormatter(value, labelAttr.labelFormatter),
      ...transform
    }
    return {
      ...options,
      children: [
        {
          ...children[0],
          labels: [label]
        },
        ...children.slice(1)
      ]
    }
  }
  protected configTooltip(chart: Chart, options: ViewSpec): ViewSpec {
    const { children } = options
    const { tooltip } = parseJson(chart.customAttr)
    if (!tooltip.show) {
      return options
    }
    // 基于数据和排序配置生成系列顺序，供 tooltip 与堆叠层级共用
    const seriesOrder = getStackSeriesOrder(chart, children[0]?.data || options.data)
    const seriesIndexMap = getStackSeriesIndexMap(seriesOrder)
    const tooltipMap = function (a) {
      return a
    }
    tooltipMap.title = undefined

    const tooltipOptions: ViewSpec = {
      tooltip: tooltipMap,
      interaction: {
        ...children[0].interaction,
        tooltip: {
          mount: createTooltipWrapper(chart),
          css: tooltipCss(tooltip),
          enterable: true,
          shared: true,
          position: 'top-right',
          render: (_, { title, items: originalItems }) => {
            const titleHtml = TOOLTIP_TITLE_TPL.replace('{title}', title)
            const tooltipItems = originalItems
            const result = []
            tooltipItems.forEach(item => {
              const value = valueFormatter(item.value, tooltip.tooltipFormatter)
              const name = `${isEmpty(item.category) ? item.field : item.category}${
                item.group ? '-' + item.group : ''
              }`
              result.push({ ...item, name, value })
            })
            // tooltip 内系列顺序与堆叠层级保持一致
            sortStackTooltipItems(result, seriesOrder, seriesIndexMap)
            // tooltip 项按维度槽位分组，帮助区分多维度明细
            const itemsHtml = renderGroupedTooltipItems(
              result,
              item => getStackTooltipGroupName(chart, item),
              item => {
                const marker = toLinearGradient(item.color)
                const label = item.name
                const value = item.value
                return TOOLTIP_ITEM_TPL.replace('{marker}', marker)
                  .replace('{label}', label)
                  .replace('{value}', value)
              }
            )
            const listHtml = `<ul class="g2-tooltip-list" style="${tooltipMaxHeight(
              chart
            )}margin: 0px; list-style-type: none; padding: 0px;">${itemsHtml}</ul>`
            return `${titleHtml}${listHtml}`
          }
        }
      }
    }
    return {
      ...options,
      children: [{ ...children[0], ...tooltipOptions }, ...children.slice(1)]
    }
  }

  protected setupOptions(chart: Chart, options: ViewSpec): ViewSpec {
    return flow(
      this.configTheme,
      this.configGroupMode,
      this.configEmptyDataStrategy,
      this.configColor,
      this.configBasicStyle,
      // 在颜色和堆叠阶段统一系列顺序，避免图例、颜色、层级错位
      this.configStackOrder,
      this.configLabel,
      this.configTooltip,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configAnalyse,
      this.configBarConditions,
      this.configSlider
    )(chart, options, {}, this)
  }

  constructor(name = 'bar-group-stack') {
    super(name)
    this.intervalOptions.encode = {
      ...this.intervalOptions.encode,
      series: d => d.group
    }
    this.intervalOptions.transform = [
      {
        type: 'stackY',
        groupBy: ['x', 'series'],
        reverse: true
      } as Transform
    ]
    this.axis = [...BAR_AXIS_TYPE, 'xAxisExt', 'extStack']
  }
}
