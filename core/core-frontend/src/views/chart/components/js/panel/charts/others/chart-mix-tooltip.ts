type MixTooltipItem = {
  name?: string
  data?: {
    quotaList?: Array<{ id?: string }>
    valueExt?: unknown
  }
}

type MixTooltipAxisType = 'yAxis' | 'yAxisExt'

function getMixTooltipAxisType(item: MixTooltipItem): MixTooltipAxisType {
  if (
    item?.name === 'valueExt' ||
    Object.prototype.hasOwnProperty.call(item?.data ?? {}, 'valueExt')
  ) {
    return 'yAxisExt'
  }
  return 'yAxis'
}

export function getMixTooltipFormatter<T>(
  formatterMap: Record<string, T>,
  item: MixTooltipItem
): T | undefined {
  const fieldId = item?.data?.quotaList?.[0]?.id
  if (!fieldId) {
    return undefined
  }
  const axisType = getMixTooltipAxisType(item)
  return formatterMap[`${fieldId}-${axisType}`] ?? formatterMap[fieldId]
}
