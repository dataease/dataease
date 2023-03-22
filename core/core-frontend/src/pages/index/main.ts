import { createApp } from 'vue'
import '@/style.css'
import '@/plugins/svgIcon'

import App from './App.vue'
import { setupI18n } from '@/plugins/vueI18n'
import { setupStore } from '@/store'
import { setupRouter } from '@/router'
import { setupElementPlus } from '@/plugins/elementPlus'
// 注册数据大屏组件
import { setupCustomComponent } from '@/custom-component'

import '@/permission'
const setupAll = async () => {
  const app = createApp(App)
  await setupI18n(app)
  setupStore(app)
  setupRouter(app)
  setupElementPlus(app)
  setupCustomComponent(app)
  app.mount('#app')
}

setupAll()
