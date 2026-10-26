import { createApp, h, ref, nextTick } from 'vue'
import ElementPlus from 'element-plus-secondary'
import '../../node_modules/element-plus-secondary/dist/index.css'
import Details from '../../src/views/menu/system/report/LogDetails.vue'
import { i18n } from './ui-mock'
import { useEmitt } from '../../src/hooks/web/useEmitt'
createApp({
  setup() {
    const details = ref()
    window['reportNotice'] = () =>
      useEmitt().emitter.emit('report-notice-call', JSON.stringify({ instanceId: '200' }))
    return () =>
      h('div', [
        h(
          'button',
          {
            onClick: async () => {
              details.value.jobLogDetailVisible = true
              await nextTick()
              details.value.startInterval('200', undefined)
            }
          },
          '打开执行日志'
        ),
        h(Details, { ref: details })
      ])
  }
})
  .use(ElementPlus)
  .use(i18n)
  .mount('#app')
