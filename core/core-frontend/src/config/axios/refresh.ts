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

const expTimeConstants = 90000

const isExpired = () => {
  const exp = wsCache.get('user.exp')
  if (!exp) {
    return false
  }
  const time = wsCache.get('user.time')
  if (!time) {
    return exp - Date.now() < expConstants
  }
  return Date.now() - time > expTimeConstants
}

const takeCachedRequests = () => {
  const requests = [...requestStore.getRequestList]
  requestStore.cleanCacheRequest()
  setRefreshStatus(false)
  return requests
}

const delayExecute = (token: string) => {
  takeCachedRequests().forEach(request => request.resolve(token))
}

const rejectCachedRequests = (error: unknown) => {
  takeCachedRequests().forEach(request => request.reject(error))
}

const getRefreshStatus = () => {
  return wsCache.get('de-global-refresh') || false
}
const setRefreshStatus = (status: boolean) => {
  wsCache.set('de-global-refresh', status, { exp: 5 })
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
    if (expired && !config.url.includes(refreshUrl)) {
      const retry = new Promise<typeof config>((resolve, reject) => {
        requestStore.addCacheRequest({
          resolve: token => {
            config.headers['X-DE-TOKEN'] = token
            resolve(config)
          },
          reject
        })
      })
      if (!getRefreshStatus()) {
        setRefreshStatus(true)
        refreshApi(Date.now())
          .then(res => {
            if (res?.data?.token) {
              userStore.setToken(res.data.token)
              userStore.setExp(res.data.exp)
              userStore.setTime(Date.now())
              config.headers['X-DE-TOKEN'] = res.data.token
              delayExecute(res.data.token)
            } else {
              rejectCachedRequests(new Error('Refresh response is missing a token'))
            }
          })
          .catch(error => {
            rejectCachedRequests(error)
          })
      }
      return retry
    } else {
      return config
    }
  }
  return config
}
