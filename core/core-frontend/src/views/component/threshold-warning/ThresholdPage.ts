export interface DialogRow {
  id: string
  name: string
  status: boolean
  enable: boolean
}

export interface ChartBaseInfo {
  id?: string
  chartId: string
  chartType: string
  chartName: string
  tableId: string
  resourceId: string
  resourceType: string
  resourceName: string
  xAxis: object[]
  xAxisExt: object[]
  yAxis: object[]
  yAxisExt: object[]
  extStack: object[]
  extBubble: object[]
  extLabel: object[]
  extTooltip: object[]
  extColor: object[]
  flowMapStartName: object[]
  flowMapEndName: object[]
}

export const convertChart2FieldList = (chartInfo: ChartBaseInfo) => {
  return [
    ...chartInfo.xAxis || [], 
    ...chartInfo.xAxisExt || [],
    ...chartInfo.yAxis || [],
    ...chartInfo.yAxisExt || [],
    ...chartInfo.extStack || [],
    ...chartInfo.extBubble || [],
    ...chartInfo.extLabel || [],
    ...chartInfo.extTooltip || [],
    ...chartInfo.extColor || [],
    ...chartInfo.flowMapStartName || [],
    ...chartInfo.flowMapEndName || []
  ]
}