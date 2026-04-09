import { importXpackTool } from '@/components/plugin/src/ImportXpackTool'

const hmac_white_list = ['/xpackModel']
export const securityConfig = async (config: any, requestPath: string) => {
  if (hmac_white_list.some(item => requestPath.includes(item))) {
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
