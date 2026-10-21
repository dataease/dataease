export const getConditionSeparator = (separatorType = 'comma', customSeparator = '') => {
  const separators: Record<string, string> = {
    comma: ',',
    chineseComma: '，',
    semicolon: ';',
    space: ' ',
    newline: '\n',
    custom: customSeparator
  }
  return separators[separatorType] ?? separators.comma
}

export const splitConditionValues = (
  value: string | number | null | undefined,
  separatorType = 'comma',
  customSeparator = ''
) => {
  const separator = getConditionSeparator(separatorType, customSeparator)
  if (!separator || value == null) {
    return []
  }
  return String(value)
    .split(separator)
    .map(item => item.trim())
    .filter(Boolean)
}
