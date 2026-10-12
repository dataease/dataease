/**
 * 统一管理预览与单图刷新计时器，不参与数据查询和图元更新
 */
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'

/**
 * 调用方提供当前启用状态、频率及秒或分钟单位
 */
interface RefreshSettings {
  enabled: boolean
  interval: number
  unit: string
}

/**
 * 独立预览与仪表板编辑页的全屏预览均允许刷新，大屏底层编辑画布不启动刷新
 */
export function isChartRefreshPreview(position: string, editMode?: string, dvType?: string) {
  return (
    ['preview', 'edit-preview'].includes(position) ||
    (['canvas', 'edit'].includes(position) && editMode === 'preview' && dvType === 'dashboard')
  )
}

/**
 * 每次调用根据预览状态和频率维护一个计时器，离开预览或卸载时停止
 * 返回响应式运行状态，用于单图刷新开启时忽略外部刷新通知
 */
export function useChartRefreshTimer(settings: () => RefreshSettings, refresh: () => void) {
  const running = ref(false)
  let timer: ReturnType<typeof setInterval> | undefined
  let stopWatching: (() => void) | undefined
  /**
   * 清理本次调用持有的计时器，并同步单图刷新优先级所需的运行状态
   */
  const stop = () => {
    if (timer !== undefined) clearInterval(timer)
    timer = undefined
    running.value = false
  }
  onMounted(() => {
    stopWatching = watch(
      () => {
        const { enabled, interval, unit } = settings()
        return [enabled, interval, unit] as const
      },
      ([enabled, interval, unit]) => {
        stop()
        if (!enabled) return
        const duration = Number(interval) * (unit === 'second' ? 1000 : 60000)
        if (!Number.isFinite(duration) || duration <= 0) return
        timer = setInterval(refresh, duration)
        running.value = true
      },
      { immediate: true, flush: 'sync' }
    )
  })
  onBeforeUnmount(() => {
    stopWatching?.()
    stop()
  })
  return running
}
