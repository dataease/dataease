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
import msg from './modules/msg'
import map from './modules/map'
import animation from '@/components/canvas/store/animation'
import compose from '@/components/canvas/store/compose'
import contextmenu from '@/components/canvas/store/contextmenu'
import copy from '@/components/canvas/store/copy'
import event from '@/components/canvas/store/event'
import layer from '@/components/canvas/store/layer'
import snapshot from '@/components/canvas/store/snapshot'
import lock from '@/components/canvas/store/lock'
import { valueValid, formatCondition } from '@/utils/conditionUtil'
import {
  DEFAULT_COMMON_CANVAS_STYLE_STRING
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
    canvasStyleData: DEFAULT_COMMON_CANVAS_STYLE_STRING, // 页面全局数据  //扩展公共样式 公共的仪表板样式，用来实时响应样式的变化
    componentData: [], // 画布组件数据
    curComponent: null,
    curCanvasScale: null,
    curComponentIndex: null,
    // 点击画布时是否点中组件，主要用于取消选中组件用。
    // 如果没点中组件，并且在画布空白处弹起鼠标，则取消当前组件的选中状态
    isClickComponent: false,
    canvasCommonStyleData: DEFAULT_COMMON_CANVAS_STYLE_STRING,
    // 联动设置状态
    linkageSettingStatus: false
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
      state.styleChangeTimes = 0
      state.curComponent = component
      state.curComponentIndex = index
    },

    setCurCanvasScale(state, curCanvasScale) {
      state.curCanvasScale = curCanvasScale
    },

    setShapeStyle({ curComponent, canvasStyleData, curCanvasScale }, { top, left, width, height, rotate }) {
      if (top || top === 0) curComponent.style.top = canvasStyleData.selfAdaption ? (top * 100 / curCanvasScale.scaleHeight) : top
      if (left || left === 0) curComponent.style.left = canvasStyleData.selfAdaption ? (left * 100 / curCanvasScale.scaleWidth) : left
      if (width || width === 0) curComponent.style.width = canvasStyleData.selfAdaption ? (width * 100 / curCanvasScale.scaleWidth) : width
      if (height || height === 0) curComponent.style.height = canvasStyleData.selfAdaption ? (height * 100 / curCanvasScale.scaleHeight) : height
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
    removeViewFilter(state, componentId) {
      state.componentData = state.componentData.map(item => {
        const newItem = item
        newItem.filters = newItem.filters && newItem.filters.filter(filter => filter.componentId !== componentId) || []
        return newItem
      })
    },
    addViewFilter(state, data) {
      const condition = formatCondition(data)
      const vValid = valueValid(condition)
      //   1.根据componentId过滤
      const filterComponentId = condition.componentId

      //   2.循环每个Component 得到 三种情况 a增加b删除c无操作
      const viewIdMatch = (viewIds, viewId) => !viewIds || viewIds.length === 0 || viewIds.includes(viewId)

      for (let index = 0; index < state.componentData.length; index++) {
        const element = state.componentData[index]
        if (!element.type || element.type !== 'view') continue
        const currentFilters = element.filters || []
        const vidMatch = viewIdMatch(condition.viewIds, element.propValue.viewId)

        let j = currentFilters.length
        // let filterExist = false
        while (j--) {
          const filter = currentFilters[j]
          if (filter.componentId === filterComponentId) {
            // filterExist = true
            // 已存在该条件 且 条件值有效 直接替换原体检
            // vidMatch && vValid && (currentFilters[j] = condition)
            // 已存在该条件 且 条件值无效 直接删除原条件
            // vidMatch && !vValid && (currentFilters.splice(j, 1))
            currentFilters.splice(j, 1)
          }
        }
        // 不存在该条件 且 条件有效 直接保存该条件
        // !filterExist && vValid && currentFilters.push(condition)
        vidMatch && vValid && currentFilters.push(condition)
        element.filters = currentFilters
        state.componentData[index] = element
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
    },
    setLinkageSettingStatus(state, status) {
      state.linkageSettingStatus = status
      console.log('linkageSettingStatus:', state.linkageSettingStatus)
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
    msg,
    map
  },
  getters
}

export default new Vuex.Store(data)
