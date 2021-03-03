
const getDefaultState = () => {
  return {
    sceneData: {},
    table: {}
  }
}

const state = getDefaultState()

const mutations = {
  setSceneData: (state, sceneData) => {
    state.sceneData = sceneData
  },
  setTable: (state, table) => {
    state.table = table
  }
}

const actions = {
  setSceneData({ commit }, sceneData) {
    commit('setSceneData', sceneData)
  },
  setTable({ commit }, table) {
    commit('setTable', table)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}

