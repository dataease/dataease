export interface ShareInfo {
  id: string
  exp?: number
  uuid: string
  pwd?: string
  autoPwd: boolean
  ticketRequire?: boolean
}

export const SHARE_BASE = '/de-link/'

export const shortcuts = [
  {
    text: '一小时',
    value: () => {
      const date = new Date()
      date.setTime(date.getTime() + 3600 * 1000)
      return date
    }
  },
  {
    text: '一天',
    value: () => {
      const date = new Date()
      date.setTime(date.getTime() + 3600 * 1000 * 24)
      return date
    }
  },
  {
    text: '一周',
    value: () => {
      const date = new Date()
      date.setTime(date.getTime() + 7 * 3600 * 1000 * 24)
      return date
    }
  }
]
