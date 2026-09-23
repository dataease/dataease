import {
  BAR_EDITOR_PROPERTY,
  BAR_EDITOR_PROPERTY_INNER
} from '@/views/chart/components/js/panel/charts/g2/bar/common'
import {
  flow,
  hexColorToRGBA,
  parseJson,
  setUpStackSeriesColor
} from '@/views/chart/components/js/util'
import { Bar } from '@/views/chart/components/js/panel/charts/g2/bar/bar'
import { formatterItem, valueFormatter } from '@/views/chart/components/js/formatter'
import { groupBy } from 'lodash-es'
import {
  configStackSeriesOrder,
  createTooltipWrapper,
  getStackSeriesIndexMap,
  getStackSeriesOrder,
  getStackTooltipGroupName,
  handleBarBreakLineNullData,
  handleEmptyDataStrategy,
  renderGroupedTooltipItems,
  sortStackTooltipItems,
  tooltipCss,
  tooltipMaxHeight,
  ViewSpec
} from '@/views/chart/components/js/panel/charts/g2/bar/barUtil'
import {
  setGradientColor,
  toLinearGradient,
  TOOLTIP_ITEM_TPL,
  TOOLTIP_TITLE_TPL
} from '@/views/chart/components/js/panel/common/common_antv'
import { defaultsDeep, isEmpty } from 'lodash-es'

/**
 * 堆叠柱状图
 */
export class StackBar extends Bar {
  properties = BAR_EDITOR_PROPERTY.filter(ele => ele !== 'threshold')
  propertyInner = {
    ...this['propertyInner'],
    'label-selector': [
      ...BAR_EDITOR_PROPERTY_INNER['label-selector'],
      'vPosition',
      'showTotal',
      'totalColor',
      'totalFontSize',
      'totalFormatter',
      'showStackQuota'
    ],
    'tooltip-selector': [
      'fontSize',
      'color',
      'backgroundColor',
      'tooltipFormatter',
      'show',
      'carousel'
    ]
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

    const labels = []
    if (labelAttr.showStackQuota ?? true) {
      labels.push({
        text: 'value',
        fillOpacity: 1,
        pointerEvents: 'none',
        fill: labelAttr.color,
        fontSize: labelAttr.fontSize,
        ...position,
        formatter: value => valueFormatter(value, labelAttr.labelFormatter),
        ...transform
      })
    }

    if (labelAttr.showTotal) {
      const formatterCfg = labelAttr.labelFormatter ?? formatterItem
      const groupedData = groupBy(options.data, 'field')
      const totalData = Object.entries(groupedData).map(([key, values]) => {
        const total = values.reduce((a, b) => a + b.value, 0)
        return {
          field: key,
          value: total,
          totalLabel: valueFormatter(total, formatterCfg)
        }
      })
      if (totalData.length) {
        children.push({
          type: 'point',
          data: totalData,
          encode: {
            x: 'field',
            y: 'value'
          },
          style: {
            opacity: 0
          },
          labels: [
            {
              text: 'totalLabel',
              fillOpacity: 1,
              pointerEvents: 'none',
              fill: labelAttr.color,
              fontSize: labelAttr.fontSize,
              position: 'top',
              dx: 0,
              dy: -10,
              ...transform,
              textAlign: 'center'
            }
          ],
          tooltip: false
        })
      }
    }

    return {
      ...options,
      children: [
        {
          ...children[0],
          labels: labels
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
              const value =
                item.value === null || item.value === undefined
                  ? ''
                  : valueFormatter(item.value, tooltip.tooltipFormatter)
              const name = this.getTooltipItemName(chart, item)
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

  protected getTooltipItemName(_chart: Chart, item: any): string {
    return isEmpty(item.category) ? item.field : item.category
  }

  protected configData(chart: Chart, options: ViewSpec): ViewSpec {
    const { xAxis, extStack, yAxis } = chart
    const mainSort = xAxis.some(axis => axis.sort !== 'none')
    const subSort = extStack.some(axis => axis.sort !== 'none')
    if (mainSort || subSort) {
      return options
    }
    const quotaSort = yAxis?.[0].sort !== 'none'
    if (!quotaSort || !extStack.length || !yAxis.length) {
      return options
    }
    const data = options.children[0]?.data || options.data
    const mainAxisValueMap = data.reduce((p, n) => {
      p[n.field] = p[n.field] ? p[n.field] + n.value : n.value || 0
      return p
    }, {})
    const sort = yAxis[0].sort
    data.sort((p, n) => {
      if (sort === 'asc') {
        return mainAxisValueMap[p.field] - mainAxisValueMap[n.field]
      } else {
        return mainAxisValueMap[n.field] - mainAxisValueMap[p.field]
      }
    })
    return options
  }

  protected configEmptyDataStrategy(chart: Chart, options: ViewSpec): ViewSpec {
    handleEmptyDataStrategy(chart, options)
    handleBarBreakLineNullData(chart, options)
    return options
  }

  public setupSeriesColor(chart: ChartObj, data?: any[]): ChartBasicStyle['seriesColor'] {
    return setUpStackSeriesColor(chart, data)
  }

  protected configColor(chart: Chart, options: ViewSpec): ViewSpec {
    const { basicStyle } = parseJson(chart.customAttr)
    const { seriesColor } = basicStyle
    if (!seriesColor?.length) {
      return options
    }
    const { xAxis, xAxisExt, yAxis, extStack } = chart
    if (!xAxis?.length || !yAxis?.length) {
      return options
    }
    const relations = []
    if (xAxisExt?.length || extStack?.length) {
      seriesColor.forEach(item => {
        let color = hexColorToRGBA(item.color, basicStyle.alpha)
        if (basicStyle.gradient) {
          color = setGradientColor(color, true, 270)
        }
        relations.push([item.id, color])
      })
    } else {
      const colorMap = seriesColor.reduce((pre, next) => {
        pre[next.id] = next.color
        return pre
      }, {})
      yAxis.forEach(item => {
        if (colorMap[item.id]) {
          let color = hexColorToRGBA(colorMap[item.id], basicStyle.alpha)
          if (basicStyle.gradient) {
            color = setGradientColor(color, true, 270)
          }
          relations.push([item.chartShowName ?? item.name, color])
        }
      })
    }
    if (relations.length) {
      const scaleOptions = {
        scale: {
          color: {
            relations
          }
        }
      }
      defaultsDeep(options, scaleOptions)
    }
    return options
  }

  protected setupOptions(chart: Chart, options: ViewSpec): ViewSpec {
    return flow(
      this.configTheme,
      this.configEmptyDataStrategy,
      this.configData,
      // 在颜色和堆叠阶段统一系列顺序，避免图例、颜色、层级错位
      configStackSeriesOrder,
      this.configColor,
      this.configBasicStyle,
      this.configLabel,
      this.configTooltip,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configAnalyse,
      this.configSlider
    )(chart, options, {}, this)
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    const chartTmp = super.setupDefaultOptions(chart)
    chartTmp.customAttr.label.showStackQuota = true
    return chartTmp
  }

  constructor(name = 'bar-stack') {
    super(name)
    this.intervalOptions.transform = [{ type: 'stackY', reverse: true }]
    this.axis = [...this.axis, 'extStack']
  }
}
