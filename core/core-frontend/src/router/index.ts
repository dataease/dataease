import { createRouter, createWebHashHistory } from 'vue-router'
import type { App } from 'vue'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      redirect: '/home'
    },
    {
      path: '/',
      component: () => import('@/layout/index.vue'),
      children: [
        {
          path: 'home',
          component: () => import('@/view/home/index.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'system',
          component: () => import('@/view/system/index.vue'),
          meta: { requiresAuth: false }
        }
      ]
    }
  ]
})

export const setupRouter = (app: App<Element>) => {
  app.use(router)
}

export default router
