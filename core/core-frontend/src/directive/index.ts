import { checkPermission } from './Permission'
import type { App } from 'vue'
export const installDirective = (app: App<Element>) => {
  app.directive('permission', checkPermission)
}
