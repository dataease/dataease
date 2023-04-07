import request from '@/config/axios'

export const mountedOrg = (keyword: string) =>
  request.post({ url: '/org/mounted', data: { keyword } })

export const switchOrg = (id: number) => request.post({ url: '/user/switch/' + id })

export const userInfo = () => request.get({ url: '/user/info' })
