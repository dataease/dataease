import { getPadding, getTheme } from '@/views/chart/chart/common/common_antv'
import { Gauge } from '@antv/g2plot'
import { DEFAULT_SIZE } from '@/views/chart/chart/chart'

export function baseGaugeOptionAntV(plot, container, chart, action) {
  let max, labelContent, startAngel, endAngel
  // theme
  const theme = getTheme(chart)
  // data
  const data = chart.data.series[0].data[0]
  // size
  let customAttr = {}
  if (chart.customAttr) {
    customAttr = JSON.parse(chart.customAttr)
    if (customAttr.size) {
      const size = JSON.parse(JSON.stringify(customAttr.size))
      max = size.gaugeMax ? size.gaugeMax : DEFAULT_SIZE.gaugeMax
      startAngel = parseInt(size.gaugeStartAngle) * Math.PI / 180
      endAngel = parseInt(size.gaugeEndAngle) * Math.PI / 180
    }
  }
  // label
  if (customAttr.label) {
    const label = JSON.parse(JSON.stringify(customAttr.label))
    if (label.show) {
      labelContent = {
        style: ({ percent }) => ({
          fontSize: parseInt(label.fontSize),
          color: label.color
        })
      }
    } else {
      labelContent = false
    }
  }

  // options
  const options = {
    theme: theme,
    percent: (parseFloat(data) / parseFloat(max)),
    startAngle: startAngel,
    endAngle: endAngel,
    appendPadding: getPadding(chart),
    // indicator: null,
    statistic: {
      content: labelContent
    }
    // range: {
    //   width: 12
    // },
    // gaugeStyle: {
    //   lineCap: 'round'
    // }
  }

  // 开始渲染
  if (plot) {
    plot.destroy()
  }
  plot = new Gauge(container, options)

  return plot
}
