import { createRouter, createWebHashHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import type { App } from 'vue'

export const routes: AppRouteRecordRaw[] = []

const router = createRouter({
  history: createWebHashHistory(),
  routes: routes as RouteRecordRaw[]
})
router.push = () => {
  console.log('push')
}
router.replace = () => {
  console.log('replace')
}

export const setupRouter = (app: App<Element>) => {
  app.use(router)
}

export default router
