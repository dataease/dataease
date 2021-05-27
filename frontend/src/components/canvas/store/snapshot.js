import store from '@/store/index'
import { deepCopy } from '@/components/canvas/utils/utils'

export default {
  state: {
    snapshotData: [], // 编辑器快照数据
    snapshotStyleData: [], // 样式改变也记录快照
    snapshotIndex: -1 // 快照索引
  },
  mutations: {
    undo(state) {
      if (state.snapshotIndex > 0) {
        state.snapshotIndex--
        store.commit('setComponentData', deepCopy(state.snapshotData[state.snapshotIndex]))
        store.commit('setCanvasStyle', deepCopy(state.snapshotStyleData[state.snapshotIndex]))
      }
    },

    redo(state) {
      debugger
      if (state.snapshotIndex < state.snapshotData.length - 1) {
        state.snapshotIndex++
        store.commit('setComponentData', deepCopy(state.snapshotData[state.snapshotIndex]))
        store.commit('setCanvasStyle', deepCopy(state.snapshotStyleData[state.snapshotIndex]))
      }
    },

    recordSnapshot(state) {
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
      state.snapshotData = []
      state.snapshotStyleData = []
      state.snapshotIndex = -1
    }
  }
}
