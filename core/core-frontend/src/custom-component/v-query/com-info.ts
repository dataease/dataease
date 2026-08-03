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
const { componentData, componentDataMultiply, canvasViewInfo, canvasViewInfoMultiply } =
  storeToRefs(dvMainStore)

export const comInfo = showPosition => {
  const componentDataCustom =
    showPosition === 'multiplexing' ? componentDataMultiply.value : componentData.value
  const canvasViewInfoCustom =
    showPosition === 'multiplexing' ? canvasViewInfoMultiply.value : canvasViewInfo.value
  const dfsComponentData = () => {
    let arr = componentDataCustom.filter(
      com => !['VQuery', 'DeTabs'].includes(com.innerType) && com.component !== 'Group'
    )
    componentDataCustom.forEach(ele => {
      if (ele.innerType === 'DeTabs') {
        ele.propValue.forEach(itx => {
          arr = [
            ...arr,
            ...itx.componentData.filter(
              com => !['VQuery', 'DeTabs'].includes(com.innerType) && com.component !== 'Group'
            )
          ]

          itx.componentData.forEach(element => {
            if (element.component === 'Group') {
              arr = [
                ...arr,
                element.propValue.filter(
                  coms =>
                    !['VQuery', 'DeTabs'].includes(coms.innerType) && coms.component !== 'Group'
                )
              ]
            }
          })
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
        const obj = canvasViewInfoCustom[ele.id]
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
