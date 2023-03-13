const modules = import.meta.glob('../views/**/*.vue')
export const Layout = () => import('@/layout/index.vue')
const layoutPath = []
// 后端控制路由生成
export const generateRoutesFn2 = (routes: AppCustomRouteRecordRaw[]): AppRouteRecordRaw[] => {
  const res: AppRouteRecordRaw[] = []

  for (const route of routes) {
    const data: AppRouteRecordRaw = {
      path: route.path,
      name: route.name,
      redirect: route.redirect,
      meta: route.meta
    }
    if (route.component) {
      const comModule = modules[`../${route.component}.vue`]

      if (!comModule) {
        return
      } else {
        data.component = layoutPath.includes(route.path) ? Layout : comModule
      }
    }
    if (route.children) {
      data.children = generateRoutesFn2(route.children)
    }
    res.push(data as AppRouteRecordRaw)
  }
  return res
}
