import { isNumber } from 'lodash-es'
import { DEFAULT_TITLE_STYLE } from '../editor/util/chart'
import { equalsAny, includesAny } from '../editor/util/StringUtils'
import { FeatureCollection } from '@antv/l7plot/dist/esm/plots/choropleth/types'
import { useMapStoreWithOut } from '@/store/modules/map'
import { getGeoJson } from '@/api/map'
import { toRaw } from 'vue'
import { Options } from '@antv/g2plot/esm'
import { PickOptions } from '@antv/g2plot/esm/core/plot'
import { innerExportDetails } from '@/api/chart'

// 同时支持将hex和rgb，转换成rgba
export function hexColorToRGBA(hex, alpha) {
  let rgb = [] // 定义rgb数组
  if (hex.indexOf('#') > -1) {
    if (/^\#[0-9A-F]{3}$/i.test(hex)) {
      // 判断传入是否为#三位十六进制数
      let sixHex = '#'
      hex.replace(/[0-9A-F]/gi, function (kw) {
        sixHex += kw + kw // 把三位16进制数转化为六位
      })
      hex = sixHex // 保存回hex
    }
    if (/^#[0-9A-F]{6}$/i.test(hex)) {
      // 判断传入是否为#六位十六进制数
      hex.replace(/[0-9A-F]{2}/gi, function (kw) {
        // eslint-disable-next-line no-eval
        rgb.push(eval('0x' + kw)) // 十六进制转化为十进制并存如数组
      })
      return `rgba(${rgb.join(',')},${alpha / 100})` // 输出RGB格式颜色
    } else {
      return 'rgb(0,0,0)'
    }
  } else {
    rgb = hex.match(/\d+/g)
    return `rgba(${rgb.join(',')},${alpha / 100})`
  }
}

export function digToHex(dig) {
  let prefix = ''
  const num = parseInt(dig * 2.55)
  if (num < 16) {
    prefix = '0'
  }
  return prefix.concat(num.toString(16).toUpperCase())
}

export function customSort(custom, data) {
  const indexArr = []
  const joinArr = []
  for (let i = 0; i < custom.length; i++) {
    const ele = custom[i]
    for (let j = 0; j < data.length; j++) {
      const d = data[j]
      if (ele === d.field) {
        joinArr.push(d)
        indexArr.push(j)
      }
    }
  }
  // 取得 joinArr 就是两者的交集
  const indexArrData = []
  for (let i = 0; i < data.length; i++) {
    indexArrData.push(i)
  }
  const indexResult = []
  for (let i = 0; i < indexArrData.length; i++) {
    if (indexArr.indexOf(indexArrData[i]) === -1) {
      indexResult.push(indexArrData[i])
    }
  }

  const subArr = []
  for (let i = 0; i < indexResult.length; i++) {
    subArr.push(data[indexResult[i]])
  }

  return joinArr.concat(subArr)
}

export function customColor(custom, res) {
  const result = []
  for (let i = 0; i < res.length; i++) {
    const r = res[i]
    let flag = false
    for (let j = 0; j < custom.length; j++) {
      const c = custom[j]
      if (r.name === c.name) {
        flag = true
        result.push(c)
      }
    }
    if (!flag) {
      result.push(r)
    }
  }
  return result
}

export function getColors(chart, colors, reset) {
  // 自定义颜色，先按照没有设定的情况，并排好序，当做最终结果
  let seriesColors = []
  let series
  if (chart.type.includes('stack')) {
    if (chart.data) {
      const data = chart.data.data
      const stackData = []
      for (let i = 0; i < data.length; i++) {
        const s = data[i]
        stackData.push(s.category)
      }
      const sArr = stackData.filter(function (item, index, stackData) {
        return stackData.indexOf(item, 0) === index
      })

      for (let i = 0; i < sArr.length; i++) {
        const s = sArr[i]
        seriesColors.push({
          name: s,
          color: colors[i % colors.length],
          isCustom: false
        })
      }
    }
  } else if (
    includesAny(chart.type, 'bar', 'scatter', 'radar', 'area') &&
    !chart.type.includes('group')
  ) {
    if (Object.prototype.toString.call(chart.yAxis) === '[object Array]') {
      series = JSON.parse(JSON.stringify(chart.yAxis))
    } else {
      series = JSON.parse(chart.yAxis)
    }
    if (series) {
      for (let i = 0; i < series.length; i++) {
        const s = series[i]
        seriesColors.push({
          name: s.name,
          color: colors[i % colors.length],
          isCustom: false
        })
      }
    }
  } else if (equalsAny(chart.type, 'bar-group', 'line')) {
    // 拿到data中的category，并去重，然后构建seriesColor
    if (chart.data) {
      const data = chart.data.data
      const s = []
      if (data) {
        data.forEach(cur => {
          if (s.indexOf(cur.category) < 0) {
            s.push(cur.category)
          }
        })
      }
      for (let i = 0; i < s.length; i++) {
        seriesColors.push({
          name: s[i],
          color: colors[i % colors.length],
          isCustom: false
        })
      }
    }
  } else {
    if (chart.data) {
      const data = chart.data.data
      for (let i = 0; i < data.length; i++) {
        const s = data[i]
        seriesColors.push({
          name: s.field,
          color: colors[i % colors.length],
          isCustom: false
        })
      }
    }
  }
  // 如果有自定义，则与上述中的结果合并。
  // res，custom，以custom为准，去掉res中不存在的，并将custom中name一样的color赋值给res，不存在的name，即新增值，使用i % colors.length，从配色方案中选
  if (!reset) {
    let sc = null
    if (Object.prototype.toString.call(chart.customAttr) === '[object Object]') {
      sc = JSON.parse(JSON.stringify(chart.customAttr)).color.seriesColors
    } else {
      sc = JSON.parse(chart.customAttr).color.seriesColors
    }
    if (sc && sc.length > 0) {
      seriesColors = customColor(sc, seriesColors)
    }
    // 根据isCustom字段，修正color
    for (let i = 0; i < seriesColors.length; i++) {
      if (!seriesColors[i].isCustom) {
        seriesColors[i].color = colors[i % colors.length]
      }
    }
  }
  return seriesColors
}

export function antVCustomColor(chart) {
  const colors = []
  if (chart.customAttr) {
    const customAttr = JSON.parse(JSON.stringify(chart.customAttr))
    // color
    if (customAttr.basicStyle) {
      const basicStyle = JSON.parse(JSON.stringify(customAttr.basicStyle))

      const customColors = getColors(chart, basicStyle.colors, false)
      for (let i = 0; i < customColors.length; i++) {
        colors.push(hexColorToRGBA(customColors[i].color, basicStyle.alpha))
      }
    }
  }
  return colors
}

export function getRemark(chart) {
  const remark = {}
  if (chart.customStyle) {
    const customStyle = JSON.parse(JSON.stringify(chart.customStyle))
    if (customStyle.text) {
      const title = JSON.parse(JSON.stringify(customStyle.text))
      remark.show = title.remarkShow ? title.remarkShow : DEFAULT_TITLE_STYLE.remarkShow
      remark.content = title.remark ? title.remark : DEFAULT_TITLE_STYLE.remark
      remark.bgFill = title.remarkBackgroundColor
        ? title.remarkBackgroundColor
        : DEFAULT_TITLE_STYLE.remarkBackgroundColor
    }
  }
  return remark
}

export const quotaViews = ['label', 'richTextView', 'indicator', 'gauge', 'liquid']

export function handleEmptyDataStrategy<O extends PickOptions>(chart: Chart, options: O): O {
  const { data } = options as unknown as Options
  if (!data?.length) {
    return options
  }
  const strategy = parseJson(chart.senior).functionCfg.emptyDataStrategy
  if (strategy === 'ignoreData') {
    handleIgnoreData(data)
    return options
  }
  const { yAxis, xAxisExt, extStack } = chart
  const multiDimension = yAxis?.length >= 2 || xAxisExt?.length > 0 || extStack?.length > 0
  switch (strategy) {
    case 'breakLine': {
      if (multiDimension) {
        // 多维度保持空
        handleBreakLineMultiDimension(data)
      }
      return {
        ...options,
        connectNulls: false
      }
    }
    case 'setZero': {
      if (multiDimension) {
        // 多维度置0
        handleSetZeroMultiDimension(data)
      } else {
        // 单维度置0
        handleSetZeroSingleDimension(data)
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

function handleSetZeroMultiDimension(data: Record<string, any>[]) {
  const dimensionInfoMap = new Map()
  const subDimensionSet = new Set()
  const quotaMap = new Map<string, { id: string }[]>()
  for (let i = 0; i < data.length; i++) {
    const item = data[i]
    if (item.value === null) {
      item.value = 0
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
          data.splice(dimensionInfo.index + insertCount + subInsertIndex, 0, {
            field,
            value: 0,
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

function handleSetZeroSingleDimension(data: Record<string, any>[]) {
  data.forEach(item => {
    if (item.value === null) {
      item.value = 0
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

export function resetRgbOpacity(sourceColor: string, times: number): string {
  if (sourceColor?.startsWith('rgb')) {
    const numbers = sourceColor.match(/(\d(\.\d+)?)+/g)
    if (numbers?.length === 4) {
      const opacity = parseFloat(numbers[3])
      if (isNumber(opacity)) {
        let resultOpacity = parseFloat((opacity * times).toFixed(2))
        if (resultOpacity > 1) {
          resultOpacity = 1
        }
        const colorArr = numbers.slice(0, 3).concat(resultOpacity.toString())
        return `rgba(${colorArr.join(',')})`
      }
    }
  }
  return sourceColor
}

export function parseJson<T>(str: T | JSONString<T>): T {
  if (typeof str !== 'string') {
    return str as T
  }
  return JSON.parse(str) as T
}

type FlowFunction<P, R> = (param: P, result: R, extra?: any[]) => R

export function flow<P, R>(...flows: FlowFunction<P, R>[]): FlowFunction<P, R> {
  return (param: P, result: R, extra?: any[]) => {
    return flows.reduce((result: R, flow: FlowFunction<P, R>) => {
      return flow(param, result, extra)
    }, result)
  }
}

export const isParent = (type: any, parentType: any) => {
  let _type = type
  while (_type) {
    if (_type === parentType) {
      return true
    }
    _type = _type.__proto__
  }
  return false
}

export const getGeoJsonFile = async (areaId: string): Promise<FeatureCollection> => {
  const mapStore = useMapStoreWithOut()
  let geoJson = mapStore.mapCache[areaId]
  if (!geoJson) {
    const res = await getGeoJson(areaId)
    geoJson = res.data
    mapStore.setMap({ id: areaId, geoJson })
  }
  return toRaw(geoJson)
}

export const exportExcelDownload = chart => {
  const fields = JSON.parse(JSON.stringify(chart.data.fields))
  const tableRow = JSON.parse(JSON.stringify(chart.data.tableRow))
  const excelHeader = fields.map(item => item.chartShowName ?? item.name)
  const excelTypes = fields.map(item => item.deType)
  const excelHeaderKeys = fields.map(item => item.dataeaseName)
  let excelData = tableRow.map(item => excelHeaderKeys.map(i => item[i]))
  const excelName = chart.title
  let detailFields = []
  if (chart.data.detailFields?.length) {
    detailFields = chart.data.detailFields.map(item => {
      return {
        name: item.name,
        deType: item.deType,
        dataeaseName: item.dataeaseName
      }
    })
    excelData = tableRow.map(item => {
      return excelHeaderKeys.map(i => {
        if (i === 'detail' && !item[i] && Array.isArray(item['details'])) {
          const arr = item['details']
          if (arr?.length) {
            return arr.map(ele => detailFields.map(field => ele[field.dataeaseName]))
          }
          return null
        }
        return item[i]
      })
    })
  }
  const request = {
    proxy: null,
    viewId: chart.id,
    viewName: excelName,
    header: excelHeader,
    details: excelData,
    excelTypes: excelTypes,
    excelHeaderKeys: excelHeaderKeys,
    viewInfo: chart,
    detailFields
  }
  const method = innerExportDetails
  method(request)
    .then(res => {
      const blob = new Blob([res.data], { type: 'application/vnd.ms-excel' })
      const link = document.createElement('a')
      link.style.display = 'none'
      link.href = URL.createObjectURL(blob)
      link.download = excelName + '.xlsx' // 下载的文件名
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
    })
    .catch(() => {
      console.error('Excel download error')
    })
}
