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
import {
  handleGeoJson,
  mapRendered,
  mapRendering
} from '@/views/chart/components/js/panel/common/common_antv'
import { FeatureCollection } from '@antv/l7plot/dist/esm/plots/choropleth/types'
import { cloneDeep, defaultsDeep } from 'lodash-es'
import { useI18n } from '@/hooks/web/useI18n'
import { valueFormatter } from '../../../formatter'
import {
  MAP_AXIS_TYPE,
  MAP_EDITOR_PROPERTY,
  MAP_EDITOR_PROPERTY_INNER,
  MapMouseEvent
} from '@/views/chart/components/js/panel/charts/map/common'
import { CategoryLegendListItem } from '@antv/l7plot-component/dist/lib/types/legend'
import createDom from '@antv/dom-util/esm/create-dom'
import {
  CONTAINER_TPL,
  ITEM_TPL,
  LIST_CLASS
} from '@antv/l7plot-component/dist/esm/legend/category/constants'
import substitute from '@antv/util/esm/substitute'

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
    mapRendering(container)
    view.once('loaded', () => {
      mapRendered(container)
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
    let minValue = misc.mapLegendMin
    let maxValue = misc.mapLegendMax
    if (legend.show) {
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
    geoJson.features.forEach(item => {
      const name = item.properties['name']
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
    if (colorScale.length) {
      options.color['value'] = colorScale.map(item => (item.color ? item.color : item))
      if (colorScale[0].value && !misc.mapAutoLegend) {
        options.color['scale']['domain'] = [minValue, maxValue]
      }
    }
    return options
  }

  private customConfigLegend(
    chart: Chart,
    options: ChoroplethOptions,
    _context: Record<string, any>
  ): ChoroplethOptions {
    const { basicStyle } = parseJson(chart.customAttr)
    if (basicStyle.suspension === false && basicStyle.showZoom === undefined) {
      return options
    }
    const { legend } = parseJson(chart.customStyle)
    if (!legend.show) {
      return options
    }
    const LEGEND_SHAPE_STYLE_MAP = {
      circle: {
        borderRadius: '50%'
      },
      square: {},
      triangle: {
        borderLeft: '5px solid transparent',
        borderRight: '5px solid transparent',
        borderBottom: '10px solid var(--bgColor)',
        background: 'unset'
      },
      diamond: {
        transform: 'rotate(45deg)'
      }
    }
    const customLegend = {
      position: 'bottomleft',
      customContent: (_: string, items: CategoryLegendListItem[]) => {
        const showItems = items?.length > 30 ? items.slice(0, 30) : items
        if (showItems?.length) {
          const containerDom = createDom(CONTAINER_TPL) as HTMLElement
          const listDom = containerDom.getElementsByClassName(LIST_CLASS)[0] as HTMLElement
          showItems.forEach(item => {
            let value = '-'
            if (item.value !== '') {
              if (Array.isArray(item.value)) {
                item.value.forEach((v, i) => {
                  item.value[i] = Number.isNaN(v) || v === 'NaN' ? 'NaN' : parseFloat(v).toFixed(0)
                })
                value = item.value.join('-')
              } else {
                const tmp = item.value as string
                value = Number.isNaN(tmp) || tmp === 'NaN' ? 'NaN' : parseFloat(tmp).toFixed(0)
              }
            }
            const substituteObj = { ...item, value }

            const domStr = substitute(ITEM_TPL, substituteObj)
            const itemDom = createDom(domStr)
            // 给 legend 形状用的
            itemDom.style.setProperty('--bgColor', item.color)
            listDom.appendChild(itemDom)
          })
          return listDom
        }
        return ''
      },
      domStyles: {
        'l7plot-legend__category-value': {
          fontSize: legend.fontSize + 'px',
          color: legend.color
        },
        'l7plot-legend__category-marker': {
          ...LEGEND_SHAPE_STYLE_MAP[legend.icon]
        }
      }
    }
    defaultsDeep(options, { legend: customLegend })
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
      this.customConfigLegend
    )(chart, options, context)
  }
}
