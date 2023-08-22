import { defineStore, storeToRefs } from 'pinia'
import toast from '@/utils/toast'
import { dvMainStoreWithOut } from './dvMain'
import { contextmenuStoreWithOut } from './contextmenu'
import { generateID } from '@/utils/generateID'
import { deepCopy } from '@/utils/utils'
import { store } from '../../index'
import eventBus from '@/utils/eventBus'
import { adaptCurThemeCommonStyle } from '@/utils/canvasStyle'

const dvMainStore = dvMainStoreWithOut()
const contextmenuStore = contextmenuStoreWithOut()
const {
  curComponent,
  curComponentIndex,
  curMultiplexingComponents,
  dvInfo,
  pcMatrixCount,
  canvasStyleData
} = storeToRefs(dvMainStore)
const { menuTop, menuLeft } = storeToRefs(contextmenuStore)

export const copyStore = defineStore('copy', {
  state: () => {
    return {
      copyData: null, // 复制粘贴剪切
      isCut: false
    }
  },
  actions: {
    copyMultiplexingComponents(
      canvasViewInfoPreview,
      outerMultiplexingComponents = curMultiplexingComponents.value
    ) {
      // eslint-disable-next-line @typescript-eslint/no-this-alias
      const _this = this
      Object.keys(outerMultiplexingComponents).forEach(function (componentId, index) {
        const newComponent = deepCopy(outerMultiplexingComponents[componentId])
        // dashboard 平铺3个
        const xPositionOffset = index % 3
        const yPositionOffset = index % 3
        newComponent.sizeX = pcMatrixCount.value.x / 3
        newComponent.sizeY = pcMatrixCount.value.y / 3
        newComponent.x = newComponent.sizeX * xPositionOffset + 1
        newComponent.y = 200
        // dataV 数据大屏
        newComponent.style.width = canvasStyleData.value.width / 4
        newComponent.style.height = canvasStyleData.value.height / 3
        newComponent.style.left = newComponent.style.width * xPositionOffset
        newComponent.style.height = newComponent.style.height * yPositionOffset

        _this.copyData = {
          data: newComponent,
          copyCanvasViewInfo: canvasViewInfoPreview,
          index: index,
          copyFrom: 'multiplexing'
        }
        _this.paste()
      })
    },
    copy() {
      if (!curComponent.value) {
        toast('请选择组件')
        return
      }

      // 如果有剪切的数据，需要先还原
      this.restorePreCutData()
      this.copyDataInfo()
      this.isCut = false
    },

    paste(isMouse?) {
      if (!this.copyData) {
        toast('请选择组件')
        return
      }
      const data = this.copyData.data
      if (dvInfo.value.type === 'dataV') {
        if (isMouse) {
          data.style.top = menuTop
          data.style.left = menuLeft
        } else {
          data.style.top += 10
          data.style.left += 10
        }
      }
      // 旧-新ID映射关系
      const idMap = {}
      const newComponent = deepCopyHelper(data, idMap)
      dvMainStore.addCopyComponent(newComponent, idMap, this.copyData.copyCanvasViewInfo)
      if (dvInfo.value.type === 'dashboard') {
        if (dvMainStore.multiplexingStyleAdapt && this.copyData.copyFrom === 'multiplexing') {
          adaptCurThemeCommonStyle(newComponent)
        }
        eventBus.emit('addDashboardItem-canvas-main', newComponent)
      }
      if (this.isCut) {
        this.copyData = null
      }
    },

    cut() {
      if (!curComponent.value) {
        toast('请选择组件')
        return
      }

      // 如果重复剪切，需要恢复上一次剪切的数据
      this.restorePreCutData()
      this.copyDataInfo()

      dvMainStore.deleteComponent()
      this.isCut = true
    },

    // 恢复上一次剪切的数据
    restorePreCutData() {
      if (this.isCut && this.copyData) {
        const data = deepCopy(this.copyData.data)
        const index = this.copyData.index
        dvMainStore.addComponent({ component: data, index })
        if (curComponentIndex.value >= index) {
          // 如果当前组件索引大于等于插入索引，需要加一，因为当前组件往后移了一位
          curComponentIndex.value++
        }
      }
    },

    copyDataInfo() {
      this.copyData = {
        data: deepCopy(curComponent.value),
        index: curComponentIndex
      }
    }
  }
})

function deepCopyHelper(data, idMap) {
  const result = deepCopy(data)
  const newComponentId = generateID()
  idMap[data.id] = newComponentId
  result.id = newComponentId
  if (result.component === 'Group') {
    result.propValue.forEach((component, i) => {
      result.propValue[i] = deepCopyHelper(component, idMap)
    })
  }

  return result
}

export const copyStoreWithOut = () => {
  return copyStore(store)
}
