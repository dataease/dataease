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
    // setTimeout(() => {
    //   store.dispatch('request/reduceLoading', identification)
    // }, 1000)
    store.dispatch('request/reduceLoading', identification)
  }
}

