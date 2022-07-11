import Vue from 'vue'
const state = {
  panelViews: {}
}

const mutations = {

  ADD_VIEW: (state, { panelId, viewId }) => {
    if (!state.panelViews[panelId]) {
      Vue.set(state.panelViews, panelId, [])
    }
    const viewIds = state.panelViews[panelId]
    if (viewIds.some(item => item === viewId)) {
      return
    }
    viewIds.push(viewId)
    state.panelViews[panelId] = viewIds
  },

  DEL_VIEW: (state, { panelId, viewId }) => {
    const views = state.panelViews[panelId]
    if (!views || !views.length) return
    let len = views.length
    while (len--) {
      const item = views[len]
      if (viewId === item) {
        views.splice(len, 1)
      }
    }
    state.panelViews[panelId] = views
  },

  DEL_PANEL_VIEW: (state, panelId) => {
    const views = state.panelViews[panelId]
    if (!views || !views.length) return
    Vue.set(state.panelViews, panelId, [])
  },

  INIT_PANEL_VIEWS: (state, { panelId, viewIds }) => {
    state.panelViews[panelId] = viewIds || []
  }
}

const actions = {
  initPanelView({ commit }, data) {
    commit('INIT_PANEL_VIEWS', data)
  },
  addView({ commit }, data) {
    commit('ADD_VIEW', data)
  },
  delView({ commit }, data) {
    commit('DEL_VIEW', data)
  },
  delPanelViews({ commit }, data) {
    commit('DEL_PANEL_VIEW', data)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}

