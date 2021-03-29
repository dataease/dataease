const state = {
  beanMap: {}
}

const mutations = {

  ADD_BEAN: (state, { key, value }) => {
    state.beanMap[key] = value
  }

}

const actions = {
  loadBean({ commit }, data) {
    commit('ADD_BEAN', data)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}

