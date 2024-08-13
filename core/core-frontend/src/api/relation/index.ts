import request from '@/config/axios'

export function getDatasourceRelationship(id) {
  return request.post({
    url: `/relation/datasource/${id}`
  })
}

export function getDatasetRelationship(id) {
  return request.post({
    url: `/relation/dataset/${id}`
  })
}

export function getPanelRelationship(id) {
  return request.post({
    url: `/relation/dv/${id}`
  })
}
