import { createRouter, createWebHashHistory } from 'vue-router_2'
import type { RouteRecordRaw } from 'vue-router_2'
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
