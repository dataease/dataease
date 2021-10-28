import request from '@/utils/request'

export function detailList(panelId) {
  return request({
    url: '/panel/view/detailList/' + panelId,
    method: 'get'
  })
}
