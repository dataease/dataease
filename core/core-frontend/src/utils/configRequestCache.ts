import { useCache } from '@/hooks/web/useCache'
import { useEmbedded } from '@/store/modules/embedded'
import { useLinkStoreWithOut } from '@/store/modules/link'

const clearers = new Set<() => void>()

// 与请求拦截器使用的身份保持一致；凭据仅用于内存比较，不写入持久化缓存。
const getScope = () => {
  const { wsCache } = useCache()
  const embedded = useEmbedded()
  const link = useLinkStoreWithOut()
  return JSON.stringify([
    wsCache.get('user.token'),
    wsCache.get('user.uid'),
    wsCache.get('user.oid'),
    wsCache.get('user.proxyInfo'),
    wsCache.get('user.language'),
    wsCache.get('app.desktop'),
    embedded.baseUrl,
    embedded.token,
    link.getLinkToken
  ])
}

export const clearConfigRequestCaches = () => {
  clearers.forEach(clear => clear())
}

export const createConfigRequestCache = <T>(load: () => Promise<T>) => {
  let current:
    | { scope: string; promise: Promise<T>; refresh: boolean; pending: boolean }
    | undefined
  const clear = () => {
    current = undefined
  }
  clearers.add(clear)
  const get = (force = false): Promise<T> => {
    const scope = getScope()
    if (force && current?.scope === scope && current.refresh && current.pending) {
      return current.promise
    }
    if (force) clear()
    if (current?.scope === scope) return current.promise
    const entry = { scope, promise: undefined as Promise<T>, refresh: force, pending: true }
    current = entry
    entry.promise = Promise.resolve()
      .then(load)
      .then(
        value => {
          entry.pending = false
          // 身份切换或主动刷新后，旧响应不能回写当前 Store。
          if (current !== entry || getScope() !== scope) return get()
          return value
        },
        error => {
          entry.pending = false
          if (current !== entry || getScope() !== scope) return get()
          clear()
          throw error
        }
      )
    return entry.promise
  }
  return { get, clear }
}
