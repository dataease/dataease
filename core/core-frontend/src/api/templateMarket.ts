import request from '@/config/axios'

export function searchMarket() {
  return request.get({
    url: '/templateMarket/search'
  })
}

export function searchMarketRecommend() {
  return request.get({
    url: '/templateMarket/searchRecommend'
  })
}

export function searchMarketPreview() {
  return request.get({
    url: '/templateMarket/searchPreview'
  })
}

export function getCategories() {
  return request.get({
    url: '/templateMarket/categories'
  })
}

export function getCategoriesObject() {
  return request.get({
    url: '/templateMarket/categoriesObject'
  })
}
