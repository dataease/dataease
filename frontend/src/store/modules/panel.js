
const getDefaultState = () => {
  return {
    panelName: '',
    panelInfo: {
      name: ''
    }
  }
}

const state = getDefaultState()

const mutations = {
  setPanelName: (state, panelName) => {
    state.panelName = panelName
  },
  setPanelInfo: (state, panelInfo) => {
    debugger
    state.panelInfo = panelInfo
  }
}

const actions = {
  setPanelName({ commit }, panelName) {
    commit('setPanelName', panelName)
  },
  setPanelInfo({ commit }, panelInfo) {
    debugger
    commit('setPanelInfo', panelInfo)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}

