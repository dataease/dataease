import request from '@/utils/request'
const pathMap = {
  queryPath: '/api/plugin/pluginGrid/',
  uninstallPath: 'api/plugin/uninstall/'
}
export function pluginLists(page, size, data) {
  return request({
    url: pathMap.queryPath + page + '/' + size,
    method: 'post',
    data,
    loading: true
  })
}

export function uninstall(pluginId) {
  return request({
    url: pathMap.uninstallPath + pluginId,
    method: 'post',
    loading: true
  })
}

