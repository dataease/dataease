import { checkPermission } from './Permission'
import { vClickOutside } from './ClickOutside'
import type { App } from 'vue'
import VueDOMPurifyHTML from 'vue-dompurify-html'
export const installDirective = (app: App<Element>) => {
  app.use(VueDOMPurifyHTML)
  app.directive('permission', checkPermission)
  app.directive('click-outside', vClickOutside)
}
