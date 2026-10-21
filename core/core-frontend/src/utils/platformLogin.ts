import CryptoJS from 'crypto-js/crypto-js'

const PREFIX = 'de-platform-login:'
const HEADER = 'X-DE-Platform-Nonce'
const TTL = 6 * 60 * 1000

interface PendingLogin {
  nonce: string
  origin: number
  expiresAt: number
}

const read = (key: string): PendingLogin | null => {
  try {
    const value = JSON.parse(localStorage.getItem(key) || 'null') as PendingLogin | null
    if (
      value &&
      typeof value.nonce === 'string' &&
      /^[A-Za-z0-9_-]{43}$/.test(value.nonce) &&
      value.expiresAt > Date.now()
    ) {
      return value
    }
  } catch {
    // Malformed or expired local state cannot authenticate a login.
  }
  localStorage.removeItem(key)
  return null
}

const nonceKey = (nonce: string) => PREFIX + CryptoJS.SHA256(nonce).toString()

export const beginPlatformLogin = (origin: number): Record<string, string> => {
  Object.keys(localStorage)
    .filter(key => key.startsWith(PREFIX))
    .forEach(read)
  const bytes = crypto.getRandomValues(new Uint8Array(32))
  const nonce = btoa(String.fromCharCode(...bytes))
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=+$/, '')
  // A separate key per exchange prevents concurrent tabs from overwriting one another.
  localStorage.setItem(
    nonceKey(nonce),
    JSON.stringify({ nonce, origin, expiresAt: Date.now() + TTL })
  )
  return { [HEADER]: nonce }
}

export const clearPlatformLogin = (headers: Record<string, string>) => {
  if (headers[HEADER]) localStorage.removeItem(nonceKey(headers[HEADER]))
}

export const platformLoginHeaders = (token: string, origin: number): Record<string, string> => {
  try {
    const payload = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
    const bytes = Uint8Array.from(atob(payload), char => char.charCodeAt(0))
    const claim = JSON.parse(new TextDecoder().decode(bytes))
    // Decoding here only selects local state. The server authenticates every JWT claim.
    if (typeof claim.nonce !== 'string' || !/^[a-f0-9]{64}$/.test(claim.nonce)) return {}
    const pending = read(PREFIX + claim.nonce)
    if (pending?.origin === origin) return { [HEADER]: pending.nonce }
  } catch {
    // Let the server reject the invalid or missing exchange binding.
  }
  return {}
}
