import { defineStore, storeToRefs } from 'pinia'
import { store } from '../../index'
import { dvMainStoreWithOut } from './dvMain'

const dvMainStore = dvMainStoreWithOut()
const { curComponent } = storeToRefs(dvMainStore)

export const lockStore = defineStore('lock', {
  actions: {
    lock() {
      curComponent.value.isLock = true
    },

    unlock() {
      curComponent.value.isLock = false
    }
  }
})

export const lockStoreWithOut = () => {
  return lockStore(store)
}
