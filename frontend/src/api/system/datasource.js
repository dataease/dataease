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
export function listDrivers() {
  return request({
    url: '/driver/list',
    loading: true,
    method: 'post'
  })
}
export function listDatasourceType() {
  return request({
    url: '/datasource/types',
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
export function listDriverByType(type) {
  return request({
    url: '/driver/list/' + type,
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

export function checkApiDatasource(data) {
  return request({
    url: 'datasource/checkApiDatasource',
    method: 'post',
    loading: false,
    data
  })
}

export function addDriver(data) {
  return request({
    url: '/driver/save',
    method: 'post',
    loading: true,
    data
  })
}

export function listDriverDetails(id) {
  return request({
    url: '/driver/listDriverDetails/' + id,
    method: 'get',
    loading: true
  })
}

export function deleteDriverFile(data) {
  return request({
    url: '/driver/deleteDriverFile',
    method: 'post',
    loading: true,
    data
  })
}

export function delDriver(data) {
  return request({
    url: 'driver/delete',
    loading: true,
    method: 'post',
    data
  })
}
export function updateDriver(data) {
  return request({
    url: 'driver/update/',
    loading: true,
    method: 'post',
    data
  })
}

export function getDatasourceDetail(id) {
  return request({
    url: `/datasource/get/${id}`,
    loading: true,
    method: 'post',
  })
}
export default { getDatasourceDetail, dsGrid, addDs, editDs, delDs, validateDs, listDatasource, getSchema }
