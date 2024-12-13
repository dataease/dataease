import axios, {
  AxiosInstance,
  AxiosRequestHeaders,
  InternalAxiosRequestConfig,
  AxiosResponse,
  AxiosError,
  AxiosRequestConfig,
  AxiosHeaders
} from 'axios'
import { tryShowLoading, tryHideLoading } from '@/utils/loading'
import qs from 'qs'
import { usePermissionStoreWithOut } from '@/store/modules/permission'
import { useEmbedded } from '@/store/modules/embedded'
import { useLinkStoreWithOut } from '@/store/modules/link'
import { config } from './config'
import { configHandler } from './refresh'
import { isMobile, getLocale } from '@/utils/utils'
import { useRequestStoreWithOut } from '@/store/modules/request'
type AxiosErrorWidthLoading<T> = T & {
  config: {
    loading?: boolean
  }
}

type InternalAxiosRequestConfigWidthLoading<T> = T & {
  loading?: boolean
}

import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import router from '@/router'

const { result_code } = config
import { useCache } from '@/hooks/web/useCache'
const { wsCache } = useCache()
const requestStore = useRequestStoreWithOut()
const embeddedStore = useEmbedded()
const basePath = import.meta.env.VITE_API_BASEPATH

const embeddedBasePath =
  basePath.startsWith('./') && basePath.length > 2 ? basePath.substring(2) : basePath
export const PATH_URL = embeddedStore.baseUrl ? embeddedStore?.baseUrl + embeddedBasePath : basePath

export interface AxiosInstanceWithLoading extends AxiosInstance {
  <T = any, R = AxiosResponse<T>, D = any>(
    config: AxiosRequestConfig<D> & { loading?: boolean }
  ): Promise<R>
}

const getTimeOut = () => {
  let time = 100
  const url = PATH_URL + '/sysParameter/requestTimeOut'
  const xhr = new XMLHttpRequest()
  xhr.onreadystatechange = () => {
    if (xhr.readyState === 4 && xhr.status === 200) {
      if (xhr.responseText) {
        try {
          const response = JSON.parse(xhr.responseText)
          if (response.code === 0) {
            time = response.data
          } else {
            ElMessage.error('系统异常，请联系管理员')
          }
        } catch (e) {
          ElMessage.error('系统异常，请联系管理员')
        }
      } else {
        ElMessage.error('网络异常，请联系网管')
      }
    }
  }

  xhr.open('get', url, false)
  xhr.send()
  return time
}

// 创建axios实例
const time = getTimeOut()
window._de_get_time_out = time
const service: AxiosInstanceWithLoading = axios.create({
  baseURL: PATH_URL, // api 的 base_url
  timeout: time ? time * 1000 : config.request_timeout // 请求超时时间
})
const mapping = {
  'zh-CN': 'zh-CN',
  en: 'en-US',
  tw: 'zh-TW'
}
const permissionStore = usePermissionStoreWithOut()
const linkStore = useLinkStoreWithOut()
const CancelToken = axios.CancelToken
const cancelMap = {}

// request拦截器
service.interceptors.request.use(
  async (c: InternalAxiosRequestConfigWidthLoading<InternalAxiosRequestConfig>) => {
    let config = configHandler(c)
    if (config instanceof Promise) {
      config = await config
    }
    if (
      config.method === 'post' &&
      (config.headers as AxiosRequestHeaders)['Content-Type'] ===
        'application/x-www-form-urlencoded'
    ) {
      config.data = qs.stringify(config.data)
    }
    if (embeddedStore.baseUrl) {
      config.baseURL = PATH_URL
    }

    if (isMobile()) {
      ;(config.headers as AxiosRequestHeaders)['X-DE-MOBILE'] = true
    }
    if (linkStore.getLinkToken) {
      ;(config.headers as AxiosRequestHeaders)['X-DE-LINK-TOKEN'] = linkStore.getLinkToken
    } else if (embeddedStore.token) {
      ;(config.headers as AxiosRequestHeaders)['X-EMBEDDED-TOKEN'] = embeddedStore.token
    }
    const locale = getLocale()
    if (locale) {
      const val = mapping[locale] || locale
      ;(config.headers as AxiosRequestHeaders)['Accept-Language'] = val
    }

    if (config.method === 'get' && config.params) {
      let url = config.url as string
      url += '?'
      const keys = Object.keys(config.params)
      for (const key of keys) {
        if (config.params[key] !== void 0 && config.params[key] !== null) {
          url += `${key}=${encodeURIComponent(config.params[key])}&`
        }
      }
      url = url.substring(0, url.length - 1)
      config.params = {}
      config.url = url
    }
    config.cancelToken = new CancelToken(function executor(c) {
      cancelMap[config.url] = c
    })
    config.loading && tryShowLoading(permissionStore.getCurrentPath)
    return config
  },
  (error: AxiosErrorWidthLoading<AxiosError>) => {
    error.config.loading && tryHideLoading(permissionStore.getCurrentPath)
    Promise.reject(error)
  }
)

// response 拦截器
service.interceptors.response.use(
  (
    response: AxiosResponse<any> & { config: InternalAxiosRequestConfig & { loading?: boolean } }
  ) => {
    executeVersionHandler(response)
    /* if (response.headers['x-de-refresh-token']) {
      wsCache.set('user.token', response.headers['x-de-refresh-token'])
      wsCache.set('user.exp', new Date().getTime() + 90000)
    } */
    if (response.headers['x-de-link-token']) {
      linkStore.setLinkToken(response.headers['x-de-link-token'])
    }
    response.config.loading && tryHideLoading(permissionStore.getCurrentPath)

    if (response.config.responseType === 'blob') {
      // 如果是文件流，直接过
      return response
    } else if (response.data.code === result_code || response.data.code === 50002) {
      return response.data
    } else if (response.config.url.match(/^\/map|geo\/\d{3}\/\d+\.json$/)) {
      //   TODO 处理静态文件
      return response
    } else if (response.config.url.includes('DEXPack.umd.js')) {
      return response
    } else if (response.config.url.startsWith('/xpackComponent/pluginStaticInfo/extensions-')) {
      return response
    } else {
      if (
        !response?.config?.url.startsWith('/xpackComponent/content') &&
        response?.data?.code !== 60003
      ) {
        ElMessage({
          type: 'error',
          message: response.data.msg,
          showClose: true
        })
        if (response.data.code === 80001) {
          localStorage.clear()
          let queryRedirectPath = '/workbranch/index'
          if (router.currentRoute.value.fullPath) {
            queryRedirectPath = router.currentRoute.value.fullPath as string
          }
          router.push(`/login?redirect=${queryRedirectPath}`)
        }
      } else if (response?.config?.url.startsWith('/xpackComponent/content')) {
        console.error(
          "never mind this error about '/xpackComponent/content', just a reminder to support the official license"
        )
      }

      return Promise.reject(response.data.msg)
    }
  },
  (error: AxiosErrorWidthLoading<AxiosError>) => {
    if (error.message?.includes('timeout of')) {
      requestStore.resetLoadingMap()
      ElMessage({
        type: 'error',
        message: '请求超时，请稍后再试',
        showClose: true
      })
    }

    if (!error?.response) {
      return Promise.reject(error)
    }
    const header = error.response?.headers as AxiosHeaders
    if (
      !error.config.url.startsWith('/xpackComponent/content') &&
      !header.has('DE-FORBIDDEN-FLAG') &&
      !header.has('DE-GATEWAY-FLAG')
    ) {
      ElMessage({
        type: 'error',
        message: error.response?.data?.msg ? error.response?.data?.msg : error.message,
        showClose: true
      })
    } else if (error?.config?.url.startsWith('/xpackComponent/content')) {
      console.error(
        "never mind this error about '/xpackComponent/content', just a reminder to support the official license"
      )
    }

    error.config.loading && tryHideLoading(permissionStore.getCurrentPath)
    if (header.has('DE-GATEWAY-FLAG')) {
      localStorage.clear()
      const flag = header.get('DE-GATEWAY-FLAG')
      localStorage.setItem('DE-GATEWAY-FLAG', flag.toString())
      let queryRedirectPath = '/workbranch/index'
      if (router.currentRoute.value.fullPath) {
        queryRedirectPath = router.currentRoute.value.fullPath as string
      }
      router.push(`/login?redirect=${queryRedirectPath}`)
    }
    if (header.has('DE-FORBIDDEN-FLAG')) {
      showMsg('当前用户权限配置已变更，请刷新页面', '-changed-')
    }
    return Promise.resolve()
  }
)

const showMsg = (msg: string, id: string) => {
  if (window['cross-permission-' + id]) {
    return
  }
  window['cross-permission-' + id] = ElMessageBox.confirm(msg, {
    confirmButtonType: 'primary',
    type: 'warning',
    confirmButtonText: '刷新',
    cancelButtonText: '取消',
    autofocus: false,
    showClose: false
  })
    .then(() => {
      window['cross-permission-' + id]
      window.location.reload()
    })
    .catch(() => {
      window['cross-permission-' + id] = null
    })
}

const executeVersionHandler = (response: AxiosResponse) => {
  const key = 'x-de-execute-version'
  const executeVersion = response.headers[key]
  const cacheVal = wsCache.get(key)
  if (!cacheVal) {
    wsCache.set(key, executeVersion)
    return
  }
  if (executeVersion && executeVersion !== cacheVal) {
    wsCache.clear()
    wsCache.set(key, executeVersion)
    showMsg('系统有升级，请点击刷新页面', '-sys-upgrade-')
  }
}
export { service, cancelMap }
