import { createApp, h } from 'vue'
import { setupStore } from '@/store'
import { setupI18n, i18n } from '@/plugins/vue-i18n'
import { useUserStoreWithOut } from '@/store/modules/user'
import { useLocaleStoreWithOut } from '@/store/modules/locale'
import { useCache } from '@/hooks/web/useCache'
import { getLocale, getBrowserLocale } from '@/utils/utils'
import { resolveLocale } from '@/utils/locale'
import { switchLangApi } from '@/api/user'
import LangSelector from '@/layout/components/LangSelector.vue'

const scenario = JSON.parse(sessionStorage.getItem('scenario') || '{}')
const visits = Number(sessionStorage.getItem('visits') || 0) + 1
sessionStorage.setItem('visits', String(visits))
if (visits > 4) throw new Error('Language reload loop')
const { wsCache } = useCache()
if (visits === 1 && scenario.cache !== undefined)
  wsCache.set('user.language', scenario.cache)
if (scenario.embedded) window.DataEaseBi = {}
const app = createApp({ render: () => h(LangSelector) })
setupStore(app)
await setupI18n(app)
const initial = useLocaleStoreWithOut().getCurrentLocale.lang
await useUserStoreWithOut().setUser()
app.component('ElIcon', { render: () => null })
app.mount('#app')
window.languageResult = {
  initial,
  language: useUserStoreWithOut().getLanguage,
  cache: wsCache.get('user.language'),
  locale: useLocaleStoreWithOut().getCurrentLocale.lang,
  i18n: i18n.global.locale.value,
  requestLocale: getLocale(),
  html: document.documentElement.lang,
  browser: getBrowserLocale(),
  visits
}
window.resolveLocale = resolveLocale

window.attemptSwitch = async (language, mode = 'api') => {
  try {
    if (mode === 'api') await switchLangApi({ lang: language })
    else await useUserStoreWithOut().setLanguage(language)
    return { ok: true }
  } catch (error) {
    return { ok: false, message: error.message }
  }
}
window.currentLanguage = () => ({
  language: useUserStoreWithOut().getLanguage,
  cache: wsCache.get('user.language'),
  locale: useLocaleStoreWithOut().getCurrentLocale.lang
})
