import { useUserStoreWithOut } from '@/store/modules/user'
import router from '@/router'
import { usePermissionStoreWithOut } from '@/store/modules/permission'
import { interactiveStoreWithOut } from '@/store/modules/interactive'
import { useCache } from '@/hooks/web/useCache'
import request from '@/config/axios'

const { wsCache } = useCache()
const permissionStore = usePermissionStoreWithOut()
const userStore = useUserStoreWithOut()
const interactiveStore = interactiveStoreWithOut()

export const logoutHandler = (justClean?: boolean) => {
  const idToken = wsCache.get('oauth2-id-token')
  if (idToken) {
    request.get({ url: `/oauth2/logout/${idToken}` }).finally(() => {
      wsCache.delete('oauth2-id-token')
    })
  }
  userStore.clear()
  userStore.$reset()
  permissionStore.clear()
  permissionStore.$reset()
  interactiveStore.clear()
  interactiveStore.$reset()
  removeCache()
  let queryRedirectPath = '/workbranch/index'
  // 如果redirect参数中有值
  if (router.currentRoute.value.fullPath) {
    queryRedirectPath = router.currentRoute.value.fullPath as string
  }
  let pathname = window.location.pathname
  if (pathname) {
    if (pathname.includes('oidcbi/') || pathname.includes('casbi/')) {
      pathname = pathname.replace('oidcbi/', '')
      pathname = pathname.replace('casbi/', '')
    }
    pathname = pathname.substring(0, pathname.length - 1)
  }
  if (wsCache.get('out_auth_platform') === 'cas') {
    const uri = window.location.href
    window.location.href = pathname + '/casbi/cas/logout?service=' + uri
    return
  }
  if (wsCache.get('out_auth_platform') === 'oidc') {
    window.location.href = pathname + '/oidcbi/oidc/logout'
    return
  }
  if (wsCache.get('custom_auth_logout_url')) {
    window.location.href = wsCache.get('custom_auth_logout_url')
  }
  router.push(justClean ? queryRedirectPath : `/login?redirect=${queryRedirectPath}`)
}

const removeCache = () => {
  const keys = Object.keys(wsCache['storage'])
  keys.forEach(key => {
    if (
      key.startsWith('de-plugin-') ||
      key === 'de-platform-client' ||
      key === 'pwd-validity-period'
    ) {
      wsCache.delete(key)
    }
  })
}
