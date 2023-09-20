import { deepCopy } from '@/utils/utils'
import componentList, {
  COMMON_COMPONENT_BACKGROUND_DARK,
  COMMON_COMPONENT_BACKGROUND_LIGHT
} from '@/custom-component/component-list'
import eventBus from '@/utils/eventBus'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { findById, saveCanvas, updateCanvas } from '@/api/visualization/dataVisualization'
import { storeToRefs } from 'pinia'
import { getPanelAllLinkageInfo } from '@/api/visualization/linkage'
import { queryVisualizationJumpInfo } from '@/api/visualization/linkJump'
import { getViewConfig } from '@/views/chart/components/editor/util/chart'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
const dvMainStore = dvMainStoreWithOut()
const { curBatchOptComponents, dvInfo, canvasStyleData, componentData, canvasViewInfo } =
  storeToRefs(dvMainStore)
const snapshotStore = snapshotStoreWithOut()

export function chartTransStr2Object(targetIn, copy) {
  const target = copy === 'Y' ? deepCopy(targetIn) : targetIn
  return target
}

export function chartTransObject2Str(targetIn, copy) {
  const target = copy === 'Y' ? deepCopy(targetIn) : targetIn
  return target
}

export function findDragComponent(componentInfo) {
  const componentInfoArray = componentInfo.split('&')
  const componentName = componentInfoArray[0]
  const innerType = componentInfoArray[1]
  return findNewComponent(componentName, innerType)
}

export function findNewComponent(componentName, innerType) {
  let newComponent
  componentList.forEach(comp => {
    if (comp.component === componentName) {
      newComponent = deepCopy(comp)
      newComponent.innerType = innerType
      if (newComponent.innerType === 'richText') {
        newComponent.propValue = {
          textValue: ''
        }
      }
      if (dvMainStore.curOriginThemes === 'light') {
        newComponent['commonBackground'] = deepCopy(COMMON_COMPONENT_BACKGROUND_LIGHT)
      } else {
        newComponent['commonBackground'] = deepCopy(COMMON_COMPONENT_BACKGROUND_DARK)
      }
    }
  })
  if (componentName === 'UserView') {
    const viewConfig = getViewConfig(innerType)
    newComponent.name = viewConfig?.title
    newComponent.label = viewConfig?.title
    newComponent.render = viewConfig?.render
  }
  return newComponent
}

export function commonHandleDragStart(e, dvModel) {
  const componentInfo = e.target.dataset.id
  if (dvModel === 'dashboard') {
    // 仪表板使用组件消息传输方式
    eventBus.emit('handleDragStartMoveIn-canvas-main', componentInfo)
  } else {
    // 大屏使用组件消息传输方式
    e.dataTransfer.setData('id', componentInfo)
  }
}
export function commonHandleDragEnd(e, dvModel) {
  if (dvModel === 'dashboard') {
    // 仪表板结束消息传输方式(用来清理未移入的组件)
    eventBus.emit('handleDragEnd-canvas-main', e)
  }
}

export function initCanvasDataPrepare(dvId, callBack) {
  findById(dvId).then(res => {
    const canvasInfo = res.data
    const dvInfo = {
      id: canvasInfo.id,
      name: canvasInfo.name,
      pid: canvasInfo.pid,
      status: canvasInfo.status,
      selfWatermarkStatus: canvasInfo.selfWatermarkStatus,
      type: canvasInfo.type,
      creatorName: canvasInfo.creatorName,
      updateName: canvasInfo.updateName,
      createTime: canvasInfo.createTime,
      updateTime: canvasInfo.updateTime
    }
    const canvasDataResult = JSON.parse(canvasInfo.componentData)
    const canvasStyleResult = JSON.parse(canvasInfo.canvasStyleData)
    const canvasViewInfoPreview = canvasInfo.canvasViewInfo
    const curPreviewGap =
      dvInfo.type === 'dashboard' && canvasStyleResult['dashboard'].gap === 'yes'
        ? canvasStyleResult['dashboard'].gapSize
        : 0
    callBack({ canvasDataResult, canvasStyleResult, dvInfo, canvasViewInfoPreview, curPreviewGap })
  })
}

export function initCanvasData(dvId, callBack) {
  initCanvasDataPrepare(
    dvId,
    function ({ canvasDataResult, canvasStyleResult, dvInfo, canvasViewInfoPreview }) {
      dvMainStore.setComponentData(canvasDataResult)
      dvMainStore.setCanvasStyle(canvasStyleResult)
      dvMainStore.updateCurDvInfo(dvInfo)
      dvMainStore.setCanvasViewInfo(canvasViewInfoPreview)
      // 刷新联动信息
      getPanelAllLinkageInfo(dvInfo.id).then(rsp => {
        dvMainStore.setNowPanelTrackInfo(rsp.data)
      })
      // 刷新跳转信息
      queryVisualizationJumpInfo(dvInfo.id).then(rsp => {
        dvMainStore.setNowPanelJumpInfo(rsp.data)
      })
      callBack({ canvasDataResult, canvasStyleResult, dvInfo, canvasViewInfoPreview })
    }
  )
}

export function checkIsBatchOptView(viewId) {
  return curBatchOptComponents.value.includes(viewId)
}

export function canvasSave(callBack) {
  const canvasInfo = {
    canvasStyleData: JSON.stringify(canvasStyleData.value),
    componentData: JSON.stringify(componentData.value),
    canvasViewInfo: canvasViewInfo.value,
    ...dvInfo.value
  }
  const method = dvInfo.value.id ? updateCanvas : saveCanvas
  method(canvasInfo).then(res => {
    if (res && res.data) {
      dvMainStore.updateDvInfoId(res.data)
    }
    snapshotStore.resetStyleChangeTimes()
    callBack(res)
  })
}

export function checkAddHttp(url) {
  if (!url) {
    return url
  } else if (/^(http(s)?:\/\/)/.test(url.toLowerCase())) {
    return url
  } else {
    return 'http://' + url
  }
}

export function setIdValueTrans(from, to, content, colList) {
  if (!content) {
    return content
  }
  let name2Id = content
  const nameIdMap = colList.reduce((pre, next) => {
    pre[next[from]] = next[to]
    return pre
  }, {})
  const on = content.match(/\[(.+?)\]/g)
  if (on) {
    on.forEach(itm => {
      const ele = itm.slice(1, -1)
      name2Id = name2Id.replace(itm, nameIdMap[ele])
    })
  }
  return name2Id
}

export function isMainCanvas(canvasId) {
  return canvasId === 'canvas-main'
}

export function isSameCanvas(item, canvasId) {
  return item.canvasId === canvasId
}

export function findComponentIndexById(componentId, componentDataMatch = componentData.value) {
  let indexResult = -1
  componentDataMatch.forEach((component, index) => {
    if (component.id === componentId) {
      indexResult = index
    }
  })
  return indexResult
}

export function canvasChangeAdaptor(component, matrixBase, usePointShadow = false) {
  const targetDomComponent = document.querySelector(
    usePointShadow ? '#point-shadow-main' : '#shape-id-' + component.id
  )
  const componentWidth = targetDomComponent['offsetWidth']
  const componentHeight = targetDomComponent['offsetHeight']
  component.sizeX = Math.round(componentWidth / matrixBase.baseWidth)
  component.sizeY = Math.round(componentHeight / matrixBase.baseHeight)
  component.style.width = componentWidth
  component.style.height = componentHeight
  if (usePointShadow) {
    const componentLeft = targetDomComponent['offsetLeft']
    const componentTop = targetDomComponent['offsetTop']
    component.x = Math.round(componentLeft / matrixBase.baseWidth)
    component.y = Math.round(componentTop / matrixBase.baseHeight)
    component.style.left = componentLeft
    component.style.height = componentTop
  }
}

export function findAllViewsId(componentData, idArray) {
  componentData.forEach(item => {
    if (item.component === 'UserView' && item.innerType != 'VQuery') {
      idArray.push(item.id)
    } else if (item.component === 'Group') {
      item.propValue.forEach(groupItem => {
        idArray.push(groupItem.id)
      })
    } else if (item.component === 'DeTabs') {
      item.propValue.forEach(tabItem => {
        tabItem.componentData.forEach(tabComponent => {
          idArray.push(tabComponent.id)
        })
      })
    }
  })
}
