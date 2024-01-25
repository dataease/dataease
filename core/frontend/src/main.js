import Vue from 'vue'
import Cookies from 'js-cookie'
import '@/styles/index.scss' // global css
import ElementUI from 'element-ui'
import Vuetify from 'vuetify'
import Fit2CloudUI from 'fit2cloud-ui'

import i18n from './lang' // internationalization
import App from './App'
import store from './store'
import router from './router'
import message from './utils/message'
import '@/icons' // icon
import '@/permission' // permission control
import api from '@/api/index.js'
import filter from '@/filter/filter'
import directives from './directive'
import VueClipboard from 'vue-clipboard2'
import widgets from '@/components/widget'
import Treeselect from '@riophae/vue-treeselect'
import '@riophae/vue-treeselect/dist/vue-treeselect.css'
import './utils/dialog'
import DeComplexInput from '@/components/business/conditionTable/DeComplexInput'
import DeComplexSelect from '@/components/business/conditionTable/DeComplexSelect'
import DeViewSelect from '@/components/deViewSelect'
import RemarkEditor from '@/views/chart/components/componentStyle/dialog/RemarkEditor'
import TitleRemark from '@/views/chart/view/TitleRemark'
import '@/components/canvas/customComponent' // 注册自定义组件
import deBtn from '@/components/deCustomCm/DeBtn.vue'

import '@/utils/DateUtil'
import draggable from 'vuedraggable'
import deWebsocket from '@/websocket'
import { GaodeMap } from '@antv/l7-maps'
import { Mix } from '@antv/g2plot'
import * as echarts from 'echarts'
import { clear } from 'size-sensor'
import UmyUi from 'umy-ui'
// 全屏插件
import fullscreen from 'vue-fullscreen'
import VueFriendlyIframe from 'vue-friendly-iframe'
import vueToPdf from 'vue-to-pdf'
import VueVideoPlayer from 'vue-video-player'
import 'video.js/dist/video-js.css'
import '@antv/s2/dist/style.min.css'
// 控制标签宽高成比例的指令
import proportion from 'vue-proportion-directive'

import xss from 'xss'
// 定义全局XSS解决方法
Object.defineProperty(Vue.prototype, '$xss', {
  value: xss
})

Vue.config.productionTip = false
Vue.use(VueClipboard)
Vue.use(widgets)
Vue.component('Draggable', draggable)
Vue.prototype.$api = api

Vue.prototype.$echarts = echarts
Vue.prototype.$gaodeMap = GaodeMap
Vue.prototype.$Mix = Mix
Vue.prototype.$G2SizeSensorClear = clear

Vue.use(UmyUi)

Vue.use(fullscreen)

Vue.use(VueFriendlyIframe)
Vue.use(Vuetify)
// import TEditor from '@/components/Tinymce/index.vue'
// Vue.component('TEditor', TEditor)

/**
 * If you don't want to use mock-server
 * you want to use MockJs for mock api
 * you can execute: mockXHR()
 *
 * Currently MockJs will be used in the production environment,
 * please remove it before going online ! ! !
 */
if (process.env.NODE_ENV === 'production') {
//   const { mockXHR } = require('../mock')
//   mockXHR()
}

// set ElementUI lang to EN
// Vue.use(ElementUI, { locale })
// 如果想要中文版 element-ui，按如下方式声明
ElementUI.Dialog.props.closeOnClickModal.default = false
ElementUI.Dialog.props.closeOnPressEscape.default = false
Vue.use(ElementUI, {
  size: Cookies.get('size') || 'medium', // set element-ui default size
  i18n: (key, value) => i18n.t(key, value)
})
Vue.use(Fit2CloudUI, {
  i18n: (key, value) => i18n.t(key, value)
})
// Vue.use(VueAxios, axios)
Vue.use(filter)
Vue.use(directives)
Vue.use(message)
Vue.component('Treeselect', Treeselect)
Vue.component('DeComplexInput', DeComplexInput)
Vue.component('DeComplexSelect', DeComplexSelect)
Vue.component('DeViewSelect', DeViewSelect)
Vue.component('RemarkEditor', RemarkEditor)
Vue.component('TitleRemark', TitleRemark)
Vue.component('DeBtn', deBtn)

Vue.config.productionTip = false

Vue.use(vueToPdf)

Vue.use(VueVideoPlayer)

Vue.use(proportion)

Vue.prototype.hasDataPermission = function(pTarget, pSource) {
  if (this.$store.state.user.user.isAdmin || pSource === 'ignore') {
    return true
  }
  if (pSource && pTarget) {
    return pSource.indexOf(pTarget) > -1
  }
  return false
}

Vue.prototype.checkPermission = function(pers) {
  const permissions = store.getters.permissions
  const hasPermission = pers.every(needP => {
    const result = permissions.includes(needP)
    return result
  })
  return hasPermission
}
Vue.use(deWebsocket)

Vue.prototype.$currentHttpRequestList = new Map()
Vue.prototype.$cancelRequest = function(cancelkey) {
  if (cancelkey) {
    if (cancelkey.indexOf('/**') > -1) {
      Vue.prototype.$currentHttpRequestList.forEach((item, key) => {
        key.indexOf(cancelkey.split('/**')[0]) > -1 && item('Operation canceled by the user.')
      })
    } else {
      Vue.prototype.$currentHttpRequestList.get(cancelkey) && Vue.prototype.$currentHttpRequestList.get(cancelkey)('Operation canceled by the user.')
    }
  }
}

new Vue({

  router,
  store,
  i18n,
  render: h => h(App)
}).$mount('#app')
