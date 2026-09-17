/**
 * 分组标识、空值区分、异常点位置对齐
 */
export const BOX_CATEGORY_FIELD = 'boxPlotCategory'
export const BOX_GROUP_FIELD = 'boxPlotGroup'
export const BOX_ID_FIELD = 'boxPlotId'

// 展示字段与原始维度分离，NULL、空字符串和同名文字不能共用分组键
export const boxPlotKey = (value: unknown) => JSON.stringify([value ?? null])

// 兼容历史保存的 RGBA/带 alpha 的 HEX，异常点透明度统一由整体控件决定
export function boxPlotOpaqueColor(color: string) {
  if (/^#[\da-f]{8}$/i.test(color)) return color.slice(0, 7)
  if (/^#[\da-f]{4}$/i.test(color)) return color.slice(0, 4)
  const rgba = color.match(/^rgba?\(([^)]+)\)$/i)
  return rgba ? `rgb(${rgba[1].split(',').slice(0, 3).join(',')})` : color
}

export function boxPlotDomain(values: unknown[], emptyLabel: string) {
  const domain = new Map<string, { value: unknown; label: string }>()
  const labels = new Set<string>()
  values.forEach(value => {
    const key = boxPlotKey(value)
    if (domain.has(key)) return
    const text = value == null ? 'NULL' : String(value).trim() ? String(value) : emptyLabel
    let label = text
    let suffix = 1
    while (labels.has(label)) label = `${text} (${++suffix})`
    labels.add(label)
    domain.set(key, { value, label })
  })
  return domain
}

type AdjustedDatum = Record<string, any>

// 复用箱体实际 dodge 结果，异常点的稀疏程度不能改变所属箱体的位置
export function alignBoxPlotOutliers(
  boxes: AdjustedDatum[][],
  points: AdjustedDatum[][],
  xField: string
) {
  const positions = new Map<string, number>()
  boxes.forEach(group =>
    group.forEach(datum => {
      positions.set(datum[BOX_ID_FIELD], datum[xField])
    })
  )
  return points.map(group =>
    group.filter(datum => {
      const x = positions.get(datum[BOX_ID_FIELD])
      if (!Number.isFinite(x)) return false
      datum[xField] = x
      return true
    })
  )
}
