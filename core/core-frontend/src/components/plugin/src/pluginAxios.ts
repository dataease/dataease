import axios, {
  AxiosHeaders,
  type AxiosAdapter,
  type AxiosInstance,
  type InternalAxiosRequestConfig
} from 'axios'
import { configHandler } from '@/config/axios/refresh'
import { securityConfig } from '@/config/axios/hmac'
import { useEmbedded } from '@/store/modules/embedded'
import { useLinkStoreWithOut } from '@/store/modules/link'
import { useCache } from '@/hooks/web/useCache'

const wrappedAdapters = new WeakSet<AxiosAdapter>()
const apiPath = import.meta.env.VITE_API_BASEPATH

const apiBase = () => {
  const embedded = useEmbedded()
  const path = embedded.baseUrl ? embedded.baseUrl + apiPath.replace(/^\.\//, '') : apiPath
  return new URL(path, document.baseURI).href.replace(/\/$/, '')
}

// Legacy plugins use ./de2api even when the div SDK lives on a different origin.
// Only rebase these known API bases; arbitrary plugin destinations remain untouched.
const isLegacyBase = (base: string) =>
  !/^(?:[a-z][a-z\d+.-]*:|\/\/)/i.test(base) &&
  [apiPath, './de2api', 'de2api'].includes(base.replace(/\/$/, ''))

const withinApi = (uri: string, base: string) => {
  const target = new URL(uri, document.baseURI)
  const expected = new URL(base)
  return (
    !target.username &&
    !target.password &&
    target.origin === expected.origin &&
    (target.pathname === expected.pathname || target.pathname.startsWith(expected.pathname + '/'))
  )
}

const clearDataEaseHeaders = (config: InternalAxiosRequestConfig) => {
  config.headers = AxiosHeaders.from(config.headers)
  for (const name of ['X-DE-TOKEN', 'X-DE-LINK-TOKEN', 'X-EMBEDDED-TOKEN', 'X-DE-ADMIN-PROXY']) {
    config.headers.delete(name)
  }
  if (
    /^Signature\s.*keyId="dataease-key"/i.test(String(config.headers.get('Authorization') || ''))
  ) {
    config.headers.delete('Authorization')
    config.headers.delete('X-Date')
  }
}

const prepareRequest = async (config: InternalAxiosRequestConfig) => {
  const base = apiBase()
  const candidate = { ...config }
  if (candidate.baseURL && isLegacyBase(candidate.baseURL)) candidate.baseURL = base
  if (!withinApi(axios.getUri(candidate), base)) {
    // Legacy interceptors may already have attached DataEase credentials to an
    // external destination. Do not propagate those credentials outside this API.
    clearDataEaseHeaders(config)
    return config
  }

  config.baseURL = candidate.baseURL
  // Old plugin bundles can retain stale signatures and independent copies of auth stores.
  clearDataEaseHeaders(config)
  config.headers.delete('X-Date')
  if (/^Signature\s/i.test(String(config.headers.get('Authorization') || ''))) {
    config.headers.delete('Authorization')
  }
  config = await configHandler(config)
  const embedded = useEmbedded()
  const link = useLinkStoreWithOut()
  if (link.getLinkToken) config.headers.set('X-DE-LINK-TOKEN', link.getLinkToken)
  else if (embedded.token) config.headers.set('X-EMBEDDED-TOKEN', embedded.token)
  const { wsCache } = useCache()
  const proxy = wsCache.get('user.proxyInfo')
  if (proxy?.proxy && proxy?.proxySecret) config.headers.set('X-DE-ADMIN-PROXY', proxy.proxySecret)

  // Bootstrap must target the deployment API, including for absolute request URLs.
  const signatureConfig = { ...config, baseURL: base }
  await securityConfig(signatureConfig, axios.getUri(config))
  config.headers = AxiosHeaders.from(signatureConfig.headers)
  return config
}

const wrapInstance = (instance: AxiosInstance): AxiosInstance => {
  // Axios runs request interceptors in reverse registration order. Register first so
  // the adapter is wrapped after the plugin's interceptors have chosen its destination.
  instance.interceptors.request.use(config => {
    const adapter = axios.getAdapter(config.adapter, config)
    if (!wrappedAdapters.has(adapter)) {
      const wrapped: AxiosAdapter = async request => {
        const prepared = await prepareRequest(request)
        prepared.cancelToken?.throwIfRequested()
        if (prepared.signal?.aborted) throw new axios.CanceledError(undefined, prepared)
        return adapter(prepared)
      }
      wrappedAdapters.add(wrapped)
      config.adapter = wrapped
    }
    return config
  })
  const create = instance.create
  instance.create = config => wrapInstance(create(config))
  return instance
}

const root = wrapInstance(axios.create())
// Keep Axios static utilities (CancelToken, isCancel, AxiosHeaders, etc.) while routing
// callable requests, methods and .create() through the isolated plugin instance.
export const pluginAxios: typeof axios = new Proxy(axios, {
  apply(_target, thisArg, args) {
    return Reflect.apply(root, thisArg, args)
  },
  get(target, key) {
    if (key === 'default') return pluginAxios
    return key in root ? Reflect.get(root, key) : Reflect.get(target, key)
  }
})
