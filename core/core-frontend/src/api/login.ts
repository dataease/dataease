import request from '@/config/axios'

export const loginApi = data => request.post({ url: '/login/localLogin', data })

export const queryDekey = () => request.get({ url: 'dekey' })

export const querySymmetricKey = () => request.get({ url: 'symmetricKey' })

export const modelApi = () => request.get({ url: 'model' })

export const platformLoginApi = origin => request.post({ url: '/login/platformLogin/' + origin })

export const logoutApi = () => request.get({ url: '/logout' })

export const refreshApi = (time?: any) => request.get({ url: '/login/refresh', params: { time } })

export const uiLoadApi = () => request.get({ url: '/sysParameter/ui' })

export const loginCategoryApi = () => request.get({ url: '/sysParameter/defaultLogin' })
