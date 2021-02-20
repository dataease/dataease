import Vue from 'vue';
import ElementUI from 'element-ui';
import '../assets/theme/index.css';
import icon from "../common/js/icon";
import filters from "../common/js/filter";
import ajax from "../common/js/ajax";
import App from './App.vue';
import message from "../common/js/message";
import router from "./components/common/router/router";
import YanProgress from 'yan-progress';
import './permission' // permission control
import i18n from "../i18n/i18n";
import store from "./store";
import {permission, roles, tester, xpack} from './permission'
import chart from "../common/js/chart";
import CalendarHeatmap from "../common/js/calendar-heatmap";
import '../common/css/menu-header.css';
import '../common/css/main.css';
import CKEditor from '@ckeditor/ckeditor5-vue';
import VueFab from 'vue-float-action-button'
import {left2RightDrag, bottom2TopDrag, right2LeftDrag} from "../common/js/directive";
import JsonSchemaEditor from './components/common/json-schema/schema/index';
import JSONPathPicker from 'vue-jsonpath-picker';

Vue.use(JsonSchemaEditor);
import VuePapaParse from 'vue-papa-parse'
Vue.use(VuePapaParse)

Vue.config.productionTip = false;
Vue.use(icon);
Vue.use(ElementUI, {
  i18n: (key, value) => i18n.t(key, value)
});
Vue.use(filters);
Vue.use(ajax);
Vue.use(chart);
Vue.use(CalendarHeatmap);
Vue.use(message);
Vue.use(CKEditor);
Vue.use(YanProgress);
Vue.use(VueFab);
Vue.use(JSONPathPicker);

// v-permission
Vue.directive('permission', permission);

// v-roles
Vue.directive('roles', roles);

Vue.directive('xpack', xpack);

Vue.directive('tester', tester);

//支持左右拖拽
Vue.directive('left-to-right-drag', left2RightDrag);
Vue.directive('right-to-left-drag', right2LeftDrag);
Vue.directive('bottom-to-top-drag', bottom2TopDrag);

new Vue({
  el: '#app',
  router,
  store,
  i18n,
  render: h => h(App)
});
