import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { getDynamicRange, getCustomTime } from '@/custom-component/v-query/time-format'
const dvMainStore = dvMainStoreWithOut()
const { componentData } = storeToRefs(dvMainStore)

const getDynamicRangeTime = (type: number, selectValue: any, timeGranularityMultiple: string) => {
  const timeType = (timeGranularityMultiple || '').split('range')[0]

  if (timeGranularityMultiple === 'datetimerange' || type === 1 || !timeType) {
    return selectValue.map(ele => +new Date(ele))
  }

  const [start, end] = selectValue

  return [
    +new Date(start),
    +getCustomTime(
      1,
      timeType,
      timeType,
      'b',
      null,
      timeGranularityMultiple,
      'start-config',
      new Date(end)
    ) - 1000
  ]
}

const forMatterValue = (
  type: number,
  selectValue: any,
  timeGranularity: string,
  timeGranularityMultiple: string
) => {
  if (![1, 7].includes(type)) {
    return Array.isArray(selectValue) ? selectValue : [selectValue]
  }
  return Array.isArray(selectValue)
    ? getDynamicRangeTime(type, selectValue, timeGranularityMultiple)
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
  if (firstLoad && !selectValue?.length) {
    return defaultValueCheck ? defaultValue : multiple ? [] : ''
  }
  return selectValue || ''
}

export const useFilter = (curComponentId: string, firstLoad = false) => {
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
              selectValue: value,
              timeGranularityMultiple,
              parametersStart,
              parametersEnd,
              defaultValueCheck,
              timeType = 'fixed',
              defaultValue,
              parameters = [],
              parametersCheck = false,
              isTree = false,
              timeGranularity = 'date',
              displayType,
              multiple
            } = item

            if (timeType === 'dynamic' && [1, 7].includes(+displayType) && firstLoad) {
              if (+displayType === 1) {
                selectValue = getDynamicRange(item)
                item.defaultValue = new Date(selectValue[0])
                item.selectValue = new Date(selectValue[0])
              } else {
                const {
                  timeNum,
                  relativeToCurrentType,
                  around,
                  arbitraryTime,
                  timeGranularity,
                  timeNumRange,
                  relativeToCurrentTypeRange,
                  aroundRange,
                  timeGranularityMultiple,
                  arbitraryTimeRange
                } = item

                const startTime = getCustomTime(
                  timeNum,
                  relativeToCurrentType,
                  timeGranularity,
                  around,
                  arbitraryTime,
                  timeGranularityMultiple,
                  'start-panel'
                )
                const endTime = getCustomTime(
                  timeNumRange,
                  relativeToCurrentTypeRange,
                  timeGranularity,
                  aroundRange,
                  arbitraryTimeRange,
                  timeGranularityMultiple,
                  'end-panel'
                )
                item.defaultValue = [startTime, endTime]
                item.selectValue = [startTime, endTime]
              }
            } else {
              selectValue = getValueByDefaultValueCheckOrFirstLoad(
                defaultValueCheck,
                defaultValue,
                value,
                firstLoad,
                multiple
              )
            }
            if (
              !!selectValue?.length ||
              Object.prototype.toString.call(selectValue) === '[object Date]'
            ) {
              const values = forMatterValue(
                +displayType,
                selectValue,
                timeGranularity,
                timeGranularityMultiple
              )
              filter.push({
                componentId: ele.id,
                fieldId: item.checkedFieldsMap[curComponentId],
                operator: [1, 7].includes(+displayType) ? 'between' : multiple ? 'in' : 'eq',
                value: values,
                parameters: parametersCheck
                  ? +displayType === 7
                    ? [
                        parametersStart,
                        parametersEnd?.id
                          ? { ...parametersEnd, id: `${parametersEnd.id}_START_END_SPLIT` }
                          : parametersEnd
                      ]
                    : parameters
                  : [],
                isTree
              })
            }
          }
        })
    }
  })
}
