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
export const fullScreenRouters = ['XpackThemeForm', 'system/datasource/DsForm', 'dataset/form']
export const filterAsyncRouter = (routers) => { // 遍历后台传来的路由字符串，转换为组件对象
  return routers.map(router => {
    if (!fullScreenRouters.includes(router.component) && router.type === 1 && router.pid === 0 && router.component && router.component !== 'Layout') {
      router = decorate(router)
    }
    if (router.isPlugin) {
      const jsName = router.component
      router.component = 'system/plugin/dynamic'
      router.props = {
        jsname: jsName,
        menuid: router.id,
        noLayout: router.noLayout
      }
    }
    if (router.component) {
      if (router.component === 'Layout') { // Layout组件特殊处理
        router.component = Layout
      } else {
        const component = router.component
        router.component = loadView(component)
      }
    }
    router.name && fillMeta(router)
    if (router.children && router.children.length) {
      router.children = filterAsyncRouter(router.children)
    }

    router.hasOwnProperty('id') && delete router.id
    router.hasOwnProperty('type') && delete router.type
    router.hasOwnProperty('pid') && delete router.pid
    router.hasOwnProperty('children') && (!router['children'] || !router['children'].length) && delete router.children
    router.hasOwnProperty('redirect') && !router['redirect'] && delete router.redirect
    return router
  })
}

// 后台设计时未考虑activeMenu字段 这里先前端处理一下
export const fillMeta = (router) => {
  router.name.includes('system-user-form') && (router.meta.activeMenu = '/system/user')
  router.name.includes('system-role-form') && (router.meta.activeMenu = '/system/role')
  router.name.includes('system-dept-form') && (router.meta.activeMenu = '/system/dept')
  // router.name.includes('sys-task-dataset') && (router.meta.activeMenu = '/system/dstask')
  // return router
}

// 包装一层父级目录
export const decorate = (router) => {
  const parent = {
    id: router.id + 1000000,
    path: router.path,
    component: 'Layout'
  }
  const current = {}
  Object.assign(current, router)
  current.type = 1
  current.path = 'index'
  current.pid = parent.id
  parent.children = [current]
  if (router.hidden) {
    parent.hidden = router.hidden
  }
  return parent
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
