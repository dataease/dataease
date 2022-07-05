import Vue from 'vue'
const state = {
  panelViews: {}
}

const mutations = {

  ADD_VIEW: (state, { panelId, viewId, title }) => {
    if (!state.panelViews[panelId]) {
      Vue.set(state.panelViews, panelId, [])
    }
    const views = state.panelViews[panelId]
    if (views.some(item => item.viewId === viewId)) {
      return
    }
    views.push({ viewId, title })
    state.panelViews[panelId] = views
  },

  DEL_VIEW: (state, { panelId, viewId }) => {
    const views = state.panelViews[panelId]
    if (!views || !views.length) return
    let len = views.length
    while (len--) {
      const item = views[len]
      if (viewId === item.viewId) {
        views.splice(len, 1)
      }
    }
    state.panelViews[panelId] = views
  }
}

const actions = {
  addView({ commit }, data) {
    commit('ADD_VIEW', data)
  },
  delView({ commit }, data) {
    commit('DEL_VIEW', data)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}

