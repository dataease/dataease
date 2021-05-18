import request from '@/utils/request'

export function validateLic() {
  return request({
    url: '/anonymous/license/validate',
    method: 'get',
    hideMsg: true
  })
}
