import Vue from 'vue'
import "@/styles/index.scss"
import Fit2CloudUI from 'fit2cloud-ui';
import ElementUI from 'element-ui';
import App from './App.vue'
import i18n from "./i18n";
import router from './router'
import store from './store'
import icons from './icons'
import plugins from "./plugins";
import directives from "./directive";
import "./permission"

Vue.config.productionTip = false

Vue.use(ElementUI, {
  size: 'small',
  i18n: (key, value) => i18n.t(key, value)
});
Vue.use(Fit2CloudUI, {
  i18n: (key, value) => i18n.t(key, value)
});
Vue.use(icons);
Vue.use(plugins);
Vue.use(directives);

new Vue({
  el: '#app',
  i18n,
  router,
  store,
  render: h => h(App),
})
