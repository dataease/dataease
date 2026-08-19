import request from '@/config/axios'

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

export const unInstallApi = id => {
  return request.post({ url: `/plugin/uninstall/${id}` })
}
