import { defineStore, storeToRefs } from 'pinia'
import { store } from '../../index'
import { dvMainStoreWithOut } from './dvMain'
import { $, _$, deepCopy } from '@/utils/utils'
import decomposeComponent from '@/utils/decomposeComponent'
import { generateID } from '@/utils/generateID'
import {
  commonStyle,
  commonAttr,
  COMMON_COMPONENT_BACKGROUND_MAP
} from '@/custom-component/component-list'
import { createGroupStyle, getComponentRotatedStyle } from '@/utils/style'
import eventBus from '@/utils/eventBus'

const dvMainStore = dvMainStoreWithOut()
const { curComponent, componentData, curOriginThemes } = storeToRefs(dvMainStore)

export const composeStore = defineStore('compose', {
  state: () => {
    return {
      areaData: {
        // 选中区域包含的组件以及区域位移信息
        style: {
          top: 0,
          left: 0,
          width: 0,
          height: 0
        },
        components: []
      },
      editorMap: {},
      isCtrlOrCmdDown: false,
      isShiftDown: false,
      editor: null
    }
  },
  actions: {
    getEditor(canvasId = 'canvas-main') {
      this.editorMap[canvasId] = $('#editor-' + canvasId)
      // this.editor = $('#editor-' + canvasId)
    },

    setAreaData(data) {
      this.areaData = data
    },

    compose: function (canvasId = 'canvas-main') {
      const editor = this.editorMap[canvasId]
      const { areaData } = this
      if (areaData.components.length === 1) {
        // 一个组件不进行组合直接释放
        areaData.components = []
        return
      }
      if (areaData.components.length > 0 && areaData.style.width === 0) {
        // 计算组合区域
        this.calcComposeArea()
      }

      const components = []
      areaData.components.forEach(component => {
        if (component.component != 'Group') {
          components.push(component)
        } else {
          // 如果要组合的组件中，已经存在组合数据，则需要提前拆分
          const parentStyle = { ...component.style }
          const subComponents = component.propValue
          const editorRect = editor.getBoundingClientRect()

          subComponents.forEach(component => {
            decomposeComponent(component, editorRect, parentStyle)
          })

          components.push(...component.propValue)
        }
      })

      const groupComponent = {
        id: generateID(),
        component: 'Group',
        name: '组合',
        label: '组合',
        icon: 'group',
        commonBackground: {
          ...deepCopy(COMMON_COMPONENT_BACKGROUND_MAP[curOriginThemes.value]),
          backgroundColorSelect: false
        },
        ...commonAttr,
        style: {
          ...commonStyle,
          ...areaData.style
        },
        propValue: components
      }

      createGroupStyle(groupComponent)
      dvMainStore.addComponent({
        component: groupComponent,
        index: undefined
      })

      eventBus.emit('hideArea-canvas-main')
      this.batchDeleteComponent(areaData.components)
      dvMainStore.setCurComponent({
        component: componentData.value[componentData.value.length - 1],
        index: componentData.value.length - 1
      })

      this.areaData.components = []
    },

    // 将已经放到 Group 组件数据删除，也就是在 componentData 中删除，因为它们已经从 componentData 挪到 Group 组件中了
    batchDeleteComponent(deleteData) {
      deleteData.forEach(component => {
        for (let i = 0, len = componentData.value.length; i < len; i++) {
          if (component.id == componentData.value[i].id) {
            componentData.value.splice(i, 1)
            break
          }
        }
      })
    },

    decompose(canvasId = 'canvas-main') {
      const editor = this.editorMap[canvasId]
      const parentStyle = { ...curComponent.value.style }
      const components = curComponent.value.propValue
      const editorRect = editor.getBoundingClientRect()
      dvMainStore.deleteComponent()
      components.forEach(component => {
        decomposeComponent(component, editorRect, parentStyle)
        dvMainStore.addComponent({ component: component, index: undefined, isFromGroup: true })
      })
    },
    calcComposeArea() {
      if (this.areaData.components <= 1) {
        return
      }
      // 根据选中区域和区域中每个组件的位移信息来创建 Group 组件
      // 要遍历选择区域的每个组件，获取它们的 left top right bottom 信息来进行比较
      let top = Infinity,
        left = Infinity
      let right = -Infinity,
        bottom = -Infinity
      this.areaData.components.forEach(component => {
        let style = { left: 0, top: 0, right: 0, bottom: 0 }
        style = getComponentRotatedStyle(component.style)

        if (style.left < left) left = style.left
        if (style.top < top) top = style.top
        if (style.right > right) right = style.right
        if (style.bottom > bottom) bottom = style.bottom
      })

      // 设置选中区域位移大小信息和区域内的组件数据
      this.areaData.style = {
        left,
        top,
        width: right - left,
        height: bottom - top
      }
    }
  }
})

export const composeStoreWithOut = () => {
  return composeStore(store)
}
