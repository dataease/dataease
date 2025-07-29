import request from '@/config/axios'
import type { BusiTreeRequest } from '@/models/tree/TreeNode'
import { originNameHandleWithArr } from '@/utils/CalculateFields'
import { cloneDeep } from 'lodash-es'
export interface ResourceOrFolder {
  name: string
  id?: number | string
  pid?: number | string
  nodeType: 'folder' | 'leaf'
  type: string
  mobileLayout: boolean
  status: boolean
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

export const updateCheckVersion = dvId =>
  request.get({ url: `/dataVisualization/updateCheckVersion/${dvId}` })

export const queryTreeApi = async (data: BusiTreeRequest): Promise<IResponse> => {
  return request.post({ url: '/dataVisualization/tree', data }).then(res => {
    return res?.data
  })
}

export const queryBusiTreeApi = async (data): Promise<IResponse> => {
  return request.post({ url: '/dataVisualization/interactiveTree', data }).then(res => {
    return res?.data
  })
}

export const findDvType = async dvId =>
  request.get({ url: `/dataVisualization/findDvType/${dvId}` })

export const save = data => request.post({ url: '/dataVisualization/save', data })

export const checkCanvasChange = data =>
  request.post({ url: '/dataVisualization/checkCanvasChange', data, loading: true })

export const saveCanvas = data =>
  request.post({ url: '/dataVisualization/saveCanvas', data, loading: true })

export const updatePublishStatus = data =>
  request.post({ url: '/dataVisualization/updatePublishStatus', data, loading: false })

export const recoverToPublished = data =>
  request.post({ url: '/dataVisualization/recoverToPublished', data, loading: true })
export const appCanvasNameCheck = async data =>
  request.post({ url: '/dataVisualization/appCanvasNameCheck', data, loading: false })

export const updateBase = data => request.post({ url: '/dataVisualization/updateBase', data })

export const updateCanvas = data => {
  const copyData = cloneDeep(data)
  const fields = [
    'xAxis',
    'xAxisExt',
    'yAxis',
    'yAxisExt',
    'extBubble',
    'extLabel',
    'extStack',
    'extTooltip',
    'extColor'
  ]

  for (const key in copyData.canvasViewInfo) {
    originNameHandleWithArr(copyData.canvasViewInfo[key], fields)
  }
  return request.post({ url: '/dataVisualization/updateCanvas', data: copyData, loading: true })
}

export const moveResource = data => request.post({ url: '/dataVisualization/move', data })

export const copyResource = data => request.post({ url: '/dataVisualization/copy', data })

export const deleteLogic = (dvId, busiFlag) =>
  request.post({ url: '/dataVisualization/deleteLogic/' + dvId + '/' + busiFlag })

export const querySubjectWithGroupApi = data =>
  request.post({ url: '/visualizationSubject/querySubjectWithGroup', data })

export const saveOrUpdateSubject = data =>
  request.post({ url: '/visualizationSubject/update', data })

export const deleteSubject = id => request.post({ url: '/visualizationSubject/delete/' + id })

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

export const queryOuterParamsDsInfo = async dvId => {
  return request.get({
    url: '/outerParams/queryDsWithVisualizationId/' + dvId,
    method: 'get',
    loading: false
  })
}

export const queryShareBaseApi = () => {
  return request.get({
    url: '/sysParameter/shareBase',
    loading: false
  })
}
