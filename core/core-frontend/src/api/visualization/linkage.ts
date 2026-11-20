import type { QueryRequest } from '@/config/axios/queryScope'
import request from '@/config/axios'
import { postCanvasRelation } from './canvasRevision'

export const getViewLinkageGather = data =>
  request.post({ url: '/linkage/getViewLinkageGather', data })

export const getViewLinkageGatherArray = data =>
  request.post({ url: '/linkage/getViewLinkageGatherArray', data })

export const saveLinkage = data => postCanvasRelation('/linkage/saveLinkage', data, data?.dvId)

export const getPanelAllLinkageInfo = (
  dvId,
  resourceTable = 'snapshot',
  queryRequest?: QueryRequest
) =>
  request.get({
    url: '/linkage/getVisualizationAllLinkageInfo/' + dvId + '/' + resourceTable,
    queryRequest
  })

export const updateLinkageActive = data =>
  postCanvasRelation('/linkage/updateLinkageActive', data, data?.dvId)

export const removeLinkage = data => postCanvasRelation('/linkage/removeLinkage', data, data?.dvId)
