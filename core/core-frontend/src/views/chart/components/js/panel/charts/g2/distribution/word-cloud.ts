import {
  filterChartDataByRange,
  flow,
  getMaxAndMinValueByData,
  hexColorToRGBA,
  parseJson
} from '@/views/chart/components/js/util'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { useI18n } from '@/hooks/web/useI18n'
import { defaultsDeep, isEmpty } from 'lodash-es'
import { DEFAULT_MISC } from '@/views/chart/components/editor/util/chart'
import { Chart as G2Chart, G2Spec } from '@antv/g2'
import { G2ChartView, G2DrawOptions } from '../../../types/impl/g2'
import { TOOLTIP_ITEM_TPL, TOOLTIP_TITLE_TPL } from '../../../common/common_antv'

const { t } = useI18n()
const DEFAULT_DATA = []

/**
 * 词云图
 */
export class WordCloud extends G2ChartView {
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
  async drawChart(drawOptions: G2DrawOptions<G2Chart>): Promise<G2Chart> {
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
      const initOptions: G2Spec = {
        data,
        type: 'wordCloud',
        autoFit: true,
        layout: {
          spiral: 'rectangular',
          rotate: 0,
          padding: misc.wordSpacing ?? DEFAULT_MISC.wordSpacing,
          fontSize: misc.wordSizeRange ?? DEFAULT_MISC.wordSizeRange,
          random: 0.5
        },
        encode: {
          color: 'field',
          size: 'value',
          text: 'field'
        },
        legend: false,
        axis: false
      }
      const options = this.setupOptions(chart, initOptions)
      const newChart = new G2Chart({ container })
      newChart.options(options)
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

  protected configTheme(chart: Chart, options: G2Spec): G2Spec {
    const customAttr = parseJson(chart.customAttr)
    const colors: string[] = []
    if (customAttr.basicStyle) {
      const basicStyle = customAttr.basicStyle
      basicStyle.colors.forEach(ele => {
        colors.push(hexColorToRGBA(ele, basicStyle.alpha))
      })
    }
    const customStyle = parseJson(chart.customStyle)
    let bgColor
    if (customStyle.background) {
      bgColor = hexColorToRGBA(customStyle.background.color, customStyle.background.alpha)
    }
    const theme = {
      color: colors[0],
      category10: colors,
      category20: colors,
      view: {
        viewFill: bgColor
      }
    }
    return { ...options, theme }
  }

  protected configTooltip(chart: Chart, options: G2Spec): G2Spec {
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
    let g2TooltipWrapper = document.getElementById('G2-TOOLTIP-WRAPPER')
    if (!g2TooltipWrapper) {
      g2TooltipWrapper = document.createElement('div')
      g2TooltipWrapper.id = 'G2-TOOLTIP-WRAPPER'
      g2TooltipWrapper.style.position = 'absolute'
      g2TooltipWrapper.style.pointerEvents = 'none'
      g2TooltipWrapper.style.zIndex = '9999'
      document.body.appendChild(g2TooltipWrapper)
    }
    const tooltipOptions: G2Spec = {
      tooltip: d => d,
      interaction: {
        tooltip: {
          mount: g2TooltipWrapper,
          css: {
            '.g2-tooltip': {
              background: tooltipAttr.backgroundColor
            },
            '.g2-tooltip-title': {
              color: tooltipAttr.color,
              'font-size': `${tooltipAttr.fontSize}px`
            },
            '.g2-tooltip-list-item-name-label': {
              color: tooltipAttr.color,
              'font-size': `${tooltipAttr.fontSize}px`
            },
            '.g2-tooltip-list-item-value': {
              color: tooltipAttr.color,
              'font-size': `${tooltipAttr.fontSize}px`
            }
          },
          render: (e, { title, items: originalItems }) => {
            const titleHtml = TOOLTIP_TITLE_TPL.replace('{title}', title)
            let tooltipItems = originalItems
            if (tooltipAttr.seriesTooltipFormatter?.length) {
              tooltipItems = originalItems.filter(item => formatterMap[item.quotaList[0].id])
            }
            const result = []
            const head = originalItems[0]
            tooltipItems.forEach(item => {
              const formatter = formatterMap[item.quotaList[0].id] ?? yAxis[0]
              const value = valueFormatter(item.value, formatter.formatterCfg)
              const name = isEmpty(formatter.chartShowName)
                ? formatter.name
                : formatter.chartShowName
              result.push({ ...item, name, value })
            })
            head.dynamicTooltipValue?.forEach(item => {
              const formatter = formatterMap[item.fieldId]
              if (formatter) {
                const value = valueFormatter(parseFloat(item.value), formatter.formatterCfg)
                const name = isEmpty(formatter.chartShowName)
                  ? formatter.name
                  : formatter.chartShowName
                result.push({ color: 'grey', name, value })
              }
            })
            const itemsHtml = result
              .map(item => {
                const marker = item.color
                const label = item.name
                const value = item.value
                return TOOLTIP_ITEM_TPL.replace('{marker}', marker)
                  .replace('{label}', label)
                  .replace('{value}', value)
              })
              .join('')
            const listHtml = `<ul class="g2-tooltip-list" style="margin: 0px; list-style-type: none; padding: 0px;">${itemsHtml}</ul>`
            return `${titleHtml}${listHtml}`
          }
        }
      }
    }
    defaultsDeep(options, tooltipOptions)
    return options
  }

  protected setupOptions(chart: Chart, options: G2Spec): G2Spec {
    return flow(this.configTooltip)(chart, options)
  }

  constructor() {
    super('word-cloud', DEFAULT_DATA)
  }
}
