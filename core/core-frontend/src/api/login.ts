import request from '@/config/axios'

export const loginApi = data => request.post({ url: '/login/localLogin', data })

export const queryDekey = () => request.get({ url: 'dekey' })

export const modelApi = () => request.get({ url: 'model' })
