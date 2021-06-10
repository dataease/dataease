import request from '@/utils/request'

export function get(url) {
  return request({
    url: url,
    method: 'get',
    loading: true
  })
}

export function execute(options) {
  if (!options || !options.url) {
    return null
  }
  options.type = options.type || 'post'

  return request({
    url: options.url,
    method: options.type,
    loading: true,
    data: options.data
  })
}
