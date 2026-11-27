import { rememberQueryDefault } from '@/utils/queryDefaultSnapshot'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import type { ManipulateType } from 'dayjs'
import { storeToRefs } from 'pinia'
import dayjs from 'dayjs'
import { getDynamicRange, getCustomTime } from '@/custom-component/v-query/time-format'
import { getCustomRange } from '@/custom-component/v-query/time-format-dayjs'
const dvMainStore = dvMainStoreWithOut()
const { componentData, canvasStyleData, componentDataMultiply } = storeToRefs(dvMainStore)

const getDynamicRangeTime = (type: number, selectValue: any, timeGranularityMultiple: string) => {
  const timeType = (timeGranularityMultiple || '').split('range')[0]

  if ('datetimerange' === timeGranularityMultiple || type === 1 || !timeType) {
    return selectValue.map(ele => +new Date(ele))
  }

  if (timeGranularityMultiple.includes('range') && type === 7) {
    return [
      +new Date(
        dayjs(selectValue[0])
          .startOf(timeType as 'month' | 'year' | 'date')
          .format('YYYY/MM/DD HH:mm:ss')
      ),
      +new Date(
        dayjs(selectValue[1])
          .endOf(timeType as 'month' | 'year' | 'date')
          .format('YYYY/MM/DD HH:mm:ss')
      )
    ]
  }

  const [start] = selectValue

  return [
    +new Date(start),
    +getCustomTime(
      1,
      timeType as ManipulateType,
      timeType,
      'b',
      null,
      timeGranularityMultiple,
      'start-config'
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
  return [
    +new Date(dayjs(timestamp).startOf('year').format('YYYY/MM/DD HH:mm:ss')),
    +new Date(dayjs(timestamp).endOf('year').format('YYYY/MM/DD HH:mm:ss'))
  ]
}

const getMonthEnd = timestamp => {
  return [
    +new Date(dayjs(timestamp).startOf('month').format('YYYY/MM/DD HH:mm:ss')),
    +new Date(dayjs(timestamp).endOf('month').format('YYYY/MM/DD HH:mm:ss'))
  ]
}

const getDayEnd = timestamp => {
  return [
    +new Date(dayjs(timestamp).startOf('day').format('YYYY/MM/DD HH:mm:ss')),
    +new Date(dayjs(timestamp).endOf('day').format('YYYY/MM/DD HH:mm:ss'))
  ]
}

const getTreePaths = (value: unknown) =>
  (Array.isArray(value) ? value : [value])
    .filter((path): path is string => typeof path === 'string' && !!path)
    .map(path => path.split('-de-'))

const appendTreeFilters = (
  filter,
  paths: string[][],
  treeFieldIds: (string | undefined)[],
  source: { filterId: string; componentId: string; filterFrom?: 'optionFilter' }
) => {
  // 各条路径只匹配自己包含的层级，同一规则下的匹配组取并集。
  const pathsByFields = new Map<string, Map<string, string[]>>()
  for (const path of paths) {
    const fields: string[] = []
    const values: string[] = []
    treeFieldIds.forEach((fieldId, index) => {
      if (fieldId && path[index]) {
        fields.push(fieldId)
        values.push(path[index])
      }
    })
    if (!fields.length) {
      continue
    }

    const fieldId = fields.join(',')
    if (!pathsByFields.has(fieldId)) {
      pathsByFields.set(fieldId, new Map())
    }
    pathsByFields.get(fieldId).set(JSON.stringify(values), values)
  }

  const treeFilterGroup =
    pathsByFields.size > 1
      ? `${source.componentId}:${source.filterId}:${source.filterFrom || 'selection'}`
      : undefined
  pathsByFields.forEach((paths, fieldId) => {
    filter.push({
      ...source,
      fieldId,
      operator: 'in',
      treePaths: [...paths.values()],
      parameters: [],
      isTree: true,
      treeFilterGroup
    })
  })
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

  if (firstLoad) {
    return defaultValueCheck ? defaultValue : multiple ? [] : ''
  }
  return selectValue ? selectValue : multiple ? [] : ''
}

export const useFilter = (curComponentId: string, firstLoad = false, showPosition: any) => {
  // 弹窗区域过滤组件是否生效
  const popupAvailable = canvasStyleData.value.popupAvailable
  const filter = []
  const treeSelections: Record<string, string[][]> = {}
  const componentDataCustom =
    showPosition === 'multiplexing' ? componentDataMultiply.value : componentData.value
  const queryComponentList = componentDataCustom.filter(
    ele =>
      ele.component === 'VQuery' &&
      (popupAvailable || (!popupAvailable && ele.category !== 'hidden'))
  )
  searchQuery(queryComponentList, filter, curComponentId, firstLoad, treeSelections)
  componentDataCustom.forEach(ele => {
    if (ele.component === 'Group') {
      const list = ele.propValue.filter(
        item =>
          item.innerType === 'VQuery' &&
          (popupAvailable || (!popupAvailable && ele.category !== 'hidden'))
      )
      searchQuery(list, filter, curComponentId, firstLoad, treeSelections)

      list.forEach(element => {
        if (element.innerType === 'DeTabs') {
          element.propValue.forEach(itx => {
            const elementArr = itx.componentData.filter(
              item =>
                item.innerType === 'VQuery' &&
                (popupAvailable || (!popupAvailable && ele.category !== 'hidden'))
            )
            searchQuery(elementArr, filter, curComponentId, firstLoad, treeSelections)
          })
        }
      })

      ele.propValue.forEach(element => {
        if (element.innerType === 'DeTabs') {
          element.propValue.forEach(itx => {
            const elementArr = itx.componentData.filter(
              item =>
                item.innerType === 'VQuery' &&
                (popupAvailable || (!popupAvailable && ele.category !== 'hidden'))
            )
            searchQuery(elementArr, filter, curComponentId, firstLoad, treeSelections)
          })
        }
      })
    }

    if (ele.innerType === 'DeTabs') {
      ele.propValue.forEach(itx => {
        itx.componentData.forEach(v => {
          if (v.component === 'Group') {
            const listGroup = v.propValue.filter(
              item =>
                item.innerType === 'VQuery' &&
                (popupAvailable || (!popupAvailable && v.category !== 'hidden'))
            )
            searchQuery(listGroup, filter, curComponentId, firstLoad, treeSelections)
          }
        })

        const arr = itx.componentData.filter(item => item.innerType === 'VQuery')
        searchQuery(arr, filter, curComponentId, firstLoad, treeSelections)
      })
    }
  })
  return {
    filter,
    treeSelections
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

const getResultNum = (
  defaultNumValueEnd,
  numValueEnd,
  numValueStart,
  defaultNumValueStart,
  defaultValueCheck,
  firstLoad
) => {
  if (firstLoad && !defaultValueCheck) {
    return []
  }
  const valueS = firstLoad ? defaultNumValueStart : numValueStart
  const valueE = firstLoad ? defaultNumValueEnd : numValueEnd
  return [valueS ?? '', valueE ?? ''].filter(ele => ele !== '')
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
  if (+displayType === 22) {
    return 'between'
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

  return [1, 7].includes(+displayType) ? 'between' : 'in'
}

const duplicateRemoval = arr => {
  const objList = []
  let idList = arr.map(ele => ele.id)
  for (let index = 0; index < arr.length; index++) {
    const element = arr[index]
    if (idList.includes(element.id)) {
      objList.push(element)
      idList = idList.filter(ele => ele !== element.id)
    }
  }
  return objList
}

export const searchQuery = (
  queryComponentList,
  filter,
  curComponentId,
  firstLoad,
  treeSelections: Record<string, string[][]> = {}
) => {
  queryComponentList.forEach(ele => {
    if (!!ele.propValue?.length) {
      ele.propValue.forEach(item => {
        if (+item.displayType === 9) {
          const hasLevelMappings = !!item.treeCheckedList?.length
          if (
            !hasLevelMappings &&
            (!item.checkedFields?.includes(curComponentId) ||
              !item.checkedFieldsMap?.[curComponentId])
          ) {
            return
          }
          const treeFieldIds = (item.treeFieldList || []).map((field, index) => {
            if (!hasLevelMappings) {
              return field?.id
            }
            const level = item.treeCheckedList[index]
            return level?.checkedFields?.includes(curComponentId)
              ? level.checkedFieldsMap?.[curComponentId]
              : undefined
          })
          if (!treeFieldIds.some(Boolean)) {
            return
          }
          const source = { filterId: item.id, componentId: ele.id }
          appendTreeFilters(filter, getTreePaths(item.optionFilter), treeFieldIds, {
            ...source,
            filterFrom: 'optionFilter'
          })
          const selected = firstLoad
            ? item.defaultValueCheck
              ? item.defaultValue
              : []
            : item.selectValue
          if (dvMainStore.canvasAttachInfo?.taskId) {
            rememberQueryDefault(item, selected, firstLoad)
          }
          const selectedPaths = getTreePaths(selected)
          if (selectedPaths.length) {
            treeSelections[item.id] = selectedPaths
          }
          appendTreeFilters(filter, selectedPaths, treeFieldIds, source)
          return
        }
        if (item.checkedFields.includes(curComponentId) && item.checkedFieldsMap[curComponentId]) {
          let selectValue
          const {
            id,
            selectValue: value,
            timeGranularityMultiple,
            defaultNumValueEnd,
            numValueEnd,
            numValueStart,
            defaultNumValueStart,
            conditionType = 0,
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
            multiple,
            optionFilter
          } = item

          if (optionFilter) {
            filter.push({
              filterId: id,
              filterFrom: 'optionFilter',
              componentId: ele.id,
              fieldId: item.checkedFieldsMap[curComponentId],
              operator: 'in',
              value: optionFilter,
              parameters: [],
              isTree: false
            })
          }

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
                relativeToCurrentRange,
                arbitraryTime,
                timeGranularity,
                timeNumRange,
                relativeToCurrentTypeRange,
                aroundRange,
                timeGranularityMultiple,
                arbitraryTimeRange
              } = item

              let startTime = getCustomTime(
                timeNum,
                relativeToCurrentType,
                timeGranularity,
                around,
                arbitraryTime,
                timeGranularityMultiple,
                'start-panel'
              )
              let endTime = getCustomTime(
                timeNumRange,
                relativeToCurrentTypeRange,
                timeGranularity,
                aroundRange,
                arbitraryTimeRange,
                timeGranularityMultiple,
                'end-panel'
              )

              if (!!relativeToCurrentRange && relativeToCurrentRange !== 'custom') {
                ;[startTime, endTime] = getCustomRange(relativeToCurrentRange)
              }
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
          } else if (displayType === '22') {
            selectValue = getResultNum(
              defaultNumValueEnd,
              numValueEnd,
              numValueStart,
              defaultNumValueStart,
              defaultValueCheck,
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
          if (dvMainStore.canvasAttachInfo?.taskId) {
            rememberQueryDefault(item, selectValue, firstLoad)
          }
          if (
            !!selectValue?.length ||
            ['[object Number]', '[object Date]'].includes(
              Object.prototype.toString.call(selectValue)
            ) ||
            displayType === '8'
          ) {
            let result = forMatterValue(
              +displayType,
              selectValue,
              timeGranularity,
              timeGranularityMultiple
            )
            let operator = getOperator(
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
              const fieldId = item.checkedFieldsMap[curComponentId]
              let parametersFilter = duplicateRemoval(
                parameters.reduce((pre, next) => {
                  if (next.id === fieldId && !pre.length) {
                    pre.push(next)
                  }
                  return pre
                }, [])
              )

              if (item.checkedFieldsMapArr?.[curComponentId]?.length) {
                const endTimeFieldId = item.checkedFieldsMapArr?.[curComponentId].find(
                  element => element !== fieldId
                )
                const resultEnd = Array(2).fill(
                  endTimeFieldId === item.checkedFieldsMapEnd[curComponentId]
                    ? result[1]
                    : result[0]
                )
                result = Array(2).fill(
                  endTimeFieldId === item.checkedFieldsMapEnd[curComponentId]
                    ? result[0]
                    : result[1]
                )
                parametersFilter = duplicateRemoval(
                  item.parametersArr[curComponentId].filter(e => e.id === fieldId)
                )

                const parametersFilterEnd = duplicateRemoval(
                  item.parametersArr[curComponentId].filter(e => e.id === endTimeFieldId)
                )
                if (endTimeFieldId.includes('|DE|')) {
                  operator = multiple ? 'in' : 'eq'
                }
                filter.push({
                  filterId: id,
                  componentId: ele.id,
                  fieldId: endTimeFieldId,
                  arrayType: 'END',
                  operator,
                  value: resultEnd,
                  parameters: parametersFilterEnd,
                  isTree: false
                })
              }

              if (item.checkedFieldsMapArrNum?.[curComponentId]?.length) {
                const endTimeFieldId = item.checkedFieldsMapArrNum?.[curComponentId].find(
                  element => element !== fieldId
                )
                const resultEnd = Array(2).fill(
                  endTimeFieldId === item.checkedFieldsMapEndNum[curComponentId]
                    ? result[1]
                    : result[0]
                )
                result = Array(2).fill(
                  endTimeFieldId === item.checkedFieldsMapEndNum[curComponentId]
                    ? result[0]
                    : result[1]
                )
                parametersFilter = duplicateRemoval(
                  item.parametersArr[curComponentId].filter(e => e.id === fieldId)
                )

                const parametersFilterEnd = duplicateRemoval(
                  item.parametersArr[curComponentId].filter(e => e.id === endTimeFieldId)
                )
                if (endTimeFieldId.includes('|DE|')) {
                  operator = multiple ? 'in' : 'eq'
                }
                filter.push({
                  filterId: id,
                  componentId: ele.id,
                  fieldId: endTimeFieldId,
                  arrayType: 'END',
                  operator,
                  value: resultEnd,
                  parameters: parametersFilterEnd,
                  isTree: false
                })
              }

              if (fieldId.includes('|DE|')) {
                operator = multiple ? 'in' : 'eq'
              }
              filter.push({
                filterId: id,
                componentId: ele.id,
                fieldId,
                operator,
                value: result,
                parameters: parametersFilter,
                isTree: false
              })
            }
          }
        }
      })
    }
  })
}
