import request from '@/config/axios'

export const findById = dvId => request.get({ url: '/dataVisualization/findById/' + dvId })

export const save = data => request.post({ url: '/dataVisualization/save', data })

export const update = data => request.post({ url: '/dataVisualization/update', data })

export const deleteLogic = dvId => request.delete({ url: '/dataVisualization/delete/' + dvId })
