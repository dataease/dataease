import request from '@/config/axios'

export function searchMarket() {
  return request.get({
    url: '/templateMarket/search'
  })
}

export function getCategories() {
  return request.get({
    url: '/templateMarket/categories'
  })
}
