import { defineStore, storeToRefs } from 'pinia'
import { store } from '../../index'
import { dvMainStoreWithOut } from './dvMain'

const dvMainStore = dvMainStoreWithOut()
const { curComponent } = storeToRefs(dvMainStore)

export const eventStore = defineStore('event', {
  actions: {
    addEvent({ event, param }) {
      curComponent.value.events[event] = param
    },

    removeEvent(event) {
      delete curComponent.value.events[event]
    }
  }
})

export const eventStoreWithOut = () => {
  return eventStore(store)
}
