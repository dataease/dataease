import Vue from 'vue'
import Link from './Link.vue'
import router from './link-router'
Vue.config.productionTip = false
new Vue({
  router,
  render: h => h(Link)
}).$mount('#link')
