import { checkPermission } from './Permission'
import { vClickOutside } from './ClickOutside'
import type { App } from 'vue'
export const installDirective = (app: App<Element>) => {
  app.directive('permission', checkPermission)
  app.directive('click-outside', vClickOutside)
}
