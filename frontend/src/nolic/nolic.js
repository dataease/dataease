import Vue from 'vue'
import Nolic from './Nolic.vue'
import router from './nolic-router'
import store from '../store'
import '@/styles/index.scss' // global css
import i18n from '../lang' // internationalization
import ElementUI from 'element-ui'
import '@/components/canvas/custom-component' // 注册自定义组件
import widgets from '@/components/widget'
import * as echarts from 'echarts'
import UmyUi from 'umy-ui'

Vue.use(UmyUi)
Vue.prototype.$echarts = echarts
Vue.config.productionTip = false
Vue.use(widgets)
Vue.use(ElementUI, {

  i18n: (key, value) => i18n.t(key, value)
})
new Vue({
  router,
  store,
  i18n,
  render: h => h(Nolic)
}).$mount('#nolic')
