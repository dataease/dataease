import { defineStore, storeToRefs } from 'pinia'
import { store } from '../../index'
import { dvMainStoreWithOut } from './dvMain'

const dvMainStore = dvMainStoreWithOut()
const { curComponent } = storeToRefs(dvMainStore)

export const animationStore = defineStore('animation', {
  actions: {
    addAnimation(animation) {
      curComponent.value.animations.push(animation)
    },

    removeAnimation(index) {
      curComponent.value.animations.splice(index, 1)
    },

    alterAnimation({ index, data = {} }) {
      if (typeof index === 'number') {
        const original = curComponent.value.animations[index]
        curComponent.value.animations[index] = { ...original, ...data }
      }
    }
  }
})

export const animationStoreWithOut = () => {
  return animationStore(store)
}
