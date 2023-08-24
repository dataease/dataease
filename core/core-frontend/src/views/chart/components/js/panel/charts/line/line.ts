import {
  G2PlotChartView,
  G2PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/g2plot'
import { Line as G2Line, LineOptions } from '@antv/g2plot/esm/plots/line'
import { getPadding } from '../../common/common_antv'
import {
  flow,
  handleEmptyDataStrategy,
  hexColorToRGBA,
  parseJson
} from '@/views/chart/components/js/util'
import { cloneDeep } from 'lodash-es'
import { formatterItem, valueFormatter } from '@/views/chart/components/js/formatter'
import {
  LINE_AXIS_TYPE,
  LINE_EDITOR_PROPERTY,
  LINE_EDITOR_PROPERTY_INNER
} from '@/views/chart/components/js/panel/charts/line/common'
import { Label } from '@antv/g2plot/lib/types/label'
import { IntervalGeometryLabelPosition } from '@antv/g2/lib/interface'
import { Datum } from '@antv/g2plot/esm/types/common'

const DEFAULT_DATA = []
/**
 * 折线图
 */
export class Line extends G2PlotChartView<LineOptions, G2Line> {
  properties = LINE_EDITOR_PROPERTY
  propertyInner = LINE_EDITOR_PROPERTY_INNER
  axis: AxisType[] = [...LINE_AXIS_TYPE, 'xAxisExt']

  drawChart(drawOptions: G2PlotDrawOptions<G2Line>): G2Line {
    const { chart, action, container } = drawOptions
    if (!chart.data.data?.length) {
      return
    }
    const data = cloneDeep(chart.data.data)
    // custom color
    const customAttr = parseJson(chart.customAttr)
    const color = customAttr.basicStyle.colors
    // options
    const initOptions: LineOptions = {
      data: data,
      xField: 'field',
      yField: 'value',
      seriesField: 'category',
      appendPadding: getPadding(chart),
      color,
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
    const newChart = new G2Line(container, options)

    newChart.on('point:click', action)

    return newChart
  }

  protected configLabel(chart: Chart, options: LineOptions): LineOptions {
    const customAttr = parseJson(chart.customAttr)
    const labelAttr = customAttr.label
    if (!labelAttr?.show) {
      return {
        ...options,
        label: false
      }
    }
    const { xAxisExt, yAxis } = chart
    const label: Label = {
      position: labelAttr.position as IntervalGeometryLabelPosition,
      offsetY: -8,
      style: {
        fill: labelAttr.color,
        fontSize: labelAttr.fontSize
      },
      formatter: function (param: Datum) {
        let res = param.value
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

  protected configBasicStyle(chart: Chart, options: LineOptions): LineOptions {
    // size
    const customAttr: DeepPartial<ChartAttr> = parseJson(chart.customAttr)
    const s = JSON.parse(JSON.stringify(customAttr.basicStyle))
    const smooth = s.lineSmooth
    const point = {
      size: s.lineSymbolSize,
      shape: s.lineSymbol
    }
    const lineStyle = {
      lineWidth: s.lineWidth
    }
    return {
      ...options,
      smooth,
      point,
      lineStyle
    }
  }

  protected configCustomColors(chart: Chart, options: LineOptions): LineOptions {
    const basicStyle = parseJson(chart.customAttr).basicStyle
    const color = basicStyle.colors.map(item => hexColorToRGBA(item, basicStyle.alpha))
    return {
      ...options,
      color
    }
  }

  protected configYAxis(chart: Chart, options: LineOptions): LineOptions {
    const tmpOptions = super.configYAxis(chart, options)
    if (!tmpOptions.yAxis) {
      return tmpOptions
    }
    const yAxis = parseJson(chart.customStyle).yAxis
    if (tmpOptions.yAxis.label) {
      tmpOptions.yAxis.label.formatter = value => {
        return valueFormatter(value, yAxis.axisLabelFormatter)
      }
    }
    return tmpOptions
  }

  protected setupOptions(chart: Chart, options: LineOptions): LineOptions {
    return flow(
      this.configTheme,
      this.configLabel,
      this.configTooltip,
      this.configBasicStyle,
      this.configCustomColors,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configSlider,
      this.configAnalyse
    )(chart, options)
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    return this.setupVerticalAxis(chart)
  }

  constructor(name = 'line') {
    super(name, DEFAULT_DATA)
  }
}
