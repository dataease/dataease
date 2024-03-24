import { defineStore, storeToRefs } from 'pinia'
import { store } from '../../index'
import { dvMainStoreWithOut } from './dvMain'
import { swap } from '@/utils/utils'
import { useEmitt } from '@/hooks/web/useEmitt'
import { getCurInfo } from '@/store/modules/data-visualization/common'

const dvMainStore = dvMainStoreWithOut()
const { curComponentIndex, curComponent } = storeToRefs(dvMainStore)

export const layerStore = defineStore('layer', {
  actions: {
    upComponent() {
      const curInfo = getCurInfo()
      if (curInfo) {
        const { index, componentData } = curInfo
        // 上移图层 index，表示元素在数组中越往后
        if (index < componentData.length - 1) {
          swap(componentData, index, index + 1)
          curComponentIndex.value = index + 1
        }
      }
    },

    downComponent() {
      const curInfo = getCurInfo()
      if (curInfo) {
        const { index, componentData } = curInfo
        // 下移图层 index，表示元素在数组中越往前
        if (index > 0) {
          swap(componentData, index, index - 1)
          curComponentIndex.value = index - 1
        }
      }
    },

    topComponent() {
      // 置顶
      const curInfo = getCurInfo()
      if (curInfo) {
        const { index, componentData } = curInfo
        if (index < componentData.length - 1) {
          componentData.splice(curComponentIndex.value, 1)
          componentData.push(curComponent.value)
          curComponentIndex.value = componentData.length - 1
        }
      }
    },

    bottomComponent() {
      // 置底
      const curInfo = getCurInfo()
      if (curInfo) {
        const { index, componentData } = curInfo
        if (index > 0) {
          componentData.splice(index, 1)
          componentData.unshift(curComponent.value)
          curComponentIndex.value = 0
        }
      }
    },

    hideComponent() {
      // 隐藏
      if (curComponent && curComponent.value) {
        curComponent.value.isShow = false
      }
    },
    showComponent() {
      // 显示
      if (curComponent && curComponent.value) {
        curComponent.value.isShow = true
        if (curComponent.value.component == 'Group') {
          curComponent.value.propValue.forEach(item => {
            if (item.innerType?.indexOf('table') !== -1) {
              setTimeout(() => {
                useEmitt().emitter.emit('renderChart-' + item.id)
              }, 400)
            }
          })
        } else if (curComponent.value?.innerType?.indexOf('table') !== -1) {
          setTimeout(() => {
            useEmitt().emitter.emit('renderChart-' + curComponent.value.id)
          }, 400)
        }
      }
    }
  }
})

export const layerStoreWithOut = () => {
  return layerStore(store)
}
