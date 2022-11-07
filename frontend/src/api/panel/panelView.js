import request from '@/utils/request'

export function detailList(panelId) {
  console.log('detailList',panelId)
  return request({
    url: '/panel/view/detailList/' + panelId,
    method: 'get'
  })
}
