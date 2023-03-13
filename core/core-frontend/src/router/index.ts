import { createRouter, createWebHashHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import type { App } from 'vue'

export const routes: AppRouteRecordRaw[] = [
  {
    path: '/',
    name: 'index',
    redirect: '/home',
    component: () => import('@/layout/index.vue'),
    meta: { hidden: true },
    children: [
      {
        path: 'home',
        name: 'home',
        component: () => import('@/views/home/index.vue'),
        meta: { hidden: true }
      },
      {
        path: 'system',
        name: 'system',
        component: () => import('@/views/system/index.vue'),
        meta: { hidden: false }
      }
    ]
  },
  {
    path: '/login',
    name: 'login',
    meta: { hidden: true },
    component: () => import('@/views/login/index.vue')
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes: routes as RouteRecordRaw[]
})
export const resetRouter = (): void => {
  const resetWhiteNameList = ['Login']
  router.getRoutes().forEach(route => {
    const { name } = route
    if (name && !resetWhiteNameList.includes(name as string)) {
      router.hasRoute(name) && router.removeRoute(name)
    }
  })
}

export const setupRouter = (app: App<Element>) => {
  app.use(router)
}

export default router
