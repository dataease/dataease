import request from '@/config/axios'

export const loadPluginApi = (key: string) =>
  request.get({ url: `/xpackComponent/contentPlugin/${key}` })

export const xpackModelApi = () => request.get({ url: '/xpackModel' })
