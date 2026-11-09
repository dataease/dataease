import { cloneDeep, isEqual } from 'lodash-es'
import type { QueryRequest, QueryScope } from '@/config/axios/queryScope'

type QueryOwner = ReturnType<QueryScope['createQuery']>

// 只复用当前组件内尚未完成的工作，不缓存候选结果或跨身份共享数据。
export const createCandidateLoader = (query: QueryOwner, loading: { value: boolean }) => {
  let pending: { key: unknown; request: QueryRequest; promise: Promise<void> } | undefined
  let cancelDelay: (() => void) | undefined
  let disposed = false

  const invalidate = () => {
    query.invalidate()
    cancelDelay?.()
    cancelDelay = undefined
    pending = undefined
    loading.value = false
  }

  const run = (key: unknown, task: (request: QueryRequest) => Promise<void>, delay = 0) => {
    if (disposed) return Promise.resolve()
    if (pending?.request.isCurrent() && isEqual(pending.key, key)) return pending.promise
    invalidate()
    const request = query.start()
    loading.value = true
    let resolve!: () => void
    const promise = new Promise<void>(done => {
      resolve = done
    })
    // 底层可能忽略 abort；调用方不必等待该任务结束，迟到写入仍由代次保护。
    const settle = () => {
      request.signal.removeEventListener('abort', settle)
      resolve()
    }
    request.signal.addEventListener('abort', settle, { once: true })
    if (request.signal.aborted) settle()
    pending = { key: cloneDeep(key), request, promise }
    const execute = async () => {
      try {
        if (delay > 0) {
          await new Promise<void>(resolve => {
            const timer = setTimeout(() => {
              cancelDelay = undefined
              resolve()
            }, delay)
            cancelDelay = () => {
              clearTimeout(timer)
              resolve()
            }
          })
        }
        if (request.isCurrent()) await task(request)
      } catch (error) {
        if (request.isCurrent() && !request.signal.aborted) console.error(error)
      } finally {
        request.finish()
        if (request.isCurrent()) {
          pending = undefined
          loading.value = false
        }
      }
    }
    void execute().then(settle)
    return promise
  }

  return {
    run,
    invalidate,
    dispose: () => {
      disposed = true
      invalidate()
    }
  }
}
