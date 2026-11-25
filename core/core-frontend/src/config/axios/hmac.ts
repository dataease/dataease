import { importXpackTool } from '@/components/plugin/src/ImportXpackTool'
import type { InternalAxiosRequestConfig } from 'axios'

const hmac_white_list = ['/xpackModel']
const isStaticImageRead = (config: InternalAxiosRequestConfig, requestPath: string) => {
  if (
    config.method !== 'get' ||
    config.responseType !== 'blob' ||
    !config.url?.startsWith('/static-resource/')
  ) {
    return false
  }
  try {
    const { pathname } = new URL(requestPath, window.location.href)
    return (
      pathname.endsWith(config.url) &&
      /^\/static-resource\/[^/\\?#%]+$/.test(decodeURIComponent(config.url))
    )
  } catch {
    return false
  }
}

export const securityConfig = async (config: InternalAxiosRequestConfig, requestPath: string) => {
  // The gateway's static resource route validates application identity without HMAC.
  if (
    hmac_white_list.some(item => requestPath.includes(item)) ||
    isStaticImageRead(config, requestPath)
  ) {
    return
  }
  try {
    const method = await importXpackTool('securityConfig')
    if (!method) {
      return
    }
    return method(config, requestPath)
  } catch (error) {
    console.error('Failed to load securityConfig method:', error)
    return
  }
}
