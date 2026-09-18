import { defineStore } from 'pinia'
import { store } from '@/store/index'

interface LinkState {
  linkToken: string
  visitorPermissions: number
}

export const useLinkStore = defineStore('linkStore', {
  state: (): LinkState => {
    return {
      linkToken: '',
      visitorPermissions: 0
    }
  },
  getters: {
    getLinkToken(): string {
      return this.linkToken
    }
  },
  actions: {
    setVisitorPermissions(data: number) {
      this.visitorPermissions = data
    },
    setLinkToken(data: string) {
      if (data !== this.linkToken) this.visitorPermissions = 0
      this.linkToken = data
    }
  }
})

export const useLinkStoreWithOut = () => {
  return useLinkStore(store)
}
