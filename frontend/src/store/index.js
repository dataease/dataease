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
import lic from './modules/lic'
import conditions from './modules/conditions'
import animation from '@/components/canvas/store/animation'
import compose from '@/components/canvas/store/compose'
import contextmenu from '@/components/canvas/store/contextmenu'
import copy from '@/components/canvas/store/copy'
import event from '@/components/canvas/store/event'
import layer from '@/components/canvas/store/layer'
import snapshot from '@/components/canvas/store/snapshot'
import lock from '@/components/canvas/store/lock'

import {
  DEFAULT_COMMON_CANVAS_STYLE
} from '@/views/panel/panel'

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
    canvasStyleData: DEFAULT_COMMON_CANVAS_STYLE, // 页面全局数据  //扩展公共样式 公共的仪表板样式，用来实时响应样式的变化
    componentData: [], // 画布组件数据
    curComponent: null,
    curCanvasScale: null,
    curComponentIndex: null,
    // 点击画布时是否点中组件，主要用于取消选中组件用。
    // 如果没点中组件，并且在画布空白处弹起鼠标，则取消当前组件的选中状态
    isClickComponent: false,
    canvasCommonStyleData: DEFAULT_COMMON_CANVAS_STYLE
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
      console.log('curComponent' + JSON.stringify(component))
      state.curComponent = component
      state.curComponentIndex = index
    },

    setCurCanvasScale(state, curCanvasScale) {
      state.curCanvasScale = curCanvasScale
    },

    setShapeStyle({ curComponent, canvasStyleData, curCanvasScale }, { top, left, width, height, rotate }) {
      if (top || top === 0) curComponent.style.top = parseInt(canvasStyleData.selfAdaption ? (top * 100 / curCanvasScale.scaleHeight) : top)
      if (left || left === 0) curComponent.style.left = parseInt(canvasStyleData.selfAdaption ? (left * 100 / curCanvasScale.scaleWidth) : left)
      if (width || width === 0) curComponent.style.width = parseInt(canvasStyleData.selfAdaption ? (width * 100 / curCanvasScale.scaleWidth) : width)
      if (height || height === 0) curComponent.style.height = parseInt(canvasStyleData.selfAdaption ? (height * 100 / curCanvasScale.scaleHeight) : height)
      if (rotate || rotate === 0) curComponent.style.rotate = rotate
      // console.log('setShapeStyle:curComponent' + 'top:' + top + ';left:' + left + '====' + JSON.stringify(curComponent))
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

    setComponentWithId(state, component) {
      for (let index = 0; index < state.componentData.length; index++) {
        const element = state.componentData[index]
        if (element.id && element.id === component.id) {
          state.componentData[index] = component
          return
        }
      }
      state.componentData.push(component)
    },
    deleteComponentWithId(state, id) {
      for (let index = 0; index < state.componentData.length; index++) {
        const element = state.componentData[index]
        if (element.id && element.id === id) {
          state.componentData.splice(index, 1)
          break
        }
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
    application,
    lic,
    conditions
  },
  getters
}

export default new Vuex.Store(data)
