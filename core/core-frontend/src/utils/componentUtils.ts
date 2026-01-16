import { EnumValue, enumValueObj } from '@/api/dataset'

let filterEnumMap = {}

const filterIdNameEnumMap = {}

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

export const filterEnumParamsReduce = (queryParams, fieldId: string) => {
  const resultMap = filterEnumMap[fieldId]
  if (resultMap) {
    const resultMapReduce = Object.fromEntries(
      Object.entries(resultMap).map(([key, value]) => [value, key])
    )
    const resultParams = []
    queryParams.forEach(param => {
      resultParams.push(resultMapReduce[param] || param)
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

export function filterParamsOptions(params, paramsOption) {
  // 如果 params 为空，直接返回 null
  if (!params || (Array.isArray(params) && params.length === 0)) {
    return null
  }
  // 如果 paramsOption 为空，直接返回 null
  if (!paramsOption || paramsOption.length === 0) {
    return null
  }
  // 创建 paramsOption 集合和前缀集合用于快速查找
  const optionSet = new Set(paramsOption)
  const prefixSet = new Set()
  // 收集所有可能的父级前缀
  paramsOption.forEach(option => {
    if (option.includes('-de-')) {
      const parts = option.split('-de-')
      // 收集所有前缀：父级、祖父级等
      for (let i = 1; i < parts.length; i++) {
        const prefix = parts.slice(0, i).join('-de-')
        prefixSet.add(prefix)
      }
    }
  })

  // 检查一个值是否在 paramsOption 中存在（考虑层级关系）
  function checkValueExists(value) {
    // 直接存在
    if (optionSet.has(value)) {
      return true
    }
    // 如果是层级结构，检查所有父级前缀
    if (value.includes('-de-')) {
      const parts = value.split('-de-')

      // 检查所有可能的父级前缀
      for (let i = 1; i < parts.length; i++) {
        const prefix = parts.slice(0, i).join('-de-')
        if (optionSet.has(prefix)) {
          return true
        }
      }
    }

    // 检查该值是否是某个选项的父级
    // 如：paramsOption 中有 "香橙店-de-浓郁椰奶"，传入 "香橙店" 也应该匹配
    if (
      Array.from(optionSet).some(option => option.startsWith(value + '-de-') || option === value)
    ) {
      return true
    }
    // 检查该值是否是某个选项的前缀（通过 prefixSet）
    if (prefixSet.has(value)) {
      return true
    }
    return false
  }
  // 处理单值情况（字符串）
  if (typeof params === 'string') {
    return checkValueExists(params) ? params : null
  }
  // 处理数组情况
  if (Array.isArray(params)) {
    // 过滤出存在的值
    const filtered = params.filter(value => typeof value === 'string' && checkValueExists(value))
    // 如果过滤后为空，返回 null，否则返回过滤后的数组
    return filtered.length > 0 ? filtered : null
  }
  // 其他类型返回 null
  return null
}
