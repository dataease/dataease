import { hexColorToRGBA } from '@/views/chart/chart/util'
import { DEFAULT_YAXIS_EXT_STYLE } from '@/views/chart/chart/chart'

export function componentStyle(chart_option, chart) {
  const padding = '8px'
  if (chart.customStyle) {
    const customStyle = JSON.parse(chart.customStyle)
    if (customStyle.text) {
      chart_option.title.show = customStyle.text.show
      // 水平方向
      if (customStyle.text.hPosition === 'left') {
        chart_option.title.left = padding
      } else if (customStyle.text.hPosition === 'right') {
        chart_option.title.right = padding
      } else {
        chart_option.title.left = customStyle.text.hPosition
      }
      // 垂直方向
      if (customStyle.text.vPosition === 'top') {
        chart_option.title.top = padding
      } else if (customStyle.text.vPosition === 'bottom') {
        chart_option.title.bottom = padding
      } else {
        chart_option.title.top = customStyle.text.vPosition
      }
      const style = chart_option.title.textStyle ? chart_option.title.textStyle : {}
      style.fontSize = customStyle.text.fontSize
      style.color = customStyle.text.color
      customStyle.text.isItalic ? style.fontStyle = 'italic' : style.fontStyle = 'normal'
      customStyle.text.isBolder ? style.fontWeight = 'bold' : style.fontWeight = 'normal'
      chart_option.title.textStyle = style
    }
    if (customStyle.legend && chart_option.legend) {
      chart_option.legend.show = customStyle.legend.show
      // 水平方向
      if (customStyle.legend.hPosition === 'left') {
        chart_option.legend.left = padding
      } else if (customStyle.legend.hPosition === 'right') {
        chart_option.legend.right = padding
      } else {
        chart_option.legend.left = customStyle.legend.hPosition
      }
      // 垂直方向
      if (customStyle.legend.vPosition === 'top') {
        chart_option.legend.top = padding
      } else if (customStyle.legend.vPosition === 'bottom') {
        chart_option.legend.bottom = padding
      } else {
        chart_option.legend.top = customStyle.legend.vPosition
      }
      chart_option.legend.orient = customStyle.legend.orient
      chart_option.legend.icon = customStyle.legend.icon
      chart_option.legend.textStyle = customStyle.legend.textStyle
      if (chart.type === 'treemap' || chart.type === 'gauge') {
        chart_option.legend.show = false
      }
    }
    if (customStyle.xAxis && (chart.type.includes('bar') || chart.type.includes('line') || chart.type.includes('scatter') || chart.type === 'chart-mix')) {
      chart_option.xAxis.show = customStyle.xAxis.show
      chart_option.xAxis.position = customStyle.xAxis.position
      chart_option.xAxis.name = customStyle.xAxis.name
      chart_option.xAxis.axisLabel = customStyle.xAxis.axisLabel
      chart_option.xAxis.splitLine = customStyle.xAxis.splitLine
      chart_option.xAxis.nameTextStyle = customStyle.xAxis.nameTextStyle

      chart_option.xAxis.axisLabel.showMaxLabel = true
      chart_option.xAxis.axisLabel.showMinLabel = true

      if (!customStyle.xAxis.show) {
        chart_option.xAxis.axisLabel.show = false
      }

      // 轴值设置
      delete chart_option.xAxis.min
      delete chart_option.xAxis.max
      delete chart_option.xAxis.split
      if (chart.type.includes('horizontal')) {
        if (customStyle.xAxis.axisValue && !customStyle.xAxis.axisValue.auto) {
          customStyle.xAxis.axisValue.min && (chart_option.xAxis.min = parseFloat(customStyle.xAxis.axisValue.min))
          customStyle.xAxis.axisValue.max && (chart_option.xAxis.max = parseFloat(customStyle.xAxis.axisValue.max))
          customStyle.xAxis.axisValue.split && (chart_option.xAxis.interval = parseFloat(customStyle.xAxis.axisValue.split))
        }
      }
    }
    if (customStyle.yAxis && (chart.type.includes('bar') || chart.type.includes('line') || chart.type.includes('scatter'))) {
      chart_option.yAxis.show = customStyle.yAxis.show
      chart_option.yAxis.position = customStyle.yAxis.position
      chart_option.yAxis.name = customStyle.yAxis.name
      chart_option.yAxis.axisLabel = customStyle.yAxis.axisLabel
      chart_option.yAxis.splitLine = customStyle.yAxis.splitLine
      chart_option.yAxis.nameTextStyle = customStyle.yAxis.nameTextStyle

      chart_option.yAxis.axisLabel.showMaxLabel = true
      chart_option.yAxis.axisLabel.showMinLabel = true

      if (!customStyle.yAxis.show) {
        chart_option.yAxis.axisLabel.show = false
      }

      // 轴值设置
      delete chart_option.yAxis.min
      delete chart_option.yAxis.max
      delete chart_option.yAxis.split
      if (!chart.type.includes('horizontal')) {
        if (customStyle.yAxis.axisValue && !customStyle.yAxis.axisValue.auto) {
          customStyle.yAxis.axisValue.min && (chart_option.yAxis.min = parseFloat(customStyle.yAxis.axisValue.min))
          customStyle.yAxis.axisValue.max && (chart_option.yAxis.max = parseFloat(customStyle.yAxis.axisValue.max))
          customStyle.yAxis.axisValue.split && (chart_option.yAxis.interval = parseFloat(customStyle.yAxis.axisValue.split))
        }
      }
    }
    if (customStyle.yAxis && chart.type === 'chart-mix') {
      chart_option.yAxis[0].show = customStyle.yAxis.show
      chart_option.yAxis[0].position = customStyle.yAxis.position
      chart_option.yAxis[0].name = customStyle.yAxis.name
      chart_option.yAxis[0].axisLabel = customStyle.yAxis.axisLabel
      chart_option.yAxis[0].splitLine = customStyle.yAxis.splitLine
      chart_option.yAxis[0].nameTextStyle = customStyle.yAxis.nameTextStyle

      chart_option.yAxis[0].axisLabel.showMaxLabel = true
      chart_option.yAxis[0].axisLabel.showMinLabel = true

      if (!customStyle.yAxis.show) {
        chart_option.yAxis[0].axisLabel.show = false
      }

      // 轴值设置
      delete chart_option.yAxis[0].min
      delete chart_option.yAxis[0].max
      delete chart_option.yAxis[0].split
      if (!chart.type.includes('horizontal')) {
        if (customStyle.yAxis.axisValue && !customStyle.yAxis.axisValue.auto) {
          customStyle.yAxis.axisValue.min && (chart_option.yAxis[0].min = parseFloat(customStyle.yAxis.axisValue.min))
          customStyle.yAxis.axisValue.max && (chart_option.yAxis[0].max = parseFloat(customStyle.yAxis.axisValue.max))
          customStyle.yAxis.axisValue.split && (chart_option.yAxis[0].interval = parseFloat(customStyle.yAxis.axisValue.split))
        }
      }

      // axis ext
      !customStyle.yAxisExt && (customStyle.yAxisExt = JSON.parse(JSON.stringify(DEFAULT_YAXIS_EXT_STYLE)))
      chart_option.yAxis[1].show = customStyle.yAxisExt.show
      chart_option.yAxis[1].position = customStyle.yAxisExt.position
      chart_option.yAxis[1].name = customStyle.yAxisExt.name
      chart_option.yAxis[1].axisLabel = customStyle.yAxisExt.axisLabel
      chart_option.yAxis[1].splitLine = customStyle.yAxisExt.splitLine
      chart_option.yAxis[1].nameTextStyle = customStyle.yAxisExt.nameTextStyle

      chart_option.yAxis[1].axisLabel.showMaxLabel = true
      chart_option.yAxis[1].axisLabel.showMinLabel = true

      if (!customStyle.yAxisExt.show) {
        chart_option.yAxis[1].axisLabel.show = false
      }

      // 轴值设置
      delete chart_option.yAxis[1].min
      delete chart_option.yAxis[1].max
      delete chart_option.yAxis[1].split
      if (!chart.type.includes('horizontal')) {
        if (customStyle.yAxisExt.axisValue && !customStyle.yAxisExt.axisValue.auto) {
          customStyle.yAxisExt.axisValue.min && (chart_option.yAxis[1].min = parseFloat(customStyle.yAxisExt.axisValue.min))
          customStyle.yAxisExt.axisValue.max && (chart_option.yAxis[1].max = parseFloat(customStyle.yAxisExt.axisValue.max))
          customStyle.yAxisExt.axisValue.split && (chart_option.yAxis[1].interval = parseFloat(customStyle.yAxisExt.axisValue.split))
        }
      }
    }
    if (customStyle.split && chart.type.includes('radar')) {
      chart_option.radar.name = customStyle.split.name
      chart_option.radar.splitNumber = customStyle.split.splitNumber
      chart_option.radar.axisLine = customStyle.split.axisLine
      chart_option.radar.axisTick = customStyle.split.axisTick
      chart_option.radar.axisLabel = customStyle.split.axisLabel
      chart_option.radar.splitLine = customStyle.split.splitLine
      chart_option.radar.splitArea = customStyle.split.splitArea
    }
    if (customStyle.background) {
      chart_option.backgroundColor = hexColorToRGBA(customStyle.background.color, customStyle.background.alpha)
    }
  }
}

export function seniorCfg(chart_option, chart) {
  if (chart.senior && chart.type && (chart.type.includes('bar') || chart.type.includes('line') || chart.type.includes('mix'))) {
    const senior = JSON.parse(chart.senior)
    if (senior.functionCfg) {
      if (senior.functionCfg.sliderShow) {
        chart_option.dataZoom = [
          {
            type: 'inside',
            start: parseInt(senior.functionCfg.sliderRange[0]),
            end: parseInt(senior.functionCfg.sliderRange[1])
          },
          {
            type: 'slider',
            start: parseInt(senior.functionCfg.sliderRange[0]),
            end: parseInt(senior.functionCfg.sliderRange[1])
          }
        ]
        if (chart.type.includes('horizontal')) {
          chart_option.dataZoom[0].yAxisIndex = [0]
          chart_option.dataZoom[1].yAxisIndex = [0]
          chart_option.dataZoom[1].left = '10px'
        }
      }
    }
    if (senior.assistLine && senior.assistLine.length > 0) {
      if (chart_option.series && chart_option.series.length > 0) {
        chart_option.series[0].markLine = {
          symbol: 'none',
          data: []
        }
        senior.assistLine.forEach(ele => {
          if (chart.type.includes('horizontal')) {
            chart_option.series[0].markLine.data.push({
              symbol: 'none',
              xAxis: parseFloat(ele.value),
              name: ele.name,
              lineStyle: {
                color: ele.color,
                type: ele.lineType
              },
              label: {
                show: true,
                color: ele.color,
                fontSize: 10,
                position: 'insideStartTop'
              },
              tooltip: {
                show: false
              }
            })
          } else {
            chart_option.series[0].markLine.data.push({
              symbol: 'none',
              yAxis: parseFloat(ele.value),
              name: ele.name,
              lineStyle: {
                color: ele.color,
                type: ele.lineType
              },
              label: {
                show: true,
                color: ele.color,
                fontSize: 10,
                position: 'insideStartTop'
              },
              tooltip: {
                show: false
              }
            })
          }
        })
      }
    }
  }
}
