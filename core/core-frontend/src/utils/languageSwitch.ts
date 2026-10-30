import { useI18n } from '@/hooks/web/useI18n'
import { useLocaleStoreWithOut } from '@/store/modules/locale'
import { matchSupportedLocale } from '@/utils/locale'

export const requireSwitchLanguage = async (language: unknown): Promise<string> => {
  if (typeof language !== 'string' || !language.trim()) {
    throw new Error(useI18n().t('common.language_required'))
  }
  const options = await useLocaleStoreWithOut().getLocaleMap
  const matched = matchSupportedLocale(
    language,
    options.map(item => item.lang)
  )
  if (!matched) {
    throw new Error(useI18n().t('common.language_unsupported'))
  }
  return matched
}
