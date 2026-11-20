import request from '@/config/axios'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { isDesktop } from '@/utils/ModelUtil'
import { guid } from '@/views/visualized/data/dataset/form/util'

export const checkCanvasRevision = (contentId?: string | null) => ({
  checkHistory: !isDesktop(),
  expectedContentId: contentId ?? null
})

export async function postCanvasRelation(
  url: string,
  data: object,
  id: string | number | null | undefined,
  loading?: boolean
): Promise<IResponse> {
  const canvas = dvMainStoreWithOut()
  if (id == null || canvas.editMode !== 'edit' || String(canvas.dvInfo.id) !== String(id)) {
    return request.post({ url, data, loading })
  }
  const revision = { ...checkCanvasRevision(canvas.dvInfo.contentId), contentId: guid() }
  const response = await request.post({ url, data: { ...data, revision }, loading })
  if (
    String(canvas.dvInfo.id) === String(id) &&
    (canvas.dvInfo.contentId ?? null) === revision.expectedContentId
  ) {
    canvas.dvInfo.contentId = revision.contentId
  }
  return response
}
