import { defineStore, storeToRefs } from 'pinia'
import toast from '@/utils/toast'
import { dvMainStoreWithOut } from './dvMain'
import { generateID } from '@/utils/generateID'
import { deepCopy } from '@/utils/utils'
import { store } from '../../index'

const dvMainStore = dvMainStoreWithOut()
const { curComponent, curComponentIndex } = storeToRefs(dvMainStore)

export const copyStore = defineStore('copy', {
  state: () => {
    return {
      copyData: null, // 复制粘贴剪切
      isCut: false
    }
  },
  actions: {
    copy() {
      if (!curComponent) {
        toast('请选择组件')
        return
      }

      // 如果有剪切的数据，需要先还原
      restorePreCutData()
      copyData()
      this.isCut = false
    },

    paste(isMouse) {
      if (!this.copyData) {
        toast('请选择组件')
        return
      }

      const data = this.copyData.data

      if (isMouse) {
        data.style.top = this.menuTop
        data.style.left = this.menuLeft
      } else {
        data.style.top += 10
        data.style.left += 10
      }
      dvMainStore.addComponent({ component: deepCopyHelper(data), index: undefined })
      if (this.isCut) {
        this.copyData = null
      }
    },

    cut() {
      if (!curComponent) {
        toast('请选择组件')
        return
      }

      // 如果重复剪切，需要恢复上一次剪切的数据
      restorePreCutData()
      copyData()

      dvMainStore.deleteComponent()
      this.isCut = true
    }
  }
})

// 恢复上一次剪切的数据
function restorePreCutData() {
  if (this.isCut && this.copyData) {
    const data = deepCopy(this.copyData.data)
    const index = this.copyData.index
    dvMainStore.addComponent({ component: data, index })
    if (curComponentIndex.value >= index) {
      // 如果当前组件索引大于等于插入索引，需要加一，因为当前组件往后移了一位
      curComponentIndex.value++
    }
  }
}

function copyData() {
  this.copyData = {
    data: deepCopy(curComponent),
    index: curComponentIndex
  }
}

function deepCopyHelper(data) {
  const result = deepCopy(data)
  result.id = generateID()
  if (result.component === 'Group') {
    result.propValue.forEach((component, i) => {
      result.propValue[i] = deepCopyHelper(component)
    })
  }

  return result
}

export const copyStoreWithOut = () => {
  return copyStore(store)
}
