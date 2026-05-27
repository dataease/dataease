type BidirectionalBarLabelParam = {
  'series-field-key'?: string
}

function getBidirectionalBarLabelAxisType(param: BidirectionalBarLabelParam) {
  return param?.['series-field-key'] === 'valueExt' ? 'yAxisExt' : 'yAxis'
}

export function getBidirectionalBarLabelFormatter<T>(
  formatterMap: Record<string, T> | undefined,
  fieldId: string,
  param: BidirectionalBarLabelParam
): T | undefined {
  if (!fieldId) {
    return undefined
  }
  const axisType = getBidirectionalBarLabelAxisType(param)
  return formatterMap?.[`${fieldId}-${axisType}`] ?? formatterMap?.[fieldId]
}
