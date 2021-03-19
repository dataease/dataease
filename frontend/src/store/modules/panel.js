
const getDefaultState = () => {
  return {
    panelName: '',
    panelInfo:{
      id:null,
      name:''
    }
  }
}

const state = getDefaultState()

const mutations = {
  setPanelName: (state, panelName) => {
    state.panelName = panelName
  },
  setPanelInfo: (state, panelInfo) => {
    state.panelInfo = panelInfo
  }
}

const actions = {
  setPanelName({ commit }, panelName) {
    commit('setPanelName', panelName)
  },
  setPanelInfo({ commit }, panelInfo) {
    commit('setPanelInfo', panelInfo)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}

