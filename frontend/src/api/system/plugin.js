import request from '@/utils/request'
const pathMap = {
  queryPath: '/api/plugin/pluginGrid/'
}
export function pluginLists(page, size, data) {
  return request({
    url: pathMap.queryPath + page + '/' + size,
    method: 'post',
    data,
    loading: true
  })
}

