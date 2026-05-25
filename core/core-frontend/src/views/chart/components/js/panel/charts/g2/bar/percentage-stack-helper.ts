import { parseJson } from '@/views/chart/components/js/util'
import type { ViewSpec } from '@/views/chart/components/js/panel/charts/g2/bar/barUtil'
import type { Chart as G2Column } from '@antv/g2'

// 空值锚点只用于保留空维度的命中区域，不参与图例、标签和 tooltip 展示。
export const PERCENTAGE_STACK_EMPTY_ANCHOR_FIELD = '__DE_PERCENTAGE_STACK_EMPTY_ANCHOR__'
const PERCENTAGE_STACK_EMPTY_ANCHOR_VALUE = 1e-12

export function isPercentageStackEmptyAnchor(item) {
  return !!item?.[PERCENTAGE_STACK_EMPTY_ANCHOR_FIELD]
}

export function configPercentageStackEmptyAnchorTooltipGuard(newChart: G2Column) {
  newChart.on('tooltip:show', event => {
    // 鼠标只命中空值锚点时，隐藏 tooltip，避免展示一条假的空数据提示。
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
  handleEmptyDataStrategy()
  // 隐藏空值策略的语义是删除空维度，不补锚点，避免空柱子继续占位。
  if (strategy === 'ignoreData') {
    return options
  }
  appendEmptyAnchors(getPercentageStackOptionsData(options), anchorContext)
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

export function getPercentageStackZeroTotalFields(data: any[]) {
  const fieldTotalMap = new Map<any, number>()
  data?.forEach(item => {
    if (isPercentageStackEmptyAnchor(item)) return
    fieldTotalMap.set(
      item?.field,
      (fieldTotalMap.get(item?.field) || 0) + (Number(item.value) || 0)
    )
  })
  return new Set([...fieldTotalMap].filter(([, total]) => total === 0).map(([field]) => field))
}

export function shouldHidePercentageStackLabelValue(value, item, fieldTotal) {
  const numberValue = Number(value)
  if (isPercentageStackEmptyAnchor(item) || !Number.isFinite(numberValue)) {
    return true
  }
  return numberValue === 0 && fieldTotal > 0
}

export function filterPercentageStackTooltipItems(items: any[] = []) {
  return items?.filter(item => !isPercentageStackEmptyAnchor(item)) ?? []
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
    items.every(item => isPercentageStackEmptyAnchor(item))
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

function appendEmptyAnchors(data: any[], anchorContext) {
  if (!data?.length && !anchorContext?.fields?.length) return

  anchorContext.fields.forEach(field => {
    // 仅对全空或全 0 的维度补一个极小值锚点，避免 normalizeY 出现无可命中的空维度。
    if (!needsEmptyAnchor(data, field)) return
    const sample = anchorContext.sampleByField.get(field) ?? anchorContext.fallbackSample ?? {}
    data.splice(getEmptyAnchorInsertIndex(data, field, anchorContext), 0, {
      ...sample,
      field,
      category: sample.category ?? anchorContext.fallbackSample?.category,
      group: sample.group ?? anchorContext.fallbackSample?.group,
      quotaList: sample.quotaList ?? anchorContext.fallbackSample?.quotaList,
      value: PERCENTAGE_STACK_EMPTY_ANCHOR_VALUE,
      [PERCENTAGE_STACK_EMPTY_ANCHOR_FIELD]: true
    })
  })
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
