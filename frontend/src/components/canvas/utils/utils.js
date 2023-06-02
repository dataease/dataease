import {
  BASE_MOBILE_STYLE,
  COMMON_BACKGROUND,
  COMMON_BACKGROUND_NONE,
  HYPERLINKS
} from '@/components/canvas/customComponent/component-list'

import { ApplicationContext } from '@/utils/ApplicationContext'
import { uuid } from 'vue-uuid'
import store from '@/store'
import { AIDED_DESIGN, MOBILE_SETTING, PAGE_LINE_DESIGN, PANEL_CHART_INFO, TAB_COMMON_STYLE } from '@/views/panel/panel'
import html2canvas from 'html2canvasde'
import xssCheck from 'xss'
import Vue from 'vue'

export function deepCopy(target) {
  if (typeof target === 'object' && target !== null) {
    const result = Array.isArray(target) ? [] : {}
    for (const key in target) {
      if (typeof target[key] === 'object') {
        result[key] = deepCopy(target[key])
      } else {
        result[key] = target[key]
      }
    }

    return result
  }

  return target
}

export function swap(arr, i, j) {
  const temp = arr[i]
  arr[i] = arr[j]
  arr[j] = temp
}

export function moveUp(arr, i) {
  arr.splice(i + 1, 0, arr.splice(i, 1)[0])
}

export function moveDown(arr, i) {
  arr.splice(i, 0, arr.splice(i - 1, 1)[0])
}

export function toTop(arr, i, j) {
  arr.push(arr.splice(i, 1)[0])
}

export function toBottom(arr, i) {
  arr.unshift(arr.splice(i, 1)[0])
}

export function $(selector) {
  return document.querySelector(selector)
}

export function mobile2MainCanvas(mainSource, mobileSource) {
  mainSource.mobileSelected = true
  mainSource.mobileStyle.style = {
    width: mobileSource.style.width,
    height: mobileSource.style.height,
    left: mobileSource.style.left,
    top: mobileSource.style.top
  }
  mainSource.mobileStyle.x = mobileSource.x
  mainSource.mobileStyle.y = mobileSource.y
  mainSource.mobileStyle.sizex = mobileSource.sizex
  mainSource.mobileStyle.sizey = mobileSource.sizey
}

export function panelInit(componentData, componentStyle) {
  // 取消视图请求
  Vue.prototype.$cancelRequest('/chart/view/getData/**')
  Vue.prototype.$cancelRequest('/api/link/viewDetail/**')
  Vue.prototype.$cancelRequest('/static-resource/**')
  panelDataPrepare(componentData, componentStyle, function() {
    // 将data 和 style 数据设置到全局store中
    store.commit('setComponentData', resetID(componentData))
    store.commit('setCanvasStyle', componentStyle)
  })
}

export function panelDataPrepare(componentData, componentStyle, callback) {
  // style初始化
  componentStyle.refreshTime = (componentStyle.refreshTime || 5)
  componentStyle.refreshViewLoading = (componentStyle.refreshViewLoading || false)
  componentStyle.refreshUnit = (componentStyle.refreshUnit || 'minute')
  componentStyle.refreshViewEnable = (componentStyle.refreshViewEnable === undefined ? true : componentStyle.refreshViewEnable)
  componentStyle.aidedDesign = (componentStyle.aidedDesign || deepCopy(AIDED_DESIGN))
  componentStyle.pdfPageLine = (componentStyle.pdfPageLine || deepCopy(PAGE_LINE_DESIGN))
  componentStyle.chartInfo = (componentStyle.chartInfo || deepCopy(PANEL_CHART_INFO))
  componentStyle.chartInfo.tabStyle = (componentStyle.chartInfo.tabStyle || deepCopy(TAB_COMMON_STYLE))
  componentStyle.themeId = (componentStyle.themeId || 'NO_THEME')
  componentStyle.panel.themeColor = (componentStyle.panel.themeColor || 'light')
  componentStyle.panel.mobileSetting = (componentStyle.panel.mobileSetting || deepCopy(MOBILE_SETTING))

  // 主题增加组件背景设置
  if (componentStyle.chartCommonStyle) {
    componentStyle.chartCommonStyle.enable = componentStyle.chartCommonStyle.enable || false
    componentStyle.chartCommonStyle.backgroundType = componentStyle.chartCommonStyle.backgroundType || 'innerImage'
    componentStyle.chartCommonStyle.innerImageColor = componentStyle.chartCommonStyle.innerImageColor || '#1094E5'
    componentStyle.chartCommonStyle.innerImage = componentStyle.chartCommonStyle.innerImage || 'board/blue_1.svg'
    componentStyle.chartCommonStyle.outerImage = componentStyle.chartCommonStyle.outerImage || null
  } else {
    componentStyle.chartCommonStyle = deepCopy(COMMON_BACKGROUND)
  }
  componentData.forEach((item, index) => {
    if (item.component && item.component === 'v-text') {
      item.propValue = xssCheck(item.propValue)
    }
    if (item.component && item.component === 'de-date') {
      const widget = ApplicationContext.getService(item.serviceName)
      if (item.options.attrs &&
        (!item.options.attrs.default || (item.serviceName === 'timeYearWidget' && item.options.attrs.default.dynamicInfill !== 'year') || (item.serviceName === 'timeMonthWidget' && item.options.attrs.default.dynamicInfill !== 'month'))) {
        if (widget && widget.defaultSetting) {
          item.options.attrs.default = widget.defaultSetting()
        }
      }
      if (item.options.attrs && widget.isTimeWidget && widget.isTimeWidget() && !Object.prototype.hasOwnProperty.call(item.options.attrs, 'showTime')) {
        item.options.attrs.showTime = false
        item.options.attrs.accuracy = 'HH:mm'
      }
    }
    if (item.type === 'de-tabs') {
      item.style.fontSize = item.style.fontSize || 16
      item.style.activeFontSize = item.style.activeFontSize || 18
      item.style.carouselEnable = item.style.carouselEnable || false
      item.style.switchTime = item.style.switchTime || 5
    }
    if (item.type === 'custom') {
      item.options.manualModify = false
    }
    if (item.filters && item.filters.length > 0) {
      item.filters = []
    }
    item.linkageFilters = (item.linkageFilters || [])
    item.auxiliaryMatrix = (item.auxiliaryMatrix || false)
    item.x = (item.x || 1)
    item.y = (item.y || 1)
    item.sizex = (item.sizex || 5)
    item.sizey = (item.sizey || 5)
    // 初始化密度为最高密度
    if (componentStyle.aidedDesign.matrixBase !== 4) {
      item.x = (item.x - 1) * 4 + 1
      item.y = (item.y - 1) * 4 + 1
      item.sizex = item.sizex * 4
      item.sizey = item.sizey * 4
    }
    item.mobileSelected = (item.mobileSelected || false)
    item.mobileStyle = (item.mobileStyle || deepCopy(BASE_MOBILE_STYLE))
    item.hyperlinks = (item.hyperlinks || deepCopy(HYPERLINKS))
    item.commonBackground = item.commonBackground || deepCopy(COMMON_BACKGROUND_NONE)
    item.commonBackground['innerImageColor'] = item.commonBackground['innerImageColor'] || '#1094E5'
    // Multi choice of colors and pictures
    if (item.commonBackground.backgroundType === 'color') {
      item.commonBackground['backgroundColorSelect'] = item.commonBackground.enable
      item.commonBackground.enable = false
      item.commonBackground.backgroundType = 'innerImage'
    }
    // picture component
    if (item.component && item.component === 'Picture') {
      item.style.adaptation = item.style.adaptation || 'adaptation'
    }
    // 增加所属画布ID（canvasId）当前所在画布的父ID（canvasPid） 主画布ID为main-canvas, PID = 0 表示当前所属canvas为最顶层
    item.canvasId = (item.canvasId || 'canvas-main')
    item.canvasPid = (item.canvasPid || '0')
  })
  // 初始化密度为最高密度
  componentStyle.aidedDesign.matrixBase = 4
  callback({
    'componentData': resetID(componentData),
    'componentStyle': componentStyle
  })
}

export function resetID(data) {
  if (data) {
    data.forEach(item => {
      item.type !== 'custom' && item.type !== 'de-tabs' && (item.id = uuid.v1())
    })
  }
  return data
}

export function matrixBaseChange(component) {
  const matrixBase = store.state.canvasStyleData.aidedDesign.matrixBase
  if (component) {
    component.x = (component.x - 1) * matrixBase
    component.y = (component.y - 1) * matrixBase
    component.sizex = component.sizex * matrixBase
    component.sizey = component.sizey * matrixBase
  }
  return component
}

export function checkViewTitle(opt, id, tile) {
  try {
    const curPanelViewsData = store.state.componentViewsData
    const curComponentViewNames = []
    store.state.componentData.forEach(item => {
      if (item.type === 'view' && item.propValue && item.propValue.viewId && curPanelViewsData[item.propValue.viewId]) {
        // 更新时自己的title不加入比较
        if ((opt === 'update' && id !== item.propValue.viewId) || opt === 'new') {
          curComponentViewNames.push(curPanelViewsData[item.propValue.viewId].title)
        }
      }
    })
    if (curComponentViewNames.includes(tile)) {
      return true
    } else {
      return false
    }
  } catch (e) {
    return false
  }
}

export function exportImg(imgName,callback) {
  const canvasID = document.getElementById('chartCanvas')
  const a = document.createElement('a')
  html2canvas(canvasID).then(canvas => {
    const dom = document.body.appendChild(canvas)
    dom.style.display = 'none'
    a.style.display = 'none'
    document.body.removeChild(dom)
    const blob = dataURLToBlob(dom.toDataURL('image/png', 1))
    a.setAttribute('href', URL.createObjectURL(blob))
    a.setAttribute('download', imgName + '.png')
    document.body.appendChild(a)
    a.click()
    URL.revokeObjectURL(blob)
    document.body.removeChild(a)
    callback()
  }).catch(() => {
    callback()
  })
}

export function dataURLToBlob(dataurl) { // ie 图片转格式
  const arr = dataurl.split(',')
  const mime = arr[0].match(/:(.*?);/)[1]
  const bstr = atob(arr[1])
  let n = bstr.length
  const u8arr = new Uint8Array(n)
  while (n--) {
    u8arr[n] = bstr.charCodeAt(n)
  }
  return new Blob([u8arr], { type: mime })
}

export function colorReverse(OldColorValue) {
  OldColorValue = '0x' + OldColorValue.replace(/#/g, '')
  const str = '000000' + (0xFFFFFF - OldColorValue).toString(16)
  return '#' + str.substring(str.length - 6, str.length)
}

export function imgUrlTrans(url) {
  if (url && typeof url === 'string' && url.indexOf('static-resource') > -1) {
    return process.env.VUE_APP_BASE_API + url.replace('/static-resource', 'static-resource')
  } else {
    return url
  }
}

export function getNowCanvasComponentData(canvasId, showPosition) {
  if (showPosition && (showPosition.includes('email-task') || showPosition.includes('multiplexing'))) {
    return store.state.previewComponentData.filter(item => item.canvasId === canvasId)
  } else {
    return store.state.componentData.filter(item => item.canvasId === canvasId)
  }
}

export function findCurComponentIndex(componentData, curComponent) {
  let curIndex = 0
  for (let index = 0; index < componentData.length; index++) {
    const element = componentData[index]
    if (element.id && element.id === curComponent.id) {
      curIndex = index
      break
    }
  }
  return curIndex
}

export function deleteTreeNode(nodeId, tree, nodeTarget) {
  if (!nodeId || !tree || !tree.length) {
    return
  }
  for (let i = 0, len = tree.length; i < len; i++) {
    if (tree[i].id === nodeId) {
      if (nodeTarget) {
        nodeTarget['target'] = tree[i]
      }
      tree.splice(i, 1)
      return
    } else if (tree[i].children && tree[i].children.length) {
      deleteTreeNode(nodeId, tree[i].children, nodeTarget)
    }
  }
}

export function moveTreeNode(nodeInfo, tree) {
  const nodeTarget = { target: null }
  deleteTreeNode(nodeInfo.id, tree, nodeTarget)
  if (nodeTarget.target) {
    nodeTarget.target.pid = nodeInfo.pid
    insertTreeNode(nodeTarget.target, tree)
  }
}

export function updateTreeNode(nodeInfo, tree) {
  if (!nodeInfo) {
    return
  }
  for (let i = 0, len = tree.length; i < len; i++) {
    if (tree[i].id === nodeInfo.id) {
      tree[i].name = nodeInfo.name
      tree[i].label = nodeInfo.label
      if (tree[i].isDefault) {
        tree[i].isDefault = nodeInfo.isDefault
      }
      return
    } else if (tree[i].children && tree[i].children.length) {
      updateTreeNode(nodeInfo, tree[i].children)
    }
  }
}

export function toDefaultTree(nodeId, tree) {
  if (!nodeId) {
    return
  }
  for (let i = 0, len = tree.length; i < len; i++) {
    if (tree[i].id === nodeId) {
      tree[i].isDefault = true
      return
    } else if (tree[i].children && tree[i].children.length) {
      toDefaultTree(nodeId, tree[i].children)
    }
  }
}

export function insertTreeNode(nodeInfo, tree) {
  if (!nodeInfo) {
    return
  }
  if (nodeInfo.pid === 0 || nodeInfo.pid === '0') {
    tree.push(nodeInfo)
    return
  }
  for (let i = 0, len = tree.length; i < len; i++) {
    if (tree[i].id === nodeInfo.pid) {
      if (!tree[i].children) {
        tree[i].children = []
      }
      tree[i].children.push(nodeInfo)
      return
    } else if (tree[i].children && tree[i].children.length) {
      insertTreeNode(nodeInfo, tree[i].children)
    }
  }
}

export function insertBatchTreeNode(nodeInfoArray, tree) {
  if (!nodeInfoArray || nodeInfoArray.size === 0) {
    return
  }
  const pid = nodeInfoArray[0].pid
  for (let i = 0, len = tree.length; i < len; i++) {
    if (tree[i].id === pid) {
      if (!tree[i].children) {
        tree[i].children = []
      }
      tree[i].children = tree[i].children.concat(nodeInfoArray)
      return
    } else if (tree[i].children && tree[i].children.length) {
      insertBatchTreeNode(nodeInfoArray, tree[i].children)
    }
  }
}

export function updateCacheTree(opt, treeName, nodeInfoFull, tree) {
  const nodeInfo = deepCopy(nodeInfoFull)
  if( nodeInfo instanceof Array){
    nodeInfo.forEach(item=>{
      delete item.panelData
      delete item.panelStyle
    })
  }else{
    delete nodeInfo.panelData
    delete nodeInfo.panelStyle
  }
  if (opt === 'new' || opt === 'copy') {
    insertTreeNode(nodeInfo, tree)
  } else if (opt === 'move') {
    moveTreeNode(nodeInfo, tree)
  } else if (opt === 'rename') {
    updateTreeNode(nodeInfo, tree)
  } else if (opt === 'delete') {
    deleteTreeNode(nodeInfo, tree)
  } else if (opt === 'newFirstFolder') {
    tree.push(nodeInfo)
  } else if (opt === 'batchNew') {
    insertBatchTreeNode(nodeInfo, tree)
  } else if (opt === 'toDefaultPanel') {
    toDefaultTree(nodeInfo.source, tree)
  }
  localStorage.setItem(treeName, JSON.stringify(tree))
}

export function formatDatasetTreeFolder(tree) {
  if (tree && tree.length) {
    for (let len = tree.length - 1; len > -1; len--) {
      if (tree[len].modelInnerType !== 'group') {
        tree.splice(len, 1)
      } else if (tree[len].children && tree[len].children.length) {
        formatDatasetTreeFolder(tree[len].children)
      }
    }
  }
}

export function getCacheTree(treeName) {
  return JSON.parse(localStorage.getItem(treeName))
}
