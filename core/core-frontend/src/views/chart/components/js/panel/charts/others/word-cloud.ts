import {
  G2PlotChartView,
  G2PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/g2plot'
import type { WordCloud as G2WordCloud, WordCloudOptions } from '@antv/g2plot/esm/plots/word-cloud'
import {
  filterChartDataByRange,
  flow,
  getMaxAndMinValueByData,
  parseJson
} from '@/views/chart/components/js/util'
import { getPadding } from '@/views/chart/components/js/panel/common/common_antv'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { useI18n } from '@/hooks/web/useI18n'
import { isEmpty } from 'lodash-es'
import { DEFAULT_MISC } from '@/views/chart/components/editor/util/chart'

const { t } = useI18n()
const DEFAULT_DATA = []
/**
 * 词云图
 */
export class WordCloud extends G2PlotChartView<WordCloudOptions, G2WordCloud> {
  properties: EditorProperty[] = [
    'basic-style-selector',
    'background-overall-component',
    'border-style',
    'title-selector',
    'tooltip-selector',
    'misc-selector',
    'jump-set',
    'linkage'
  ]
  propertyInner: EditorPropertyInner = {
    'background-overall-component': ['all'],
    'border-style': ['all'],
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
    'misc-selector': ['wordSizeRange', 'wordSpacing', 'wordCloudAxisValueRange'],
    'tooltip-selector': ['color', 'fontSize', 'backgroundColor', 'seriesTooltipFormatter', 'show']
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
  setDataRange = (action, maxValue, minValue) => {
    action({
      from: 'word-cloud',
      data: {
        max: maxValue,
        min: minValue
      }
    })
  }
  async drawChart(drawOptions: G2PlotDrawOptions<G2WordCloud>): Promise<G2WordCloud> {
    const { chart, container, action } = drawOptions
    if (chart?.data) {
      // data
      let data = chart.data.data
      const { misc } = parseJson(chart.customAttr)
      let minValue = 0
      let maxValue = 0
      if (
        !misc.wordCloudAxisValueRange?.auto &&
        misc.wordCloudAxisValueRange?.fieldId === chart.yAxis[0].id
      ) {
        minValue = misc.wordCloudAxisValueRange.min
        maxValue = misc.wordCloudAxisValueRange.max
      }
      getMaxAndMinValueByData(data ?? [], 'value', maxValue, minValue, (max, min) => {
        maxValue = max
        minValue = min
      })
      data = filterChartDataByRange(data ?? [], maxValue, minValue)
      // options
      const initOptions: WordCloudOptions = {
        data: data,
        wordField: 'field',
        weightField: 'value',
        colorField: 'field',
        wordStyle: {
          fontFamily: 'Verdana',
          fontSize: (misc.wordSizeRange ?? DEFAULT_MISC.wordSizeRange) as [number, number],
          rotation: [0, 0],
          padding: misc.wordSpacing ?? DEFAULT_MISC.wordSpacing
        },
        random: () => 0.5,
        appendPadding: getPadding(chart),
        legend: false,
        interactions: []
      }
      const options = this.setupOptions(chart, initOptions)
      const { WordCloud: G2WordCloud } = await import('@antv/g2plot/esm/plots/word-cloud')
      const newChart = new G2WordCloud(container, options)
      newChart.on('click', () => {
        this.setDataRange(action, maxValue, minValue)
      })
      newChart.on('afterrender', () => {
        this.setDataRange(action, maxValue, minValue)
      })
      newChart.on('point:click', param => {
        action({ x: param.x, y: param.y, data: { data: param.data.data.datum } })
      })
      return newChart
    }
  }

  protected configTooltip(chart: Chart, options: WordCloudOptions): WordCloudOptions {
    const customAttr: DeepPartial<ChartAttr> = parseJson(chart.customAttr)
    const tooltipAttr = customAttr.tooltip
    const yAxis = chart.yAxis
    if (!tooltipAttr.show) {
      return {
        ...options,
        tooltip: false
      }
    }
    const formatterMap = tooltipAttr.seriesTooltipFormatter
      ?.filter(i => i.show)
      .reduce((pre, next) => {
        pre[next.id] = next
        return pre
      }, {}) as Record<string, SeriesFormatter>
    const tooltip: WordCloudOptions['tooltip'] = {
      showTitle: true,
      title: () => undefined,
      customItems(originalItems) {
        let tooltipItems = originalItems
        if (tooltipAttr.seriesTooltipFormatter?.length) {
          tooltipItems = originalItems.filter(item => formatterMap[item.data.datum.quotaList[0].id])
        }
        const result = []
        const head = originalItems[0]
        tooltipItems.forEach(item => {
          const formatter = formatterMap[item.data.datum.quotaList[0].id] ?? yAxis[0]
          const value = valueFormatter(item.value, formatter.formatterCfg)
          const name = isEmpty(formatter.chartShowName) ? formatter.name : formatter.chartShowName
          result.push({ ...item, name, value })
        })
        head.data.datum.dynamicTooltipValue?.forEach(item => {
          const formatter = formatterMap[item.fieldId]
          if (formatter) {
            const value = valueFormatter(parseFloat(item.value), formatter.formatterCfg)
            const name = isEmpty(formatter.chartShowName) ? formatter.name : formatter.chartShowName
            result.push({ color: 'grey', name, value })
          }
        })
        return result
      }
    }
    return {
      ...options,
      tooltip
    }
  }

  protected setupOptions(chart: Chart, options: WordCloudOptions): WordCloudOptions {
    return flow(this.configTheme, this.configTooltip)(chart, options)
  }

  constructor() {
    super('word-cloud', DEFAULT_DATA)
  }
}
