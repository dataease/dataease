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
import { useLoading } from '@/hooks/web/useLoading'
import { h } from 'vue'

const appearanceStore = useAppearanceStoreWithOut()
const permissionStore = usePermissionStoreWithOut()
const { wsCache } = useCache()
const userStore = useUserStoreWithOut()
const linkStore = useLinkStoreWithOut()
// 简化版骨架屏
const skeletonContent = h(
  'div',
  {
    style: {
      padding: '10px',
      width: '100%',
      height: '100%'
    }
  },
  [
    h(
      'style',
      {},
      `
    @keyframes shimmer {
      0% { background-position: -200% 0; }
      100% { background-position: 200% 0; }
    }
    .skeleton-bar {
      height: 16px;
      margin-bottom: 12px;
      border-radius: 4px;
      background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
      background-size: 200% 100%;
      animation: shimmer 1.5s infinite;
    }
  `
    ),
    h('div', { class: 'skeleton-bar', style: { width: '100%', height: '24%' } }),
    h('div', { class: 'skeleton-bar', style: { width: '100%', height: '24%' } }),
    h('div', { class: 'skeleton-bar', style: { width: '100%', height: '24%' } }),
    h('div', { class: 'skeleton-bar', style: { width: '100%', height: '24%' } })
  ]
)

const { open } = useLoading('skeleton-loading', skeletonContent as any)
const { start, done } = useNProgress()
const interactiveStore = interactiveStoreWithOut()

const { loadStart, loadDone } = usePageLoading()
const whiteList = ['/login', '/panel', '/DashboardEmpty', '/preview'] // 不重定向白名单

router.beforeEach(async (to, _, next) => {
  if (to.path.startsWith('/de-link/')) {
    open()
  }
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
