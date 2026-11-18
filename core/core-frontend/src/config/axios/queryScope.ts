// 请求归属仅用于显式纳管的读操作，不根据 HTTP method 推断读写语义。
export interface QueryRequest {
  readonly scopeId: number
  readonly instanceId: string
  readonly requestId: number
  readonly purpose: string
  readonly signal: AbortSignal
  isCurrent: () => boolean
  finish: () => void
  fork: (purpose?: string) => QueryRequest
}

let nextId = 0

export const createQueryScope = () => {
  const scopeId = ++nextId
  const instances = new Set<() => void>()
  const requests = new Map<number, AbortController>()
  const loadedCharts = new Set<string>()
  let disposed = false

  const createQuery = (purpose: string, instanceId = `query-${++nextId}`) => {
    let generation = 0
    let controller: AbortController | undefined
    const requestIds = new Set<number>()
    let released = false
    const invalidate = () => {
      generation++
      controller?.abort()
      controller = undefined
      requestIds.forEach(id => requests.delete(id))
      requestIds.clear()
    }
    const dispose = () => {
      released = true
      invalidate()
      instances.delete(dispose)
    }
    if (!disposed) instances.add(dispose)
    return {
      invalidate,
      dispose,
      start: (): QueryRequest => {
        invalidate()
        const version = generation
        const current = new AbortController()
        controller = current
        if (disposed || released) current.abort()
        const isCurrent = () =>
          !disposed && !released && version === generation && !current.signal.aborted
        const createRequest = (requestPurpose: string): QueryRequest => {
          const id = ++nextId
          if (isCurrent()) {
            requests.set(id, current)
            requestIds.add(id)
          }
          return {
            scopeId,
            instanceId,
            requestId: id,
            purpose: requestPurpose,
            signal: current.signal,
            isCurrent,
            // 网络结束不使工作流失效；后续请求独立登记并共享本轮代次。
            finish: () => {
              requests.delete(id)
              requestIds.delete(id)
            },
            fork: (childPurpose = requestPurpose) => createRequest(childPurpose)
          }
        }
        return createRequest(purpose)
      }
    }
  }

  return {
    scopeId,
    loadedCharts,
    createQuery,
    get pendingCount() {
      return requests.size
    },
    dispose: () => {
      disposed = true
      ;[...instances].forEach(dispose => dispose())
      requests.clear()
      loadedCharts.clear()
    }
  }
}

export type QueryScope = ReturnType<typeof createQueryScope>
