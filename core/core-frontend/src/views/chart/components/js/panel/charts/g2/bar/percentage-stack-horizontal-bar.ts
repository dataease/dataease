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
import { formatterItem, valueFormatter } from '@/views/chart/components/js/formatter'
import {
  configPercentageStackEmptyAnchorStyle,
  configPercentageStackEmptyAnchorTooltipGuard,
  configPercentageStackEmptyDataStrategy,
  filterPercentageStackEmptyAnchorTooltipItem,
  filterPercentageStackTooltipItems,
  formatPercentageStackRatio,
  getPercentageStackFieldTotal,
  getPercentageStackOptionsData,
  getPercentageStackZeroLabelAlignMap,
  isPercentageStackEmptyAnchor,
  isPercentageStackZeroAnchor
} from '@/views/chart/components/js/panel/charts/g2/bar/percentage-stack-helper'

/**
 * 百分比条形图
 */
export class PercentageStackBar extends HorizontalStackBar {
  propertyInner = {
    ...this['propertyInner'],
    'label-selector': ['color', 'fontSize', 'hPosition', 'showQuota', 'showProportion'],
    'tooltip-selector': [
      'color',
      'fontSize',
      'backgroundColor',
      'show',
      'showQuota',
      'tooltipFormatter'
    ]
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
    const chartData = getPercentageStackOptionsData(options)
    const seriesOrder = getStackSeriesOrder(chart, chartData)
    const zeroLabelAlignMap = getPercentageStackZeroLabelAlignMap(chartData, seriesOrder)
    const getZeroLabelAlign = data =>
      isPercentageStackZeroAnchor(data) ? 'start' : zeroLabelAlignMap.get(data)
    const defaultPosition = {
      position: labelAttr.position === 'middle' ? 'inside' : labelAttr.position,
      // 百分比条形左右边界标签向绘图区内侧展开，避免覆盖同侧维度轴
      textAlign:
        labelAttr.position === 'left' ? 'start' : labelAttr.position === 'right' ? 'end' : 'center',
      dy: labelAttr.position === 'top' ? -10 : 0,
      dx: labelAttr.position === 'left' ? 4 : labelAttr.position === 'right' ? -4 : 0
    }
    const position = {
      // 零宽标签根据左右落点向绘图区内侧对齐，避免覆盖维度轴标签
      position: data => {
        const zeroAlign = getZeroLabelAlign(data)
        if (zeroAlign === 'start') return 'left'
        if (zeroAlign === 'end') return 'right'
        if (zeroAlign === 'center') return 'inside'
        return defaultPosition.position
      },
      textAlign: data => {
        const zeroAlign = getZeroLabelAlign(data)
        if (zeroAlign) return zeroAlign
        return defaultPosition.textAlign
      },
      dy: defaultPosition.dy,
      dx: data => {
        const zeroAlign = getZeroLabelAlign(data)
        if (zeroAlign === 'start') return 4
        if (zeroAlign === 'end') return -4
        return defaultPosition.dx
      }
    }
    const transform = labelAttr.fullDisplay
      ? { transform: [{ type: 'exceedAdjust' }] }
      : { transform: [{ type: 'exceedAdjust' }, { type: 'overlapHide' }] }

    const label = {
      text: 'value',
      fillOpacity: 1,
      pointerEvents: 'none',
      fill: labelAttr.color,
      fontSize: labelAttr.fontSize,
      ...position,
      formatter: (value, _data, _, o) => {
        const isZeroAnchor = isPercentageStackZeroAnchor(_data)
        if (isPercentageStackEmptyAnchor(_data) && !isZeroAnchor) return ''
        const numberValue = isZeroAnchor ? 0 : Number(value)
        if (!Number.isFinite(numberValue)) return ''
        // 计算与当前数据相同 field 的 value 总和
        const sum = getPercentageStackFieldTotal(o, _data.field)
        if (!isZeroAnchor && numberValue === 0 && sum === 0) return ''
        const showQuota = labelAttr.showQuota === true
        const showProportion = labelAttr.showProportion ?? true
        if (!showQuota && !showProportion) return ''

        const quotaText = showQuota
          ? valueFormatter(
              numberValue,
              labelAttr.quotaLabelFormatter ?? labelAttr.labelFormatter ?? formatterItem
            )
          : ''
        const proportionText = showProportion
          ? `${showQuota ? ' (' : ''}${formatPercentageStackRatio(
              numberValue,
              sum,
              labelAttr.reserveDecimalCount
            )}${showQuota ? ')' : ''}`
          : ''
        return `${quotaText}${proportionText}`
      },
      ...transform
    }
    children[0].labels = [label]
    return options
  }

  protected configTooltip(chart: Chart, options: ViewSpec): ViewSpec {
    const { label: labelAttr, tooltip } = parseJson(chart.customAttr)
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
          filter: filterPercentageStackEmptyAnchorTooltipItem,
          render: (_, { title, items: originalItems }) => {
            const titleHtml = TOOLTIP_TITLE_TPL.replace('{title}', title)
            // 从源数据补齐零宽系列，纯空锚点仍不展示
            const tooltipItems = filterPercentageStackTooltipItems(
              originalItems,
              options,
              seriesOrder
            )
            if (!tooltipItems.length) return ''
            const sum = tooltipItems?.reduce(
              (acc, { value = 0 }: { value: number }) => acc + value,
              0
            )
            const result = []
            tooltipItems.forEach(item => {
              const proportionText = formatPercentageStackRatio(
                item.value,
                sum,
                labelAttr.reserveDecimalCount
              )
              const value = tooltip.showQuota
                ? `${valueFormatter(item.value, tooltip.tooltipFormatter)} (${proportionText})`
                : proportionText
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
    chart.customAttr.label.showProportion = true
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
