import request from '@/config/axios'
import { guid } from '@/views/visualized/data/dataset/form/util.js'

const staticResourcePath = '/static-resource/'

export const uploadFile = (fileId: number | string, param) =>
  request.post({
    url: '/staticResource/upload/' + fileId,
    headersType: 'multipart/form-data',
    data: param
  })

export function uploadFileResult(file, callback) {
  const fileId = guid()
  const fileName = file.name
  const newFileName = fileId + fileName.substr(fileName.lastIndexOf('.'), fileName.length)
  const fileUrl = staticResourcePath + newFileName
  const param = new FormData()
  param.append('file', file)
  uploadFile(fileId, param).then(() => {
    callback(fileUrl)
  })
}

export function findResourceAsBase64(params) {
  return request.post({
    url: '/staticResource/findResourceAsBase64',
    method: 'post',
    data: params,
    loading: false
  })
}
