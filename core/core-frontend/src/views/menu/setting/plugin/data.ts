import request from '@/config/axios'
import { pluginOperationHeaders, type PluginOperationAuthorization } from './operationConfirmation'

export interface PluginItem {
  id: string
  name: string
  icon: string
  version: string
  installTime: number
  developer: string
  flag: string
}

export const loadPluginApi = () => {
  return request.get({ url: '/plugin/query' })
}

export const unInstallApi = (id, authorization: PluginOperationAuthorization) => {
  return request.post({
    url: `/plugin/uninstall/${id}`,
    headers: pluginOperationHeaders(authorization)
  })
}
