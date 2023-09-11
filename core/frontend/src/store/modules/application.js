const state = {
  beanMap: {},
  leftWidgetMap: {},
  dialogWidgetMap: {},
  drawWidgetMap: {}
}

const mutations = {

  ADD_BEAN: (state, { key, value }) => {
    state.beanMap[key] = value
  },

  ADD_LEFT_WIDGET: (state, { uuid, leftPanel }) => {
    state.leftWidgetMap[uuid] = leftPanel
  },

  ADD_DIALOG_WIDGET: (state, { uuid, dialogPanel }) => {
    state.dialogWidgetMap[uuid] = dialogPanel
  },

  ADD_DRAW_WIDGET: (state, { uuid, drawPanel }) => {
    state.drawWidgetMap[uuid] = drawPanel
  }

}

const actions = {
  loadBean({ commit }, data) {
    commit('ADD_BEAN', data)
  },

  addLeftWidget({ commit }, data) {
    commit('ADD_LEFT_WIDGET', data)
  },

  addDialogWidget({ commit }, data) {
    commit('ADD_DIALOG_WIDGET', data)
  },

  addDrawWidget({ commit }, data) {
    commit('ADD_DRAW_WIDGET', data)
  }

}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}

