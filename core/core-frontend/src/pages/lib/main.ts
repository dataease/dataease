import '@/style/index.less'
import '@/plugins/svg-icon'
import 'normalize.css/normalize.css'
import { setupI18n } from '@/plugins/vue-i18n'
import { setupStore } from '@/store'
import { setupElementPlus } from '@/plugins/element-plus'
import { App } from 'vue'
import DeDashboard from './dashboard'

export default {
  install: async (app: App) => {
    app.use(DeDashboard)
    await setupI18n(app)
    setupStore(app)
    setupElementPlus(app)
  }
}
