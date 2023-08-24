import {
  getLabel,
  getPadding,
  getTheme,
  getTooltip,
  getXAxis,
  getYAxis,
  setGradientColor
} from '@/views/chart/chart/common/common_antv'
import { Waterfall } from '@antv/g2plot'
import { formatterItem, valueFormatter } from '@/views/chart/chart/formatter'

export function baseWaterfallOptionAntV(plot, container, chart, action) {
  // theme
  const theme = getTheme(chart)
  // attr
  const label = getLabel(chart)
  const tooltip = getTooltip(chart)
  // style
  // const legend = getLegend(chart)
  const xAxis = getXAxis(chart)
  const yAxis = getYAxis(chart)
  // fix yAxis
  if (yAxis) {
    yAxis.min = yAxis.minLimit
    yAxis.max = yAxis.maxLimit
    delete yAxis.minLimit
    delete yAxis.maxLimit
  }
  // data
  const data = chart.data.data
  const [risingColorRgba, fallingColorRgba, totalColorRgba] = theme.styleSheet.paletteQualitative10

  let customAttrCopy = {}
  if (chart.customAttr) {
    customAttrCopy = JSON.parse(chart.customAttr)
  }
  // total
  const total = {
    label: '合计',
    style: {
      fill: setGradientColor(totalColorRgba, customAttrCopy.color.gradient, 270)
    }
  }
  // options
  const options = {
    theme: theme,
    data: data,
    xField: 'field',
    yField: 'value',
    seriesField: 'category',
    appendPadding: getPadding(chart),
    meta: getMeta(chart),
    label: label,
    tooltip: tooltip,
    legend: {
      items: [
        { name: '增加', marker: {
          style: {
            fill: setGradientColor(risingColorRgba, customAttrCopy.color.gradient, 270)
          }
        }},
        { name: '减少', marker: {
          style: {
            fill: setGradientColor(fallingColorRgba, customAttrCopy.color.gradient, 270)
          }
        }},
        { name: '合计', marker: {
          style: {
            fill: setGradientColor(totalColorRgba, customAttrCopy.color.gradient, 270)
          }
        }}
      ]
    },
    xAxis: xAxis,
    yAxis: yAxis,
    risingFill: setGradientColor(risingColorRgba, customAttrCopy.color.gradient, 270),
    fallingFill: setGradientColor(fallingColorRgba, customAttrCopy.color.gradient, 270),
    total: total,
    interactions: [
      {
        type: 'tooltip', cfg: {
          start: [{ trigger: 'interval:mousemove', action: 'tooltip:show' }],
          end: [{ trigger: 'interval:mouseleave', action: 'tooltip:hide' }]
        }
      }
    ]
  }

  let customAttr = {}
  if (chart.customAttr) {
    customAttr = JSON.parse(chart.customAttr)
    if (customAttr.size) {
      const s = JSON.parse(JSON.stringify(customAttr.size))
      if (s.barDefault) {
        delete options.marginRatio
      } else {
        options.marginRatio = s.barGap
      }
    }
  }

  // 开始渲染
  if (plot) {
    plot.destroy()
  }
  plot = new Waterfall(container, options)

  plot.off('interval:click')
  plot.on('interval:click', action)

  return plot
}

function getMeta(chart) {
  const meta = {
    field: {
      type: 'cat'
    }
  }
  const yaxis = JSON.parse(chart.yaxis)
  if (yaxis && yaxis.length > 0) {
    const f = yaxis[0]
    meta.value = {
      alias: f.name,
      formatter: (value) => {
        let res
        if (f.formatterCfg) {
          res = valueFormatter(value, f.formatterCfg)
        } else {
          res = valueFormatter(value, formatterItem)
        }
        return res
      }
    }
  }
  return meta
}
