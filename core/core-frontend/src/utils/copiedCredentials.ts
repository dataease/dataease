/** Copy only configuration data returned by the masked configuration APIs. */
export const clearCopiedCredentials = <T>(value: T): T => {
  if (value === '******') return '' as T
  if (Array.isArray(value)) return value.map(clearCopiedCredentials) as T
  if (value !== null && typeof value === 'object') {
    return Object.fromEntries(
      Object.entries(value).map(([key, item]) => [key, clearCopiedCredentials(item)])
    ) as T
  }
  return value
}
