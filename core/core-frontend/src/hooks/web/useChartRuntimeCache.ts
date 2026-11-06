import { inject, onBeforeUnmount, provide, shallowReactive, watch, type InjectionKey } from 'vue'
import type { SpreadSheet } from '@antv/s2'
import { cloneDeep } from 'lodash-es'

export interface CanvasIdentity {
  id: string
  type: string
  name?: string
}
const canvasContextKey: InjectionKey<() => CanvasRuntime> = Symbol('canvas-runtime-context')

export interface ChartRuntimeSnapshot {
  canvasInfo?: CanvasIdentity
  view?: Chart
  data?: Partial<Chart['data']>
  request?: Chart['chartExtRequest']
  instance?: SpreadSheet
}
export interface ChartRuntimeCache extends ChartRuntimeSnapshot {
  id: string
  originData?: Partial<Chart['data']>
  pureData?: Partial<Chart['data']>
  pageInfo?: { currentPage: number; pageSize: number; total?: number }
  readonly legacy: boolean
  readonly publishToStore: boolean
  setDetails: (response: Partial<Chart>) => void
  snapshot: () => ChartRuntimeSnapshot | undefined
}
const key: InjectionKey<ChartRuntimeCache> = Symbol('chart-runtime')
const runtimes = new Map<string, ChartRuntimeCache>()
let sequence = 0

export const useChartRuntimeCache = () => inject(key, undefined)

export const provideChartRuntimeCache = (
  getView: () => Chart | undefined,
  getPosition: () => string,
  getCanvasInfo?: () => CanvasIdentity
) => {
  const canvasContext = inject(canvasContextKey, undefined)
  const parent = useChartRuntimeCache()
  if (parent && getView()?.id && parent.view?.id === getView()?.id) return parent
  const cache: ChartRuntimeCache = shallowReactive<ChartRuntimeCache>({
    id: `chart-runtime-${++sequence}`,
    get canvasInfo() {
      return getCanvasInfo?.() || canvasContext?.().dvInfo
    },
    get view() {
      return getView()
    },
    get legacy() {
      const view = getView()
      return !!(view?.isPlugin || (typeof view?.plugin === 'object' && view.plugin?.isPlugin))
    },
    get publishToStore() {
      return ['canvas', 'canvasDataV', 'edit'].includes(getPosition())
    },
    request: getView()?.chartExtRequest,
    setDetails(response) {
      cache.data = response.data
      // 保留本实例的数据集参数，预览不能写入另一个画布的图表配置。
      const view = cache.view
      if (view && response.calParams) {
        const values = new Map((view.calParams || []).map(param => [param.id, param.value]))
        view.calParams = response.calParams.map(param => ({
          ...param,
          value: values.has(param.id) ? values.get(param.id) : param.value
        }))
      }
    },
    snapshot: (): ChartRuntimeSnapshot | undefined =>
      cache.legacy
        ? undefined
        : {
            canvasInfo: cloneDeep(cache.canvasInfo),
            view: cloneDeep(cache.view),
            data: cloneDeep(cache.data),
            request: cloneDeep(cache.request),
            instance: cache.instance
          }
  })
  runtimes.set(cache.id, cache)
  provide(key, cache)
  const reset = () => {
    cache.data = undefined
    cache.originData = undefined
    cache.pureData = undefined
    cache.request = getView()?.chartExtRequest
    cache.instance = undefined
    cache.pageInfo = undefined
  }
  watch(() => getView()?.id, reset, { flush: 'sync' })
  onBeforeUnmount(() => {
    runtimes.delete(cache.id)
    reset()
  })
  return cache
}

export const collectChartRuntimeCaches = (root: HTMLElement) => {
  const ids = new Set<string>()
  if (root.dataset.chartRuntime) ids.add(root.dataset.chartRuntime)
  root.querySelectorAll<HTMLElement>('[data-chart-runtime]').forEach(element => {
    if (element.dataset.chartRuntime) ids.add(element.dataset.chartRuntime)
  })
  return [...ids].map(id => runtimes.get(id)).filter((cache): cache is ChartRuntimeCache => !!cache)
}

export interface CanvasRuntime {
  componentData: object
  canvasStyleData: { screenAdaptor?: string; popupAvailable?: boolean }
  canvasViewInfo: Record<string, Chart>
  dvInfo: CanvasIdentity
}
export const useCanvasRuntime = () => inject(canvasContextKey, undefined)

const canvases = new Map<string, () => CanvasRuntime>()
export const registerCanvasRuntime = (getCanvas: () => CanvasRuntime, inheritContext = false) => {
  const id = `canvas-runtime-${++sequence}`
  canvases.set(id, getCanvas)
  if (!inheritContext || !useCanvasRuntime()) provide(canvasContextKey, getCanvas)
  onBeforeUnmount(() => canvases.delete(id))
  return id
}
export const getCanvasRuntime = (root: HTMLElement) => {
  const id = root.dataset.canvasRuntime
  return id ? canvases.get(id)?.() : undefined
}
