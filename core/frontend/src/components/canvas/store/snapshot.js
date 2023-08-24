import store from '@/store/index'
import { deepCopy } from '@/components/canvas/utils/utils'

export default {
  state: {
    snapshotData: [{}], // 编辑器快照数据
    snapshotStyleData: [{}], // 样式改变也记录快照
    snapshotIndex: -1, // 快照索引
    changeTimes: -1, // 修改次数
    lastSaveSnapshotIndex: 0, // 最后保存是snapshotIndex的索引
    styleChangeTimes: 0, // 组件样式修改次数
    cacheStyleChangeTimes: 0, // 仪表板未缓存的组件样式修改次数
    doSnapshotIndex: -1 // snapshot undo redo 时的索引记录
  },
  mutations: {
    canvasChange(state) {
      state.styleChangeTimes++
      state.cacheStyleChangeTimes++
    },
    canvasCacheChange(state) {
      state.cacheStyleChangeTimes++
    },
    undo(state) {
      store.commit('setCurComponent', { component: null, index: null })
      if (state.snapshotIndex > 0) {
        state.snapshotIndex--
        state.doSnapshotIndex = state.snapshotIndex
        store.commit('setComponentData', deepCopy(state.snapshotData[state.snapshotIndex]))
        store.commit('setCanvasStyle', deepCopy(state.snapshotStyleData[state.snapshotIndex]))
      }
    },

    redo(state) {
      store.commit('setCurComponent', { component: null, index: null })
      if (state.snapshotIndex < state.snapshotData.length - 1) {
        state.snapshotIndex++
        state.doSnapshotIndex = state.snapshotIndex
        store.commit('setComponentData', deepCopy(state.snapshotData[state.snapshotIndex]))
        store.commit('setCanvasStyle', deepCopy(state.snapshotStyleData[state.snapshotIndex]))
      }
    },

    recordSnapshot(state) {
      state.cacheStyleChangeTimes++
      state.changeTimes++
      // 添加新的快照
      state.snapshotData[++state.snapshotIndex] = deepCopy(state.componentData)
      state.snapshotStyleData[state.snapshotIndex] = deepCopy(state.canvasStyleData)
      // 在 undo 过程中，添加新的快照时，要将它后面的快照清理掉
      if (state.snapshotIndex < state.snapshotData.length - 1) {
        state.snapshotData = state.snapshotData.slice(0, state.snapshotIndex + 1)
        state.snapshotStyleData = state.snapshotStyleData.slice(0, state.snapshotIndex + 1)
      }
    },
    refreshSnapshot(state) {
      // 刷新快照
      state.snapshotData = [deepCopy(state.componentData)]
      state.snapshotStyleData = [deepCopy(state.canvasStyleData)]
      state.snapshotIndex = 0
      state.changeTimes = -1
      state.lastSaveSnapshotIndex = 0
    },
    refreshSaveStatus(state) {
      state.changeTimes = 0
      state.lastSaveSnapshotIndex = deepCopy(state.snapshotIndex)
    },
    recordChangeTimes(state) {
      state.changeTimes++
    },
    recordStyleChange(state) {
      state.styleChangeTimes++
    }
  }
}
