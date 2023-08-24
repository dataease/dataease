import { Liquid } from '@antv/g2plot'
import { hexColorToRGBA } from '@/views/chart/chart/util'
import { DEFAULT_LABEL, DEFAULT_SIZE } from '@/views/chart/chart/chart'
import { valueFormatter } from '@/views/chart/chart/formatter'

let labelFormatter = null

export function baseLiquid(plot, container, chart) {
  let value = 0
  const colors = []
  let max, radius, bgColor, shape, labelContent
  if (chart.data?.series.length > 0) {
    value = chart.data.series[0].data[0]
  }
  let customAttr = {}
  if (chart.customAttr) {
    customAttr = JSON.parse(chart.customAttr)
    // color
    if (customAttr.color) {
      const c = JSON.parse(JSON.stringify(customAttr.color))
      c.colors.forEach(ele => {
        colors.push(hexColorToRGBA(ele, c.alpha))
      })
    }
    // size
    if (customAttr.size) {
      const size = JSON.parse(JSON.stringify(customAttr.size))
      if (size.liquidMaxType === 'dynamic') {
        max = chart.data?.series[chart.data?.series.length - 1]?.data[0]
      } else {
        max = size.liquidMax ? size.liquidMax : DEFAULT_SIZE.liquidMax
      }
      radius = parseFloat((size.liquidSize ? size.liquidSize : DEFAULT_SIZE.liquidSize) / 100)
      shape = size.liquidShape ? size.liquidShape : DEFAULT_SIZE.liquidShape
    }
    // label
    if (customAttr.label) {
      const label = JSON.parse(JSON.stringify(customAttr.label))
      labelFormatter = label.gaugeLabelFormatter ? label.gaugeLabelFormatter : DEFAULT_LABEL.gaugeLabelFormatter
      if (label.show) {
        labelContent = {
          style: ({ percent }) => ({
            fontSize: parseInt(label.fontSize),
            color: label.color
          }),
          formatter: function(v) {
            const value = v.percent
            return valueFormatter(value, labelFormatter)
          }
        }
      } else {
        labelContent = false
      }
    }
  }
  let customStyle
  if (chart.customStyle) {
    customStyle = JSON.parse(chart.customStyle)
    if (customStyle.background) {
      bgColor = hexColorToRGBA(customStyle.background.color, customStyle.background.alpha)
    }
  }
  // 开始渲染
  if (plot) {
    plot.destroy()
  }
  plot = new Liquid(container, {
    theme: {
      styleSheet: {
        brandColor: colors[0],
        paletteQualitative10: colors,
        paletteQualitative20: colors,
        backgroundColor: bgColor
      }
    },
    percent: (parseFloat(value) / parseFloat(max)),
    radius: radius,
    shape: shape,
    statistic: {
      content: labelContent
    }
  })
  return plot
}
