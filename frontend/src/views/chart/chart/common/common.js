export function componentStyle(chart_option, chart) {
  if (chart.customStyle) {
    const customStyle = JSON.parse(chart.customStyle)
    if (customStyle.text) {
      chart_option.title.show = customStyle.text.show
      chart_option.title.left = customStyle.text.hPosition
      chart_option.title.top = customStyle.text.vPosition
      const style = chart_option.title.textStyle ? chart_option.title.textStyle : {}
      style.fontSize = customStyle.text.fontSize
      style.color = customStyle.text.color
      customStyle.text.isItalic ? style.fontStyle = 'italic' : style.fontStyle = 'normal'
      chart_option.title.textStyle = style
    }
    if (customStyle.legend) {
      chart_option.legend.show = customStyle.legend.show
      chart_option.legend.left = customStyle.legend.hPosition
      chart_option.legend.top = customStyle.legend.vPosition
      chart_option.legend.orient = customStyle.legend.orient
    }
  }
}
