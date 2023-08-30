import { useCache } from '@/hooks/web/useCache'
import { platformLoginApi } from '@/api/login'
import { useUserStoreWithOut } from '@/store/modules/user'
import router from '@/router'
const { wsCache } = useCache()
const userStore = useUserStoreWithOut()

export const toPlatformPage = val => {
  const curOrigin = window.location.origin
  const curLocation = getCurLocation()
  if (val === 0) {
    wsCache.set('out_auth_platform', 'default')
  } else if (val === 1) {
    wsCache.set('out_auth_platform', 'ldap')
    window.location.href = curOrigin + '/ldapbi/#' + curLocation
  } else if (val === 2) {
    wsCache.set('out_auth_platform', 'oidc')
    window.location.href = curOrigin + '/oidcbi/#' + curLocation
  } else if (val === 3) {
    wsCache.set('out_auth_platform', 'cas')
    window.location.href = curOrigin + '/casbi/#' + curLocation
  }
  return
}

export const setLoginForm = state => {
  const platform = wsCache.get('out_auth_platform')
  state.loginForm.loginType = 0
  if (platform === 'default') {
    state.loginForm.loginType = 0
  }
  if (platform === 'ldap') {
    state.loginForm.loginType = 1
  }
  if (platform === 'oidc') {
    state.loginForm.loginType = 2
  }
  if (platform === 'cas') {
    state.loginForm.loginType = 3
  }
}

const platformLogin = origin => {
  platformLoginApi(origin).then(res => {
    const token = res.data
    userStore.setToken(token)
    const queryRedirectPath = getCurLocation()
    router.push({ path: queryRedirectPath })
  })
}

export const callback = () => {
  const pathname = window.location.pathname
  let origin = 0
  if (pathname.startsWith('/casbi')) {
    origin = 3
  }
  if (pathname.startsWith('/oidcbi')) {
    origin = 2
  }
  if (pathname.startsWith('/ldapbi')) {
    origin = 1
  }
  if (origin) {
    platformLogin(origin)
  }
}

const getCurLocation = () => {
  let queryRedirectPath = '/workbranch/index'
  if (router.currentRoute.value.query.redirect) {
    queryRedirectPath = router.currentRoute.value.query.redirect as string
  }
  return queryRedirectPath
}
