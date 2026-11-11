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
import { isMobile, getLocale, isLink } from '@/utils/utils'
import { useI18n } from '@/hooks/web/useI18n'
// 注意：不得在模块顶层 const { t } = useI18n() —— 本模块求值早于 setupI18n，
// 顶层捕获会永久得到降级透传 t（永远返回 key）。必须在函数体内实时调用 useI18n()。
import { useRequestStoreWithOut } from '@/store/modules/request'
import { clearCache } from '@/utils/cacheUtil'
import { securityConfig } from './hmac'
import { getHmacFailure } from './hmacError'
import { getHmacMessage } from './hmacMessage'
import { DataEaseError, errorBody, toDataEaseError, markErrorContractResponse } from './error'

type AxiosErrorWidthLoading<T> = T & {
  config: {
    loading?: boolean
    handleErrorLocally?: boolean
    queryRequest?: QueryRequest
    releaseCancellation?: () => void
    loadingPath?: string
  }
}

type InternalAxiosRequestConfigWidthLoading<T> = T & {
  loading?: boolean
  queryRequest?: QueryRequest
  releaseCancellation?: () => void
  loadingPath?: string
}

import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import router from '@/router'
import { type QueryRequest } from './queryScope'
import {
  cancelMap,
  cancelRequestBatch,
  cancelAllRequest,
  registerLegacyCancellation
} from './legacyCancellation'

const { result_code } = config
import { useCache } from '@/hooks/web/useCache'
const { wsCache } = useCache()
// Discard the old /dekey response, which contained the deployment storage AES key.
const cachedDekey = wsCache.get('DataEaseKey')
if (cachedDekey && (typeof cachedDekey !== 'string' || !/^MI[A-Za-z0-9+/=]+$/.test(cachedDekey))) {
  wsCache.delete('DataEaseKey')
}
const requestStore = useRequestStoreWithOut()
const embeddedStore = useEmbedded()
const basePath = import.meta.env.VITE_API_BASEPATH

const embeddedBasePath =
  basePath.startsWith('./') && basePath.length > 2 ? basePath.substring(2) : basePath
export const PATH_URL = embeddedStore.baseUrl ? embeddedStore?.baseUrl + embeddedBasePath : basePath

export interface AxiosInstanceWithLoading extends AxiosInstance {
  <T = any, R = AxiosResponse<T>, D = any>(
    config: AxiosRequestConfig<D> & {
      loading?: boolean
      handleErrorLocally?: boolean
      queryRequest?: QueryRequest
    }
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
            // 模块求值期早于 setupI18n，i18n 未就绪，此处暂不国际化（保留中文）
            ElMessage.error('系统异常，请联系管理员')
          }
        } catch (e) {
          // 模块求值期早于 setupI18n，i18n 未就绪，此处暂不国际化（保留中文）
          ElMessage.error('系统异常，请联系管理员')
        }
      } else {
        // 模块求值期早于 setupI18n，i18n 未就绪，此处暂不国际化（保留中文）
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
let reloadingLink = false

const revalidateLink = (requestConfig: InternalAxiosRequestConfig) => {
  if (!isLink() || !requestConfig.headers?.['X-DE-LINK-TOKEN']) {
    return false
  }
  // 分享鉴权失效后重新走 proxyInfo，由分享页显示过期、删除或密码变更等状态。
  // 保留当前 URL 和用户登录缓存，并合并并发图表请求触发的刷新。
  if (!reloadingLink) {
    reloadingLink = true
    window.location.reload()
  }
  return true
}

// request拦截器
service.interceptors.request.use(
  async (c: InternalAxiosRequestConfigWidthLoading<InternalAxiosRequestConfig>) => {
    let config = configHandler(c)
    if (config instanceof Promise) {
      config = await config
    }
    if (embeddedStore.baseUrl) {
      config.baseURL = PATH_URL
    }
    // A coordinated capability declaration; old external consumers send no header.
    config.headers.set('X-DE-Error-Contract', '2')
    config.headers.set(
      'X-DE-Request-ID',
      Array.from(crypto.getRandomValues(new Uint8Array(16)), value =>
        value.toString(16).padStart(2, '0')
      ).join('')
    )
    await securityConfig(config, service.getUri(config))
    if (
      config.method === 'post' &&
      (config.headers as AxiosRequestHeaders)['Content-Type'] ===
        'application/x-www-form-urlencoded'
    ) {
      config.data = qs.stringify(config.data)
    }

    if (isMobile()) {
      ;(config.headers as AxiosRequestHeaders)['X-DE-MOBILE'] = true
    }
    if (linkStore.getLinkToken) {
      ;(config.headers as AxiosRequestHeaders)['X-DE-LINK-TOKEN'] = linkStore.getLinkToken
    } else if (embeddedStore.token) {
      ;(config.headers as AxiosRequestHeaders)['X-EMBEDDED-TOKEN'] = embeddedStore.token
    }
    const storedProxyInfo = wsCache.get('user.proxyInfo')
    if (storedProxyInfo?.proxy && storedProxyInfo?.proxySecret) {
      ;(config.headers as AxiosRequestHeaders)['X-DE-ADMIN-PROXY'] = storedProxyInfo.proxySecret
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

    if (config.queryRequest) {
      if (!config.queryRequest.isCurrent()) {
        throw new axios.CanceledError('Obsolete query', config)
      }
      config.signal = config.queryRequest.signal
    } else {
      config.releaseCancellation = registerLegacyCancellation(config)
    }

    config.loadingPath = permissionStore.getCurrentPath
    config.loading && tryShowLoading(config.loadingPath)
    return config
  },
  (error: AxiosErrorWidthLoading<AxiosError<unknown>>) => {
    return Promise.reject(error)
  }
)

// response 拦截器
service.interceptors.response.use(
  (
    response: AxiosResponse<any> & {
      config: InternalAxiosRequestConfigWidthLoading<InternalAxiosRequestConfig>
    }
  ) => {
    response.config.releaseCancellation?.()
    response.config.queryRequest?.finish()
    response.config.loading && tryHideLoading(response.config.loadingPath)
    if (response.config.queryRequest && !response.config.queryRequest.isCurrent()) {
      return Promise.reject(new axios.CanceledError('Obsolete query', response.config))
    }
    if (response.headers['x-de-error-contract'] === '2') markErrorContractResponse(response.data)
    executeVersionHandler(response)
    if (response.headers['x-de-link-token']) {
      linkStore.setLinkToken(response.headers['x-de-link-token'])
    }

    if (response.config.responseType === 'blob') {
      // Successful binary responses keep their original shape. JSON errors are handled below.
      if (!(response.data instanceof Blob) || !response.data.type.includes('json')) return response
      return response.data.text().then(text => {
        let body: unknown
        try {
          body = JSON.parse(text)
        } catch {
          return response
        }
        if (!errorBody(body).code) return response
        return rejectBusinessResponse({ ...response, data: body })
      })
    } else if (response.data.code === result_code || response.data.code === 50002) {
      return response.data
    } else if (response.config.url.match(/^\/map|geo\/\d{3}\/\d+\.json$/)) {
      //   TODO 处理静态文件
      return response
    } else if (response.config.url.includes('/i18n/custom_')) {
      return response
    } else if (response.config.url.startsWith('/xpackComponent/pluginStaticInfo/extensions-')) {
      return response
    } else {
      return rejectBusinessResponse(response)
    }
  },
  async (error: AxiosErrorWidthLoading<AxiosError<unknown>>) => {
    return rejectHttpResponse(error)
  }
)

const rejectBusinessResponse = (
  response: AxiosResponse & {
    config: InternalAxiosRequestConfig & { handleErrorLocally?: boolean }
  }
) => {
  const failure = new DataEaseError(response)
  const legacySessionFailure = response.data.code === 80001 && !response.data.category
  if (legacySessionFailure && revalidateLink(response.config)) {
    return Promise.reject(failure)
  }
  if (
    !response?.config?.url.startsWith('/xpackComponent/contentPlugin/') &&
    response?.data?.code !== 60003
  ) {
    let errMsg = response.data.msg
    if (errMsg?.includes('rsa info has been changed')) {
      wsCache.delete('DataEaseKey')
      errMsg = useI18n().t('common.secret_changed_tips')
    }
    if (!response.config.handleErrorLocally) {
      ElMessage({ type: 'error', message: errMsg, showClose: true })
      failure.presented = true
    }
    if (legacySessionFailure) {
      clearCache()
      let queryRedirectPath = '/workbranch/index'
      if (router.currentRoute.value.fullPath) {
        queryRedirectPath = router.currentRoute.value.fullPath as string
      }
      router.push(`/login?redirect=${queryRedirectPath}`)
    }
  } else if (response?.config?.url.startsWith('/xpackComponent/contentPlugin/')) {
    console.error(
      "never mind this error about '/xpackComponent/contentPlugin/', just a reminder to support the official license"
    )
  }

  return Promise.reject(failure)
}

const rejectHttpResponse = async (error: AxiosErrorWidthLoading<AxiosError<unknown>>) => {
  error.config?.releaseCancellation?.()
  error.config?.queryRequest?.finish()
  error.config?.loading && tryHideLoading(error.config.loadingPath)
  if (
    axios.isCancel(error) ||
    (error.config?.queryRequest && !error.config.queryRequest.isCurrent())
  ) {
    return Promise.reject(error)
  }
  // Image failures belong to the image loader, as with native <img> requests. Preserve
  // the rejection so denied/missing images cannot become successful exports or trigger a login loop.
  if (error.response?.data instanceof Blob && error.response.data.type.includes('json')) {
    try {
      error.response.data = JSON.parse(await error.response.data.text())
    } catch {
      /* Non-JSON proxy failures retain their original response. */
    }
  }
  const normalized = toDataEaseError(error)
  if (error.config?.handleErrorLocally) return Promise.reject(normalized)
  if (error.message?.includes('timeout of')) {
    normalized.presented = true
    requestStore.resetLoadingMap()
    ElMessage({
      type: 'error',
      message: useI18n().t('common.timeout_tips'),
      showClose: true
    })
  }

  if (!error?.response) {
    return Promise.reject(normalized)
  }

  const hmacFailure = getHmacFailure(error)
  if (hmacFailure) {
    ElMessage({
      type: 'error',
      message: await getHmacMessage(hmacFailure),
      showClose: true,
      grouping: true,
      duration: 0
    })
    // A rejected signature must not become an undefined successful response or a login redirect.
    normalized.presented = true
    return Promise.reject(normalized)
  }

  if (error?.response.status === 413) {
    ElMessage({
      type: 'error',
      message: useI18n().t('common.file_size_exceed_tips'),
      showClose: true
    })
    normalized.presented = true
    return Promise.reject(normalized)
  }
  const header = AxiosHeaders.from(error.response?.headers as AxiosHeaders)
  const reason = header.get('X-DE-Error-Reason') || normalized.category
  if (
    normalized.messageKey === 'i18n_error_key_changed' ||
    reason === 'KEY_CHANGED' ||
    normalized.message.includes('rsa info has been changed')
  ) {
    wsCache.delete('DataEaseKey')
    normalized.message = useI18n().t('common.secret_changed_tips')
  }
  if (normalized.code === 50005 && normalized.httpStatus === 409) return Promise.reject(normalized)
  const gatewayFailure = header.has('DE-GATEWAY-FLAG') || reason === 'UNAUTHENTICATED'
  const forbiddenFailure =
    header.has('DE-FORBIDDEN-FLAG') || header.get('X-DE-Error-Reason') === 'FORBIDDEN'
  if (
    !error.config.url.startsWith('/xpackComponent/contentPlugin/') &&
    normalized.code !== 60003 &&
    !forbiddenFailure &&
    !gatewayFailure
  ) {
    ElMessage({
      type: 'error',
      message: normalized.message,
      showClose: true
    })
  } else if (error?.config?.url.startsWith('/xpackComponent/contentPlugin/')) {
    console.error(
      "never mind this error about '/xpackComponent/contentPlugin/', just a reminder to support the official license"
    )
  }

  if (gatewayFailure) {
    if (revalidateLink(error.config)) {
      return Promise.reject(normalized)
    }
    const userToken = wsCache.get('user.token')
    const inPlatformClient = !!wsCache.get('de-platform-client')
    clearCache()
    if (!(userToken && inPlatformClient)) {
      const flag = header.get('DE-GATEWAY-FLAG') || 'Invalid%20token'
      localStorage.setItem('DE-GATEWAY-FLAG', flag.toString())
    }
    let queryRedirectPath = '/workbranch/index'
    if (router.currentRoute.value.fullPath) {
      queryRedirectPath = router.currentRoute.value.fullPath as string
    }
    router.push(`/login?redirect=${queryRedirectPath}`)
  }
  if (forbiddenFailure) {
    if (header.get('DE-FORBIDDEN-FLAG') === 'Resource not exist' || reason === 'NOT_FOUND') {
      // 资源不存在（已删除）：直接错误提示，不弹权限框
      ElMessage({
        type: 'error',
        message: useI18n().t('common.resource_not_exist_tips'),
        showClose: true
      })
    } else {
      showMsg(useI18n().t('common.permission_denied_tips'), '-changed-')
    }
  }
  normalized.presented = true
  return Promise.reject(normalized)
}

const showMsg = (msg: string, id: string) => {
  if (window['cross-permission-' + id]) {
    return
  }
  window['cross-permission-' + id] = ElMessageBox.confirm(msg, {
    confirmButtonType: 'primary',
    type: 'warning',
    confirmButtonText: useI18n().t('common.refresh'),
    cancelButtonText: useI18n().t('common.cancel'),
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
    wsCache.set(key, executeVersion)
    showMsg(useI18n().t('common.system_upgrade_tips'), '-sys-upgrade-')
  }
}

export { service, cancelMap, cancelRequestBatch, cancelAllRequest }
