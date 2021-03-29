import Vue from 'vue'
import Vuex from 'vuex'
import getters from './getters'
import app from './modules/app'
import settings from './modules/settings'
import user from './modules/user'
import permission from './modules/permission'
import dataset from './modules/dataset'
import chart from './modules/chart'
import request from './modules/request'
import panel from './modules/panel'
import application from './modules/application'
import animation from './animation'
import compose from './compose'
import contextmenu from './contextmenu'
import copy from './copy'
import event from './event'
import layer from './layer'
import snapshot from './snapshot'
import lock from './lock'

Vue.use(Vuex)

const data = {
  state: {
    ...animation.state,
    ...compose.state,
    ...contextmenu.state,
    ...copy.state,
    ...event.state,
    ...layer.state,
    ...snapshot.state,
    ...lock.state,

    editMode: 'edit', // 编辑器模式 edit preview
    canvasStyleData: { // 页面全局数据
      width: 1200,
      height: 740,
      scale: 100
    },
    componentData: [], // 画布组件数据
    curComponent: null,
    curComponentIndex: null,
    // 点击画布时是否点中组件，主要用于取消选中组件用。
    // 如果没点中组件，并且在画布空白处弹起鼠标，则取消当前组件的选中状态
    isClickComponent: false
  },
  mutations: {
    ...animation.mutations,
    ...compose.mutations,
    ...contextmenu.mutations,
    ...copy.mutations,
    ...event.mutations,
    ...layer.mutations,
    ...snapshot.mutations,
    ...lock.mutations,

    setClickComponentStatus(state, status) {
      state.isClickComponent = status
    },

    setEditMode(state, mode) {
      state.editMode = mode
    },

    setCanvasStyle(state, style) {
      state.canvasStyleData = style
    },

    setCurComponent(state, { component, index }) {
      state.curComponent = component
      state.curComponentIndex = index
    },

    setShapeStyle({ curComponent }, { top, left, width, height, rotate }) {
      if (top) curComponent.style.top = top
      if (left) curComponent.style.left = left
      if (width) curComponent.style.width = width
      if (height) curComponent.style.height = height
      if (rotate) curComponent.style.rotate = rotate
    },

    setShapeSingleStyle({ curComponent }, { key, value }) {
      curComponent.style[key] = value
    },

    setComponentData(state, componentData = []) {
      Vue.set(state, 'componentData', componentData)
    },

    addComponent(state, { component, index }) {
      if (index !== undefined) {
        state.componentData.splice(index, 0, component)
      } else {
        state.componentData.push(component)
      }
    },

    deleteComponent(state, index) {
      if (index === undefined) {
        index = state.curComponentIndex
      }

      state.componentData.splice(index, 1)
    }
  },
  modules: {
    app,
    settings,
    user,
    permission,
    dataset,
    chart,
    request,
    panel,
    application
  },
  getters
}

export default new Vuex.Store(data)
