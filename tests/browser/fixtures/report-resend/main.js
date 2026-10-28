import { createApp, h, ref, onMounted } from 'vue'
import ElementPlus from 'element-plus-secondary'
import 'element-plus-secondary/dist/index.css'
import { i18n } from './i18n'
import LogDetails from '@/views/menu/system/report/LogDetails.vue'
createApp({
  setup() {
    const detail = ref()
    onMounted(() => {
      detail.value.jobLogDetailVisible = true
      detail.value.startInterval('fixture-resend')
    })
    return () => h(LogDetails, { ref: detail })
  }
})
  .use(ElementPlus)
  .use(i18n)
  .mount('#app')
