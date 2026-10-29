import { nextTick, onBeforeUnmount, onMounted, ref, watch, type Ref } from 'vue'

const exportLoaders = new Map<HTMLElement, () => Promise<void>>()

// 仅补齐本次导出容器中的图表，避免触发其他仪表板或弹窗的数据查询。
export const prepareViewportChartsForExport = async (root: HTMLElement) => {
  await Promise.all(
    [...exportLoaders].filter(([element]) => root.contains(element)).map(([, load]) => load())
  )
  await nextTick()
  await new Promise<void>(resolve => requestAnimationFrame(() => resolve()))
}

export const useChartViewportLoad = (
  target: Ref<HTMLElement | null>,
  enabled: () => boolean,
  loading: Ref<boolean>,
  flushQuery: () => void
) => {
  const activated = ref(false)
  let ready = false
  let pending: (() => void) | undefined
  let observer: IntersectionObserver | undefined
  let disposed = false
  const cleanupWaiters = new Set<() => void>()

  const activate = () => {
    if (disposed || activated.value) return
    activated.value = true
    observer?.disconnect()
    // 先挂载图表渲染器，再调用其 calcData；避免首轮取数时 ref 尚未就绪。
    nextTick(() => {
      if (disposed) return
      ready = true
      const task = pending
      pending = undefined
      task?.()
    })
  }

  const run = (task: () => void) => {
    if (disposed) return
    if (!enabled() || ready) {
      task()
    } else {
      // 多次筛选只保留最近一次已确认的查询，不读取尚未点击查询的输入值。
      pending = task
    }
  }

  const loadForExport = async () => {
    flushQuery()
    activate()
    await nextTick()
    if (!loading.value || disposed) return
    await new Promise<void>((resolve, reject) => {
      const finish = (error?: Error) => {
        stop()
        clearTimeout(timer)
        cleanupWaiters.delete(cancel)
        error ? reject(error) : resolve()
      }
      const cancel = () => finish(new Error('Chart was unmounted during export'))
      const stop = watch(loading, value => {
        if (!value) finish()
      })
      const timer = setTimeout(() => finish(new Error('Timed out loading chart for export')), 60000)
      cleanupWaiters.add(cancel)
    })
  }

  onMounted(() => {
    if (!enabled() || !target.value || typeof IntersectionObserver === 'undefined') {
      activate()
      return
    }
    exportLoaders.set(target.value, loadForExport)
    observer = new IntersectionObserver(
      entries => {
        if (entries.some(entry => entry.isIntersecting)) activate()
      },
      { rootMargin: '200px 0px', threshold: 0 }
    )
    observer.observe(target.value)
  })

  // 编辑/预览模式在同一个组件实例中切换时，不遗留尚未执行的查询。
  watch(enabled, value => {
    if (!value) activate()
  })
  onBeforeUnmount(() => {
    disposed = true
    observer?.disconnect()
    if (target.value) exportLoaders.delete(target.value)
    pending = undefined
    cleanupWaiters.forEach(cancel => cancel())
  })

  return { run, activated }
}
