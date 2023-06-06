import { G2PlotChartView, G2PlotDrawOptions } from '@/views/chart/components/js/panel/types'
import { WordCloud as G2WordCloud, WordCloudOptions } from '@antv/g2plot'
import { flow } from '@/views/chart/components/js/util'
import { getPadding } from '@/views/chart/components/js/panel/common/common_antv'

const DEFAULT_DATA = []
export class WordCloud extends G2PlotChartView<WordCloudOptions, G2WordCloud> {
  drawChart(drawOptions: G2PlotDrawOptions<G2WordCloud>): G2WordCloud {
    const chart = drawOptions.chart
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

  protected setupOptions(chart: Chart, options: WordCloudOptions): WordCloudOptions {
    return flow(this.configTheme, this.configTooltip)(chart, options)
  }

  constructor() {
    super('word-cloud', DEFAULT_DATA)
  }
}
