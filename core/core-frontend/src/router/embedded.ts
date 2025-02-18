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
  console.log('hash', hash)
  rawInstall(app)
  console.log('hash1', window.location.hash)
  setTimeout(() => {
    console.log('hash2', window.location.hash)
  }, 300)
  window.location.hash = hash
  console.log('hash3', window.location.hash)
}

export const setupRouter = (app: App<Element>) => {
  app.use(router)
}

export default router
