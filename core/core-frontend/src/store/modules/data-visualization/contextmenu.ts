import { defineStore } from 'pinia'
import { store } from '../../index'

export const contextmenuStore = defineStore('contextmenu', {
  state: () => {
    return {
      menuTop: 0, // 右击菜单数据
      menuLeft: 0,
      menuShow: false,
      position: 'canvasCore'
    }
  },
  actions: {
    showContextMenu({ top, left, position }) {
      this.menuShow = true
      this.menuTop = top
      this.menuLeft = left
      this.position = position
    },

    hideContextMenu() {
      this.menuShow = false
    }
  }
})

export const contextmenuStoreWithOut = () => {
  return contextmenuStore(store)
}
