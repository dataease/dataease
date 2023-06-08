import { defineStore } from 'pinia'
import { store } from '../index'
import { useCache } from '@/hooks/web/useCache'
import { useLocaleStoreWithOut } from './locale'

import { userInfo } from '@/api/user'
const { wsCache } = useCache()
const locale = useLocaleStoreWithOut()
interface UserState {
  token: string
  uid: string
  name: string
  oid: string
  language: string
}

export const userStore = defineStore('user', {
  state: (): UserState => {
    return {
      token: null,
      uid: null,
      name: null,
      oid: null,
      language: 'zh-CN'
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
    }
  },
  actions: {
    async setUser() {
      const desktop = wsCache.get('app.desktop')
      const res = desktop
        ? { data: { uid: '1', name: 'admin', oid: '1', language: 'zh-CN' } }
        : await userInfo()
      const data = res.data
      data.token = wsCache.get('user.token')

      const keys: string[] = ['token', 'uid', 'name', 'oid', 'language']

      keys.forEach(key => {
        const dkey = key === 'uid' ? 'id' : key
        this[key] = data[dkey]
        wsCache.set('user.' + key, this[key])
      })
      this.setLanguage(this.language)
    },
    setToken(token: string) {
      wsCache.set('user.token', token)
      this.token = token
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
    setLanguage(language: string) {
      wsCache.set('user.language', language)
      this.language = language
      locale.setLang(language)
    },
    clear() {
      const keys: string[] = ['token', 'uid', 'name', 'oid', 'language']
      keys.forEach(key => wsCache.delete('user.' + key))
    }
  }
})

export const useUserStoreWithOut = () => {
  return userStore(store)
}
