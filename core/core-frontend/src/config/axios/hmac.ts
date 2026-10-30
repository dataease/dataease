import { securityConfig as se } from '@/views/tools/HmacTool'
import { HmacInitializationError } from './hmacTransport'
import { AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus-secondary'
import { getHmacMessage } from './hmacMessage'

const hmac_white_list = ['/xpackModel']
export const securityConfig = async (config: InternalAxiosRequestConfig, requestPath: string) => {
  if (hmac_white_list.some(item => requestPath.includes(item))) {
    return
  }
  try {
    return await se(config, requestPath)
  } catch (error) {
    const reason = error instanceof HmacInitializationError ? error.reason : 'initialization_failed'
    const message = await getHmacMessage(reason)
    ElMessage.error(message)
    throw new AxiosError(message, 'ERR_HMAC_INITIALIZATION', config)
  }
}
