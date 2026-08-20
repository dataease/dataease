import { securityConfig as se } from '@/views/tools/HmacTool'

const hmac_white_list = ['/xpackModel']
export const securityConfig = async (config: any, requestPath: string) => {
  if (hmac_white_list.some(item => requestPath.includes(item))) {
    return
  }
  try {
    if (!se) {
      return
    }
    return se(config, requestPath)
  } catch (error) {
    console.error('Failed to load securityConfig method:', error)
    return
  }
}
