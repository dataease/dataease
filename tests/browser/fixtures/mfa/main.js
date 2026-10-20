import { createApp, h, ref } from 'vue'
import ElementPlus from 'element-plus-secondary'
import 'element-plus-secondary/dist/index.css'
import MfaStep from 'mfa-component'
import { events } from './bridge'
const params = new URLSearchParams(location.search)
const visible = ref(true)
window.unmountMfa = () => { visible.value = false }
window.mountMfa = () => { visible.value = true }
createApp({ render: () => visible.value ? h(MfaStep, {
  mfaData: { enabled: true, ready: !params.has('bind'), uid: '101', origin: 0, challenge: 'synthetic-challenge' },
  isLogin: !params.has('bind'),
  onSuccess: () => events.success++, onRefreshBind: () => events.bindings++
}) : null }).use(ElementPlus).component('Icon', { render: () => null }).mount('#app')
