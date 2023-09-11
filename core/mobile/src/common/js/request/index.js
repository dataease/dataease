import axios from 'axios'
import Config from '@/settings'
import { getToken, setToken, setUserInfo, parseLanguage } from '@/common/utils'
const TokenKey = Config.TokenKey
const RefreshTokenKey = Config.RefreshTokenKey
const white_list = Config.WHITE_LIST


let service = axios.create({
  // baseURL: 'http://localhost:8081',
  baseURL: process.env.VUE_APP_BASE_API,
  timeout: 5000
})

// request interceptor
service.interceptors.request.use(
  config => {

    let lang = parseLanguage() || uni.getLocale()
    if (lang === 'en') {
        config.headers['Accept-Language'] = 'en-US'
    }else if(lang === 'zh-Hant'){
        config.headers['Accept-Language'] = 'zh-TW'
    }else {
        config.headers['Accept-Language'] = 'zh-CN'
    }


    if (white_list.includes(config.url)) {
        return config
    }
    let token = getToken()
    if (token) {
      config.headers[TokenKey] = token
    }
    else {
        logout()
    }

    return config
  },
  error => {

    return Promise.reject(error)
  }
)



// 请根据实际需求修改
service.interceptors.response.use(response => {
  checkAuth(response)
  return response.data
}, error => {
checkAuth(error.response)
  let msg
  if (error.response) {
    checkAuth(error.response)
    msg = error.response.data.message || error.response.data
  } else {
    msg = error.message
  }
  if (msg?.startsWith('MultiLoginError')) {
    return Promise.reject(error)
  } 
  uni.showToast({
    icon: 'error',
    title: msg
  })
  return Promise.reject(error)
})
const logout = () => {
    setToken(null)
    setUserInfo(null)
    uni.reLaunch({
        url: '/'
    });
}
const checkAuth = response => {
    if (response.headers['authentication-status'] === 'login_expire') {
        uni.showToast({
            icon: 'none',
            title: 'token超时'
        });
        logout()
    }

    if (response.headers['authentication-status'] === 'invalid') {
        uni.showToast({
            icon: 'none',
            title: 'token错误'
        });
        logout()
    }
    // token到期后自动续命 刷新token
    if (response.headers[RefreshTokenKey]) {
      const refreshToken = response.headers[RefreshTokenKey]
      setToken(refreshToken)
    }


  }


export default service
