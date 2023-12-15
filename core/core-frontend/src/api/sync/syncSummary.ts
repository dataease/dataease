import request from '@/config/axios'

export interface IChartData {
  executeDateList: []
  executeDateRunningList: []
  executeDateSuccessList: []
  executeDateFailList: []
  successCount: 0
  failCount: 0
  runningCount: 0
}

interface IResourceCount {
  jobCount: number
  datasourceCount: number
  jobLogCount: number
}

export const getResourceCount = () => {
  return request.get<IResourceCount>({
    url: 'sync/summary/resourceCount',
    method: 'get'
  })
}

export const getJobLogLienChartInfo = () => {
  return request.post({
    url: '/sync/summary/logChartData',
    method: 'post',
    data: ''
  })
}
