import { BusiTreeNode } from '@/models/tree/TreeNode'
import { useCache } from '@/hooks/web/useCache'
import { loadScript } from '@/utils/RemoteJs'
import { ElMessage } from 'element-plus-secondary'

const { wsCache } = useCache()
export function deepCopy(target) {
  if (target === null || target === undefined) {
    return target
  } else if (typeof target == 'object') {
    const result = Array.isArray(target) ? [] : {}
    for (const key in target) {
      if (target[key] === null || target[key] === undefined) {
        result[key] = target[key]
      } else if (target[key] instanceof Date) {
        // 日期特殊处理
        result[key] = new Date(target[key])
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

export const getQueryString = (name: string) => {
  const reg = new RegExp('(^|&)' + name + '=([^&]*)(&|$)', 'i')
  const r = window.location.search.substr(1).match(reg)
  if (r != null) {
    return unescape(r[2])
  }
  return null
}

export const isLarkPlatform = () => {
  return !!getQueryString('state') && !!getQueryString('code')
}

export const isPlatformClient = () => {
  return !!getQueryString('client') || getQueryString('state')?.includes('client')
}

export const checkPlatform = () => {
  const flagArray = ['/casbi', 'oidcbi']
  const pathname = window.location.pathname
  if (
    !flagArray.some(flag => pathname.includes(flag)) &&
    !isLarkPlatform() &&
    !isPlatformClient()
  ) {
    return cleanPlatformFlag()
  }
  return true
}
export const cleanPlatformFlag = () => {
  const platformKey = 'out_auth_platform'
  wsCache.delete(platformKey)
  return false
}
export const isInIframe = () => {
  try {
    return window.top !== window.self
  } catch (error) {
    console.error(error)
    return true
  }
}

export const isBtnShow = (val: string) => {
  if (!val || val === '0') {
    return true
  } else if (val === '1') {
    return false
  } else {
    return !isInIframe()
  }
}
export function isMobile() {
  return (
    navigator.userAgent.match(
      /(phone|pad|pod|iPhone|iPod|ios|iPad|Android|Mobile|BlackBerry|IEMobile|MQQBrowser|JUC|Fennec|wOSBrowser|BrowserNG|WebOS|Symbian|Windows Phone)/i
    ) && !isTablet()
  )
}

export function isISOMobile() {
  return navigator.userAgent.match(/(iPhone|iPad|iPod)/i) && !isTablet()
}

export const isDingTalk = window.navigator.userAgent.toLowerCase().includes('dingtalk')

export const setTitle = (title?: string) => {
  if (!isDingTalk) {
    document.title = title || 'DataEase'
    return
  }
  const jsUrl = 'https://g.alicdn.com/dingding/dingtalk-jsapi/3.0.25/dingtalk.open.js'
  const jsId = 'fit2cloud-dataease-v2-platform-client-dingtalk'
  if (window['dd'] && window['dd'].biz?.navigation?.setTitle) {
    window['dd'].biz.navigation.setTitle({
      title: title
    })
    return
  }
  const awaitMethod = loadScript(jsUrl, jsId)
  awaitMethod
    .then(() => {
      window['dd'].ready(() => {
        window['dd'].biz.navigation.setTitle({
          title: title
        })
      })
    })
    .catch(() => {
      document.title = title || 'DataEase'
    })
}

export function isTablet() {
  const userAgent = navigator.userAgent
  const tabletRegex = /iPad|Silk|Galaxy Tab|PlayBook|BlackBerry|(tablet|ipad|playbook)/i
  return tabletRegex.test(userAgent)
}
export function cutTargetTree(tree: BusiTreeNode[], targetId: string | number) {
  tree.forEach((node, index) => {
    if (node.id === targetId) {
      tree.splice(index, 1)
      return
    } else if (node.children) {
      cutTargetTree(node.children, targetId)
    }
  })
}

export const isLink = () => {
  return window.location.hash.startsWith('#/de-link/')
}

export const isNull = arg => {
  return typeof arg === 'undefined' || arg === null || arg === 'null'
}

export const exportPermission = (weight, ext) => {
  const result = [0, 0, 0]
  if (!weight || weight === 1) {
    return result
  } else if (weight === 9) {
    return [1, 1, 1]
  }
  if (!ext) {
    return result
  }
  const extArray = formatExt(ext) || []
  for (let index = 0; index < extArray.length; index++) {
    result[index] = extArray[index]
  }
  return result
}

export const formatExt = (num: number): number[] | null => {
  if (!num) {
    return null
  }
  const reversedStr = num.toString().split('').reverse().join('')
  const reversedNumArray = reversedStr?.split('')?.map(Number) ?? []
  return reversedNumArray
}

export const getBrowserLocale = () => {
  const language = navigator.language
  if (!language) {
    return 'zh-CN'
  }
  if (language.startsWith('en')) {
    return 'en'
  }
  if (language.toLowerCase().startsWith('zh')) {
    const temp = language.toLowerCase().replace('_', '-')
    return temp === 'zh' ? 'zh-CN' : temp === 'zh-cn' ? 'zh-CN' : 'tw'
  }
  return language
}
export const getLocale = () => {
  return wsCache.get('user.language') || getBrowserLocale() || 'zh-CN'
}

export const isFreeFolder = (node, flag) => {
  const oid = wsCache.get('user.oid')
  if (!oid) {
    return false
  }
  const freeRootId = (Number(oid) + flag).toString()
  let cNode = node
  while (cNode) {
    const data = cNode.data
    const id = data['id']
    if (id === freeRootId) {
      return true
    }
    cNode = cNode['parent']
  }
  return false
}

export const filterFreeFolder = (list, flagText) => {
  const flagArray = ['dashboard', 'dataV', 'dataset', 'datasource']
  const index = flagArray.findIndex(item => item === flagText)
  const oid = wsCache.get('user.oid')
  if (!oid || index < 0) {
    return
  }
  const freeRootId = (Number(oid) + index + 1).toString()
  let len = list.length
  while (len--) {
    const node = list[len]
    if (node['id'] === freeRootId) {
      list.splice(len, 1)
      return
    }
    if (node['id'] === '0') {
      const children = node['children']
      let innerLen = children?.length
      while (innerLen--) {
        const kid = children[innerLen]
        if (kid['id'] === freeRootId) {
          children.splice(innerLen, 1)
          return
        }
      }
    }
  }
}
export const nameTrim = (target: {}, msg = '名称字段长度1-64个字符') => {
  if (target.name) {
    target.name = target.name.trim()
    if (target.name.length < 1 || target.name.length > 64) {
      ElMessage.warning(msg)
      throw new Error(msg)
    }
  }
}

export const getActiveCategories = contents => {
  const result = ['最近使用']
  if (contents) {
    contents.forEach(item => {
      if (item.showFlag) {
        item.categories.forEach(category => {
          result.push(category.name)
        })
      }
    })
  }
  return new Set(result)
}
