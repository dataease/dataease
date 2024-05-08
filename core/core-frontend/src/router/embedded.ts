import { createRouter, createWebHashHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import type { App } from 'vue'

export const routes: AppRouteRecordRaw[] = []

const router = createRouter({
  history: createWebHashHistory(),
  routes: routes as RouteRecordRaw[]
})

export const setupRouter = (app: App<Element>) => {
  app.use(router)
}

export default router
