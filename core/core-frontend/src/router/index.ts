import { createRouter, createWebHashHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import type { App } from 'vue'

export const routes: AppRouteRecordRaw[] = [
  {
    path: '/',
    name: 'index',
    redirect: '/home',
    component: () => import('@/layout/index.vue'),
    hidden: true,
    meta: {},
    children: [
      {
        path: 'home',
        name: 'home',
        hidden: true,
        component: () => import('@/views/home/index.vue'),
        meta: { hidden: true }
      }
    ]
  },
  {
    path: '/login',
    name: 'login',
    hidden: true,
    meta: {},
    component: () => import('@/views/login/index.vue')
  },
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
  },
  {
    path: '/chart',
    name: 'chart',
    hidden: true,
    meta: {},
    component: () => import('@/views/chart/index.vue')
  },
  {
    path: '/previewShow',
    name: 'previewShow',
    hidden: true,
    meta: {},
    component: () => import('@/views/data-visualization/PreviewShow.vue')
  },
  {
    path: '/DeResourceTree',
    name: 'DeResourceTree',
    hidden: true,
    meta: {},
    component: () => import('@/views/common/DeResourceTree.vue')
  },
  {
    path: '/dataset',
    name: 'dataset',
    hidden: true,
    meta: {},
    component: () => import('@/views/visualized/data/dataset/form/index.vue')
  },
  {
    path: '/preview',
    name: 'preview',
    hidden: true,
    meta: {},
    component: () => import('@/views/data-visualization/PreviewCanvas.vue')
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
