import {
  BASE_MOBILE_STYLE, COMMON_BACKGROUND_NONE,
  HYPERLINKS
} from '@/components/canvas/custom-component/component-list'

import {
  ApplicationContext
} from '@/utils/ApplicationContext'
import { uuid } from 'vue-uuid'
import store from '@/store'
import { AIDED_DESIGN, PANEL_CHART_INFO, TAB_COMMON_STYLE } from '@/views/panel/panel'
import html2canvas from 'html2canvasde'

export function deepCopy(target) {
  if (typeof target === 'object') {
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
  componentStyle.aidedDesign = (componentStyle.aidedDesign || deepCopy(AIDED_DESIGN))
  componentStyle.chartInfo = (componentStyle.chartInfo || deepCopy(PANEL_CHART_INFO))
  componentStyle.chartInfo.tabStyle = (componentStyle.chartInfo.tabStyle || deepCopy(TAB_COMMON_STYLE))
  componentStyle.themeId = (componentStyle.themeId || 'NO_THEME')
  componentStyle.panel.themeColor = (componentStyle.panel.themeColor || 'light')
  componentData.forEach((item, index) => {
    if (item.component && item.component === 'de-date') {
      const widget = ApplicationContext.getService(item.serviceName)
      if (item.options.attrs &&
        (!item.options.attrs.default || (item.serviceName === 'timeYearWidget' && item.options.attrs.default.dynamicInfill !== 'year') || (item.serviceName === 'timeMonthWidget' && item.options.attrs.default.dynamicInfill !== 'month'))) {
        if (widget && widget.defaultSetting) {
          item.options.attrs.default = widget.defaultSetting()
        }
      }
      if (item.options.attrs && widget.isTimeWidget && widget.isTimeWidget() && !item.options.attrs.hasOwnProperty('showTime')) {
        item.options.attrs.showTime = false
        item.options.attrs.accuracy = 'HH:mm'
      }
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
      item.type !== 'custom' && (item.id = uuid.v1())
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

export function exportImg(imgName) {
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
  })
}

export function dataURLToBlob(dataurl) { // ie 图片转格式
  const arr = dataurl.split(','); const mime = arr[0].match(/:(.*?);/)[1]
  const bstr = atob(arr[1]); let n = bstr.length; const u8arr = new Uint8Array(n)
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
  if(url && typeof url === 'string' && url.indexOf('static-resource') > -1){
    return process.env.VUE_APP_BASE_API + url.replace('/static-resource','static-resource')
  }else {
    return url
  }

}
