import request from '@/config/axios'

export function searchMarket(data) {
  return request.post({
    url: '/template/market/search',
    data
  })
}

export function getCategories() {
  return request.post({
    url: '/template/market/categories'
  })
}
