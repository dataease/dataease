import { BASE_CHART, BASE_CHART_STRING } from '@/views/chart/chart/chart'
import { deepCopy } from '@/components/canvas/utils/utils'

export const DEFAULT_PANEL_STYLE = {
  color: '#ffffff',
  imageUrl: null,
  backgroundType: 'image',
  gap: 'no'
}

export const CANVAS_STYLE = {
  width: 1600,
  height: 900,
  scale: 100,
  scaleWidth: 100,
  scaleHeight: 100,
  selfAdaption: true,
  auxiliaryMatrix: true,
  openCommonStyle: true,
  panel: DEFAULT_PANEL_STYLE
}

export const DEFAULT_COMMON_CANVAS_STYLE_STRING = {
  ...CANVAS_STYLE,
  chart: BASE_CHART_STRING
}

export function chartTransStr2Object(targetIn, copy) {
  const target = copy === 'Y' ? deepCopy(targetIn) : targetIn
  if (target.chart) {
    if (target.chart.xaxis && typeof target.chart.xaxis === 'string') {
      target.chart.xaxis = JSON.parse(target.chart.xaxis)
    }
    if (target.chart.yaxis && typeof target.chart.yaxis === 'string') {
      target.chart.yaxis = JSON.parse(target.chart.yaxis)
    }
    if (target.chart.customAttr && typeof target.chart.customAttr === 'string') {
      target.chart.customAttr = JSON.parse(target.chart.customAttr)
    }
    if (target.chart.customStyle && typeof target.chart.customStyle === 'string') {
      target.chart.customStyle = JSON.parse(target.chart.customStyle)
    }
    if (target.chart.customFilter && typeof target.chart.customFilter === 'string') {
      target.chart.customFilter = JSON.parse(target.chart.customFilter)
    }
  }
  return target
}

export function chartTransObject2Str(targetIn, deepCopy) {
  const target = copy === 'Y' ? deepCopy(targetIn) : targetIn
  if (target.chart) {
    if (target.chart.xaxis && typeof target.chart.xaxis !== 'string') {
      target.chart.xaxis = JSON.stringify(target.chart.xaxis)
    }
    if (target.chart.yaxis && typeof target.chart.yaxis !== 'string') {
      target.chart.yaxis = JSON.stringify(target.chart.yaxis)
    }
    if (target.chart.customAttr && typeof target.chart.customAttr !== 'string') {
      target.chart.customAttr = JSON.stringify(target.chart.customAttr)
    }
    if (target.chart.customStyle && typeof target.chart.customStyle !== 'string') {
      target.chart.customStyle = JSON.stringify(target.chart.customStyle)
    }
    if (target.chart.customFilter && typeof target.chart.customFilter !== 'string') {
      target.chart.customFilter = JSON.stringify(target.chart.customFilter)
    }
  }
  return target
}
