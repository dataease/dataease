export function deepCopy(target) {
  if (target === null || target === undefined) {
    return target
  } else if (typeof target == 'object') {
    const result = Array.isArray(target) ? [] : {}
    for (const key in target) {
      if (target[key] === null || target[key] === undefined) {
        result[key] = target[key]
      } else if (typeof target[key] == 'object') {
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

export function _$(selector) {
  return document.querySelector(selector)
}

export function $(selector) {
  return document.querySelector(selector)
}

const components = ['VText', 'RectShape', 'CircleShape']
export function isPreventDrop(component) {
  return !components.includes(component) && !component.startsWith('SVG')
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

export const setColorName = (obj, keyword: string, key?: string, colorKey?: string) => {
  key = key || 'name'
  colorKey = colorKey || 'colorName'
  if (!keyword) {
    obj[colorKey] = null
    return
  }
  const name = obj[key]
  const index = name.indexOf(keyword)
  if (index > -1) {
    const textCode =
      name.substring(0, index) +
      '<span class="search-key-span">' +
      keyword +
      '</span>' +
      name.substring(index + keyword.length, name.length)
    obj[colorKey] = textCode
    return
  }
  obj[colorKey] = null
}
