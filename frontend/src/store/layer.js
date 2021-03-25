import { swap } from '@/utils/utils'
import toast from '@/utils/toast'

export default {
    mutations: {
        upComponent({ componentData, curComponentIndex }) {
            // 上移图层 index，表示元素在数组中越往后
            if (curComponentIndex < componentData.length - 1) {
                swap(componentData, curComponentIndex, curComponentIndex + 1)
            } else {
                toast('已经到顶了')
            }
        },

        downComponent({ componentData, curComponentIndex }) {
            // 下移图层 index，表示元素在数组中越往前
            if (curComponentIndex > 0) {
                swap(componentData, curComponentIndex, curComponentIndex - 1)
            } else {
                toast('已经到底了')
            }
        },

        topComponent({ componentData, curComponentIndex }) {
            // 置顶
            if (curComponentIndex < componentData.length - 1) {
                swap(componentData, curComponentIndex, componentData.length - 1)
            } else {
                toast('已经到顶了')
            }
        },

        bottomComponent({ componentData, curComponentIndex }) {
            // 置底
            if (curComponentIndex > 0) {
                swap(componentData, curComponentIndex, 0)
            } else {
                toast('已经到底了')
            }
        },
    },
}