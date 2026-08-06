import {
  BAR_EDITOR_PROPERTY,
  BAR_EDITOR_PROPERTY_INNER
} from '@/views/chart/components/js/panel/charts/g2/bar/common'
import { flow, parseJson } from '@/views/chart/components/js/util'
import {
  configStackSeriesOrder,
  createTooltipWrapper,
  getHorizontalBarAxisSafeLabelStyle,
  getStackSeriesIndexMap,
  getStackSeriesOrder,
  getStackTooltipGroupName,
  renderGroupedTooltipItems,
  sortStackTooltipItems,
  tooltipCss,
  ViewSpec
} from '@/views/chart/components/js/panel/charts/g2/bar/barUtil'
import { useI18n } from '@/hooks/web/useI18n'
import {
  toLinearGradient,
  TOOLTIP_ITEM_TPL,
  TOOLTIP_TITLE_TPL
} from '@/views/chart/components/js/panel/common/common_antv'
import { formatterItem, valueFormatter } from '@/views/chart/components/js/formatter'
import { HorizontalBar } from '@/views/chart/components/js/panel/charts/g2/bar/horizontal-bar'
import { groupBy, isEmpty } from 'lodash-es'

const { t } = useI18n()

/**
 * 堆叠条形图
 */
export class HorizontalStackBar extends HorizontalBar {
  properties = BAR_EDITOR_PROPERTY.filter(ele => ele !== 'threshold')
  axisConfig = {
    ...this['axisConfig'],
    extStack: {
      name: `${t('chart.stack_item')} / ${t('chart.dimension')}`,
      type: 'd',
      limit: 1,
      allowEmpty: true
    }
  }
  propertyInner = {
    ...this['propertyInner'],
    'label-selector': [
      ...BAR_EDITOR_PROPERTY_INNER['label-selector'],
      'hPosition',
      'showTotal',
      'totalColor',
      'totalFontSize',
      'totalFormatter',
      'showStackQuota'
    ],
    'tooltip-selector': ['fontSize', 'color', 'backgroundColor', 'tooltipFormatter', 'show']
  }

  protected configLabel(chart: Chart, options: ViewSpec): ViewSpec {
    const { label: labelAttr } = parseJson(chart.customAttr) || {}
    if (!labelAttr?.show) return options

    const { children } = options
    const position = {
      position: labelAttr.position === 'middle' ? 'inside' : labelAttr.position,
      textAlign: 'start',
      dy: labelAttr.position === 'top' ? -10 : 0,
      dx: 0,
      ...getHorizontalBarAxisSafeLabelStyle(chart, labelAttr.position)
    }
    const transform = [
      { type: 'exceedAdjust' },
      ...(labelAttr.fullDisplay ? [] : [{ type: 'overlapHide' }])
    ]

    const labels = []
    if (labelAttr.showStackQuota ?? true) {
      labels.push({
        text: 'value',
        fillOpacity: 1,
        pointerEvents: 'none',
        fill: labelAttr.color,
        fontSize: labelAttr.fontSize,
        ...position,
        formatter: value => {
          if (value === null || value === undefined) {
            return ''
          }
          return valueFormatter(value, labelAttr.labelFormatter)
        },
        transform
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
          coordinate: { transform: [{ type: 'transpose' }] },
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
              position: 'right',
              dx: 4,
              dy: 0,
              transform,
              textAlign: 'start',
              ...getHorizontalBarAxisSafeLabelStyle(chart, 'right')
            }
          ],
          tooltip: false
        })
      }
    }

    return {
      ...options,
      children: [{ ...children[0], labels }, ...children.slice(1)]
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

    const tooltipOptions: ViewSpec = {
      tooltip: a => a,
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
            const tooltipItems = originalItems
            const result = []
            tooltipItems.forEach(item => {
              const value =
                item.value === null || item.value === undefined
                  ? ''
                  : valueFormatter(item.value, tooltip.tooltipFormatter)
              const name = isEmpty(item.category) ? item.field : item.category
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
    const childrenList = [{ ...children[0], ...tooltipOptions }, ...children.slice(1)]
    return {
      ...options,
      children: childrenList
    }
  }

  protected configData(chart: Chart, options: ViewSpec): ViewSpec {
    const { xAxis, extStack, yAxis } = chart
    const mainSort = xAxis.some(axis => axis.sort !== 'none')
    const subSort = extStack.some(axis => axis.sort !== 'none')
    if (mainSort || subSort) {
      return options
    }
    const quotaSort = yAxis?.[0]?.sort !== 'none'
    if (!quotaSort || !extStack.length || !yAxis.length) {
      return options
    }
    const { data } = options
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

  protected setupOptions(chart: Chart, options: ViewSpec): ViewSpec {
    return flow(
      this.configTheme,
      this.configEmptyDataStrategy,
      this.configBasicStyle,
      this.configData,
      // 在颜色和堆叠阶段统一系列顺序，避免图例、颜色、层级错位
      configStackSeriesOrder,
      this.configColor,
      this.configLabel,
      this.configTooltip,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configAnalyse,
      this.configSlider
    )(chart, options, {}, this)
  }

  constructor(name = 'bar-stack-horizontal') {
    super(name)
    Object.assign(this.intervalOptions, {
      transform: [{ type: 'stackY' }],
      coordinate: { transform: [{ type: 'transpose' }] }
    })
    this.axis = [...this.axis, 'extStack']
  }
}
