const state = {
  loadingMap: {}
}

const mutations = {

  SET_LOADING_MAP: (state, value) => {
    state.loadingMap = value
  },
  ADD_LOADING: (state, key) => {
    if (Object.prototype.hasOwnProperty.call(state.loadingMap, key)) {
      const map = state.loadingMap
      map[key] += 1
      state.loadingMap = map
    } else {
      const nMap = {}
      nMap[key] = 1

      state.loadingMap = nMap
    }
  },
  REDUCE_LOADING: (state, key) => {
    if (state.loadingMap) {
      const map = state.loadingMap
      map[key] -= 1
      state.loadingMap = map
    }
  }
}

const actions = {
  addLoading({ commit }, data) {
    commit('ADD_LOADING', data)
  },
  reduceLoading({ commit }, data) {
    commit('REDUCE_LOADING', data)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}

