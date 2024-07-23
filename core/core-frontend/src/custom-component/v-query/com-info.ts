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

export const comInfo = () => {
  const dfsComponentData = () => {
    let arr = componentData.value.filter(
      com => !['VQuery', 'DeTabs'].includes(com.innerType) && com.component !== 'Group'
    )
    componentData.value.forEach(ele => {
      if (ele.innerType === 'DeTabs') {
        ele.propValue.forEach(itx => {
          arr = [
            ...arr,
            ...itx.componentData.filter(
              com => !['VQuery', 'DeTabs'].includes(com.innerType) && com.component !== 'Group'
            )
          ]
        })
      } else if (ele.component === 'Group') {
        arr = [
          ...arr,
          ele.propValue.filter(
            com => !['VQuery', 'DeTabs'].includes(com.innerType) && com.component !== 'Group'
          )
        ]
        ele.propValue.forEach(element => {
          if (element.innerType === 'DeTabs') {
            element.propValue.forEach(itx => {
              arr = [
                ...arr,
                ...itx.componentData.filter(
                  com => !['VQuery', 'DeTabs'].includes(com.innerType) && com.component !== 'Group'
                )
              ]
            })
          }
        })
      }
    })

    return arr.flat()
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
