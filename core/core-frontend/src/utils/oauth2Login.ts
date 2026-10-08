import request from '@/config/axios'
import { platformTokenApi } from '@/api/login'
import { beginPlatformLogin, clearPlatformLogin } from '@/utils/platformLogin'

const PREFIX = 'de-oauth2-authorization:'
const STATE = /^fit2clouddeoauth2[A-Za-z0-9_-]{43}$/
const TTL = 5 * 60 * 1000

interface PendingAuthorization {
  headers: Record<string, string>
  codeKey: string
  expiresAt: number
}

interface Authorization {
  state: string
  authEndpoint: string
  clientId: string
  scope: string
  redirectUri: string
  codeKey?: string
  codeChallenge?: string
  codeChallengeMethod?: string
}

const pending = (state: string): PendingAuthorization | null => {
  if (!STATE.test(state)) return null
  try {
    const value = JSON.parse(localStorage.getItem(PREFIX + state) || 'null')
    if (
      value?.expiresAt > Date.now() &&
      typeof value.codeKey === 'string' &&
      /^[A-Za-z0-9_-]{43}$/.test(value.headers?.['X-DE-Platform-Nonce'])
    )
      return value
  } catch {
    // Invalid local transactions must never start an exchange.
  }
  localStorage.removeItem(PREFIX + state)
  return null
}

export const prepareOauth2Login = async (): Promise<string> => {
  Object.keys(localStorage)
    .filter(key => key.startsWith(PREFIX))
    .forEach(key => {
      pending(key.slice(PREFIX.length))
    })
  const headers = beginPlatformLogin(9)
  try {
    const res = await request.get({ url: '/oauth2/auth', headers })
    const data = res.data as Authorization
    if (!data || !STATE.test(data.state)) throw new Error('Invalid OAuth2 authorization')
    const url = new URL(data.authEndpoint)
    if (!['https:', 'http:'].includes(url.protocol)) throw new Error('Invalid OAuth2 endpoint')
    url.searchParams.set('response_type', 'code')
    url.searchParams.set('client_id', data.clientId)
    url.searchParams.set('scope', data.scope)
    url.searchParams.set('state', data.state)
    url.searchParams.set('redirect_uri', data.redirectUri)
    if (data.codeChallenge) {
      if (data.codeChallengeMethod !== 'S256') throw new Error('Invalid PKCE method')
      url.searchParams.set('code_challenge', data.codeChallenge)
      url.searchParams.set('code_challenge_method', 'S256')
    } else {
      url.searchParams.delete('code_challenge')
      url.searchParams.delete('code_challenge_method')
    }
    localStorage.setItem(
      PREFIX + data.state,
      JSON.stringify({
        headers,
        codeKey: data.codeKey && data.codeKey !== 'null' ? data.codeKey : 'code',
        expiresAt: Date.now() + TTL
      } satisfies PendingAuthorization)
    )
    return url.toString()
  } catch (error) {
    clearPlatformLogin(headers)
    throw error
  }
}

export const oauth2CodeKey = (state: string): string => pending(state)?.codeKey || 'code'

export const exchangeOauth2Token = async (code: string, state: string) => {
  const transaction = pending(state)
  if (!code || !transaction) throw new Error('Invalid or expired OAuth2 authorization')
  // The same nonce was sent before visiting the IdP. Never create one on callback.
  localStorage.removeItem(PREFIX + state)
  return platformTokenApi(9, { code, state }, transaction.headers)
}
