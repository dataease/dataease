import { createApp, h, ref } from 'vue'
import ElementPlus from 'element-plus-secondary'
import 'element-plus-secondary/dist/index.css'
import FontSettings from '@/views/system/font/FontSettings.vue'
import { store } from '@/store'
import { setupI18n } from '@/plugins/vue-i18n'
import { useLocaleStoreWithOut } from '@/store/modules/locale'
const app = createApp({
  setup() {
    const dialog = ref()
    return () =>
      h('div', [
        h('button', { onClick: () => dialog.value.open() }, '打开字体设置'),
        h(FontSettings, { ref: dialog })
      ])
  }
})
app.use(store)
app.use(ElementPlus)
const locale = useLocaleStoreWithOut()
locale.customLoaded = true
locale.setLang('zh-CN')
await setupI18n(app)
app.mount('#app')
