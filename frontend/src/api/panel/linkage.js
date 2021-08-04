import request from '@/utils/request'

export function getViewLinkageGather(requestInfo) {
  return request({
    url: '/linkage/getViewLinkageGather',
    method: 'post',
    data: requestInfo,
    loading: true
  })
}

