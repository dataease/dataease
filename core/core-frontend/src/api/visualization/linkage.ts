import type { QueryRequest } from '@/config/axios/queryScope'
import request from '@/config/axios'
import { postCanvasRelation } from './canvasRevision'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'

export const getViewLinkageGather = data =>
  request.post({ url: '/linkage/getViewLinkageGather', data })

export const getViewLinkageGatherArray = data =>
  request.post({ url: '/linkage/getViewLinkageGatherArray', data })

export const saveLinkage = data => postCanvasRelation('/linkage/saveLinkage', data, data?.dvId)

export const getPanelAllLinkageInfo = async (
  dvId,
  resourceTable = 'snapshot',
  queryRequest?: QueryRequest
) => {
  const canvas = dvMainStoreWithOut()
  const editor =
    resourceTable === 'snapshot' &&
    canvas.editMode === 'edit' &&
    String(canvas.dvInfo.id) === String(dvId)
      ? canvas.dvInfo
      : null
  const contentId = editor?.contentId
  const response = await request.get({
    url: '/linkage/getVisualizationAllLinkageInfo/' + dvId + '/' + resourceTable,
    queryRequest
  })
  // 保存或切换快照前发出的请求不能覆盖当前编辑器的联动配置。
  if (editor && (canvas.dvInfo !== editor || editor.contentId !== contentId)) {
    return { ...response, data: canvas.nowPanelTrackInfo }
  }
  return response
}

export const updateLinkageActive = data =>
  postCanvasRelation('/linkage/updateLinkageActive', data, data?.dvId)

export const removeLinkage = data => postCanvasRelation('/linkage/removeLinkage', data, data?.dvId)
