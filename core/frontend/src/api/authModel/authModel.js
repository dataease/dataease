import request from '@/utils/request'

export function queryAuthModel(data, loading = true, timeout = 60000) {
  return request({
    url: 'authModel/queryAuthModel',
    method: 'post',
    loading: loading,
    data
  })
}
