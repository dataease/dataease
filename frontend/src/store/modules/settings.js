import defaultSettings from '@/settings'
import variables from '@/styles/variables.scss'
const { showSettings, sidebarLogo } = defaultSettings
const state = {
  theme: variables.theme,
  showSettings: showSettings,
  //   fixedHeader: fixedHeader,
  sidebarLogo: sidebarLogo
}

const mutations = {
  CHANGE_SETTING: (state, { key, value }) => {
    if (state.hasOwnProperty(key)) {
      state[key] = value
    }
  }
}

const actions = {
  changeSetting({ commit }, data) {
    commit('CHANGE_SETTING', data)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}

