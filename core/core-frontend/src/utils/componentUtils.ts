import { EnumValue, enumValueObj } from '@/api/dataset'

const findFilterEnum = async (val: EnumValue) => {
  const queryId = val.queryId
  const displayId = val.displayId
  const arr = await enumValueObj({ queryId: queryId, displayId: displayId, searchText: '' })
  return arr?.reduce((acc, item) => {
    acc[item[queryId]] = item[displayId]
    return acc
  }, {})
}

export const filterEnumMap = async (queryParams, val: EnumValue) => {
  const resultMap = await findFilterEnum(val)
  const resultParams = []
  queryParams.forEach(param => {
    resultParams.push(resultMap[param] || param)
  })
  return resultParams
}
