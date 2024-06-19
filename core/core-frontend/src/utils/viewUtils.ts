import { getRange } from '@/utils/timeUitils'

export function viewFieldTimeTrans(viewDataInfo, params) {
  if (viewDataInfo && params && params.dimensionList) {
    const idNameMap = viewDataInfo.fields.reduce((pre, next) => {
      pre[next['id']] = next['dataeaseName']
      return pre
    }, {})

    const nameTypeMap = viewDataInfo.fields.reduce((pre, next) => {
      pre[next['dataeaseName']] = next['deType']
      return pre
    }, {})

    const nameDateStyleMap = viewDataInfo.fields.reduce((pre, next) => {
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
