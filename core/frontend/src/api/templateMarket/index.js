import request from '@/utils/request'

export function searchMarket(data) {
  return request({
    url: '/template/market/search',
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
