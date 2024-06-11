import { createRouter, createWebHashHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import type { App } from 'vue'

export const routes: AppRouteRecordRaw[] = [
  {
    path: '/',
    name: '/',
    redirect: '/index',
    hidden: true,
    meta: {}
  },
  {
    path: '/index',
    name: 'index',
    component: () => import('@/views/mobile/index.vue'),
    hidden: true,
    meta: {}
  },
  {
    path: '/login',
    name: 'login',
    hidden: true,
    meta: {},
    component: () => import('@/views/mobile/login/index.vue')
  },
  {
    path: '/panel',
    name: 'panel',
    hidden: true,
    meta: {},
    component: () => import('@/views/mobile/panel/index.vue')
  },
  {
    path: '/de-link/:uuid',
    name: 'link',
    hidden: true,
    meta: {},
    component: () => import('@/views/share/link/mobile.vue')
  },
  {
    path: '/pc/de-link/:uuid',
    name: 'linkPc',
    hidden: true,
    meta: {},
    component: () => import('@/views/share/link/index.vue')
  },
  {
    path: '/panel/mobile',
    name: 'mobile',
    hidden: true,
    meta: {},
    component: () => import('@/views/mobile/panel/Mobile.vue')
  },
  {
    path: '/dvCanvas',
    name: 'dv',
    hidden: true,
    meta: {},
    component: () => import('@/views/mobile/panel/NotSupport.vue')
  },
  {
    path: '/DashboardEmpty',
    name: 'DashboardEmpty',
    hidden: true,
    meta: {},
    component: () => import('@/views/mobile/panel/DashboardEmpty.vue')
  },
  {
    path: '/preview',
    name: 'preview',
    hidden: true,
    meta: {},
    component: () => import('@/views/data-visualization/PreviewCanvasMobile.vue')
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes: routes as RouteRecordRaw[]
})

export const setupRouter = (app: App<Element>) => {
  app.use(router)
}

export default router
