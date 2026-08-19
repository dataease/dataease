import request from '@/config/axios'

export const thresholdGridApi = (current: number, size: number, data) => {
  return request.post({ url: `/threshold/pager/${current}/${size}`, data })
}

export const thresholdDelApi = (idList, resourceTable = 'core') => {
  return request.post({ url: '/threshold/delete/' + resourceTable, data: idList })
}


export const thresholdSwitchApi = data => {
  return request.post({ url: '/threshold/switch', data })
}

export const thresholdInstanceGridApi = (current: number, size: number, data) => {
  return request.post({ url: `/threshold/instancePager/${current}/${size}`, data })
}