import {
  BAR_AXIS_TYPE,
  BAR_EDITOR_PROPERTY,
  BAR_EDITOR_PROPERTY_INNER
} from '@/views/chart/components/js/panel/charts/g2/bar/common'
import {
  flow,
  hexColorToRGBA,
  parseJson,
  setUpGroupSeriesColor
} from '@/views/chart/components/js/util'
import { StackBar } from '@/views/chart/components/js/panel/charts/g2/bar/stack-bar'
import { Chart as G2Column, stdlib } from '@antv/g2'
import { ViewSpec } from '@/views/chart/components/js/panel/charts/g2/bar/barUtil'
import { useI18n } from '@/hooks/web/useI18n'
import { G2DrawOptions } from '@/views/chart/components/js/panel/types/impl/g2'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { addExtremumText } from '@/views/chart/components/js/extremumUitl'
import { setGradientColor } from '@/views/chart/components/js/panel/common/common_antv'
import { defaultsDeep } from 'lodash-es'

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
    if (!labelAttr.show) return options

    const { children } = options
    if (labelAttr.showExtremum) {
      const { x: xField, color: colorField } = children[0].encode
      addExtremumText(options.children, [], xField, 'value', colorField, false)
    }

    if (!labelAttr.childrenShow) {
      if (labelAttr.showExtremum) {
        const ghostLabel = {
          text: (d: any) => (d.extremum ? '' : ''),
          fillOpacity: 0,
          pointerEvents: 'none',
          fontSize: 0
        } as any
        return {
          ...options,
          children: [
            {
              ...children[0],
              labels: [ghostLabel]
            },
            ...children.slice(1)
          ]
        }
      }
      return options
    }
    const position = {
      position: labelAttr.position === 'middle' ? 'inside' : labelAttr.position,
      textAlign: 'center',
      dy: labelAttr.position === 'top' ? -10 : 0,
      dx: 0
    }
    const label = {
      text: 'value',
      fillOpacity: 1,
      pointerEvents: 'none',
      fill: labelAttr.color,
      fontSize: labelAttr.fontSize,
      ...position,
      formatter: (value, data) =>
        data.extremum && labelAttr.showExtremum
          ? ''
          : valueFormatter(value, labelAttr.labelFormatter)
    } as any
    if (!labelAttr.fullDisplay) {
      label.transform = [{ type: 'overlapHide' }]
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

  protected getTooltipItemName(chart: Chart, item: any): string {
    const quota = chart.yAxis?.[0]
    const quotaName = quota?.chartShowName || quota?.name
    const categoryName = chart.xAxisExt?.length ? super.getTooltipItemName(chart, item) : ''
    // marker 后展示指标名称
    return [quotaName, categoryName]
      .filter(name => name !== null && name !== undefined && `${name}`.trim() !== '')
      .join(' - ')
  }

  public setupSeriesColor(chart: ChartObj, data?: any[]): ChartBasicStyle['seriesColor'] {
    return setUpGroupSeriesColor(chart, data)
  }

  protected configColor(chart: Chart, options: ViewSpec): ViewSpec {
    const { basicStyle } = parseJson(chart.customAttr)
    const { seriesColor } = basicStyle
    if (!seriesColor?.length) {
      return options
    }
    const { xAxis, xAxisExt, yAxis } = chart
    if (!xAxis?.length || !yAxis?.length) {
      return options
    }
    const relations = []
    if (xAxisExt?.length) {
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
          relations.push([' ', color])
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

  protected configGroupPosition(_chart: Chart, options: ViewSpec): ViewSpec {
    const interval = options.children[0]
    const data = interval.data || options.data
    if (!Array.isArray(data) || !data.length) return options
    const field = interval.encode.x as string
    const categories = new Set(data.map(item => item[field]))
    const singleBar = categories.size === data.length
    const colorField = interval.encode.color as string
    const series = [...new Set(data.map(item => item[colorField]))]
    // 完整分组保留原有布局；稀疏分组按原系列顺序紧凑排列，避免缺失系列留空。
    if (!singleBar && data.length === categories.size * series.length) return options
    const centeredDodge = () => (indices, mark) => {
      const x = mark.encode.x.value
      const color = mark.encode.color?.value || indices.map(() => '')
      const order = new Map([...new Set(indices.map(i => color[i]))].map((v, i) => [v, i]))
      const groups = new Map<unknown, number[]>()
      indices.forEach(i => {
        const group = groups.get(x[i]) || []
        group.push(i)
        groups.set(x[i], group)
      })
      const count = Math.max(1, ...[...groups.values()].map(group => group.length))
      const positions = []
      groups.forEach(group => {
        group.sort((a, b) => Number(order.get(color[a])) - Number(order.get(color[b])))
        // 用半个槽位补齐左右余量，奇偶数量不同的分组也能对齐中心。
        group.forEach((index, rank) => (positions[index] = rank + (count - group.length) / 2))
      })
      const centeredBand = (scaleOptions, context) => {
        const scale = stdlib()['scale.band'](scaleOptions, context)
        const map = scale.map.bind(scale)
        scale.map = value => {
          const slot = Math.floor(Number(value))
          return Number(map(slot)) + (Number(value) - slot) * scale.getStep(slot)
        }
        return scale
      }
      return [
        indices,
        {
          ...mark,
          encode: { ...mark.encode, series: { type: 'column', value: positions } },
          scale: {
            ...mark.scale,
            series: {
              type: centeredBand,
              name: 'series',
              domain: Array.from({ length: count }, (_, i) => i),
              paddingInner: 0.1,
              paddingOuter: 0.1
            }
          }
        }
      ]
    }
    return {
      ...options,
      children: [
        {
          ...interval,
          transform: interval.transform?.map(transform =>
            transform.type === 'dodgeX' ? { type: centeredDodge } : transform
          )
        },
        ...options.children.slice(1)
      ]
    }
  }

  protected setupOptions(chart: Chart, options: ViewSpec): ViewSpec {
    return flow(
      this.configTheme,
      this.configEmptyDataStrategy,
      this.configColor,
      this.configBasicStyle,
      this.configGroupPosition,
      this.configLabel,
      this.configTooltip,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configAnalyse,
      this.configBarConditions,
      this.configSlider
    )(chart, options, {}, this)
  }

  constructor(name = 'bar-group') {
    super(name)
    this.intervalOptions.transform = [{ type: 'dodgeX' }]
    this.axis = [...BAR_AXIS_TYPE, 'xAxisExt']
  }
}
