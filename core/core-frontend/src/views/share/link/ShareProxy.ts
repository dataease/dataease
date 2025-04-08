import request from '@/config/axios'
import { useCache } from '@/hooks/web/useCache'
import { isInIframe } from '@/utils/utils'
const { wsCache } = useCache()

export interface TicketValidVO {
  ticketValid: boolean
  ticketExp: boolean
  args: string
}
export interface ProxyInfo {
  resourceId: string
  uid: string
  exp?: boolean
  pwdValid?: boolean
  type: string
  inIframeError: boolean
  shareDisable: boolean
  peRequireValid: boolean
  ticketValidVO: TicketValidVO
  pwd?: string
  uuid: string
}
class ShareProxy {
  uuid: string
  pwd?: string
  constructor() {
    this.uuid = ''
  }
  getTicket() {
    const curLocation = window.location.href
    const pmIndex = curLocation.lastIndexOf('?')
    if (pmIndex == -1) {
      return null
    }
    const searchText = curLocation.substring(pmIndex + 1)
    const regex = /([^&=]+)=([^&]*)/g
    let m
    while ((m = regex.exec(searchText)) !== null) {
      const key = decodeURIComponent(m[1])
      if (key === 'ticket') {
        return decodeURIComponent(m[2])
      }
    }
    return null
  }
  setUuid() {
    const curLocation = window.location.href
    const pmIndex = curLocation.lastIndexOf('?')
    const uuidObj = curLocation.substring(
      curLocation.lastIndexOf('de-link/') + 8,
      pmIndex > 0 ? pmIndex : curLocation.length
    )

    if (uuidObj?.includes(',')) {
      const index = uuidObj.indexOf(',')
      this.uuid = uuidObj.substring(0, index)
      if (uuidObj.length > index + 1) {
        this.pwd = uuidObj.substring(index + 1)
      }
    } else {
      this.uuid = uuidObj
    }
  }
  async loadProxy() {
    this.setUuid()
    if (!this.uuid) {
      return null
    }
    const uuid = this.uuid
    const url = '/share/proxyInfo'
    const inIframe = isInIframe()
    const ticket = this.getTicket()
    const param = { uuid, ciphertext: null, inIframe, ticket }
    const ciphertext = wsCache.get(`link-${uuid}`)
    if (ciphertext) {
      param['ciphertext'] = ciphertext
    }
    const res = await request.post({ url, data: param })
    const proxyInfo: ProxyInfo = res.data as ProxyInfo
    if (proxyInfo) {
      proxyInfo.uuid = uuid
      if (this.pwd) {
        proxyInfo.pwd = this.pwd
      }
    }
    return proxyInfo
  }
}

export const shareProxy = new ShareProxy()
