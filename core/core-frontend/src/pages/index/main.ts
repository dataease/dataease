import { createApp } from 'vue'
import '@/style/index.less'
import 'normalize.css/normalize.css'
import '@antv/s2/dist/s2.min.css'
import App from './App.vue'
import { setupI18n } from '@/plugins/vue-i18n'
import { setupStore } from '@/store'
import { setupRouter } from '@/router'
import { setupElementPlus } from '@/plugins/element-plus'
import VueDOMPurifyHTML from 'vue-dompurify-html'
// 注册数据大屏组件
import { setupCustomComponent } from '@/custom-component'
import { installDirective } from '@/directive'
import '@/permission'
import WebSocketPlugin from '../../websocket'
const setupAll = async () => {
  const app = createApp(App)
  installDirective(app)
  setupStore(app)
  await setupI18n(app)
  setupRouter(app)
  setupElementPlus(app)
  setupCustomComponent(app)
  app.use(WebSocketPlugin)
  app.use(VueDOMPurifyHTML)
  app.mount('#app')
}

setupAll()
