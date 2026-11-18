import axios from 'axios'
import { config } from './config'

export interface HmacInfo {
  version: number
  enabled: boolean
}

export interface HmacEnvelope extends HmacInfo {
  requestId: string
  encryptedKey?: string
  iv?: string
  ciphertext?: string
}

export class HmacInitializationError extends Error {
  constructor(readonly reason: 'initialization_failed' | 'random_source_required') {
    super(reason)
  }
}

// Bootstrap requests deliberately bypass the service's signing/refresh interceptors.
// Never interpret a network error or a missing enterprise endpoint as HMAC being disabled.
export const requestHmac = async <T>(baseUrl: string, request?: object): Promise<T> => {
  if (!request) {
    // The core capability endpoint exists even when XPack has no controller mapping.
    const model = await axios.get(`${baseUrl}/xpackModel`, {
      timeout: config.request_timeout,
      headers: { Accept: 'application/json', 'Cache-Control': 'no-store' }
    })
    if (
      model.data?.code !== 0 ||
      (model.data.data !== null && typeof model.data.data !== 'boolean')
    ) {
      throw new HmacInitializationError('initialization_failed')
    }
    // The core implementation returns null when no XPack replacement is loaded.
    if (model.data.data !== true) return { version: 2, enabled: false } as T
  }
  const response = await axios.request({
    url: `${baseUrl}/perSetting/hmac/${request ? 'exchange' : 'info'}`,
    method: request ? 'post' : 'get',
    data: request,
    timeout: config.request_timeout,
    headers: { Accept: 'application/json', 'Cache-Control': 'no-store' }
  })
  const body = response.data
  if (body?.code !== 0 || !body.data || typeof body.data !== 'object') {
    throw new HmacInitializationError('initialization_failed')
  }
  return body.data as T
}
