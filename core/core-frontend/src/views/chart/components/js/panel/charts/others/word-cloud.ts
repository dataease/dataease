import {
  G2PlotChartView,
  G2PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/g2plot'
import { WordCloud as G2WordCloud, WordCloudOptions } from '@antv/g2plot/esm/plots/word-cloud'
import { flow, parseJson } from '@/views/chart/components/js/util'
import { getPadding } from '@/views/chart/components/js/panel/common/common_antv'
import { formatterItem, valueFormatter } from '@/views/chart/components/js/formatter'
import { Datum } from '@antv/g2plot/esm/types/common'
import { useI18n } from '@/hooks/web/useI18n'

const { t } = useI18n()
const DEFAULT_DATA = []
/**
 * 词云图
 */
export class WordCloud extends G2PlotChartView<WordCloudOptions, G2WordCloud> {
  properties: EditorProperty[] = [
    'basic-style-selector',
    'background-overall-component',
    'title-selector',
    'tooltip-selector'
  ]
  propertyInner: EditorPropertyInner = {
    'background-overall-component': ['all'],
    'basic-style-selector': ['colors', 'alpha'],
    'title-selector': [
      'title',
      'fontSize',
      'color',
      'hPosition',
      'isItalic',
      'isBolder',
      'remarkShow',
      'fontFamily',
      'letterSpace',
      'fontShadow'
    ],
    'tooltip-selector': ['color', 'fontSize', 'backgroundColor']
  }
  axis: AxisType[] = ['xAxis', 'yAxis', 'filter']
  axisConfig: AxisConfig = {
    xAxis: {
      name: `${t('chart.drag_block_word_cloud_label')} / ${t('chart.dimension_or_quota')}`,
      type: 'd',
      limit: 1
    },
    yAxis: {
      name: `${t('chart.drag_block_word_cloud_size')} / ${t('chart.quota')}`,
      type: 'q',
      limit: 1
    }
  }
  drawChart(drawOptions: G2PlotDrawOptions<G2WordCloud>): G2WordCloud {
    const { chart, container, action } = drawOptions
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
      const newChart = new G2WordCloud(container, options)
      newChart.on('point:click', action)
      return newChart
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
