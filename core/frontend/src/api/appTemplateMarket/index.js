import request from '@/utils/request'

export function searchAppTemplate(data) {
  return request({
    url: '/appTemplate/find',
    method: 'post',
    loading: true,
    hideMsg: true,
    data
  })
}

export function getCategories() {
  return request({
    url: '/template/market/categories',
    method: 'get',
    hideMsg: true,
    loading: true
  })
}
