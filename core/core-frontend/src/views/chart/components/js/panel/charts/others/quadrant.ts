import {
  G2PlotChartView,
  G2PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/g2plot'
import { ScatterOptions, Scatter as G2Scatter } from '@antv/g2plot/esm/plots/scatter'
import { flow, parseJson } from '../../../util'
import { valueFormatter } from '../../../formatter'
import { useI18n } from '@/hooks/web/useI18n'
import { isEmpty } from 'lodash-es'

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
    'basic-style-selector': ['colors', 'alpha', 'scatterSymbol', 'scatterSymbolSize'],
    'label-selector': ['fontSize', 'color'],
    'tooltip-selector': ['fontSize', 'color', 'backgroundColor', 'seriesTooltipFormatter'],
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
    const { colorFieldObj, sizeFieldObj, xFieldObj, yFieldObj } = this.getFieldObject(chart)
    if (!xFieldObj.id || !yFieldObj.id || yFieldObj.id === xFieldObj.id) {
      return
    }
    const data: any[] = []
    // 根据指标字段对数据列表进行分组
    const groupedData = chart.data?.data
      ?.filter(item => item['category'] != null)
      .reduce((result, item) => {
        ;(result[item['field']] = result[item['field']] || []).push(item)
        return result
      }, {})
    // 维度字段数据分组
    chart.data?.data
      ?.filter(item => item['category'] === null)
      .forEach(item => {
        ;(groupedData[colorFieldObj.name] = groupedData[colorFieldObj.name] || []).push(
          item['field']
        )
      })
    // 去掉groupedData每个key中集合的对象重复项
    Object.keys(groupedData).forEach(key => {
      groupedData[key] = Array.from(this.getUniqueObjects(groupedData[key]))
    })
    // 一个指标字段的数据长度，视为数据长度，也就是有多少数据
    const dataLength = chart.data?.data.length / chart.data?.fields.length
    for (let index = 0; index < dataLength; index++) {
      const tmpData = {
        dimensionList: groupedData[xFieldObj.name][index].dimensionList,
        quotaList: groupedData[xFieldObj.name][index].quotaList,
        [xFieldObj.name]: groupedData[xFieldObj.name][index].value
      }
      if (groupedData[yFieldObj.name]) {
        tmpData[yFieldObj.name] = groupedData[yFieldObj.name][index].value
      }
      if (
        groupedData[sizeFieldObj.name] &&
        sizeFieldObj.name !== yFieldObj.name &&
        sizeFieldObj.name !== xFieldObj.name
      ) {
        tmpData[sizeFieldObj.name] = groupedData[sizeFieldObj.name]?.[index].value
      }
      if (groupedData[colorFieldObj.name]) {
        tmpData[colorFieldObj.name] = groupedData[colorFieldObj.name][index]
      }
      data.push(tmpData)
    }
    // x轴基准线 默认值
    const xValues = data.map(item => item[xFieldObj.name])
    const xBaseline = ((Math.max(...xValues) + Math.min(...xValues)) / 2).toFixed()
    // y轴基准线 默认值
    const yValues = data.map(item => item[yFieldObj.name])
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
    const colorField = colorFieldObj.name ? { colorField: colorFieldObj.name } : {}
    const baseOptions: ScatterOptions = {
      ...colorField,
      quadrant: {
        ...defaultBaselineQuadrant
      },
      data: data,
      xField: xFieldObj.name,
      yField: yFieldObj.name,
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
    const extBubbleObj = { id: chart.extBubble[0]?.id, name: chart.extBubble[0]?.['originName'] }
    if (chart.extBubble?.length) {
      return {
        ...options,
        size: [4, 30],
        sizeField: extBubbleObj.name,
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
              return datum[chart.xAxis[0]?.['originName']]
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
    const tooltip: ScatterOptions['tooltip'] = {
      showTitle: true,
      title: (_title, datum) => {
        return datum?.[xAxisTitle['originName']]
      },
      customItems(originalItems) {
        if (!tooltipAttr.seriesTooltipFormatter?.length) {
          return originalItems
        }
        const result = []
        originalItems
          ?.filter(i => i.name !== xAxisTitle['originName'])
          .forEach(item => {
            Object.keys(formatterMap).forEach(key => {
              if (formatterMap[key]['originName'] === item.name) {
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

  protected setupOptions(chart: Chart, options: ScatterOptions) {
    return flow(
      this.configTheme,
      this.configLabel,
      this.configTooltip,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configAnalyse,
      this.configSlider,
      this.configBasicStyle
    )(chart, options)
  }

  constructor() {
    super('quadrant', [])
  }
}
