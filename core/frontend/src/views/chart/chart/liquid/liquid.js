import { Liquid } from '@antv/g2plot'
import { hexColorToRGBA } from '@/views/chart/chart/util'
import { DEFAULT_LABEL, DEFAULT_SIZE } from '@/views/chart/chart/chart'
import { valueFormatter } from '@/views/chart/chart/formatter'

let labelFormatter = null

export function baseLiquid(container, chart) {
  let value = 0
  const colors = []
  let max; let radius; let bgColor; let shape; let labelContent; let liquidStyle; let originVal = 0
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
    originVal = (parseFloat(value) / parseFloat(max))
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
          formatter: () => {
            return valueFormatter(originVal, labelFormatter)
          }
        }
      } else {
        labelContent = false
      }
    }
  }
  // senior
  const senior = JSON.parse(chart.senior)
  if (senior?.threshold) {
    const { liquidThreshold } = senior?.threshold
    if (liquidThreshold) {
      liquidStyle = () => {
        const thresholdArr = liquidThreshold.split(',')
        let index = 0
        thresholdArr.forEach((v, i) => {
          if (originVal > v / 100) {
            index = i + 1
          }
        })
        return {
          fill: colors[index % colors.length],
          stroke: colors[index % colors.length]
        }
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
  return new Liquid(container, {
    theme: {
      styleSheet: {
        brandColor: colors[0],
        paletteQualitative10: colors,
        paletteQualitative20: colors,
        backgroundColor: bgColor
      }
    },
    percent: originVal > 1 ? 1 : originVal,
    radius: radius,
    shape: shape,
    statistic: {
      content: labelContent
    },
    liquidStyle
  })
}
