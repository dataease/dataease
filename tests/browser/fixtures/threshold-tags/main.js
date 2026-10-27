import { createApp, h } from 'vue'
import ElementPlus from 'element-plus-secondary'
import 'element-plus-secondary/dist/index.css'
import Form from 'threshold-form'
const data = {
  reciFlagList: [1], uidList: ['user'], ridList: [], emailList: ['test@example.invalid'],
  larkGroupList: ['group-0'], larksuiteGroupList: ['group-0'], webhookList: ['hook'],
  msgType: 0, msgTitle: '', msgContent: '', repeatSend: false, showFieldValue: false
}
createApp({ render: () => h(Form, { isEdit: true, warnFormData: data, resourceId: 'fixture', resourceFlag: 0 }) }).use(ElementPlus, { namespace: 'ed' }).mount('#app')
