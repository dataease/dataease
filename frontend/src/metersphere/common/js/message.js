import {Message} from 'element-ui';

export default {
  install(Vue) {
    if (!Message) {
      window.console.error('You have to install Message of ElementUI');
      return
    }

    Vue.prototype.$success = function (message) {
      Message.success({
        message: message,
        type: "success",
        showClose: true,
        duration: 1500
      })
    };

    Vue.prototype.$info = function (message, duration) {
      Message.info({
        message: message,
        type: "info",
        showClose: true,
        duration: duration || 3000
      })
    };

    Vue.prototype.$warning = function (message) {
      Message.warning({
        message: message,
        type: "warning",
        showClose: true,
        duration: 5000
      })
    };

    Vue.prototype.$error = function (message, duration) {
      Message.error({
        message: message,
        type: "error",
        showClose: true,
        duration: duration || 10000
      })
    };


  }
}
