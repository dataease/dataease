import {
  DEFAULT_COLOR_CASE,
  DEFAULT_SIZE,
  DEFAULT_TITLE_STYLE,
  DEFAULT_LEGEND_STYLE,
  DEFAULT_LABEL,
  DEFAULT_TOOLTIP,
  DEFAULT_XAXIS_STYLE,
  DEFAULT_YAXIS_STYLE,
  DEFAULT_BACKGROUND_COLOR
} from '@/views/chart/chart/chart'

export const DEFAULT_PANEL_STYLE = {
  color: '#ffffff',
  imageUrl: null,
  backgroundType: 'image',
  gap: 'no'
}

export const DEFAULT_COMMON_CANVAS_STYLE = {
  width: 1680,
  height: 1050,
  scale: 100,
  openCommonStyle: true,
  panel: DEFAULT_PANEL_STYLE,
  chart: {
    xaxis: [],
    yaxis: [],
    show: true,
    type: 'panel',
    title: '',
    customAttr: {
      color: DEFAULT_COLOR_CASE,
      size: DEFAULT_SIZE,
      label: DEFAULT_LABEL,
      tooltip: DEFAULT_TOOLTIP
    },
    customStyle: {
      text: DEFAULT_TITLE_STYLE,
      legend: DEFAULT_LEGEND_STYLE,
      xAxis: DEFAULT_XAXIS_STYLE,
      yAxis: DEFAULT_YAXIS_STYLE,
      background: DEFAULT_BACKGROUND_COLOR
    },
    customFilter: []
  }
}
