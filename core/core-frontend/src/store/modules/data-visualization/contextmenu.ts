import { defineStore } from 'pinia'
import { store } from '../../index'

export const contextmenuStore = defineStore('contextmenu', {
  state: () => {
    return {
      menuTop: 0, // 右击菜单数据
      menuLeft: 0,
      menuShow: false
    }
  },
  actions: {
    showContextMenu({ top, left }) {
      this.menuShow = true
      this.menuTop = top
      this.menuLeft = left
    },

    hideContextMenu() {
      this.menuShow = false
    }
  }
})

export const contextmenuWithOut = () => {
  return contextmenuStore(store)
}
