import { checkPermission } from './Permission'
import type { App } from 'vue'
export const installDerective = (app: App<Element>) => {
  app.directive('permission', checkPermission)
}
