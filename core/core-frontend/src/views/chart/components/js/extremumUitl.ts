import { hexToRgba, parseJson } from '@/views/chart/components/js/util'
import { isEmpty } from 'lodash-es'
import { valueFormatter } from '@/views/chart/components/js/formatter'

/**
 * 判断 RGBA 颜色是亮色还是暗色
 * 使用加权灰度公式：0.299R + 0.587G + 0.114B
 * @param rgbaString - RGBA 颜色字符串，如 "rgba(255, 255, 255, 1)"
 * @param greyValue - 判断阈值，默认 128
 * @returns true 表示亮色，false 表示暗色；无效颜色返回 false
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
 * 从字符串中提取最后一个 rgba(...) 颜色值
 * 支持多个 rgba 出现，取最后一个
 * @param rgbaString - 包含 rgba 的字符串
 * @returns {r, g, b, a} 对象或 null
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

/**
 * 判断图表类型是否支持最值标注（极值标签）
 * @param chart - 图表配置对象
 * @returns boolean
 */
export const supportExtremumChartType = (chart): boolean => {
  return ['line', 'area', 'bar', 'bar-group', 'area-stack'].includes(chart.type)
}

/**
 * 极值标签事件处理：添加极值 HTML 标签并调整位置
 * @param newChart - G2 图表实例
 * @param chart - 当前图表配置
 * @param options - G2 图表选项
 * @param container - 容器 ID
 * @param scale - 图表缩放配置
 * @param isSeriesLabel - 是否启用系列级标签配置
 */
export const extremumEvt = (
  newChart,
  chart,
  options,
  container,
  scale = 1,
  isSeriesLabel = true
) => {
  const parent = document.getElementById(container)
  if (!parent || !supportExtremumChartType(chart)) return

  // 清理空 div
  Array.from(parent.querySelectorAll('div')).forEach(div => {
    if (div.innerHTML.trim() === '') div.remove()
  })

  const { label: labelAttr } = parseJson(chart.customAttr)
  const showExtremumIds = isSeriesLabel
    ? (labelAttr.seriesLabelFormatter || []).filter(item => item.showExtremum).map(item => item.id)
    : []

  // 若未启用极值显示，直接返回
  if (!(isSeriesLabel ? showExtremumIds.length : labelAttr.showExtremum)) return

  const formatterMap = (labelAttr.seriesLabelFormatter || []).reduce((map, item) => {
    map[item.id] = item
    return map
  }, {})

  // 这里获取 y 字段
  // 部分图表传过来的是options包含 children 的数组
  // 部分图表是children数组中的对象，line or bar
  const { y: yField } = options.encode
  const chartData = options.children ? options.children : [options]
  // 遍历所有 series，为标签注入 HTML 和样式
  chartData
    .filter(item => item.labels?.length)
    .forEach(item => {
      item.labels.forEach(label => {
        const oldPosition = label.position || 'top'
        label.style = {
          ...label.style,
          fill: data => {
            if (data.extremum) return ''
            const cfg = isSeriesLabel ? formatterMap[data.quotaList?.[0]?.id] : labelAttr
            return cfg?.color ?? '#000'
          },
          position: data => (data.extremum ? 'top' : oldPosition)
        }
        label.innerHTML = extremumHtml(chart, yField, isSeriesLabel)
      })
    })

  // 获取 point 大小，用于定位箭头
  let pointSize = 0
  options.children
    ?.filter(item => item.type === 'point')
    .forEach(item => {
      pointSize = Math.max(pointSize, item.encode?.size || 0)
    })

  const parentRect = parent?.getBoundingClientRect()
  // 渲染后调整极值标签位置，防止溢出
  newChart.on('afterrender', () => {
    document.querySelectorAll('.extremum-' + chart.container).forEach(item => {
      item.style.display = 'block'
      const itemRect = item.getBoundingClientRect()
      const childNode = item.childNodes[1] as HTMLElement
      // 判断是否顶部溢出
      if (itemRect.top < parentRect.top) {
        item.style.transform = `translate(-50%) translateY(${pointSize / scale + 10}px)`
        childNode.style.cssText += 'transform: translateX(-50%) rotate(180deg); top: -5px;'
      }
      // 判断是否右侧溢出
      if (itemRect.right > parentRect.right) {
        const currentLeft = parseFloat(window.getComputedStyle(item).left) || 0
        const newLeft = currentLeft - (itemRect.right - parentRect.right)
        item.style.left = `${newLeft}px`
        // childNode 反向偏移，保持始终指向在数据点上
        childNode.style.left = itemRect.width / 2 + Math.abs(newLeft) + 'px'
      }
      // 判断是否左侧溢出
      if (itemRect.left < parentRect.left) {
        const currentLeft = parseFloat(window.getComputedStyle(item).left) || 0
        const newLeft = currentLeft + (parentRect.left - itemRect.left)
        item.style.left = `${newLeft}px`
        // childNode 反向偏移，保持始终指向在数据点上
        childNode.style.left = itemRect.width / 2 - Math.abs(newLeft) + 'px'
      }
    })
  })
}

/**
 * 添加最大值和最小值文本标记
 * @param optionsChildren - 图表配置的子选项数组
 * @param showExtremumIds - 显示极值的 ID 列表
 * @param x - x 轴字段
 * @param y - y 轴字段
 * @param color - 颜色字段
 * @param isSeriesLabel - 是否启用系列级标签配置
 */
export const addExtremumText = (
  optionsChildren,
  showExtremumIds,
  x,
  y,
  color,
  isSeriesLabel = true
) => {
  addText(optionsChildren, showExtremumIds, x, y, color, 'max', isSeriesLabel)
  addText(optionsChildren, showExtremumIds, x, y, color, 'min', isSeriesLabel)
}

/**
 * 该方法计算图表数据的最大最小值，然后标记为极值（全量数据）
 * 使用 G2 transform 添加极值
 * selector: 'max' | 'min'
 */
const addText = (
  optionsChildren,
  showExtremumIds,
  xField,
  yField,
  colorField,
  selector,
  isSeriesLabel
) => {
  optionsChildren.push({
    type: 'text',
    encode: {
      x: xField,
      y: yField,
      color: colorField,
      series: colorField
    },
    style: {
      textAlign: 'center',
      background: true,
      backgroundFill: obj => {
        // 标记极值
        obj.extremum = isSeriesLabel
          ? obj.quotaList?.some?.(item => showExtremumIds.includes(item.id))
          : true
        return ''
      }
    },
    transform: [{ type: 'selectY', selector }],
    tooltip: false
  })
}

/**
 * 生成极值标签的 HTML 内容
 */
const extremumHtml = (chart, yField, isSeriesLabel) => {
  const { label: labelAttr, basicStyle } = parseJson(chart.customAttr)
  const formatterMap = (labelAttr.seriesLabelFormatter || []).reduce((map, item) => {
    map[item.id] = item
    return map
  }, {})

  return (obj, _, __, d) => {
    if (!obj.extremum) return ''
    const cfg = isSeriesLabel ? formatterMap[obj.quotaList?.[0]?.id] : labelAttr
    const formatter = isSeriesLabel ? cfg?.formatterCfg : labelAttr.labelFormatter
    const fontSize = cfg?.fontSize || 12
    const rawColor = d.element.__data__.color || '#000'
    const bgColor = rawColor.startsWith('#')
      ? hexToRgba(rawColor, basicStyle.alpha / 100)
      : getRgbaColorLastRgba(rawColor)
    const textColor = isColorLight(rawColor) ? '#000' : '#fff'
    const textContent = valueFormatter(obj[yField], formatter)
    const { r, g, b, a } = bgColor
    const color = `${r},${g},${b},${a}`
    return `
      <div class="extremum-${chart.container}" style="
        position: relative;
        font-size: ${fontSize}px;
        transform: translate(-50%, -100%) translateY(-5px);
        padding: 4px 5px;
        border-radius: 2px;
        color: ${textColor};
        background: rgba(${color});
      ">
        ${textContent}
        <span style="
          position: absolute;
          bottom: -4.8px;
          left: 50%;
          transform: translateX(-50%);
          width: 0; height: 0;
          border-top: 5px solid rgba(${color});
          border-left: 4px solid transparent;
          border-right: 4px solid transparent;
          border-bottom: 0;
        "></span>
      </div>
    `
  }
}
