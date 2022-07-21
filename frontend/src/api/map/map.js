import request from '@/utils/request'

export const areaMapping = () => {
  return request({
    url: '/api/map/globalEntitys/0',
    method: 'get',
    loading: true
  })
}

export const globalMapping = () => {
  return request({
    url: '/api/map/globalEntitys/0',
    method: 'get',
    loading: true
  })
}

export function geoJson(areaCode) {
  const countryCode = areaCode.substring(0, 3)
  return request({
    url: '/geo/full/' + countryCode + '/' + areaCode + '_full.json',
    method: 'get',
    loading: true
  })
}

export function saveMap(data) {
  return request({
    url: '/api/map/saveMapNode',
    method: 'post',
    loading: true,
    data
  })
}

export function removeMap(data) {
  return request({
    url: '/api/map/delMapNode',
    method: 'post',
    loading: true,
    data
  })
}
