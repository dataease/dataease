import { constantRoutes } from '@/router'
import Layout from '@/layout/index'

const state = {
  currentPath: null,
  routes: [],
  addRoutes: [],
  currentRoutes: {}
}

const mutations = {
  SET_ROUTERS: (state, routes) => {
    state.addRoutes = routes
    state.routes = constantRoutes.concat(routes)
  },
  SET_CURRENT_ROUTES: (state, routes) => {
    state.currentRoutes = routes
  },
  SET_CURRENT_PATH: (state, path) => {
    state.currentPath = path
  }
}

const actions = {
  GenerateRoutes({ commit }, asyncRouter) {
    commit('SET_ROUTERS', asyncRouter)
  },
  setCurrentPath({ commit }, path) {
    commit('SET_CURRENT_PATH', path)
  }
}

export const filterAsyncRouter = (routers) => { // 遍历后台传来的路由字符串，转换为组件对象
  return routers.filter(router => {
    if (router.component) {
      if (router.component === 'Layout') { // Layout组件特殊处理
        router.component = Layout
      } else {
        const component = router.component
        router.component = loadView(component)
      }
    }
    if (router.children && router.children.length) {
      router.children = filterAsyncRouter(router.children)
    }
    return true
  }).map(router => {
    router.hasOwnProperty('id') && delete router.id
    router.hasOwnProperty('pid') && delete router.pid
    router.hasOwnProperty('children') && (!router['children'] || !router['children'].length) && delete router.children
    router.hasOwnProperty('redirect') && !router['redirect'] && delete router.redirect
    return router
  })
}

export const loadView = (view) => {
  return (resolve) => require([`@/views/${view}`], resolve)
}
export default {
  namespaced: true,
  state,
  mutations,
  actions
}
