import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'

const dvMainStore = dvMainStoreWithOut()
const { componentData } = storeToRefs(dvMainStore)

const forMatterValue = (type: number, selectValue: any, timeGranularity: string) => {
  if (![1, 7].includes(type)) {
    return Array.isArray(selectValue) ? selectValue : [selectValue]
  }
  return Array.isArray(selectValue)
    ? selectValue.map(ele => +new Date(ele))
    : getRange(selectValue, timeGranularity)
}

const getRange = (selectValue, timeGranularity) => {
  switch (timeGranularity) {
    case 'year':
      return getYearEnd(selectValue)
    case 'month':
      return getMonthEnd(selectValue)
    case 'date':
      return getDayEnd(selectValue)
    case 'datetime':
      return [+new Date(selectValue), +new Date(selectValue)]
    default:
      break
  }
}
const getYearEnd = timestamp => {
  const time = new Date(timestamp)
  return [
    +new Date(time.getFullYear(), 0, 1),
    +new Date(time.getFullYear(), 11, 31) + 60 * 1000 * 60 * 24 - 1000
  ]
}

const getMonthEnd = timestamp => {
  const time = new Date(timestamp)
  const date = new Date(time.getFullYear(), time.getMonth(), 1)
  date.setDate(1)
  date.setMonth(date.getMonth() + 1)
  return [+new Date(time.getFullYear(), time.getMonth(), 1), +new Date(date.getTime() - 1000)]
}

const getDayEnd = timestamp => {
  return [+new Date(timestamp), +new Date(timestamp) + 60 * 1000 * 60 * 24 - 1000]
}

const getValueByDefaultValueCheckOrFirstLoad = (
  defaultValueCheck: boolean,
  defaultValue: any,
  selectValue: any,
  firstLoad: boolean,
  multiple: boolean
) => {
  if (firstLoad) {
    return defaultValueCheck ? defaultValue : multiple ? [] : ''
  }
  return selectValue || ''
}

export const useFilter = (curComponentId: number, firstLoad = false) => {
  const filter = []
  const queryComponentList = componentData.value.filter(ele => ele.component === 'VQuery')
  searchQuery(queryComponentList, filter, curComponentId, firstLoad)
  componentData.value.forEach(ele => {
    if (ele.innerType === 'DeTabs') {
      ele.propValue.forEach(itx => {
        const arr = itx.componentData.filter(item => item.innerType === 'VQuery')
        searchQuery(arr, filter, curComponentId, firstLoad)
      })
    }
  })
  return {
    filter
  }
}

export const searchQuery = (queryComponentList, filter, curComponentId, firstLoad) => {
  queryComponentList.forEach(ele => {
    if (!!ele.propValue?.length) {
      ele.propValue
        .filter(itx => itx.visible)
        .forEach(item => {
          if (
            item.checkedFields.includes(curComponentId) &&
            item.checkedFieldsMap[curComponentId]
          ) {
            let selectValue = ''
            const {
              operator = 'eq',
              selectValue: value,
              defaultValueCheck,
              defaultValue,
              parameters = [],
              parametersCheck = false,
              isTree = false,
              timeGranularity = 'date',
              displayType,
              multiple
            } = item
            selectValue = getValueByDefaultValueCheckOrFirstLoad(
              defaultValueCheck,
              defaultValue,
              value,
              firstLoad,
              multiple
            )
            if (
              !!selectValue?.length ||
              Object.prototype.toString.call(selectValue) === '[object Date]'
            ) {
              filter.push({
                componentId: ele.id,
                fieldId: item.checkedFieldsMap[curComponentId],
                operator: [1, 7].includes(+displayType) ? 'between' : operator,
                value: forMatterValue(+displayType, selectValue, timeGranularity),
                parameters: parametersCheck ? parameters : [],
                isTree
              })
            }
          }
        })
    }
  })
}
