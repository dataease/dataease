import type {
  Bullet as G2Bullet,
  BulletOptions as G2BulletOptions
} from '@antv/g2plot/esm/plots/bullet'
import {
  G2PlotChartView,
  G2PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/g2plot'
import {
  BAR_AXIS_TYPE,
  BAR_EDITOR_PROPERTY,
  BAR_EDITOR_PROPERTY_INNER
} from '@/views/chart/components/js/panel/charts/bar/common'
import { useI18n } from '@/hooks/web/useI18n'
import { flow, parseJson } from '@/views/chart/components/js/util'
import { BulletOptions } from '@antv/g2plot'
import { isEmpty } from 'lodash-es'
import {
  configAxisLabelLengthLimit,
  configPlotTooltipEvent,
  getPadding,
  getTooltipContainer,
  TOOLTIP_TPL
} from '@/views/chart/components/js/panel/common/common_antv'
import { valueFormatter } from '@/views/chart/components/js/formatter'

const { t } = useI18n()

/**
 * 子弹图
 */
export class BulletGraph extends G2PlotChartView<G2BulletOptions, G2Bullet> {
  constructor() {
    super('bullet-graph', [])
  }

  axis: AxisType[] = [...BAR_AXIS_TYPE, 'yAxisExt', 'extBubble']
  axisConfig = {
    ...this['axisConfig'],
    xAxis: { name: `${t('chart.form_type')} / ${t('chart.dimension')}`, type: 'd', limit: 1 },
    yAxis: { name: `${t('chart.progress_current')} / ${t('chart.quota')}`, type: 'q', limit: 1 },
    yAxisExt: { name: `${t('chart.progress_target')} / ${t('chart.quota')}`, type: 'q', limit: 1 },
    extBubble: {
      name: `${t('chart.range_bg')} / ${t('chart.quota')}`,
      type: 'q',
      allowEmpty: true,
      limit: 1
    }
  }
  properties: EditorProperty[] = [
    ...BAR_EDITOR_PROPERTY.filter(
      item => !['function-cfg', 'assist-line', 'threshold'].includes(item)
    ),
    'bullet-graph-selector'
  ]
  propertyInner = {
    'basic-style-selector': ['radiusColumnBar', 'layout'],
    'label-selector': ['hPosition', 'fontSize', 'color', 'labelFormatter'],
    'tooltip-selector': ['fontSize', 'color', 'backgroundColor', 'seriesTooltipFormatter', 'show'],
    'x-axis-selector': [
      ...BAR_EDITOR_PROPERTY_INNER['x-axis-selector'].filter(item => item != 'position'),
      'showLengthLimit'
    ],
    'y-axis-selector': [
      ...BAR_EDITOR_PROPERTY_INNER['y-axis-selector'].filter(
        item => item !== 'axisValue' && item !== 'position'
      ),
      'axisLabelFormatter'
    ],
    'legend-selector': ['showRange', 'orient', 'fontSize', 'color', 'hPosition', 'vPosition']
  }

  async drawChart(drawOption: G2PlotDrawOptions<G2Bullet>): Promise<G2Bullet> {
    const { chart, container, action } = drawOption
    if (!chart.data?.data?.length) return
    const result = mergeBulletData(chart)
    // 处理自定义区间
    const { bullet } = parseJson(chart.customAttr).misc
    if (bullet.bar.ranges.showType === 'fixed') {
      const customRange = bullet.bar.ranges.fixedRange?.map(item => item.fixedRangeValue) || [0]
      result.forEach(item => (item.ranges = customRange))
    } else {
      result.forEach(item => (item.ranges = item.originalRanges))
    }
    // 处理自定义目标值
    if (bullet.bar.target.showType === 'fixed') {
      const customTarget = bullet.bar.target.value || 0
      result.forEach(item => (item.target = customTarget))
    } else {
      result.forEach(item => (item.target = item.originalTarget))
    }
    const initialOptions: BulletOptions = {
      appendPadding: getPadding(chart),
      data: result.reverse(),
      measureField: 'measures',
      rangeField: 'ranges',
      targetField: 'target',
      xField: 'title',
      meta: {
        title: {
          type: 'cat'
        }
      },
      interactions: [
        {
          type: 'active-region',
          cfg: {
            start: [{ trigger: 'element:mousemove', action: 'active-region:show' }],
            end: [{ trigger: 'element:mouseleave', action: 'active-region:hide' }]
          }
        }
      ]
    }
    const options = this.setupOptions(chart, initialOptions)
    let newChart = null
    const { Bullet: BulletClass } = await import('@antv/g2plot/esm/plots/bullet')
    newChart = new BulletClass(container, options)
    newChart.on('element:click', ev => {
      const pointData = ev?.data?.data
      const dimensionList = options.data.find(item => item.title === pointData.title)?.dimensionList
      const actionParams = {
        x: ev.x,
        y: ev.y,
        data: {
          data: {
            ...pointData,
            dimensionList
          }
        }
      }
      action(actionParams)
    })
    configPlotTooltipEvent(chart, newChart)
    configAxisLabelLengthLimit(chart, newChart, null)
    return newChart
  }

  protected configBasicStyle(chart: Chart, options: BulletOptions): BulletOptions {
    const basicStyle = parseJson(chart.customAttr).basicStyle
    const { radiusColumnBar, columnBarRightAngleRadius, layout } = basicStyle
    let radiusValue = 0
    let rangeLength = 1
    if (radiusColumnBar === 'roundAngle' || radiusColumnBar === 'topRoundAngle') {
      radiusValue = columnBarRightAngleRadius
      rangeLength = options.data[0]?.ranges?.length
    }
    const barRadiusStyle = { radius: Array(2).fill(radiusValue) }
    const baseRadius = [...barRadiusStyle.radius, ...barRadiusStyle.radius]
    options = {
      ...options,
      bulletStyle: {
        range: datum => {
          if (!datum.rKey) return { fill: 'rgba(0, 0, 0, 0)' }
          if (rangeLength === 1) {
            return {
              radius:
                radiusColumnBar === 'topRoundAngle' ? [...barRadiusStyle.radius, 0, 0] : baseRadius
            }
          }
          if (rangeLength > 1 && datum.rKey === 'ranges_0') {
            return {
              radius: radiusColumnBar === 'topRoundAngle' ? [] : [0, 0, ...barRadiusStyle.radius]
            }
          }
          if (rangeLength > 1 && datum.rKey === 'ranges_' + (rangeLength - 1)) {
            return { radius: [...barRadiusStyle.radius, 0, 0] }
          }
        },
        measure: datum => {
          if (datum.measures) {
            return {
              radius:
                radiusColumnBar === 'topRoundAngle' ? [...barRadiusStyle.radius, 0, 0] : baseRadius
            }
          } else {
            return undefined
          }
        },
        target: datum => (datum.tKey === 'target' ? { lineWidth: 2 } : undefined)
      }
    }
    if (layout === 'vertical') options = { ...options, layout: 'vertical' }
    return options
  }

  protected configMisc(chart: Chart, options: BulletOptions): BulletOptions {
    const { bullet } = parseJson(chart.customAttr).misc
    const isDynamic = bullet.bar.ranges.showType === 'dynamic'
    // 动态背景按大小升序
    const rangeColor = isDynamic
      ? bullet.bar.ranges.fill
      : bullet.bar.ranges.fixedRange
          ?.sort((a, b) => (a.fixedRangeValue ?? 0) - (b.fixedRangeValue ?? 0))
          .map(item => item.fill) || []
    return {
      ...options,
      color: {
        measure: [].concat(bullet.bar.measures.fill),
        range: [].concat(rangeColor),
        target: [].concat(bullet.bar.target.fill)
      },
      size: {
        measure: bullet.bar.measures.size,
        range: bullet.bar.ranges.size,
        target: bullet.bar.target.size
      }
    }
  }

  protected configXAxis(chart: Chart, options: BulletOptions): BulletOptions {
    const tmpOptions = super.configXAxis(chart, options)
    if (!tmpOptions.xAxis || !tmpOptions.xAxis.label) return tmpOptions

    const { layout, xAxis } = tmpOptions
    const position = xAxis.position
    const style: any = { ...xAxis.label.style }

    if (layout === 'vertical') {
      style.textAlign = 'center'
      style.textBaseline = position === 'bottom' ? 'top' : 'bottom'
    } else {
      style.textAlign = position === 'bottom' ? 'end' : 'start'
      style.textBaseline = 'middle'
    }

    xAxis.label.style = style
    return tmpOptions
  }

  protected configYAxis(chart: Chart, options: BulletOptions): BulletOptions {
    const tmpOptions = super.configYAxis(chart, options)
    if (!tmpOptions.yAxis || !tmpOptions.yAxis.label) return tmpOptions

    const yAxis = parseJson(chart.customStyle).yAxis
    tmpOptions.yAxis.label.formatter = value => valueFormatter(value, yAxis.axisLabelFormatter)

    const { layout, yAxis: yAxisConfig } = tmpOptions
    const position = yAxisConfig.position
    const style: any = { ...yAxisConfig.label.style }

    if (layout === 'vertical') {
      style.textAlign = position === 'left' ? 'end' : 'start'
      style.textBaseline = 'middle'
    } else {
      style.textAlign = 'center'
      style.textBaseline = position === 'left' ? 'top' : 'bottom'
    }

    yAxisConfig.label.style = style
    return tmpOptions
  }

  protected configLabel(chart: Chart, options: BulletOptions): BulletOptions {
    const tmpOptions = super.configLabel(chart, options)
    if (!tmpOptions.label) return tmpOptions

    const labelAttr = parseJson(chart.customAttr).label
    const label: any = {
      ...tmpOptions.label,
      formatter: param =>
        param.mKey === 'measures'
          ? valueFormatter(param.measures, labelAttr.labelFormatter)
          : undefined
    }
    return { ...tmpOptions, label: { measure: label } }
  }

  protected configLegend(chart: Chart, options: BulletOptions): BulletOptions {
    const baseLegend = super.configLegend(chart, options).legend
    if (!baseLegend) return options

    const { bullet } = parseJson(chart.customAttr).misc
    const customStyleLegend = parseJson(chart.customStyle).legend
    const items = []

    const createLegendItem = (value, name, symbol, fill, size = 4) => ({
      value,
      name,
      marker: { symbol, style: { fill, stroke: value === 'measure' ? '' : fill, r: size } }
    })

    if (customStyleLegend.showRange) {
      if (bullet.bar.ranges.showType === 'dynamic') {
        if (chart.extBubble.length) {
          const rangeName = chart.extBubble[0]?.chartShowName || bullet.bar.ranges.name
          items.push(
            createLegendItem(
              'dynamic',
              rangeName || chart.extBubble[0]?.name,
              bullet.bar.ranges.symbol,
              [].concat(bullet.bar.ranges.fill)[0],
              bullet.bar.ranges.symbolSize
            )
          )
        }
      } else {
        bullet.bar.ranges.fixedRange?.forEach(item => {
          items.push(
            createLegendItem(
              item.name,
              item.name,
              bullet.bar.ranges.symbol,
              item.fill,
              bullet.bar.ranges.symbolSize
            )
          )
        })
      }
    }

    const targetName = chart.yAxisExt[0]?.chartShowName || bullet.bar.target.name
    items.push(
      createLegendItem(
        'target',
        targetName || chart.yAxisExt[0]?.name,
        'line',
        [].concat(bullet.bar.target.fill)[0],
        bullet.bar.ranges.symbolSize
      )
    )

    const measureName = chart.yAxis[0]?.chartShowName || bullet.bar.measures.name
    items.push(
      createLegendItem(
        'measure',
        measureName || chart.yAxis[0]?.name,
        'square',
        [].concat(bullet.bar.measures.fill)[0],
        bullet.bar.ranges.symbolSize
      )
    )

    return {
      ...options,
      legend: { custom: true, position: baseLegend.position, layout: baseLegend.layout, items }
    }
  }

  protected configTooltip(chart: Chart, options: BulletOptions): BulletOptions {
    const customAttr: DeepPartial<ChartAttr> = parseJson(chart.customAttr)
    const tooltipAttr = customAttr.tooltip
    const { bullet } = parseJson(chart.customAttr).misc
    if (!tooltipAttr.show) return { ...options, tooltip: false }

    const formatterMap = tooltipAttr.seriesTooltipFormatter
      ?.filter(i => i.show)
      .reduce((pre, next, _index) => {
        switch (next.axisType) {
          case 'yAxis':
            pre['measures'] = next
            return pre
          case 'yAxisExt':
            pre['target'] = next
            return pre
          case 'extBubble':
            pre['ranges'] = next
            return pre
          default:
            return pre
        }
      }, {}) as Record<string, SeriesFormatter>

    const tooltip = {
      shared: true,
      showMarkers: true,
      customItems(originalItems) {
        if (!tooltipAttr.seriesTooltipFormatter?.length) return originalItems
        const isDynamic = bullet.bar.ranges.showType === 'dynamic'
        const rangeFormatter = chart.extBubble[0]
        const result = []
        const data = options.data.find(item => item.title === originalItems[0].title)
        Object.keys(formatterMap).forEach((key, _index) => {
          if (key === '记录数*') return
          const formatter = formatterMap[key]
          if (formatter) {
            let name = isEmpty(formatter.chartShowName) ? formatter.name : formatter.chartShowName
            let value = valueFormatter(parseFloat(data[key] as string), formatter.formatterCfg)
            let color = bullet.bar[key]?.fill ?? 'grey'
            if (key === 'ranges') {
              if (!isDynamic && rangeFormatter) {
                name = isEmpty(rangeFormatter.chartShowName)
                  ? rangeFormatter.name
                  : rangeFormatter.chartShowName
                value = valueFormatter(parseFloat(data.minRanges[0]), rangeFormatter.formatterCfg)
                color = 'grey'
              } else {
                return
              }
            }
            result.push({
              color,
              name,
              value
            })
          }
        })
        const ranges = data.ranges
        ranges.forEach((range, index) => {
          const value = isDynamic
            ? valueFormatter(parseFloat(data.minRanges[0]), rangeFormatter.formatterCfg)
            : (range as string)
          let name = ''
          let color: string | string[]
          if (bullet.bar.ranges.showType === 'dynamic') {
            name = isEmpty(rangeFormatter.chartShowName)
              ? rangeFormatter.name
              : rangeFormatter.chartShowName
            color = bullet.bar['ranges'].fill
          } else {
            const customRange = bullet.bar.ranges.fixedRange[index].name
            name = customRange
              ? customRange
              : isEmpty(rangeFormatter.chartShowName)
              ? rangeFormatter.name
              : rangeFormatter.chartShowName
            color = bullet.bar['ranges'].fixedRange[index].fill
          }
          result.push({ ...originalItems[0], color, name, value })
        })
        const dynamicTooltipValue = chart.data.data.find(
          d => d.field === originalItems[0]['title']
        )?.dynamicTooltipValue
        if (dynamicTooltipValue.length > 0) {
          dynamicTooltipValue.forEach(dy => {
            const q = tooltipAttr.seriesTooltipFormatter.filter(i => i.id === dy.fieldId)
            if (q && q.length > 0) {
              const value = valueFormatter(parseFloat(dy.value as string), q[0].formatterCfg)
              const name = isEmpty(q[0].chartShowName) ? q[0].name : q[0].chartShowName
              result.push({ color: 'grey', name, value })
            }
          })
        }
        result.sort((a, b) => (a.color === 'grey' ? 1 : b.color === 'grey' ? -1 : 0))
        return result
      },
      container: getTooltipContainer(`tooltip-${chart.id}`),
      itemTpl: TOOLTIP_TPL,
      enterable: true
    }
    return { ...options, tooltip }
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    chart.customAttr.label.position = 'middle'
    chart.customStyle.yAxis.splitLine.show = false
    return super.setupDefaultOptions(chart)
  }

  protected setupOptions(chart: Chart, options: BulletOptions): BulletOptions {
    return flow(
      this.configTheme,
      this.configBasicStyle,
      this.configMisc,
      this.configXAxis,
      this.configYAxis,
      this.configLabel,
      this.configLegend,
      this.configTooltip
    )(chart, options, {}, this)
  }
}

/**
 * 组装子弹图数据
 * @param chart
 */
function mergeBulletData(chart): any[] {
  // 先根据维度分组，再根据指标字段组装成子弹图的格式
  const groupedData = chart.data.data.reduce((acc, item) => {
    const field = item.field
    if (!acc[field]) {
      acc[field] = []
    }
    acc[field].push(item)
    return acc
  }, {})
  const result = []
  // 组装子弹图数据，每个维度对应一个子弹图
  Object.keys(groupedData).forEach(field => {
    const items = groupedData[field]
    // 初始化子弹图条目结构
    const entry = {
      title: field,
      ranges: [],
      measures: [],
      target: [],
      dimensionList: items[0].dimensionList,
      quotaList: []
    }

    // 防止指标相同时无数据有可能会导致数据不一致
    items.forEach(item => {
      const quotaId = item.quotaList[0]?.id
      const v = item.value || 0
      if (quotaId === chart.yAxis[0]?.id) {
        entry.measures.push(v)
      }
      if (quotaId === chart.yAxisExt[0]?.id) {
        entry.target.push(v)
      }
      if (quotaId === chart.extBubble[0]?.id) {
        entry.ranges.push(v)
      }
      entry.quotaList.push(item.quotaList[0])
    })
    // 对数据进行累加
    const ranges = chart.extBubble[0]?.id
      ? [].concat(entry.ranges?.reduce((acc, curr) => acc + curr, 0))
      : []
    const target = [].concat(entry.target?.reduce((acc, curr) => acc + curr, 0))
    const measures = [].concat(entry.measures?.reduce((acc, curr) => acc + curr, 0))
    const bulletData = {
      ...entry,
      measures: measures,
      target: target,
      ranges: ranges,
      quotaList: [...entry.quotaList],
      minRanges: ranges,
      originalRanges: ranges,
      originalTarget: target
    }
    result.push(bulletData)
  })
  return result
}
