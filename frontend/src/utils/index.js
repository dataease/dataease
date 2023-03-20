import Cookies from 'js-cookie'
import i18n from '@/lang'
import { $error, $confirm } from '@/utils/message'
import { seizeLogin } from '@/api/user'
import router from '@/router'
import store from '@/store'
import { Loading } from 'element-ui'
export function timeSection(date, type, labelFormat = 'yyyy-MM-dd') {
  if (!date) {
    return null
  }
  if (!(date instanceof Date)) {
    date = new Date(date)
  }
  const timeRanger = new Array(2)

  const formatArr = labelFormat ? labelFormat.split(' ') : []
  const methods = ['setHours', 'setMinutes', 'setSeconds', 'setMilliseconds']
  let methodsLen = methods.length
  if (type === 'datetime' && formatArr.length > 1) {
    const childArr = formatArr[1] ? formatArr[1].split(':') : []
    const childArrLength = childArr ? childArr.length : 0

    while (--methodsLen >= childArrLength) {
      date[methods[methodsLen]](0)
    }
  } else {
    methods.forEach(m => date[m](0))
  }

  const end = new Date(date)
  if (type === 'year') {
    date.setDate(1)
    date.setMonth(0)
    end.setFullYear(date.getFullYear() + 1)
    timeRanger[1] = end.getTime() - 1
  }
  if (type === 'month') {
    date.setDate(1)
    const currentMonth = date.getMonth()
    if (currentMonth === 11) {
      end.setFullYear(date.getFullYear() + 1)
      end.setMonth(0)
    } else {
      end.setMonth(date.getMonth() + 1)
    }
    timeRanger[1] = end.getTime() - 1
  }

  if (type === 'date') {
    end.setHours(23)
    end.setMinutes(59)
    end.setSeconds(59)
    end.setMilliseconds(999)
    timeRanger[1] = end.getTime()
  }

  if (type === 'datetime') {
    methodsLen = methods.length
    if (formatArr.length > 1) {
      const childArr = formatArr[1] ? formatArr[1].split(':') : []
      const childArrLength = childArr ? childArr.length : 0

      while (--methodsLen >= childArrLength) {
        end[methods[methodsLen]](methodsLen === 0 ? 23 : methodsLen === 3 ? 999 : 59)
      }
    } else {
      while (--methodsLen >= 0) {
        end[methods[methodsLen]](methodsLen === 0 ? 23 : methodsLen === 3 ? 999 : 59)
      }
    }
    timeRanger[1] = end.getTime()
  }
  timeRanger[0] = date.getTime()

  return timeRanger
}
export function dateFormat(date, fmt) {
  let ret
  const opt = {
    'y+': date.getFullYear().toString(), // 年
    'M+': (date.getMonth() + 1).toString(), // 月
    'd+': date.getDate().toString(), // 日
    'H+': date.getHours().toString(), // 时
    'm+': date.getMinutes().toString(), // 分
    's+': date.getSeconds().toString() // 秒
    // 有其他格式化字符需求可以继续添加，必须转化成字符串
  }
  for (const k in opt) {
    ret = new RegExp('(' + k + ')').exec(fmt)
    if (ret) {
      fmt = fmt.replace(ret[1], (ret[1].length === 1) ? (opt[k]) : (opt[k].padStart(ret[1].length, '0')))
    }
  }
  return fmt
}

/**
 * Parse the time to string
 * @param {(Object|string|number)} time
 * @param {string} cFormat
 * @returns {string | null}
 */
export function parseTime(time, cFormat) {
  if (arguments.length === 0) {
    return null
  }
  const format = cFormat || '{y}-{m}-{d} {h}:{i}:{s}'
  let date
  if (typeof time === 'object') {
    date = time
  } else {
    if ((typeof time === 'string') && (/^[0-9]+$/.test(time))) {
      time = parseInt(time)
    }
    if ((typeof time === 'number') && (time.toString().length === 10)) {
      time = time * 1000
    }
    date = new Date(time)
  }
  const formatObj = {
    y: date.getFullYear(),
    m: date.getMonth() + 1,
    d: date.getDate(),
    h: date.getHours(),
    i: date.getMinutes(),
    s: date.getSeconds(),
    a: date.getDay()
  }
  const time_str = format.replace(/{([ymdhisa])+}/g, (result, key) => {
    const value = formatObj[key]
    // Note: getDay() returns 0 on Sunday
    if (key === 'a') {
      return ['日', '一', '二', '三', '四', '五', '六'][value]
    }
    return value.toString().padStart(2, '0')
  })
  return time_str
}

/**
 * Check if an element has a class
 * @param {HTMLElement} elm
 * @param {string} cls
 * @returns {boolean}
 */
export function hasClass(ele, cls) {
  return !!ele.className.match(new RegExp('(\\s|^)' + cls + '(\\s|$)'))
}

/**
 * Add class to element
 * @param {HTMLElement} elm
 * @param {string} cls
 */
export function addClass(ele, cls) {
  if (!hasClass(ele, cls)) ele.className += ' ' + cls
}

/**
 * Remove class from element
 * @param {HTMLElement} elm
 * @param {string} cls
 */
export function removeClass(ele, cls) {
  if (hasClass(ele, cls)) {
    const reg = new RegExp('(\\s|^)' + cls + '(\\s|$)')
    ele.className = ele.className.replace(reg, ' ')
  }
}

/**
 * @param {number} time
 * @param {string} option
 * @returns {string}
 */
export function formatTime(time, option) {
  if (('' + time).length === 10) {
    time = parseInt(time) * 1000
  } else {
    time = +time
  }
  const d = new Date(time)
  const now = Date.now()

  const diff = (now - d) / 1000

  if (diff < 30) {
    return '刚刚'
  } else if (diff < 3600) {
    // less 1 hour
    return Math.ceil(diff / 60) + '分钟前'
  } else if (diff < 3600 * 24) {
    return Math.ceil(diff / 3600) + '小时前'
  } else if (diff < 3600 * 24 * 2) {
    return '1天前'
  }
  if (option) {
    return parseTime(time, option)
  } else {
    return (
      d.getMonth() +
      1 +
      '月' +
      d.getDate() +
      '日' +
      d.getHours() +
      '时' +
      d.getMinutes() +
      '分'
    )
  }
}

/**
 * @param {string} url
 * @returns {Object}
 */
export function param2Obj(url) {
  const search = url.split('?')[1]
  if (!search) {
    return {}
  }
  return JSON.parse(
    '{"' +
    decodeURIComponent(search)
      .replace(/"/g, '\\"')
      .replace(/&/g, '","')
      .replace(/=/g, '":"')
      .replace(/\+/g, ' ') +
    '"}'
  )
}

export function formatCondition(param) {
  if (!param) {
    return null
  }
  const result = {
    conditions: []
  }
  // eslint-disable-next-line no-unused-vars
  for (const [key, value] of Object.entries(param)) {
    result.conditions.push(value)
  }
  return result
}
/**
 * 驼峰转下划线
 * @param {*} name
 * @returns
 */
export function toLine(name) {
  return name.replace(/([A-Z])/g, '_$1').toLowerCase()
}
export function addOrder(order, orders) {
  order.field = toLine(order.field)
  if (order.value.startsWith('desc')) {
    order.value = 'desc'
  } else {
    order.value = 'asc'
  }
  orders = orders || []
  for (let index = 0; index < orders.length; index++) {
    const element = orders[index]
    if (order.field === element.field) {
      orders[index] = order
      return
    }
  }
  orders.push(order)
}

export function formatOrders(orders) {
  return orders.map(order => order.field + ' ' + order.value)
}

export function formatQuickCondition(param, quickField) {
  let quickObj = null
  if (!param || !(quickObj = param.quick) || !quickField) {
    quickObj && delete param.quick
    return param
  }
  param[quickField] = {
    field: quickField,
    operator: 'like',
    value: quickObj.value
  }
  delete param.quick
  return param
}

export function getQueryVariable(variable) {
  let query = window.location.search.substring(1)
  if (!query) {
    query = Cookies.get(variable)
  }
  if (query !== undefined) {
    const vars = query.split('&')
    for (var i = 0; i < vars.length; i++) {
      const pair = vars[i].split('=')
      if (pair[0] === variable) {
        return pair[1]
      }
    }
  }
  return (false)
}

export function isMobile() {
  const flag = navigator.userAgent.match(/(phone|pad|pod|iPhone|iPod|ios|iPad|Android|Mobile|BlackBerry|IEMobile|MQQBrowser|JUC|Fennec|wOSBrowser|BrowserNG|WebOS|Symbian|Windows Phone)/i)
  return flag
}

export const isSameVueObj = (source, target) => {
  if (!source && !target) return true
  if (!!source && !!target) {
    return JSON.stringify(source) === JSON.stringify(target)
  }
  return false
}

export const isSameArr = (source, target) => {
  if (!source && !target) return true
  if (source?.length && target?.length && source.length === target.length) {
    const sortSource = source.sort()
    const sortTarget = target.sort()
    return JSON.stringify(sortSource) === JSON.stringify(sortTarget)
  }
  return false
}

export const changeFavicon = link => {
  let $favicon = document.querySelector('link[rel="icon"]')
  if ($favicon !== null) {
    $favicon.href = link
  } else {
    $favicon = document.createElement('link')
    $favicon.rel = 'icon'
    $favicon.href = link
    document.head.appendChild($favicon)
  }
}

export const mergeCustomSortOption = (customSortList, sourceList) => {
  if (!customSortList?.length) return sourceList?.length ? sourceList : []

  if (!sourceList?.length) return customSortList?.length ? customSortList : []

  const result = [...customSortList, ...sourceList]
  return [...new Set(result)]
}

export const inOtherPlatform = () => {
  const cookieStr = Cookies.get('inOtherPlatform')
  if (cookieStr && cookieStr === 'true') {
    return true
  }
  return false
}

export const showMultiLoginMsg = () => {
  const multiLoginError1 = Cookies.get('MultiLoginError1')
  if (multiLoginError1) {
    Cookies.remove('MultiLoginError1')
    const infos = JSON.parse(multiLoginError1)
    const content = infos.map(info => buildMultiLoginErrorItem(info)).join('</br>')
    let msgContent = '<strong>' + i18n.t('multi_login_lang.title') + '</strong>'
    msgContent += content + '<p>' + i18n.t('multi_login_lang.label') + '</p>'
    $error(msgContent, 10000, true)
  }
  const multiLoginError2 = Cookies.get('MultiLoginError2')
  if (multiLoginError2) {
    const infos = JSON.parse(multiLoginError2)
    Cookies.remove('MultiLoginError2')
    const content = infos.map(info => buildMultiLoginErrorItem(info)).join('</br>')
    let msgContent = '<strong>' + i18n.t('multi_login_lang.confirm_title') + '</strong>'
    msgContent += content + '<p>' + i18n.t('multi_login_lang.confirm') + '</p>'
    $confirm(msgContent, () => seize(infos[0]), {
      dangerouslyUseHTMLString: true
    })
  }
}
const seize = model => {
  const loadingInstance = Loading.service({})
  const token = model.token
  const param = {
    token
  }
  seizeLogin(param).then(res => {
    const resultToken = res.data.token
    store.dispatch('user/refreshToken', resultToken)
    router.push('/')
    loadingInstance.close()
  })
}
const buildMultiLoginErrorItem = (info) => {
  if (!info) return null
  const ip = i18n.t('multi_login_lang.ip')
  const time = i18n.t('multi_login_lang.time')
  return '<p>' + ip + ': ' + info.ip + ', ' + time + ': ' + new Date(info.loginTime).format('yyyy-MM-dd hh:mm:ss') + '</p>'
}
