import { isAxiosError, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'

export interface ErrorBody {
  code?: string | number
  msg?: string
  category?: string
  messageKey?: string
  args?: unknown[]
  requestId?: string
  fieldErrors?: { field: string; messageKey?: string; message: string }[]
  retryable?: boolean
  data?: unknown
}

/** Business code and Axios transport code deliberately have separate fields. */
export class DataEaseError extends Error {
  readonly code?: string | number
  readonly transportCode?: string
  readonly httpStatus?: number
  readonly requestId?: string
  readonly fieldErrors?: ErrorBody['fieldErrors']
  readonly category?: string
  readonly messageKey?: string
  readonly retryable: boolean
  readonly response?: AxiosResponse<unknown>
  readonly config?: InternalAxiosRequestConfig
  readonly cause?: unknown
  presented = false

  constructor(response?: AxiosResponse<unknown>, cause?: unknown) {
    const body = errorBody(response?.data)
    super(
      body.msg ||
        (cause instanceof Error
          ? cause.message
          : typeof cause === 'string'
          ? cause
          : 'Request failed')
    )
    this.name = 'DataEaseError'
    this.response = response
    this.cause = cause
    this.config = response?.config || (isAxiosError(cause) ? cause.config : undefined)
    this.transportCode = isAxiosError(cause) ? cause.code : undefined
    this.presented = this.transportCode === 'ERR_HMAC_INITIALIZATION'
    this.code = body.code ?? this.transportCode
    this.httpStatus = response?.status
    const headerRequestId =
      response?.headers['x-de-request-id'] || response?.headers['X-DE-Request-ID']
    this.requestId =
      body.requestId || (typeof headerRequestId === 'string' ? headerRequestId : undefined)
    this.fieldErrors = body.fieldErrors
    this.category = body.category
    this.messageKey = body.messageKey
    this.retryable = body.retryable === true
  }

  toString() {
    return this.message
  }
}

export const errorBody = (value: unknown): ErrorBody =>
  value !== null && typeof value === 'object' ? (value as ErrorBody) : {}

export const toDataEaseError = (cause: unknown): DataEaseError =>
  cause instanceof DataEaseError
    ? cause
    : new DataEaseError(isAxiosError(cause) ? cause.response : undefined, cause)

/** Retains compatibility for consumers which also receive Axios errors outside our service. */
export const isRequestError = (
  error: unknown
): error is DataEaseError | import('axios').AxiosError =>
  error instanceof DataEaseError || isAxiosError(error)

export const getErrorMessage = (error: unknown, fallback = ''): string =>
  typeof error === 'string' ? error : error instanceof Error ? error.message : fallback

// Capability belongs to this response, not a global switch shared by concurrent requests.
const contractResponses = new WeakSet<object>()
export const markErrorContractResponse = (response: unknown) => {
  if (response !== null && typeof response === 'object') contractResponses.add(response)
}
export const hasErrorContract = (response: unknown): boolean =>
  response !== null && typeof response === 'object' && contractResponses.has(response)
