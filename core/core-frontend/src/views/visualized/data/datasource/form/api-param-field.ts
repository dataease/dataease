interface ApiParamField {
  name?: string
  originName?: string
}

export const getApiParamFieldValue = (field: ApiParamField) => {
  return field?.name || field?.originName || ''
}

export const getApiParamFieldKey = (field: ApiParamField, index: number) => {
  return `${getApiParamFieldValue(field)}-${index}`
}
