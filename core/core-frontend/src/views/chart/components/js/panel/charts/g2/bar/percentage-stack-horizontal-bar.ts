import { BAR_AXIS_TYPE } from '@/views/chart/components/js/panel/charts/g2/bar/common'
import { flow, parseJson } from '@/views/chart/components/js/util'
import {
  configStackSeriesOrder,
  createTooltipWrapper,
  getStackSeriesIndexMap,
  getStackSeriesOrder,
  getStackTooltipGroupName,
  renderGroupedTooltipItems,
  sortStackTooltipItems,
  tooltipCss,
  ViewSpec
} from '@/views/chart/components/js/panel/charts/g2/bar/barUtil'
import {
  TOOLTIP_ITEM_TPL,
  toLinearGradient,
  TOOLTIP_TITLE_TPL
} from '@/views/chart/components/js/panel/common/common_antv'
import { isEmpty } from 'lodash-es'
import { HorizontalStackBar } from '@/views/chart/components/js/panel/charts/g2/bar/stack-horizontal-bar'
import type { G2DrawOptions } from '@/views/chart/components/js/panel/types/impl/g2'
import type { Chart as G2Column } from '@antv/g2'
import {
  configPercentageStackEmptyAnchorStyle,
  configPercentageStackEmptyAnchorTooltipGuard,
  configPercentageStackEmptyDataStrategy,
  filterPercentageStackTooltipItems,
  formatPercentageStackRatio,
  getPercentageStackFieldTotal,
  getPercentageStackOptionsData,
  getPercentageStackZeroTotalFields,
  shouldHidePercentageStackLabelValue
} from '@/views/chart/components/js/panel/charts/g2/bar/percentage-stack-helper'

/**
 * 百分比条形图
 */
export class PercentageStackBar extends HorizontalStackBar {
  propertyInner = {
    ...this['propertyInner'],
    'label-selector': ['color', 'fontSize', 'vPosition', 'reserveDecimalCount'],
    'tooltip-selector': ['color', 'fontSize', 'backgroundColor', 'show']
  }

  async drawChart(drawOptions: G2DrawOptions<G2Column>): Promise<G2Column> {
    const newChart = await super.drawChart(drawOptions)
    if (newChart) {
      configPercentageStackEmptyAnchorTooltipGuard(newChart)
    }
    return newChart
  }

  protected configEmptyDataStrategy(chart: Chart, options: ViewSpec): ViewSpec {
    return configPercentageStackEmptyDataStrategy(chart, options, () => {
      super.configEmptyDataStrategy(chart, options)
    })
  }

  protected configEmptyAnchorStyle(_chart: Chart, options: ViewSpec): ViewSpec {
    return configPercentageStackEmptyAnchorStyle(options)
  }

  protected configLabel(chart: Chart, options: ViewSpec): ViewSpec {
    const customAttr = parseJson(chart.customAttr)
    const { label: labelAttr } = customAttr
    if (!labelAttr.show) return options

    const { children } = options
    const zeroTotalFields = getPercentageStackZeroTotalFields(
      getPercentageStackOptionsData(options)
    )
    const isZeroTotalField = data => zeroTotalFields.has(data?.field)
    const position = {
      position: labelAttr.position === 'middle' ? 'inside' : labelAttr.position,
      textAlign: data => (isZeroTotalField(data) ? 'start' : 'center'),
      dy: labelAttr.position === 'top' ? -10 : 0,
      dx: data => (isZeroTotalField(data) ? 4 : 0)
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
      formatter: (value, _data, _, o) => {
        // 计算与当前数据相同 field 的 value 总和
        const sum = getPercentageStackFieldTotal(o, _data.field)
        if (shouldHidePercentageStackLabelValue(value, _data, sum)) return ''
        if (!sum) return `${(0).toFixed(labelAttr.reserveDecimalCount)}%`
        // 返回百分比格式化结果
        return `${((value / sum) * 100).toFixed(labelAttr.reserveDecimalCount)}%`
      },
      ...transform
    }
    children[0].labels = [label]
    return options
  }

  protected configTooltip(chart: Chart, options: ViewSpec): ViewSpec {
    const { tooltip } = parseJson(chart.customAttr)
    const { children } = options
    if (!tooltip.show) {
      return options
    }
    // 百分比堆叠从过滤后的图形数据取系列顺序，避免锚点数据干扰排序
    const seriesOrder = getStackSeriesOrder(chart, getPercentageStackOptionsData(options))
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
          bounding: {
            x: 0,
            y: 0
          },
          position: 'top-right',
          render: (_, { title, items: originalItems }) => {
            const titleHtml = TOOLTIP_TITLE_TPL.replace('{title}', title)
            // 锚点只负责鼠标命中，不应出现在 tooltip 明细里
            const tooltipItems = filterPercentageStackTooltipItems(originalItems)
            if (!tooltipItems.length) return ''
            const sum = tooltipItems?.reduce(
              (acc, { value = 0 }: { value: number }) => acc + value,
              0
            )
            const result = []
            tooltipItems.forEach(item => {
              const value = formatPercentageStackRatio(
                item.value,
                sum,
                tooltip.tooltipFormatter.decimalCount
              )
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
            const listHtml = `<ul class="g2-tooltip-list" style="margin: 0px; list-style-type: none; padding: 0px;">${itemsHtml}</ul>`
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
      this.configEmptyDataStrategy,
      this.configBasicStyle,
      this.configColor,
      // 在颜色和堆叠阶段统一系列顺序，避免图例、颜色、层级错位
      configStackSeriesOrder,
      this.configLabel,
      this.configTooltip,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configAnalyse,
      this.configEmptyAnchorStyle,
      this.configSlider
    )(chart, options, {}, this)
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    chart.customAttr.label.position = 'middle'
    return super.setupDefaultOptions(chart)
  }

  constructor(name = 'percentage-bar-stack-horizontal') {
    super(name)
    // 百分比堆叠没有分组槽位，移除 series band，避免默认 series padding 放大条间距
    delete this.intervalOptions.encode.series
    this.intervalOptions.transform = [{ type: 'stackY' }, { type: 'normalizeY' }]
    this.axis = [...BAR_AXIS_TYPE, 'extStack']
  }
}
