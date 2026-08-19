export type CustomSortValue = string | number

const getValueKey = (value: CustomSortValue) => String(value).toLowerCase()

/**
 * Keep saved values while they remain queryable, then append newly available values
 * in the API order. The saved primitive is retained so existing snapshots stay stable.
 */
export const mergeCustomSortValues = (
  savedValues: CustomSortValue[] = [],
  visibleValues: CustomSortValue[] = []
): CustomSortValue[] => {
  const visibleKeys = new Set(visibleValues.map(getValueKey))
  const includedKeys = new Set<string>()
  const mergedValues: CustomSortValue[] = []

  savedValues.forEach(value => {
    const key = getValueKey(value)
    if (visibleKeys.has(key) && !includedKeys.has(key)) {
      mergedValues.push(value)
      includedKeys.add(key)
    }
  })

  visibleValues.forEach(value => {
    const key = getValueKey(value)
    if (!includedKeys.has(key)) {
      mergedValues.push(value)
      includedKeys.add(key)
    }
  })

  return mergedValues
}

export const formatCustomSortValue = (value: CustomSortValue) => {
  return value === '' ? '空值' : String(value)
}
