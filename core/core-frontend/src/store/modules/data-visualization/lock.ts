import { defineStore, storeToRefs } from 'pinia'
import { store } from '../../index'
import { dvMainStoreWithOut } from './dvMain'

const dvMainStore = dvMainStoreWithOut()
const { curComponent } = storeToRefs(dvMainStore)

export const lockStore = defineStore('lock', {
  actions: {
    lock(optComponent = curComponent.value) {
      optComponent.isLock = true
      if (optComponent.component === 'Group') {
        optComponent.propValue?.forEach(component => {
          component.isLock = true
        })
      }
    },

    unlock(optComponent = curComponent.value) {
      optComponent.isLock = false
      if (optComponent.component === 'Group') {
        optComponent.propValue?.forEach(component => {
          component.isLock = false
        })
      }
    }
  }
})

export const lockStoreWithOut = () => {
  return lockStore(store)
}
