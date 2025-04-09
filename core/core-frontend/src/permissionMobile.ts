import router from './router/mobile'
import { useUserStoreWithOut } from '@/store/modules/user'
import { useNProgress } from '@/hooks/web/useNProgress'
import { usePageLoading } from '@/hooks/web/usePageLoading'
import { useCache } from '@/hooks/web/useCache'
import { getRoleRouters } from '@/api/common'
import { usePermissionStoreWithOut } from '@/store/modules/permission'
import { interactiveStoreWithOut } from '@/store/modules/interactive'
import { useAppearanceStoreWithOut } from '@/store/modules/appearance'
import { useLinkStoreWithOut } from '@/store/modules/link'

const appearanceStore = useAppearanceStoreWithOut()
const permissionStore = usePermissionStoreWithOut()
const { wsCache } = useCache()
const userStore = useUserStoreWithOut()
const linkStore = useLinkStoreWithOut()

const { start, done } = useNProgress()
const interactiveStore = interactiveStoreWithOut()

const { loadStart, loadDone } = usePageLoading()
const whiteList = ['/login', '/panel', '/DashboardEmpty', '/preview'] // 不重定向白名单

router.beforeEach(async (to, _, next) => {
  start()
  loadStart()
  await appearanceStore.setAppearance()
  if (to.name === 'link') {
    next()
  } else if (wsCache.get('user.token')) {
    linkStore.setLinkToken('')
    if (!userStore.getUid) {
      await userStore.setUser()
    }
    if (to.path === '/login') {
      next({ path: '/index' })
    } else {
      const roleRouters = (await getRoleRouters()) || []
      const routers: any[] = roleRouters as AppCustomRouteRecordRaw[]
      routers.forEach(item => (item['top'] = true))
      await permissionStore.generateRoutes(routers as AppCustomRouteRecordRaw[])
      permissionStore.setIsAddRouters(true)
      await interactiveStore.initInteractive(true)
      next()
    }
  } else {
    if (whiteList.includes(to.path) || to.name === 'link') {
      next()
    } else {
      linkStore.setLinkToken('')
      next('/login') // 否则全部重定向到登录页
    }
  }
})

router.afterEach(() => {
  done()
  loadDone()
})
