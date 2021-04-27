import request from '@/utils/request'

export function post(url, data) {
  return request({
    url: url,
    method: 'post',
    loading: true,
    data
  })
}

export function get(url) {
  return request({
    url: url,
    method: 'get',
    loading: true
  })
}

export function fileUpload(url, file, files, param) {
  const formData = new FormData()
  if (file) {
    formData.append('file', file)
  }
  if (files) {
    files.forEach(f => {
      formData.append('files', f)
    })
  }
  formData.append('request', new Blob([JSON.stringify(param)], { type: 'application/json' }))
  return request({
    method: 'POST',
    loading: true,
    url: url,
    data: formData
  })
}
export default { fileUpload }
