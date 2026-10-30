import { AxiosHeaders, type AxiosError } from 'axios'

export type HmacFailure = 'authentication_failed' | 'clock_skew_failed' | 'signature_missing'

/** Classify gateway failures without confusing them with login or resource authorization. */
export const getHmacFailure = (error: AxiosError<unknown>): HmacFailure | null => {
  const response = error.response
  if (response?.status !== 401) return null
  const headers = AxiosHeaders.from(response.headers)
  if (headers.has('DE-GATEWAY-FLAG') || headers.has('DE-FORBIDDEN-FLAG')) return null
  const body = response.data
  const data = body && typeof body === 'object' ? (body as Record<string, unknown>) : null
  // The bundled HMAC plugin can reject unsigned requests when its switch differs from the backend.
  // These exact messages identify the signature scheme; an outgoing signature cannot be required here.
  if (
    data?.message === "client request can't be validated: missing Authorization header" ||
    data?.message ===
      "client request can't be validated: Authorization header does not start with 'Signature'"
  )
    return 'signature_missing'
  if (data?.code === 'HMAC_CLOCK_SKEW') return 'clock_skew_failed'
  if (data?.code === 'HMAC_AUTH_FAILED') return 'authentication_failed'
  // Upstream APISIX identifies the authentication scheme in its challenge header.
  const challenge = headers.get('WWW-Authenticate')
  if (typeof challenge === 'string' && /^hmac(?:\s|$)/i.test(challenge)) {
    return data?.message === "client request can't be validated: Clock skew exceeded"
      ? 'clock_skew_failed'
      : 'authentication_failed'
  }
  // The bundled older plugin has no challenge header and returns this exact message.
  // Require a signed outgoing request as additional evidence, and never match arbitrary 401 text.
  const authorization =
    error.config?.headers && AxiosHeaders.from(error.config.headers).get('Authorization')
  if (typeof authorization !== 'string' || !authorization.startsWith('Signature ')) return null
  const message = data?.message
  if (typeof message !== 'string') return null
  if (message === "client request can't be validated: Clock skew exceeded")
    return 'clock_skew_failed'
  if (
    message === "client request can't be validated" ||
    message === "client request can't be validated: Invalid signature"
  ) {
    return 'authentication_failed'
  }
  return null
}
