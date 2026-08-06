import { parseJson } from '@/views/chart/components/js/util'
import type { ViewSpec } from '@/views/chart/components/js/panel/charts/g2/bar/barUtil'
import type { Chart as G2Column } from '@antv/g2'

// 空值锚点用于保留全空或全 0 维度的布局与命中区域
export const PERCENTAGE_STACK_EMPTY_ANCHOR_FIELD = '__DE_PERCENTAGE_STACK_EMPTY_ANCHOR__'
const PERCENTAGE_STACK_EMPTY_ANCHOR_VALUE = 1e-12
const PERCENTAGE_STACK_ZERO_ANCHOR_FIELD = '__DE_PERCENTAGE_STACK_ZERO_ANCHOR__'
const PERCENTAGE_STACK_TOOLTIP_SOURCE_CACHE = new WeakMap<any[], Map<any, any[]>>()

export function isPercentageStackEmptyAnchor(item) {
  return !!item?.[PERCENTAGE_STACK_EMPTY_ANCHOR_FIELD]
}

export function isPercentageStackZeroAnchor(item) {
  return isPercentageStackEmptyAnchor(item) && !!item?.[PERCENTAGE_STACK_ZERO_ANCHOR_FIELD]
}

export function filterPercentageStackEmptyAnchorTooltipItem(item) {
  // 在 G2 更新 tooltip DOM 前过滤纯空锚点，避免极小占位值闪现
  return !isPercentageStackEmptyAnchor(item) || isPercentageStackZeroAnchor(item)
}

export function configPercentageStackEmptyAnchorTooltipGuard(newChart: G2Column) {
  newChart.on('tooltip:show', event => {
    // 纯空锚点继续隐藏，真实全 0 锚点交给 tooltip 恢复零值明细
    if (isOnlyPercentageStackEmptyAnchorTooltip(event)) {
      newChart.emit('tooltip:hide')
    }
  })
}

export function configPercentageStackEmptyDataStrategy(
  chart: Chart,
  options: ViewSpec,
  handleEmptyDataStrategy: () => void
): ViewSpec {
  const strategy = parseJson(chart.senior).functionCfg.emptyDataStrategy
  const data = getPercentageStackOptionsData(options)
  // 先记录原始维度顺序和示例数据，空值处理后才能把锚点补回正确位置。
  const anchorContext = buildEmptyAnchorContext(data)
  // 沿用原空值策略，保持为空与置为 0 的展示语义分离
  handleEmptyDataStrategy()
  // 隐藏空值时不恢复已删空维度，但真实全 0 维度仍需锚点支撑标签和 tooltip
  appendEmptyAnchors(
    getPercentageStackOptionsData(options),
    anchorContext,
    strategy !== 'ignoreData'
  )
  return options
}

export function configPercentageStackEmptyAnchorStyle(options: ViewSpec): ViewSpec {
  const { children } = options
  const child = children[0]
  const style = child.style ?? {}
  const { fill, fillOpacity, strokeOpacity } = style

  return {
    ...options,
    children: [
      {
        ...child,
        style: {
          ...style,
          fill: data => {
            if (isPercentageStackEmptyAnchor(data)) return 'rgba(0,0,0,0)'
            return typeof fill === 'function' ? fill(data) : fill ?? data.color
          },
          fillOpacity: data => {
            // 保留极低透明度，让 G2 仍能为锚点生成可命中的 interval。
            if (isPercentageStackEmptyAnchor(data)) return 0.001
            return typeof fillOpacity === 'function' ? fillOpacity(data) : fillOpacity
          },
          strokeOpacity: data => {
            if (isPercentageStackEmptyAnchor(data)) return 0
            return typeof strokeOpacity === 'function' ? strokeOpacity(data) : strokeOpacity
          }
        }
      },
      ...children.slice(1)
    ]
  }
}

export function getPercentageStackOptionsData(options: ViewSpec): any[] {
  const data = options.children?.[0]?.data ?? options.data
  if (Array.isArray(data)) return data
  if (Array.isArray(data?.value)) return data.value
  return []
}

export function getPercentageStackFieldTotal(dataItems: any[], field) {
  return (
    dataItems?.reduce(
      (acc, item) =>
        item?.field === field && !isPercentageStackEmptyAnchor(item)
          ? acc + (Number(item.value) || 0)
          : acc,
      0
    ) || 0
  )
}

export function shouldHidePercentageStackLabelValue(value, item, fieldTotal) {
  if (isPercentageStackEmptyAnchor(item)) {
    return !isPercentageStackZeroAnchor(item)
  }
  const numberValue = Number(value)
  if (!Number.isFinite(numberValue)) {
    return true
  }
  // 全 0 维度只由锚点显示一个标签，避免每个零值系列重复占位
  return numberValue === 0 && fieldTotal === 0
}

export function isPercentageStackZeroLabelItem(item) {
  return (
    isPercentageStackZeroAnchor(item) ||
    (!isPercentageStackEmptyAnchor(item) && isPercentageStackZeroValue(item?.value))
  )
}

export function getPercentageStackZeroLabelAlignMap(data: any[], seriesOrder: any[]) {
  const alignMap = new WeakMap<object, 'start' | 'center' | 'end'>()
  const fieldItemsMap = new Map<any, any[]>()
  const seriesRank = new Map(seriesOrder.map((series, index) => [`${series}`, index]))

  data.forEach(item => {
    if (isPercentageStackEmptyAnchor(item)) return
    const fieldItems = fieldItemsMap.get(item?.field) ?? []
    fieldItems.push(item)
    fieldItemsMap.set(item?.field, fieldItems)
  })

  fieldItemsMap.forEach(fieldItems => {
    const orderedItems = [...fieldItems].sort(
      (first, second) =>
        (seriesRank.get(`${first?.category}`) ?? seriesOrder.length) -
        (seriesRank.get(`${second?.category}`) ?? seriesOrder.length)
    )
    const total = orderedItems.reduce((sum, item) => sum + (Number(item?.value) || 0), 0)
    if (total <= 0) return

    let cumulative = 0
    const tolerance = Math.max(total, 1) * Number.EPSILON * 10
    orderedItems.forEach(item => {
      const value = Number(item?.value)
      if (isPercentageStackZeroValue(item?.value)) {
        // 零宽标签按堆叠落点向绘图区内侧展开
        const align =
          cumulative <= tolerance ? 'start' : cumulative >= total - tolerance ? 'end' : 'center'
        alignMap.set(item, align)
        return
      }
      if (Number.isFinite(value)) cumulative += value
    })
  })

  return alignMap
}

export function filterPercentageStackTooltipItems(
  items: any[] = [],
  options?: ViewSpec,
  seriesOrder: any[] = []
) {
  const realItems = items?.filter(item => !isPercentageStackEmptyAnchor(item)) ?? []
  const field = items?.find(item => item?.field !== undefined)?.field
  const sourceItems = getPercentageStackTooltipSourceItems(options, field)
  if (sourceItems.length) {
    // 零宽 interval 可能不进入 G2 命中结果，从图形源数据补齐同维度系列
    return sourceItems.map(item => {
      const renderedItem = realItems.find(candidate => isSamePercentageStackSeries(candidate, item))
      return {
        ...item,
        value: Number(item.value),
        color:
          renderedItem?.color ??
          getPercentageStackSeriesColor(item, options, seriesOrder, items?.[0]?.color)
      }
    })
  }
  if (realItems.length) return realItems
  return []
}

export function formatPercentageStackRatio(value, total, decimalCount) {
  const itemValue = value ? (value as number) : 0
  return `${total ? ((itemValue / total) * 100).toFixed(decimalCount) : (0).toFixed(decimalCount)}%`
}

function isOnlyPercentageStackEmptyAnchorTooltip(event) {
  const items = event?.data?.items
  return (
    Array.isArray(items) &&
    items.length > 0 &&
    items.every(item => isPercentageStackEmptyAnchor(item)) &&
    !items.some(item => isPercentageStackZeroAnchor(item))
  )
}

function buildEmptyAnchorContext(data: any[]) {
  const fields: any[] = []
  const sampleByField = new Map<any, any>()
  const indexByField = new Map<any, number>()
  let fallbackSample

  data?.forEach((item, index) => {
    if (item?.field === undefined) return
    if (!sampleByField.has(item.field)) {
      fields.push(item.field)
      sampleByField.set(item.field, item)
      indexByField.set(item.field, index)
    }
    if (!fallbackSample && item?.category !== undefined) {
      fallbackSample = item
    }
  })

  return { fields, sampleByField, indexByField, fallbackSample }
}

function appendEmptyAnchors(data: any[], anchorContext, restoreMissingField) {
  if (!data?.length && !anchorContext?.fields?.length) return

  anchorContext.fields.forEach(field => {
    const hasFieldData = data.some(item => item?.field === field)
    if (!restoreMissingField && !hasFieldData) return
    // 仅对全空或总和为 0 的维度补极小值锚点，避免 normalizeY 无法生成图形
    if (!needsEmptyAnchor(data, field)) return
    const sample = anchorContext.sampleByField.get(field) ?? anchorContext.fallbackSample ?? {}
    data.splice(getEmptyAnchorInsertIndex(data, field, anchorContext), 0, {
      ...sample,
      field,
      category: sample.category ?? anchorContext.fallbackSample?.category,
      group: sample.group ?? anchorContext.fallbackSample?.group,
      quotaList: sample.quotaList ?? anchorContext.fallbackSample?.quotaList,
      value: PERCENTAGE_STACK_EMPTY_ANCHOR_VALUE,
      [PERCENTAGE_STACK_EMPTY_ANCHOR_FIELD]: true,
      [PERCENTAGE_STACK_ZERO_ANCHOR_FIELD]: isPercentageStackZeroField(data, field)
    })
  })
}

function isPercentageStackZeroField(data: any[], field) {
  const fieldData = data.filter(
    item => item?.field === field && !isPercentageStackEmptyAnchor(item)
  )
  const hasZeroValue = fieldData.some(item => isPercentageStackZeroValue(item?.value))
  const hasNonZeroValue = fieldData.some(item => {
    if (item?.value === null || item?.value === undefined) return false
    const numberValue = Number(item.value)
    return !Number.isFinite(numberValue) || numberValue !== 0
  })
  return hasZeroValue && !hasNonZeroValue
}

function isPercentageStackZeroValue(value) {
  if (value === null || value === undefined || value === '') return false
  const numberValue = Number(value)
  return Number.isFinite(numberValue) && numberValue === 0
}

function isPercentageStackTooltipValue(value) {
  if (value === null || value === undefined || value === '') return false
  return Number.isFinite(Number(value))
}

function getPercentageStackTooltipSourceItems(options, field) {
  if (!options) return []
  const data = getPercentageStackOptionsData(options)
  let sourceMap = PERCENTAGE_STACK_TOOLTIP_SOURCE_CACHE.get(data)
  if (!sourceMap) {
    // 按数据数组缓存维度索引，避免 hover 时反复全量扫描
    sourceMap = new Map<any, any[]>()
    data.forEach(item => {
      if (isPercentageStackEmptyAnchor(item) || !isPercentageStackTooltipValue(item?.value)) return
      const sourceItems = sourceMap.get(item?.field) ?? []
      sourceItems.push(item)
      sourceMap.set(item?.field, sourceItems)
    })
    PERCENTAGE_STACK_TOOLTIP_SOURCE_CACHE.set(data, sourceMap)
  }
  return sourceMap.get(field) ?? []
}

function isSamePercentageStackSeries(first, second) {
  return `${first?.category}` === `${second?.category}` && `${first?.group}` === `${second?.group}`
}

function getPercentageStackSeriesColor(item, options, seriesOrder, fallbackColor) {
  const series = item?.category
  const colorScale = options?.scale?.color ?? {}
  const relation = colorScale.relations?.find(([key]) => `${key}` === `${series}`)
  if (relation?.[1]) return relation[1]

  const domain = colorScale.domain?.length ? colorScale.domain : seriesOrder
  const range = options?.children?.[0]?.scale?.color?.range?.length
    ? options.children[0].scale.color.range
    : options?.theme?.category10
  const seriesIndex = domain?.findIndex(value => `${value}` === `${series}`) ?? -1
  if (seriesIndex >= 0 && range?.length) {
    return range[seriesIndex % range.length]
  }
  return fallbackColor
}

function getEmptyAnchorInsertIndex(data: any[], field, anchorContext) {
  // 若该维度还有部分真实数据，锚点跟随真实数据之后，避免打散同一维度的数据。
  const sameFieldLastIndex = data.reduce(
    (lastIndex, item, index) => (item?.field === field ? index : lastIndex),
    -1
  )
  if (sameFieldLastIndex >= 0) {
    return sameFieldLastIndex + 1
  }

  // 若该维度已被空值策略删光，按空值处理前的原始维度顺序插回。
  const fieldIndex = anchorContext.indexByField.get(field)
  const nextIndex = data.findIndex(item => {
    const itemIndex = anchorContext.indexByField.get(item?.field)
    return itemIndex !== undefined && itemIndex > fieldIndex
  })
  return nextIndex === -1 ? data.length : nextIndex
}

function needsEmptyAnchor(data: any[], field) {
  const fieldData = data.filter(
    item => item?.field === field && !isPercentageStackEmptyAnchor(item)
  )
  if (!fieldData.length) return true
  const total = fieldData.reduce((sum, item) => sum + (Number(item.value) || 0), 0)
  return total === 0
}
