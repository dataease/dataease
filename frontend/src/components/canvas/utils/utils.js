import {
  BASE_MOBILE_STYLE,
  HYPERLINKS
} from '@/components/canvas/custom-component/component-list'

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
  console.log('before:' + i + '-->' + j + JSON.stringify(arr))
  const temp = arr[i]
  arr[i] = arr[j]
  arr[j] = temp
  console.log('after:' + JSON.stringify(arr))
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

export function panelInit(componentDatas) {
  componentDatas.forEach(item => {
    if (item.component && item.component === 'de-date') {
      if (item.serviceName === 'timeDateWidget' && item.options.attrs && !item.options.attrs.default) {
        item.options.attrs.default = {
          isDynamic: false,
          dkey: 0,
          dynamicPrefix: 1,
          dynamicInfill: 'day',
          dynamicSuffix: 'before'
        }
      }
      if (item.serviceName === 'timeDateRangeWidget' && item.options.attrs && !item.options.attrs.default) {
        item.options.attrs.default = {
          isDynamic: false,
          dkey: 0,
          sDynamicPrefix: 1,
          sDynamicInfill: 'day',
          sDynamicSuffix: 'before',
          eDynamicPrefix: 1,
          eDynamicInfill: 'day',
          eDynamicSuffix: 'after'
        }
      }
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
    item.mobileSelected = (item.mobileSelected || false)
    item.mobileStyle = (item.mobileStyle || deepCopy(BASE_MOBILE_STYLE))
    if (item.type === 'picture-add') {
      item.hyperlinks = (item.hyperlinks || HYPERLINKS)
    }
  })
}
