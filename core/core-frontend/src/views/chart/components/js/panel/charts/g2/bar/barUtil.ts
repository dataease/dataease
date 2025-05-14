import { parseJson } from '@/views/chart/components/js/util'
import { G2Spec } from '@antv/g2'

export type ViewSpec = { children?: G2Spec[]; [key: string]: any } & G2Spec
export type Transform = {
  type: string
  [key: string]: any
}

export function handleEmptyDataStrategy<O extends ViewSpec>(chart: Chart, options: O): O {
  const { data } = options.children[0]
  const isChartMix = chart.type.includes('chart-mix')
  if (!data?.length) {
    return options
  }
  const strategy = parseJson(chart.senior).functionCfg.emptyDataStrategy
  if (strategy === 'ignoreData') {
    if (isChartMix) {
      for (let i = 0; i < data.length; i++) {
        handleIgnoreData(data[i] as Record<string, any>[])
      }
    } else {
      handleIgnoreData(data)
    }
    return options
  }
  const { yAxis, xAxisExt, extStack, extBubble } = chart
  const multiDimension = yAxis?.length >= 2 || xAxisExt?.length > 0 || extStack?.length > 0
  switch (strategy) {
    case 'breakLine': {
      if (isChartMix) {
        if (data[0]) {
          if (xAxisExt?.length > 0 || extStack?.length > 0) {
            handleBreakLineMultiDimension(data[0] as Record<string, any>[])
          }
        }
        if (data[1]) {
          if (extBubble?.length > 0) {
            handleBreakLineMultiDimension(data[1] as Record<string, any>[])
          }
        }
      } else {
        if (multiDimension) {
          handleBreakLineMultiDimension(data)
        }
      }
      return {
        ...options,
        connectNulls: false
      }
    }
    case 'setZero': {
      if (isChartMix) {
        if (data[0]) {
          if (xAxisExt?.length > 0 || extStack?.length > 0) {
            handleSetZeroMultiDimension(data[0] as Record<string, any>[])
          } else {
            handleSetZeroSingleDimension(data[0] as Record<string, any>[])
          }
        }
        if (data[1]) {
          if (extBubble?.length > 0) {
            handleSetZeroMultiDimension(data[1] as Record<string, any>[], true)
          } else {
            handleSetZeroSingleDimension(data[1] as Record<string, any>[], true)
          }
        }
      } else {
        if (multiDimension) {
          // 多维度置0
          handleSetZeroMultiDimension(data)
        } else {
          // 单维度置0
          handleSetZeroSingleDimension(data)
        }
      }
      break
    }
  }
  return options
}

function handleBreakLineMultiDimension(data) {
  const dimensionInfoMap = new Map()
  const subDimensionSet = new Set()
  const quotaMap = new Map<string, { id: string }[]>()
  for (let i = 0; i < data.length; i++) {
    const item = data[i]
    const dimensionInfo = dimensionInfoMap.get(item.field)
    if (dimensionInfo) {
      dimensionInfo.set.add(item.category)
    } else {
      dimensionInfoMap.set(item.field, { set: new Set([item.category]), index: i })
    }
    subDimensionSet.add(item.category)
    quotaMap.set(item.category, item.quotaList)
  }
  // Map 是按照插入顺序排序的，所以插入索引往后推
  let insertCount = 0
  dimensionInfoMap.forEach((dimensionInfo, field) => {
    if (dimensionInfo.set.size < subDimensionSet.size) {
      let subInsertIndex = 0
      subDimensionSet.forEach(dimension => {
        if (!dimensionInfo.set.has(dimension)) {
          data.splice(dimensionInfo.index + insertCount + subInsertIndex, 0, {
            field,
            value: null,
            category: dimension,
            quotaList: quotaMap.get(dimension as string)
          })
        }
        subInsertIndex++
      })
      insertCount += subDimensionSet.size - dimensionInfo.set.size
    }
  })
}

function handleSetZeroMultiDimension(data: Record<string, any>[], isExt = false) {
  const dimensionInfoMap = new Map()
  const subDimensionSet = new Set()
  const quotaMap = new Map<string, { id: string }[]>()
  for (let i = 0; i < data.length; i++) {
    const item = data[i]
    if (item.value === null) {
      item.value = 0
      if (isExt) {
        item.valueExt = 0
      }
    }
    const dimensionInfo = dimensionInfoMap.get(item.field)
    if (dimensionInfo) {
      dimensionInfo.set.add(item.category)
    } else {
      dimensionInfoMap.set(item.field, { set: new Set([item.category]), index: i })
    }
    subDimensionSet.add(item.category)
    quotaMap.set(item.category, item.quotaList)
  }
  let insertCount = 0
  dimensionInfoMap.forEach((dimensionInfo, field) => {
    if (dimensionInfo.set.size < subDimensionSet.size) {
      let subInsertIndex = 0
      subDimensionSet.forEach(dimension => {
        if (!dimensionInfo.set.has(dimension)) {
          const _temp = {
            field,
            value: 0,
            category: dimension,
            quotaList: quotaMap.get(dimension as string)
          } as any
          if (isExt) {
            _temp.valueExt = 0
          }

          data.splice(dimensionInfo.index + insertCount + subInsertIndex, 0, _temp)
        }
        subInsertIndex++
      })
      insertCount += subDimensionSet.size - dimensionInfo.set.size
    }
  })
}

function handleSetZeroSingleDimension(data: Record<string, any>[], isExt = false) {
  data.forEach(item => {
    if (item.value === null) {
      if (!isExt) {
        item.value = 0
      } else {
        item.valueExt = 0
      }
    }
  })
}

function handleIgnoreData(data: Record<string, any>[]) {
  for (let i = data.length - 1; i >= 0; i--) {
    const item = data[i]
    if (item.value === null) {
      data.splice(i, 1)
    }
  }
}
