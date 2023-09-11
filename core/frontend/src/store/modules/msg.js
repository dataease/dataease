const state = {
  msgTypes: []
}

const mutations = {

  SET_MSG_TYPES: (state, value) => {
    state.msgTypes = value
  }

}

const actions = {
  setMsgTypes({ commit }, data) {
    commit('SET_MSG_TYPES', data)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}

