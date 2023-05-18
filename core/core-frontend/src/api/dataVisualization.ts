import request from '@/config/axios'

export interface DvOrFolder {
  name: string
  id?: number | string
  pid?: number | string
  nodeType: 'folder' | 'dv'
  type: string
}

export const findById = dvId => request.get({ url: '/dataVisualization/findById/' + dvId })

export const findTree = data => request.post({ url: '/dataVisualization/findTree', data })

export const save = data => request.post({ url: '/dataVisualization/save', data })

export const savaOrUpdateBase = data =>
  request.post({ url: '/dataVisualization/savaOrUpdateBase', data })

export const update = data => request.post({ url: '/dataVisualization/update', data })

export const deleteLogic = dvId => request.delete({ url: '/dataVisualization/deleteLogic/' + dvId })
