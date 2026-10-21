const PREFIX = 'de-platform-bind:'
export const BIND_NONCE_HEADER = 'X-DE-Bind-Nonce'
const STATE = /^de_bind_[A-Za-z0-9_-]{43}$/
const RETURN_PATHS = ['/', '/oidcbi/', '/casbi/']

export interface PreparedBinding {
  state: string
  origin: number
  redirectUri: string
  returnPath: string
  expiresAt: number
}

export interface PendingBinding extends PreparedBinding {
  nonce: string
}

export const bindingNonce = () => {
  const bytes = crypto.getRandomValues(new Uint8Array(32))
  return btoa(String.fromCharCode(...bytes))
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=+$/, '')
}

export const bindingReturnPath = () => {
  const path = window.location.pathname
  return RETURN_PATHS.find(item => item !== '/' && path.startsWith(item)) || '/'
}

export const saveBinding = (prepared: PreparedBinding, nonce: string) => {
  sessionStorage.setItem(PREFIX + prepared.state, JSON.stringify({ ...prepared, nonce }))
}

export const clearBindings = () => {
  Object.keys(sessionStorage)
    .filter(key => key.startsWith(PREFIX))
    .forEach(key => sessionStorage.removeItem(key))
}

export const clearBinding = (state: string) => sessionStorage.removeItem(PREFIX + state)

export const pendingBinding = (state: string | null): PendingBinding | null => {
  if (!state || !STATE.test(state)) return null
  try {
    const value = JSON.parse(
      sessionStorage.getItem(PREFIX + state) || 'null'
    ) as PendingBinding | null
    if (
      value?.state === state &&
      [4, 5, 6, 7].includes(value.origin) &&
      RETURN_PATHS.includes(value.returnPath) &&
      /^[A-Za-z0-9_-]{43}$/.test(value.nonce) &&
      value.expiresAt > Date.now()
    ) {
      return value
    }
  } catch {
    // Invalid local state must never trigger an exchange.
  }
  clearBinding(state)
  return null
}

// Recognize old callbacks only to reject them; never dispatch a provider from their contents.
export const isBindingCallback = (state: string | null) =>
  !!state &&
  (STATE.test(state) ||
    /^fit2cloud-(dingtalk|larksuite|wecom|lark)-qr_de_bind(?:_path_(oidcbi|casbi))?$/.test(state))

export const bindingCallbackPath = (pending: PendingBinding) =>
  window.location.origin + pending.returnPath
