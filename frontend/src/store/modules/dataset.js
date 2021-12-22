
const getDefaultState = () => {
  return {
    sceneData: {},
    table: {},
    hideCustomDs: false
  }
}

const state = getDefaultState()

const mutations = {
  setSceneData: (state, sceneData) => {
    state.sceneData = sceneData
  },
  setTable: (state, table) => {
    state.table = table
  },
  setHideCustomDs: (state, hideCustomDs) => {
    state.hideCustomDs = hideCustomDs
  }
}

const actions = {
  setSceneData({ commit }, sceneData) {
    commit('setSceneData', sceneData)
  },
  setTable({ commit }, table) {
    commit('setTable', table)
  },
  setHideCustomDs({ commit }, hideCustomDs) {
    commit('setHideCustomDs', hideCustomDs)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}

