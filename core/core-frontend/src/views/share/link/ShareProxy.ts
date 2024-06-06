import request from '@/config/axios'
import { useCache } from '@/hooks/web/useCache'
import { isInIframe } from '@/utils/utils'
const { wsCache } = useCache()
export interface ProxyInfo {
  resourceId: string
  uid: string
  exp?: boolean
  pwdValid?: boolean
  type: string
  inIframeError: boolean
}
class ShareProxy {
  uuid: string
  constructor() {
    this.uuid = ''
  }
  setUuid() {
    const curLocation = window.location.href
    const uuidObj = curLocation.substring(
      curLocation.lastIndexOf('de-link/') + 8,
      curLocation.lastIndexOf('?') > 0 ? curLocation.lastIndexOf('?') : curLocation.length
    )
    this.uuid = uuidObj
  }
  async loadProxy() {
    this.setUuid()
    if (!this.uuid) {
      return null
    }
    const uuid = this.uuid
    const url = '/share/proxyInfo'
    const inIframe = isInIframe()
    const param = { uuid, ciphertext: null, inIframe }
    const ciphertext = wsCache.get(`link-${uuid}`)
    if (ciphertext) {
      param['ciphertext'] = ciphertext
    }
    const res = await request.post({ url, data: param })
    const proxyInfo: ProxyInfo = res.data as ProxyInfo
    return proxyInfo
  }
}

export const shareProxy = new ShareProxy()
