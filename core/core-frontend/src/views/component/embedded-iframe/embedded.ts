import request from '@/config/axios'

export const initApi = (token: string, origin: string) => request.post({url: '/embedded/initIframe', data: { token, origin }})