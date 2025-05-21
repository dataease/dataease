import { BAR_EDITOR_PROPERTY } from '@/views/chart/components/js/panel/charts/g2/bar/common'
import { flow, parseJson } from '@/views/chart/components/js/util'
import {
  createTooltipWrapper,
  tooltipCss,
  ViewSpec
} from '@/views/chart/components/js/panel/charts/g2/bar/barUtil'
import { useI18n } from '@/hooks/web/useI18n'
import {
  TOOLTIP_ITEM_TPL,
  TOOLTIP_TITLE_TPL
} from '@/views/chart/components/js/panel/common/common_antv'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { Chart } from '@antv/g2'
import { HorizontalBar } from '@/views/chart/components/js/panel/charts/g2/bar/horizontal-bar'
import { isEmpty } from 'lodash-es'

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
    'label-selector': ['color', 'fontSize', 'hPosition', 'labelFormatter'],
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
      dx: 0
    }
    const transform = [
      { type: 'exceedAdjust' },
      ...(labelAttr.fullDisplay ? [] : [{ type: 'overlapHide' }])
    ]

    const labels = [
      {
        text: 'value',
        fillOpacity: 1,
        fill: labelAttr.color,
        fontSize: labelAttr.fontSize,
        ...position,
        formatter: (value, _data) => valueFormatter(value, labelAttr.labelFormatter),
        transform
      }
    ]

    return {
      ...options,
      children: [{ ...children[0], labels }, ...children.slice(1)]
    }
  }

  protected configTooltip(chart: Chart, options: ViewSpec): ViewSpec {
    const { children } = options
    const { tooltip } = parseJson(chart.customAttr)
    if (!tooltip.show) {
      return {
        ...options,
        children: [{ ...children[0], tooltip: false }, ...children.slice(1)]
      }
    }

    const tooltipOptions: ViewSpec = {
      tooltip: a => a,
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
            const result = []
            tooltipItems.forEach(item => {
              const value = valueFormatter(item.value, tooltip.tooltipFormatter)
              const name = isEmpty(item.category) ? item.field : item.category
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
      children: [{ ...children[0], ...tooltipOptions }, ...children.slice(1)]
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

  constructor(name = 'bar-stack-horizontal') {
    super(name)
    Object.assign(this.intervalOptions, {
      transform: [{ type: 'stackY' }],
      coordinate: { transform: [{ type: 'transpose' }] }
    })
    this.axis = [...this.axis, 'extStack']
  }
}
