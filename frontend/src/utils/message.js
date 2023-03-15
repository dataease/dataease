import { MessageBox, Message } from 'element-ui'
import i18n from '@/lang'
export const $alert = (message, callback, options) => {
  const title = i18n.t('commons.message_box.alert')
  MessageBox.alert(message, title, options).then(() => {
    callback()
  })
}

export const $confirm = (message, callback, options = {}) => {
  const defaultOptions = {
    confirmButtonText: i18n.t('commons.message_box.ok'),
    cancelButtonText: i18n.t('commons.message_box.cancel'),
    type: 'warning',
    ...options
  }
  const title = i18n.t('commons.message_box.confirm')
  MessageBox.confirm(message, title, defaultOptions).then(() => {
    callback()
  }).catch(() => {})
}

export const $success = (message, duration) => {
  Message.success({
    message: message,
    type: 'success',
    showClose: true,
    duration: duration || 1500
  })
}

export const $info = (message, duration) => {
  Message.info({
    message: message,
    type: 'info',
    showClose: true,
    duration: duration || 3000
  })
}

export const $warning = (message, duration) => {
  Message.warning({
    message: message,
    type: 'warning',
    showClose: true,
    duration: duration || 5000
  })
}

export const $error = (message, duration, useHtml) => {
  Message.error({
    message: message,
    type: 'error',
    showClose: true,
    duration: duration || 10000,
    dangerouslyUseHTMLString: useHtml
  })
}

export default {
  install(Vue) {
    // 使用$$前缀，避免与Element UI的冲突
    Vue.prototype.$$confirm = $confirm
    Vue.prototype.$$alert = $alert

    Vue.prototype.$success = $success
    Vue.prototype.$info = $info
    Vue.prototype.$warning = $warning
    Vue.prototype.$error = $error
  }
}
