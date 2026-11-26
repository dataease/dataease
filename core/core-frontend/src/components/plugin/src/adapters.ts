import * as Vue from 'vue'
import * as Pinia from 'pinia'
import router from '@/router'
import request from '@/config/axios'
import { loadPluginApi, xpackModelApi } from '@/api/plugin'
import { useCache } from '@/hooks/web/useCache'
import { useEmitt } from '@/hooks/web/useEmitt'
import { i18n } from '@/plugins/vue-i18n'
import { isNull } from '@/utils/utils'
import { execute, formatArray, randomKey } from './convert'
import { pluginAxios } from './pluginAxios'
import {
  PluginLoadError,
  PluginModuleLoader,
  isStalePluginCacheKey,
  pluginCacheKey
} from './loader'
import type { PluginModuleAdapter, PreparedPluginModule } from './loader'

const { wsCache } = useCache()

const DISTRIBUTED_KEY = 'xpack-model-distributed'
// 一期只按前端构建版本失效；插件模块级版本需要 XPack 契约，属二期
const CACHE_VERSION =
  typeof __DE_PLUGIN_CACHE_VERSION__ === 'string' ? __DE_PLUGIN_CACHE_VERSION__ : 'dev'

const generateRamStr = (len: number) => {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
  let randomStr = ''
  for (let i = 0; i < len; i++) {
    randomStr += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  return randomStr
}

const decodeModuleName = (jsname: unknown): string => {
  try {
    const moduleName = window.atob(String(jsname ?? '')).split('/')[0]
    if (!moduleName) throw new Error('empty module name')
    return moduleName
  } catch (error) {
    throw new PluginLoadError('LOAD_FAILED', 'invalid plugin jsname', error)
  }
}

// 仅用于区分 unavailable 与 failed 两个状态；更细的失败分类属二期
const classifyRequestError = (
  error: unknown,
  moduleName: string,
  action: string
): PluginLoadError => {
  if (error instanceof PluginLoadError) return error
  const status = Number((error as any)?.httpStatus ?? (error as any)?.response?.status)
  const code = String((error as any)?.code ?? (error as any)?.response?.data?.code ?? '')
  if (status === 401 || status === 403 || code === '60003') {
    return new PluginLoadError('UNAVAILABLE', `plugin ${action} unavailable: ${moduleName}`, error)
  }
  return new PluginLoadError('LOAD_FAILED', `plugin ${action} failed: ${moduleName}`, error)
}

let pluginCacheCleaned = false

// 每个页面只清理一次：删除旧构建版本和旧格式的插件路径缓存
const cleanupStalePluginCache = () => {
  if (pluginCacheCleaned) return
  pluginCacheCleaned = true
  try {
    const storage = (wsCache as any).storage
    if (!storage) return
    Object.keys(storage).forEach(key => {
      if (isStalePluginCacheKey(key, CACHE_VERSION)) wsCache.delete(key)
    })
  } catch {
    // 清理失败不影响本次加载
  }
}

const readCachedSegments = (moduleName: string) => {
  const raw = wsCache.get(pluginCacheKey(CACHE_VERSION, moduleName))
  if (!raw) return null
  try {
    const parsed = typeof raw === 'string' ? JSON.parse(raw) : raw
    return Array.isArray(parsed) ? parsed : null
  } catch {
    return null
  }
}

const deleteCachedSegments = (moduleName: string) => {
  try {
    wsCache.delete(pluginCacheKey(CACHE_VERSION, moduleName))
  } catch {
    // 缓存清理失败不影响重新加载
  }
}

const storeSegments = (moduleName: string, segments: any[]) => {
  try {
    const serialized = segments.map(item => [...item])
    wsCache.set(pluginCacheKey(CACHE_VERSION, moduleName), JSON.stringify(serialized))
  } catch {
    // 缓存写入失败不影响本次加载
  }
}

const importPluginProxy = async (segments: any[], moduleName: string) => {
  try {
    // 分布式构建把 extensions 设为 external，这里保留运行期相对路径，不参与前端打包
    const module = await import(
      `../../../../../../../extensions/${formatArray(segments[8])}/${formatArray(
        segments[9]
      )}/${formatArray(segments[10])}/${formatArray(segments[11])}/${formatArray(segments[12])}.vue`
    )
    if (!module?.default) throw new Error('plugin proxy has no default export')
    return module.default
  } catch (error) {
    throw new PluginLoadError('LOAD_FAILED', `plugin proxy import failed: ${moduleName}`, error)
  }
}

const fetchPluginSegments = async (moduleName: string) => {
  const key = generateRamStr(randomKey())
  const moduleNameKey = window.btoa(moduleName)
  let response
  try {
    response = await loadPluginApi(`${key},${moduleNameKey}`)
  } catch (error) {
    throw classifyRequestError(error, moduleName, 'content')
  }
  const payload = response?.data ?? response
  const segments = execute(String(payload ?? ''), key)
  if (!segments) {
    throw new PluginLoadError('LOAD_FAILED', `plugin payload validation failed: ${moduleName}`)
  }
  return segments
}

class SourcePluginAdapter implements PluginModuleAdapter {
  async prepare(moduleName: string): Promise<PreparedPluginModule> {
    const cached = readCachedSegments(moduleName)
    if (cached) {
      try {
        const component = await importPluginProxy(cached, moduleName)
        return {
          resolve: () => Promise.resolve(component)
        }
      } catch {
        // 缓存路径失效时丢弃并重新获取，避免每次重试都命中同一份坏缓存
        deleteCachedSegments(moduleName)
      }
    }
    const segments = await fetchPluginSegments(moduleName)
    const component = await importPluginProxy(segments, moduleName)
    storeSegments(moduleName, segments)
    return {
      resolve: () => Promise.resolve(component)
    }
  }
}

const installGlobalBridge = () => {
  const bridge = window as any
  bridge['VueDe'] = Vue
  bridge['AxiosDe'] = pluginAxios
  bridge['PiniaDe'] = Pinia
  bridge['vueRouterDe'] = router
  bridge['MittAllDe'] = useEmitt().emitter.all
  bridge['I18nDe'] = i18n
}

class DistributedPluginAdapter implements PluginModuleAdapter {
  async prepare(moduleName: string): Promise<PreparedPluginModule> {
    let pluginModule = (window as any)[moduleName]
    if (!pluginModule) {
      installGlobalBridge()
      let response
      try {
        response = await request.get({ url: `/xpackComponent/pluginStaticInfo/${moduleName}` })
      } catch (error) {
        throw classifyRequestError(error, moduleName, 'static info')
      }
      const script = response?.data ?? response
      try {
        new Function(String(script))()
      } catch (error) {
        throw new PluginLoadError(
          'LOAD_FAILED',
          `plugin script execution failed: ${moduleName}`,
          error
        )
      }
      pluginModule = (window as any)[moduleName]
    }
    const mapping = pluginModule?.mapping
    if (!mapping) {
      throw new PluginLoadError('LOAD_FAILED', `plugin mapping is missing: ${moduleName}`)
    }
    return {
      resolve: async (jsname: string) => {
        const entry = mapping[jsname]
        if (entry === undefined || entry === null) {
          throw new PluginLoadError('LOAD_FAILED', `plugin component is missing: ${jsname}`)
        }
        const loaded = await entry
        const component = loaded?.default ?? loaded
        if (!component) {
          throw new PluginLoadError('LOAD_FAILED', `plugin component is invalid: ${jsname}`)
        }
        return component
      }
    }
  }
}

let distributedModePromise: Promise<boolean | null> | null = null

const readCachedMode = (): boolean | null | undefined => {
  const cached = wsCache.get(DISTRIBUTED_KEY)
  if (cached === null || cached === undefined) return undefined
  if (cached === 'null') return null
  if (cached === true || cached === 'true') return true
  if (cached === false || cached === 'false') return false
  return undefined
}

const resolveDistributedMode = (): Promise<boolean | null> => {
  if (import.meta.env.MODE === 'dev') {
    wsCache.set(DISTRIBUTED_KEY, false)
    return Promise.resolve(false)
  }
  const cached = readCachedMode()
  if (cached !== undefined) return Promise.resolve(cached)
  if (!distributedModePromise) {
    distributedModePromise = xpackModelApi()
      .then(response => {
        const value = isNull(response?.data) ? null : Boolean(response.data)
        wsCache.set(DISTRIBUTED_KEY, value === null ? 'null' : value)
        return value
      })
      .catch(error => {
        throw classifyRequestError(error, 'xpack-model', 'mode query')
      })
      .finally(() => {
        // 只做并发单飞；模式本身以缓存为准，授权变化清缓存后可重新查询
        distributedModePromise = null
      })
  }
  return distributedModePromise
}

const loaderByMode = {
  source: new PluginModuleLoader(new SourcePluginAdapter()),
  distributed: new PluginModuleLoader(new DistributedPluginAdapter())
}

export const loadPluginComponent = async (jsname: unknown): Promise<unknown> => {
  cleanupStalePluginCache()
  const moduleName = decodeModuleName(jsname)
  const distributed = await resolveDistributedMode()
  if (distributed === null) {
    throw new PluginLoadError(
      'UNAVAILABLE',
      `plugin distributed mode is unavailable: ${moduleName}`
    )
  }
  return loaderByMode[distributed ? 'distributed' : 'source'].load(moduleName, String(jsname))
}
