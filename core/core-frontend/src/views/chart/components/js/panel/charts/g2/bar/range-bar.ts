import {
  BAR_EDITOR_PROPERTY_INNER,
  BAR_RANGE_EDITOR_PROPERTY
} from '@/views/chart/components/js/panel/charts/g2/bar/common'
import { flow, parseJson } from '@/views/chart/components/js/util'
import {
  configTooltip,
  createTooltipWrapper,
  tooltipCss,
  ViewSpec
} from '@/views/chart/components/js/panel/charts/g2/bar/barUtil'
import { useI18n } from '@/hooks/web/useI18n'
import { Chart as G2Column, Chart } from '@antv/g2'
import { HorizontalBar } from '@/views/chart/components/js/panel/charts/g2/bar/horizontal-bar'
import { G2DrawOptions } from '@/views/chart/components/js/panel/types/impl/g2'
import { cloneDeep, isEmpty } from 'lodash-es'
import {
  TOOLTIP_ITEM_TPL,
  TOOLTIP_TITLE_TPL
} from '@/views/chart/components/js/panel/common/common_antv'
import { valueFormatter } from '@/views/chart/components/js/formatter'

const { t } = useI18n()

/**
 * 堆叠条形图
 */
export class RangeBar extends HorizontalBar {
  axisConfig = {
    xAxis: {
      name: `${t('chart.drag_block_type_axis')} / ${t('chart.dimension')}`,
      type: 'd'
    },
    yAxis: {
      name: `${t('chart.drag_block_value_start')} / ${t('chart.time_dimension_or_quota')}`,
      limit: 1,
      type: 'q'
    },
    yAxisExt: {
      name: `${t('chart.drag_block_value_end')} / ${t('chart.time_dimension_or_quota')}`,
      limit: 1,
      type: 'q'
    }
  }
  properties = BAR_RANGE_EDITOR_PROPERTY.filter(p => p !== 'threshold')
  propertyInner = {
    ...BAR_EDITOR_PROPERTY_INNER,
    'label-selector': ['hPosition', 'color', 'fontSize', 'labelFormatter', 'showGap'],
    'tooltip-selector': [
      'fontSize',
      'color',
      'backgroundColor',
      'tooltipFormatter',
      'showGap',
      'show'
    ],
    'x-axis-selector': [...BAR_EDITOR_PROPERTY_INNER['x-axis-selector'], 'axisLabelFormatter'],
    'y-axis-selector': [
      'name',
      'color',
      'fontSize',
      'axisLine',
      'splitLine',
      'axisForm',
      'axisLabel',
      'position',
      'showLengthLimit'
    ]
  }

  async drawChart(drawOptions: G2DrawOptions<G2Column>): Promise<G2Column> {
    const { chart, container, action } = drawOptions
    chart.container = container
    if (!chart?.data?.data?.length) {
      return
    }
    let data = cloneDeep(drawOptions.chart.data?.data)
    const isDate = !!chart.data.isDate
    const dateSplit = chart.yAxis?.[0]?.datePattern === 'date_split' ? '/' : '-'
    const dateFormat =
      {
        y: 'yyyy',
        y_M: `yyyy${dateSplit}MM`,
        y_M_d: `yyyy${dateSplit}MM${dateSplit}dd`,
        y_M_d_H: `yyyy${dateSplit}MM${dateSplit}dd hh`,
        y_M_d_H_m: `yyyy${dateSplit}MM${dateSplit}dd hh:mm`,
        y_M_d_H_m_s: `yyyy${dateSplit}MM${dateSplit}dd hh:mm:ss`
      }[chart.yAxis?.[0]?.dateStyle] || 'yyyy-MM-dd hh:mm:ss'
    if (isDate) {
      data = cloneDeep(data).map(item => ({
        ...item,
        values: item.values.map(dateStr => new Date(dateStr)),
        dateFormat: dateFormat
      }))
    }
    const initOptions: ViewSpec = {
      type: 'view',
      children: [
        {
          ...this.intervalOptions,
          encode: { ...this.intervalOptions.encode, y: 'values' },
          transform: [].concat(this.intervalOptions.transform),
          scale: {
            ...this.intervalOptions.scale,
            y: {
              ...this.intervalOptions.scale.y,
              mask: isDate ? dateFormat : undefined,
              labelFormatter: val => (isDate ? new Date(val).format(dateFormat) : val)
            }
          },
          data
        }
      ]
    }
    const options: ViewSpec = this.setupOptions(chart, initOptions)
    const newChart = new G2Column({ container, autoFit: true })
    newChart.options(options)
    newChart.on('interval:click', action)
    configTooltip(newChart, chart)
    return newChart
  }

  protected configYAxis(chart: Chart, options: ViewSpec): ViewSpec {
    const tmpOptions = super.configYAxis(chart, options)
    const customStyle = parseJson(chart.customStyle)
    const axis = JSON.parse(JSON.stringify(customStyle['xAxis']))
    const isDate = !!chart.data.isDate
    const dateFormat = options.children[0].scale.y.mask
    tmpOptions.children[0].axis.y = {
      ...tmpOptions.children[0].axis.y,
      labelFormatter: val => {
        if (isDate) {
          return new Date(val).format(dateFormat)
        }
        return valueFormatter(val, axis.axisLabelFormatter)
      }
    }
    return tmpOptions
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
      dx: labelAttr.position === 'middle' ? 0 : 15
    }
    const transform = labelAttr.fullDisplay
      ? {}
      : { transform: [{ type: 'exceedAdjust' }, { type: 'overlapHide' }] }
    const isDate = !!chart.data.isDate
    const dateFormat = children[0].scale.y.mask
    const label = {
      text: 'value',
      fillOpacity: 1,
      fill: labelAttr.color,
      fontSize: labelAttr.fontSize,
      ...position,
      formatter: (_value, data) => {
        if (labelAttr.showGap) {
          return data.gap
        }
        let value
        if (isDate) {
          value = data.values
            .map((dateStr: string) => new Date(dateStr).format(dateFormat))
            .join(' ~ ')
        } else {
          value = data.values
            .map((dateStr: string) => valueFormatter(dateStr, labelAttr.labelFormatter))
            .join(' ~ ')
        }
        return value
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
      children[0].tooltip = false
      return options
    }
    const isDate = !!chart.data.isDate
    const dateFormat = children[0].scale.y.mask
    const tooltipOptions: ViewSpec = {
      tooltip: {
        items: [
          (datum, index, data, _column) => ({
            value: datum.values,
            original_data: data[index]
          })
        ]
      },
      interaction: {
        tooltip: {
          mount: createTooltipWrapper(chart),
          css: tooltipCss(tooltip),
          enterable: true,
          shared: true,
          bounding: { x: 0, y: 0 },
          position: 'top-right',
          render: (_, { title, items: originalItems }) => {
            const titleHtml = TOOLTIP_TITLE_TPL.replace('{title}', title)
            const itemsHtml = originalItems
              .map(item => {
                const value =
                  item.value
                    .map((dateStr: string) =>
                      isDate
                        ? new Date(dateStr).format(dateFormat)
                        : valueFormatter(dateStr, tooltip.tooltipFormatter)
                    )
                    .join(' ~ ') + (tooltip.showGap ? ` (${item.original_data.gap})` : '')
                const name = isEmpty(item.original_data.category)
                  ? item.original_data.field
                  : item.original_data.category
                return TOOLTIP_ITEM_TPL.replace('{marker}', item.color)
                  .replace('{label}', name)
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
      children: [{ ...options.children[0], ...tooltipOptions }, ...options.children.slice(1)]
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
      this.configYAxis
    )(chart, options, {}, this)
  }

  constructor(name = 'bar-range') {
    super(name)
    Object.assign(this.intervalOptions, {
      transform: [],
      coordinate: { transform: [{ type: 'transpose' }] }
    })
    this.axis = [...this.axis, 'yAxisExt']
  }
}
