import { createApp } from 'vue'
import '@/style.less'
import App from './App.vue'
// import { setupI18n } from './plugins/vueI18n'
import { setupStore } from '@/store'
import { setupRouter } from '@/router'
import { setupElementPlus } from '@/plugins/elementPlus'

const setupAll = async () => {
  const app = createApp(App)
  // await setupI18n(app)
  setupStore(app)
  setupRouter(app)
  setupElementPlus(app)
  app.mount('#app')
}

setupAll()
