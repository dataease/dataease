import {
  L7PlotChartView,
  L7PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/l7plot'
import { Choropleth, ChoroplethOptions } from '@antv/l7plot/dist/esm/plots/choropleth'
import {
  filterChartDataByRange,
  flow,
  getDynamicColorScale,
  getGeoJsonFile,
  setMapChartDefaultMaxAndMinValueByData,
  hexColorToRGBA,
  parseJson
} from '@/views/chart/components/js/util'
import { handleGeoJson } from '@/views/chart/components/js/panel/common/common_antv'
import { FeatureCollection } from '@antv/l7plot/dist/esm/plots/choropleth/types'
import { cloneDeep } from 'lodash-es'
import { useI18n } from '@/hooks/web/useI18n'
import { valueFormatter } from '../../../formatter'
import {
  MAP_AXIS_TYPE,
  MAP_EDITOR_PROPERTY,
  MAP_EDITOR_PROPERTY_INNER,
  MapMouseEvent
} from '@/views/chart/components/js/panel/charts/map/common'

const { t } = useI18n()

/**
 * 地图
 */
export class Map extends L7PlotChartView<ChoroplethOptions, Choropleth> {
  properties: EditorProperty[] = [...MAP_EDITOR_PROPERTY, 'legend-selector']
  propertyInner: EditorPropertyInner = {
    ...MAP_EDITOR_PROPERTY_INNER,
    'basic-style-selector': ['colors', 'alpha', 'areaBorderColor', 'zoom', 'gradient-color'],
    'legend-selector': ['icon', 'fontSize', 'color']
  }
  axis = MAP_AXIS_TYPE
  axisConfig: AxisConfig = {
    xAxis: {
      name: `${t('chart.area')} / ${t('chart.dimension')}`,
      type: 'd',
      limit: 1
    },
    yAxis: {
      name: `${t('chart.chart_data')} / ${t('chart.quota')}`,
      type: 'q',
      limit: 1
    }
  }

  constructor() {
    super('map', [])
  }

  async drawChart(drawOption: L7PlotDrawOptions<Choropleth>): Promise<Choropleth> {
    const { chart, level, areaId, container, action } = drawOption
    if (!areaId) {
      return
    }
    const sourceData = JSON.parse(JSON.stringify(chart.data?.data || []))
    let data = []
    const { misc } = parseJson(chart.customAttr)
    const { legend } = parseJson(chart.customStyle)
    // 自定义图例
    if (!misc.mapAutoLegend && legend.show) {
      let minValue = misc.mapLegendMin
      let maxValue = misc.mapLegendMax
      setMapChartDefaultMaxAndMinValueByData(sourceData, maxValue, minValue, (max, min) => {
        maxValue = max
        minValue = min
        action({
          from: 'map',
          data: {
            max: maxValue,
            min: minValue,
            legendNumber: 9
          }
        })
      })
      data = filterChartDataByRange(sourceData, maxValue, minValue)
    } else {
      data = sourceData
    }
    const geoJson = cloneDeep(await getGeoJsonFile(areaId))
    let options: ChoroplethOptions = {
      preserveDrawingBuffer: true,
      map: {
        type: 'mapbox',
        style: 'blank'
      },
      geoArea: {
        type: 'geojson'
      },
      source: {
        data: data,
        joinBy: {
          sourceField: 'name',
          geoField: 'name',
          geoData: geoJson
        }
      },
      viewLevel: {
        level,
        adcode: 'all'
      },
      autoFit: true,
      chinaBorder: false,
      color: {
        field: 'value'
      },
      style: {
        opacity: 1,
        lineWidth: 0.6,
        lineOpacity: 1
      },
      label: {
        field: '_DE_LABEL_',
        style: {
          textAllowOverlap: true,
          textAnchor: 'center'
        }
      },
      state: {
        active: { stroke: 'green', lineWidth: 1 }
      },
      tooltip: {},
      // 禁用线上地图数据
      customFetchGeoData: () => null
    }
    const context = { drawOption, geoJson }
    options = this.setupOptions(chart, options, context)
    const view = new Choropleth(container, options)
    this.configZoomButton(chart, view)
    view.once('loaded', () => {
      view.scene.map['keyboard'].disable()
      view.on('fillAreaLayer:click', (ev: MapMouseEvent) => {
        const data = ev.feature.properties
        action({
          x: ev.x,
          y: ev.y,
          data: {
            data,
            extra: { adcode: data.adcode }
          }
        })
      })
    })

    return view
  }

  private configBasicStyle(
    chart: Chart,
    options: ChoroplethOptions,
    context: Record<string, any>
  ): ChoroplethOptions {
    const { areaId }: L7PlotDrawOptions<any> = context.drawOption
    const geoJson: FeatureCollection = context.geoJson
    const { basicStyle, label, misc } = parseJson(chart.customAttr)
    const senior = parseJson(chart.senior)
    const curAreaNameMapping = senior.areaMapping?.[areaId]
    handleGeoJson(geoJson, curAreaNameMapping)
    options.color = {
      field: 'value',
      value: [basicStyle.colors[0]],
      scale: {
        type: 'quantize',
        unknown: basicStyle.areaBaseColor
      }
    }
    if (!chart.data?.data?.length || !geoJson?.features?.length) {
      options.label && (options.label.field = 'name')
      return options
    }
    const sourceData = JSON.parse(JSON.stringify(chart.data.data))
    const colors = basicStyle.colors.map(item => hexColorToRGBA(item, basicStyle.alpha))
    const { legend } = parseJson(chart.customStyle)
    let data = []
    data = sourceData
    let colorScale = []
    if (legend.show) {
      let minValue = misc.mapLegendMin
      let maxValue = misc.mapLegendMax
      let mapLegendNumber = misc.mapLegendNumber
      setMapChartDefaultMaxAndMinValueByData(sourceData, maxValue, minValue, (max, min) => {
        maxValue = max
        minValue = min
        mapLegendNumber = 9
      })
      // 非自动，过滤数据
      if (!misc.mapAutoLegend) {
        data = filterChartDataByRange(sourceData, maxValue, minValue)
      } else {
        mapLegendNumber = 9
      }
      // 定义最大值、最小值、区间数量和对应的颜色
      colorScale = getDynamicColorScale(minValue, maxValue, mapLegendNumber, colors)
    } else {
      colorScale = colors
    }
    const areaMap = data.reduce((obj, value) => {
      obj[value['field']] = value.value
      return obj
    }, {})
    let validArea = 0
    geoJson.features.forEach(item => {
      const name = item.properties['name']
      if (areaMap[name]) {
        validArea += 1
      }
      // trick, maybe move to configLabel, here for perf
      if (label.show) {
        const content = []
        if (label.showDimension) {
          content.push(name)
        }
        if (label.showQuota) {
          areaMap[name] && content.push(valueFormatter(areaMap[name], label.quotaLabelFormatter))
        }
        item.properties['_DE_LABEL_'] = content.join('\n\n')
      }
    })
    if (validArea < colorScale.length && !misc.mapAutoLegend) {
      colorScale = colorScale.map(item => (item.color ? item.color : item)).slice(0, validArea)
    }
    if (colorScale.length) {
      options.color['value'] = colorScale.map(item => (item.color ? item.color : item))
    }
    return options
  }

  protected setupOptions(
    chart: Chart,
    options: ChoroplethOptions,
    context: Record<string, any>
  ): ChoroplethOptions {
    return flow(
      this.configEmptyDataStrategy,
      this.configLabel,
      this.configStyle,
      this.configTooltip,
      this.configBasicStyle,
      this.configLegend
    )(chart, options, context)
  }
}
