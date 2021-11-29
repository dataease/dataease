import request from '@/utils/request'

export function queryAuthModel(data, loading = true, timeout = 30000) {
  return request({
    url: 'authModel/queryAuthModel',
    method: 'post',
    loading: loading,
    timeout: timeout,
    data
  })
}
