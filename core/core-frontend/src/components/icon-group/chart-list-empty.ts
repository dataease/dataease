import outerParams from '@/assets/svg/icon_params_setting.svg'
import filter from '@/assets/svg/filter.svg'
import pictureGroup from '@/assets/svg/picture-group.svg'
import richText from '@/assets/svg/rich-text.svg'
import chartMixDualLine from '@/assets/svg/empty-light/icon_line_light.svg'
import { defineAsyncComponent } from 'vue'
const svgs = import.meta.glob('@/assets/svg/empty-light/*.svg')
const iconChartMapEmpty = {
  'icon_bar-stack-percent_light': 'percentage-bar-stack',
  'icon_bar-stack_light': 'bar-stack',
  icon_bar_light: 'bar',
  'icon_bubble-map_gray_light': 'bubble-map',
  'icon_bullet-chart_light': 'bullet-graph',
  'icon_chart-quadrant_light': 'quadrant',
  'icon_chart-scatter_light': 'scatter',
  'icon_circle-packing_light': 'circle-packing',
  icon_circular_light: 'pie-donut',
  icon_combination_light: 'chart-mix',
  'icon_common-table_light': 'table-info',
  icon_dashboard_light: 'gauge',
  'icon_flow-map_gray_light': 'flow-map',
  'icon_funnel-plot_light': 'funnel',
  'icon_group-bar-stack_light': 'bar-group-stack',
  'icon_group-bar_light': 'bar-group',
  'icon_group-combination_light': 'chart-mix-group',
  icon_heatmap_light: 't-heatmap',
  icon_indicator_light: 'indicator',
  'icon_interval-bar_light': 'bar-range',
  'icon_k-line_light': 'stock-line',
  icon_line_light: 'line',
  'icon_map-lbs-heat_gray_light': 'heat-map',
  icon_map_gray_light: 'map',
  icon_pie_light: 'pie',
  'icon_pivot-table_light': 'table-pivot',
  'icon_polyline-stack_light': 'area-stack',
  icon_polyline_light: 'area',
  'icon_progress-bar_light': 'progress-bar',
  icon_radar_light: 'radar',
  'icon_rectangle-tree_light': 'treemap',
  'icon_rose-circular_light': 'pie-donut-rose',
  'icon_rose-pie_light': 'pie-rose',
  icon_sankey_light: 'sankey',
  'icon_stack-combination_light': 'chart-mix-stack',
  'icon_strip-stack-percent_light': 'percentage-bar-stack-horizontal',
  'icon_strip-stack_light': 'bar-stack-horizontal',
  icon_strip_light: 'bar-horizontal',
  'icon_summary-table_light': 'table-normal',
  'icon_symbolic-map_gray_light': 'symbolic-map',
  'icon_symmetric-bar_light': 'bidirectional-bar',
  icon_waterfall_light: 'waterfall',
  icon_waterwave_light: 'liquid',
  'icon_word-cloud_light': 'word-cloud'
}
const iconChartMap = {
  'chart-mix-dual-line': chartMixDualLine,
  'rich-text': richText,
  'picture-group': pictureGroup,
  filter: filter,
  outerParams: outerParams
}
Object.keys(svgs).forEach(path => {
  const name = path.match(/\/assets\/svg\/empty-light\/([^/]+)\.svg$/)[1]
  iconChartMap[iconChartMapEmpty[name]] = defineAsyncComponent(svgs[path])
})
export { iconChartMap, iconChartMapEmpty }
