export function baseLineOption(chart_option, chart) {
// 处理data
  if (chart.data) {
    chart_option.title.text = chart.title
    chart_option.xAxis.data = chart.data.x
    chart.data.series.forEach(function(y) {
      chart_option.legend.data.push(y.name)
      chart_option.series.push(y)
    })
  }
  // console.log(chart_option);
  // 处理shape attr
  if (chart.customAttr) {
    const customAttr = JSON.parse(chart.customAttr)
    if (customAttr.color) {
      chart_option.color = customAttr.color.colors
    }
  }
  return chart_option
}

