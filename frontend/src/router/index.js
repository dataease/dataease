import Vue from 'vue'
import Router from 'vue-router'

// 加载modules中的路由
const modules = require.context('./modules', true, /\.js$/)

// 修复路由变更后报错的问题
const routerPush = Router.prototype.push;
Router.prototype.push = function push(location) {
  return routerPush.call(this, location).catch(error => error)
}

Vue.use(Router)

import Layout from '@/business/app-layout/horizontal-layout'

export const constantRoutes = [
  {
    path: '/redirect',
    component: Layout,
    hidden: true,
    children: [
      {
        path: '/redirect/:path(.*)',
        component: () => import('@/components/redirect')
      }
    ]
  },
  {
    path: '/login',
    component: () => import('@/business/login'),
    hidden: true
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        component: () => import('@/business/dashboard'),
        name: 'Dashboard',
        meta: {title: 'Dashboard', icon: 'el-icon-s-marketing', affix: true}
      }
    ]
  }
]

/**
 * 用户登录后根据角色加载的路由
 */
export const rolesRoutes = [
  ...modules.keys().map(key => modules(key).default),
  {path: '*', redirect: '/', hidden: true}
]

const createRouter = () => new Router({
  scrollBehavior: () => ({y: 0}),
  routes: constantRoutes
})

const router = createRouter()

export function resetRouter() {
  const newRouter = createRouter()
  router.matcher = newRouter.matcher // reset router
}

export default router
