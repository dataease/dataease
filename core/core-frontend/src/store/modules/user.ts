import { defineStore } from 'pinia'
import { store } from '../index'
import { useCache } from '@/hooks/web/useCache'
const { wsCache } = useCache()
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
