import { defineStore, storeToRefs } from 'pinia'
import { store } from '../../index'
import { dvMainStoreWithOut } from './dvMain'
import { deepCopy } from '@/utils/utils'

const dvMainStore = dvMainStoreWithOut()
const { curComponent, componentData } = storeToRefs(dvMainStore)

let defaultcomponentData = []

export const snapshotStore = defineStore('snapshot', {
  state: () => {
    return {
      snapshotData: [], // 编辑器快照数据
      snapshotIndex: -1 // 快照索引
    }
  },
  actions: {
    undo() {
      if (this.snapshotIndex >= 0) {
        this.snapshotIndex--
        const componentData =
          deepCopy(this.snapshotData[this.snapshotIndex]) || getDefaultcomponentData()
        if (curComponent) {
          // 如果当前组件不在 componentData 中，则置空
          const needClean = !componentData.find(component => curComponent.value.id === component.id)

          if (needClean) {
            dvMainStore.setCurComponent({
              component: null,
              index: null
            })
          }
        }
        dvMainStore.setComponentData(componentData)
      }
    },

    redo() {
      if (this.snapshotIndex < this.snapshotData.length - 1) {
        this.snapshotIndex++
        dvMainStore.setComponentData(deepCopy(this.snapshotData[this.snapshotIndex]))
      }
    },

    recordSnapshot() {
      // 添加新的快照
      this.snapshotData[++this.snapshotIndex] = deepCopy(componentData)
      // 在 undo 过程中，添加新的快照时，要将它后面的快照清理掉
      if (this.snapshotIndex < this.snapshotData.length - 1) {
        this.snapshotData = this.snapshotData.slice(0, this.snapshotIndex + 1)
      }
    }
  }
})

function getDefaultcomponentData() {
  return defaultcomponentData
}

export function setDefaultcomponentData(data = []) {
  defaultcomponentData = data
}

export const snapshotStoreWithOut = () => {
  return snapshotStore(store)
}
