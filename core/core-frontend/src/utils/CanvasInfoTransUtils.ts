import { deepCopy } from '@/utils/utils'

/** 查询控件与图表/字段的关联字段，执行定时报告时应始终取自当前画布，不能被 filter_info 快照覆盖 */
const LIVE_FILTER_RELATION_KEYS = [
  'checkedFields',
  'checkedFieldsMap',
  'checkedFieldsMapArr',
  'checkedFieldsMapArrNum',
  'checkedFieldsMapStart',
  'checkedFieldsMapEnd',
  'checkedFieldsMapStartNum',
  'checkedFieldsMapEndNum'
] as const

/**
 * 将定时报告保存的筛选值合并到当前画布查询条件上。
 * 保留 liveFilter 的图表关联，避免仪表板新增图表并关联后，旧报告仍按旧关联列表执行。
 */
export function mergeReportFilterWithLive(liveFilter: any, reportFilterInfo: string | Record<string, any>) {
  if (!liveFilter || reportFilterInfo == null) {
    return liveFilter
  }
  const reportItem =
    typeof reportFilterInfo === 'string' ? JSON.parse(reportFilterInfo) : deepCopy(reportFilterInfo)
  if (!reportItem || typeof reportItem !== 'object') {
    return liveFilter
  }
  const merged = deepCopy(reportItem)
  LIVE_FILTER_RELATION_KEYS.forEach(key => {
    if (liveFilter[key] !== undefined) {
      merged[key] = deepCopy(liveFilter[key])
    }
  })
  if (merged['defaultValueCheck']) {
    merged['defaultValueFirstItem'] = false
  }
  return merged
}

export default function defaultConditionTrans(canvasInfo) {
  const { reportFilterInfo, componentData } = canvasInfo
  const componentDataArray = JSON.parse(componentData)
  const allFilter = []
  const componentMap = {}
  // 获取所有查询条件
  componentDataArray.forEach(item => {
    if (item.component === 'VQuery') {
      item.propValue.forEach(filterItem => {
        componentMap[filterItem.id] = item
      })
      Array.prototype.push.apply(allFilter, item.propValue)
    }
  })

  const allDefaultFilter = deepCopy(allFilter)
  if (reportFilterInfo) {
    allFilter.forEach((itemFilter, index) => {
      if (reportFilterInfo[itemFilter.id]) {
        allDefaultFilter.splice(
          index,
          1,
          mergeReportFilterWithLive(itemFilter, reportFilterInfo[itemFilter.id].filterInfo)
        )
      }
    })
  }

  return {
    sourceFilter: allFilter,
    defaultFilter: allDefaultFilter,
    sourceDefaultFilter: deepCopy(allDefaultFilter),
    componentMap: componentMap
  }
}
