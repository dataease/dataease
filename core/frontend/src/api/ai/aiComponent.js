import request from '@/utils/request'

export async function findBaseParams() {
  return request({
    url: '/aiBase/findTargetUrl',
    method: 'get',
    loading: false
  })
}

