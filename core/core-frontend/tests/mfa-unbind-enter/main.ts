import { createApp, h, ref } from 'vue'
import ElementPlus from 'element-plus-secondary'
import 'element-plus-secondary/dist/index.css'
import UserForm from '../../src/views/menu/user-center/UserForm.vue'
createApp({
  setup() {
    const form = ref()
    const bound = ref(true)
    return () => [
      h('button', { onClick: () => form.value.edit() }, '打开个人信息'),
      h(UserForm, {
        ref: form,
        userMfaBound: bound.value,
        onRefreshMfa: value => (bound.value = value)
      })
    ]
  }
})
  .use(ElementPlus)
  .mount('#app')
