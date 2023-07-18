import request from '@/config/axios'

export interface ResourceOrFolder {
  name: string
  id?: number | string
  pid?: number | string
  nodeType: 'folder' | 'leaf'
  type: string
}

export const findById = dvId => request.get({ url: '/dataVisualization/findById/' + dvId })

// export const queryTreeApi = data => request.post({ url: '/dataVisualization/tree', data })
export const queryTreeApi = async (busiType): Promise<IResponse> => {
  return request.get({ url: `/dataVisualization/tree/${busiType}`, data: {} }).then(res => {
    return res?.data
  })
}

export const save = data => request.post({ url: '/dataVisualization/save', data })

export const savaOrUpdateBase = data =>
  request.post({ url: '/dataVisualization/savaOrUpdateBase', data })

export const update = data => request.post({ url: '/dataVisualization/update', data })

export const deleteLogic = dvId => request.delete({ url: '/dataVisualization/deleteLogic/' + dvId })

export const querySubjectWithGroupApi = data =>
  request.post({ url: '/visualizationSubject/querySubjectWithGroup', data })

export const saveOrUpdateSubject = data =>
  request.post({ url: '/visualizationSubject/update', data })

export const deleteSubject = id => request.delete({ url: '/visualizationSubject/delete/' + id })

export const dvNameCheck = data => request.post({ url: '/dataVisualization/nameCheck', data })
