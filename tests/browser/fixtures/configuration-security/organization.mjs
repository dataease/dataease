// Exercise the same platform-admin organization selection as the product UI.
export async function enterOrganization(page) {
  const result = await page.evaluate(async () => {
    const { default: request } = await import('/src/config/axios/index.ts')
    const { ensureDekey, rsaEncryp } = await import('/src/utils/encryption.ts')
    const { useUserStoreWithOut } = await import('/src/store/modules/user.ts')
    const tree = (await request.get({ url: '/org/page/tree' })).data
    const flatten = (items) =>
      (items || []).flatMap((item) => [item, ...flatten(item.children)])
    const org = flatten(Array.isArray(tree) ? tree : tree?.records || []).find(
      (item) => Number(item.id) > 0
    )
    if (!org)
      throw new Error(
        'No existing organization can be selected by the test account'
      )
    await ensureDekey()
    useUserStoreWithOut().setProxyInfo({
      proxy: true,
      proxyOid: String(org.id),
      proxySecret: rsaEncryp(String(org.id))
    })
    const menus = (await request.get({ url: '/menu/query' })).data
    const paths = (items, prefix = '') =>
      (items || []).flatMap((item) => {
        const full = item.path?.startsWith('/')
          ? item.path
          : `${prefix}/${item.path || ''}`
        return [
          { path: full, component: item.component },
          ...paths(item.children, full)
        ]
      })
    return { menuCount: menus.length, routes: paths(menus) }
  })
  await page.reload()
  await page.waitForFunction(
    () => {
      const router =
        document.querySelector('#app')?.__vue_app__?.config.globalProperties
          .$router
      return router
        ?.getRoutes()
        .some((route) => route.path === '/data/datasource')
    },
    null,
    { timeout: 30000 }
  )
  await page.evaluate(async () => {
    await document
      .querySelector('#app')
      .__vue_app__.config.globalProperties.$router.isReady()
  })
  return result
}
