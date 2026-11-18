import { createApp, h } from 'vue'
import ElementPlus from 'element-plus-secondary'
import 'element-plus-secondary/dist/index.css'
import RecipientForm from 'report-recipient'

createApp({
  render: () =>
    h(RecipientForm, {
      isEdit: false,
      reciFormData: {},
      resourceId: 'fixture',
      resourceFlag: 0
    })
})
  .use(ElementPlus, { namespace: 'ed' })
  .mount('#app')
