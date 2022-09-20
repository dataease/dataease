import store from '@/store/index'
import toast from '@/components/canvas/utils/toast'
import generateID from '@/components/canvas/utils/generateID'
import { deepCopy } from '@/components/canvas/utils/utils'
import { chartBatchCopy, chartCopy } from '@/api/chart/chart'
import { uuid } from 'vue-uuid'
import { adaptCurThemeCommonStyle } from '@/components/canvas/utils/style'
import Vue from "vue";

export default {
  state: {
    copyData: null, // 复制粘贴剪切
    isCut: false,
    baseStyle: {
      width: 300,
      height: 200,
      top: 0,
      left: 0
    },
    viewBase: {
      x: 1,
      y: 216,
      sizex: 48,
      sizey: 24
    }
  },
  mutations: {
    copyMultiplexingComponents(state) {
      let pYMax = 0
      const _this = this
      state.isCut = false
      state.componentData.forEach(item => {
        if (item.y > pYMax) {
          pYMax = item.y
        }
      })
      const canvasStyleData = state.canvasStyleData
      const curCanvasScale = state.curCanvasScale
      const componentGap = state.componentGap
      Object.keys(state.curMultiplexingComponents).forEach(function(componentId, index) {
        const component =
          {
            ...deepCopy(state.curMultiplexingComponents[componentId]),
            ...deepCopy(state.viewBase),
            'auxiliaryMatrix': canvasStyleData.auxiliaryMatrix
          }

        component.style = {
          ...component.style,
          ...deepCopy(state.baseStyle)
        }

        const tilePosition = index % 3
        const divisiblePosition = parseInt(index / 3)
        if (canvasStyleData.auxiliaryMatrix) {
          const width = component.sizex * curCanvasScale.matrixStyleOriginWidth
          // 取余 平铺4个 此处x 位置偏移
          component.x = component.x + component.sizex * tilePosition
          // Y 方向根据当前应该放置的最大值 加上50矩阵余量
          component.y = pYMax + 50 + state.viewBase.sizex * divisiblePosition
          component.style.left = (component.x - 1) * curCanvasScale.matrixStyleOriginWidth
          component.style.top = (component.y - 1) * curCanvasScale.matrixStyleOriginHeight
          component.style.width = width
          component.style.height = component.sizey * curCanvasScale.matrixStyleOriginHeight
        } else {
          const width = component.style.width
          const height = component.style.height
          component.style.top = component.style.top + divisiblePosition * (height + componentGap)
          component.style.left = component.style.left + tilePosition * (width + componentGap)
          component.style.width = width
          component.style.height = height
        }
        state.copyData = {
          data: component,
          index: index
        }
        _this.commit('paste', true)
      })
    },
    copy(state) {
      if (!state.curComponent) return
      state.copyData = {
        data: deepCopy(state.curComponent),
        index: state.curComponentIndex
      }

      state.isCut = false
    },

    paste(state, needAdaptor) {
      if (!state.copyData) {
        toast('请选择组件')
        return
      }

      const data = state.copyData.data
      // 仪表板复制的组件默认不在移动端部署中mobileSelected = false
      data.mobileSelected = false
      if (!state.curComponent.auxiliaryMatrix) {
        data.style.top += 20
        data.style.left += 20
      }
      data.id = generateID()
      // 如果是用户视图 测先进行底层复制
      if (data.type === 'view') {
        chartCopy(data.propValue.viewId, state.panel.panelInfo.id).then(res => {
          const newView = deepCopy(data)
          Vue.set(newView, 'needAdaptor', needAdaptor)
          newView.id = uuid.v1()
          newView.propValue.viewId = res.data
          if (newView.filters && newView.filters.length) {
            newView.filters = []
          }
          needAdaptor && adaptCurThemeCommonStyle(newView)
          store.commit('addComponent', { component: newView })
        })
      } else if (data.type === 'de-tabs') {
        const sourceAndTargetIds = {}
        const newCop = deepCopy(data)
        newCop.id = uuid.v1()
        newCop.options.tabList.forEach((item) => {
          if (item.content && item.content.type === 'view') {
            const newViewId = uuid.v1()
            sourceAndTargetIds[item.content.propValue.viewId] = newViewId
            item.content.propValue.viewId = newViewId
            Vue.set(item.content, 'needAdaptor', needAdaptor)
            if (item.content.filters && item.content.filters.length) {
              item.content.filters = []
            }
          }
        })
        chartBatchCopy({ 'sourceAndTargetIds': sourceAndTargetIds }, state.panel.panelInfo.id).then((rsp) => {
          needAdaptor && adaptCurThemeCommonStyle(newCop)
          store.commit('addComponent', { component: newCop })
        })
      } else {
        const newCop = deepCopy(data)
        newCop.id = uuid.v1()
        needAdaptor && adaptCurThemeCommonStyle(newCop)
        store.commit('addComponent', { component: newCop })
      }
      if (state.isCut) {
        state.copyData = null
      }
    },

    cut(state) {
      if (!state.curComponent) {
        toast('请选择组件')
        return
      }

      if (state.copyData) {
        const data = deepCopy(state.copyData.data)
        const index = state.copyData.index
        data.id = generateID()
        store.commit('addComponent', { component: data, index })
        if (state.curComponentIndex >= index) {
          // 如果当前组件索引大于等于插入索引，需要加一，因为当前组件往后移了一位
          state.curComponentIndex++
        }
      }

      store.commit('copy')
      store.commit('deleteComponent')
      state.isCut = true
    }
  }
}
