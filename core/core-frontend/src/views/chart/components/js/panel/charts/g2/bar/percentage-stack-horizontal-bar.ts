import { BAR_AXIS_TYPE } from '@/views/chart/components/js/panel/charts/g2/bar/common'
import { flow, parseJson } from '@/views/chart/components/js/util'
import {
  createTooltipWrapper,
  tooltipCss,
  ViewSpec
} from '@/views/chart/components/js/panel/charts/g2/bar/barUtil'
import {
  TOOLTIP_ITEM_TPL,
  TOOLTIP_TITLE_TPL
} from '@/views/chart/components/js/panel/common/common_antv'
import { isEmpty } from 'lodash-es'
import { HorizontalStackBar } from '@/views/chart/components/js/panel/charts/g2/bar/stack-horizontal-bar'

/**
 * 百分比堆叠柱状图
 */
export class PercentageStackBar extends HorizontalStackBar {
  propertyInner = {
    ...this['propertyInner'],
    'label-selector': ['color', 'fontSize', 'vPosition', 'reserveDecimalCount'],
    'tooltip-selector': ['color', 'fontSize', 'backgroundColor', 'show', 'carousel']
  }

  protected configLabel(chart: Chart, options: ViewSpec): ViewSpec {
    const customAttr = parseJson(chart.customAttr)
    const { label: labelAttr } = customAttr
    if (!labelAttr || !labelAttr.show) return options

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
      formatter: (value, _data, _, o) => {
        // 计算与当前数据相同 field 的 value 总和
        const sum =
          o?.reduce(
            (acc, item) => (item.field === _data.field ? acc + (item.value || 0) : acc),
            0
          ) || 1
        // 返回百分比格式化结果
        return `${((value / sum) * 100).toFixed(labelAttr.reserveDecimalCount)}%`
      },
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
    const { tooltip } = parseJson(chart.customAttr)
    if (!tooltip.show) {
      return {
        ...options,
        tooltip: false
      }
    }
    const tooltipMap = function (a) {
      return a
    }
    tooltipMap.title = undefined

    const tooltipOptions: ViewSpec = {
      tooltip: tooltipMap,
      interaction: {
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
            const sum = tooltipItems?.reduce(
              (acc, { value = 0 }: { value: number }) => acc + value,
              0
            )
            const result = []
            tooltipItems.forEach(item => {
              const itemValue = item.value ? (item.value as number) : 0
              const value = `${((itemValue / sum) * 100).toFixed(
                tooltip.tooltipFormatter.decimalCount
              )}%`
              const name = `${isEmpty(item.category) ? item.field : item.category}${
                item.group ? '-' + item.group : ''
              }`
              result.push({ ...item, name, value })
            })
            const itemsHtml = result
              .map(item => {
                const marker = item.color
                const label = item.name
                const value = item.value
                return TOOLTIP_ITEM_TPL.replace('{marker}', marker)
                  .replace('{label}', label)
                  .replace('{value}', value)
              })
              .join('')
            const listHtml = `<ul class="g2-tooltip-list" style="margin: 0px; list-style-type: none; padding: 0px;">${itemsHtml}</ul>`
            return `${titleHtml}${listHtml}`
          }
        }
      }
    }
    return {
      ...options,
      children: [
        {
          ...options.children[0],
          ...tooltipOptions
        },
        ...options.children.slice(1)
      ]
    }
  }

  protected setupOptions(chart: Chart, options: ViewSpec): ViewSpec {
    return flow(
      this.configTheme,
      this.configBasicStyle,
      this.configLabel,
      this.configTooltip,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configAnalyse,
      this.configBarConditions
    )(chart, options, {}, this)
  }

  constructor(name = 'percentage-bar-stack-horizontal') {
    super(name)
    this.intervalOptions.encode = {
      ...this.intervalOptions.encode,
      series: d => d.group
    }
    this.intervalOptions.transform = [{ type: 'stackY' }, { type: 'normalizeY' }]
    this.axis = [...BAR_AXIS_TYPE, 'extStack']
  }
}
