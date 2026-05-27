export interface CascadeItem {
  datasetId?: string
  selectValue?: unknown
  currentSelectValue?: unknown
}

export type CascadeList = CascadeItem[][]

export const isEmptyCascadeValue = (value: unknown) => {
  if (Array.isArray(value)) {
    return value.length === 0
  }
  return value === undefined || value === null || value === ''
}

const getQueryId = (datasetId?: string) => {
  return datasetId?.split('--')[1]
}

const getCascadeValue = (item: CascadeItem, isConfig: boolean) => {
  return isConfig ? item.selectValue : item.currentSelectValue
}

export const isCascadeParentCleared = (
  cascadeList: CascadeList,
  queryId: string,
  isConfig: boolean
) => {
  return (cascadeList || []).some(cascadeItems => {
    const index = cascadeItems.findIndex(item => getQueryId(item.datasetId) === queryId)
    if (index <= 0) {
      return false
    }
    return isEmptyCascadeValue(getCascadeValue(cascadeItems[index - 1], isConfig))
  })
}
