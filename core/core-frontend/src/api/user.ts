import request from '@/config/axios'

export const mountedOrg = () => request.get({ url: '/org/mounted' })

export const switchOrg = (id: number) => request.post({ url: '/user/switch/' + id })

export const userInfo = () => request.get({ url: '/user/info' })
