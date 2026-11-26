export type PluginLoadErrorCode =
  | 'UNAVAILABLE'
  | 'LOAD_FAILED'
  | 'TIMEOUT'
  | 'METHOD_NOT_FOUND'
  | 'QUEUE_OVERFLOW'
  | 'DISPOSED'

export class PluginLoadError extends Error {
  code: PluginLoadErrorCode
  detail?: unknown

  constructor(code: PluginLoadErrorCode, message?: string, detail?: unknown) {
    super(message || code)
    this.name = 'PluginLoadError'
    this.code = code
    this.detail = detail
  }
}

export interface PluginCallParam {
  methodName: string
  args?: unknown[]
}

export interface PreparedPluginModule {
  resolve(jsname: string): Promise<unknown>
}

export interface PluginModuleAdapter {
  prepare(moduleName: string): Promise<PreparedPluginModule>
}

export type PluginModuleState = 'loading' | 'ready' | 'unavailable' | 'failed'

export type PluginInstanceState = PluginModuleState | 'disposed'

export interface PluginFailPayload {
  state: 'unavailable' | 'failed'
  code: PluginLoadErrorCode
  message: string
}

export function toPluginLoadError(error: unknown): PluginLoadError {
  if (error instanceof PluginLoadError) return error
  if (error instanceof Error) return new PluginLoadError('LOAD_FAILED', error.message, error)
  return new PluginLoadError(
    'LOAD_FAILED',
    typeof error === 'string' ? error : 'plugin load failed',
    error
  )
}

export function isUnavailablePluginError(error: unknown): boolean {
  return error instanceof PluginLoadError && error.code === 'UNAVAILABLE'
}

export const PLUGIN_CACHE_PREFIX = 'de-plugin-proxy-'

export function pluginCacheKey(version: string, moduleName: string): string {
  return `${PLUGIN_CACHE_PREFIX}${version}-${moduleName}`
}

export function isStalePluginCacheKey(key: string, version: string): boolean {
  if (!key.startsWith(PLUGIN_CACHE_PREFIX)) return false
  return !key.startsWith(`${PLUGIN_CACHE_PREFIX}${version}-`)
}

const DEFAULT_LOAD_TIMEOUT = 30000

class ModuleRecord {
  state: PluginModuleState = 'loading'
  error?: PluginLoadError
  promise!: Promise<PreparedPluginModule>
  components = new Map<string, Promise<unknown>>()
}

const withTimeout = <T>(promise: Promise<T>, timeoutMs: number): Promise<T> => {
  if (!timeoutMs || timeoutMs <= 0) return promise
  return new Promise<T>((resolve, reject) => {
    const timer = setTimeout(() => {
      reject(new PluginLoadError('TIMEOUT', `plugin load timed out after ${timeoutMs}ms`))
    }, timeoutMs)
    promise.then(
      value => {
        clearTimeout(timer)
        resolve(value)
      },
      error => {
        clearTimeout(timer)
        reject(toPluginLoadError(error))
      }
    )
  })
}

export class PluginModuleLoader {
  adapter: PluginModuleAdapter
  timeoutMs: number
  modules = new Map<string, ModuleRecord>()

  constructor(adapter: PluginModuleAdapter, options: { timeoutMs?: number } = {}) {
    this.adapter = adapter
    this.timeoutMs = options.timeoutMs ?? DEFAULT_LOAD_TIMEOUT
  }

  moduleState(moduleName: string): PluginModuleState | undefined {
    return this.modules.get(moduleName)?.state
  }

  async load(moduleName: string, jsname: string): Promise<unknown> {
    const record = this.getRecord(moduleName)
    const prepared = await record.promise
    let component = record.components.get(jsname)
    if (!component) {
      component = withTimeout(
        Promise.resolve().then(() => prepared.resolve(jsname)),
        this.timeoutMs
      ).catch(error => {
        record.components.delete(jsname)
        throw toPluginLoadError(error)
      })
      record.components.set(jsname, component)
    }
    return component
  }

  reset(moduleName?: string) {
    if (moduleName) this.modules.delete(moduleName)
    else this.modules.clear()
  }

  getRecord(moduleName: string): ModuleRecord {
    const existing = this.modules.get(moduleName)
    if (existing) {
      if (existing.state !== 'failed') return existing
      this.modules.delete(moduleName)
    }
    const record = new ModuleRecord()
    record.promise = withTimeout(
      Promise.resolve().then(() => this.adapter.prepare(moduleName)),
      this.timeoutMs
    ).then(
      prepared => {
        record.state = 'ready'
        return prepared
      },
      error => {
        const failure = toPluginLoadError(error)
        record.state = failure.code === 'UNAVAILABLE' ? 'unavailable' : 'failed'
        record.error = failure
        throw failure
      }
    )
    this.modules.set(moduleName, record)
    return record
  }
}

interface PendingCall {
  param: PluginCallParam
  resolve: (value: unknown) => void
  reject: (reason?: unknown) => void
}

export class PluginMethodQueue {
  maxPending: number
  pending: PendingCall[] = []
  target: ((param: PluginCallParam) => unknown) | null = null
  closedError: PluginLoadError | null = null

  constructor(maxPending = 64) {
    this.maxPending = maxPending
  }

  get size(): number {
    return this.pending.length
  }

  push(param: PluginCallParam): Promise<unknown> {
    if (this.closedError) return Promise.reject(this.closedError)
    if (this.target) {
      // 就绪后保持同步调用契约：异常同步抛出，返回值可由调用方直接使用
      return Promise.resolve(this.target(param))
    }
    if (this.pending.length >= this.maxPending) {
      return Promise.reject(
        new PluginLoadError('QUEUE_OVERFLOW', `plugin call queue exceeds ${this.maxPending}`)
      )
    }
    return new Promise<unknown>((resolve, reject) => {
      this.pending.push({ param, resolve, reject })
    })
  }

  bind(target: (param: PluginCallParam) => unknown) {
    this.target = target
  }

  unbind() {
    this.target = null
  }

  close(error: PluginLoadError) {
    this.closedError = error
    this.rejectPending(error)
  }

  reset(error: PluginLoadError) {
    this.target = null
    this.rejectPending(error)
    this.closedError = null
  }

  drain() {
    const target = this.target
    if (!target || !this.pending.length) return
    const calls = this.pending.splice(0)
    calls.forEach(call => {
      try {
        call.resolve(target(call.param))
      } catch (error) {
        call.reject(error)
      }
    })
  }

  rejectPending(error: PluginLoadError) {
    const calls = this.pending.splice(0)
    calls.forEach(call => call.reject(error))
  }
}
