// These application boundaries are replaced by bridge.js in the component fixture.
export const useUserStoreWithOut: () => {
  uid: string | null
  oid: string | null
  proxyInfo: { proxyOid: string | null; proxy?: boolean }
}
export const useI18n: () => { t: (key: string, args?: Record<string, unknown>) => string }
declare const request: {
  get: (option: unknown) => Promise<any>
  post: (option: unknown) => Promise<any>
  put: (option: unknown) => Promise<any>
  delete: (option: unknown) => Promise<any>
}
export default request
