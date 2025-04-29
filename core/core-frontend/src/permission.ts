import router from './router'
import { useUserStoreWithOut } from '@/store/modules/user'
import { useAppStoreWithOut } from '@/store/modules/app'
import type { RouteRecordRaw } from 'vue-router'
import { getDefaultSettings } from '@/api/common'
import { useNProgress } from '@/hooks/web/useNProgress'
import { usePermissionStoreWithOut, pathValid, getFirstAuthMenu } from '@/store/modules/permission'
import { usePageLoading } from '@/hooks/web/usePageLoading'
import { getRoleRouters } from '@/api/common'
import { useCache } from '@/hooks/web/useCache'
import { isMobile, checkPlatform, isLarkPlatform, isPlatformClient } from '@/utils/utils'
import { interactiveStoreWithOut } from '@/store/modules/interactive'
import { useAppearanceStoreWithOut } from '@/store/modules/appearance'
import { useEmbedded } from '@/store/modules/embedded'
const appearanceStore = useAppearanceStoreWithOut()
const { wsCache } = useCache()
const permissionStore = usePermissionStoreWithOut()
const interactiveStore = interactiveStoreWithOut()
const userStore = useUserStoreWithOut()
const appStore = useAppStoreWithOut()

const { start, done } = useNProgress()

const { loadStart, loadDone } = usePageLoading()

const whiteList = ['/login', '/de-link', '/chart-view', '/admin-login', '/401'] // 不重定向白名单
const embeddedWindowWhiteList = ['/dvCanvas', '/dashboard', '/preview', '/dataset-embedded-form']
const embeddedRouteWhiteList = ['/dataset-embedded', '/dataset-form', '/dataset-embedded-form']
router.beforeEach(async (to, from, next) => {
  start()
  loadStart()
  const platform = checkPlatform()
  let isDesktop = wsCache.get('app.desktop')
  if (isDesktop === null) {
    await appStore.setAppModel()
    isDesktop = appStore.getDesktop
  }
  if (isMobile() && !['/chart-view'].includes(to.path)) {
    done()
    loadDone()
    if (to.name === 'link') {
      let linkQuery = ''
      if (Object.keys(to.query)) {
        const tempQuery = Object.keys(to.query)
          .map(key => key + '=' + to.query[key])
          .join('&')
        if (tempQuery) {
          linkQuery = '?' + tempQuery
        }
      }
      let pathname = window.location.pathname
      pathname = pathname.replace('casbi/', '')
      pathname = pathname.replace('oidc/', '')
      pathname = pathname.substring(0, pathname.length - 1)
      const prefix = window.origin + pathname
      let toPath = to.fullPath
      if (toPath.includes('?')) {
        toPath = to.fullPath.substring(0, to.fullPath.lastIndexOf('?'))
      }
      window.location.href = prefix + '/mobile.html#' + toPath + linkQuery
    } else if (
      wsCache.get('user.token') ||
      isDesktop ||
      (!isPlatformClient() && !isLarkPlatform())
    ) {
      let pathname = window.location.pathname
      pathname = pathname.substring(0, pathname.length - 1)
      let url = window.origin + pathname + '/mobile.html#/index'
      if (window.location.search) {
        url += window.location.search
      }
      window.location.href = url
    }
  }
  await appearanceStore.setAppearance()
  await appearanceStore.setFontList()
  const defaultSort = await getDefaultSettings()
  wsCache.set('TreeSort-backend', defaultSort['basic.defaultSort'] ?? '1')
  wsCache.set('open-backend', defaultSort['basic.defaultOpen'] ?? '0')
  if ((wsCache.get('user.token') || isDesktop) && !to.path.startsWith('/de-link/')) {
    if (!userStore.getUid) {
      await userStore.setUser()
    }
    if (to.path === '/login') {
      next({ path: '/workbranch/index' })
    } else {
      permissionStore.setCurrentPath(to.path)
      if (permissionStore.getIsAddRouters) {
        let str = ''
        if (((from.query.redirect as string) || '?').split('?')[0] === to.path) {
          str = ((window.location.hash as string) || '?').split('?').reverse()[0]
          if (str.includes('redirect=')) {
            str = ''
          }
        }
        if (str) {
          to.fullPath += '?' + str
          to.query = str.split('&').reduce((pre, itx) => {
            const [key, val] = itx.split('=')
            pre[key] = val
            return pre
          }, {})
        }
        if (!pathValid(to.path) && to.path !== '/404' && !to.path.startsWith('/de-link')) {
          const firstPath = getFirstAuthMenu()
          next({ path: firstPath || '/404' })
          return
        }
        next()
        return
      }

      let roleRouters = (await getRoleRouters()) || []
      if (isDesktop) {
        roleRouters = roleRouters.filter(item => item.name !== 'system')
      }
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
      await interactiveStore.initInteractive(true)

      if (!pathValid(to.path) && to.path !== '/404' && !to.path.startsWith('/de-link')) {
        const firstPath = getFirstAuthMenu()
        next({ path: firstPath || '/404' })
        return
      }
      next(nextData)
    }
  } else {
    const embeddedStore = useEmbedded()
    if (
      embeddedStore.getToken &&
      appStore.getIsIframe &&
      embeddedRouteWhiteList.includes(to.path)
    ) {
      if (to.path.includes('/dataset-form')) {
        next({ path: '/dataset-embedded-form', query: to.query })
        return
      }
      permissionStore.setCurrentPath(to.path)
      next()
    } else if (
      (!platform && embeddedWindowWhiteList.includes(to.path)) ||
      whiteList.includes(to.path) ||
      to.path.startsWith('/de-link/')
    ) {
      await appearanceStore.setFontList()
      permissionStore.setCurrentPath(to.path)
      next()
    } else {
      next(`/login?redirect=${to.fullPath || to.path}`) // 否则全部重定向到登录页
    }
  }
})

router.afterEach(() => {
  done()
  loadDone()
})
