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

export const getRange = (selectValue, timeGranularity) => {
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

const getFieldId = (arr, result) => {
  const [obj] = result
  const idArr = obj.split(',')
  return arr
    .map(ele => ele.id)
    .slice(0, idArr.length)
    .join(',')
}

const getValueByDefaultValueCheckOrFirstLoad = (
  defaultValueCheck: boolean,
  defaultValue: any,
  selectValue: any,
  firstLoad: boolean,
  multiple: boolean,
  defaultMapValue: any,
  optionValueSource: number,
  mapValue: any,
  displayType: string,
  displayId: string
) => {
  if (+displayType === 9) {
    if (firstLoad) {
      return defaultValueCheck
        ? multiple
          ? defaultValue.map(ele => ele.split('-de-').join(','))
          : (defaultValue || '').split('-de-').join(',')
        : []
    }
    return selectValue?.length
      ? multiple
        ? selectValue.map(ele => ele.split('-de-').join(','))
        : (selectValue || '').split('-de-').join(',')
      : []
  }
  if (
    optionValueSource === 1 &&
    (defaultMapValue?.length || displayId) &&
    ![1, 7].includes(+displayType)
  ) {
    if (firstLoad) {
      return defaultValueCheck ? defaultMapValue : multiple ? [] : ''
    }
    return (selectValue?.length ? mapValue : selectValue) || ''
  }

  if (firstLoad && !selectValue?.length) {
    return defaultValueCheck ? defaultValue : multiple ? [] : ''
  }
  return selectValue ? selectValue : ''
}

export const useFilter = (curComponentId: string, firstLoad = false) => {
  const filter = []
  const queryComponentList = componentData.value.filter(ele => ele.component === 'VQuery')
  searchQuery(queryComponentList, filter, curComponentId, firstLoad)
  componentData.value.forEach(ele => {
    if (ele.component === 'Group') {
      const list = ele.propValue.filter(item => item.innerType === 'VQuery')
      searchQuery(list, filter, curComponentId, firstLoad)

      list.forEach(element => {
        if (element.innerType === 'DeTabs') {
          element.propValue.forEach(itx => {
            const elementArr = itx.componentData.filter(item => item.innerType === 'VQuery')
            searchQuery(elementArr, filter, curComponentId, firstLoad)
          })
        }
      })
    }

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

const getResult = (
  conditionType,
  defaultConditionValueF,
  defaultConditionValueS,
  conditionValueF,
  conditionValueS,
  firstLoad
) => {
  const valueF = firstLoad ? defaultConditionValueF : conditionValueF
  const valueS = firstLoad ? defaultConditionValueS : conditionValueS
  if (conditionType === 0) {
    return valueF === '' ? [] : valueF
  }
  return [valueF || '', valueS || ''].filter(ele => ele !== '')
}

const getOperator = (
  displayType,
  multiple,
  conditionType,
  defaultConditionValueOperatorF,
  defaultConditionValueF,
  defaultConditionValueOperatorS,
  defaultConditionValueS,
  conditionValueOperatorF,
  conditionValueF,
  conditionValueOperatorS,
  conditionValueS,
  firstLoad
) => {
  if (+displayType === 9) {
    return multiple ? 'in' : 'eq'
  }
  const valueF = firstLoad ? defaultConditionValueF : conditionValueF
  const valueS = firstLoad ? defaultConditionValueS : conditionValueS
  const operatorF = firstLoad ? defaultConditionValueOperatorF : conditionValueOperatorF
  const operatorS = firstLoad ? defaultConditionValueOperatorS : conditionValueOperatorS
  if (displayType === '8') {
    if (conditionType === 0) {
      return operatorF
    }
    const operatorArr = [valueF === '' ? '' : operatorF, valueS === '' ? '' : operatorS].filter(
      ele => ele !== ''
    )
    if (operatorArr.length === 2) {
      return operatorArr.join(`-${conditionType === 1 ? 'and' : 'or'}-`)
    }
    return valueF === '' ? operatorS : operatorF
  }

  return [1, 7].includes(+displayType) ? 'between' : multiple ? 'in' : 'eq'
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
            let selectValue
            const {
              selectValue: value,
              timeGranularityMultiple,
              conditionType = 0,
              treeFieldList = [],
              defaultConditionValueOperatorF = 'eq',
              defaultConditionValueF = '',
              defaultConditionValueOperatorS = 'like',
              defaultConditionValueS = '',
              conditionValueOperatorF = 'eq',
              conditionValueF = '',
              conditionValueOperatorS = 'like',
              conditionValueS = '',
              defaultValueCheck,
              timeType = 'fixed',
              defaultValue,
              optionValueSource,
              defaultMapValue,
              mapValue,
              parameters = [],
              timeGranularity = 'date',
              displayType,
              displayId,
              multiple
            } = item

            const isTree = +displayType === 9

            if (
              timeType === 'dynamic' &&
              [1, 7].includes(+displayType) &&
              firstLoad &&
              defaultValueCheck
            ) {
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
                selectValue = [startTime, endTime]
              }
            } else if (displayType === '8') {
              selectValue = getResult(
                conditionType,
                defaultConditionValueF,
                defaultConditionValueS,
                conditionValueF,
                conditionValueS,
                firstLoad
              )
            } else {
              selectValue = getValueByDefaultValueCheckOrFirstLoad(
                defaultValueCheck,
                defaultValue,
                value,
                firstLoad,
                multiple,
                defaultMapValue,
                optionValueSource,
                mapValue,
                displayType,
                displayId
              )
            }
            if (
              !!selectValue?.length ||
              ['[object Number]', '[object Date]'].includes(
                Object.prototype.toString.call(selectValue)
              ) ||
              displayType === '8'
            ) {
              const result = forMatterValue(
                +displayType,
                selectValue,
                timeGranularity,
                timeGranularityMultiple
              )
              const operator = getOperator(
                displayType,
                multiple,
                conditionType,
                defaultConditionValueOperatorF,
                defaultConditionValueF,
                defaultConditionValueOperatorS,
                defaultConditionValueS,
                conditionValueOperatorF,
                conditionValueF,
                conditionValueOperatorS,
                conditionValueS,
                firstLoad
              )
              if (result?.length) {
                filter.push({
                  componentId: ele.id,
                  fieldId: isTree
                    ? getFieldId(treeFieldList, result)
                    : item.checkedFieldsMap[curComponentId],
                  operator,
                  value: result,
                  parameters,
                  isTree
                })
              }
            }
          }
        })
    }
  })
}
