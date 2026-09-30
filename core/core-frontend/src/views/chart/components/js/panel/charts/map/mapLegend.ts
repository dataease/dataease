import { formatterItem, valueFormatter } from '../../../formatter'
import { parseJson } from '@/views/chart/components/js/util'

export const mapRangeKey = (range: number[]) => JSON.stringify(range)

export const mapLegendFormatter = (chart: Chart) => {
  const config: MapLegendOptions = parseJson(chart.customStyle)?.legend?.map || {}
  const custom = config.formatMode === 'custom'
  const formatter = {
    ...formatterItem,
    ...(custom ? config.formatterCfg : chart.yAxis?.[0]?.formatterCfg)
  }
  return (value: unknown): string => {
    if (
      (typeof value !== 'number' && typeof value !== 'string') ||
      (typeof value === 'string' && !value.trim()) ||
      !Number.isFinite(Number(value))
    ) {
      return '-'
    }
    const number = Number(value)
    // Existing maps retain their integer labels until a formatting mode is explicitly selected.
    if (!config.formatMode || config.formatMode === 'legacy') return number.toFixed(0)
    return `${custom ? config.prefix || '' : ''}${valueFormatter(number, { ...formatter })}`
  }
}

export const mapLegendLabel = (chart: Chart, value: unknown): string => {
  const format = mapLegendFormatter(chart)
  if (!Array.isArray(value)) return format(value)
  const misc = parseJson(chart.customAttr)?.misc
  if (!chart.drill && !misc?.mapAutoLegend && misc?.mapLegendRangeType === 'custom') {
    const label = parseJson(chart.customStyle)?.legend?.map?.rangeLabels?.[mapRangeKey(value)]
    if (label?.trim()) return label
  }
  const mode = parseJson(chart.customStyle)?.legend?.map?.formatMode
  if (!mode || mode === 'legacy') return value.map(format).join('-')
  const [min, max] = value
  if (min === -Infinity && Number.isFinite(max)) return `< ${format(max)}`
  if (max === Infinity && Number.isFinite(min)) return `≥ ${format(min)}`
  if (min === max) return format(min)
  return `${format(min)} - ${format(max)}`
}

export const mapTooltipValue = (chart: Chart, value: number, formatter: BaseFormatter) =>
  chart.type === 'map' && parseJson(chart.customStyle)?.legend?.map?.syncTooltip
    ? mapLegendFormatter(chart)(value)
    : valueFormatter(value, formatter)
