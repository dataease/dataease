const get = () => {
  return localStorage.getItem('sidebarStatus')
}
const set = value => {
  localStorage.setItem('sidebarStatus', value)
}
const state = {
  sidebar: {
    opened: get() ? !!+get() : true
  },
  device: 'desktop'
}

const mutations = {
  TOGGLE_SIDEBAR: state => {
    state.sidebar.opened = !state.sidebar.opened
    if (state.sidebar.opened) {
      set(1)
    } else {
      set(0)
    }
  },
  OPEN_SIDEBAR: (state) => {
    set('sidebarStatus', 1)
    state.sidebar.opened = true
  },
  CLOSE_SIDEBAR: (state) => {
    set('sidebarStatus', 0)
    state.sidebar.opened = false
  }
}

const actions = {
  toggleSideBar({commit}) {
    commit('TOGGLE_SIDEBAR')
  },
  openSideBar({commit}) {
    commit('OPEN_SIDEBAR')
  },
  closeSideBar({commit}) {
    commit('CLOSE_SIDEBAR')
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
