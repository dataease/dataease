import {
  DEFAULT_COLOR_CASE_DARK,
  DEFAULT_COLOR_CASE_LIGHT,
  DEFAULT_TITLE_STYLE_DARK,
  DEFAULT_TITLE_STYLE_LIGHT,
  FILTER_COMMON_STYLE_DARK,
  FILTER_COMMON_STYLE_LIGHT,
  TAB_COMMON_STYLE_DARK,
  TAB_COMMON_STYLE_LIGHT
} from '@/views/chart/components/editor/util/chart'
import {
  COMMON_COMPONENT_BACKGROUND_DARK,
  COMMON_COMPONENT_BACKGROUND_LIGHT
} from '@/custom-component/component-list'

export const PANEL_CHART_INFO_LIGHT = {
  chartTitle: DEFAULT_TITLE_STYLE_LIGHT,
  chartColor: DEFAULT_COLOR_CASE_LIGHT,
  chartCommonStyle: COMMON_COMPONENT_BACKGROUND_LIGHT,
  filterStyle: FILTER_COMMON_STYLE_LIGHT,
  tabStyle: TAB_COMMON_STYLE_LIGHT
}

export const PANEL_CHART_INFO_DARK = {
  chartTitle: DEFAULT_TITLE_STYLE_DARK,
  chartColor: DEFAULT_COLOR_CASE_DARK,
  chartCommonStyle: COMMON_COMPONENT_BACKGROUND_DARK,
  filterStyle: FILTER_COMMON_STYLE_DARK,
  tabStyle: TAB_COMMON_STYLE_DARK
}

export const MOBILE_SETTING_BASE = {
  customSetting: false,
  imageUrl: null,
  backgroundType: 'image'
}

export const MOBILE_SETTING_LIGHT = {
  ...MOBILE_SETTING_BASE,
  color: '#000'
}

export const MOBILE_SETTING_DARK = {
  ...MOBILE_SETTING_BASE,
  color: '#fff'
}

export const DEFAULT_DASHBOARD_STYLE_BASE = {
  themeColor: 'light',
  gap: 'yes',
  gapSize: 10,
  resultMode: 'all', // 视图结果显示模式 all 视图 custom 仪表板自定义
  resultCount: 1000 // 视图结果显示条数
}

export const DEFAULT_DASHBOARD_STYLE_LIGHT = {
  ...DEFAULT_DASHBOARD_STYLE_BASE,
  mobileSetting: MOBILE_SETTING_LIGHT
}

export const DEFAULT_DASHBOARD_STYLE_DARK = {
  ...DEFAULT_DASHBOARD_STYLE_BASE,
  mobileSetting: MOBILE_SETTING_DARK
}

export const DEFAULT_CANVAS_STYLE_DATA_BASE = {
  width: 1920,
  height: 1080,
  refreshViewEnable: false, // 开启视图刷新（默认关闭）
  refreshViewLoading: true, // 仪表板视图loading提示
  refreshUnit: 'minute', // 仪表板刷新时间带外 默认 分钟
  refreshTime: 5, // 仪表板刷新时间 默认5分钟
  scale: 60,
  scaleWidth: 100,
  scaleHeight: 100,
  backgroundType: 'backgroundColor',
  background: '',
  openCommonStyle: true,
  opacity: 1,
  fontSize: 14
}

// 基础亮色主题
export const DEFAULT_CANVAS_STYLE_DATA_LIGHT = {
  ...DEFAULT_CANVAS_STYLE_DATA_BASE,
  // 页面全局数据
  themeId: 'system_light',
  color: '#000',
  backgroundColor: '#fff',
  dashboard: DEFAULT_DASHBOARD_STYLE_LIGHT,
  component: PANEL_CHART_INFO_LIGHT
}

// 基础暗色主题
export const DEFAULT_CANVAS_STYLE_DATA_DARK = {
  ...DEFAULT_CANVAS_STYLE_DATA_BASE,
  // 页面全局数据
  themeId: 'system_dark',
  color: '#fff',
  backgroundColor: '#000',
  dashboard: DEFAULT_DASHBOARD_STYLE_DARK,
  component: PANEL_CHART_INFO_DARK
}

// 基础主题
export const BASE_THEMES = {
  light: DEFAULT_CANVAS_STYLE_DATA_LIGHT,
  dark: DEFAULT_CANVAS_STYLE_DATA_DARK
}
