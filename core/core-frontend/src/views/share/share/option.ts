import { useI18n } from '@/hooks/web/useI18n'

export interface ShareInfo {
  visitorPermissions?: number
  allowedVisitorPermissions?: number
  id: string
  exp?: number
  uuid: string
  pwd?: string
  autoPwd: boolean
  ticketRequire?: boolean
}

export const SHARE_BASE = '/de-link/'

const { t } = useI18n()

export const shortcuts = [
  {
    text: t('commons.date.one_hour'),
    value: () => {
      const date = new Date()
      date.setTime(date.getTime() + 3600 * 1000)
      return date
    }
  },
  {
    text: t('commons.date.one_day'),
    value: () => {
      const date = new Date()
      date.setTime(date.getTime() + 3600 * 1000 * 24)
      return date
    }
  },
  {
    text: t('commons.date.one_week'),
    value: () => {
      const date = new Date()
      date.setTime(date.getTime() + 7 * 3600 * 1000 * 24)
      return date
    }
  }
]
