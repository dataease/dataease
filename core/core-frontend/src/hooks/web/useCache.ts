// utils/cache.ts
import WebStorageCache from 'web-storage-cache'

type CacheType = 'localStorage' | 'sessionStorage'

const getPathPrefix = () => {
  const pathname = window.location.pathname
  const match = pathname.match(/^\/([^\/]+)/)
  return match ? `${match[1]}_` : 'de_v2_'
}

export const useCache = (type: CacheType = 'localStorage') => {
  const originalCache = new WebStorageCache({ storage: type })
  const prefix = getPathPrefix()

  const shouldAddPrefix = (key: string): boolean => {
    return key.startsWith('user.')
  }

  const processKey = (key: string): string => {
    return shouldAddPrefix(key) ? `${prefix}${key}` : key
  }

  const methodsNeedKeyPrefix = new Set(['get', 'delete', 'touch', 'add', 'replace'])

  const wrappedCache = new Proxy(originalCache, {
    get(target, prop, receiver) {
      const originalMethod = target[prop as keyof typeof target]

      if (typeof originalMethod !== 'function') {
        return originalMethod
      }

      if (methodsNeedKeyPrefix.has(prop as string)) {
        return function (this: any, key: string, ...args: any[]) {
          const processedKey = processKey(key)
          return originalMethod.call(target, processedKey, ...args)
        }
      }

      if (prop === 'set') {
        return function (this: any, key: string, value: any, ...args: any[]) {
          const processedKey = processKey(key)
          return originalMethod.call(target, processedKey, value, ...args)
        }
      }
      return originalMethod.bind(target)
    }
  })

  return {
    wsCache: wrappedCache
  }
}
