import { defineStore, storeToRefs } from 'pinia'
import { store } from '../../index'
import { dvMainStoreWithOut } from './dvMain'
import { deepCopy } from '@/utils/utils'
import { BASE_THEMES } from '@/views/chart/components/editor/util/dataVisualiztion'
import eventBus from '@/utils/eventBus'
import { useEmitt } from '@/hooks/web/useEmitt'

const dvMainStore = dvMainStoreWithOut()
const {
  curComponent,
  componentData,
  canvasStyleData,
  canvasViewInfo,
  curOriginThemes,
  dataPrepareState
} = storeToRefs(dvMainStore)

let defaultCanvasInfo = {
  componentData: [],
  canvasStyleData: deepCopy(BASE_THEMES[curOriginThemes.value]),
  canvasViewInfo: {},
  cacheViewIdInfo: {
    snapshotCacheViewCalc: [],
    snapshotCacheViewRender: []
  }
}

// 存储快照结构 {componentData:[],canvasStyleData:{},canvasViewInfo:{}}
export const snapshotStore = defineStore('snapshot', {
  state: () => {
    return {
      styleChangeTimes: 0, // 组件样式修改次数
      cacheStyleChangeTimes: 0, // 仪表板未缓存的组件样式修改次数
      snapshotCacheTimes: 0, // 当前未计入镜像中的修改变动次数, 此为定时缓存，缓存间隔时间5秒一次 针对类型样式这种变动不大的修改
      cacheViewIdInfo: {
        snapshotCacheViewCalc: [], // 当前未计入镜像需要视图计算的视图ID, all代表全部
        snapshotCacheViewRender: [] // 当前未计入镜像需要视图渲染的视图ID,all代表全部
      },
      snapshotData: [], // 编辑器快照数据
      snapshotIndex: -1 // 快照索引
    }
  },
  actions: {
    //定时检查变动次数 存在变动次数则进行镜像处理
    snapshotCatchToStore() {
      // console.log('snapshotCatchToStore-' + this.snapshotCacheTimes)
      if (this.snapshotCacheTimes) {
        this.recordSnapshot('snapshotCatchToStore')
      }
    },
    recordSnapshotCache(type, viewId) {
      if (dataPrepareState.value) {
        if (type === 'calcData') {
          this.cacheViewIdInfo.snapshotCacheViewCalc.push(viewId)
        } else if (type === 'renderChart') {
          this.cacheViewIdInfo.snapshotCacheViewRender.push(viewId)
        }
        if (type) this.snapshotCacheTimes++
      }
    },
    undo() {
      if (this.snapshotIndex > 0) {
        this.snapshotIndex--
        const componentSnapshot =
          deepCopy(this.snapshotData[this.snapshotIndex]) || getDefaultCanvasInfo()
        // undo 是当前没有记录
        this.snapshotPublish(componentSnapshot)
      }
    },

    redo() {
      // console.log('redo')
      if (this.snapshotIndex < this.snapshotData.length - 1) {
        this.snapshotIndex++
        const snapshotInfo = deepCopy(this.snapshotData[this.snapshotIndex])
        this.snapshotPublish(snapshotInfo)
      }
    },
    snapshotPublish(snapshotInfo) {
      // console.log('snapshotPublish-' + JSON.stringify(snapshotInfo.canvasViewInfo))
      dvMainStore.setComponentData(snapshotInfo.componentData)
      dvMainStore.setCanvasStyle(snapshotInfo.canvasStyleData)
      dvMainStore.setCanvasViewInfo(snapshotInfo.canvasViewInfo)
      const curCacheViewIdInfo = deepCopy(this.cacheViewIdInfo)
      this.cacheViewIdInfo = snapshotInfo.cacheViewIdInfo

      if (curComponent.value) {
        let curComponentMatch = false
        // 如果当前组件不在 componentData 中，则置空
        snapshotInfo.componentData.forEach((component, index) => {
          if (curComponent.value.id === component.id) {
            curComponentMatch = true
            dvMainStore.setCurComponent({
              component: component,
              index: index
            })
          }
        })
        if (!curComponentMatch) {
          dvMainStore.setCurComponent({
            component: null,
            index: null
          })
        }
      }

      const paramCacheViewInfo = {
        snapshotCacheViewCalc: [
          ...curCacheViewIdInfo.snapshotCacheViewCalc,
          ...this.cacheViewIdInfo.snapshotCacheViewCalc
        ],
        snapshotCacheViewRender: [
          ...curCacheViewIdInfo.snapshotCacheViewRender,
          ...this.cacheViewIdInfo.snapshotCacheViewRender
        ],
        canvasViewInfo: snapshotInfo.canvasViewInfo
      }
      eventBus.emit('snapshotChange')
      if (
        paramCacheViewInfo.snapshotCacheViewCalc.length > 0 ||
        paramCacheViewInfo.snapshotCacheViewRender.length > 0
      ) {
        useEmitt().emitter.emit('snapshotChangeToView', paramCacheViewInfo)
      }
    },

    recordSnapshot(type) {
      if (dataPrepareState.value) {
        // console.log('recordSnapshot-' + type)
        // 添加新的快照
        const newSnapshot = {
          componentData: deepCopy(componentData.value),
          canvasStyleData: deepCopy(canvasStyleData.value),
          canvasViewInfo: deepCopy(canvasViewInfo.value),
          cacheViewIdInfo: deepCopy(this.cacheViewIdInfo)
        }
        this.snapshotData[++this.snapshotIndex] = newSnapshot
        // 在 undo 过程中，添加新的快照时，要将它后面的快照清理掉
        if (this.snapshotIndex < this.snapshotData.length - 1) {
          this.snapshotData = this.snapshotData.slice(0, this.snapshotIndex + 1)
        }
        // console.log('recordSnapshot-' + JSON.stringify(this.snapshotData))
        // 清理缓存计数器
        this.snapshotCacheTimes = 0
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
