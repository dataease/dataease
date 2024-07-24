import { defineStore, storeToRefs } from 'pinia'
import { store } from '../../index'
import { dvMainStoreWithOut } from './dvMain'
import { $, deepCopy } from '@/utils/utils'
import decomposeComponent from '@/utils/decomposeComponent'
import { generateID } from '@/utils/generateID'
import {
  commonStyle,
  commonAttr,
  COMMON_COMPONENT_BACKGROUND_MAP
} from '@/custom-component/component-list'
import { createGroupStyle, getComponentRotatedStyle, groupStyleRevert } from '@/utils/style'
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
      laterIndex: null, //最后点击组件的索引
      editor: null
    }
  },
  actions: {
    getEditor(canvasId = 'canvas-main') {
      this.editorMap[canvasId] = $('#editor-' + canvasId)
    },

    setLaterIndex(value) {
      this.laterIndex = value
    },
    setIsCtrlOrCmdDownStatus(value) {
      this.isCtrlOrCmdDown = value
    },
    setIsShiftDownStatus(value) {
      this.isShiftDown = value
    },
    setAreaData(data) {
      this.areaData = data
    },
    updateGroupBorder(canvasId) {
      if (canvasId) {
        // 1.查找所属分组
        const groupId = canvasId.replace('Group-', '')
        const sourceGroupComponent = componentData.value.filter(ele => ele.id === groupId)[0]
        const sourceSubComponents = sourceGroupComponent.propValue
        // 2. 还原分组内部组件再主画布位置
        const sourceParentStyle = { ...sourceGroupComponent.style }
        sourceSubComponents.forEach(subcomponent => {
          decomposeComponent(subcomponent, null, sourceParentStyle)
        })
        const newAreaData = {
          // 选中区域包含的组件以及区域位移信息
          style: {
            top: 0,
            left: 0,
            width: 0,
            height: 0
          },
          components: sourceSubComponents
        }
        // 3.重新计算分组区域边界
        this.calcComposeArea(newAreaData)
        sourceGroupComponent.style = {
          ...sourceGroupComponent.style,
          ...newAreaData.style
        }
        sourceSubComponents.forEach(component => {
          component.canvasId = canvasId
        })
        // 4.计算内部子组件位置
        createGroupStyle(sourceGroupComponent)
      }
    },

    alignment: function (params) {
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
      const { left, top, width, height } = areaData.style
      const areaRight = left + width
      const areaTransverseCenter = left + width / 2 // 横向中心点
      const areaBottom = top + height
      const areaDirectionCenter = top + height / 2 // 纵向中心点

      areaData.components.forEach(component => {
        if (params === 'left') {
          // 居左
          component.style.left = left
        } else if (params === 'right') {
          // 居右
          component.style.left = areaRight - component.style.width
        } else if (params === 'top') {
          // 居上
          component.style.top = top
        } else if (params === 'bottom') {
          // 居下
          component.style.top = areaBottom - component.style.height
        } else if (params === 'transverse') {
          // 横向居中
          component.style.left = areaTransverseCenter - component.style.width / 2
        } else if (params === 'direction') {
          // 纵向
          component.style.top = areaDirectionCenter - component.style.height / 2
        }
      })
    },

    compose: function (canvasId = 'canvas-main') {
      const editor = this.editorMap[canvasId]
      const { areaData } = this
      if (areaData.components.length === 1) {
        // 一个组件不进行组合直接释放
        areaData.components = []
        return
      }
      if (areaData.components.length > 0) {
        // 计算组合区域
        this.calcComposeArea()
      }

      const components = []
      areaData.components.forEach(component => {
        if (!['Group', 'GroupArea'].includes(component.component)) {
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

      const newId = generateID()
      components.forEach(component => {
        component.canvasId = 'Group-' + newId
      })
      const groupComponent = {
        id: newId,
        component: 'Group',
        canvasActive: false,
        name: '组合',
        label: '组合',
        icon: 'group',
        expand: false,
        commonBackground: {
          ...deepCopy(COMMON_COMPONENT_BACKGROUND_MAP[curOriginThemes.value]),
          backgroundColorSelect: false,
          innerPadding: 0
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
      dvMainStore.deleteComponentById(curComponent.value.id)
      components.forEach(component => {
        decomposeComponent(component, editorRect, parentStyle)
        dvMainStore.addComponent({ component: component, index: undefined, isFromGroup: true })
      })
    },
    calcComposeArea(areaDataValue = this.areaData) {
      if (areaDataValue.components <= 1) {
        return
      }
      // 根据选中区域和区域中每个组件的位移信息来创建 Group 组件
      // 要遍历选择区域的每个组件，获取它们的 left top right bottom 信息来进行比较
      let top = Infinity,
        left = Infinity
      let right = -Infinity,
        bottom = -Infinity
      areaDataValue.components.forEach(component => {
        let style = { left: 0, top: 0, right: 0, bottom: 0 }
        style = getComponentRotatedStyle(component.style)

        if (style.left < left) left = style.left
        if (style.top < top) top = style.top
        if (style.right > right) right = style.right
        if (style.bottom > bottom) bottom = style.bottom
      })

      // 设置选中区域位移大小信息和区域内的组件数据
      areaDataValue.style = {
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
