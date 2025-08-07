import {
  BAR_AXIS_TYPE,
  BAR_EDITOR_PROPERTY,
  BAR_EDITOR_PROPERTY_INNER
} from '@/views/chart/components/js/panel/charts/g2/bar/common'
import { flow, parseJson } from '@/views/chart/components/js/util'
import { StackBar } from '@/views/chart/components/js/panel/charts/g2/bar/stack-bar'
import { Chart as G2Column } from '@antv/g2'
import { ViewSpec } from '@/views/chart/components/js/panel/charts/g2/bar/barUtil'
import { useI18n } from '@/hooks/web/useI18n'
import { G2DrawOptions } from '@/views/chart/components/js/panel/types/impl/g2'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { addExtremumText } from '@/views/chart/components/js/extremumUitl'

const { t } = useI18n()
/**
 * 分组柱状图
 */
export class GroupBar extends StackBar {
  properties = BAR_EDITOR_PROPERTY
  propertyInner = {
    ...this['propertyInner'],
    'label-selector': [...BAR_EDITOR_PROPERTY_INNER['label-selector'], 'vPosition', 'showExtremum']
  }
  axisConfig = {
    ...this['axisConfig'],
    yAxis: {
      name: `${t('chart.drag_block_value_axis')} / ${t('chart.quota')}`,
      type: 'q',
      limit: 1
    }
  }

  async drawChart(drawOptions: G2DrawOptions<G2Column>): Promise<G2Column> {
    const plot = await super.drawChart(drawOptions)
    if (!plot) {
      return plot
    }
    const { chart } = drawOptions
    const { xAxis, xAxisExt, yAxis } = chart
    let innerSort = !!(xAxis.length && xAxisExt.length && yAxis.length)
    if (innerSort && yAxis[0].sort === 'none') {
      innerSort = false
    }
    if (innerSort && xAxisExt[0].sort !== 'none') {
      const sortPriority = chart.sortPriority ?? []
      const yAxisIndex = sortPriority?.findIndex(e => e.id === yAxis[0].id)
      const xAxisExtIndex = sortPriority?.findIndex(e => e.id === xAxisExt[0].id)
      if (xAxisExtIndex <= yAxisIndex) {
        innerSort = false
      }
    }
    if (!innerSort) {
      return plot
    }
    return plot
  }

  protected configLabel(chart: Chart, options: ViewSpec): ViewSpec {
    const customAttr = parseJson(chart.customAttr)
    const { label: labelAttr } = customAttr
    if (!labelAttr || !labelAttr.show) return options

    const { children } = options
    if (labelAttr.showExtremum) {
      const { x: xField, y: yField, color: colorField } = children[0].encode
      addExtremumText(options.children, [], xField, yField, colorField, false)
    }
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
      formatter: (value, _data) => valueFormatter(value, labelAttr.labelFormatter),
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

  protected configColor(_chart: Chart, options: ViewSpec): ViewSpec {
    // return this.configGroupColor(chart, options)
    return options
  }

  public setupSeriesColor(_chart: ChartObj, _data?: any[]): ChartBasicStyle['seriesColor'] {
    // return setUpGroupSeriesColor(chart, data)
    return []
  }

  protected setupOptions(chart: Chart, options: ViewSpec): ViewSpec {
    return flow(
      this.configTheme,
      this.configEmptyDataStrategy,
      this.configColor,
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

  constructor(name = 'bar-group') {
    super(name)
    this.intervalOptions.transform = [{ type: 'dodgeX' }]
    this.axis = [...BAR_AXIS_TYPE, 'xAxisExt']
  }
}
