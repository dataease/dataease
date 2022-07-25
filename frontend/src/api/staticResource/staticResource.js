import request from '@/utils/request'
import { uuid } from 'vue-uuid'
import store from '@/store'

export function uploadFile(fileId, param) {
  return request({
    url: '/static/resource/upload/' + fileId,
    method: 'post',
    headers: { 'Content-Type': 'multipart/form-data' },
    data: param,
    loading: false
  })
}

export function uploadFileResult(file, callback) {
  const fileId = uuid.v1()
  const fileName = file.name
  const newFileName = fileId + fileName.substr(fileName.lastIndexOf('.'), fileName.length)
  const fileUrl = store.state.staticResourcePath + newFileName
  const param = new FormData()
  param.append('file', file)
  uploadFile(fileId, param).then(() => {
    callback(fileUrl)
  })
}

export function findResourceAsBase64(params) {
  return request({
    url: '/static/resource/findResourceAsBase64',
    method: 'post',
    data: params,
    loading: false
  })
}

