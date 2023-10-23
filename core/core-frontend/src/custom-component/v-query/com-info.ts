import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
interface DatasetField {
  type?: string
  innerType?: string
  title: string
  id: string
  tableId: string
}
const dvMainStore = dvMainStoreWithOut()
const { componentData, canvasViewInfo } = storeToRefs(dvMainStore)

export const comInfo = (queryElementId: string) => {
  const dfsComponentData = () => {
    const isMain = componentData.value.some(ele => ele.id === queryElementId)
    let arr = componentData.value.filter(com => !['VQuery', 'DeTabs'].includes(com.innerType))
    let tabArr = []
    componentData.value.forEach(ele => {
      if (ele.innerType === 'DeTabs') {
        ele.propValue.forEach(itx => {
          if (itx.componentData.some(item => item.id === queryElementId) && !isMain) {
            tabArr = itx.componentData.filter(com => !['VQuery', 'DeTabs'].includes(com.innerType))
          } else {
            arr = [
              ...arr,
              ...itx.componentData.filter(com => !['VQuery', 'DeTabs'].includes(com.innerType))
            ]
          }
        })
      }
    })
    return isMain ? arr : tabArr
  }

  const datasetFieldList = computed(() => {
    return dfsComponentData()
      .map(ele => {
        const obj = canvasViewInfo.value[ele.id]
        if (!obj) return null
        const { id, title, tableId, type } = obj as DatasetField
        return !!id && !!tableId
          ? {
              id,
              type,
              title,
              tableId
            }
          : null
      })
      .filter(ele => !!ele)
  })
  return {
    datasetFieldList
  }
}
