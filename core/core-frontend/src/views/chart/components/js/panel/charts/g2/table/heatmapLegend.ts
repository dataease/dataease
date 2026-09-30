import { formatterItem, valueFormatter } from '../../../../formatter'
import { parseJson } from '@/views/chart/components/js/util'

export const heatmapDataDomain = (chart: Chart): [number, number] | undefined => {
  const fields = [...(chart.xAxis || []), ...(chart.xAxisExt || []), ...(chart.extColor || [])]
  const field = chart.extColor?.[0]?.dataeaseName
  const values = (chart.data?.tableRow || [])
    .filter(row => fields.every(axis => ![null, undefined, ''].includes(row[axis.dataeaseName])))
    .map(row => Number(row[field]))
    .filter(Number.isFinite)
  if (!values.length) return
  return values.reduce<[number, number]>(
    ([min, max], value) => [Math.min(min, value), Math.max(max, value)],
    [Infinity, -Infinity]
  )
}

export const validHeatmapRange = (min: number, max: number) =>
  typeof min === 'number' &&
  typeof max === 'number' &&
  Number.isFinite(min) &&
  Number.isFinite(max) &&
  min < max

export const heatmapLegendDomain = (chart: Chart): [number, number] | undefined => {
  const config = parseJson(chart.customStyle)?.legend?.heatmap
  if (config?.rangeMode === 'custom' && validHeatmapRange(config.min, config.max)) {
    return [config.min, config.max]
  }
  return heatmapDataDomain(chart)
}

export const heatmapLegendFormatter = (chart: Chart) => {
  const config = parseJson(chart.customStyle)?.legend?.heatmap
  const custom = config?.formatMode === 'custom'
  // The shared formatter normalizes its configuration; never mutate the metric's persisted settings.
  const formatter = {
    ...formatterItem,
    ...(custom ? config.formatterCfg : chart.extColor?.[0]?.formatterCfg)
  }
  return (value: number) =>
    `${custom ? config.prefix || '' : ''}${valueFormatter(value, { ...formatter })}`
}

export const heatmapValueSelected = (value: number, selection: number[], domain: number[]) => {
  // A handle at either end includes that tail, matching the clamped endpoint color.
  return (
    (selection[0] <= domain[0] || value >= selection[0]) &&
    (selection[1] >= domain[1] || value <= selection[1])
  )
}
