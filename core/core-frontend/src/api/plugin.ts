import request from '@/config/axios'

export const load = (key: string) => request.get({ url: `/xpackComponent/content/${key}` })

export const loadDistributed = () => request.get({ url: '/DEXPack.umd.js' })

export const xpackModelApi = () => request.get({ url: '/xpackModel' })
