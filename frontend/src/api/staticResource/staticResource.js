import request from '@/utils/request'
import { uuid } from 'vue-uuid'
import store from '@/store'

export function uploadFile(fileId, file) {
  const param = new FormData()
  param.append('file', file.file)
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
  const fileName = file.file.name
  const newFileName = fileId + fileName.substr(fileName.lastIndexOf('.'), fileName.length)
  const fileUrl = store.state.staticResourcePath + newFileName
  uploadFile(fileId, file).then(() => {
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

