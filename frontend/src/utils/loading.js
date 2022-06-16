import store from '@/store'

export const tryShowLoading = identification => {
  if (!identification) return
  // const count = store.getters.loadingMap[identification]

  store.dispatch('request/addLoading', identification)
}

export const tryHideLoading = identification => {
  if (!identification) return
  const count = store.getters.loadingMap[identification]
  if (count > 0) {
    store.dispatch('request/reduceLoading', identification)
  }
}

/**
 * 说明
 * 要在view中加loading只需要两个步骤
 * 1.在vue页面文件中加 v-loading="$store.getters.loadingMap[$store.getters.currentPath]"
 *     例如：views/system/user/index.vue
 * 2.在需要loading的api中添加config {loading: true}
 *     例如：api/system/user.js中userLists方法 需要配置loading: true
 * 针对整个view页面，需要局部loading请在对应页面中自己添加
 */
