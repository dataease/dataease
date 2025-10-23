import {
  handleBreakLineMultiDimension,
  handleIgnoreData,
  handleSetZeroMultiDimension,
  handleSetZeroSingleDimension,
  parseJson
} from '@/views/chart/components/js/util'
import { Chart as G2Chart, G2Spec } from '@antv/g2'

export type ViewSpec = { children?: G2Spec[]; [key: string]: any } & G2Spec
export type Transform = {
  type: string
  [key: string]: any
}

export function handleEmptyDataStrategy<O extends ViewSpec>(chart: Chart, options: O): O {
  const { data } = options.children[0]
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
  return g2TooltipWrapper
}

export function tooltipCss(tooltipAttr: DeepPartial<ChartTooltipAttr>) {
  return {
    '.g2-tooltip': {
      background: tooltipAttr.backgroundColor,
      'max-height': '50vh',
      'overflow-y': 'auto',
      position: 'fixed'
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

export function listenerTooltipShow(newChart: G2Chart, chart: Chart) {
  newChart.on('tooltip:show', event => {
    const tooltipWrapper = document.getElementById(tooltipWrapperId(chart.container))
    const allTooltips = tooltipWrapper?.querySelectorAll('.g2-tooltip')
    if (!allTooltips) return
    allTooltips.forEach(item => {
      const tooltip = item as HTMLElement
      const tooltipMouseleave = () => {
        tooltip.style.visibility = 'hidden'
      }
      tooltip.removeEventListener('mouseleave', tooltipMouseleave)
      tooltip.addEventListener('mouseleave', tooltipMouseleave)
      // 调整 tooltip z-index 和位置
      tooltipWrapper.style.zIndex = chart.container.indexOf('viewDialog') > -1 ? '9999' : '2000'
      const clientY = event.client?.y
      if (!clientY) return
      if (clientY < tooltip.getBoundingClientRect().height) {
        tooltip.style.top = '0px'
      } else {
        tooltip.style.top = `${clientY - tooltip.getBoundingClientRect().height}px`
      }
    })
  })
}
