const state = {
  geoMap: {}
}

const mutations = {

  SET_GEO: (state, { key, value }) => {
    state.geoMap[key] = value
  }

}

const actions = {
  setGeo({ commit }, data) {
    commit('SET_GEO', data)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}

