import { EnumValue, enumValueObj } from '@/api/dataset'

let filterEnumMap = {}

const findFilterEnum = async (val: EnumValue) => {
  const queryId = val.queryId
  const displayId = val.displayId
  const arr = await enumValueObj({ queryId: queryId, displayId: displayId, searchText: '' })
  return arr?.reduce((acc, item) => {
    acc[item[displayId]] = item[queryId]
    return acc
  }, {})
}

export const filterEnumParams = (queryParams, fieldId: string) => {
  const resultMap = filterEnumMap[fieldId]
  if (resultMap) {
    const resultParams = []
    queryParams.forEach(param => {
      resultParams.push(resultMap[param] || param)
    })
    return resultParams
  } else {
    return queryParams
  }
}

export const filterEnumMapSync = async componentData => {
  filterEnumMap = {}
  for (const element of componentData) {
    if (element.component === 'VQuery') {
      for (const filterItem of element.propValue) {
        const { optionValueSource, field, displayId } = filterItem
        if (optionValueSource === 1 && field.id) {
          filterEnumMap[field.id] = await findFilterEnum({
            queryId: field.id,
            displayId,
            searchText: ''
          })
        }
      }
    }
  }
}
