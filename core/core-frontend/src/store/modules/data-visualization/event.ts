import { defineStore, storeToRefs } from 'pinia'
import { store } from '../../index'
import { dvMainStoreWithOut } from './dvMain'

const dvMainStore = dvMainStoreWithOut()
const { curComponent, componentData } = storeToRefs(dvMainStore)

export const eventStore = defineStore('event', {
  actions: {
    addEvent({ event, param }) {
      curComponent.value.events[event] = param
    },

    removeEvent(event) {
      delete curComponent.value.events[event]
    },

    displayEventChange(component) {
      component.events.displayChange.value = !component.events.displayChange.value
      dvMainStore.canvasStateChange({ key: 'curPointArea', value: area })
      componentData.value.forEach(item => {
        if (item.category === 'hidden') {
          item.isShow = component.events.displayChange.value
        }
      })
    }
  }
})

export const eventStoreWithOut = () => {
  return eventStore(store)
}
