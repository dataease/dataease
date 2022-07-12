import request from '@/utils/request'

export function queryBackground() {
  return request({
    url: 'background/findAll',
    method: 'get'
  })
}
export function uploadImgUrl(data) {
  return request({
    url: '/api/filePicture/uploadFile',
    method: 'post',
    data
  })
}
