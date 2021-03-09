
const getDefaultState = () => {
  return {
    panelName: ''
  }
}

const state = getDefaultState()

const mutations = {
  setPanelName: (state, panelName) => {
    state.panelName = panelName
  }
}

const actions = {
  setPanelName({ commit }, panelName) {
    commit('setPanelName', panelName)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}

