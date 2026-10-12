import { Disposable } from '@univerjs/core'
import { IDialogService, ILayoutService } from '@univerjs/ui'

// shortcut: 依赖 Univer 0.25 的面板标记和标题栏结构，升级 Univer 时需核对。
const DIALOG_CLASS = 'dataease-find-replace-dialog'

export class FindReplaceLayoutController extends Disposable {
  constructor(
    @IDialogService dialogService: IDialogService,
    @ILayoutService layoutService: ILayoutService
  ) {
    super()
    let dialog: HTMLElement | null = null
    let revealed = false
    let frame = 0
    let x = 0
    let y = 0
    let dragStart: { clientX: number; clientY: number; x: number; y: number } | null = null

    const updatePosition = () => {
      if (!dialog) return
      const viewport = document.documentElement
      const maxX = Math.max(0, viewport.clientWidth - dialog.offsetWidth)
      const maxY = Math.max(0, viewport.clientHeight - dialog.offsetHeight)
      x = Math.max(0, Math.min(x, maxX))
      y = Math.max(0, Math.min(y, maxY))
      dialog.style.setProperty('--dataease-find-replace-x', `${x}px`)
      dialog.style.setProperty('--dataease-find-replace-y', `${y}px`)
    }
    // 查找切换到替换、窗口缩放都会改变面板尺寸，统一按实际可视区域限制位置。
    const resizeObserver = new ResizeObserver(updatePosition)
    const clear = () => {
      cancelAnimationFrame(frame)
      frame = 0
      dragStart = null
      resizeObserver.disconnect()
      if (dialog) {
        dialog.classList.remove(DIALOG_CLASS)
        dialog.style.removeProperty('--dataease-find-replace-x')
        dialog.style.removeProperty('--dataease-find-replace-y')
        dialog = null
      }
    }
    const bindDialog = () => {
      frame = 0
      if (!revealed) return
      // 面板通过 Portal 挂在 body 下，用布局服务区分编辑器和预览的 Univer 实例。
      const content = Array.from(
        document.querySelectorAll<HTMLElement>('[data-u-comp="find-replace-dialog"]')
      ).find(element => layoutService.checkElementInCurrentContainers(element))
      const panel = content?.closest<HTMLElement>('[role="dialog"]')
      if (!panel) {
        // 等待 React 挂载面板并向布局服务注册容器；关闭时取消等待。
        frame = requestAnimationFrame(bindDialog)
        return
      }
      const rect = panel.getBoundingClientRect()
      x = rect.left
      y = rect.top
      dialog = panel
      dialog.classList.add(DIALOG_CLASS)
      updatePosition()
      resizeObserver.observe(dialog)
    }
    const onMouseDown = (event: MouseEvent) => {
      if (!dialog || event.button !== 0 || !(event.target instanceof Element)) return
      const handle = event.target.closest('[data-drag-handle="true"]')
      if (!handle || !dialog.contains(handle)) return
      event.preventDefault()
      event.stopPropagation()
      // 同时接管标题栏拖动，避免原生 useDraggable 的旧坐标在缩放后覆盖新位置。
      dragStart = { clientX: event.clientX, clientY: event.clientY, x, y }
    }
    const onMouseMove = (event: MouseEvent) => {
      if (!dragStart || !dialog) return
      event.preventDefault()
      event.stopPropagation()
      x = dragStart.x + event.clientX - dragStart.clientX
      y = dragStart.y + event.clientY - dragStart.clientY
      updatePosition()
    }
    const endDrag = () => {
      dragStart = null
    }
    const onResize = () => {
      endDrag()
      updatePosition()
    }
    const subscription = dialogService.getDialogs$().subscribe(options => {
      const open = options.some(
        option => option.children?.label === 'FindReplaceDialog' && option.open
      )
      if (revealed === open) return
      revealed = open
      clear()
      if (revealed) frame = requestAnimationFrame(bindDialog)
    })
    document.addEventListener('mousedown', onMouseDown, true)
    document.addEventListener('mousemove', onMouseMove, true)
    document.addEventListener('mouseup', endDrag, true)
    window.addEventListener('resize', onResize)
    window.addEventListener('blur', endDrag)
    this.disposeWithMe({
      dispose: () => {
        subscription.unsubscribe()
        clear()
        document.removeEventListener('mousedown', onMouseDown, true)
        document.removeEventListener('mousemove', onMouseMove, true)
        document.removeEventListener('mouseup', endDrag, true)
        window.removeEventListener('resize', onResize)
        window.removeEventListener('blur', endDrag)
      }
    })
  }
}
