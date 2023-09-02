import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'

const dvMainStore = dvMainStoreWithOut()
const { componentData } = storeToRefs(dvMainStore)

const forMatterValue = (type: number, selectValue: any) => {
  if (type !== 1) {
    return Array.isArray(selectValue) ? selectValue : [selectValue]
  }
  return Array.isArray(selectValue)
    ? selectValue.map(ele => +new Date(ele))
    : [getDayStart(+new Date(selectValue)), +new Date(selectValue)]
}

const getDayStart = timestamp => {
  const time = new Date(timestamp)
  return (
    timestamp -
    time.getHours() * 60 * 60 * 1000 -
    time.getMinutes() * 60 * 1000 -
    time.getSeconds() * 1000 -
    time.getMilliseconds()
  )
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
              field,
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
              !!selectValue.length ||
              Object.prototype.toString.call(selectValue) === '[object Date]'
            ) {
              filter.push({
                componentId: ele.id,
                fieldId: item.checkedFieldsMap[curComponentId],
                operator,
                value: forMatterValue(field.deType, selectValue),
                parameters: parametersCheck ? parameters : [],
                isTree
              })
            }
          }
        })
    }
  })
}
