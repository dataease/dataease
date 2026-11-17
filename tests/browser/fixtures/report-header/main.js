import { createApp, ref } from 'vue'
import ElementPlus from 'element-plus-secondary'
import 'element-plus-secondary/dist/index.css'
import BaseForm from '@/views/menu/system/report/form/BaseForm.vue'
createApp({
  components: { BaseForm },
  setup() {
    const form = ref()
    const key = ref(0)
    const saved = ref({
      rid: 'dv',
      name: '合成任务',
      title: '合成标题',
      viewIdList: ['view'],
    })
    window.headerTest = {
      async save() {
        return JSON.parse(JSON.stringify(await form.value.getFormData()))
      },
      reopen(value) {
        saved.value = value
        key.value++
      },
    }
    return { form, key, saved }
  },
  template:
    '<BaseForm :key="key" ref="form" :is-edit="true" :base-form-data="saved" />',
})
  .use(ElementPlus)
  .mount('#app')
