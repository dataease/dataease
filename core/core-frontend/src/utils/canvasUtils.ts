import { cloneDeep } from 'lodash-es'
import componentList, {
  ACTION_SELECTION,
  COMMON_COMPONENT_BACKGROUND_DARK,
  COMMON_COMPONENT_BACKGROUND_LIGHT
} from '@/custom-component/component-list'
import eventBus from '@/utils/eventBus'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import {
  decompression,
  findById,
  findCopyResource,
  saveCanvas,
  updateCanvas
} from '@/api/visualization/dataVisualization'
import { storeToRefs } from 'pinia'
import { getPanelAllLinkageInfo } from '@/api/visualization/linkage'
import { queryVisualizationJumpInfo } from '@/api/visualization/linkJump'
import {
  getViewConfig,
  SENIOR_STYLE_SETTING_LIGHT
} from '@/views/chart/components/editor/util/chart'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { deepCopy } from '@/utils/utils'
const dvMainStore = dvMainStoreWithOut()
const { curBatchOptComponents, dvInfo, canvasStyleData, componentData, canvasViewInfo } =
  storeToRefs(dvMainStore)
const snapshotStore = snapshotStoreWithOut()

export function chartTransStr2Object(targetIn, copy) {
  const target = copy === 'Y' ? cloneDeep(targetIn) : targetIn
  return target
}

export function chartTransObject2Str(targetIn, copy) {
  const target = copy === 'Y' ? cloneDeep(targetIn) : targetIn
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
    if (comp.component === componentName || comp.component === innerType) {
      newComponent = cloneDeep(comp)
      newComponent.innerType = innerType
      if (newComponent.innerType === 'richText') {
        newComponent.propValue = {
          textValue: ''
        }
      }
      if (dvMainStore.curOriginThemes === 'light') {
        newComponent['commonBackground'] = cloneDeep(COMMON_COMPONENT_BACKGROUND_LIGHT)
      } else {
        newComponent['commonBackground'] = cloneDeep(COMMON_COMPONENT_BACKGROUND_DARK)
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

export function initCanvasDataPrepare(dvId, busiFlag, callBack) {
  const copyFlag = busiFlag != null && busiFlag.includes('-copy')
  const busiFlagCustom = copyFlag ? busiFlag.split('-')[0] : busiFlag
  const method = copyFlag ? findCopyResource : findById
  method(dvId, busiFlagCustom).then(res => {
    const canvasInfo = res.data
    const watermarkInfo = {
      ...canvasInfo.watermarkInfo,
      settingContent: JSON.parse(canvasInfo.watermarkInfo.settingContent)
    }

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
      updateTime: canvasInfo.updateTime,
      watermarkInfo: watermarkInfo,
      mobileLayout: canvasInfo.mobileLayout || false
    }
    const canvasVersion = canvasInfo.version

    const canvasDataResult = JSON.parse(canvasInfo.componentData)
    const canvasStyleResult = JSON.parse(canvasInfo.canvasStyleData)
    const canvasViewInfoPreview = canvasInfo.canvasViewInfo
    //历史字段适配
    canvasStyleResult.component['seniorStyleSetting'] =
      canvasStyleResult.component['seniorStyleSetting'] || deepCopy(SENIOR_STYLE_SETTING_LIGHT)

    canvasDataResult.forEach(componentItem => {
      componentItem['canvasActive'] = false
      if (componentItem.component === 'Group') {
        componentItem.expand = componentItem.expand || false
      }
      componentItem['maintainRadio'] = componentItem['maintainRadio'] || false
      componentItem['aspectRatio'] = componentItem['aspectRatio'] || 1
      if (componentItem.component === 'UserView') {
        componentItem.actionSelection = componentItem.actionSelection || deepCopy(ACTION_SELECTION)
      }
      // 2 为基础版本 此处需要增加仪表板矩阵密度
      if ((!canvasVersion || canvasVersion === 2) && canvasInfo.type === 'dashboard') {
        componentItem.x = 1 + (componentItem.x - 1) * 2
        componentItem.y = 1 + (componentItem.y - 1) * 2
        componentItem.sizeX = componentItem.sizeX * 2
        componentItem.sizeY = componentItem.sizeY * 2
        componentItem.mx = 1 + (componentItem.mx - 1) * 2
        componentItem.my = 1 + (componentItem.my - 1) * 2
        componentItem.mSizeX = componentItem.mSizeX * 2
        componentItem.mSizeY = componentItem.mSizeY * 2
      }
    })
    const curPreviewGap =
      dvInfo.type === 'dashboard' && canvasStyleResult['dashboard'].gap === 'yes'
        ? canvasStyleResult['dashboard'].gapSize
        : 0
    callBack({ canvasDataResult, canvasStyleResult, dvInfo, canvasViewInfoPreview, curPreviewGap })
  })
}

export async function initCanvasData(dvId, busiFlag, callBack) {
  initCanvasDataPrepare(
    dvId,
    busiFlag,
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

export async function backCanvasData(dvId, busiFlag, callBack) {
  initCanvasDataPrepare(dvId, busiFlag, function ({ canvasDataResult, canvasStyleResult }) {
    const componentDataCopy = canvasDataResult.filter(ele => !!ele.inMobile)
    const componentDataId = componentDataCopy.map(ele => ele.id)
    componentData.value.forEach(ele => {
      ele.inMobile = componentDataId.includes(ele.id)
      if (ele.inMobile) {
        const { mx, my, mSizeX, mSizeY } = componentDataCopy.find(itx => itx.id === ele.id)
        ele.mx = mx
        ele.my = my
        ele.mSizeX = mSizeX
        ele.mSizeY = mSizeY
        if (ele.component === 'DeTabs') {
          ele.propValue.forEach(tabItem => {
            tabItem.componentData.forEach(tabComponent => {
              tabComponent.mx = tabComponent.mx
              tabComponent.my = tabComponent.my
              tabComponent.mSizeX = tabComponent.mSizeX
              tabComponent.mSizeY = tabComponent.mSizeY
            })
          })
        }
      }
    })
    dvMainStore.setComponentData(componentData.value)
    const canvasStyleDataCopy = cloneDeep(canvasStyleData.value)
    if (!canvasStyleDataCopy.mobileSetting) {
      canvasStyleDataCopy.mobileSetting = {
        backgroundColorSelect: false,
        background: '',
        color: '#ffffff',
        backgroundImageEnable: false,
        customSetting: false
      }
    } else {
      canvasStyleDataCopy.mobileSetting = canvasStyleResult.mobileSetting
    }
    dvMainStore.setCanvasStyle(canvasStyleDataCopy)
    callBack()
  })
}

export function initCanvasDataMobile(dvId, busiFlag, callBack) {
  initCanvasDataPrepare(
    dvId,
    busiFlag,
    function ({ canvasDataResult, canvasStyleResult, dvInfo, canvasViewInfoPreview }) {
      const componentData = canvasDataResult.filter(ele => !!ele.inMobile)
      canvasDataResult.forEach(ele => {
        const { mx, my, mSizeX, mSizeY } = ele
        ele.x = mx
        ele.y = my
        ele.sizeX = mSizeX
        ele.sizeY = mSizeY
        if (ele.component === 'DeTabs') {
          ele.propValue.forEach(tabItem => {
            tabItem.componentData.forEach(tabComponent => {
              tabComponent.x = tabComponent.mx
              tabComponent.y = tabComponent.my
              tabComponent.sizeX = tabComponent.mSizeX
              tabComponent.sizeY = tabComponent.mSizeY
            })
          })
        }
      })
      dvMainStore.setComponentData(componentData)
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
      callBack({
        canvasDataResult: componentData,
        canvasStyleResult,
        dvInfo,
        canvasViewInfoPreview
      })
    }
  )
}

export function checkIsBatchOptView(viewId) {
  return curBatchOptComponents.value.includes(viewId)
}

export function canvasSave(callBack) {
  const componentDataToSave = cloneDeep(componentData.value)
  componentDataToSave.forEach(item => {
    if (item.component === 'UserView') {
      item.linkageFilters = []
    } else if (item.component === 'Group') {
      item.propValue.forEach(groupItem => {
        groupItem.linkageFilters = []
      })
    } else if (item.component === 'DeTabs') {
      item.propValue.forEach(tabItem => {
        tabItem.componentData.forEach(tabComponent => {
          tabComponent.linkageFilters = []
        })
      })
    }
  })
  const canvasInfo = {
    canvasStyleData: JSON.stringify(canvasStyleData.value),
    componentData: JSON.stringify(componentDataToSave),
    canvasViewInfo: canvasViewInfo.value,
    ...dvInfo.value,
    watermarkInfo: null
  }

  const method = dvInfo.value.id && dvInfo.value.optType !== 'copy' ? updateCanvas : saveCanvas
  method(canvasInfo).then(res => {
    dvMainStore.updateDvInfoId(res.data)
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

export function isGroupCanvas(canvasId) {
  return canvasId && canvasId.includes('Group')
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

export function markTreeFolder(elementInfo) {
  if (elementInfo) {
    if (elementInfo instanceof Array) {
      elementInfo.forEach(elementSon => {
        elementSon['disabled'] = !elementSon['leaf']
        if (elementSon['children']) {
          markTreeFolder(elementSon['children'])
        }
      })
    } else {
      elementInfo['disabled'] = !elementInfo['leaf']
    }
  }
}

export function filterEmptyFolderTree(nodes) {
  // 递归过滤树节点数据，只显示包含子文件夹或文件的文件夹
  return nodes.filter(node => {
    if (node.leaf) {
      return true
    } else if (node.children && node.children.length > 0) {
      // 如果节点有子节点，继续递归过滤子节点
      node.children = filterEmptyFolderTree(node.children)
      return true
    } else {
      return false // 不显示空文件夹
    }
  })
}

export function findParentIdByChildIdRecursive(tree, targetChildId) {
  function findParentId(node, targetChildId) {
    if (node.children) {
      for (const childNode of node.children) {
        if (childNode.id === targetChildId) {
          return node.id // 找到匹配的子节点，返回其父节点的 ID
        }
        const parentId = findParentId(childNode, targetChildId)
        if (parentId !== null) {
          return parentId // 在子节点中找到匹配的父节点
        }
      }
    }
    return null // 没有找到匹配的子节点
  }

  for (const node of tree) {
    const parentId = findParentId(node, targetChildId)
    if (parentId !== null) {
      return parentId // 在整个树中找到匹配的父节点
    }
  }

  return null // 没有找到匹配的子节点
}

export async function decompressionPre(params, callBack) {
  let deTemplateData
  await decompression(params)
    .then(response => {
      const deTemplateDataTemp = response.data
      const sourceComponentData = JSON.parse(deTemplateDataTemp['componentData'])
      sourceComponentData.forEach(componentItem => {
        // 2 为基础版本 此处需要增加仪表板矩阵密度
        if (
          (!deTemplateDataTemp.version || deTemplateDataTemp.version === 2) &&
          deTemplateDataTemp.type === 'dashboard'
        ) {
          componentItem.x = 1 + (componentItem.x - 1) * 2
          componentItem.y = 1 + (componentItem.y - 1) * 2
          componentItem.sizeX = componentItem.sizeX * 2
          componentItem.sizeY = componentItem.sizeY * 2
        }
      })
      const sourceCanvasStyle = JSON.parse(deTemplateDataTemp['canvasStyleData'])
      //历史字段适配
      sourceCanvasStyle.component['seniorStyleSetting'] =
        sourceCanvasStyle.component['seniorStyleSetting'] || deepCopy(SENIOR_STYLE_SETTING_LIGHT)
      deTemplateData = {
        canvasStyleData: sourceCanvasStyle,
        componentData: sourceComponentData,
        canvasViewInfo: deTemplateDataTemp['canvasViewInfo']
      }
    })
    .catch(e => {
      console.error(e)
    })
  callBack(deTemplateData)
}

export function trackBarStyleCheck(element, trackbarStyle, _scale, trackMenuNumber) {
  const { width, height } = element.style
  const widthReal = width
  const heightReal = height
  // 浮窗高度
  function calculateTrackHeight(trackMenuNumber) {
    if (trackMenuNumber === 2) {
      return 75
    } else {
      const increment = Math.floor(trackMenuNumber - 2) * 35
      return 75 + increment
    }
  }
  if (trackbarStyle.left < 0) {
    trackbarStyle.left = 0
  } else if (widthReal - trackbarStyle.left < 60) {
    trackbarStyle.left = trackbarStyle.left - 60
  }
  const trackMenuHeight = calculateTrackHeight(trackMenuNumber)
  if (trackbarStyle.top < 0) {
    trackbarStyle.top = 0
  } else if (trackbarStyle.top + trackMenuHeight + 60 > heightReal) {
    trackbarStyle.top = trackbarStyle.top - trackMenuHeight
  }
}
