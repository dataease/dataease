import { Disposable, IConfigService } from '@univerjs/core'
import { type IUniverUIConfig, UI_PLUGIN_CONFIG_KEY } from '@univerjs/ui'

// Univer 0.25 的 SheetBarMenu 未暴露菜单高度配置，通过触发器关联限定适配范围。
const MENU_SELECTOR = '[data-slot="dropdown-menu-content"][data-state="open"]'

export class SheetMenuLayoutController extends Disposable {
  constructor(@IConfigService configService: IConfigService) {
    super()
    const configured = configService.getConfig<IUniverUIConfig>(UI_PLUGIN_CONFIG_KEY)?.container
    const container =
      typeof configured === 'string' ? document.querySelector<HTMLElement>(configured) : configured
    if (!container) return

    const pendingFrames = new Set<number>()
    const initialized = new WeakSet<HTMLElement>()
    const configure = (menu: HTMLElement) => {
      if (initialized.has(menu)) return
      const trigger = document.getElementById(menu.getAttribute('aria-labelledby') ?? '')
      if (
        !trigger ||
        !container.contains(trigger) ||
        !trigger.querySelector('.univerjs-icon-convert-icon')
      )
        return
      initialized.add(menu)
      menu.classList.add('dataease-sheet-switch-menu')
      menu.style.maxHeight =
        'min(320px, 50dvh, max(0px, calc(var(--radix-popper-available-height, 100dvh) - 8px)))'
      menu.style.overflowY = 'auto'
      menu.style.overscrollBehaviorY = 'contain'
      // 等待 Radix 完成打开时的焦点处理，再定位当前项；只滚动菜单自身。
      const frame = requestAnimationFrame(() => {
        pendingFrames.delete(frame)
        if (!menu.isConnected || menu.dataset.state !== 'open') return
        const selected = menu
          .querySelector<HTMLElement>('.univerjs-icon-check-mark-icon')
          ?.closest<HTMLElement>('[role="menuitem"]')
        if (!selected) return
        selected.focus({ preventScroll: true })
        menu.scrollTop = selected.offsetTop - (menu.clientHeight - selected.offsetHeight) / 2
      })
      pendingFrames.add(frame)
    }
    // 菜单通过 Portal 挂在 body 下，仅检查新增菜单，不监听表格样式或文本变化。
    const observer = new MutationObserver(records => {
      records.forEach(record => {
        if (record.type === 'attributes' && record.target instanceof HTMLElement) {
          if (record.target.matches(MENU_SELECTOR)) configure(record.target)
          else if (record.target.dataset.state === 'closed') initialized.delete(record.target)
        }
        record.addedNodes.forEach(node => {
          if (!(node instanceof HTMLElement)) return
          if (node.matches(MENU_SELECTOR)) configure(node)
          node.querySelectorAll<HTMLElement>(MENU_SELECTOR).forEach(configure)
        })
      })
    })
    observer.observe(document.body, {
      childList: true,
      subtree: true,
      attributes: true,
      attributeFilter: ['data-state']
    })
    this.disposeWithMe({
      dispose: () => {
        observer.disconnect()
        pendingFrames.forEach(cancelAnimationFrame)
        pendingFrames.clear()
      }
    })
  }
}
