import {
  G2PlotChartView,
  G2PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/g2plot'
import { Datum, Line as G2Line, LineOptions } from '@antv/g2plot'
import { getPadding } from '../../common/common_antv'
import {
  antVCustomColor,
  flow,
  handleEmptyDataStrategy,
  parseJson
} from '@/views/chart/components/js/util'
import { cloneDeep } from 'lodash-es'
import { formatterItem, valueFormatter } from '@/views/chart/components/js/formatter'

const DEFAULT_DATA = []
/**
 * 折线图
 */
export class Line extends G2PlotChartView<LineOptions, G2Line> {
  drawChart(drawOptions: G2PlotDrawOptions<G2Line>) {
    const chart = drawOptions.chart
    // data
    if (chart?.data) {
      const data = cloneDeep(chart.data.data)
      // size
      let customAttr: DeepPartial<ChartAttr> = {}
      let smooth, point, lineStyle
      if (chart.customAttr) {
        customAttr = parseJson(chart.customAttr)
        if (customAttr.size) {
          const s = JSON.parse(JSON.stringify(customAttr.size)) as ChartSizeAttr
          smooth = s.lineSmooth
          point = {
            size: s.lineSymbolSize,
            shape: s.lineSymbol
          }
          lineStyle = {
            lineWidth: s.lineWidth
          }
        }
      }
      // custom color
      const color = antVCustomColor(chart)
      // options
      const initOptions: LineOptions = {
        data: data,
        xField: 'field',
        yField: 'value',
        seriesField: 'category',
        appendPadding: getPadding(chart),
        color,
        point,
        lineStyle,
        smooth,
        interactions: [
          {
            type: 'legend-active',
            cfg: {
              start: [{ trigger: 'legend-item:mouseenter', action: ['element-active:reset'] }],
              end: [{ trigger: 'legend-item:mouseleave', action: ['element-active:reset'] }]
            }
          },
          {
            type: 'legend-filter',
            cfg: {
              start: [
                {
                  trigger: 'legend-item:click',
                  action: [
                    'list-unchecked:toggle',
                    'data-filter:filter',
                    'element-active:reset',
                    'element-highlight:reset'
                  ]
                }
              ]
            }
          },
          {
            type: 'tooltip',
            cfg: {
              start: [{ trigger: 'point:mousemove', action: 'tooltip:show' }],
              end: [{ trigger: 'point:mouseleave', action: 'tooltip:hide' }]
            }
          },
          {
            type: 'active-region',
            cfg: {
              start: [{ trigger: 'element:mousemove', action: 'active-region:show' }],
              end: [{ trigger: 'element:mouseleave', action: 'active-region:hide' }]
            }
          }
        ]
      }
      const options = this.setupOptions(chart, initOptions)
      // 处理空值
      if (chart.senior) {
        let emptyDataStrategy = parseJson(chart.senior)?.functionCfg?.emptyDataStrategy
        if (!emptyDataStrategy) {
          emptyDataStrategy = 'breakLine'
        }
        handleEmptyDataStrategy(emptyDataStrategy, chart, data, options)
      }
      // 开始渲染
      if (drawOptions.chartObj) {
        drawOptions.chartObj.destroy()
      }
      drawOptions.chartObj = new G2Line(drawOptions.container, options)

      drawOptions.chartObj.off('point:click')
      drawOptions.chartObj.on('point:click', drawOptions.action)

      return drawOptions.chartObj
    }
  }

  protected configLabel(chart: Chart, options: LineOptions): LineOptions {
    let customAttr: DeepPartial<ChartAttr>
    let label
    if (chart.customAttr) {
      customAttr = parseJson(chart.customAttr)
      if (customAttr.label) {
        const labelAttr = customAttr.label
        if (labelAttr.show) {
          label = {
            position: labelAttr.position,
            offsetY: -8,
            style: {
              fill: labelAttr.color,
              fontSize: labelAttr.fontSize
            },
            formatter: function (param: Datum) {
              let res = param.value
              const xAxisExt = chart.xAxisExt
              const yAxis = chart.yAxis
              let f
              if (xAxisExt?.length > 0) {
                f = yAxis[0]
              } else {
                for (let i = 0; i < yAxis.length; i++) {
                  if (yAxis[i].name === param.category) {
                    f = yAxis[i]
                    break
                  }
                }
              }
              if (!f) {
                return res
              }
              if (!f.formatterCfg) {
                f.formatterCfg = formatterItem
              }
              res = valueFormatter(param.value, f.formatterCfg)
              return res
            }
          }
        } else {
          label = false
        }
      }
    }
    return { ...options, label }
  }

  protected configTooltip(chart: Chart, options: LineOptions): LineOptions {
    let tooltip
    const customAttr: DeepPartial<ChartAttr> = parseJson(chart.customAttr)
    if (customAttr.tooltip) {
      const tooltipAttr = customAttr.tooltip
      if (tooltipAttr.show) {
        tooltip = {
          formatter: function (param: Datum) {
            let res
            const obj = { name: param.category, value: param.value }
            const xAxisExt = chart.xAxisExt
            const yAxis = chart.yAxis
            for (let i = 0; i < yAxis.length; i++) {
              const f = yAxis[i]
              if (f.name === param.category || (yAxis.length && xAxisExt.length)) {
                if (f.formatterCfg) {
                  res = valueFormatter(param.value, f.formatterCfg)
                } else {
                  res = valueFormatter(param.value, formatterItem)
                }
                break
              }
            }
            obj.value = res ?? ''
            return obj
          }
        }
      } else {
        tooltip = false
      }
    }
    return { ...options, tooltip }
  }

  protected setupOptions(chart: Chart, options: LineOptions): LineOptions {
    return flow(
      this.configTheme,
      this.configLabel,
      this.configTooltip,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configSlider,
      this.configAnalyse
    )(chart, options)
  }

  constructor() {
    super('line', DEFAULT_DATA)
  }
}
