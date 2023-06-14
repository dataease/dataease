import request from '@/config/axios'

export const queryVisualizationBackground = () =>
  request.get({ url: '/visualizationBackground/findAll' })
