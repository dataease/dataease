import {
  BAR_AXIS_TYPE,
  BAR_EDITOR_PROPERTY_INNER
} from '@/views/chart/components/js/panel/charts/g2/bar/common'
import { flow, parseJson } from '@/views/chart/components/js/util'
import { StackBar } from '@/views/chart/components/js/panel/charts/g2/bar/stack-bar'
import { Transform, ViewSpec } from '@/views/chart/components/js/panel/charts/g2/bar/barUtil'
import { valueFormatter } from '@/views/chart/components/js/formatter'

/**
 * 分组堆叠柱状图
 */
export class GroupStackBar extends StackBar {
  propertyInner = {
    ...this['propertyInner'],
    'label-selector': [...BAR_EDITOR_PROPERTY_INNER['label-selector'], 'vPosition']
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

  constructor(name = 'bar-group-stack') {
    super(name)
    this.intervalOptions.encode = {
      ...this.intervalOptions.encode,
      series: d => d.group
    }
    this.intervalOptions.transform = [{ type: 'stackY', groupBy: ['x', 'series'] } as Transform]
    this.axis = [...BAR_AXIS_TYPE, 'xAxisExt', 'extStack']
  }
}
