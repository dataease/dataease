import { createApp, h, ref, nextTick } from 'vue'
import ElementPlus from 'element-plus-secondary'
import 'element-plus-secondary/dist/index.css'
import Form from 'sync-import-form'
createApp({
  setup() {
    const form = ref()
    const open = async (another = false) => {
      form.value.syncTargetToDatasourceFormVisible = true
      await nextTick()
      form.value.init({
        id: another ? 'synthetic-target-2' : 'synthetic-target',
        name: another ? 'Second target' : 'Synthetic Doris',
        type: new URLSearchParams(location.search).get('type') || 'doris',
        configuration: { host: 'fixture.invalid', password: '******' }
      })
    }
    return () =>
      h('main', [
        h('button', { onClick: () => open() }, 'Open import'),
        h('button', { onClick: () => open(true) }, 'Open another import'),
        h(Form, { ref: form })
      ])
  }
})
  .component('Icon', {
    render() {
      return null
    }
  })
  .use(ElementPlus, { namespace: 'ed' })
  .mount('#app')
