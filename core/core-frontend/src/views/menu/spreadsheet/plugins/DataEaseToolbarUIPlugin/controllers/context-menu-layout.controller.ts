import { Disposable, IConfigService } from '@univerjs/core'
import { type IUniverUIConfig, UI_PLUGIN_CONFIG_KEY } from '@univerjs/ui'

const SUBMENU_SELECTOR = '[data-u-context-menu-submenu="true"]'

export class ContextMenuLayoutController extends Disposable {
  constructor(@IConfigService configService: IConfigService) {
    super()
    const configured = configService.getConfig<IUniverUIConfig>(UI_PLUGIN_CONFIG_KEY)?.container
    const container =
      typeof configured === 'string' ? document.querySelector<HTMLElement>(configured) : configured
    if (!container) return

    let ownsMenu = false
    let frame = 0
    const pending = new Set<HTMLElement>()
    const triggers = new Map<HTMLElement, HTMLElement>()
    let hoveredTrigger: HTMLElement | null = null
    const activate = (menu: HTMLElement) => {
      const path = new Set<HTMLElement>()
      let ancestor: HTMLElement | null = menu
      while (ancestor && !path.has(ancestor)) {
        path.add(ancestor)
        ancestor = triggers.get(ancestor)?.closest<HTMLElement>(SUBMENU_SELECTOR) ?? null
      }
      triggers.forEach((_, item) => {
        item.classList.toggle('dataease-inactive-context-submenu', !path.has(item))
      })
    }
    const onMouseOver = (event: MouseEvent) => {
      if (!ownsMenu || !(event.target instanceof Element)) return
      hoveredTrigger = event.target.closest<HTMLElement>('button')
      triggers.forEach((trigger, menu) => {
        if (trigger === hoveredTrigger && menu.style.visibility === 'visible') activate(menu)
      })
    }
    const onContextMenu = (event: MouseEvent) => {
      ownsMenu = event.target instanceof Node && container.contains(event.target)
      pending.clear()
      hoveredTrigger = null
    }
    const observer = new MutationObserver(records => {
      if (!ownsMenu) return
      records.forEach(record => {
        const menu = record.target
        if (
          menu instanceof HTMLElement &&
          menu.style.visibility === 'visible' &&
          triggers.get(menu) === hoveredTrigger
        )
          activate(menu)
        if (
          !(menu instanceof HTMLElement) ||
          !menu.matches(SUBMENU_SELECTOR) ||
          menu.style.visibility !== 'hidden' ||
          !/visibility:\s*visible(?:;|$)/.test(record.oldValue ?? '')
        )
          return
        pending.add(menu)
      })
      if (!pending.size || frame) return
      frame = requestAnimationFrame(() => {
        frame = 0
        const menu = [...pending].find(
          item => item.isConnected && item.style.visibility === 'hidden'
        )
        pending.clear()
        if (!ownsMenu || !menu) return
        // Univer 0.25 在重复进入已展开的父菜单时隐藏子菜单，却未重新触发定位 effect。
        // 通过其已有的滚动定位监听恢复 React 状态，不直接覆盖可见性或改写依赖源码。
        // 事件不产生实际滚动；只处理曾可见、随后被隐藏且仍挂载的右键子菜单。
        menu.dispatchEvent(new Event('scroll'))
      })
    })
    // Portal 位于 body 下；只监听子菜单自身样式，避免订阅整个表格的样式更新。
    const menus = new Set<HTMLElement>()
    const refreshMenus = () => {
      const current = [...document.querySelectorAll<HTMLElement>(SUBMENU_SELECTOR)]
      if (current.length === menus.size && current.every(menu => menus.has(menu))) return
      observer.disconnect()
      triggers.forEach((_, menu) => {
        if (!current.includes(menu)) triggers.delete(menu)
      })
      menus.clear()
      current.forEach(menu => {
        menus.add(menu)
        if (ownsMenu && hoveredTrigger && !triggers.has(menu)) {
          triggers.set(menu, hoveredTrigger)
          if (menu.style.visibility === 'visible') activate(menu)
        }
        observer.observe(menu, {
          attributes: true,
          attributeFilter: ['style'],
          attributeOldValue: true
        })
      })
    }
    const portalObserver = new MutationObserver(refreshMenus)
    document.addEventListener('contextmenu', onContextMenu, true)
    document.addEventListener('mouseover', onMouseOver, true)
    portalObserver.observe(document.body, { childList: true })
    refreshMenus()
    this.disposeWithMe({
      dispose: () => {
        document.removeEventListener('contextmenu', onContextMenu, true)
        document.removeEventListener('mouseover', onMouseOver, true)
        triggers.forEach((_, menu) => menu.classList.remove('dataease-inactive-context-submenu'))
        triggers.clear()
        observer.disconnect()
        portalObserver.disconnect()
        menus.clear()
        cancelAnimationFrame(frame)
        pending.clear()
      }
    })
  }
}
