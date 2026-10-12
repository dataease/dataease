import type { Chart as G2Chart, G2Spec } from '@antv/g2'

/**
 * 组合图仅在渲染通道中使用轴归属，保留原始 category 供展示和业务交互使用
 */
export const getMixSeriesKey = (axis: string, category: unknown): string =>
  JSON.stringify([axis, String(category)])

/**
 * 提示补全也遵循图例筛选，避免从源数据重新补回已隐藏的系列
 */
export const trackMixSeriesSelection = (chart: G2Chart) => {
  let selected: Set<string> | undefined
  const filter = event => {
    if (event?.data?.channel === 'color' && Array.isArray(event.data.values)) {
      selected = new Set(event.data.values)
    }
  }
  const focus = event => {
    if (event?.data?.channel === 'color' && event.data.value !== undefined) {
      selected = new Set([event.data.value])
    }
  }
  const reset = () => {
    selected = undefined
  }
  const dispose = () => {
    chart.off('legend:filter', filter)
    chart.off('legend:focus', focus)
    chart.off('legend:reset', reset)
    chart.off('beforedestroy', dispose)
  }
  chart.on('legend:filter', filter)
  chart.on('legend:focus', focus)
  chart.on('legend:reset', reset)
  chart.on('beforedestroy', dispose)
  return (key: string) => !selected || selected.has(key)
}

export const usesMixSeriesKey = (type: string): boolean =>
  ['chart-mix', 'chart-mix-group', 'chart-mix-stack', 'chart-mix-dual-line'].includes(type)

/**
 * 普通柱线图左侧按指标着色，其余系列按原始分类值着色
 */
export const getMixSeriesCategory = (chart: Chart, axis: string, datum: Record<string, any>) =>
  chart.type === 'chart-mix' && axis === 'left'
    ? chart.yAxis[0]?.chartShowName ?? chart.yAxis[0]?.name
    : datum.category

interface MixSeriesGroup {
  axis: 'left' | 'right'
  marks: G2Spec[]
  relations: [string, string][]
  category?: (datum: Record<string, any>) => unknown
}

/**
 * 显式指定各侧图元，仅替换颜色通道，保留分组、堆叠、数据和样式配置
 */
export const configureMixSeries = (groups: MixSeriesGroup[]) => {
  const names = new Map<string, string>()
  const relations: [string, string][] = groups.flatMap(group =>
    group.relations
      .filter(([name, color]) => name !== undefined && name !== null && Boolean(color))
      .map(([name, color]): [string, string] => {
        const id = getMixSeriesKey(group.axis, name)
        names.set(id, name)
        return [id, color]
      })
  )
  const scale = {
    type: 'ordinal' as const,
    key: 'color',
    independent: false,
    domain: relations.map(([id]) => id),
    range: relations.map(([, color]) => color),
    relations
  }
  groups.forEach(group => {
    group.marks.forEach(mark => {
      mark.encode.color = datum =>
        getMixSeriesKey(group.axis, group.category ? group.category(datum) : datum.category)
      mark.scale = { ...mark.scale, color: { ...scale } }
    })
  })
  return { scale, names }
}

/**
 * 内置与独立图例共用名称还原，长文本提示不暴露内部系列标识
 */
export const formatMixSeriesLegend = (legend: Record<string, any>, names: Map<string, string>) => {
  const labelFormatter = legend.labelFormatter
  const itemPoptip = legend.itemPoptip
  const displayName = id => names.get(id) ?? id
  legend.labelFormatter = id => {
    const name = displayName(id)
    return typeof labelFormatter === 'function' ? labelFormatter(name) : name
  }
  if (typeof itemPoptip === 'function') {
    legend.itemPoptip = item => {
      const poptip = itemPoptip({ ...item, id: displayName(item.id) })
      return poptip && typeof poptip.render === 'function'
        ? { ...poptip, render: item => poptip.render({ ...item, id: displayName(item.id) }) }
        : poptip
    }
  }
}

/**
 * 提示取色、排序和显隐使用与图形相同的系列标识
 */
export const createMixSeriesTooltip = (
  chart: Chart,
  instance: G2Chart,
  relations: [string, string][]
) => {
  const selected = trackMixSeriesSelection(instance)
  const colors = new Map(relations)
  const order = new Map(relations.map(([key], index) => [key, index]))
  const keyOf = (item: Record<string, any>) => {
    const axis = item.left ? 'left' : 'right'
    return getMixSeriesKey(axis, getMixSeriesCategory(chart, axis, item))
  }
  return {
    visible: (item: Record<string, any>) => selected(keyOf(item)),
    color: (item: Record<string, any>) => colors.get(keyOf(item)) ?? item.color,
    keyOf,
    compare: (a: { groupIndex: number; seriesKey: string }, b: typeof a) =>
      a.groupIndex - b.groupIndex ||
      (order.get(a.seriesKey) ?? relations.length) - (order.get(b.seriesKey) ?? relations.length)
  }
}
