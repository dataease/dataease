import request from '@/config/axios'

export const getViewLinkageGather = data =>
  request.post({ url: '/linkage/getViewLinkageGather', data })

export const getViewLinkageGatherArray = data =>
  request.post({ url: '/linkage/getViewLinkageGatherArray', data })

export const saveLinkage = data => request.post({ url: '/linkage/saveLinkage', data })

export const getPanelAllLinkageInfo = dvId =>
  request.get({ url: '/linkage/getVisualizationAllLinkageInfo/' + dvId })
