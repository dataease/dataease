import { defineStore } from 'pinia'
import { store } from '../index'
import { useCache } from '@/hooks/web/useCache'
const { wsCache } = useCache()
interface UserState {
  token: string
  uid: number
  name: string
  oid: number
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
      return wsCache.get('user.token') || this.token
    },
    getUid(): number {
      return wsCache.get('user.uid') || this.uid
    },
    getName(): string {
      return wsCache.get('user.name') || this.name
    },
    getOid(): number {
      return wsCache.get('user.oid') || this.oid
    },
    getLanguage(): string {
      return wsCache.get('user.language') || this.language
    }
  },
  actions: {
    setToken(token: string) {
      wsCache.set('user.token', token)
      this.token = token
    },
    setUid(uid: number) {
      wsCache.set('user.uid', uid)
      this.uid = uid
    },
    setName(name: string) {
      wsCache.set('user.name', name)
      this.name = name
    },
    setOid(oid: number) {
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
