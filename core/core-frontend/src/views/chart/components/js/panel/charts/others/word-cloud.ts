import { G2PlotChartView, G2PlotDrawOptions } from '@/views/chart/components/js/panel/types'
import { Datum, WordCloud as G2WordCloud, WordCloudOptions } from '@antv/g2plot'
import { flow, parseJson } from '@/views/chart/components/js/util'
import { getPadding } from '@/views/chart/components/js/panel/common/common_antv'
import { formatterItem, valueFormatter } from '@/views/chart/components/js/formatter'

const DEFAULT_DATA = []
export class WordCloud extends G2PlotChartView<WordCloudOptions, G2WordCloud> {
  drawChart(drawOptions: G2PlotDrawOptions<G2WordCloud>): G2WordCloud {
    const chart = drawOptions.chart
    if (chart?.data) {
      // data
      const data = chart.data.data
      // options
      const initOptions: WordCloudOptions = {
        data: data,
        wordField: 'field',
        weightField: 'value',
        colorField: 'field',
        wordStyle: {
          fontFamily: 'Verdana',
          fontSize: [8, 32],
          rotation: [0, 0],
          padding: 6
        },
        random: () => 0.5,
        appendPadding: getPadding(chart),
        legend: false,
        interactions: []
      }
      const options = this.setupOptions(chart, initOptions)

      // 开始渲染
      if (drawOptions.chartObj) {
        drawOptions.chartObj.destroy()
      }
      drawOptions.chartObj = new G2WordCloud(drawOptions.container, options)

      return drawOptions.chartObj
    }
  }

  protected configTooltip(chart: Chart, options: WordCloudOptions): WordCloudOptions {
    let tooltip
    const customAttr: DeepPartial<ChartAttr> = parseJson(chart.customAttr)
    if (customAttr.tooltip) {
      const tooltipAttr = customAttr.tooltip
      if (tooltipAttr.show) {
        tooltip = {
          formatter: function (param: Datum) {
            let res
            const obj = { name: param.text, value: param.value }
            const yAxis = chart.yAxis
            for (let i = 0; i < yAxis.length; i++) {
              const f = yAxis[i]
              if (f.formatterCfg) {
                res = valueFormatter(param.value, f.formatterCfg)
              } else {
                res = valueFormatter(param.value, formatterItem)
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

  protected setupOptions(chart: Chart, options: WordCloudOptions): WordCloudOptions {
    return flow(this.configTheme, this.configTooltip)(chart, options)
  }

  constructor() {
    super('word-cloud', DEFAULT_DATA)
  }
}
