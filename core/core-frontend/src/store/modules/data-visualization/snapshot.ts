import { defineStore, storeToRefs } from 'pinia'
import { store } from '../../index'
import { dvMainStoreWithOut } from './dvMain'
import { deepCopy } from '@/utils/utils'
import { BASE_THEMES } from '@/views/chart/components/editor/util/dataVisualiztion'

const dvMainStore = dvMainStoreWithOut()
const { curComponent, componentData, canvasStyleData, canvasViewInfo, curOriginThemes } =
  storeToRefs(dvMainStore)

let defaultCanvasInfo = {
  componentData: [],
  canvasStyleData: deepCopy(BASE_THEMES[curOriginThemes.value]),
  canvasViewInfo: {}
}

// 存储快照结构 {componentData:[],canvasStyleData:{},canvasViewInfo:{}}
export const snapshotStore = defineStore('snapshot', {
  state: () => {
    return {
      styleChangeTimes: 0, // 组件样式修改次数
      cacheStyleChangeTimes: 0, // 仪表板未缓存的组件样式修改次数
      snapshotCacheTimes: 0, // 当前未计入镜像中的修改变动次数, 此为定时缓存，缓存间隔时间5秒一次 针对类型样式这种变动不大的修改
      snapshotData: [], // 编辑器快照数据
      snapshotIndex: -1 // 快照索引
    }
  },
  actions: {
    //定时检查变动次数 存在变动次数则进行镜像处理
    snapshotCatchToStore() {
      if (this.snapshotCacheTimes) {
        this.recordSnapshot()
      }
    },
    recordSnapshotCache() {
      this.snapshotCacheTimes++
    },
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
      // 清理缓存计数器
      this.cacheChangeTimes = 0
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
