
const state = {
  linkToken: null
}

const mutations = {
  SET_LINK_TOKEN: (state, value) => {
    state.linkToken = value
  }
}

const actions = {
  setLinkToken({ commit }, data) {
    commit('SET_LINK_TOKEN', data)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}

