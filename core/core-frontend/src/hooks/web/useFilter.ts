import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'

const dvMainStore = dvMainStoreWithOut()
const { componentData } = storeToRefs(dvMainStore)

export const useFilter = (curComponentId: number) => {
  const filter = []
  const queryComponentList = componentData.value.filter(ele => ele.component === 'VQuery')
  queryComponentList.forEach(ele => {
    if (!!ele.propValue?.length) {
      ele.propValue.forEach(item => {
        if (item.checkedFieldsMap[curComponentId]) {
          const { operator = 'eq', selectValue = '', parameters = [], isTree = false } = item
          if (!!selectValue.length) {
            filter.push({
              componentId: ele.id,
              fieldId: item.checkedFieldsMap[curComponentId],
              operator,
              value: Array.isArray(selectValue) ? selectValue : [selectValue],
              parameters,
              isTree
            })
          }
        }
      })
    }
  })
  return {
    filter
  }
}
