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
import { useAppStoreWithOut } from '@/store/modules/app'
import { useLoading } from '@/hooks/web/useLoading'
import { h } from 'vue'

const permissionStore = usePermissionStoreWithOut()
const { wsCache } = useCache()
const userStore = useUserStoreWithOut()
const linkStore = useLinkStoreWithOut()
const appStore = useAppStoreWithOut()
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
      border-radius: 6px;
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

// 外观与 xpack 有效性属于全局配置：首次导航加载即可，无需每次导航重复请求
let appearanceLoaded = false
let xpackChecked = false

// 首次导航并发去重：同一登录会话内复用进行中的初始化 Promise；失败后清理以便重试
let initPermissionPromise: Promise<void> | undefined
// 会话代次：登出/登录失效时递增，使进行中的旧任务不再回写完成标志
let initGeneration = 0

const initializePermissionState = async (generation: number) => {
  const isCurrent = () => generation === initGeneration
  const roleRouters = (await getRoleRouters()) || []
  // 应用响应前校验会话：旧会话的响应不得覆盖新会话
  if (!isCurrent()) return
  const routers: any[] = roleRouters as AppCustomRouteRecordRaw[]
  routers.forEach(item => (item['top'] = true))
  await permissionStore.generateRoutes(routers as AppCustomRouteRecordRaw[])
  // 生成路由会写入 Store，之后再校验一次，避免为过期会话初始化交互状态
  if (!isCurrent()) return
  await interactiveStore.initInteractive(true, isCurrent)
  // 交互初始化完成后再校验，会话已变则不回写完成标志
  if (!isCurrent()) return
  // 全部初始化成功后再置位，避免中途失败被误判为已完成
  permissionStore.setIsAddRouters(true)
}

const ensurePermissionInitialized = (): Promise<void> => {
  if (permissionStore.getIsAddRouters) return Promise.resolve()
  if (initPermissionPromise) return initPermissionPromise
  const generation = initGeneration
  const settle = () => {
    // 仅当仍属于当前会话时清理共享 Promise，避免旧任务清掉新会话的进行中任务
    if (generation === initGeneration) initPermissionPromise = undefined
  }
  const promise = initializePermissionState(generation).then(
    () => settle(),
    error => {
      settle()
      throw error
    }
  )
  initPermissionPromise = promise
  return promise
}

// 未认证导航（登出、登录失效或登录页）标志新会话边界：作废旧初始化状态与进行中的任务
const invalidatePermissionInitialization = () => {
  initGeneration++
  initPermissionPromise = undefined
  if (permissionStore.getIsAddRouters) permissionStore.setIsAddRouters(false)
}

router.beforeEach(async (to, _, next) => {
  if (to.path.startsWith('/de-link/')) {
    open()
  }
  start()
  loadStart()
  const appearanceStore = useAppearanceStoreWithOut()
  if (!appearanceLoaded) {
    await appearanceStore.setAppearance()
    appearanceLoaded = true
  }
  if (!xpackChecked) {
    await appStore.setXpackValid()
    xpackChecked = true
  }
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
      if (!permissionStore.getIsAddRouters) {
        await ensurePermissionInitialized()
      }
      next()
    }
  } else {
    invalidatePermissionInitialization()
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
