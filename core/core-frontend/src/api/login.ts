import request from '@/config/axios'

export const loginApi = data => request.post({ url: '/login/localLogin', data })
