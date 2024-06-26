import { deepCopy } from '@/utils/utils'

export default function defaultConditionTrans(canvasInfo) {
  const { reportFilterInfo, componentData } = canvasInfo
  const componentDataArray = JSON.parse(componentData)
  const allFilter = []
  // 获取所有查询条件
  componentDataArray.forEach(item => {
    if (item.component === 'VQuery') {
      Array.prototype.push.apply(allFilter, item.propValue)
    }
  })

  const allDefaultFilter = deepCopy(allFilter)
  if (reportFilterInfo) {
    allFilter.forEach((itemFilter, index) => {
      if (reportFilterInfo[itemFilter.id]) {
        allDefaultFilter.splice(index, 1, JSON.parse(reportFilterInfo[itemFilter.id]))
      }
    })
  }

  return {
    sourceFilter: allFilter,
    defaultFilter: allDefaultFilter
  }
}
