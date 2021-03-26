import Vue from 'vue'
import Link from './Link.vue'
import router from './link-router'
import '@/styles/index.scss' // global css
import i18n from '../lang' // internationalization
import ElementUI from 'element-ui'
Vue.config.productionTip = false
Vue.use(ElementUI, {

  i18n: (key, value) => i18n.t(key, value)
})
new Vue({
  router,
  render: h => h(Link)
}).$mount('#link')
