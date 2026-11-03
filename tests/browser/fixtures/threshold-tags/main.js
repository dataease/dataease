import { createApp, h, ref, onMounted } from 'vue'
import ElementPlus from 'element-plus-secondary'
import 'element-plus-secondary/dist/index.css'
import Form from 'threshold-form'
import Batch from 'threshold-batch'
const data = {
  reciFlagList: [1], uidList: ['user'], ridList: [], emailList: ['test@example.invalid'],
  larkGroupList: ['group-0'], larksuiteGroupList: ['group-0'], webhookList: ['hook'],
  msgType: 0, msgTitle: '', msgContent: '', repeatSend: false, showFieldValue: false
}
const mode = new URLSearchParams(location.search).get('form')
const batch = mode === 'batch'
createApp({ setup() {
  const form = ref()
  onMounted(() => { if (batch) form.value.init(['fixture']) })
  return () => batch ? h(Batch, { ref: form }) : h(Form, { isEdit: mode !== 'new', warnFormData: data, resourceId: 'fixture', resourceFlag: 0 })
} }).use(ElementPlus, { namespace: 'ed' }).mount('#app')
