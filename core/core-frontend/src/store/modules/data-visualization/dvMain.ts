import { defineStore } from 'pinia'
import { store } from '../../index'
import { deepCopy } from '@/utils/utils'
import { BASE_VIEW_CONFIG } from '@/views/chart/components/editor/util/chart'

export const DEFAULT_CANVAS_STYLE_DATA = {
  // 页面全局数据
  width: 1920,
  height: 1080,
  backgroundType: 'backgroundColor',
  background: '',
  scale: 60,
  color: '#fff',
  opacity: 1,
  backgroundColor: '#000',
  fontSize: 14
}

export const dvMainStore = defineStore('dataVisualization', {
  state: () => {
    return {
      staticResourcePath: '/static-resource/',
      canvasCollapse: {
        defaultSide: false,
        realTimeComponent: false,
        canvas: false,
        componentProp: false,
        chartAreaCollapse: false,
        datasetAreaCollapse: false
      },
      editMode: 'edit', // 编辑器模式 edit preview
      canvasStyleData: deepCopy(DEFAULT_CANVAS_STYLE_DATA),
      isInEditor: false, // 是否在编辑器中，用于判断复制、粘贴组件时是否生效，如果在编辑器外，则无视这些操作
      componentData: [], // 画布组件数据
      curComponent: null,
      curComponentIndex: null,
      // 点击画布时是否点中组件，主要用于取消选中组件用。
      // 如果没点中组件，并且在画布空白处弹起鼠标，则取消当前组件的选中状态
      isClickComponent: false,
      // 大屏基础信息
      dvInfo: {
        id: null,
        name: null,
        pid: null,
        status: null,
        selfWatermarkStatus: null
      },
      // 视图信息
      canvasViewInfo: {}
    }
  },
  actions: {
    aceSetCanvasData(value) {
      this.canvasStyleData = value
    },

    aceSetCurComponent(value) {
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
    setCanvasViewInfo(canvasViewInfo) {
      this.canvasViewInfo = canvasViewInfo
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
        this.setCurComponent({ component: component, index: index })
      } else {
        this.componentData.push(component)
        this.setCurComponent({ component: component, index: this.componentData.length - 1 })
      }
      //如果当前的组件是UserView 视图，则想canvasView中增加一项 UserView ID 和componentID保持一致
      if (component.component === 'UserView') {
        const newView = {
          ...deepCopy(BASE_VIEW_CONFIG),
          id: component.id,
          type: component.innerType
        }
        this.canvasViewInfo[component.id] = newView
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
    },
    updateCurDvInfo(dvInfo) {
      this.dvInfo = dvInfo
    }
  }
})

export const dvMainStoreWithOut = () => {
  return dvMainStore(store)
}
