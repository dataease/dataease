import { useI18n } from '@/hooks/web/useI18n'
import { getLocale } from '@/utils/utils'
import type { HmacFailure } from './hmacError'
import type { HmacInitializationError } from './hmacTransport'

export const getHmacMessage = async (reason: HmacFailure | HmacInitializationError['reason']) => {
  const key = `setting_hmac.${reason}`
  const message = useI18n().t(key)
  if (message !== key) return message
  // i18nOptions itself is an API call made before setupI18n finishes.
  // Load only the needed built-in locale instead of showing a translation key at startup.
  const language = getLocale()
  const locale = language === 'en' || language === 'tw' ? language : 'zh-CN'
  const messages = await import(`../../locales/${locale}.ts`)
  return messages.default.setting_hmac[reason] as string
}
