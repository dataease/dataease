import request from '@/config/axios'

export const findBaseParams = async () => request.get({ url: '/aiBase/findTargetUrl' })
