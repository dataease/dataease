import request from '@/config/axios'
import type { BusiTreeRequest } from '@/models/tree/TreeNode'
export interface ResourceOrFolder {
  name: string
  id?: number | string
  pid?: number | string
  nodeType: 'folder' | 'leaf'
  type: string
}

export interface Panel {
  name: string
  type: string
  updateTime: number
  createBy: string
  updateBy: string
}

export const findCopyResource = async (dvId, busiFlag): Promise<IResponse> => {
  return request.get({ url: '/dataVisualization/findCopyResource/' + dvId + '/' + busiFlag })
}

export const findById = async (
  dvId,
  busiFlag,
  attachInfo = { source: 'main', taskId: null }
): Promise<IResponse> => {
  let busiFlagResult = busiFlag
  if (!busiFlagResult) {
    await findDvType(dvId).then(res => {
      busiFlagResult = res.data
    })
  }
  const data = { id: dvId, busiFlag: busiFlagResult, ...attachInfo }
  return request.post({ url: '/dataVisualization/findById', data })
}

export const queryTreeApi = async (data: BusiTreeRequest): Promise<IResponse> => {
  return request.post({ url: '/dataVisualization/tree', data }).then(res => {
    return res?.data
  })
}

export const findDvType = async dvId =>
  request.get({ url: `/dataVisualization/findDvType/${dvId}` })

export const save = data => request.post({ url: '/dataVisualization/save', data })

export const saveCanvas = data =>
  request.post({ url: '/dataVisualization/saveCanvas', data, loading: true })

export const appCanvasNameCheck = async data =>
  request.post({ url: '/dataVisualization/appCanvasNameCheck', data, loading: false })

export const updateBase = data => request.post({ url: '/dataVisualization/updateBase', data })

export const updateCanvas = data =>
  request.post({ url: '/dataVisualization/updateCanvas', data, loading: true })

export const moveResource = data => request.post({ url: '/dataVisualization/move', data })

export const copyResource = data => request.post({ url: '/dataVisualization/copy', data })

export const deleteLogic = (dvId, busiFlag) =>
  request.delete({ url: '/dataVisualization/deleteLogic/' + dvId + '/' + busiFlag })

export const querySubjectWithGroupApi = data =>
  request.post({ url: '/visualizationSubject/querySubjectWithGroup', data })

export const saveOrUpdateSubject = data =>
  request.post({ url: '/visualizationSubject/update', data })

export const deleteSubject = id => request.delete({ url: '/visualizationSubject/delete/' + id })

export const dvNameCheck = async data => request.post({ url: '/dataVisualization/nameCheck', data })

export const storeApi = (data): Promise<IResponse> => {
  return request.post({ url: '/store/execute', data })
}

export const storeStatusApi = (id: string): Promise<IResponse> => {
  return request.get({ url: `/store/favorited/${id}` })
}

export const decompression = async data =>
  request.post({ url: '/dataVisualization/decompression', data, loading: true })

export const viewDetailList = dvId => {
  return request.get({
    url: '/dataVisualization/viewDetailList/' + dvId,
    method: 'get',
    loading: false
  })
}

export const getComponentInfo = dvId => {
  return request.get({
    url: '/panel/view/getComponentInfo/' + dvId,
    loading: false
  })
}

export const export2AppCheck = params => {
  return request.post({
    url: '/dataVisualization/export2AppCheck',
    data: params,
    loading: true
  })
}
