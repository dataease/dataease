import router from './router'
import { useAppStoreWithOut } from '@/store/modules/app'
import { useCache } from '@/hooks/web/useCache'
import type { RouteRecordRaw } from 'vue-router'
import { useNProgress } from '@/hooks/web/useNProgress'
import { usePermissionStoreWithOut } from '@/store/modules/permission'
import { usePageLoading } from '@/hooks/web/usePageLoading'
import { getRoleRouters } from '@/api/common'

const permissionStore = usePermissionStoreWithOut()

const appStore = useAppStoreWithOut()

const { wsCache } = useCache()

const { start, done } = useNProgress()

const { loadStart, loadDone } = usePageLoading()

const whiteList = ['/login'] // 不重定向白名单

router.beforeEach(async (to, from, next) => {
  start()
  loadStart()

  if (wsCache.get(appStore.getToken)) {
    if (to.path === '/login') {
      next({ path: '/home/index' })
    } else {
      if (permissionStore.getIsAddRouters) {
        next()
        return
      }

      const roleRouters = (await getRoleRouters()) || []
      const routers: any[] = roleRouters as AppCustomRouteRecordRaw[]
      routers.forEach(item => (item['top'] = true))
      await permissionStore.generateRoutes(routers as AppCustomRouteRecordRaw[])

      permissionStore.getAddRouters.forEach(route => {
        router.addRoute(route as unknown as RouteRecordRaw) // 动态添加可访问路由表
      })

      const redirectPath = from.query.redirect || to.path
      const redirect = decodeURIComponent(redirectPath as string)
      const nextData = to.path === redirect ? { ...to, replace: true } : { path: redirect }
      permissionStore.setIsAddRouters(true)
      next(nextData)
    }
  } else {
    if (whiteList.indexOf(to.path) !== -1) {
      next()
    } else {
      next(`/login?redirect=${to.path}`) // 否则全部重定向到登录页
    }
  }
})

router.afterEach(() => {
  done()
  loadDone()
})
