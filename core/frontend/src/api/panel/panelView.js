import request from '@/utils/request'

export function detailList(panelId) {
  return request({
    url: '/panel/view/detailList/' + panelId,
    method: 'get',
    loading: false
  })
}

export function getComponentInfo(panelId) {
  return request({
    url: '/panel/view/getComponentInfo/' + panelId,
    method: 'get',
    loading: false
  })
}
