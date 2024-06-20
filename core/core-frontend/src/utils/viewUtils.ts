import { getRange } from '@/utils/timeUitils'
import { union } from 'lodash-es'

export function viewFieldTimeTrans(viewDataInfo, params) {
  if (viewDataInfo && params && params.dimensionList) {
    const fields = viewDataInfo.fields
      ? viewDataInfo.fields
      : viewDataInfo.left?.fields || viewDataInfo.right?.fields
      ? union(viewDataInfo.left?.fields, viewDataInfo.right?.fields)
      : []

    const idNameMap = fields.reduce((pre, next) => {
      pre[next['id']] = next['dataeaseName']
      return pre
    }, {})

    const nameTypeMap = fields.reduce((pre, next) => {
      pre[next['dataeaseName']] = next['deType']
      return pre
    }, {})

    const nameDateStyleMap = fields.reduce((pre, next) => {
      pre[next['dataeaseName']] = next['dateStyle']
      return pre
    }, {})

    params.dimensionList.forEach(dimension => {
      const dataeaseName = idNameMap[dimension.id]
      // deType === 1 表示是时间类型
      if (nameTypeMap[dataeaseName] === 1) {
        dimension['timeValue'] = getRange(dimension.value, nameDateStyleMap[dataeaseName])
      }
    })
  }
}
