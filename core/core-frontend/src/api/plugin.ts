import request from '@/config/axios'

export const load = (key: string) => request.get({ url: `/xpackComponent/content/${key}` })

export const loadPluginApi = (key: string) =>
  request.get({ url: `/xpackComponent/contentPlugin/${key}` })

export const loadDistributed = () => request.get({ url: '/DEXPack.umd.js' })

export const xpackModelApi = () => request.get({ url: '/xpackModel' })
