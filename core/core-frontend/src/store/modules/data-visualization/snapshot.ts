import { defineStore, storeToRefs } from 'pinia'
import { store } from '../../index'
import { DEFAULT_CANVAS_STYLE_DATA, dvMainStoreWithOut } from './dvMain'
import { deepCopy } from '@/utils/utils'

const dvMainStore = dvMainStoreWithOut()
const { curComponent, componentData, canvasStyleData, canvasViewInfo } = storeToRefs(dvMainStore)

let defaultCanvasInfo = {
  componentData: [],
  canvasStyleData: deepCopy(DEFAULT_CANVAS_STYLE_DATA),
  canvasViewInfo: {}
}

// 存储快照结构 {componentData:[],canvasStyleData:{},canvasViewInfo:{}}
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
        const componentDataSnapshot =
          deepCopy(this.snapshotData[this.snapshotIndex]) || getDefaultCanvasInfo()
        if (curComponent.value) {
          // 如果当前组件不在 componentData 中，则置空
          const needClean = !componentDataSnapshot.find(
            component => curComponent.value.id === component.id
          )

          if (needClean) {
            dvMainStore.setCurComponent({
              component: null,
              index: null
            })
          }
        }
        dvMainStore.setComponentData(componentDataSnapshot.componentData)
        dvMainStore.setCanvasStyle(componentDataSnapshot.canvasStyleData)
        dvMainStore.setCanvasViewInfo(componentDataSnapshot.canvasViewInfo)
      }
    },

    redo() {
      if (this.snapshotIndex < this.snapshotData.length - 1) {
        this.snapshotIndex++
        const snapshotInfo = deepCopy(this.snapshotData[this.snapshotIndex])
        dvMainStore.setComponentData(snapshotInfo.componentData)
        dvMainStore.setCanvasStyle(snapshotInfo.canvasStyleData)
        dvMainStore.setCanvasViewInfo(snapshotInfo.canvasViewInfo)
      }
    },

    recordSnapshot() {
      // 添加新的快照
      const newSnapshot = {
        componentData: deepCopy(componentData.value),
        canvasStyleData: deepCopy(canvasStyleData.value),
        canvasViewInfo: deepCopy(canvasViewInfo.value)
      }
      this.snapshotData[++this.snapshotIndex] = newSnapshot
      // 在 undo 过程中，添加新的快照时，要将它后面的快照清理掉
      if (this.snapshotIndex < this.snapshotData.length - 1) {
        this.snapshotData = this.snapshotData.slice(0, this.snapshotIndex + 1)
      }
    }
  }
})

export function setDefaultComponentData(data = []) {
  defaultCanvasInfo.componentData = data
}

function getDefaultCanvasInfo() {
  return defaultCanvasInfo
}

export function setDefaultCanvasInfo(data) {
  defaultCanvasInfo = data
}

export const snapshotStoreWithOut = () => {
  return snapshotStore(store)
}
