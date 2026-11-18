import axios, { type AxiosRequestConfig } from 'axios'

// 兼容尚未迁移的明确读接口；保存、发布、上传等写请求不进入取消表。
const legacyReads = [
  '/chartData/getData',
  '/datasetData/previewData',
  '/datasource/checkApiDatasource',
  '/dataVisualization/findById',
  '/linkage/getVisualizationAllLinkageInfo',
  '/linkJump/queryVisualizationJumpInfo'
]
export const cancelMap: Record<string, (message?: string) => void> = Object.create(null)

export const registerLegacyCancellation = (config: AxiosRequestConfig) => {
  const url = config.url
  if (!url) return
  const path = `/${url.replace(/^\//, '').split('?')[0]}`
  if (!legacyReads.some(url => path === url || path.startsWith(`${url}/`))) return
  const key = path === '/chartData/getData' ? `chartData/getData/${config.data.id}` : url
  let cancel: (message?: string) => void
  const token = new axios.CancelToken(c => {
    cancel = message => {
      if (cancelMap[key] === cancel) delete cancelMap[key]
      c(message)
      // CancelToken 的取消异常默认不带 config，补齐供 loading/清理拦截器使用。
      if (token.reason) Object.assign(token.reason, { config })
    }
    cancelMap[key] = cancel
  })
  config.cancelToken = token
  return () => {
    // 旧请求结束不得删除同 key 的新请求句柄。
    if (cancelMap[key] === cancel) delete cancelMap[key]
  }
}

export const cancelRequestBatch = (cancelKey: string) => {
  if (!cancelKey) return
  if (cancelKey.includes('/**')) {
    const prefix = cancelKey.split('/**')[0]
    Object.keys(cancelMap).forEach(key => {
      if (key.includes(prefix)) cancelMap[key]?.()
    })
  } else {
    cancelMap[cancelKey]?.()
  }
}

// 仅兼容旧调用，不能取消已纳管的实例请求。
export const cancelAllRequest = () => Object.values(cancelMap).forEach(cancel => cancel())
