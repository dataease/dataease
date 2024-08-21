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
  getLegend,
  getPadding,
  getXAxis,
  getYAxis
} from '@/views/chart/components/js/panel/common/common_antv'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { Datum } from '@antv/g2plot/esm/types/common'

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
    'linkage'
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
    'tooltip-selector': ['color', 'fontSize', 'backgroundColor']
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
    const containerDom = document.getElementById(container)
    const containerHeight = containerDom?.clientHeight || 100
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
          type: 'cat'
        },
        [xFieldExt]: {
          type: 'cat'
        }
      },
      legend: {
        layout: 'vertical',
        position: 'right',
        slidable: true,
        maxHeight: containerHeight - containerHeight * 0.2,
        label: {
          align: 'left',
          spacing: 10
        }
      }
    }
    const options = this.setupOptions(chart, initOptions)
    const { Heatmap } = await import('@antv/g2plot/esm/plots/heatmap')
    const newChart = new Heatmap(container, options)
    newChart.on('plot:click', param => {
      if (!param.data?.data) {
        return
      }
      const pointData = param.data.data
      const dimensionList = []
      const quotaList = []
      chart.data.fields.forEach((item, index) => {
        Object.keys(pointData).forEach(key => {
          if (key.startsWith('f_') && item.dataeaseName === key) {
            dimensionList.push({
              id: item.id,
              dataeaseName: item.dataeaseName,
              value: pointData[key]
            })
          }
          if (!key.startsWith('f_')) {
            quotaList.push({
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
            value: quotaList[0].value,
            name: dimensionList[0].id,
            dimensionList: dimensionList,
            quotaList: quotaList
          }
        }
      })
    })
    newChart.on('afterrender', ev => {
      const l = JSON.parse(JSON.stringify(parseJson(chart.customStyle).legend))
      if (l.show) {
        const extColor = deepCopy(chart.extColor)
        const containerDom = document.getElementById(container)
        const containerHeight = containerDom?.clientHeight || 100
        const containerWidth = containerDom?.clientWidth || 100
        let defaultLength = getLegend(chart)
        if (l.orient === 'vertical') {
          defaultLength = containerHeight - containerHeight * 0.5
        } else {
          defaultLength = containerWidth - containerWidth * 0.5
        }
        ev.view.getController('legend').option[extColor[0].dataeaseName]['rail'].defaultLength =
          defaultLength
      }
    })
    return newChart
  }

  protected configBasicStyle(chart: Chart, options: HeatmapOptions): HeatmapOptions {
    const basicStyle = parseJson(chart.customAttr).basicStyle
    const color = basicStyle.colors?.map((ele, index) => {
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
        const t = JSON.parse(JSON.stringify(customAttr.tooltip))
        if (t.show) {
          tooltip = {
            showTitle: true,
            customItems(originalItems) {
              const name = extColor[0]?.chartShowName
                ? extColor[0]?.chartShowName
                : extColor[0]?.name
              let value = originalItems[0].value
              if (!isNaN(Number(value))) {
                value = valueFormatter(originalItems[0].value, extColor[0]?.formatterCfg)
              }
              const newItems = {
                ...originalItems[0],
                name: name,
                value: value
              }
              return [newItems]
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
    return { ...options, xAxis: { ...xAxis, grid: null } }
  }

  protected configYAxis(chart: Chart, options: HeatmapOptions): HeatmapOptions {
    const yAxis = getYAxis(chart)
    return { ...options, yAxis: { ...yAxis, grid: null } }
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
      if (l.orient === 'vertical') {
        tmpOptions.legend.offsetY = -5
      }
      tmpOptions.legend.rail = {
        defaultLength: 100
      }
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
