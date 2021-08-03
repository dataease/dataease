import axios from 'axios'
// import { MessageBox, Message } from 'element-ui'
import store from '@/store'
import { $alert, $error } from './message'
import { getToken } from '@/utils/auth'
import Config from '@/settings'
import i18n from '@/lang'
import { tryShowLoading, tryHideLoading } from './loading'
import { getLinkToken, setLinkToken } from '@/utils/auth'
// import router from '@/router'
const interruptTokenContineUrls = Config.interruptTokenContineUrls
const TokenKey = Config.TokenKey
const RefreshTokenKey = Config.RefreshTokenKey
const LinkTokenKey = Config.LinkTokenKey
// create an axios instance
const service = axios.create({
  baseURL: process.env.VUE_APP_BASE_API, // url = base url + request url
  // withCredentials: true, // send cookies when cross-domain requests
  timeout: 20000 // request timeout
})

// request interceptor
service.interceptors.request.use(
  config => {
    // do something before request is sent

    if (store.getters.token) {
      // let each request carry token
      // ['X-Token'] is a custom headers key
      // please modify it according to the actual situation
      config.headers[TokenKey] = getToken()
    }
    let linkToken = null
    if ((linkToken = getLinkToken()) !== null) {
      config.headers[LinkTokenKey] = linkToken
    }

    if (i18n.locale) {
      const lang = i18n.locale.replace('_', '-')
      config.headers['Accept-Language'] = lang
    }
    // 增加loading

    config.loading && tryShowLoading(store.getters.currentPath)

    return config
  },
  error => {
    error.config.loading && tryHideLoading(store.getters.currentPath)
    // do something with request error
    return Promise.reject(error)
  }
)

const checkAuth = response => {
  // 请根据实际需求修改

  if (response.headers['authentication-status'] === 'login_expire') {
    const message = i18n.t('login.expires')
    // store.dispatch('user/setLoginMsg', message)
    $alert(message, () => {
      store.dispatch('user/logout').then(() => {
        location.reload()
      })
    }, {
      confirmButtonText: i18n.t('login.login_again'),
      showClose: false
    })
  }

  if (response.headers['authentication-status'] === 'invalid') {
    const message = i18n.t('login.tokenError')
    $alert(message, () => {
      store.dispatch('user/logout').then(() => {
        location.reload()
      })
    }, {
      confirmButtonText: i18n.t('login.login_again'),
      showClose: false
    })
  }
  // token到期后自动续命 刷新token
  if (response.headers[RefreshTokenKey] && !interruptTokenContineUrls.some(item => response.config.url.indexOf(item) >= 0)) {
    const refreshToken = response.headers[RefreshTokenKey]
    store.dispatch('user/refreshToken', refreshToken)
  }

  if (response.headers[LinkTokenKey.toLocaleLowerCase()] || (response.config.headers && response.config.headers[LinkTokenKey.toLocaleLowerCase()])) {
    const linkToken = response.headers[LinkTokenKey.toLocaleLowerCase()] || response.config.headers[LinkTokenKey.toLocaleLowerCase()]
    setLinkToken(linkToken)
  }
  // 许可状态改变 刷新页面
//   if (response.headers['lic-status']) {
//     location.reload()
//   }
}

// 请根据实际需求修改
service.interceptors.response.use(response => {
  response.config.loading && tryHideLoading(store.getters.currentPath)
  checkAuth(response)
  return response.data
}, error => {
  const config = error.response && error.response.config || error.config
  const headers = error.response && error.response.headers || error.response || config.headers
  config.loading && tryHideLoading(store.getters.currentPath)

  let msg
  if (error.response) {
    checkAuth(error.response)
    // checkPermission(error.response)
    msg = error.response.data.message || error.response.data
  } else {
    msg = error.message
  }
  !config.hideMsg && (!headers['authentication-status']) && $error(msg)
  return Promise.reject(error)
})
export default service
