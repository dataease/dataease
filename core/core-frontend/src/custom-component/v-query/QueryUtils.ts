import { cloneDeep } from 'lodash-es'
import { useEmitt } from '@/hooks/web/useEmitt'
const { emitter } = useEmitt()

export const reRenderAll = (oldArr, newArr) => {
  const newArrIds = newArr.map(ele => ele.id)
  const emitterList = (oldArr || []).reduce((pre, next) => {
    if (newArrIds.includes(next.id)) return pre
    const keyList = getKeyList(next)
    pre = [...new Set([...keyList, ...pre])]
    return pre
  }, [])
  if (!emitterList.length) return
  emitterList.forEach(ele => {
    emitter.emit(`query-data-${ele}`)
  })
}

export const reRenderAfterDelete = oldArr => {
  const emitterList = (oldArr || []).reduce((pre, next) => {
    const keyList = getKeyList(next)
    pre = [...new Set([...keyList, ...pre])]
    return pre
  }, [])
  if (!emitterList.length) return
  emitterList.forEach(ele => {
    emitter.emit(`query-data-${ele}`)
  })
}

export const getKeyList = next => {
  let checkedFieldsMapArr = Object.entries(next.checkedFieldsMap).filter(ele =>
    next.checkedFields.includes(ele[0])
  )
  if (next.displayType === '9') {
    checkedFieldsMapArr = (
      next.treeCheckedList?.length
        ? next.treeCheckedList.filter((_, index) => index < next.treeFieldList.length)
        : next.treeFieldList.map(() => {
            return {
              checkedFields: [...next.checkedFields],
              checkedFieldsMap: cloneDeep(next.checkedFieldsMap)
            }
          })
    )
      .map(item =>
        Object.entries(item.checkedFieldsMap).filter(ele => item.checkedFields.includes(ele[0]))
      )
      .flat()
  }
  return checkedFieldsMapArr.filter(ele => !!ele[1]).map(ele => ele[0])
}
