import { useCache } from '@/hooks/web/useCache'
import { refreshApi } from '@/api/login'
import { useUserStoreWithOut } from '@/store/modules/user'
import { useRequestStoreWithOut } from '@/store/modules/request'

import { isLink } from '@/utils/utils'
const { wsCache } = useCache()
const userStore = useUserStoreWithOut()
const requestStore = useRequestStoreWithOut()
const refreshUrl = '/login/refresh'

const expConstants = 10000

const isExpired = () => {
  const exp = wsCache.get('user.exp')
  if (!exp) {
    return false
  }
  return exp - new Date().getTime() < expConstants
}

const delayExecute = (token: string) => {
  const cachedRequestList = requestStore.getRequestList
  cachedRequestList.forEach(cb => {
    cb(token)
  })
  requestStore.cleanCacheRequest()
  /* cachedRequestList.forEach(cb => {
    cb(token)
  })
  cachedRequestList = [] */
}

const getRefreshStatus = () => {
  return wsCache.get('de-global-refresh') || false
}
const setRefreshStatus = (status: boolean) => {
  wsCache.set('de-global-refresh', status, { exp: 5 })
}

const cacheRequest = cb => {
  requestStore.addCacheRequest(cb)
  // cachedRequestList.push(cb)
}

export const configHandler = config => {
  const desktop = wsCache.get('app.desktop')
  if (desktop) {
    return config
  }
  if (isLink()) {
    return config
  }
  if (wsCache.get('user.token')) {
    config.headers['X-DE-TOKEN'] = wsCache.get('user.token')
    const expired = isExpired()
    if (expired && config.url !== refreshUrl) {
      if (!getRefreshStatus()) {
        setRefreshStatus(true)
        refreshApi()
          .then(res => {
            userStore.setToken(res.data.token)
            userStore.setExp(res.data.exp)
            config.headers['X-DE-TOKEN'] = res.data.token
            delayExecute(res.data.token)
          })
          .catch(e => {
            console.error(e)
          })
          .finally(() => {
            setRefreshStatus(false)
          })
      }
      const retry = new Promise(resolve => {
        cacheRequest(token => {
          config.headers['X-DE-TOKEN'] = token
          resolve(config)
        })
      })
      return retry
    } else {
      return config
    }
  }
  return config
}
