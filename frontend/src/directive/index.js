import permission from '@/directive/Permission'

export default {
  install(Vue) {
    Vue.directive('permission', permission)
  }
}

