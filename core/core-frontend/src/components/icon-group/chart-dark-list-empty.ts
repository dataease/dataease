import richTextDark from '@/assets/svg/rich-text-dark.svg'
import chartMixDualLineDark from '@/assets/svg/empty-dark/icon_line_light.svg'
import { defineAsyncComponent } from 'vue'
const svgs = import.meta.glob('@/assets/svg/empty-dark/*.svg')
const iconChartMapEmpty = {
  'icon_bar-stack-percent_light': 'percentage-bar-stack-dark',
  'icon_bar-stack_light': 'bar-stack-dark',
  icon_bar_light: 'bar-dark',
  'icon_bubble-map_gray_light': 'bubble-map-dark',
  'icon_bullet-chart_light': 'bullet-graph-dark',
  'icon_chart-quadrant_light': 'quadrant-dark',
  'icon_chart-scatter_light': 'scatter-dark',
  'icon_circle-packing_light': 'circle-packing-dark',
  icon_circular_light: 'pie-donut-dark',
  icon_combination_light: 'chart-mix-dark',
  'icon_common-table_light': 'table-info-dark',
  icon_dashboard_light: 'gauge-dark',
  'icon_flow-map_gray_light': 'flow-map-dark',
  'icon_funnel-plot_light': 'funnel-dark',
  'icon_group-bar-stack_light': 'bar-group-stack-dark',
  'icon_group-bar_light': 'bar-group-dark',
  'icon_group-combination_light': 'chart-mix-group-dark',
  icon_heatmap_light: 't-heatmap-dark',
  icon_indicator_light: 'indicator-dark',
  'icon_interval-bar_light': 'bar-range-dark',
  'icon_k-line_light': 'stock-line-dark',
  icon_line_light: 'line-dark',
  'icon_map-lbs-heat_gray_light': 'heat-map-dark',
  icon_map_gray_light: 'map-dark',
  icon_pie_light: 'pie-dark',
  'icon_pivot-table_light': 'table-pivot-dark',
  'icon_polyline-stack_light': 'area-stack-dark',
  icon_polyline_light: 'area-dark',
  'icon_progress-bar_light': 'progress-bar-dark',
  icon_radar_light: 'radar-dark',
  'icon_rectangle-tree_light': 'treemap-dark',
  'icon_rose-circular_light': 'pie-donut-rose-dark',
  'icon_rose-pie_light': 'pie-rose-dark',
  icon_sankey_light: 'sankey-dark',
  'icon_stack-combination_light': 'chart-mix-stack-dark',
  'icon_strip-stack-percent_light': 'percentage-bar-stack-horizontal-dark',
  'icon_strip-stack_light': 'bar-stack-horizontal-dark',
  icon_strip_light: 'bar-horizontal-dark',
  'icon_summary-table_light': 'table-normal-dark',
  'icon_symbolic-map_gray_light': 'symbolic-map-dark',
  'icon_symmetric-bar_light': 'bidirectional-bar-dark',
  icon_waterfall_light: 'waterfall-dark',
  icon_waterwave_light: 'liquid-dark',
  'icon_word-cloud_light': 'word-cloud-dark'
}
const iconChartDarkMap = {
  'chart-mix-dual-line-dark': chartMixDualLineDark,
  'rich-text-dark': richTextDark
}
Object.keys(svgs).forEach(path => {
  const name = path.match(/\/assets\/svg\/empty-dark\/([^/]+)\.svg$/)[1]
  iconChartDarkMap[iconChartMapEmpty[name]] = defineAsyncComponent(svgs[path])
})

export { iconChartDarkMap, iconChartMapEmpty }
