import request from '@/config/axios'

export const thresholdGridApi = (current: number, size: number, data) => {
  return request.post({ url: `/threshold/pager/${current}/${size}`, data })
}

export const thresholdRestoreApi = data => {
  return request.post({ url: `/threshold/thresholdSnapshotRestore`, data })
}

export const thresholdDelApi = (idList, resourceTable = 'core') => {
  return request.post({ url: '/threshold/delete/' + resourceTable, data: idList })
}

export const thresholdSwitchApi = data => {
  return request.post({ url: '/threshold/switch', data })
}

export const thresholdSaveApi = data => {
  return request.post({ url: '/threshold/save', data })
}

export const thresholdEditApi = data => {
  return request.post({ url: '/threshold/edit', data })
}

export const thresholdInfoApi = (id, resourceTable = 'core') => {
  return request.get({ url: `/threshold/formInfo/${id}/${resourceTable}` })
}

export const datasetFieldApi = datasetId => {
  return request.post({ url: `/datasetField/listByDatasetGroup/${datasetId}` });
}

export const chartInfoApi = (id, resourceTable='core') => {
  return request.get({ url: `/chart/chartBaseInfo/${id}/${resourceTable}` })
}

export const thresholdBatchReciApi = data => {
  return request.post({ url: '/threshold/batchReci', data })
}

export const thresholdPreviewApi = data => {
  return request.post({ url: '/threshold/preview', data })
}

export const queryAnyThresholdApi =  (chartId,resourceTable = 'core') => {
  return request.get({ url: `/threshold/anyThreshold/${chartId}/${resourceTable}` })
}

export const delWithChartApi = (chartId,resourceTable = 'core') => {
  return request.get({ url: `/threshold/deleteWithChart/${chartId}/${resourceTable}` })
}

export const webhookOptionsApi = () => {
  return request.get({ url: '/webhook/options' })
}