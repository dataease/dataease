import {
  G2PlotChartView,
  G2PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/g2plot'
import type { Heatmap, HeatmapOptions } from '@antv/g2plot/esm/plots/heatmap'
import { flow, hexColorToRGBA, parseJson } from '@/views/chart/components/js/util'
import { useI18n } from '@/hooks/web/useI18n'
import { deepCopy } from '@/utils/utils'
import { cloneDeep } from 'lodash-es'
import {
  getPadding,
  getXAxis,
  getYAxis
} from '@/views/chart/components/js/panel/common/common_antv'
import { valueFormatter } from '@/views/chart/components/js/formatter'

const { t } = useI18n()
const DEFAULT_DATA = []
/**
 * 热力图
 */
export class TableHeatmap extends G2PlotChartView<HeatmapOptions, Heatmap> {
  properties: EditorProperty[] = [
    'basic-style-selector',
    'background-overall-component',
    'label-selector',
    'legend-selector',
    'x-axis-selector',
    'y-axis-selector',
    'title-selector',
    'tooltip-selector',
    'jump-set',
    'linkage',
    'border-style'
  ]
  propertyInner: EditorPropertyInner = {
    'background-overall-component': ['all'],
    'basic-style-selector': ['colors'],
    'label-selector': ['fontSize', 'color'],
    'x-axis-selector': ['name', 'color', 'fontSize', 'position', 'axisLabel', 'axisLine'],
    'y-axis-selector': ['name', 'color', 'fontSize', 'position', 'axisLabel', 'axisLine'],
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
    'legend-selector': ['orient', 'color', 'fontSize', 'hPosition', 'vPosition'],
    'tooltip-selector': ['show', 'color', 'fontSize', 'backgroundColor'],
    'border-style': ['all']
  }
  axis: AxisType[] = ['xAxis', 'xAxisExt', 'extColor', 'filter']
  axisConfig: AxisConfig = {
    xAxis: {
      name: `横轴 / ${t('chart.dimension')}`,
      type: 'd',
      limit: 1
    },
    xAxisExt: {
      name: `纵轴 / ${t('chart.dimension')}`,
      type: 'd',
      limit: 1
    },
    extColor: {
      name: `${t('chart.color')} / ${t('chart.dimension_or_quota')}`,
      limit: 1
    }
  }
  protected getDefaultLength = (chart, l) => {
    const containerDom = document.getElementById(chart.container)
    const containerHeight = containerDom?.clientHeight || 100
    const containerWidth = containerDom?.clientWidth || 100
    let defaultLength = containerHeight - containerHeight * 0.5
    if (l.orient !== 'vertical') {
      defaultLength = containerWidth - containerWidth * 0.5
    }
    return defaultLength
  }
  protected sortData = (fieldObj, data) => {
    const { deType, sort, customSort } = fieldObj

    if (sort === 'desc') {
      if (deType === 0) {
        return data.sort().reverse()
      } else {
        return data.sort((a, b) => b - a)
      }
    } else if (sort === 'asc') {
      if (deType === 0) {
        return data.sort()
      } else {
        return data.sort((a, b) => a - b)
      }
    }

    // 如果没有指定排序方式，直接返回原始数据或 customSort
    return customSort && customSort.length > 0 ? customSort : data
  }
  async drawChart(drawOptions: G2PlotDrawOptions<Heatmap>): Promise<Heatmap> {
    const { chart, container, action } = drawOptions
    const xAxis = deepCopy(chart.xAxis)
    const xAxisExt = deepCopy(chart.xAxisExt)
    const extColor = deepCopy(chart.extColor)
    if (!xAxis?.length || !xAxisExt?.length || !extColor?.length) {
      return
    }
    const xField = xAxis[0].dataeaseName
    const xFieldExt = xAxisExt[0].dataeaseName
    const extColorField = extColor[0].dataeaseName
    // data
    const data = cloneDeep(chart.data.tableRow)
    data.forEach(i => {
      Object.keys(i).forEach(key => {
        if (key === '*') {
          i['@'] = i[key]
        }
      })
    })

    // options
    const initOptions: HeatmapOptions = {
      data: data,
      xField: xField,
      yField: xFieldExt,
      colorField: extColorField === '*' ? '@' : extColorField,
      appendPadding: getPadding(chart),
      meta: {
        [xField]: {
          type: 'cat',
          values: this.sortData(xAxis[0], [...new Set(data.map(i => i[[xField]]))])
        },
        [xFieldExt]: {
          type: 'cat',
          values: this.sortData(xAxisExt[0], [...new Set(data.map(i => i[[xFieldExt]]))]).reverse()
        }
      },
      legend: {
        layout: 'vertical',
        position: 'right',
        slidable: true,
        label: {
          align: 'left',
          spacing: 10
        }
      }
    }
    chart.container = container
    const options = this.setupOptions(chart, initOptions)
    const { Heatmap } = await import('@antv/g2plot/esm/plots/heatmap')
    const newChart = new Heatmap(container, options)
    newChart.on('plot:click', param => {
      if (!param.data?.data) {
        return
      }
      const pointData = param.data.data
      const dimensionList = []
      chart.data.fields.forEach(item => {
        Object.keys(pointData).forEach(key => {
          if (key.startsWith('f_') && item.dataeaseName === key) {
            dimensionList.push({
              id: item.id,
              dataeaseName: item.dataeaseName,
              value: pointData[key]
            })
          }
        })
      })
      action({
        x: param.data.x,
        y: param.data.y,
        data: {
          data: {
            ...param.data.data,
            value: dimensionList[1].value,
            name: dimensionList[1].id,
            dimensionList: dimensionList,
            quotaList: [dimensionList[1]]
          }
        }
      })
    })
    newChart.on('afterrender', ev => {
      const l = JSON.parse(JSON.stringify(parseJson(chart.customStyle).legend))
      if (l.show) {
        const rail = ev.view.getController('legend').option[extColor[0].dataeaseName]?.['rail']
        if (rail) {
          rail.defaultLength = this.getDefaultLength(chart, l)
        }
      }
    })
    return newChart
  }

  protected configBasicStyle(chart: Chart, options: HeatmapOptions): HeatmapOptions {
    const basicStyle = parseJson(chart.customAttr).basicStyle
    const color = basicStyle.colors?.map(ele => {
      return hexColorToRGBA(ele, basicStyle.alpha)
    })
    return {
      ...options,
      color
    }
  }
  protected configTooltip(chart: Chart, options: HeatmapOptions): HeatmapOptions {
    let tooltip
    let customAttr: DeepPartial<ChartAttr>
    if (chart.customAttr) {
      customAttr = parseJson(chart.customAttr)
      // tooltip
      if (customAttr.tooltip) {
        const extColor = deepCopy(chart.extColor)
        const xAxisExt = deepCopy(chart.xAxisExt)
        const tooltipFiledList = [xAxisExt, extColor]
        const t = JSON.parse(JSON.stringify(customAttr.tooltip))
        if (t.show) {
          tooltip = {
            showTitle: true,
            customItems(originalItems) {
              const items = []
              const createItem = (fieldObj, items, originalItems) => {
                const name = fieldObj?.chartShowName ? fieldObj?.chartShowName : fieldObj?.name
                let value = originalItems[0].data[fieldObj.dataeaseName]
                if (!isNaN(Number(value))) {
                  value = valueFormatter(value, fieldObj?.formatterCfg)
                }
                items.push({
                  ...originalItems[0],
                  name: name,
                  value: value
                })
              }
              tooltipFiledList.forEach(field => {
                createItem(field[0], items, originalItems)
              })
              return items
            }
          }
        } else {
          tooltip = false
        }
      }
    }
    return {
      ...options,
      tooltip
    }
  }

  protected configXAxis(chart: Chart, options: HeatmapOptions): HeatmapOptions {
    const xAxis = getXAxis(chart, options)
    return {
      ...options,
      xAxis: xAxis ? { ...xAxis, grid: null } : false
    }
  }

  protected configYAxis(chart: Chart, options: HeatmapOptions): HeatmapOptions {
    const yAxis = getYAxis(chart)
    return {
      ...options,
      yAxis: yAxis ? { ...yAxis, grid: null } : false
    }
  }

  protected configLegend(chart: Chart, options: HeatmapOptions): HeatmapOptions {
    const tmpOptions = super.configLegend(chart, options)
    if (tmpOptions.legend) {
      const l = JSON.parse(JSON.stringify(parseJson(chart.customStyle).legend))
      tmpOptions.legend.slidable = true
      tmpOptions.legend.minHeight = 10
      tmpOptions.legend.minWidth = 10
      tmpOptions.legend.maxHeight = 600
      tmpOptions.legend.maxWidth = 600
      const containerDom = document.getElementById(chart.container)
      const containerHeight = containerDom?.clientHeight || 100
      const containerWidth = containerDom?.clientWidth || 100
      let defaultLength = containerHeight - containerHeight * 0.5
      if (l.orient === 'vertical') {
        tmpOptions.legend.offsetY = -5
      } else {
        defaultLength = containerWidth - containerWidth * 0.5
      }
      tmpOptions.legend.rail = { defaultLength: defaultLength }
      tmpOptions.legend.label = {
        spacing: 10,
        style: {
          fill: l.color,
          fontSize: l.fontSize
        }
      }
    }
    return tmpOptions
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    chart.customStyle.legend.orient = 'vertical'
    chart.customStyle.legend.vPosition = 'center'
    chart.customStyle.legend.hPosition = 'right'
    chart.customStyle.legend.rail = { defaultLength: 100 }
    return chart
  }

  protected configLabel(chart: Chart, options: HeatmapOptions): HeatmapOptions {
    const tmpOptions = super.configLabel(chart, options)
    if (tmpOptions.label) {
      const extColor = deepCopy(chart.extColor)
      const label = {
        ...tmpOptions.label,
        position: 'middle',
        layout: [{ type: 'hide-overlap' }, { type: 'limit-in-canvas' }],
        formatter: data => {
          const value = data[extColor[0]?.dataeaseName]
          if (!isNaN(Number(value))) {
            return valueFormatter(value, extColor[0]?.formatterCfg)
          }
          return value
        }
      }
      return {
        ...tmpOptions,
        label
      }
    }
    return tmpOptions
  }

  protected setupOptions(chart: Chart, options: HeatmapOptions): HeatmapOptions {
    return flow(
      this.configTheme,
      this.configXAxis,
      this.configYAxis,
      this.configBasicStyle,
      this.configLegend,
      this.configTooltip,
      this.configLabel
    )(chart, options)
  }

  constructor() {
    super('t-heatmap', DEFAULT_DATA)
  }
}
