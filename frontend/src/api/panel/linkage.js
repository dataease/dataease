import request from '@/utils/request'

export function getViewLinkageGather(requestInfo) {
  return request({
    url: '/linkage/getViewLinkageGather',
    method: 'post',
    data: requestInfo,
    loading: true
  })
}

export function saveLinkage(requestInfo) {
  return request({
    url: '/linkage/saveLinkage',
    method: 'post',
    data: requestInfo,
    loading: true
  })
}

export function getPanelAllLinkageInfo(panelId) {
  return request({
    url: '/linkage/getPanelAllLinkageInfo/' + panelId,
    method: 'get',
    loading: false
  })
}
