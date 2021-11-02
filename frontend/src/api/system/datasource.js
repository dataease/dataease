import request from '@/utils/request'

export function dsGrid(pageIndex, pageSize, data) {
  return request({
    url: 'datasource/list/' + pageIndex + '/' + pageSize,
    method: 'post',
    loading: true,
    data
  })
}
export function listDatasource() {
  return request({
    url: '/datasource/list',
    loading: true,
    method: 'get'
  })
}
export function listDatasourceByType(type) {
  return request({
    url: '/datasource/list/' + type,
    loading: true,
    method: 'get'
  })
}
export function addDs(data) {
  return request({
    url: 'datasource/add/',
    method: 'post',
    loading: true,
    data
  })
}

export function editDs(data) {
  return request({
    url: 'datasource/update/',
    method: 'post',
    loading: true,
    data
  })
}

export function delDs(id) {
  return request({
    url: 'datasource/delete/' + id,
    loading: true,
    method: 'post'
  })
}

export function validateDs(data) {
  return request({
    url: 'datasource/validate/',
    method: 'post',
    loading: true,
    data
  })
}

export function validateDsById(datasourceId) {
  return request({
    url: 'datasource/validate/' + datasourceId,
    method: 'get',
    loading: true
  })
}

export function getSchema(data) {
  return request({
    url: 'datasource/getSchema/',
    method: 'post',
    loading: true,
    data
  })
}

export default { dsGrid, addDs, editDs, delDs, validateDs, listDatasource, getSchema }
