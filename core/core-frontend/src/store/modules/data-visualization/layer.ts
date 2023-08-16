import { defineStore, storeToRefs } from 'pinia'
import { store } from '../../index'
import { dvMainStoreWithOut } from './dvMain'
import { swap } from '@/utils/utils'

const dvMainStore = dvMainStoreWithOut()
const { componentData, curComponentIndex, curComponent } = storeToRefs(dvMainStore)

export const layerStore = defineStore('layer', {
  actions: {
    upComponent() {
      // 上移图层 index，表示元素在数组中越往后
      if (curComponentIndex.value < componentData.value.length - 1) {
        swap(componentData.value, curComponentIndex.value, curComponentIndex.value + 1)
        curComponentIndex.value = curComponentIndex.value + 1
      }
    },

    downComponent() {
      // 下移图层 index，表示元素在数组中越往前
      if (curComponentIndex.value > 0) {
        swap(componentData.value, curComponentIndex.value, curComponentIndex.value - 1)
        curComponentIndex.value = curComponentIndex.value - 1
      }
    },

    topComponent() {
      // 置顶
      if (curComponentIndex.value < componentData.value.length - 1) {
        componentData.value.splice(curComponentIndex.value, 1)
        componentData.value.push(curComponent.value)
        curComponentIndex.value = componentData.value.length - 1
      }
    },

    bottomComponent() {
      // 置底
      if (curComponentIndex.value > 0) {
        componentData.value.splice(curComponentIndex.value, 1)
        componentData.value.unshift(curComponent.value)
        curComponentIndex.value = 0
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
      }
    }
  }
})

export const layerStoreWithOut = () => {
  return layerStore(store)
}
