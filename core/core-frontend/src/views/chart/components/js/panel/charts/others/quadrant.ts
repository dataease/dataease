import {
  G2PlotChartView,
  G2PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/g2plot'
import { ScatterOptions, Scatter as G2Scatter } from '@antv/g2plot/esm/plots/scatter'
import { flow, parseJson, setUpSingleDimensionSeriesColor } from '../../../util'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { useI18n } from '@/hooks/web/useI18n'
import { isEmpty, map } from 'lodash-es'
import { cloneDeep, defaultTo } from 'lodash-es'

const { t } = useI18n()
/**
 * 象限图
 */
export class Quadrant extends G2PlotChartView<ScatterOptions, G2Scatter> {
  properties: EditorProperty[] = [
    'background-overall-component',
    'basic-style-selector',
    'x-axis-selector',
    'y-axis-selector',
    'title-selector',
    'label-selector',
    'tooltip-selector',
    'legend-selector',
    'jump-set',
    'linkage',
    'quadrant-selector'
  ]
  propertyInner: EditorPropertyInner = {
    'basic-style-selector': [
      'colors',
      'alpha',
      'scatterSymbol',
      'scatterSymbolSize',
      'seriesColor'
    ],
    'label-selector': ['fontSize', 'color'],
    'tooltip-selector': ['fontSize', 'color', 'backgroundColor', 'seriesTooltipFormatter', 'show'],
    'x-axis-selector': [
      'position',
      'name',
      'color',
      'fontSize',
      'axisLine',
      'axisValue',
      'splitLine',
      'axisForm',
      'axisLabel',
      'axisLabelFormatter'
    ],
    'y-axis-selector': [
      'position',
      'name',
      'color',
      'fontSize',
      'axisValue',
      'axisLine',
      'splitLine',
      'axisForm',
      'axisLabel',
      'axisLabelFormatter'
    ],
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
    'legend-selector': ['icon', 'orient', 'color', 'fontSize', 'hPosition', 'vPosition'],
    'quadrant-selector': ['regionStyle', 'label', 'lineStyle']
  }
  axis: AxisType[] = [
    'xAxis',
    'yAxis',
    'yAxisExt',
    'extBubble',
    'filter',
    'drill',
    'extLabel',
    'extTooltip'
  ]
  axisConfig: AxisConfig = {
    ...this['axisConfig'],
    extBubble: {
      name: `${t('chart.bubble_size')} / ${t('chart.quota')}`,
      type: 'q',
      limit: 1
    },
    xAxis: {
      name: `${t('chart.form_type')} / ${t('chart.dimension')}`,
      type: 'd',
      limit: 1
    },
    yAxis: {
      name: `${t('chart.x_axis')} / ${t('chart.quota')}`,
      type: 'q',
      limit: 1
    },
    yAxisExt: {
      name: `${t('chart.y_axis')} / ${t('chart.quota')}`,
      type: 'q',
      limit: 1
    }
  }

  public getFieldObject(chart: Chart) {
    const colorFieldObj = { id: chart.xAxis[0]?.id, name: chart.xAxis[0]?.['originName'] }
    const sizeFieldObj = { id: chart.extBubble[0]?.id, name: chart.extBubble[0]?.['originName'] }
    const xFieldObj = { id: chart.yAxis[0]?.id, name: chart.yAxis[0]?.['originName'] }
    const yFieldObj = { id: chart.yAxisExt[0]?.id, name: chart.yAxisExt[0]?.['originName'] }
    return { colorFieldObj, sizeFieldObj, xFieldObj, yFieldObj }
  }
  public getUniqueObjects<T>(arr: T[]): T[] {
    return [...new Set(arr.map(JSON.stringify))].map(JSON.parse) as T[]
  }

  public drawChart(drawOptions: G2PlotDrawOptions<G2Scatter>) {
    const { chart, container, action, quadrantDefaultBaseline } = drawOptions
    if (!chart.data?.data) {
      return
    }
    // data
    const sourceData: Array<any> = cloneDeep(chart.data.data)
    const data1 = defaultTo(sourceData[0]?.data, [])
    const data2 = defaultTo(sourceData[1]?.data, [])
    const data3 = defaultTo(sourceData[2]?.data, [])
    const xData = data1.map(item => {
      return {
        ...item,
        id: item.quotaList[0]?.id,
        field: item.field,
        value: item.value
      }
    })
    const yData = data2.map(item => {
      return {
        ...item,
        id: item.quotaList[0]?.id,
        field: item.field,
        value: item.value
      }
    })
    const eData = data3.map(item => {
      return {
        ...item,
        id: item.quotaList[0]?.id,
        field: item.field,
        value: item.value
      }
    })
    // x轴基准线 默认值
    const xValues = xData.map(item => item.value)
    const xBaseline = ((Math.max(...xValues) + Math.min(...xValues)) / 2).toFixed()
    // y轴基准线 默认值
    const yValues = yData.map(item => item.value)
    const yBaseline = ((Math.max(...yValues) + Math.min(...yValues)) / 2).toFixed()
    const defaultBaselineQuadrant = {
      ...chart.customAttr['quadrant']
    }
    // 新建图表
    if (defaultBaselineQuadrant.xBaseline === undefined) {
      // 默认基准线值
      defaultBaselineQuadrant.xBaseline = xBaseline
      defaultBaselineQuadrant.yBaseline = yBaseline
    }
    const getQuotaList = d => {
      const eQuotaList = eData.find(item => item.field === d.field)?.quotaList
      const yQuotaList = yData.find(item => item.field === d.field)?.quotaList
      if (JSON.stringify(eQuotaList) === JSON.stringify(yQuotaList)) {
        return yQuotaList
      }
      return [...(eQuotaList || []), ...(yQuotaList || [])]
    }
    const data = map(defaultTo(xData, []), d => {
      return {
        ...d,
        yAxis: d.value,
        quotaList: getQuotaList(d),
        yAxisExt: yData.find(item => item.field === d.field)?.value,
        extBubble: eData.find(item => item.field === d.field)?.value
      }
    })
    const baseOptions: ScatterOptions = {
      colorField: 'field',
      quadrant: {
        ...defaultBaselineQuadrant
      },
      data: data,
      xField: 'yAxis',
      yField: 'yAxisExt',
      appendPadding: 30,
      pointStyle: {
        fillOpacity: 0.8,
        stroke: '#bbb'
      }
    }

    const options = this.setupOptions(chart, baseOptions)
    const newChart = new G2Scatter(container, options)
    newChart.on('point:click', action)
    newChart.on('click', () => quadrantDefaultBaseline(defaultBaselineQuadrant))
    newChart.on('afterrender', () => quadrantDefaultBaseline(defaultBaselineQuadrant))
    return newChart
  }

  protected configBasicStyle(chart: Chart, options: ScatterOptions): ScatterOptions {
    const customAttr = parseJson(chart.customAttr)
    const basicStyle = customAttr.basicStyle
    if (chart.extBubble?.length) {
      return {
        ...options,
        size: [4, 30],
        sizeField: 'extBubble',
        shape: basicStyle.scatterSymbol
      }
    }
    return {
      ...options,
      size: basicStyle.scatterSymbolSize,
      shape: basicStyle.scatterSymbol
    }
  }

  protected configXAxis(chart: Chart, options: ScatterOptions): ScatterOptions {
    const tmpOptions = super.configXAxis(chart, options)
    if (!tmpOptions.xAxis) {
      return tmpOptions
    }
    const xAxis = parseJson(chart.customStyle).xAxis
    if (tmpOptions.xAxis.label) {
      tmpOptions.xAxis.label.formatter = value => {
        return valueFormatter(value, xAxis.axisLabelFormatter)
      }
    }
    const axisValue = xAxis.axisValue
    if (!axisValue?.auto) {
      const axis = {
        xAxis: {
          ...tmpOptions.xAxis,
          min: axisValue.min,
          max: axisValue.max,
          minLimit: axisValue.min,
          maxLimit: axisValue.max,
          tickCount: axisValue.splitCount
        }
      }
      return { ...tmpOptions, ...axis }
    }
    return tmpOptions
  }

  protected configYAxis(chart: Chart, options: ScatterOptions): ScatterOptions {
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
    const axisValue = yAxis.axisValue
    if (!axisValue?.auto) {
      const axis = {
        yAxis: {
          ...tmpOptions.yAxis,
          min: axisValue.min,
          max: axisValue.max,
          minLimit: axisValue.min,
          maxLimit: axisValue.max,
          tickCount: axisValue.splitCount
        }
      }
      return { ...tmpOptions, ...axis }
    }
    return tmpOptions
  }

  protected configLabel(chart: Chart, options: ScatterOptions): ScatterOptions {
    let label
    let customAttr: DeepPartial<ChartAttr>
    if (chart.customAttr) {
      customAttr = parseJson(chart.customAttr)
      // label
      if (customAttr.label) {
        const l = customAttr.label
        if (l.show) {
          label = {
            offset: 0,
            style: {
              fill: l.color,
              fontSize: l.fontSize
            },
            content: datum => {
              return datum['name']
            },
            layout: [{ type: 'limit-in-shape' }]
          }
        } else {
          label = false
        }
      }
    }
    return { ...options, label }
  }

  protected configTooltip(chart: Chart, options: ScatterOptions): ScatterOptions {
    const customAttr: DeepPartial<ChartAttr> = parseJson(chart.customAttr)
    const tooltipAttr = customAttr.tooltip
    const xAxisTitle = chart.xAxis[0]
    const yAxisTitle = chart.yAxis[0]
    const yAxisExtTitle = chart.yAxisExt[0]
    if (!tooltipAttr.show || (!xAxisTitle && !yAxisTitle && !yAxisExtTitle)) {
      return {
        ...options,
        tooltip: false
      }
    }
    const formatterMap = tooltipAttr.seriesTooltipFormatter
      ?.filter(i => i.show)
      .reduce((pre, next) => {
        pre[next['seriesId']] = next
        return pre
      }, {}) as Record<string, SeriesFormatter>
    const optionsData = cloneDeep(options.data)
    const tooltip: ScatterOptions['tooltip'] = {
      showTitle: true,
      title: (_title, datum) => {
        return datum?.['name']
      },
      customItems(originalItems) {
        if (!tooltipAttr.seriesTooltipFormatter?.length) {
          return originalItems
        }
        const result = []
        originalItems.forEach(item => {
          Object.keys(formatterMap).forEach(key => {
            if (key.endsWith(item.name)) {
              const formatter = formatterMap[key]
              if (formatter) {
                const value =
                  formatter.groupType === 'q'
                    ? valueFormatter(parseFloat(item.value as string), formatter.formatterCfg)
                    : item.value
                const name = isEmpty(formatter.chartShowName)
                  ? formatter.name
                  : formatter.chartShowName
                result.push({ color: item.color, name, value })
              }
            }
          })
        })
        const dynamicTooltipValue = optionsData.find(
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
        return result
      }
    }
    return {
      ...options,
      tooltip
    }
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    chart.customStyle.yAxis.splitLine = {
      ...chart.customStyle.yAxis.splitLine,
      show: false
    }
    chart.customStyle.yAxisExt.splitLine = {
      ...chart.customStyle.yAxisExt.splitLine,
      show: false
    }
    chart.customStyle.yAxis.axisLine = {
      ...chart.customStyle.yAxis.axisLine,
      show: true
    }
    chart.customStyle.yAxisExt.axisLine = {
      ...chart.customStyle.yAxisExt.axisLine,
      show: true
    }
    return chart
  }
  protected configColor(chart: Chart, options: ScatterOptions): ScatterOptions {
    const { xAxis, yAxis, yAxisExt } = chart
    if (!(xAxis?.length && yAxis?.length && yAxisExt?.length)) {
      return options
    }
    return this.configSingleDimensionColor(chart, options)
  }
  public setupSeriesColor(chart: ChartObj, data?: any[]): ChartBasicStyle['seriesColor'] {
    const { xAxis, yAxis, yAxisExt } = chart
    if (!(xAxis?.length && yAxis?.length && yAxisExt?.length)) {
      return []
    }
    const tmp = data[0].data
    return setUpSingleDimensionSeriesColor(chart, tmp)
  }
  protected setupOptions(chart: Chart, options: ScatterOptions) {
    return flow(
      this.configTheme,
      this.configColor,
      this.configLabel,
      this.configTooltip,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configAnalyse,
      this.configSlider,
      this.configBasicStyle
    )(chart, options, {}, this)
  }

  constructor() {
    super('quadrant', [])
  }
}
