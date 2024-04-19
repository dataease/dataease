import { createRouter, createWebHashHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import type { App } from 'vue'

export const routes: AppRouteRecordRaw[] = [
  {
    path: '/dvCanvas',
    name: 'dvCanvas',
    hidden: true,
    meta: {},
    component: () => import('@/views/data-visualization/index.vue')
  },
  {
    path: '/dashboard',
    name: 'dashboard',
    hidden: true,
    meta: {},
    component: () => import('@/views/dashboard/index.vue')
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
