
const getDefaultState = () => {
  return {
    panelName: '',
    panelInfo: {
      id: null,
      name: '',
      preStyle: null,
      proxy: null
    },
    canvasStyleDataTemp: null, // 页面全局临时存储数据
    componentDataTemp: null, // 画布组件临时存储数据
    mainActiveName: 'PanelMain'
  }
}

const state = getDefaultState()

const mutations = {
  setPanelName: (state, panelName) => {
    state.panelName = panelName
  },
  setMainActiveName: (state, mainActiveName) => {
    state.mainActiveName = mainActiveName
  },
  setPanelInfo: (state, panelInfo) => {
    state.panelInfo = panelInfo
  },
  setCanvasStyleDataTemp: (state, canvasStyleDataTemp) => {
    state.canvasStyleDataTemp = canvasStyleDataTemp
  },
  setComponentDataTemp: (state, componentDataTemp) => {
    state.componentDataTemp = componentDataTemp
  }
}

const actions = {
  setPanelName({ commit }, panelName) {
    commit('setPanelName', panelName)
  },
  setMainActiveName({ commit }, mainActiveName) {
    commit('setMainActiveName', mainActiveName)
  },
  setPanelInfo({ commit }, panelInfo) {
    commit('setPanelInfo', panelInfo)
  },
  setCanvasStyleDataTemp({ commit }, canvasStyleDataTemp) {
    commit('setCanvasStyleDataTemp', canvasStyleDataTemp)
  },
  setComponentDataTemp({ commit }, componentDataTemp) {
    commit('setComponentDataTemp', componentDataTemp)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}

