import { createApp, h } from 'vue'
import ElementPlus from 'element-plus-secondary'
import 'element-plus-secondary/dist/index.css'
import Workbranch from 'workbranch-real'

const IconStub = { name: 'IconStub', render: () => null }

document.documentElement.style.setProperty('--ed-color-primary', '#f4c542')

createApp({ render: () => h(Workbranch) })
  .component('Icon', IconStub)
  .use(ElementPlus, { namespace: 'ed' })
  .mount('#app')
