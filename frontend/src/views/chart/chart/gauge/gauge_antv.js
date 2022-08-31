import { getPadding, getTheme } from '@/views/chart/chart/common/common_antv'
import { Gauge } from '@antv/g2plot'
import { DEFAULT_LABEL, DEFAULT_SIZE, DEFAULT_THRESHOLD } from '@/views/chart/chart/chart'
import { getScaleValue } from '@/components/canvas/utils/style'
import { valueFormatter } from '@/views/chart/chart/formatter'

let labelFormatter = null

export function baseGaugeOptionAntV(plot, container, chart, action, scale = 1) {
  let min, max, labelContent, startAngel, endAngel
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
      if (size.gaugeMinType === 'dynamic' && size.gaugeMaxType === 'dynamic') {
        min = chart.data?.series[chart.data?.series.length - 2]?.data[0]
        max = chart.data?.series[chart.data?.series.length - 1]?.data[0]
      } else if (size.gaugeMinType !== 'dynamic' && size.gaugeMaxType === 'dynamic') {
        min = size.gaugeMin ? size.gaugeMin : DEFAULT_SIZE.gaugeMin
        max = chart.data?.series[chart.data?.series.length - 1]?.data[0]
      } else if (size.gaugeMinType === 'dynamic' && size.gaugeMaxType !== 'dynamic') {
        min = chart.data?.series[chart.data?.series.length - 1]?.data[0]
        max = size.gaugeMax ? size.gaugeMax : DEFAULT_SIZE.gaugeMax
      } else {
        min = size.gaugeMin ? size.gaugeMin : DEFAULT_SIZE.gaugeMin
        max = size.gaugeMax ? size.gaugeMax : DEFAULT_SIZE.gaugeMax
      }
      startAngel = parseInt(size.gaugeStartAngle) * Math.PI / 180
      endAngel = parseInt(size.gaugeEndAngle) * Math.PI / 180
    }
  }
  const per = (parseFloat(data) - parseFloat(min)) / (parseFloat(max) - parseFloat(min))
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
          let value
          if (labelFormatter.type === 'percent') {
            value = per
          } else {
            value = data
          }
          return valueFormatter(value, labelFormatter)
        }
      }
    } else {
      labelContent = false
    }
  }

  const range = [0]
  let index = 0
  let flag = false
  let hasThreshold = false

  if (chart.senior) {
    const senior = JSON.parse(chart.senior)
    const threshold = JSON.parse(JSON.stringify(senior.threshold ? senior.threshold : DEFAULT_THRESHOLD))
    if (threshold.gaugeThreshold && threshold.gaugeThreshold !== '') {
      hasThreshold = true
      const arr = threshold.gaugeThreshold.split(',')
      for (let i = 0; i < arr.length; i++) {
        const ele = arr[i]
        const p = parseFloat(ele) / 100
        range.push(p)
        if (!flag && per <= p) {
          flag = true
          index = i
        }
      }
      if (!flag) {
        index = arr.length
      }
    }
  }
  range.push(1)

  // options
  const options = {
    theme: theme,
    percent: per,
    startAngle: startAngel,
    endAngle: endAngel,
    appendPadding: getPadding(chart),
    // indicator: null,
    statistic: {
      content: labelContent
    },
    axis: {
      tickInterval: 0.2,
      label: {
        style: {
          fontSize: getScaleValue(14, scale) // 刻度值字体大小
        },
        formatter: function(v) {
          return v === '0' ? v : (v * 100 + '%')
        }
      },
      tickLine: {
        length: getScaleValue(12, scale) * -1, // 刻度线长度
        style: {
          lineWidth: getScaleValue(1, scale)// 刻度线宽度
        }
      },
      subTickLine: {
        count: 4, // 子刻度数
        length: getScaleValue(6, scale) * -1, // 子刻度线长度
        style: {
          lineWidth: getScaleValue(1, scale)// 子刻度线宽度
        }
      }
    }
  }
  if (hasThreshold) {
    options.range = {
      color: theme.styleSheet.paletteQualitative10,
      ticks: range
    }
    options.indicator = {
      pointer: {
        style: {
          stroke: theme.styleSheet.paletteQualitative10[index % theme.styleSheet.paletteQualitative10.length]
        }
      },
      pin: {
        style: {
          stroke: theme.styleSheet.paletteQualitative10[index % theme.styleSheet.paletteQualitative10.length],
          r: getScaleValue(10, scale)
        }
      }
    }
  } else {
    options.indicator = {
      pin: {
        style: {
          r: getScaleValue(10, scale)
        }
      }
    }
  }

  // 开始渲染
  if (plot) {
    plot.destroy()
  }
  plot = new Gauge(container, options)

  return plot
}
