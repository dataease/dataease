import type { createI18n } from 'vue-i18n'

// Keep this module independent of locale loading, stores and HTTP hooks so their
// HMR updates cannot replace the instance already installed on the application.
export let i18n: ReturnType<typeof createI18n> = import.meta.hot?.data.i18n

export const setI18n = (instance: ReturnType<typeof createI18n>) => {
  i18n = instance
}

if (import.meta.hot) {
  import.meta.hot.dispose(data => {
    data.i18n = i18n
  })
}
