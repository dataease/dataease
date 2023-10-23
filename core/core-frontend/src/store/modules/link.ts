import { defineStore } from 'pinia'
import { store } from '@/store/index'

interface LinkState {
  linkToken: string
}

export const useLinkStore = defineStore('linkStore', {
  state: (): LinkState => {
    return {
      linkToken: ''
    }
  },
  getters: {
    getLinkToken(): string {
      return this.linkToken
    }
  },
  actions: {
    setLinkToken(data: string) {
      this.linkToken = data
    }
  }
})

export const useLinkStoreWithOut = () => {
  return useLinkStore(store)
}
