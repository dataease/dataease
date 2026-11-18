import { ref, onBeforeUnmount, onMounted } from 'vue'
import { useCache } from '@/hooks/web/useCache'
import { useEmitt } from '@/hooks/web/useEmitt'

type Sidebar = 'DATASET' | 'DASHBOARD' | 'DATASOURCE' | 'DATA-FILLING' | 'SPREADSHEET'

export const useMoveLine = (type: Sidebar) => {
  const { wsCache } = useCache('localStorage')
  const { emitter } = useEmitt()
  const width = ref(wsCache.get(type) || 280)
  wsCache.set('current-collapse_bar', width.value)
  let dragging = false
  let frameId: number | null = null
  let pendingWidth: number | null = null
  let previousUserSelect = ''

  const getCoordinates = () => {
    if (dragging) return
    dragging = true
    ele.classList.add('dragging')
    document.addEventListener('mousemove', setCoordinates)
    document.addEventListener('mouseup', cancelEvent)
    previousUserSelect = document.body.style.userSelect
    document.body.style.userSelect = 'none'
  }

  const updateWidth = () => {
    frameId = null
    if (pendingWidth === null) return
    const nextWidth = pendingWidth
    pendingWidth = null
    if (width.value === nextWidth) return
    width.value = nextWidth
    ele.style.left = width.value - 5 + 'px'
    emitter.emit('current-collapse_bar', width.value)
  }

  const setCoordinates = (e: MouseEvent) => {
    pendingWidth = Math.max(Math.min(401, e.clientX), 279)
    if (frameId === null) {
      frameId = requestAnimationFrame(updateWidth)
    }
  }

  const clearDrag = () => {
    if (frameId !== null) {
      cancelAnimationFrame(frameId)
      frameId = null
    }
    pendingWidth = null
    document.removeEventListener('mousemove', setCoordinates)
    document.removeEventListener('mouseup', cancelEvent)
    if (dragging) {
      dragging = false
      ele.classList.remove('dragging')
      document.body.style.userSelect = previousUserSelect
    }
  }

  const cancelEvent = () => {
    if (!dragging) return
    if (frameId !== null) {
      cancelAnimationFrame(frameId)
    }
    updateWidth()
    clearDrag()
    wsCache.set('current-collapse_bar', width.value)
    wsCache.set(type, width.value)
  }

  const node = ref()

  const ele = document.createElement('div')
  ele.className = 'sidebar-move-line'
  ele.style.top = '0'
  ele.style.left = width.value - 5 + 'px'
  ele.addEventListener('mousedown', getCoordinates)

  onMounted(() => {
    ;(node.value?.$el || node.value)?.appendChild(ele)
  })

  onBeforeUnmount(() => {
    clearDrag()
    ele.removeEventListener('mousedown', getCoordinates)
    ele.remove()
  })

  return {
    width,
    node
  }
}
