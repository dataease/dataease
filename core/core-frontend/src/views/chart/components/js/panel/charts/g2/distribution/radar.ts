import { flow, hexColorToRGBA, parseJson } from '@/views/chart/components/js/util'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { useI18n } from '@/hooks/web/useI18n'
import { defaultsDeep, isEmpty } from 'lodash-es'
import { DEFAULT_LABEL } from '@/views/chart/components/editor/util/chart'
import { Chart as G2Chart, G2Spec } from '@antv/g2'
import { G2ChartView, G2DrawOptions } from '../../../types/impl/g2'
import { TOOLTIP_ITEM_TPL, TOOLTIP_TITLE_TPL } from '../../../common/common_antv'

const { t } = useI18n()

export class Radar extends G2ChartView {
  properties: EditorProperty[] = [
    'background-overall-component',
    'border-style',
    'basic-style-selector',
    'label-selector',
    'tooltip-selector',
    'title-selector',
    'legend-selector',
    'misc-style-selector',
    'jump-set',
    'linkage'
  ]
  propertyInner: EditorPropertyInner = {
    'basic-style-selector': [
      'colors',
      'alpha',
      'radarShape',
      'seriesColor',
      'radarShowPoint',
      'radarPointSize',
      'radarAreaColor'
    ],
    'label-selector': ['seriesLabelFormatter'],
    'tooltip-selector': ['color', 'fontSize', 'backgroundColor', 'seriesTooltipFormatter', 'show'],
    'misc-style-selector': ['showName', 'color', 'fontSize', 'axisColor', 'axisValue'],
    'title-selector': [
      'show',
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
    'legend-selector': ['icon', 'orient', 'color', 'fontSize', 'hPosition', 'vPosition']
  }
  selectorSpec: EditorSelectorSpec = {
    ...this['selectorSpec'],
    'misc-style-selector': {
      title: `${t('chart.tooltip_axis')}`
    }
  }
  axis: AxisType[] = ['xAxis', 'yAxis', 'drill', 'filter', 'extLabel', 'extTooltip']
  axisConfig: AxisConfig = {
    xAxis: {
      name: `${t('chart.drag_block_radar_label')} / ${t('chart.dimension')}`,
      type: 'd'
    },
    yAxis: {
      name: `${t('chart.drag_block_radar_length')} / ${t('chart.quota')}`,
      type: 'q'
    }
  }

  async drawChart(drawOptions: G2DrawOptions<G2Chart>): Promise<G2Chart> {
    const { chart, container, action } = drawOptions
    if (!chart.data?.data) {
      return
    }
    const data = chart.data.data
    const baseOptions: G2Spec = {
      type: 'view',
      autoFit: true,
      data,
      coordinate: { type: 'polar' },
      children: [{ zIndex: 1, type: 'line', style: { lineWidth: 2 } }],
      encode: {
        x: 'field',
        y: 'value',
        color: 'category'
      },
      scale: {
        x: {
          padding: 0.5,
          align: 0
        },
        y: {
          nice: true
        }
      }
    }
    const options = this.setupOptions(chart, baseOptions)
    const newChart = new G2Chart({ container })
    newChart.options(options)
    newChart.on('point:click', action)
    if (options.children[0].labels?.length) {
      newChart.on('label:click', e => {
        action({
          x: e.x,
          y: e.y,
          data: {
            data: e.target.attrs.data
          }
        })
      })
    }
    return newChart
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

  protected configColor(chart: Chart, options: G2Spec): G2Spec {
    const basicStyle = parseJson(chart.customAttr).basicStyle
    const { seriesColor } = basicStyle
    if (seriesColor?.length) {
      const { yAxis } = chart
      const seriesMap = seriesColor.reduce((p, n) => {
        p[n.id] = n
        return p
      }, {})
      const colorRelations = []
      yAxis?.forEach(axis => {
        const curAxisColor = seriesMap[axis.id]
        if (curAxisColor) {
          colorRelations.push([
            axis.name ?? axis.chartShowName,
            hexColorToRGBA(curAxisColor.color, basicStyle.alpha)
          ])
        }
      })
      if (colorRelations.length) {
        defaultsDeep(options, {
          scale: {
            color: {
              relations: colorRelations
            }
          }
        })
      }
    }
    return options
  }

  protected configBasicStyle(chart: Chart, options: G2Spec): G2Spec {
    const { radarShowPoint, radarPointSize, radarAreaColor } = parseJson(
      chart.customAttr
    ).basicStyle

    if (radarShowPoint) {
      options.children.push({
        type: 'point',
        encode: {
          shape: 'point',
          size: radarPointSize
        },
        tooltip: false
      })
    }
    if (radarAreaColor) {
      options.children.push({
        type: 'area',
        style: {
          opacity: 0.5
        },
        tooltip: false
      })
    }
    return options
  }

  protected configLabel(chart: Chart, options: G2Spec): G2Spec {
    const labelAttr = parseJson(chart.customAttr).label
    if (!labelAttr.show) {
      return options
    }
    const formatterMap = labelAttr.seriesLabelFormatter?.reduce((pre, next) => {
      pre[next.id] = next
      return pre
    }, {})
    const lineMark = options.children[0]
    const label = {
      text: 'value',
      transform: labelAttr.fullDisplay === true ? [] : [{ type: 'overlapHide' }],
      style: {
        fill: data => {
          const color = DEFAULT_LABEL.color
          if (!labelAttr.seriesLabelFormatter?.length) {
            return color
          }
          const labelCfg = formatterMap?.[data.quotaList[0].id] as SeriesFormatter
          if (!labelCfg?.show) {
            return color
          }
          return labelCfg.color
        },
        fontSize: data => {
          const fontSize = DEFAULT_LABEL.fontSize
          if (!labelAttr.seriesLabelFormatter?.length) {
            return fontSize
          }
          const labelCfg = formatterMap?.[data.quotaList[0].id] as SeriesFormatter
          if (!labelCfg?.show) {
            return fontSize
          }
          return labelCfg.fontSize
        },
        opacity: 1
      },
      formatter: (value, data) => {
        if (!labelAttr.seriesLabelFormatter?.length) {
          return value
        }
        const labelCfg = formatterMap?.[data.quotaList[0].id] as SeriesFormatter
        if (!labelCfg) {
          return value
        }
        if (!labelCfg.show) {
          return ''
        }
        const result = valueFormatter(value, labelCfg.formatterCfg)
        return result
      }
    }
    lineMark.labels = [label]
    return options
  }

  protected configAxis(chart: Chart, options: G2Spec): G2Spec {
    const customAttr = parseJson(chart.customAttr)
    const customStyle = parseJson(chart.customStyle)
    const basicStyle = customAttr.basicStyle
    const misc = customStyle.misc
    const axis = {
      axis: {
        x: {
          grid: true,
          tick: true,
          tickLength: 10,
          tickStrokeOpacity: 0,
          label: misc.showName,
          labelFill: misc.color,
          labelFontSize: misc.fontSize,
          labelAlign: 'horizontal',
          labelOpacity: 1,
          gridLineDash: [0, 0],
          gridStroke: misc.axisColor,
          gridStrokeOpacity: 1,
          gridLineWidth: 1
        },
        y: {
          zIndex: -1,
          title: false,
          labelFilter: () => false,
          gridConnect: basicStyle.radarShape === 'polygon' ? 'line' : undefined,
          gridStroke: misc.axisColor,
          gridStrokeOpacity: 1,
          gridLineWidth: 1,
          gridLineDash: [0, 0]
        }
      }
    }
    defaultsDeep(options, axis)
    const axisValue = misc.axisValue
    if (!axisValue?.auto) {
      const yScale = {
        scale: {
          y: {
            domainMin: axisValue.min,
            domainMax: axisValue.max,
            tickCount: axisValue.splitCount,
            tickMethod: (min, max, count) => {
              const step = (max - min) / count
              const ticks = []
              for (let i = 0; i <= count; i++) {
                ticks.push(min + step * i)
              }
              return ticks
            }
          }
        }
      }
      defaultsDeep(options, yScale)
    }
    return options
  }

  protected configLegend(chart: Chart, options: G2Spec): G2Spec {
    const { legend } = parseJson(chart.customStyle)
    if (!legend.show) {
      return { ...options, legend: false }
    }
    const baseLegend = this.getLegend(chart)
    const tmpLegend = {
      legend: {
        color: {
          ...baseLegend,
          itemMarkerSize: legend.size,
          itemMarker: legend.icon
        }
      }
    }
    defaultsDeep(options, tmpLegend)
    return options
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
    const lineMark = options.children[0]
    const tooltipOptions: G2Spec = {
      tooltip: d => d,
      interaction: {
        tooltip: {
          crosshairsLineDash: [4, 4],
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
    defaultsDeep(lineMark, tooltipOptions)
    return options
  }

  protected setupOptions(chart: Chart, options: G2Spec): G2Spec {
    return flow(
      this.configTheme,
      this.configColor,
      this.configLabel,
      this.configLegend,
      this.configTooltip,
      this.configAxis,
      this.configBasicStyle
    )(chart, options, {}, this)
  }

  constructor() {
    super('radar', [])
  }
}
