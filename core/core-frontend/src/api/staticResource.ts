import request from '@/config/axios'
import { guid } from '@/views/visualized/data/dataset/form/util.js'
import { ElMessage } from 'element-plus-secondary'

const staticResourcePath = '/static-resource/'
export const uploadFile = (fileId: number | string, param) =>
  request.post({
    url: '/staticResource/upload/' + fileId,
    headersType: 'multipart/form-data',
    loading: true,
    data: param
  })

export function beforeUploadCheck(file) {
  const isImage = file.type.startsWith('image/')
  const isSizeValid = file.size / 1024 / 1024 < 15 // 15MB

  if (!isImage) {
    ElMessage.error('请上传图片')
    return false
  }
  if (!isSizeValid) {
    ElMessage.error('图片大小不能超过15M')
    return false
  }
  return true
}

export function uploadFileResult(file, callback) {
  const fileId = guid()
  const fileName = file.name
  const newFileName = fileId + fileName.substr(fileName.lastIndexOf('.'), fileName.length)
  const fileUrl = staticResourcePath + newFileName
  const param = new FormData()
  param.append('file', file)
  return uploadFile(fileId, param).then(() => {
    callback(fileUrl)
  })
}

export function findResourceAsBase64(params) {
  return request.post({
    url: '/staticResource/findResourceAsBase64',
    data: params
  })
}
