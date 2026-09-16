import { Disposable, IConfigService } from '@univerjs/core'
import { type IUniverUIConfig, UI_PLUGIN_CONFIG_KEY } from '@univerjs/ui'

export class IconSetValueController extends Disposable {
  constructor(@IConfigService configService: IConfigService) {
    super()
    const configured = configService.getConfig<IUniverUIConfig>(UI_PLUGIN_CONFIG_KEY)?.container
    const container =
      typeof configured === 'string' ? document.querySelector<HTMLElement>(configured) : configured
    if (!container) return
    let observer: MutationObserver | undefined
    let frame = 0
    const clear = () => {
      observer?.disconnect()
      observer = undefined
      cancelAnimationFrame(frame)
    }
    const capture = (event: Event) => {
      if (!(event.target instanceof Element)) return
      const trigger = event.target.closest<HTMLElement>('[data-u-comp="select"]')
      if (!trigger || !container.contains(trigger)) return
      clear()
      const operator = trigger.textContent?.trim()
      if (!['>', '>=', '≥'].includes(operator ?? '')) return
      const row = trigger.parentElement
      // 图标集每一项由图标/比较符、标题、类型/数值三行组成，避免影响其他条件格式。
      if (
        !row?.querySelector('[data-slot="popover-trigger"] img, .univerjs-icon-slash-double-icon')
      )
        return
      const input = row.parentElement?.querySelector<HTMLInputElement>('input')
      if (!input || input.value.trim() === '' || !Number.isFinite(Number(input.value))) return
      const value = input.value
      observer = new MutationObserver(() => {
        if (trigger.textContent?.trim() === operator && trigger.dataset.state !== 'closed') return
        observer?.disconnect()
        // 等待原组件写入默认值后，通过正常 input 事件同步其编辑状态和区间校验。
        frame = requestAnimationFrame(() => {
          if (!input.isConnected || !trigger.isConnected) return
          const setter = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value')?.set
          setter?.call(input, value)
          input.dispatchEvent(new Event('input', { bubbles: true }))
        })
      })
      observer.observe(trigger, {
        childList: true,
        subtree: true,
        characterData: true,
        attributes: true,
        attributeFilter: ['data-state']
      })
    }
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') clear()
      else if (['Enter', ' ', 'ArrowDown', 'ArrowUp'].includes(event.key)) capture(event)
    }
    container.addEventListener('pointerdown', capture, true)
    container.addEventListener('keydown', onKeyDown, true)
    this.disposeWithMe({
      dispose: () => {
        clear()
        container.removeEventListener('pointerdown', capture, true)
        container.removeEventListener('keydown', onKeyDown, true)
      }
    })
  }
}
