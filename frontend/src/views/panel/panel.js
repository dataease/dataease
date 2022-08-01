// eslint-disable-next-line no-unused-vars
import { DEFAULT_COLOR_CASE, DEFAULT_TITLE_STYLE } from '@/views/chart/chart/chart'
import { deepCopy } from '@/components/canvas/utils/utils'
import { COMMON_BACKGROUND_BASE } from '@/components/canvas/custom-component/component-list'
export const TAB_COMMON_STYLE = {
  headFontColor: '#000000',
  headFontActiveColor: '#000000',
  headBorderColor: null,
  headBorderActiveColor: null
}

export const FILTER_COMMON_STYLE = {
  horizontal: 'left',
  vertical: 'top',
  color: '#000000',
  brColor: '',
  wordColor: '',
  innerBgColor: ''
}

export const FILTER_COMMON_STYLE_DARK = {
  horizontal: 'left',
  vertical: 'top',
  color: '#FFFFFF',
  brColor: '#4E4B4B',
  wordColor: '#FFFFFF',
  innerBgColor: '#131E42'
}

export const DEFAULT_PANEL_STYLE = {
  themeColor: 'light',
  color: '#ffffff',
  imageUrl: null,
  backgroundType: 'image',
  gap: 'yes',
  resultMode: 'all', // 视图结果显示模式 all 视图 custom 仪表板自定义
  resultCount: 1000 // 视图结果显示条数
}

export const PANEL_CHART_INFO = {
  chartTitle: DEFAULT_TITLE_STYLE,
  chartColor: DEFAULT_COLOR_CASE,
  chartCommonStyle: COMMON_BACKGROUND_BASE,
  filterStyle: FILTER_COMMON_STYLE,
  tabStyle: TAB_COMMON_STYLE
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
  panel: DEFAULT_PANEL_STYLE,
  aidedDesign: {
    showGrid: false,
    matrixBase: 4 // 当前matrix的基数 （是pcMatrixCount的几倍）
  }, // 辅助设计
  refreshViewLoading: true, // 仪表板视图loading提示
  refreshUnit: 'minute', // 仪表板刷新时间带外 默认 分钟
  refreshTime: 5, // 仪表板刷新时间 默认5分钟
  themeId: 'system_1', // 当前所选主题ID 默认系统主题1
  chartInfo: PANEL_CHART_INFO
}

export const AIDED_DESIGN = {
  showGrid: false,
  matrixBase: 1 // 当前matrix的基数 （是pcMatrixCount的几倍）
}

export const DEFAULT_COMMON_CANVAS_STYLE_STRING = {
  ...CANVAS_STYLE
}

export function chartTransStr2Object(targetIn, copy) {
  const target = copy === 'Y' ? deepCopy(targetIn) : targetIn
  return target
}

export function chartTransObject2Str(targetIn, copy) {
  // eslint-disable-next-line no-undef
  const target = copy === 'Y' ? deepCopy(targetIn) : targetIn
  return target
}
