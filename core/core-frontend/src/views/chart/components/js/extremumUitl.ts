import { hexToRgba, parseJson } from '@/views/chart/components/js/util'
import { isEmpty } from 'lodash-es'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { register, SelectY } from '@antv/g2'
import type { Canvas, DisplayObject } from '@antv/g'

// 以 HTML 回调关联最值选择器，Spec 的浅拷贝不会丢失关联，也不向 G2 注入额外样式字段
const extremumLabelSelectors = new WeakMap<object, (data: unknown[]) => unknown[]>()

export const getExtremumLabelData = (labels: Record<string, any>[], data: unknown[]) => {
  const result = new Set<unknown>()
  const selectors = new Set(labels.map(label => extremumLabelSelectors.get(label.innerHTML)))
  selectors.forEach(selector => {
    selector?.(data).forEach(datum => result.add(datum))
  })
  return result
}

const isExtremumLabel = (label: DisplayObject) => !!label.attributes.datum?.extremum

const setLabelVisibility = (label: DisplayObject['children'][number], visible: boolean) => {
  label.style.visibility = visible ? 'visible' : 'hidden'
  label.children.forEach(child => setLabelVisibility(child, visible))
}

const extremumOverlapHide =
  ({ priority }) =>
  (labels: DisplayObject[]) => {
    const ordered = [...labels].sort(
      (a, b) => Number(isExtremumLabel(b)) - Number(isExtremumLabel(a)) || priority?.(a, b) || 0
    )
    const visibleBounds: ReturnType<DisplayObject['getLocalBounds']>[] = []
    ordered.forEach(label => {
      setLabelVisibility(label, true)
      const bounds = label.getLocalBounds()
      // 最值始终参与占位且不被隐藏，普通标签沿用矩形重叠判定
      const visible =
        isExtremumLabel(label) ||
        !visibleBounds.some(
          other =>
            bounds.min[0] < other.max[0] &&
            bounds.max[0] > other.min[0] &&
            bounds.min[1] < other.max[1] &&
            bounds.max[1] > other.min[1]
        )
      if (visible) visibleBounds.push(bounds)
      else setLabelVisibility(label, false)
    })
    return labels
  }

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

const EXTREMUM_LABEL_MARK_TYPES = ['point', 'interval']

// 图例筛选会读取 transform.type 字符串，使用注册名称兼容其转换链
register('transform.deBarExtremum', ({ showExtremumIds, isSeriesLabel }) => {
  const selectors = (['max', 'min'] as const).map(selector => SelectY({ selector }))
  const transform: ReturnType<typeof SelectY> = (indices, mark, context) => {
    // 每次转换清理旧标记，避免刷新数据或筛选后保留上一次的最值
    mark.data.forEach(datum => delete datum.extremum)
    const extremumMark = {
      ...mark,
      encode: { ...mark.encode, series: mark.encode.color }
    }
    // 只标记数据，不添加 text 图元，保留 G2 柱状图按维度区域命中 tooltip 的行为
    selectors.forEach(select => {
      const [selected] = select(indices, extremumMark, context) as [number[], unknown]
      selected.forEach(index => {
        const datum = mark.data[index]
        if (
          datum &&
          (!isSeriesLabel || datum.quotaList?.some(item => showExtremumIds.includes(item.id)))
        ) {
          datum.extremum = true
        }
      })
    })
    return [indices, mark]
  }
  return transform
})

export const getBarExtremumTransform = (showExtremumIds: string[], isSeriesLabel = true) => ({
  type: 'deBarExtremum',
  showExtremumIds,
  isSeriesLabel
})

/**
 * 判断图表类型是否支持最值标注（极值标签）
 * @param chart - 图表配置对象
 * @returns boolean
 */
export const supportExtremumChartType = (chart): boolean => {
  return ['line', 'area', 'bar', 'bar-group', 'area-stack'].includes(chart.type)
}

const EXTREMUM_ARROW_HEIGHT = 5

const getSvgExtremumPath = (width: number, height: number, anchorX: number, below: boolean) => {
  const radius = Math.min(2, width / 4, height / 2)
  const halfArrowWidth = Math.min(4, (width - 2 * radius) / 2)
  const arrowX = Math.max(
    radius + halfArrowWidth,
    Math.min(anchorX, width - radius - halfArrowWidth)
  )
  const top = below ? EXTREMUM_ARROW_HEIGHT : 0
  const bottom = top + height
  // 矩形和箭头共用一条外轮廓，只填充一次，消除拼接缝及半透明背景的重叠色差
  return [
    `M ${radius} ${top}`,
    below ? `H ${arrowX - halfArrowWidth} L ${arrowX} 0 L ${arrowX + halfArrowWidth} ${top}` : '',
    `H ${width - radius} A ${radius} ${radius} 0 0 1 ${width} ${top + radius}`,
    `V ${bottom - radius} A ${radius} ${radius} 0 0 1 ${width - radius} ${bottom}`,
    below
      ? ''
      : `H ${arrowX + halfArrowWidth} L ${arrowX} ${bottom + EXTREMUM_ARROW_HEIGHT} L ${
          arrowX - halfArrowWidth
        } ${bottom}`,
    `H ${radius} A ${radius} ${radius} 0 0 1 0 ${bottom - radius}`,
    `V ${top + radius} A ${radius} ${radius} 0 0 1 ${radius} ${top} Z`
  ].join(' ')
}

const getExtremumSize = (item: HTMLElement) => {
  // 使用未缩放的 CSS 小数尺寸，避免取整后背景与文字边界错位
  const style = getComputedStyle(item)
  const borderBox = style.boxSizing === 'border-box'
  const width =
    parseFloat(style.width) +
    (borderBox
      ? 0
      : parseFloat(style.paddingLeft) +
        parseFloat(style.paddingRight) +
        parseFloat(style.borderLeftWidth) +
        parseFloat(style.borderRightWidth))
  const height =
    parseFloat(style.height) +
    (borderBox
      ? 0
      : parseFloat(style.paddingTop) +
        parseFloat(style.paddingBottom) +
        parseFloat(style.borderTopWidth) +
        parseFloat(style.borderBottomWidth))
  return { width, height }
}

const setExtremumBackground = (
  item: HTMLElement,
  span: HTMLElement,
  width: number,
  height: number,
  anchorX: number,
  below: boolean
) => {
  const parent = item.parentElement
  let background = parent.querySelector<SVGSVGElement>('svg[data-extremum-background]')
  if (!background) {
    const namespace = 'http://www.w3.org/2000/svg'
    background = document.createElementNS(namespace, 'svg')
    background.setAttribute('xmlns', namespace)
    background.setAttribute('data-extremum-background', '')
    background.setAttribute('aria-hidden', 'true')
    background.style.cssText = 'position:absolute;left:0;top:0;z-index:0;pointer-events:none'
    background.appendChild(document.createElementNS(namespace, 'path'))
    parent.insertBefore(background, item)
  }
  const totalHeight = height + EXTREMUM_ARROW_HEIGHT
  background.setAttribute('width', String(width))
  background.setAttribute('height', String(totalHeight))
  background.setAttribute('viewBox', `0 0 ${width} ${totalHeight}`)
  const path = background.firstElementChild
  path.setAttribute('d', getSvgExtremumPath(width, height, anchorX, below))
  path.setAttribute('fill', span.style.borderTopColor)
  path.setAttribute('stroke', 'none')
  // Canvas 与 SVG 的 HTML 标签共用单路径背景，文字不应用额外透明度
  item.style.background = 'transparent'
  item.style.zIndex = '1'
  span.style.display = 'none'
  return background
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
  const { y: yField, color: colorField } = options.encode
    ? options.encode
    : options.children[0].encode
  const chartData = options.children ? options.children : [options]
  const selectExtremumData = (data: Record<string, any>[]) => {
    const indices = data.map((_, index) => index)
    const mark = {
      encode: {
        y: { value: data.map(datum => datum[yField]) },
        series: { value: data.map(datum => datum[colorField]) }
      }
    }
    // 采样发生在 G2 标记 extremum 之前，复用 selectY 保持分系列、空值和并列值语义一致
    return (['max', 'min'] as const).flatMap(selector => {
      const [selected] = SelectY({ selector })(indices, mark as any, newChart.getContext()) as [
        number[],
        unknown
      ]
      return selected
        .map(index => data[index])
        .filter(
          datum =>
            datum &&
            (!isSeriesLabel || datum.quotaList?.some(item => showExtremumIds.includes(item.id)))
        )
    })
  }
  let layoutFrame: number | undefined
  let layoutCanvas: Canvas
  const scheduleExtremumPosition = (labels: DisplayObject[], { canvas }: { canvas: Canvas }) => {
    if (layoutFrame !== undefined) layoutCanvas.cancelAnimationFrame(layoutFrame)
    layoutCanvas = canvas
    // 图例和滑块只重绘局部视图，标签转换后等待 DOM 落地再恢复 HTML 尺寸与位置
    layoutFrame = canvas.requestAnimationFrame(() => {
      layoutFrame = canvas.requestAnimationFrame(() => {
        layoutFrame = undefined
        setExtremumPosition()
      })
    })
    return labels
  }
  // 遍历所有 series，为标签注入 HTML 和样式
  chartData
    .filter(item => item.labels?.length && EXTREMUM_LABEL_MARK_TYPES.includes(item.type))
    .forEach(item => {
      item.labels.forEach(label => {
        const oldPosition = label.position || 'top'
        const oldFill = label.style?.fill
        label.style = {
          ...label.style,
          // 全量标签和避让标签都让最值置顶，避免后绘制的普通标签遮挡气泡
          zIndex: data => (data.extremum ? 1 : 0),
          fill: data => {
            if (data.extremum) return ''
            if (typeof oldFill === 'function') {
              return oldFill(data)
            }
            if (oldFill) {
              return oldFill
            }
            const cfg = isSeriesLabel ? formatterMap[data.quotaList?.[0]?.id] : labelAttr
            return cfg?.color ?? '#000'
          },
          position: data => (data.extremum ? 'top' : oldPosition)
        }
        label.innerHTML = extremumHtml(chart, yField, isSeriesLabel)
        extremumLabelSelectors.set(label.innerHTML, selectExtremumData)
        label.transform = [
          ...(label.transform || []).map(transform =>
            transform.type === 'overlapHide'
              ? { ...transform, type: extremumOverlapHide }
              : transform
          ),
          { type: () => scheduleExtremumPosition }
        ]
      })
    })

  // 获取 point 大小，用于定位箭头
  let pointSize = 0
  options.children
    ?.filter(item => item.type === 'point')
    .forEach(item => {
      pointSize = Math.max(pointSize, item.encode?.size || 0)
    })

  const setExtremumPosition = () => {
    const chartScale = scale || 1
    document.querySelectorAll<HTMLElement>('.extremum-' + chart.container)?.forEach(item => {
      item.style.display = 'block'
      const parentElement = item.parentElement
      const parentParentElement = parentElement?.parentElement
      const spanElement = parentElement?.querySelector<HTMLElement>('span')
      if (!parentElement || !parentParentElement || !spanElement) return
      const foreignObject = item.closest<SVGForeignObjectElement>('foreignObject')
      if (foreignObject) {
        // SVG 中的 HTML 需要显式尺寸，定位边界使用图表容器而非 foreignObject
        const containerRect = parent.getBoundingClientRect()
        if (!containerRect.width || !containerRect.height) return
        // 每次从 G2 给出的锚点重新布局，避免缩放、重绘时叠加上次的偏移
        foreignObject.setAttribute('x', '0')
        foreignObject.setAttribute('y', '0')
        const matrix = foreignObject.getScreenCTM()
        if (!matrix || matrix.a * matrix.d - matrix.b * matrix.c === 0) return
        item.style.width = 'max-content'
        item.style.whiteSpace = 'nowrap'
        item.style.transform = 'none'
        const { width, height } = getExtremumSize(item)
        if (!width || !height) return

        // 将容器边界换算到标签坐标系，同时兼容横纵比例不同的大屏缩放
        const inverse = matrix.inverse()
        const topLeft = new DOMPoint(containerRect.left, containerRect.top).matrixTransform(inverse)
        const bottomRight = new DOMPoint(containerRect.right, containerRect.bottom).matrixTransform(
          inverse
        )
        const arrowHeight = EXTREMUM_ARROW_HEIGHT
        const totalHeight = height + arrowHeight
        const below = -totalHeight < topLeft.y && bottomRight.y > -topLeft.y
        const x = Math.max(topLeft.x, Math.min(-width / 2, bottomRight.x - width))
        const y = Math.max(
          topLeft.y,
          Math.min(below ? pointSize + 5 : -totalHeight, bottomRight.y - totalHeight)
        )
        foreignObject.setAttribute('width', String(width))
        foreignObject.setAttribute('height', String(totalHeight))
        foreignObject.setAttribute('x', String(x))
        foreignObject.setAttribute('y', String(y))
        // 气泡和箭头都放进有效区域，避免图片、PDF 截图依赖溢出内容
        parentElement.style.position = 'relative'
        parentElement.style.width = `${width}px`
        parentElement.style.height = `${totalHeight}px`
        item.style.top = below ? `${arrowHeight}px` : '0px'
        setExtremumBackground(item, spanElement, width, height, -x, below)
        return
      }
      const { width, height } = getExtremumSize(item)
      if (!width || !height) return
      const itemRect = item.getBoundingClientRect()
      const itemParentRect = parentElement.getBoundingClientRect()
      // 判断是否顶部溢出
      const itemParentParentRect = parentParentElement.getBoundingClientRect()
      const positionScaleX =
        parentParentElement.offsetWidth > 0
          ? itemParentParentRect.width / parentParentElement.offsetWidth
          : chartScale
      const toLocalX = (value: number) => value / (positionScaleX || 1)
      const overflowRight = itemParentRect.left + itemRect.width / 2 - itemParentParentRect.right
      // 保留初始百分比居中，仅在右侧溢出时额外左移
      const translateX = overflowRight > 0 ? `calc(-50% - ${toLocalX(overflowRight)}px)` : '-50%'
      const below = itemParentRect.top - itemParentParentRect.top <= itemParentRect.height
      const anchorX = width / 2 + (overflowRight > 0 ? toLocalX(overflowRight) : 0)
      const background = setExtremumBackground(item, spanElement, width, height, anchorX, below)
      // 保留 Canvas 文字定位，完整背景比矩形多出箭头高度，分别补偿上下偏移
      if (!below) {
        item.style.transform = `translate(${translateX}, -100%) translateY(-5px)`
        background.style.transform = `translate(${translateX}, -100%)`
      } else {
        item.style.transform = `translate(${translateX}, ${pointSize / chartScale + 5 * 2}px)`
        background.style.transform = `translate(${translateX}, ${
          pointSize / chartScale + EXTREMUM_ARROW_HEIGHT
        }px)`
      }
      item.style.right = ''
    })
  }
  newChart.on('afterchangesize', () => {
    setExtremumPosition()
  })
  newChart.on('afterrender', () => {
    setExtremumPosition()
  })
  newChart.on('beforedestroy', () => {
    if (layoutFrame !== undefined) layoutCanvas.cancelAnimationFrame(layoutFrame)
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
      </div>
      <span style="
          position: absolute;
          top: -6px;
          left: -4px;
          width: 0; height: 0;
          border-top: 5px solid rgba(${color});
          border-left: 4px solid transparent;
          border-right: 4px solid transparent;
        "></span>
    `
  }
}
