import Cookies from 'js-cookie'
export function timeSection(date, type) {
  if (!date) {
    return null
  }
  if (!(date instanceof Date)) {
    date = new Date(date)
  }
  const timeRanger = new Array(2)

  date.setHours(0)
  date.setMinutes(0)
  date.setSeconds(0)
  date.setMilliseconds(0)
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

// export function formatCondition(param) {
//   if (!param) {
//     return null
//   }
//   const condition = {}
//   for (const key in param) {
//     if (Object.hasOwnProperty.call(param, key)) {
//       const element = param[key]
//       condition[element.field] = element.value
//     }
//   }
//   return condition
// }

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



/*
 *  description: 把对象转换成数组(浅转换)
 *  param {object} obj
 *  return: array{key:key,value:value}
 */
export function lowObjToArray(obj) {
  let array = [];
  Object.keys(obj).map((key) => {
    array.push({
      key: key,
      value: obj[key],
    });
  });
  return array;
}

//DataType("young"); // "string"
//DataType(20190214); // "number"
//DataType(true); // "boolean"
//DataType([], "array"); // true
//DataType({}, "array"); // false
export function DataType(tgt, type) {
  const dataType = Object.prototype.toString
    .call(tgt)
    .replace(/\[object (\w+)\]/, "$1")
    .toLowerCase();
  return type ? dataType === type : dataType;
}

