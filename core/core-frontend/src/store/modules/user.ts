import { defineStore } from 'pinia'
import { store } from '../index'
import { useCache } from '@/hooks/web/useCache'
import { useLocaleStoreWithOut } from './locale'
import { useLocale } from '@/hooks/web/useLocale'
const { wsCache } = useCache()
const { changeLocale } = useLocale()

interface UserState {
  token: string
  uid: string
  name: string
  oid: string
  language: string
  exp: number
  hasXapck: boolean
  time: number
  proxyInfo: {
    proxy: boolean
    proxyOid: string | null
    proxySecret: string | null
  }
}

export const userStore = defineStore('user', {
  state: (): UserState => {
    return {
      token: null,
      hasXapck: false,
      uid: null,
      name: null,
      oid: null,
      language: 'zh-CN',
      exp: null,
      time: null,
      proxyInfo: {
        proxy: false,
        proxyOid: null,
        proxySecret: null
      }
    }
  },
  getters: {
    getToken(): string {
      return this.token
    },
    getUid(): string {
      return this.uid
    },
    getName(): string {
      return this.name
    },
    getOid(): string {
      return this.oid
    },
    getLanguage(): string {
      return this.language
    },
    getExp(): number {
      return this.exp
    },
    getTime(): number {
      return this.time
    },
    getXpack(): boolean {
      return this.hasXapck
    },
    getProxyInfo(): { proxy: boolean; proxyOid: string | null; proxySecret: string | null } {
      return this.proxyInfo
    }
  },
  actions: {
    async setUser() {
      const user = await import('@/api/user')
      const plugin = await import('@/api/plugin')
      const res = await user.userInfo()
      const data = res.data
      data.token = wsCache.get('user.token')
      data.exp = wsCache.get('user.exp')
      data.time = wsCache.get('user.time')
      const keys: string[] = ['token', 'uid', 'name', 'oid', 'language', 'exp', 'time']

      keys.forEach(key => {
        const dkey = key === 'uid' ? 'id' : key
        this[key] = data[dkey]
        wsCache.set('user.' + key, this[key])
      })
      const cachedProxyInfo = wsCache.get('user.proxyInfo')
      if (cachedProxyInfo) {
        this.proxyInfo = cachedProxyInfo
      }
      const locale = useLocaleStoreWithOut()
      if (locale.getCurrentLocale?.lang !== this.language && !window.DataEaseBi) {
        window.location.reload()
      }
      const hasXapck = await plugin.xpackModelApi()
      this.setXpack(hasXapck.data || false)
      this.setLanguage(this.language)
    },
    setToken(token: string) {
      wsCache.set('user.token', token)
      this.token = token
    },
    setXpack(hasXapck: boolean) {
      this.hasXapck = hasXapck
    },
    setExp(exp: number) {
      wsCache.set('user.exp', exp)
      this.exp = exp
    },
    setTime(time: number) {
      wsCache.set('user.time', time)
      this.time = time
    },
    setUid(uid: string) {
      wsCache.set('user.uid', uid)
      this.uid = uid
    },
    setName(name: string) {
      wsCache.set('user.name', name)
      this.name = name
    },
    setOid(oid: string) {
      wsCache.set('user.oid', oid)
      this.oid = oid
    },
    setProxyInfo(proxyInfo: {
      proxy: boolean
      proxyOid: string | null
      proxySecret: string | null
    }) {
      wsCache.set('user.proxyInfo', proxyInfo)
      this.proxyInfo = proxyInfo
    },
    setLanguage(language: string) {
      const locale = useLocaleStoreWithOut()
      if (!language || language === 'zh_CN') {
        language = 'zh-CN'
      }
      wsCache.set('user.language', language)
      this.language = language
      locale.setLang(language)
      changeLocale(language as any)
    },
    clear() {
      const keys: string[] = ['token', 'uid', 'name', 'oid', 'language', 'exp', 'time', 'proxyInfo']
      keys.forEach(key => wsCache.delete('user.' + key))
    }
  }
})

export const useUserStoreWithOut = () => {
  return userStore(store)
}
