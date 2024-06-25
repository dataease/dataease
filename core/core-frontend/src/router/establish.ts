import { isExternal } from '@/utils/validate'
import { cloneDeep } from 'lodash'
import { XpackComponent } from '@/components/plugin'
const modules = import.meta.glob('../views/**/*.vue')
export const Layout = () => import('@/layout/index.vue')
const xpackComName = 'components/plugin'
export const LayoutTransition = () => import('@/layout/components/LayoutTransition.vue')
// 后端控制路由生成
export const generateRoutesFn2 = (routes: AppCustomRouteRecordRaw[]): AppRouteRecordRaw[] => {
  const res: AppRouteRecordRaw[] = []

  for (const router of routes) {
    let route = { ...router }

    if (route.top && route.inLayout) {
      route = decorate(route)
    }

    if (route.plugin) {
      const jsName = route.component
      route.component = xpackComName
      route.props = {
        jsname: jsName,
        inLayout: route.inLayout
      }
    }

    const data: AppRouteRecordRaw = {
      path: route.path,
      hidden: route.hidden,
      name: route.name,
      redirect: route.redirect,
      meta: route.meta,
      props: route.props as Recordable
    }

    if (route.component) {
      let comModule = null
      if (route.component === xpackComName) {
        comModule = XpackComponent
      } else if (!route.component.startsWith('Layout')) {
        comModule = modules[`../views/${route.component}/index.vue`]
      }

      if (route.component === 'Layout') {
        data.component = Layout
      } else if (route.component === 'LayoutTransition') {
        data.component = LayoutTransition
      } else if (!comModule) {
      } else {
        data.component = comModule
      }
    }
    if (route.children) {
      data.children = generateRoutesFn2(route.children)
    }
    res.push(data as AppRouteRecordRaw)
  }

  return res
}

export const formatRoute = (arr: AppCustomRouteRecordRaw[]): AppCustomRouteRecordRaw[] => {
  return arr.map(ele => {
    const router = cloneDeep(ele)
    const { path, children = [] } = router

    if (children?.length === 1 && router.path !== '/data') {
      const [route] = children
      router.path = `${path}/${route.path}`
      router.children = []
    }
    return router
  })
}

// 包装一层父级目录
export const decorate = (router: AppCustomRouteRecordRaw): AppCustomRouteRecordRaw => {
  const { path, meta, children = [], inLayout, hidden } = router
  const parent = {
    path,
    meta,
    inLayout,
    component: 'Layout',
    children,
    hidden
  }

  const current = { ...router }
  current.inLayout = false
  if (!children?.length) {
    current.path = 'index'
    parent.children = [current]
  }
  return parent
}

export const resolvePath = (item: AppRouteRecordRaw) => {
  // 如果是首页，就返回重定向路由
  if (item.path === '/') {
    return item.redirect as string
  }

  // 如果有子项，默认跳转第一个子项路由
  let path = ''
  /**
   * item 路由子项
   * parent 路由父项
   */
  const getDefaultPath = (ele, parent?) => {
    // 如果path是个外部链接（不建议），直接返回链接，存在个问题：如果是外部链接点击跳转后当前页内容还是上一个路由内容
    if (isExternal(ele.path)) {
      path = ele.path
      return
    }
    // 第一次需要父项路由拼接，所以只是第一个传parent
    if (parent) {
      path += parent.path + '/' + ele.path
    } else {
      path += '/' + ele.path
    }
    // 如果还有子项，继续递归
    if (ele.children) {
      getDefaultPath(ele.children[0])
    }
  }

  if (item.children) {
    getDefaultPath(item.children[0], item)
    return path
  }

  return item.path
}
