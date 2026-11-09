import { onBeforeUnmount } from 'vue'
import { createCanvasEditSession } from '@/utils/canvasEditSession'

export const useCanvasEditSession = (...args: Parameters<typeof createCanvasEditSession>) => {
  const session = createCanvasEditSession(...args)
  onBeforeUnmount(session.dispose)
  return session
}
