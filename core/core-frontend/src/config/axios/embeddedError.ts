import { useI18n } from '@/hooks/web/useI18n'

export const embeddedErrorMessage = (messageKey?: string) => {
  const keys: Record<string, string> = {
    i18n_embedded_origin_mismatch: 'common.embedded_origin_mismatch',
    i18n_embedded_token_expired: 'common.embedded_token_expired',
    i18n_embedded_token_invalid: 'common.embedded_token_invalid'
  }
  return useI18n().t(keys[messageKey || ''] || 'common.embedded_auth_failed')
}
