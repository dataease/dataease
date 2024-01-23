import { defineStore, storeToRefs } from 'pinia'
import { dvMainStoreWithOut } from './dvMain'
import { contextmenuStoreWithOut } from './contextmenu'
import { generateID } from '@/utils/generateID'
import { deepCopy } from '@/utils/utils'
import { store } from '../../index'
import eventBus from '@/utils/eventBus'
import { adaptCurThemeCommonStyle } from '@/utils/canvasStyle'
import { composeStoreWithOut } from '@/store/modules/data-visualization/compose'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'

const dvMainStore = dvMainStoreWithOut()
const composeStore = composeStoreWithOut()
const contextmenuStore = contextmenuStoreWithOut()
const {
  curComponent,
  curComponentIndex,
  curMultiplexingComponents,
  dvInfo,
  pcMatrixCount,
  canvasStyleData,
  componentData
} = storeToRefs(dvMainStore)
const { menuTop, menuLeft } = storeToRefs(contextmenuStore)

const snapshotStore = snapshotStoreWithOut()

export const copyStore = defineStore('copy', {
  state: () => {
    return {
      copyDataArray: [], // 批量复制粘贴剪切
      copyData: null, // 复制粘贴剪切
      isCut: false
    }
  },
  actions: {
    copyMultiplexingComponents(
      canvasViewInfoPreview,
      outerMultiplexingComponents = curMultiplexingComponents.value,
      keepSize = false,
      copyFrom = 'multiplexing'
    ) {
      // eslint-disable-next-line @typescript-eslint/no-this-alias
      const _this = this
      Object.keys(outerMultiplexingComponents).forEach(function (componentId, index) {
        const newComponent = deepCopy(outerMultiplexingComponents[componentId])
        newComponent.canvasId = 'canvas-main'
        if (keepSize) {
          newComponent.style.top = newComponent.style.height + newComponent.style.top
        } else {
          // dashboard 平铺2个
          const xPositionOffset = index % 2
          const yPositionOffset = index % 2
          newComponent.sizeX = pcMatrixCount.value.x / 2
          newComponent.sizeY = 7
          newComponent.x = newComponent.sizeX * xPositionOffset + 1
          newComponent.y = 200
          // dataV 数据大屏
          newComponent.style.width = canvasStyleData.value.width / 3
          newComponent.style.height = canvasStyleData.value.height / 3
          newComponent.style.left = newComponent.style.width * xPositionOffset
          newComponent.style.top = newComponent.style.height * yPositionOffset
        }
        _this.copyData = {
          data: [newComponent],
          copyCanvasViewInfo: canvasViewInfoPreview,
          index: index,
          copyFrom: copyFrom
        }
        _this.paste()
      })
    },
    copy() {
      if (curComponent.value) {
        this.copyDataInfo([curComponent.value])
      } else if (composeStore.areaData.components.length) {
        this.copyDataInfo(composeStore.areaData.components)
      }
      this.isCut = false
    },

    paste(isMouse?) {
      if (!this.copyData) {
        return
      }
      const dataArray = this.copyData.data

      let i = 0
      const copyDataTemp = this.copyData
      const moveTime = dataArray.length > 1 ? 300 : 10
      const timeId = setInterval(function () {
        if (i >= dataArray.length) {
          clearInterval(timeId)
        } else {
          const data = dataArray[i]
          if (dvInfo.value.type === 'dataV') {
            if (isMouse) {
              data.style.top = menuTop
              data.style.left = menuLeft
            } else {
              data.style.top += 10
              data.style.left += 10
            }
          } else {
            // 向下移动一个高度矩阵单位
            data.y = data.y + data.sizeY
          }
          // 旧-新ID映射关系
          const idMap = {}
          const newComponent = deepCopyHelper(data, idMap)
          if (newComponent.canvasId.includes('Group')) {
            newComponent.canvasId = 'canvas-main'
          }
          dvMainStore.addCopyComponent(newComponent, idMap, copyDataTemp.copyCanvasViewInfo)
          if (dvInfo.value.type === 'dashboard') {
            if (dvMainStore.multiplexingStyleAdapt && copyDataTemp.copyFrom === 'multiplexing') {
              adaptCurThemeCommonStyle(newComponent)
            }
            eventBus.emit('addDashboardItem-' + newComponent.canvasId, newComponent)
          }

          i++
        }
      }, moveTime)
      snapshotStore.recordSnapshotCache()
    },
    cut(curComponentData = componentData.value) {
      if (curComponent.value) {
        this.copyDataInfo([curComponent.value])
        dvMainStore.deleteComponentById(curComponent.value.id, curComponentData)
      } else if (composeStore.areaData.components.length) {
        this.copyDataInfo(composeStore.areaData.components)
        composeStore.areaData.components.forEach(component => {
          dvMainStore.deleteComponentById(component.id)
        })
        composeStore.setAreaData({
          style: {
            left: 0,
            top: 0,
            width: 0,
            height: 0
          },
          components: []
        })
      }
      snapshotStore.recordSnapshotCache()
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

    copyDataArrayInfo() {
      this.copyDataArray = deepCopy(composeStore.areaData.components)
    },

    copyDataInfo(copyData) {
      this.copyData = {
        data: deepCopy(copyData),
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
  // 深度拷贝Tab
  if (result.component === 'DeTabs') {
    result.propValue.forEach(tabItem => {
      tabItem.componentData.forEach((tabComponent, i) => {
        tabItem.componentData[i] = deepCopyHelper(tabComponent, idMap)
        // 对Tab的深度复制需要更换新组件的canvasId (tabsId--tabName)
        tabItem.componentData[i].canvasId = result.id + '--' + tabItem.name
      })
    })
  }

  return result
}

export const copyStoreWithOut = () => {
  return copyStore(store)
}
