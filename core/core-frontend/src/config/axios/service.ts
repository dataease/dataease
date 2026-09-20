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
import { useI18n, isI18nReady } from '@/hooks/web/useI18n'
// 注意：不得在模块顶层 const { t } = useI18n() —— 本模块求值早于 setupI18n，
// 顶层捕获会永久得到降级透传 t（永远返回 key）。必须在函数体内实时调用 useI18n()。
import { useRequestStoreWithOut } from '@/store/modules/request'
import { clearCache } from '@/utils/cacheUtil'
import { securityConfig } from './hmac'

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
const CancelToken = axios.CancelToken
const cancelMap = {}

// request拦截器
service.interceptors.request.use(
  async (c: InternalAxiosRequestConfigWidthLoading<InternalAxiosRequestConfig>) => {
    let config = configHandler(c)
    if (config instanceof Promise) {
      config = await config
    }
    await securityConfig(config, service.getUri(config))
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

    if (config.url.endsWith('chartData/getData')) {
      const chartKey = `chartData/getData/${(config.data as any).id}`
      config.cancelToken = new CancelToken(function executor(c) {
        cancelMap[chartKey] = c
      })
    } else {
      config.cancelToken = new CancelToken(function executor(c) {
        cancelMap[config.url] = c
      })
    }

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
    } else if (
      response.config.url.includes('DEXPack.umd.js') ||
      response.config.url.includes('/i18n/custom_')
    ) {
      return response
    } else if (response.config.url.startsWith('/xpackComponent/pluginStaticInfo/extensions-')) {
      return response
    } else {
      if (
        !response?.config?.url.startsWith('/xpackComponent/content') &&
        response?.data?.code !== 60003
      ) {
        let errMsg = response.data.msg
        if (errMsg?.includes('rsa info has been changed')) {
          wsCache.delete('DataEaseKey')
          errMsg = useI18n().t('common.secret_changed_tips')
        }
        ElMessage({
          type: 'error',
          message: errMsg,
          showClose: true
        })
        if (response.data.code === 80001) {
          clearCache()
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
        message: useI18n().t('common.timeout_tips'),
        showClose: true
      })
    }

    if (!error?.response) {
      return Promise.reject(error)
    }

    if (error?.response.status === 413) {
      ElMessage({
        type: 'error',
        message: useI18n().t('common.file_size_exceed_tips'),
        showClose: true
      })
      return
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
      const userToken = wsCache.get('user.token')
      const inPlatformClient = !!wsCache.get('de-platform-client')
      clearCache()
      if (!(userToken && inPlatformClient)) {
        const flag = header.get('DE-GATEWAY-FLAG')
        localStorage.setItem('DE-GATEWAY-FLAG', flag.toString())
      }
      let queryRedirectPath = '/workbranch/index'
      if (router.currentRoute.value.fullPath) {
        queryRedirectPath = router.currentRoute.value.fullPath as string
      }
      router.push(`/login?redirect=${queryRedirectPath}`)
    }
    if (header.has('DE-FORBIDDEN-FLAG')) {
      if (header.get('DE-FORBIDDEN-FLAG') === 'Resource not exist') {
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
    if (error?.response.status === 400) {
      return Promise.reject(error)
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

// 等待 i18n 就绪后再执行的任务队列：按 key 去重（仅保留最新任务），配合轮询在就绪时补发。
const deferredTasks = new Map<string, () => void>()
let deferPollTimer: ReturnType<typeof setInterval> | null = null

const flushDeferredTasks = () => {
  if (deferPollTimer !== null) {
    clearInterval(deferPollTimer)
    deferPollTimer = null
  }
  const tasks = [...deferredTasks.values()]
  deferredTasks.clear()
  tasks.forEach(task => {
    try {
      task()
    } catch (e) {
      console.error(e)
    }
  })
}

const queueUntilI18nReady = (key: string, task: () => void) => {
  // 已就绪直接执行，避免不必要的排队
  if (isI18nReady()) {
    task()
    return
  }
  deferredTasks.set(key, task)
  if (deferPollTimer !== null) return
  let ticks = 0
  // 每 100ms 轮询一次；正常在 setupI18n 完成后毫秒级即触发。
  // 设置上限（约 30s）避免 i18n 始终未就绪时定时器常驻导致内存泄漏。
  deferPollTimer = setInterval(() => {
    if (isI18nReady()) {
      flushDeferredTasks()
      return
    }
    if (++ticks > 300) {
      if (deferPollTimer !== null) {
        clearInterval(deferPollTimer)
        deferPollTimer = null
      }
      deferredTasks.clear()
    }
  }, 100)
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
    // i18n 初始化期间（如 setupI18n 触发的 /sysParameter/i18nOptions 请求）也会走到这里，
    // 此时全局 i18n 尚未创建，useI18n().t() 会透传返回 key，导致弹窗展示原始文案。
    // 未就绪时入队并轮询，待 i18n 就绪后补发正确文案（补发前不写缓存，保证仍能识别版本差异）。
    queueUntilI18nReady('system-upgrade', () => {
      wsCache.set(key, executeVersion)
      showMsg(useI18n().t('common.system_upgrade_tips'), '-sys-upgrade-')
    })
  }
}

const cancelRequestBatch = cancelKey => {
  if (cancelKey) {
    if (cancelKey.indexOf('/**') > -1) {
      const cancelKeyPre = cancelKey.split('/**')[0]
      Object.keys(cancelMap).forEach(key => {
        if (key.indexOf(cancelKeyPre) > -1) {
          cancelMap[key]?.(() => {
            console.warn('Operation canceled by the user,url:' + key)
          })
        }
      })
    } else {
      cancelMap[cancelKey]?.(() => {
        console.warn('Operation canceled by the user,url:' + cancelKey)
      })
    }
  }
}

const cancelAllRequest = () => {
  Object.keys(cancelMap).forEach(key => {
    cancelMap[key]?.(() => {
      console.warn('Operation canceled by the user,url:' + key)
    })
  })
}
export { service, cancelMap, cancelRequestBatch, cancelAllRequest }
