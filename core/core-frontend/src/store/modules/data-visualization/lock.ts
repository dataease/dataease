import { defineStore, storeToRefs } from 'pinia'
import { store } from '../../index'
import { dvMainStoreWithOut } from './dvMain'

const dvMainStore = dvMainStoreWithOut()
const { curComponent } = storeToRefs(dvMainStore)

export const lockStore = defineStore('lock', {
  actions: {
    lock() {
      curComponent.value.isLock = true
      if (curComponent.value.component === 'Group') {
        curComponent.value.propValue.forEach(component => {
          component.isLock = true
        })
      }
    },

    unlock() {
      curComponent.value.isLock = false
      if (curComponent.value.component === 'Group') {
        curComponent.value.propValue.forEach(component => {
          component.isLock = false
        })
      }
    }
  }
})

export const lockStoreWithOut = () => {
  return lockStore(store)
}
