import type { AxiosRequestConfig } from 'axios'
import { createHmacRecipient, type HmacSigner } from './hmacCrypto'
import {
  requestHmac,
  HmacInitializationError,
  type HmacInfo,
  type HmacEnvelope
} from '@/config/axios/hmacTransport'

const VERSION = 2
// Signer closures stay within this module; no secret is placed on window or in browser storage.
const states = new Map<string, Promise<HmacSigner | null>>()

const apiBase = (baseUrl: string) =>
  new URL(baseUrl || './', document.baseURI).href.replace(/\/$/, '')

export const invalidateHmac = () => {
  states.clear()
}

const initialize = async (baseUrl: string): Promise<HmacSigner | null> => {
  const status = await requestHmac<HmacInfo>(baseUrl)
  if (status.version !== VERSION || typeof status.enabled !== 'boolean') {
    throw new HmacInitializationError('initialization_failed')
  }
  if (!status.enabled) return null
  const recipient = await createHmacRecipient()
  const { publicKey, requestId } = recipient
  const envelope = await requestHmac<HmacEnvelope>(baseUrl, {
    version: VERSION,
    requestId,
    publicKey
  })
  if (envelope.version !== VERSION || envelope.requestId !== requestId || !envelope.enabled) {
    // The setting changed during initialization: retry on the next request, never send unsigned.
    throw new HmacInitializationError('initialization_failed')
  }
  if (!envelope.encryptedKey || !envelope.iv || !envelope.ciphertext) {
    throw new HmacInitializationError('initialization_failed')
  }
  return recipient.open(envelope)
}

const loadKey = async (baseUrl: string): Promise<HmacSigner | null> => {
  const base = apiBase(baseUrl)
  let pending = states.get(base)
  if (!pending) {
    pending = initialize(base)
    states.set(base, pending)
  }
  try {
    const key = await pending
    // A configuration save invalidates even an initialization that was already in flight.
    return states.get(base) === pending ? key : loadKey(base)
  } catch (error) {
    if (states.get(base) === pending) states.delete(base)
    throw error
  }
}

export const securityConfig = async (config: AxiosRequestConfig, uri: string) => {
  const secretKey = await loadKey(config.baseURL || '')
  if (!secretKey) return
  const keyId = 'dataease-key'
  const gmtTime = new Date().toUTCString()
  const requestMethod = (config.method || 'get').toUpperCase()
  let requestPath = uri
  if (/^https?:\/\//.test(requestPath)) {
    const target = new URL(requestPath)
    requestPath = target.pathname + target.search
  }
  if (requestPath.startsWith('/api')) {
    requestPath = '/de2api' + requestPath.substring(4)
  }
  if (requestPath.startsWith('./de2api')) {
    requestPath = location.pathname.replace('mobile.html', '') + requestPath.substring(2)
  }
  if (requestPath.includes('/casbi')) {
    requestPath = requestPath.replace('/casbi', '')
  }
  if (requestPath.includes('/oidcbi')) {
    requestPath = requestPath.replace('/oidcbi', '')
  }
  // Generate the date after asynchronous initialization, so slow bootstrap cannot age the signature.
  const signingString = `${keyId}\n${requestMethod} ${requestPath}\nX-Date: ${gmtTime}\n`
  const signature = await secretKey(signingString)
  config.headers = {
    ...config.headers,
    'X-Date': gmtTime,
    Authorization: `Signature keyId="${keyId}",algorithm="hmac-sha256",headers="@request-target X-Date",signature="${signature}"`
  }
}
