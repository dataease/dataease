import request from '@/utils/request'

export const areaMapping = () => {
  return request({
    url: '/api/map/areaEntitys/0',
    method: 'get',
    loading: true
  })
}

export function geoJson(areaCode) {
  return request({
    url: '/api/map/resourceFull/' + areaCode,
    method: 'get',
    loading: true
  })
}
