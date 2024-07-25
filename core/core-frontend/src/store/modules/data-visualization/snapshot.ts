import { defineStore, storeToRefs } from 'pinia'
import { store } from '../../index'
import { dvMainStoreWithOut } from './dvMain'
import { deepCopy } from '@/utils/utils'
import { BASE_THEMES } from '@/views/chart/components/editor/util/dataVisualization'
import eventBus from '@/utils/eventBus'
import { useEmitt } from '@/hooks/web/useEmitt'
import { useCache } from '@/hooks/web/useCache'
const { wsCache } = useCache('localStorage')

const dvMainStore = dvMainStoreWithOut()
const {
  dvInfo,
  curComponent,
  componentData,
  canvasStyleData,
  canvasViewInfo,
  curOriginThemes,
  dataPrepareState,
  nowPanelTrackInfo,
  nowPanelJumpInfo
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
      styleChangeTimes: -1, // 组件样式修改次数
      cacheStyleChangeTimes: 0, // 仪表板未缓存的组件样式修改次数
      snapshotCacheTimes: 0, // 当前未计入镜像中的修改变动次数, 此为定时缓存，缓存间隔时间5秒一次 针对类型样式这种变动不大的修改
      cacheViewIdInfo: {
        snapshotCacheViewCalc: [], // 当前未计入镜像需要图表计算的图表ID, all代表全部
        snapshotCacheViewRender: [] // 当前未计入镜像需要图表渲染的图表ID,all代表全部
      },
      snapshotData: [], // 编辑器快照数据
      snapshotIndex: -1 // 快照索引
    }
  },
  actions: {
    //定时检查变动次数 存在变动次数则进行镜像处理
    snapshotCatchToStore() {
      if (this.snapshotCacheTimes) {
        this.recordSnapshot('snapshotCatchToStore')
      }
    },
    recordSnapshotCache(type?, viewId = 'all') {
      if (dataPrepareState.value) {
        if (type === 'calcData') {
          this.cacheViewIdInfo.snapshotCacheViewCalc.push(viewId)
        } else if (type === 'renderChart') {
          this.cacheViewIdInfo.snapshotCacheViewRender.push(viewId)
        }
        this.snapshotCacheTimes++
      }
    },
    undo() {
      if (this.snapshotIndex > 0) {
        this.snapshotIndex--
        const componentSnapshot =
          deepCopy(this.snapshotData[this.snapshotIndex]) || getDefaultCanvasInfo()
        // undo 是当前没有记录
        this.snapshotPublish(componentSnapshot)
        this.styleChangeTimes++
      }
    },

    redo() {
      if (this.snapshotIndex < this.snapshotData.length - 1) {
        this.snapshotIndex++
        const snapshotInfo = deepCopy(this.snapshotData[this.snapshotIndex])
        this.snapshotPublish(snapshotInfo)
        this.styleChangeTimes++
      }
    },
    snapshotPublish(snapshotInfo) {
      dvMainStore.setComponentData(snapshotInfo.componentData)
      dvMainStore.setCanvasStyle(snapshotInfo.canvasStyleData)
      dvMainStore.setCanvasViewInfo(snapshotInfo.canvasViewInfo)
      dvMainStore.setNowPanelJumpInfoInner(snapshotInfo.nowPanelJumpInfo)
      dvMainStore.setNowPanelTrackInfo(snapshotInfo.nowPanelTrackInfo)
      dvMainStore.updateCurDvInfo(snapshotInfo.dvInfo)
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
        if (
          !curComponentMatch ||
          (curComponent.value.innerType && curComponent.value.innerType === 'rich-text')
        ) {
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

    resetStyleChangeTimes() {
      this.styleChangeTimes = 0
    },
    resetSnapshot() {
      this.styleChangeTimes = -1
      this.cacheStyleChangeTimes = 0
      this.snapshotCacheTimes = 0
      this.cacheViewIdInfo = {
        snapshotCacheViewCalc: [],
        snapshotCacheViewRender: []
      }
      this.snapshotData = []
      this.snapshotIndex = -1
      this.recordSnapshot()
    },

    recordSnapshot() {
      this.styleChangeTimes = ++this.styleChangeTimes
      if (dataPrepareState.value) {
        const snapshotComponentData = deepCopy(componentData.value)
        dvMainStore.removeGroupArea(snapshotComponentData)
        // 添加新的快照
        const newSnapshot = {
          componentData: snapshotComponentData,
          canvasStyleData: deepCopy(canvasStyleData.value),
          canvasViewInfo: deepCopy(canvasViewInfo.value),
          cacheViewIdInfo: deepCopy(this.cacheViewIdInfo),
          nowPanelTrackInfo: deepCopy(nowPanelTrackInfo.value),
          nowPanelJumpInfo: deepCopy(nowPanelJumpInfo.value),
          dvInfo: deepCopy(dvInfo.value)
        }
        this.snapshotData[++this.snapshotIndex] = newSnapshot
        // 在 undo 过程中，添加新的快照时，要将它后面的快照清理掉
        if (this.snapshotIndex < this.snapshotData.length - 1) {
          this.snapshotData = this.snapshotData.slice(0, this.snapshotIndex + 1)
        }
        // 清理缓存计数器
        this.snapshotCacheTimes = 0
        if (this.snapshotData.length > 1) {
          wsCache.set('DE-DV-CATCH-' + dvInfo.value.id, newSnapshot)
        }
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
