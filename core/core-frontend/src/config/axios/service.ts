import axios, {
  AxiosInstance,
  AxiosRequestHeaders,
  InternalAxiosRequestConfig,
  AxiosResponse,
  AxiosError
} from 'axios'

import qs from 'qs'

import { config } from './config'

import { ElMessage } from 'element-plus-secondary'

const { result_code } = config
import { useCache } from '@/hooks/web/useCache'
const { wsCache } = useCache()

export const PATH_URL = window.DataEaseBi
  ? window.DataEaseBi?.baseUrl + 'de2api/'
  : import.meta.env.VITE_API_BASEPATH

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: PATH_URL, // api 的 base_url
  timeout: config.request_timeout // 请求超时时间
})
const mapping = {
  'zh-CN': 'zh-CN',
  en: 'en_US',
  tw: 'zh_TW'
}
// request拦截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    if (
      config.method === 'post' &&
      (config.headers as AxiosRequestHeaders)['Content-Type'] ===
        'application/x-www-form-urlencoded'
    ) {
      config.data = qs.stringify(config.data)
    }
    if (window.DataEaseBi?.baseUrl) {
      config.baseURL = window.DataEaseBi.baseUrl + 'de2api/'
    }
    if (wsCache.get('user.token')) {
      ;(config.headers as AxiosRequestHeaders)['X-DE-TOKEN'] = wsCache.get('user.token')
    } else if (window.DataEaseBi?.token) {
      ;(config.headers as AxiosRequestHeaders)['X-EMBEDDED-TOKEN'] = window.DataEaseBi.token
    }
    if (wsCache.get('user.language')) {
      const key = wsCache.get('user.language')
      const val = mapping[key] || key
      ;(config.headers as AxiosRequestHeaders)['Accept-Language'] = val
    }
    ;(config.headers as AxiosRequestHeaders)['out_auth_platform'] = wsCache.get('out_auth_platform')
      ? wsCache.get('out_auth_platform')
      : 'default'

    // ;(config.headers as AxiosRequestHeaders)['Token'] = 'test test'
    // get参数编码
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
    return config
  },
  (error: AxiosError) => {
    Promise.reject(error)
  }
)

// response 拦截器
service.interceptors.response.use(
  (response: AxiosResponse<any>) => {
    if (response.config.responseType === 'blob') {
      // 如果是文件流，直接过
      return response
    } else if (response.data.code === result_code) {
      if (response.headers['x-de-refresh-token']) {
        wsCache.set('user.token', response.headers['x-de-refresh-token'])
      }
      return response.data
    } else if (response.config.url.match(/^\/map\/\d{3}\/\d+\.json$/)) {
      //   TODO 处理静态文件
      return response
    } else {
      ElMessage.error(response.data.msg)
    }
  },
  (error: AxiosError) => {
    ElMessage.error(error.message)
    return Promise.reject(error)
  }
)

export { service }
