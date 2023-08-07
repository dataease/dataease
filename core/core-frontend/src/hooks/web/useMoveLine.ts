import { ref, onBeforeUnmount, onMounted } from 'vue'
import { useCache } from '@/hooks/web/useCache'

type Sidebar = 'DATASET' | 'DASHBOARD' | 'DATASOURCE'

export const useMoveLine = (type: Sidebar) => {
  const { wsCache } = useCache('localStorage')
  const width = ref(wsCache.get(type) || 260)

  const getCoordinates = () => {
    document.addEventListener('mousemove', setCoordinates)
    document.addEventListener('mouseup', cancelEvent)
    document.querySelector('body').style['user-select'] = 'none'
  }

  const setCoordinates = (e: MouseEvent) => {
    const x = e.clientX
    if (x > 401 || x < 259) {
      width.value = Math.max(Math.min(401, x), 259)
      ele.style.left = width.value - 2 + 'px'
      return
    }
    ele.style.left = width.value - 2 + 'px'
    width.value = x
  }

  const cancelEvent = () => {
    document.querySelector('body').style['user-select'] = 'auto'
    wsCache.set(type, width.value)
    document.removeEventListener('mousemove', setCoordinates)
  }

  const node = ref()

  const ele = document.createElement('div')
  ele.className = 'sidebar-move-line'
  ele.style.top = '0'
  ele.style.left = width.value - 2 + 'px'
  ele.addEventListener('mousedown', getCoordinates)

  onMounted(() => {
    node.value.$el.appendChild(ele)
  })

  onBeforeUnmount(() => {
    cancelEvent()
    ele.removeEventListener('mousedown', getCoordinates)
    node.value.$el?.removeChild?.(ele)
    width.value = null
  })

  return {
    width,
    node
  }
}
