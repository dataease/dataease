import { useEventListener } from '@vueuse/core'

const TELEPORTED_POPPER_SELECTOR = '.ed-popper'

export const useDatasetReplacementOutsideClick = (
  getActiveElement: () => HTMLElement | undefined,
  onOutside: () => void
) => {
  useEventListener(
    document,
    'click',
    event => {
      const activeElement = getActiveElement()
      const target = event.target
      if (!activeElement || !(target instanceof Node)) return
      if (activeElement.contains(target)) return

      const targetElement = target instanceof Element ? target : target.parentElement
      if (targetElement?.closest(TELEPORTED_POPPER_SELECTOR)) return

      onOutside()
    },
    { capture: true }
  )
}
