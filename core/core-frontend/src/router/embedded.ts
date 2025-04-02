import { createRouter, createWebHashHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import type { App } from 'vue'

export const routes: AppRouteRecordRaw[] = []
const router = createRouter({
  history: createWebHashHistory(),
  routes: routes as RouteRecordRaw[]
})

const rawInstall = router.install
router.install = app => {
  const hash = window.location.hash
  rawInstall(app)
  setTimeout(() => {
    window.location.hash = hash
  }, 100)
}

export const setupRouter = (app: App<Element>) => {
  app.use(router)
}

export default router
