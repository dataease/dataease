import { defineStore, storeToRefs } from 'pinia'
import { markRaw } from 'vue'
import { store } from '../../index'
import { dvMainStoreWithOut } from './dvMain'
import { deepCopy } from '@/utils/utils'
import { BASE_THEMES } from '@/views/chart/components/editor/util/dataVisualization'
import eventBus from '@/utils/eventBus'
import { useEmitt } from '@/hooks/web/useEmitt'
import { ElMessage } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import {
  discardCanvasDraft,
  flushCanvasDraft,
  queueCanvasDraft,
  setCanvasDraftErrorHandler
} from '@/utils/canvasDraftCache'

export const SNAPSHOT_MAX_COUNT = 50
export const SNAPSHOT_MAX_BYTES = 20 * 1024 * 1024

// 估算历史模型占用，不重复 JSON 序列化；单个超预算模型仍保留当前态。
const estimateSnapshotBytes = value => {
  let bytes = 0
  const pending = [value]
  while (pending.length && bytes <= SNAPSHOT_MAX_BYTES) {
    const item = pending.pop()
    if (typeof item === 'string') {
      bytes += item.length * 2
    } else if (item && typeof item === 'object') {
      bytes += 32
      for (const key in item) {
        bytes += key.length * 2 + 8
        pending.push(item[key])
      }
    } else {
      bytes += 8
    }
  }
  return bytes
}

setCanvasDraftErrorHandler(() => {
  ElMessage.warning({
    message: useI18n().t('visualization.canvas_draft_storage_failed'),
    grouping: true
  })
})

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
  nowPanelJumpInfo,
  mobileInPc
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
      snapshotDisableTime: 1, // 镜像禁用时间，解决redo undo 造成的样式变更
      styleChangeTimes: -1, // 组件样式修改次数
      cacheStyleChangeTimes: 0, // 仪表板未缓存的组件样式修改次数
      snapshotCacheTimes: 0, // 当前未计入镜像中的修改次数，主画布每秒检查有变动时才记录
      cacheViewIdInfo: {
        snapshotCacheViewCalc: [], // 当前未计入镜像需要图表计算的图表ID, all代表全部
        snapshotCacheViewRender: [] // 当前未计入镜像需要图表渲染的图表ID,all代表全部
      },
      snapshotData: [], // 编辑器快照数据
      snapshotSizes: [], // 每份快照的估算字节数
      snapshotBytes: 0,
      snapshotIndex: -1 // 快照索引
    }
  },
  actions: {
    initSnapShot() {
      // 切换资源前提交已排队的草稿；任务持有原资源 ID，不跟随新画布变化。
      void flushCanvasDraft()
      this.styleChangeTimes = -1
      this.cacheStyleChangeTimes = 0
      this.snapshotCacheTimes = 0
      this.cacheViewIdInfo = {
        snapshotCacheViewCalc: [],
        snapshotCacheViewRender: []
      }
      this.snapshotData = []
      this.snapshotSizes = []
      this.snapshotBytes = 0
      this.snapshotIndex = -1
    },
    //定时检查变动次数 存在变动次数则进行镜像处理
    snapshotCatchToStore() {
      if (this.snapshotCacheTimes) {
        this.recordSnapshot('snapshotCatchToStore')
      }
    },
    recordSnapshotCacheToMobile(type, component = curComponent.value, otherComponent = null) {
      if (mobileInPc.value && component) {
        //移动端设计
        useEmitt().emitter.emit('onMobileStatusChange', {
          type: 'componentStyleChange',
          value: {
            type: type,
            component: JSON.parse(JSON.stringify(component)),
            otherComponent: otherComponent
          }
        })
      }
      this.recordSnapshotCache(type)
    },
    recordSnapshotCacheWithPositionChange(type?, viewId = 'all') {
      dvMainStore.setLastHiddenComponent()
      this.recordSnapshotCache(type, viewId)
    },
    recordSnapshotCache(type?, viewId = 'all') {
      if (dataPrepareState.value) {
        const appendViewId = list => {
          if (list.includes('all')) return
          if (viewId === 'all') list.splice(0, list.length, 'all')
          else if (!list.includes(viewId)) list.push(viewId)
        }
        if (type === 'calcData') {
          appendViewId(this.cacheViewIdInfo.snapshotCacheViewCalc)
        } else if (type === 'renderChart') {
          appendViewId(this.cacheViewIdInfo.snapshotCacheViewRender)
        }
        this.snapshotCacheTimes++
      }
    },
    undo() {
      if (this.snapshotIndex > 0) {
        this.snapshotIndex--
        const componentSnapshot =
          deepCopy(this.snapshotData[this.snapshotIndex]) || getDefaultCanvasInfo()
        componentSnapshot.dvInfo.id = dvInfo.value.id
        componentSnapshot.dvInfo.pid = dvInfo.value.pid
        componentSnapshot.dvInfo.dataState = dvInfo.value.dataState
        componentSnapshot.dvInfo.contentId = dvInfo.value.contentId
        // 历史模型不可变，可直接用于草稿；发布到画布的副本会重新挂入临时素材包。
        queueCanvasDraft({
          ...this.snapshotData[this.snapshotIndex],
          dvInfo: { ...componentSnapshot.dvInfo }
        })
        // undo 是当前没有记录
        this.snapshotPublish(componentSnapshot)
        this.styleChangeTimes++
        this.snapshotDisableTime = Date.now() + 3000
      }
    },

    redo() {
      if (this.snapshotIndex < this.snapshotData.length - 1) {
        this.snapshotIndex++
        const snapshotInfo = deepCopy(this.snapshotData[this.snapshotIndex])
        snapshotInfo.dvInfo.id = dvInfo.value.id
        snapshotInfo.dvInfo.pid = dvInfo.value.pid
        snapshotInfo.dvInfo.dataState = dvInfo.value.dataState
        snapshotInfo.dvInfo.contentId = dvInfo.value.contentId
        queueCanvasDraft({
          ...this.snapshotData[this.snapshotIndex],
          dvInfo: { ...snapshotInfo.dvInfo }
        })
        this.snapshotPublish(snapshotInfo)
        this.styleChangeTimes++
        this.snapshotDisableTime = Date.now() + 3000
      }
    },
    snapshotPublish(snapshotInfo) {
      // Keep an unfinished package outside undo snapshots; binary originals can be large.
      if (dvInfo.value.materialBundle)
        snapshotInfo.dvInfo.materialBundle = dvInfo.value.materialBundle
      else delete snapshotInfo.dvInfo.materialBundle
      dvMainStore.updateCurDvInfo(snapshotInfo.dvInfo)
      dvMainStore.setComponentData(snapshotInfo.componentData)
      dvMainStore.setCanvasStyle(snapshotInfo.canvasStyleData)
      dvMainStore.setCanvasViewInfo(snapshotInfo.canvasViewInfo)
      dvMainStore.setNowPanelJumpInfoInner(snapshotInfo.nowPanelJumpInfo)
      dvMainStore.setNowPanelTrackInfo(snapshotInfo.nowPanelTrackInfo)
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
      this.snapshotCacheTimes = 0
    },
    clearCanvasDraft(resourceId = dvInfo.value.id) {
      if (String(resourceId) === String(dvInfo.value.id)) {
        this.snapshotCacheTimes = 0
      }
      return discardCanvasDraft(resourceId)
    },
    resetSnapshot() {
      void flushCanvasDraft()
      this.styleChangeTimes = -1
      this.cacheStyleChangeTimes = 0
      this.snapshotCacheTimes = 0
      this.cacheViewIdInfo = {
        snapshotCacheViewCalc: [],
        snapshotCacheViewRender: []
      }
      this.snapshotData = []
      this.snapshotSizes = []
      this.snapshotBytes = 0
      this.snapshotIndex = -1
      this.recordSnapshot()
    },

    recordSnapshot(type) {
      // 移动端设计时暂不保存镜像
      if (dataPrepareState.value && !mobileInPc.value && Date.now() > this.snapshotDisableTime) {
        this.styleChangeTimes = ++this.styleChangeTimes
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
          dvInfo: deepCopy(
            Object.fromEntries(
              Object.entries(dvInfo.value).filter(([key]) => key !== 'materialBundle')
            )
          )
        }
        // 在 undo 过程中，添加新的快照时，要将它后面的快照清理掉
        if (this.snapshotIndex < this.snapshotData.length - 1) {
          this.snapshotData.splice(this.snapshotIndex + 1)
          const removedSizes = this.snapshotSizes.splice(this.snapshotIndex + 1)
          this.snapshotBytes -= removedSizes.reduce((sum, size) => sum + size, 0)
        }
        const size = estimateSnapshotBytes(newSnapshot)
        this.snapshotData.push(markRaw(newSnapshot))
        this.snapshotSizes.push(size)
        this.snapshotBytes += size
        while (
          this.snapshotData.length > 1 &&
          (this.snapshotData.length > SNAPSHOT_MAX_COUNT || this.snapshotBytes > SNAPSHOT_MAX_BYTES)
        ) {
          this.snapshotData.shift()
          this.snapshotBytes -= this.snapshotSizes.shift()
        }
        this.snapshotIndex = this.snapshotData.length - 1
        // 清理缓存计数器
        this.snapshotCacheTimes = 0
        if (this.styleChangeTimes > 0) {
          queueCanvasDraft(newSnapshot)
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
