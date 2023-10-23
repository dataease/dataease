import request from '@/config/axios'

export const load = (key: string) => request.get({ url: `/xpackComponent/content/${key}` })
