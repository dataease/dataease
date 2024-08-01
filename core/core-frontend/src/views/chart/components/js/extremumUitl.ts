import { valueFormatter } from '@/views/chart/components/js/formatter'
import { parseJson } from '@/views/chart/components/js/util'
import { isEmpty } from 'lodash-es'

export const clearExtremum = chart => {
  // 清除图表标注
  const pointElement = document.getElementById(chartPointParentId(chart))
  if (pointElement) {
    pointElement.remove()
    pointElement.parentNode?.removeChild(pointElement)
  }
}

/**
 * 判断给定的RGBA字符串表示的颜色是亮色还是暗色
 * 通过计算RGB颜色值的加权平均值（灰度值），判断颜色的明暗
 * 如果给定的字符串不包含有效的RGBA值，则原样返回该字符串
 *
 * @param rgbaString 一个RGBA颜色字符串，例如 "rgba(255, 255, 255, 1)"
 * @param greyValue 灰度值默认128
 * @returns 如果计算出的灰度值大于等于128，则返回true，表示亮色；否则返回false，表示暗色。
 *          如果rgbaString不包含有效的RGBA值，则返回原字符串
 */
const isColorLight = (rgbaString: string, greyValue = 128) => {
  const lastRGBA = getRgbaColorLastRgba(rgbaString)
  if (!isEmpty(lastRGBA)) {
    // 计算灰度值的公式
    const grayLevel = lastRGBA.r * 0.299 + lastRGBA.g * 0.587 + lastRGBA.b * 0.114
    return grayLevel >= greyValue
  } else {
    return false
  }
}

/**
 * 从给定的rgba颜色字符串中提取最后一个rgba值
 * @param rgbaString 包含一个或多个rgba颜色值的字符串
 * @returns 返回最后一个解析出的rgba对象，如果未找到rgba值，则返回null
 */
const getRgbaColorLastRgba = (rgbaString: string) => {
  const rgbaPattern = /rgba\((\d+),\s*(\d+),\s*(\d+),\s*([\d.]+)\)/g
  let match: string[]
  let lastRGBA = null
  while ((match = rgbaPattern.exec(rgbaString)) !== null) {
    const r = parseInt(match[1])
    const g = parseInt(match[2])
    const b = parseInt(match[3])
    const a = parseFloat(match[4])
    lastRGBA = { r, g, b, a }
  }
  return lastRGBA
}

function createExtremumDiv(id, value, formatterCfg, chart) {
  // 空值不处理
  if (!value && value != 0) {
    return
  }
  // 装标注的div
  const parentElement = document.getElementById(chartPointParentId(chart))
  if (parentElement) {
    // 标注div
    const element = document.getElementById(id)
    if (element) {
      return
    }
    const div = document.createElement('div')
    div.id = id
    div.className = 'child'
    div.setAttribute(
      'style',
      `width: auto;
        height: auto;
        border-radius: 2px;
        position: relative;
        padding: 4px 5px 4px 5px;
        display:none;
        transform: translateX(-50%);
        white-space:nowrap;`
    )
    div.textContent = valueFormatter(value, formatterCfg)
    const span = document.createElement('span')
    span.setAttribute(
      'style',
      `display: block;
        width: 0px;
        height: 0px;
        border: 4px solid transparent;
        border-top-color: red;
        position: absolute;
        left: calc(50% - 4px);
        margin-top:4px;`
    )
    div.appendChild(span)
    parentElement.appendChild(div)
  }
}
/**
 * 没有子类别字段的图表
 * @param chart
 */
const noChildrenFieldChart = chart => {
  return ['area', 'bar'].includes(chart.type)
}

const chartContainerId = chart => {
  return chart.container + '_'
}

const chartPointParentId = chart => {
  return chart.container + '_point_' + chart.id + '_'
}

const overlap = chart => {
  const container = document.getElementById(chartPointParentId(chart))
  const children = Array.from(container.getElementsByClassName('child'))

  function getOverlapArea(rect1, rect2) {
    const x_overlap = Math.max(
      0,
      Math.min(rect1.right, rect2.right) - Math.max(rect1.left, rect2.left)
    )
    const y_overlap = Math.max(
      0,
      Math.min(rect1.bottom, rect2.bottom) - Math.max(rect1.top, rect2.top)
    )
    return x_overlap * y_overlap
  }

  function checkAndHideOverlappedElements() {
    children.forEach(child => {
      const childRect = child.getBoundingClientRect()
      let totalOverlapArea = 0

      children.forEach(otherChild => {
        if (child !== otherChild) {
          const otherChildRect = otherChild.getBoundingClientRect()
          totalOverlapArea += getOverlapArea(childRect, otherChildRect)
        }
      })

      const childArea = childRect.width * childRect.height
      if (totalOverlapArea / childArea > 0.3) {
        child.parentNode?.removeChild(child)
      }
    })
  }

  checkAndHideOverlappedElements()
}

export const extremumEvt = (newChart, chart, _options, container) => {
  chart.container = container
  const { label: labelAttr } = parseJson(chart.customAttr)
  const { yAxis } = parseJson(chart)
  newChart.once('beforerender', ev => {
    ev.view.on('beforepaint', () => {
      newChart.chart.geometries[0]?.beforeMappingData.forEach(i => {
        i.forEach(item => {
          delete item._origin.EXTREME
        })
        const { minItem, maxItem } = findMinMax(i.filter(item => item._origin.value))
        if (!minItem || !maxItem) {
          return
        }
        let showExtremum = false
        if (noChildrenFieldChart(chart) || yAxis.length > 1) {
          const seriesLabelFormatter = labelAttr.seriesLabelFormatter.find(
            d => d.name === minItem._origin.category || d.name === maxItem._origin.category
          )
          showExtremum = seriesLabelFormatter?.showExtremum
        } else {
          if (['bar-group'].includes(chart.type)) {
            showExtremum = labelAttr.showExtremum
          } else {
            showExtremum = labelAttr.seriesLabelFormatter[0]?.showExtremum
          }
        }
        if (showExtremum) {
          minItem._origin.EXTREME = true
          maxItem._origin.EXTREME = true
        }
      })
    })
    newChart.chart.geometries[0].on('afteranimate', () => {
      createExtremumPoint(chart, ev)
      overlap(chart)
    })
  })
  newChart.on('legend-item:click', ev => {
    const legendShowSize = ev.view
      .getController('legend')
      .components[0].component.cfg.items.filter(l => !l.unchecked)
    if (legendShowSize.length === 0) {
      const allElement = document.getElementById(chartPointParentId(chart))
      if (allElement && allElement.childNodes) {
        allElement.childNodes.forEach(c => {
          c.style.display = 'none'
        })
      }
    }
  })
}

const findMinMax = (data): { minItem; maxItem } => {
  return data.reduce(
    ({ minItem, maxItem }, currentItem) => {
      if (minItem === undefined || currentItem._origin.value < minItem._origin.value) {
        minItem = currentItem
      }
      if (maxItem === undefined || currentItem._origin.value > maxItem._origin.value) {
        maxItem = currentItem
      }
      delete minItem?._origin.EXTREME
      delete maxItem?._origin.EXTREME
      return { minItem, maxItem }
    },
    { minItem: undefined, maxItem: undefined }
  )
}
export const createExtremumPoint = (chart, ev) => {
  // 获取标注样式
  const { label: labelAttr, basicStyle } = parseJson(chart.customAttr)
  const pointSize = basicStyle.lineSymbolSize
  const { yAxis } = parseJson(chart)
  clearExtremum(chart)
  // 创建标注父元素
  const divParentElement = document.getElementById(chartPointParentId(chart))
  if (!divParentElement) {
    const divParent = document.createElement('div')
    divParent.id = chartPointParentId(chart)
    divParent.style.position = 'fixed'
    divParent.style.zIndex = '1'
    // 将父标注加入到图表中
    const containerElement = document.getElementById(chart.container)
    containerElement.insertBefore(divParent, containerElement.firstChild)
  }
  let geometriesDataArray = []
  // 获取数据点
  const intervalPoint = ev.view
    .getGeometries()
    .find((intervalItem: { type: string }) => intervalItem.type === 'interval')
  if (intervalPoint) {
    geometriesDataArray = intervalPoint.dataArray
  }
  const pointPoint = ev.view
    .getGeometries()
    .find((pointItem: { type: string }) => pointItem.type === 'point')
  if (pointPoint) {
    geometriesDataArray = pointPoint.dataArray
  }
  geometriesDataArray?.forEach(pointObjList => {
    if (pointObjList && pointObjList.length > 0) {
      const pointObj = pointObjList[0]
      const [minItem, maxItem] = pointObjList.filter(i => i._origin.EXTREME)
      let attr
      let showExtremum = false
      if (noChildrenFieldChart(chart) || yAxis.length > 1) {
        const seriesLabelFormatter = labelAttr.seriesLabelFormatter.find(
          d => d.name === pointObj._origin.category
        )
        showExtremum = seriesLabelFormatter?.showExtremum
        attr = seriesLabelFormatter
      } else {
        if (['bar-group'].includes(chart.type)) {
          showExtremum = labelAttr.showExtremum
        } else {
          showExtremum = labelAttr.seriesLabelFormatter[0]?.showExtremum
          attr = labelAttr.seriesLabelFormatter[0]
        }
      }
      const fontSize = attr ? attr.fontSize : labelAttr.fontSize
      if (!minItem) {
        return
      }
      const maxKey =
        chartContainerId(chart) +
        chartPointParentId(chart) +
        pointObj._origin.category +
        '_' +
        (maxItem ? maxItem._origin.value : minItem._origin.value)
      const minKey =
        chartContainerId(chart) +
        chartPointParentId(chart) +
        pointObj._origin.category +
        '_' +
        minItem._origin.value
      // 最值标注
      if (showExtremum && labelAttr.show) {
        if (maxItem) {
          createExtremumDiv(
            maxKey,
            maxItem._origin.value,
            attr ? attr.formatterCfg : labelAttr.labelFormatter,
            chart
          )
        }
        createExtremumDiv(
          minKey,
          minItem._origin.value,
          attr ? attr.formatterCfg : labelAttr.labelFormatter,
          chart
        )
        pointObjList.forEach(point => {
          const pointElement = document.getElementById(
            chartContainerId(chart) +
              chartPointParentId(chart) +
              point._origin.category +
              '_' +
              point._origin.value
          )
          if (pointElement && point._origin.EXTREME) {
            pointElement.style.position = 'absolute'
            pointElement.style.position = 'absolute'
            pointElement.style.top =
              (point.y[1] ? point.y[1] : point.y) -
              (fontSize + (pointSize ? pointSize : 0) + 12) +
              'px'
            pointElement.style.left = point.x + 'px'
            pointElement.style.zIndex = '10'
            pointElement.style.fontSize = fontSize + 'px'
            pointElement.style.lineHeight = fontSize + 'px'
            // 渐变颜色时需要获取最后一个rgba的值作为背景
            const { r, b, g, a } = getRgbaColorLastRgba(point.color)
            pointElement.style.backgroundColor = 'rgba(' + r + ',' + g + ',' + b + ',' + a + ')'
            pointElement.style.color = isColorLight(point.color) ? '#000' : '#fff'
            pointElement.children[0]['style'].borderTopColor =
              'rgba(' + r + ',' + g + ',' + b + ',' + a + ')'
            pointElement.style.display = 'table'
          }
        })
      } else {
        removeDivElement(maxKey)
        removeDivElement(minKey)
      }
    }
  })

  function removeDivElement(key) {
    const element = document.getElementById(key)
    if (element) {
      element.remove()
      element.parentNode?.removeChild(element)
    }
  }
}
