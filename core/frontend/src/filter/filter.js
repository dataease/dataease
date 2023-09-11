const options = function(value, array) {
  if (!value) return ''
  if (array) {
    for (let i = 0; i < array.length; i++) {
      if (value === array[i].key) {
        return array[i].value
      }
    }
  }
  return value
}

const timestampFormatDate = function(timestamp, showMs) {
  if (!timestamp || timestamp === -1) {
    return '-'
  }

  const date = new Date(timestamp)

  const y = date.getFullYear()

  let MM = date.getMonth() + 1
  MM = MM < 10 ? ('0' + MM) : MM

  let d = date.getDate()
  d = d < 10 ? ('0' + d) : d

  let h = date.getHours()
  h = h < 10 ? ('0' + h) : h

  let m = date.getMinutes()
  m = m < 10 ? ('0' + m) : m

  let s = date.getSeconds()
  s = s < 10 ? ('0' + s) : s

  let format = y + '-' + MM + '-' + d + ' ' + h + ':' + m + ':' + s

  if (showMs === true) {
    const ms = date.getMilliseconds()
    format += ':' + ms
  }

  return format
}

const filters = {
  'options': options,
  'timestampFormatDate': timestampFormatDate
}

export default {
  install(Vue) {
    // 注册公用过滤器
    Object.keys(filters).forEach(key => {
      Vue.filter(key, filters[key])
    })
  }
}

