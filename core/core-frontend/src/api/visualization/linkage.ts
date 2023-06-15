import request from '@/config/axios'

export const getViewLinkageGather = requestInfo =>
  request.post({ url: '/linkage/getViewLinkageGather', requestInfo })

export const saveLinkage = requestInfo => request.post({ url: '/linkage/saveLinkage', requestInfo })

export const getPanelAllLinkageInfo = dvId =>
  request.get({ url: '/linkage/getPanelAllLinkageInfo/' + dvId })
