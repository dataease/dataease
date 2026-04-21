import {
  handleBreakLineMultiDimension,
  handleIgnoreData,
  handleSetZeroMultiDimension,
  handleSetZeroSingleDimension,
  parseJson
} from '@/views/chart/components/js/util'
import { Chart as G2Chart } from '@antv/g2'

/**
 * 运行时形态与 G2Spec 完全一致 ，G2 以普通对象消费
 */
export interface ChildSpec {
  axis?: Record<string, any>
  encode?: Record<string, any>
  scale?: Record<string, any>
  style?: Record<string, any>
  transform?: Array<Record<string, any>>
  labels?: any[]
  tooltip?: any
  interaction?: Record<string, any>
  data?: any
  [key: string]: any
}

export interface ViewSpec {
  type?: string
  children?: ChildSpec[]
  data?: any
  scale?: Record<string, any>
  theme?: Record<string, any>
  coordinate?: Record<string, any>
  title?: any
  legend?: Record<string, any>
  tooltip?: any
  interaction?: Record<string, any>
  annotations?: any[]
  [key: string]: any
}

export type Transform = {
  type: string
  [key: string]: any
}

export function handleEmptyDataStrategy<O extends ViewSpec>(chart: Chart, options: O): O {
  const childData = options.children?.[0]?.data
  const rootData = (options as any).data
  const data = childData ?? rootData
  if (!data?.length) return options
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
        handleBreakLineMultiDimension(data)
      }
      break
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

export function tooltipWrapperId(container: string) {
  return 'G2-TOOLTIP-WRAPPER-' + container
}

export function createTooltipWrapper(chart: Chart) {
  const wrapperId = tooltipWrapperId(chart.container)
  let g2TooltipWrapper = document.getElementById(wrapperId)
  if (!g2TooltipWrapper) {
    g2TooltipWrapper = document.createElement('div')
    g2TooltipWrapper.id = wrapperId
    g2TooltipWrapper.style.position = 'absolute'
    g2TooltipWrapper.style.pointerEvents = 'none'
    g2TooltipWrapper.style.zIndex = '2000'
    g2TooltipWrapper.style.top = '0px'
    document.body.appendChild(g2TooltipWrapper)
  }
  // 如果开启轮播则不使用自定义tooltip容器
  const customAttr = parseJson(chart.customAttr)
  return customAttr?.tooltip?.carousel?.enable ? undefined : g2TooltipWrapper
}

export function tooltipCss(tooltipAttr: DeepPartial<ChartTooltipAttr>) {
  return {
    '.g2-tooltip': {
      background: tooltipAttr.backgroundColor,
      'max-height': '50vh',
      'overflow-y': 'auto',
      position: 'fixed',
      'scrollbar-width': tooltipAttr.carousel.enable ? 'none !important' : 'auto'
    },
    '.g2-tooltip-title': {
      color: tooltipAttr.color,
      'font-size': `${tooltipAttr.fontSize}px`
    },
    '.g2-tooltip-list-item-name-label': {
      color: tooltipAttr.color,
      'font-size': `${tooltipAttr.fontSize}px`
    },
    '.g2-tooltip-list-item-value': {
      color: tooltipAttr.color,
      'font-size': `${tooltipAttr.fontSize}px`
    }
  }
}

/**
 * 计算 tooltip 最大高度
 * @param chart
 */
export function tooltipMaxHeight(chart: Chart) {
  const chartContainer = document.getElementById(chart.container)
  const defaultHeight = 80
  const chartRect = chartContainer?.getBoundingClientRect()
  let doubleHeight = chartRect.height * 2 - 20
  const customAttr = parseJson(chart.customAttr)
  if (customAttr?.tooltip?.carousel?.enable) {
    doubleHeight = chartRect.height / 1.2 - 20
  }
  const maxHeight = chartContainer ? Math.max(doubleHeight, defaultHeight) : defaultHeight
  return `max-height: ${maxHeight}px;max-width: ${chartRect.width / 2}px;`
}

export function listenerTooltipShow(newChart: G2Chart, chart: Chart) {
  newChart.on('tooltip:show', event => {
    const customAttr = parseJson(chart.customAttr)
    const isCarousel = customAttr?.tooltip?.carousel?.enable
    const tooltipWrapper = isCarousel
      ? document.getElementById(chart.container)
      : document.getElementById(tooltipWrapperId(chart.container))
    if (!tooltipWrapper) return
    tooltipWrapper.style.zIndex = chart.container.indexOf('viewDialog') > -1 ? '9999' : '2000'
    const allTooltips = tooltipWrapper?.querySelectorAll('.g2-tooltip')
    if (!allTooltips) return
    allTooltips.forEach(item => {
      const tooltip = item as HTMLElement
      const tooltipMouseleave = () => {
        tooltip.style.visibility = 'hidden'
      }
      tooltip.removeEventListener('mouseleave', tooltipMouseleave)
      tooltip.addEventListener('mouseleave', tooltipMouseleave)
      if (isCarousel) {
        tooltip.style.top = '0px'
      } else {
        const clientY = event?.client?.y
        if (!clientY) return
        if (clientY < tooltip.getBoundingClientRect().height) {
          tooltip.style.top = '0px'
        } else {
          tooltip.style.top = `${clientY - tooltip.getBoundingClientRect().height - 20}px`
        }
        const clientX = event.client?.x
        const targetDiv = document.getElementById(chart.container)
        if (!targetDiv || clientX == null) return

        const tooltipWidth = tooltip.getBoundingClientRect().width
        const left = clientX

        tooltip.style.left =
          left + tooltipWidth > targetDiv.getBoundingClientRect().right
            ? `${clientX - tooltipWidth - 20}px`
            : `${left + 20}px`
      }
    })
  })
}
