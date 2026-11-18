import { createApp } from 'vue'
import ElementPlus from 'element-plus-secondary'
import 'element-plus-secondary/dist/index.css'
import UserImport from 'user-import'
createApp(UserImport)
  .use(ElementPlus, { namespace: 'ed' })
  .component('Icon', { template: '<span><slot /></span>' })
  .mount('#app')
