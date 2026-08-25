import { onBeforeUnmount, ref } from 'vue'
import { useEmitt } from '@/hooks/web/useEmitt'

export const RENDER_CHART_ALL = 'renderChart-all'
export const RENDER_CHART_ALL_START = 'renderChart-all-start'
export const RENDER_CHART_ALL_DISPATCHED = 'renderChart-all-dispatched'
export const RENDER_CHART_ALL_ITEM_START = 'renderChart-all-item-start'
export const RENDER_CHART_ALL_ITEM_FINISH = 'renderChart-all-item-finish'

export interface RenderChartAllPayload {
  renderTaskId: string
  source: string
}

export interface RenderChartAllItemPayload extends RenderChartAllPayload {
  viewId: string
}

export const isRenderChartAllPayload = (payload: unknown): payload is RenderChartAllPayload => {
  return Boolean(
    payload &&
      typeof payload === 'object' &&
      'renderTaskId' in payload &&
      typeof (payload as RenderChartAllPayload).renderTaskId === 'string'
  )
}

const isRenderChartAllItemPayload = (payload: unknown): payload is RenderChartAllItemPayload => {
  return Boolean(
    isRenderChartAllPayload(payload) &&
      'viewId' in payload &&
      typeof (payload as RenderChartAllItemPayload).viewId === 'string'
  )
}

const createRenderChartAllPayload = (source: string): RenderChartAllPayload => ({
  renderTaskId: `${source}-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
  source
})

export const emitRenderChartAllWithLoading = (source: string) => {
  const payload = createRenderChartAllPayload(source)
  const { emitter } = useEmitt()
  emitter.emit(RENDER_CHART_ALL_START, payload)
  emitter.emit(RENDER_CHART_ALL, payload)
  emitter.emit(RENDER_CHART_ALL_DISPATCHED, payload)
}

export const useRenderChartAllLoading = () => {
  const renderChartAllLoading = ref(false)
  const pendingViewIds = new Set<string>()
  let currentRenderTaskId = ''
  let renderAllDispatched = false
  let closeTimer: number | undefined
  let fallbackTimer: number | undefined

  const clearCloseTimer = () => {
    if (closeTimer) {
      window.clearTimeout(closeTimer)
      closeTimer = undefined
    }
  }

  const clearFallbackTimer = () => {
    if (fallbackTimer) {
      window.clearTimeout(fallbackTimer)
      fallbackTimer = undefined
    }
  }

  const closeLoading = () => {
    clearCloseTimer()
    clearFallbackTimer()
    pendingViewIds.clear()
    currentRenderTaskId = ''
    renderAllDispatched = false
    renderChartAllLoading.value = false
  }

  const resetFallbackTimer = () => {
    clearFallbackTimer()
    fallbackTimer = window.setTimeout(closeLoading, 10000)
  }

  const isCurrentTask = (payload: RenderChartAllPayload) => {
    return payload.renderTaskId === currentRenderTaskId
  }

  const tryCloseLoading = () => {
    if (!renderAllDispatched || pendingViewIds.size > 0) {
      return
    }
    clearCloseTimer()
    closeTimer = window.setTimeout(closeLoading, 100)
  }

  useEmitt({
    name: RENDER_CHART_ALL_START,
    callback: payload => {
      if (!isRenderChartAllPayload(payload)) {
        return
      }
      closeLoading()
      currentRenderTaskId = payload.renderTaskId
      renderChartAllLoading.value = true
      resetFallbackTimer()
    }
  })

  useEmitt({
    name: RENDER_CHART_ALL_ITEM_START,
    callback: payload => {
      if (!isRenderChartAllItemPayload(payload) || !isCurrentTask(payload)) {
        return
      }
      clearCloseTimer()
      pendingViewIds.add(payload.viewId)
      resetFallbackTimer()
    }
  })

  useEmitt({
    name: RENDER_CHART_ALL_ITEM_FINISH,
    callback: payload => {
      if (!isRenderChartAllItemPayload(payload) || !isCurrentTask(payload)) {
        return
      }
      pendingViewIds.delete(payload.viewId)
      resetFallbackTimer()
      tryCloseLoading()
    }
  })

  useEmitt({
    name: RENDER_CHART_ALL_DISPATCHED,
    callback: payload => {
      if (!isRenderChartAllPayload(payload) || !isCurrentTask(payload)) {
        return
      }
      renderAllDispatched = true
      resetFallbackTimer()
      tryCloseLoading()
    }
  })

  onBeforeUnmount(closeLoading)
  console.log('==test==' + renderChartAllLoading.value)
  return renderChartAllLoading
}
