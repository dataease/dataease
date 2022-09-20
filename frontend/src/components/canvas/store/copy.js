import store from '@/store/index'
import toast from '@/components/canvas/utils/toast'
import generateID from '@/components/canvas/utils/generateID'
import { deepCopy } from '@/components/canvas/utils/utils'
import { chartCopy } from '@/api/chart/chart'
import { uuid } from 'vue-uuid'

export default {
  state: {
    copyData: null, // 复制粘贴剪切
    isCut: false
  },
  mutations: {
    copy(state) {
      if (!state.curComponent) return
      state.copyData = {
        data: deepCopy(state.curComponent),
        index: state.curComponentIndex
      }
      if (state.copyData.data.type === 'de-nav') {
        state.copyData.data.options.navTabList.forEach(ele => {
          ele.name = ele.name + '-copy'
          ele.relation = []
        })
      }
      console.log('触发复制---------', state.copyData)
      state.isCut = false
    },

    paste(state, isMouse) {
      if (!state.copyData) {
        toast('请选择组件')
        return
      }

      const data = state.copyData.data
      // 仪表板复制的组件默认不在移动端部署中mobileSelected = false
      data.mobileSelected = false
      if (state.curComponent && !state.curComponent.auxiliaryMatrix) {
        data.style.top += 20
        data.style.left += 20
      }

      // if (isMouse) {
      //   data.style.top = state.menuTop
      //   data.style.left = state.menuLeft
      // } else {
      //   data.style.top += 10
      //   data.style.left += 10
      // }

      data.id = generateID()

      // 如果是用户视图 测先进行底层复制
      if (data.type === 'view') {
        chartCopy(data.propValue.viewId, state.panel.panelInfo.id).then(res => {
          const newView = deepCopy(data)
          newView.id = uuid.v1()
          newView.propValue.viewId = res.data
          store.commit('addComponent', { component: newView })
        })
      } else {
        const newCop = deepCopy(data)
        newCop.id = uuid.v1()
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
