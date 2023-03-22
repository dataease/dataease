import { defineStore } from 'pinia'
import { store } from '../../index'

export const dvMainStore = defineStore('dataVisualization', {
  state: () => {
    return {
      editMode: 'edit', // 编辑器模式 edit preview
      canvasStyleData: {
        // 页面全局数据
        width: 1200,
        height: 740,
        scale: 100,
        color: '#000',
        opacity: 1,
        background: '#fff',
        fontSize: 14
      },
      isInEditor: false, // 是否在编辑器中，用于判断复制、粘贴组件时是否生效，如果在编辑器外，则无视这些操作
      componentData: [], // 画布组件数据
      curComponent: null,
      curComponentIndex: null,
      // 点击画布时是否点中组件，主要用于取消选中组件用。
      // 如果没点中组件，并且在画布空白处弹起鼠标，则取消当前组件的选中状态
      isClickComponent: false
    }
  },
  actions: {
    aceSetCanvasData(value) {
      this.canvasStyleData = value
    },

    aceSetcurComponent(value) {
      for (let i = 0; i < this.componentData.length; i++) {
        if (this.componentData[i].id === value.id) {
          this.componentData.splice(i, 1)
        }
      }
      this.componentData.push(value)
      this.curComponent = value
    },

    setClickComponentStatus(status) {
      this.isClickComponent = status
    },

    setEditMode(mode) {
      this.editMode = mode
    },

    setInEditorStatus(status) {
      this.isInEditor = status
    },

    setCanvasStyle(style) {
      this.canvasStyleData = style
    },

    setCurComponent({ component, index }) {
      this.curComponent = component
      this.curComponentIndex = index
    },

    setShapeStyle({ top, left, width, height, rotate }) {
      if (top) this.curComponent.style.top = Math.round(top)
      if (left) this.curComponent.style.left = Math.round(left)
      if (width) this.curComponent.style.width = Math.round(width)
      if (height) this.curComponent.style.height = Math.round(height)
      if (rotate) this.curComponent.style.rotate = Math.round(rotate)
    },

    setShapeSingleStyle({ key, value }) {
      this.curComponent.style[key] = value
    },

    setComponentData(componentData = []) {
      this.componentData = componentData
    },

    addComponent({ component, index }) {
      if (index !== undefined) {
        this.componentData.splice(index, 0, component)
      } else {
        this.componentData.push(component)
      }
    },

    deleteComponent(index?) {
      if (index === undefined) {
        index = this.curComponentIndex
      }

      if (index == this.curComponentIndex) {
        this.curComponentIndex = null
        this.curComponent = null
      }

      if (/\d/.test(index)) {
        this.componentData.splice(index, 1)
      }
    }
  }
})

export const dvMainStoreWithOut = () => {
  return dvMainStore(store)
}
