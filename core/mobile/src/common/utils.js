/* eslint-disable no-extend-native */
Date.prototype.format = function(fmt) {
    var o = {
      'M+': this.getMonth() + 1, // 月份
      'd+': this.getDate(), // 日
      'h+': this.getHours(), // 小时
      'm+': this.getMinutes(), // 分
      's+': this.getSeconds(), // 秒
      'q+': Math.floor((this.getMonth() + 3) / 3), // 季度
      'S': this.getMilliseconds() // 毫秒
    }
    if (/(y+)/.test(fmt)) {
      fmt = fmt.replace(RegExp.$1, (this.getFullYear() + '').substr(4 - RegExp.$1.length))
    }
    for (var k in o) {
      if (new RegExp('(' + k + ')').test(fmt)) {
        fmt = fmt.replace(RegExp.$1, (RegExp.$1.length === 1) ? (o[k]) : (('00' + o[k]).substr(('' + o[k]).length)))
      }
    }
    return fmt
}
  
import Config from 'settings'
import JSEncrypt from 'jsencrypt/bin/jsencrypt'
export function getDeviceUUID() {
	let deviceId = uni.getStorageSync('uni_deviceId') ||
		uni.getSystemInfoSync().deviceId ||
		uni.getSystemInfoSync().system + '_' + Math.random().toString(36).substr(2);

	uni.setStorageSync('uni_deviceId', deviceId)
	return deviceId;
}


export function encrypt(txt) {
    const publicKey = getLocalPK()
    const encryptor = new JSEncrypt()
    encryptor.setPublicKey(publicKey) // 设置公钥
    return encryptor.encrypt(txt) // 对需要加密的数据进行加密
}

export function getToken() {
    return uni.getStorageSync(Config.TokenKey);
}

export function setToken(token) {
    uni.setStorageSync(Config.TokenKey, token);
}


export function setLocalPK(data) {
    uni.setStorageSync(Config.PUBLICKEY, data)
}

export function getLocalPK() {
    return uni.getStorageSync(Config.PUBLICKEY)
}

export function getUserInfo() {
    return uni.getStorageSync(Config.USER_INFO_KEY)
}

export function setUserInfo(data) {
    return uni.setStorageSync(Config.USER_INFO_KEY, data)
}

export function addRecent(data) {
    const userInfo = getUserInfo()
    const key = Config.RECENT_KEY
    const list = getRecent(key + userInfo.userId)
    
    const len = list.length
    let index = len

    while(index-- > 0) {
        if (list[index].id === data.id) {
            list.splice(index, 1)
        }
    }
    data.time = Date.now()
    list.splice(0, 0, data)
	list.splice(10, 100)
    uni.setStorageSync(key + userInfo.userId, list);
}


export function getRecent() {
    const userInfo = getUserInfo()
    const key = Config.RECENT_KEY
    const list = uni.getStorageSync(key + userInfo.userId)
    return list || []
}


function isToday(time) {
    return (new Date()).toDateString() === (new Date(time)).toDateString()
}

function isYestday(time) {
    const date = new Date(time)
    const yestday = new Date(Date.now() - 24 * 60 * 60 * 1000)
    return date.getYear() === yestday.getYear() && date.getMonth() === yestday.getMonth() && date.getDate() === yestday.getDate()
}

function isCurrentYear(time) {
    const date = new Date(time)
    const cur = new Date()
    return date.getYear() === cur.getYear()
}

export function formatHistoryDate(time) {
    if(isToday(time)) {
        return (new Date(time)).format('hh:mm')
    }
    if(isYestday(time)) {
        return '昨天'
    }
    if (isCurrentYear(time)) {
        return (new Date(time)).format('MM月dd日')
    }
    return (new Date(time)).format('yyyy年MM月dd日')
}

export function setLanguage(val) {
    const userInfo = getUserInfo()
    const key = 'my-language-'
    uni.setStorageSync(key + userInfo.userId, val)
}

export function getLanguage() {
    const userInfo = getUserInfo()
    if(!userInfo || !userInfo.userId) return 'sys'
    const key = 'my-language-'
    const result = uni.getStorageSync(key + userInfo.userId)
    if(!result) {
        return 'sys'
    }
    return result
}

export function parseLanguage() {
    const language = getLanguage()
    if(language === 'sys') return uni.getLocale()
    return language
}

export function getUrlParams(url){
  const Params = {}
  if(url.indexOf('?')>0){//判断是否有qurey
    let parmas = url.slice(url.indexOf('?')+1)//截取出query
    const paramlists = parmas.split('&')//分割键值对
    for (const param of paramlists) {
      let a = param.split('=')
      Object.assign(Params,{[a[0]]:a[1]})//将键值对封装成对象
    }
  }
  return Params
}
