import { createRouter, createWebHashHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import type { App } from 'vue'

export const routes: AppRouteRecordRaw[] = []
createRouter({
  history: createWebHashHistory(),
  routes: routes as RouteRecordRaw[]
})
const fn = () => {
  console.log('push')
}

const router = {
  install: () => {
    console.log('install')
  },
  currentRoute: fn,
  listening: true,
  addRoute: fn,
  removeRoute: fn,
  hasRoute: fn,
  getRoutes: fn,
  resolve: fn,
  options: fn,
  push: fn,
  replace: fn,
  go: fn,
  back: fn,
  forward: fn
}

export const setupRouter = (app: App<Element>) => {
  app.use(router)
}

export default router
