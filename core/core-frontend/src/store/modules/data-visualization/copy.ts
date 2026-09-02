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
import { maxYComponentCount } from '@/utils/canvasUtils'

const dvMainStore = dvMainStoreWithOut()
const composeStore = composeStoreWithOut()
const contextmenuStore = contextmenuStoreWithOut()
const {
  multiplexingStyleAdapt,
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
      copyFrom = 'multiplexing',
      multiplexingScale = canvasStyleData.value?.scale
    ) {
      // eslint-disable-next-line @typescript-eslint/no-this-alias
      const _this = this
      const { scale } = canvasStyleData.value
      const componentIds = Object.keys(outerMultiplexingComponents)
      // 预生成 旧-新ID 全局映射，保证 VQuery.propValue 中引用的其他组件ID能被同步替换为新ID
      const outerIdMap = {}
      // 递归收集组件及其嵌套子组件的旧ID，DeTabs/Group 内层组件也需一并预生成映射
      const collectOuterIds = function (comp) {
        if (!comp) {
          return
        }
        if (comp.id) {
          outerIdMap[comp.id] = generateID()
        }
        // VQuery 的 propValue 中每个查询条件项都有独立ID，一并预生成映射，避免复用后条件项ID冲突
        if (comp.component === 'VQuery' && Array.isArray(comp.propValue)) {
          comp.propValue.forEach(function (item) {
            if (item && item.id) {
              outerIdMap[item.id] = generateID()
            }
          })
        }
        // Group 的 propValue 为嵌套子组件数组，逐个递归收集
        if (comp.component === 'Group' && Array.isArray(comp.propValue)) {
          comp.propValue.forEach(function (child) {
            collectOuterIds(child)
          })
        }
        // DeTabs 的 propValue 为多个 Tab，每个 Tab 的 componentData 为该页内的组件数组，逐个递归收集
        if (comp.component === 'DeTabs' && Array.isArray(comp.propValue)) {
          comp.propValue.forEach(function (tabItem) {
            if (tabItem && Array.isArray(tabItem.componentData)) {
              tabItem.componentData.forEach(function (child) {
                collectOuterIds(child)
              })
            }
          })
        }
      }
      componentIds.forEach(function (componentId) {
        collectOuterIds(outerMultiplexingComponents[componentId])
      })
      // 按原始顺序完成布局计算，收集待粘贴组件
      const pendingComponents = componentIds.map(function (componentId, index) {
        const newComponent = deepCopy(outerMultiplexingComponents[componentId])
        newComponent.canvasId = 'canvas-main'
        if (keepSize) {
          newComponent.style.top = newComponent.style.height + newComponent.style.top
        } else {
          // dashboard 平铺2个
          const xPositionOffset = index % 2
          const yPositionOffset = index % 2
          if (!(copyFrom === 'multiplexing' && !multiplexingStyleAdapt.value)) {
            newComponent.sizeX = pcMatrixCount.value.x / 2
            newComponent.sizeY = 14
            // dataV 数据大屏
            newComponent.style.width = ((canvasStyleData.value.width / 3) * scale) / 100
            newComponent.style.height = ((canvasStyleData.value.height / 3) * scale) / 100
          } else {
            newComponent.style.width = (newComponent.style.width * scale) / multiplexingScale
            newComponent.style.height = (newComponent.style.height * scale) / multiplexingScale
          }
          // dataV 数据大屏
          newComponent.x = newComponent.sizeX * xPositionOffset + 1
          newComponent.y = maxYComponentCount() + 10
          // dataV 数据大屏
          newComponent.style.left = 0
          newComponent.style.top = 0
        }
        return newComponent
      })
      // VQuery(过滤组件) 先加入仪表板
      pendingComponents.sort(function (a, b) {
        const aIsQuery = a.component !== 'VQuery' ? 0 : 1
        const bIsQuery = b.component !== 'VQuery' ? 0 : 1
        return aIsQuery - bIsQuery
      })
      const oldIds = Object.keys(outerIdMap)
      // 匹配任意旧组件ID，单次替换，避免链式替换污染
      const idReplaceReg = oldIds.length ? new RegExp(oldIds.join('|'), 'g') : null
      // VQuery.propValue/cascade 内引用了其他组件的旧ID，转字符串批量替换为新ID后还原
      const replaceQueryRefs = function (comp) {
        if (!comp || !idReplaceReg) {
          return
        }
        if (comp.component === 'VQuery' && comp.propValue) {
          const propValueStr = JSON.stringify(comp.propValue)
          comp.propValue = JSON.parse(
            propValueStr.replace(idReplaceReg, function (matched) {
              return outerIdMap[matched] || matched
            })
          )
          if (comp.cascade) {
            const cascadeStr = JSON.stringify(comp.cascade)
            comp.cascade = JSON.parse(
              cascadeStr.replace(idReplaceReg, function (matched) {
                return outerIdMap[matched] || matched
              })
            )
          }
        }
        // Group 内层组件递归处理
        if (comp.component === 'Group' && Array.isArray(comp.propValue)) {
          comp.propValue.forEach(function (child) {
            replaceQueryRefs(child)
          })
        }
        // DeTabs 每个 Tab 的 componentData 内层组件递归处理
        if (comp.component === 'DeTabs' && Array.isArray(comp.propValue)) {
          comp.propValue.forEach(function (tabItem) {
            if (tabItem && Array.isArray(tabItem.componentData)) {
              tabItem.componentData.forEach(function (child) {
                replaceQueryRefs(child)
              })
            }
          })
        }
      }
      pendingComponents.forEach(function (newComponent, index) {
        replaceQueryRefs(newComponent)
        _this.copyData = {
          data: [newComponent],
          copyCanvasViewInfo: canvasViewInfoPreview,
          index: index,
          copyFrom: copyFrom,
          outerIdMap: outerIdMap
        }
        _this.paste()
      })
    },
    copy() {
      if (curComponent.value && curComponent.value.component !== 'GroupArea') {
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
          const idMap = deepCopy(copyDataTemp.outerIdMap || {})
          const newComponent = deepCopyHelper(data, idMap)
          newComponent['category'] = 'base'
          if (newComponent.canvasId.includes('Group')) {
            newComponent.canvasId = 'canvas-main'
          }
          dvMainStore.addCopyComponent(newComponent, idMap, copyDataTemp.copyCanvasViewInfo)
          if (dvMainStore.multiplexingStyleAdapt && copyDataTemp.copyFrom === 'multiplexing') {
            adaptCurThemeCommonStyle(newComponent)
          }
          if (dvInfo.value.type === 'dashboard') {
            eventBus.emit('addDashboardItem-' + newComponent.canvasId, newComponent)
          }
          if (i === dataArray.length - 1) {
            dvMainStore.setCurComponent({
              component: newComponent,
              index: componentData.value.length - 1
            })
          }
          i++
        }
      }, moveTime)
      snapshotStore.recordSnapshotCache('paste')
    },
    cut(curComponentData = componentData.value) {
      if (curComponent.value && curComponent.value.component !== 'GroupArea') {
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
      snapshotStore.recordSnapshotCache('cut')
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

export function deepCopyTabItemHelper(newCanvasId, tabComponentData, idMap) {
  const resultComponentData = []
  tabComponentData.forEach(item => {
    const newItem = deepCopyHelper(item, idMap)
    newItem.canvasId = newCanvasId
    resultComponentData.push(newItem)
  })
  return resultComponentData
}

function deepCopyHelper(data, idMap) {
  const result = deepCopy(data)
  if (result.freeze) {
    result.freeze = false
  }
  // 若已在映射中预置新ID(如批量复用场景)，则复用，保证引用关系一致
  const newComponentId = idMap[data.id] || generateID()
  idMap[data.id] = newComponentId
  result.id = newComponentId
  // 复制清理移动端样式
  result.inMobile = false
  delete result.mStyle
  delete result.mEvents
  delete result.mCommonBackground
  if (result.component === 'VQuery') {
    const idMapValues = new Set(Object.values(idMap))
    result.propValue?.forEach(queryItem => {
      if (idMap[queryItem.id]) {
        // 命中映射，替换为预生成的新ID
        queryItem.id = idMap[queryItem.id]
      } else if (!idMapValues.has(queryItem.id)) {
        // 既不是旧ID也不是已生成的新ID，才需要生成
        queryItem.id = generateID()
      }
      // 否则 queryItem.id 已是新ID，保持不变
    })
  }
  if (result.component === 'Group') {
    result.propValue?.forEach((component, i) => {
      result.propValue[i] = deepCopyHelper(component, idMap)
    })
  }
  // 深度拷贝Tab
  if (result.component === 'DeTabs') {
    result.propValue?.forEach(tabItem => {
      tabItem.componentData?.forEach((tabComponent, i) => {
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
