const modules = import.meta.glob('../views/**/*.vue')
export const Layout = () => import('@/layout/index.vue')
export const fullScreenRouters = [
  'XpackThemeForm',
  'system/datasource/DsForm',
  'dataset/Form',
  'DeAutoLogin'
]

// 后端控制路由生成
export const generateRoutesFn2 = (routes: AppCustomRouteRecordRaw[]): AppRouteRecordRaw[] => {
  const res: AppRouteRecordRaw[] = []

  for (const router of routes) {
    let route = { ...router }

    if (
      !fullScreenRouters.includes(route.component) &&
      route.type === 1 &&
      route.pid === 0 &&
      route.component &&
      route.component !== 'Layout'
    ) {
      route = decorate(route)
    }

    const data: AppRouteRecordRaw = {
      path: route.path,
      hidden: route.hidden,
      id: route.id,
      name: route.name,
      redirect: route.redirect,
      meta: route.meta
    }

    if (route.isPlugin) {
      const jsName = route.component
      route.component = 'system/plugin/Dynamic'
      route.props = {
        jsname: jsName,
        menuid: route.id,
        noLayout: route.noLayout
      }
    }

    if (route.component) {
      const comModule = modules[`../views/${route.component}.vue`]

      if (route.component === 'Layout') {
        data.component = Layout
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

// 后台设计时未考虑activeMenu字段 这里先前端处理一下
export const fillMeta = (router: AppCustomRouteRecordRaw) => {
  router.name.includes('system-user-form') && (router.meta.activeMenu = '/system/user')
  router.name.includes('system-role-form') && (router.meta.activeMenu = '/system/role')
  router.name.includes('system-dept-form') && (router.meta.activeMenu = '/system/dept')
}

// 包装一层父级目录
export const decorate = (router: AppCustomRouteRecordRaw): AppCustomRouteRecordRaw => {
  const parent = {
    id: router.id + 1000000,
    path: router.path,
    meta: router.meta,
    component: 'Layout',
    children: [],
    hidden: false
  }
  const current = { ...router }
  current.type = 1
  current.path = 'index'
  current.pid = parent.id
  parent.children = [current]
  if (router.hidden) {
    parent.hidden = router.hidden
  }

  return parent
}
