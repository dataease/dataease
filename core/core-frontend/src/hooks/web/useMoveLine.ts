import { ref, onBeforeUnmount } from 'vue'

import { useCache } from '@/hooks/web/useCache'

type Sidebar = 'DATASET' | 'DASHBOARD' | 'DATASOURCE'

export const useMoveLine = (type: Sidebar, node: HTMLElement, width: ref) => {
  const ele = ref<null | HTMLDivElement>(null)
  const { wsCache } = useCache('localStorage')
  width.value = wsCache.get(type) || 260

  const getCoordinates = () => {
    document.addEventListener('mousemove', setCoordinates)
    document.addEventListener('mouseup', cancelEvent)
    document.querySelector('body').style['user-select'] = 'none'
  }

  const setCoordinates = (e: MouseEvent) => {
    const x = e.clientX
    if (x > 400 || x < 260) {
      cancelEvent()
      return
    }
    width.value = x
    ele.value.style.left = width.value - 2 + 'px'
  }

  const cancelEvent = () => {
    if (!ele.value) return
    document.querySelector('body').style['user-select'] = 'auto'
    wsCache.set(type, width.value)
    document.removeEventListener('mousemove', setCoordinates)
  }

  ele.value = document.createElement('div')
  ele.value.className = 'sidebar-move-line'
  ele.value.style.top = '0'
  ele.value.style.left = width.value - 2 + 'px'
  node.appendChild(ele.value)
  ele.value.addEventListener('mousedown', getCoordinates)

  onBeforeUnmount(() => {
    cancelEvent()
    ele.value.removeEventListener('mousedown', getCoordinates)
    node?.removeChild(ele.value)
    ele.value = null
    width.value = null
  })
}
