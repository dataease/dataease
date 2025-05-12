import {
  BAR_EDITOR_PROPERTY,
  BAR_EDITOR_PROPERTY_INNER
} from '@/views/chart/components/js/panel/charts/g2/bar/common'
import { flow, parseJson, setUpStackSeriesColor } from '@/views/chart/components/js/util'
import { Bar } from '@/views/chart/components/js/panel/charts/g2/bar/bar'
import { formatterItem, valueFormatter } from '@/views/chart/components/js/formatter'
import { groupBy } from 'lodash'
import {
  handleEmptyDataStrategy,
  ViewSpec
} from '@/views/chart/components/js/panel/charts/g2/bar/barUtil'

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

    const labels = []
    if (labelAttr.showStackQuota ?? true) {
      labels.push({
        text: 'value',
        fillOpacity: 1,
        fill: labelAttr.color,
        fontSize: labelAttr.fontSize,
        ...position,
        formatter: (value, _data) => valueFormatter(value, labelAttr.labelFormatter),
        ...transform
      })
    }

    if (labelAttr.showTotal) {
      const formatterCfg = labelAttr.labelFormatter ?? formatterItem
      const groupedData = groupBy(children[0].data.value, 'field')
      for (const [key, values] of Object.entries(groupedData)) {
        const total = values.reduce((a, b) => a + b.value, 0)
        const value = valueFormatter(total, formatterCfg)
        children.push({
          type: 'text',
          data: [key, total],
          style: {
            text: value,
            textAlign: 'center',
            dy: -10,
            fill: labelAttr.color,
            fontSize: labelAttr.fontSize
          },
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

  protected configTooltip(_chart: Chart, options: ViewSpec): ViewSpec {
    return options
  }

  protected configColor(_chart: Chart, options: ViewSpec): ViewSpec {
    return options
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
    const { data } = options.children[0]
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
    const { data } = options.children[0]
    if (!data?.length) return options
    handleEmptyDataStrategy(chart, options)
    return options
  }

  public setupSeriesColor(chart: ChartObj, data?: any[]): ChartBasicStyle['seriesColor'] {
    return setUpStackSeriesColor(chart, data)
  }

  protected setupOptions(chart: Chart, options: ViewSpec): ViewSpec {
    return flow(
      this.configTheme,
      this.configEmptyDataStrategy,
      this.configData,
      this.configColor,
      this.configBasicStyle,
      this.configLabel,
      this.configTooltip,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configAnalyse
    )(chart, options, {}, this)
  }

  constructor(name = 'bar-stack') {
    super(name)
    this.intervalOptions.transform = [{ type: 'stackY' }]
    this.axis = [...this.axis, 'extStack']
  }
}
