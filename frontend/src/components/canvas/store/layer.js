import { findCurComponentIndex, moveDown, moveUp, toBottom, toTop } from '@/components/canvas/utils/utils'
import toast from '@/components/canvas/utils/toast'

export default {
  mutations: {

    upComponent({ componentData, curComponent }) {
      if (curComponent) {
        const curComponentIndex = findCurComponentIndex(componentData, curComponent)
        // 上移图层 index，表示元素在数组中越往后
        if (curComponentIndex < componentData.length - 1) {
          moveUp(componentData, curComponentIndex)
        } else {
          toast('已经到顶了')
        }
      }
    },

    downComponent({ componentData, curComponent }) {
      if (curComponent) {
        const curComponentIndex = findCurComponentIndex(componentData, curComponent)
        // 下移图层 index，表示元素在数组中越往前
        if (curComponentIndex > 0) {
          moveDown(componentData, curComponentIndex)
        } else {
          toast('已经到底了')
        }
      }
    },

    topComponent({ componentData, curComponent }) {
      if (curComponent) {
        const curComponentIndex = findCurComponentIndex(componentData, curComponent)
        // 置顶
        if (curComponentIndex < componentData.length - 1) {
          toTop(componentData, curComponentIndex)
        }
      }
    },

    bottomComponent({ componentData, curComponent }) {
      if (curComponent) {
        const curComponentIndex = findCurComponentIndex(componentData, curComponent)
        // 置底
        if (curComponentIndex > 0) {
          toBottom(componentData, curComponentIndex)
        } else {
          toast('已经到底了')
        }
      }
    }
  }
}
