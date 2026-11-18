import { getRange } from '@/utils/timeUitils'
import { union } from 'lodash-es'

export function viewFieldTimeTrans(viewDataInfo, params) {
  if (params && params.dimensionList) {
    const fields = viewDataInfo?.fields
      ? viewDataInfo.fields
      : viewDataInfo?.left?.fields || viewDataInfo?.right?.fields
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

    const nameDatePatternMap = fields.reduce((pre, next) => {
      pre[next['dataeaseName']] = next['datePattern']
      return pre
    }, {})

    // 指标卡的维度可能直接引用图表轴配置，只在副本上保存本次联动的时间信息。
    const convertedDimensions = new Map()
    params.dimensionList = params.dimensionList.map(dimension => {
      if (convertedDimensions.has(dimension)) {
        return convertedDimensions.get(dimension)
      }
      const converted = { ...dimension }
      delete converted.timeValue
      delete converted.linkageDateStyle
      delete converted.linkageDatePattern
      const dataeaseName = idNameMap[dimension.id]
      // deType === 1 表示是时间类型
      if (nameTypeMap[dataeaseName] === 1) {
        const dateStyle = nameDateStyleMap[dataeaseName]
        converted.timeValue = getRange(dimension.value, dateStyle)
        if (params.option === 'linkage' && ['H_m_s', 'M_d'].includes(dateStyle)) {
          converted.linkageDateStyle = dateStyle
          converted.linkageDatePattern = nameDatePatternMap[dataeaseName]
        }
      }
      convertedDimensions.set(dimension, converted)
      return converted
    })
    // 区间条形图和子弹图会将维度同时用作指标，保留这种引用关系，避免旧值覆盖联动条件。
    if (params.quotaList) {
      params.quotaList = params.quotaList.map(quota => convertedDimensions.get(quota) || quota)
    }
  }
}

export function checkIsSameDs(canvasViewInfo, sourceViewId, targetViewId) {
  return canvasViewInfo[sourceViewId]['tableId'] === canvasViewInfo[targetViewId]['tableId']
}
