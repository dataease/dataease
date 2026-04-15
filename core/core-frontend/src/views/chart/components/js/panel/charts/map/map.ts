import {
  L7PlotChartView,
  L7PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/l7plot'
import type { Choropleth, ChoroplethOptions } from '@antv/l7plot/dist/esm/plots/choropleth'
import {
  filterChartDataByRange,
  filterEmptyMinValue,
  flow,
  getDynamicColorScale,
  getGeoJsonFile,
  getMaxAndMinValueByData,
  hexColorToRGBA,
  parseJson
} from '@/views/chart/components/js/util'
import {
  handleGeoJson,
  mapRendered,
  mapRendering
} from '@/views/chart/components/js/panel/common/common_antv'
import type { FeatureCollection } from '@antv/l7plot/dist/esm/plots/choropleth/types'
import { cloneDeep, defaultsDeep, isEmpty } from 'lodash-es'
import { useI18n } from '@/hooks/web/useI18n'
import { valueFormatter } from '../../../formatter'
import {
  MAP_AXIS_TYPE,
  MAP_EDITOR_PROPERTY,
  MAP_EDITOR_PROPERTY_INNER,
  MapMouseEvent
} from '@/views/chart/components/js/panel/charts/map/common'
import type { CategoryLegendListItem } from '@antv/l7plot-component/dist/lib/types/legend'
import createDom from '@antv/dom-util/esm/create-dom'
import {
  CONTAINER_TPL,
  ITEM_TPL,
  LIST_CLASS
} from '@antv/l7plot-component/dist/esm/legend/category/constants'
import substitute from '@antv/util/esm/substitute'
import { configCarouselTooltip } from '@/views/chart/components/js/panel/charts/map/tooltip-carousel'
import { getCustomGeoArea } from '@/api/map'
import { centroid } from '@turf/centroid'
import { TextLayer } from '@antv/l7plot/dist/esm'

const { t } = useI18n()

/**
 * 地图
 */
export class Map extends L7PlotChartView<ChoroplethOptions, Choropleth> {
  properties: EditorProperty[] = [...MAP_EDITOR_PROPERTY, 'legend-selector']
  propertyInner: EditorPropertyInner = {
    ...MAP_EDITOR_PROPERTY_INNER,
    'basic-style-selector': [
      'colors',
      'alpha',
      'areaBorderColor',
      'areaBaseColor',
      'zoom',
      'gradient-color'
    ],
    'legend-selector': ['icon', 'fontSize', 'color'],
    'tooltip-selector': [...MAP_EDITOR_PROPERTY_INNER['tooltip-selector'], 'carousel']
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
    const { chart, level, container, action, scope } = drawOption
    const { areaId, gadmName } = drawOption
    if (!areaId) {
      return
    }
    chart.container = container
    let sourceData = JSON.parse(JSON.stringify(chart.data?.data || []))
    const { misc } = parseJson(chart.customAttr)
    const { legend } = parseJson(chart.customStyle)
    let geoJson = {} as FeatureCollection
    // 自定义区域，去除非区域数据，优先级最高
    let customSubArea: CustomGeoSubArea[] = []
    if (areaId.startsWith('custom_')) {
      customSubArea = (await getCustomGeoArea(areaId)).data || []
      geoJson = cloneDeep(await getGeoJsonFile('156'))
      const areaNameMap = geoJson.features.reduce((p, n) => {
        p['156' + n.properties.adcode] = n.properties.name
        return p
      }, {})
      const { areaMapping } = parseJson(chart.senior)
      const areaMap = customSubArea.reduce((p, n) => {
        const mappedName = areaMapping?.[areaId]?.[n.name]
        if (mappedName) {
          n.name = mappedName
        }
        p[n.name] = n
        n.scopeArr = n.scope?.split(',') || []
        return p
      }, {})
      const fakeData = []
      sourceData.forEach(d => {
        const area = areaMap[d.name]
        if (area) {
          area.scopeArr.forEach(adcode => {
            fakeData.push({
              ...d,
              name: areaNameMap[adcode],
              field: areaNameMap[adcode],
              scope: area.scopeArr,
              areaName: d.name
            })
          })
        }
      })
      sourceData = fakeData
    } else {
      if (scope) {
        geoJson = cloneDeep(await getGeoJsonFile('156'))
        geoJson.features = geoJson.features.filter(f => scope.includes('156' + f.properties.adcode))
      } else {
        geoJson = cloneDeep(await getGeoJsonFile(areaId))
      }
    }
    if (areaId.startsWith('geo_') && geoJson?.features?.length) {
      const levelNames = Object.keys(geoJson?.features[0]?.properties).filter(key =>
        key.startsWith('NAME_')
      )
      const nameKey = levelNames[levelNames.length - 1]
      geoJson.features.forEach(item => {
        if (item.properties[nameKey]) {
          item.properties['name'] = item.properties[nameKey]
        }
      })
      if (areaId.length > 7) {
        geoJson.features = geoJson.features.filter(f => {
          const names = Object.keys(f.properties)
            .filter(key => key.startsWith('NAME_'))
            .map(key => f.properties[key])
            .filter(Boolean)
            .join('@')
          if (isEmpty(names) || !gadmName) {
            return true
          }
          return names.replace(/@[^@]*$/, '') === gadmName
        })
      }
    }
    let data = []
    // 自定义图例
    if (!misc.mapAutoLegend && legend.show) {
      let minValue = misc.mapLegendMin
      let maxValue = misc.mapLegendMax
      let legendNumber = 9
      if (misc.mapLegendRangeType === 'custom') {
        maxValue = 0
        minValue = 0
        legendNumber = misc.mapLegendNumber
      }
      getMaxAndMinValueByData(sourceData, 'value', maxValue, minValue, (max, min) => {
        maxValue = max
        minValue = min
        action({
          from: 'map',
          data: {
            max: maxValue,
            min: minValue ?? filterEmptyMinValue(sourceData, 'value'),
            legendNumber: legendNumber
          }
        })
      })
      data = filterChartDataByRange(sourceData, maxValue, minValue)
      if (chart.drill) {
        getMaxAndMinValueByData(sourceData, 'value', 0, 0, (max, min) => {
          data = filterChartDataByRange(sourceData, max, min)
        })
      }
    } else {
      data = sourceData
    }
    let options: ChoroplethOptions = {
      preserveDrawingBuffer: true,
      minZoom: -2,
      map: {
        type: 'mapbox',
        style: 'blank',
        minZoom: -2
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
    const context: Record<string, any> = { drawOption, geoJson, customSubArea }
    options = this.setupOptions(chart, options, context)
    const { Choropleth } = await import('@antv/l7plot/dist/esm/plots/choropleth')
    const view = new Choropleth(container, options)
    this.configZoomButton(chart, view)
    mapRendering(container)
    view.once('loaded', () => {
      mapRendered(container)
      const { layers } = context
      if (layers) {
        layers.forEach(l => {
          view.addLayer(l)
        })
      }
      view.scene.map['keyboard'].disable()
      view.on('fillAreaLayer:click', (ev: MapMouseEvent) => {
        const evData = ev.feature.properties
        if (areaId.startsWith('custom_')) {
          evData.name = evData.areaName
          evData.adcode = '156'
        }
        let adcode = evData.adcode
        let names = ''
        if (adcode + '' !== '156' && !areaId.startsWith('156')) {
          adcode = 'geo_' + adcode
          names = Object.keys(evData)
            .filter(key => key.startsWith('NAME_'))
            .map(key => evData[key])
            .filter(Boolean)
            .join('@')
        }
        action({
          x: ev.x,
          y: ev.y,
          data: {
            data: evData,
            extra: { adcode, scope: evData.scope, gadmName: names }
          }
        })
      })
      chart.container = container
      configCarouselTooltip(chart, view, data, null, customSubArea, drawOption)
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
    handleGeoJson(geoJson, curAreaNameMapping, senior.useGlobalAreaMapping)
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
    const sourceData = options.source.data
    const colors = basicStyle.colors.map(item => hexColorToRGBA(item, basicStyle.alpha))
    const { legend } = parseJson(chart.customStyle)
    let data = sourceData
    let colorScale = []
    let minValue = misc.mapAutoLegend ? 0 : misc.mapLegendMin
    let maxValue = misc.mapAutoLegend ? 0 : misc.mapLegendMax
    let mapLegendNumber = misc.mapLegendNumber
    if (legend.show) {
      getMaxAndMinValueByData(sourceData, 'value', maxValue, minValue, (max, min) => {
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
      mapLegendNumber = misc.mapAutoLegend
        ? this.calculateAutoLegendNumber(sourceData)
        : mapLegendNumber
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
          ;(areaMap[name] || areaMap[name] === 0) &&
            content.push(valueFormatter(areaMap[name], label.quotaLabelFormatter))
        }
        item.properties['_DE_LABEL_'] = content.join('\n\n')
      }
    })
    if (colorScale.length) {
      options.color['value'] = colorScale.map(item =>
        item.color && item.value ? new ColorWrapper(item.color, item.value) : new ColorWrapper(item)
      )
      if (colorScale[0].value && !misc.mapAutoLegend) {
        options.color['scale']['domain'] = [
          minValue ?? filterEmptyMinValue(sourceData, 'value'),
          maxValue
        ]
      }
    }
    return options
  }

  /**
   * 自动图例数量计算
   * 根据数据的最大值、最小值和数据条数动态确定合适的图例数量
   * @param data 源数据
   */
  private calculateAutoLegendNumber(data: any[]): number {
    if (!data || data.length === 0) {
      return 1
    }
    // 提取有效数值
    const values = data
      .map(item => parseFloat(item.value))
      .filter(v => !isNaN(v) && v !== null && v !== undefined)
    if (values.length === 0) {
      return 1
    }
    // 计算最大值和最小值
    const maxValue = Math.max(...values)
    const minValue = Math.min(...values)
    // 如果所有数据都相同，只需要 1 个图例
    if (maxValue === minValue) {
      return 1
    }
    // 根据数据条数和值范围计算合适的图例数量
    const dataCount = values.length
    // 基础图例数量：根据数据量决定
    let legendNumber: number
    if (dataCount <= 5) {
      // 数据很少，每个数据一个图例
      legendNumber = dataCount
    } else if (dataCount <= 9) {
      // 数据适中，按数据量
      legendNumber = dataCount
    } else {
      // 数据较多，根据值范围平均分配，最多 9 个
      // 计算每个区间的跨度
      legendNumber = Math.max(9, Math.ceil(Math.sqrt(dataCount)))
    }
    // 确保图例数量在 1-9 之间
    return Math.max(1, Math.min(9, legendNumber))
  }

  // 内部函数 创建自定义图例的内容
  private createLegendCustomContent = showItems => {
    const containerDom = createDom(CONTAINER_TPL) as HTMLElement
    const listDom = containerDom.getElementsByClassName(LIST_CLASS)[0] as HTMLElement
    showItems.forEach(item => {
      let value = '-'
      if (item.value !== '') {
        if (Array.isArray(item.value)) {
          const arr = item.value.every(Number.isNaN) ? item.color.value || [] : item.value
          value = arr
            .map(v => (Number.isNaN(v) || String(v) === 'NaN' ? 'NaN' : parseFloat(v).toFixed(0)))
            .join('-')
        } else {
          const tmp = item.value as string
          value = Number.isNaN(tmp) || tmp === 'NaN' ? 'NaN' : parseFloat(tmp).toFixed(0)
        }
      }
      if (value && value !== '') {
        const substituteObj = { ...item, value }

        const domStr = substitute(ITEM_TPL, substituteObj)
        const itemDom = createDom(domStr)
        // 给 legend 形状用的
        itemDom.style.setProperty('--bgColor', item.color)
        listDom.appendChild(itemDom)
      }
    })
    return listDom
  }

  private customConfigLegend(chart: Chart, options: ChoroplethOptions): ChoroplethOptions {
    const { basicStyle, misc } = parseJson(chart.customAttr)
    const colors = basicStyle.colors.map(item => hexColorToRGBA(item, basicStyle.alpha))
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
        border: 'unset',
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
      domStyles: {
        'l7plot-legend__category-value': {
          fontSize: legend.fontSize + 'px',
          color: legend.color,
          'font-family': chart.fontFamily ? chart.fontFamily : undefined
        },
        'l7plot-legend__category-marker': {
          ...LEGEND_SHAPE_STYLE_MAP[legend.icon],
          width: legend.size + 'px',
          height: legend.size + 'px',
          ...(legend.icon === 'triangle'
            ? {
                ...LEGEND_SHAPE_STYLE_MAP[legend.icon]['triangle'],
                borderLeft: `${legend.size / 2}px solid transparent`,
                borderRight: `${legend.size / 2}px solid transparent`,
                borderBottom: `${legend.size}px solid var(--bgColor)`
              }
            : { border: '0.01px solid #f4f4f4' }),
          ...(legend.icon === 'diamond'
            ? {
                transform: 'rotate(45deg)',
                marginBottom: `${legend.size / 4}px`
              }
            : {})
        }
      }
    }
    // 不是自动图例、自定义图例区间、不是下钻时
    if (!misc.mapAutoLegend && misc.mapLegendRangeType === 'custom' && !chart.drill) {
      // 获取图例区间数据
      const items = []
      // 区间数组
      const ranges = misc.mapLegendCustomRange
        .slice(0, -1)
        .map((item, index) => [item, misc.mapLegendCustomRange[index + 1]])
      ranges.forEach((range, index) => {
        const tmpRange = [range[0], range[1]]
        const colorIndex = index % colors.length
        // 当区间第一个值小于最小值时，颜色取地图底色
        const isLessThanMin = range[0] < ranges[0][0] && range[1] < ranges[0][0]
        let rangeColor = colors[colorIndex]
        if (isLessThanMin) {
          rangeColor = basicStyle.areaBaseColor
        }
        items.push({
          value: tmpRange,
          color: rangeColor
        })
      })
      customLegend['customContent'] = () => {
        if (items?.length) {
          return this.createLegendCustomContent(items)
        }
        return ''
      }
      options.color['value'] = ({ value }) => {
        const item = items.find(item => value >= item.value[0] && value <= item.value[1])
        return item ? item.color : basicStyle.areaBaseColor
      }
      options.color.scale.domain = [ranges[0][0], ranges[ranges.length - 1][1]]
    } else {
      customLegend['customContent'] = (_: string, items: CategoryLegendListItem[]) => {
        // 去重逻辑
        const uniqueItems = items.reduce(
          (acc, item) => {
            const valueKey = JSON.stringify(item.value)
            if (!acc.seen.has(valueKey)) {
              acc.seen.add(valueKey)
              acc.result.push(item)
            }
            return acc
          },
          { seen: new Set(), result: [] }
        ).result
        // 限制最多显示 30 个元素
        const showItems = uniqueItems.length > 30 ? uniqueItems.slice(0, 30) : uniqueItems
        if (showItems?.length) {
          if (showItems.length === 1) {
            const domain = options.color.scale.domain
            if (domain) {
              showItems[0].value = domain?.slice(0, 2)
            } else {
              const firstValue = showItems[0].value?.[0]
              const secondValue = showItems[0].value?.[1]
              if (
                firstValue !== undefined &&
                secondValue !== undefined &&
                !Number.isNaN(firstValue) &&
                !Number.isNaN(secondValue)
              ) {
                showItems[0].value = [firstValue, secondValue]
              } else {
                const v = firstValue ?? secondValue
                showItems[0].value = [v, v]
              }
            }
          }
          return this.createLegendCustomContent(showItems)
        }
        return ''
      }
    }
    // 下钻时按照数据值计算图例
    if (chart.drill) {
      getMaxAndMinValueByData(options.source.data, 'value', 0, 0, (max, min) => {
        options.color.scale.domain = [min, max]
      })
    }
    defaultsDeep(options, { legend: customLegend })
    return options
  }

  protected configCustomArea(
    chart: Chart,
    options: ChoroplethOptions,
    context: Record<string, any>
  ): ChoroplethOptions {
    const { drawOption, customSubArea, geoJson } = context
    if (!drawOption.areaId.startsWith('custom_')) {
      return options
    }
    const customAttr = parseJson(chart.customAttr)
    const { label } = customAttr
    const data = chart.data.data
    const areaMap = data?.reduce((obj, value) => {
      obj[value['field']] = value
      return obj
    }, {})
    const geoJsonMap = geoJson.features.reduce((p, n) => {
      if (n.properties['adcode']) {
        p['156' + n.properties['adcode']] = n
      }
      return p
    }, {})
    customSubArea.forEach(area => {
      const areaJsonArr = []
      area.scopeArr?.forEach(adcode => {
        const json = geoJsonMap[adcode]
        json && areaJsonArr.push(json)
      })
      if (areaJsonArr.length) {
        const areaJson: FeatureCollection = {
          type: 'FeatureCollection',
          features: areaJsonArr
        }
        const center = centroid(areaJson)
        // 轮播用
        area.centroid = [center.geometry.coordinates[0], center.geometry.coordinates[1]]
      }
    })
    //处理label
    options.label = {
      visible: false
    }
    if (label.show) {
      const labelLocation = []
      customSubArea.forEach(area => {
        if (area.centroid) {
          const content = []
          if (label.showDimension) {
            content.push(area.name)
          }
          if (label.showQuota) {
            ;(areaMap[area.name] || areaMap[area.name] === 0) &&
              content.push(valueFormatter(areaMap[area.name].value, label.quotaLabelFormatter))
          }
          labelLocation.push({
            name: content.join('\n\n'),
            x: area.centroid[0],
            y: area.centroid[1]
          })
        }
      })
      const areaLabelLayer = new TextLayer({
        name: 'areaLabelLayer',
        source: {
          data: labelLocation,
          parser: {
            type: 'json',
            x: 'x',
            y: 'y'
          }
        },
        field: 'name',
        style: {
          fill: label.color,
          fontSize: label.fontSize,
          opacity: 1,
          fontWeight: 'bold',
          textAnchor: 'center',
          textAllowOverlap: label.fullDisplay,
          padding: !label.fullDisplay ? [2, 2] : undefined
        }
      })
      context.layers = [areaLabelLayer]
    }
    // 处理tooltip
    const subAreaMap = customSubArea.reduce((p, n) => {
      n.scopeArr.forEach(a => {
        p[a] = n.name
      })
      return p
    }, {})
    if (options.tooltip && options.tooltip.showComponent) {
      options.tooltip.items = ['name', 'adcode', 'value']
      options.tooltip.customTitle = ({ name, adcode }) => {
        adcode = '156' + adcode
        return subAreaMap[adcode] ?? name
      }
      const tooltip = customAttr.tooltip
      const formatterMap = tooltip.seriesTooltipFormatter
        ?.filter(i => i.show)
        .reduce((pre, next) => {
          pre[next.id] = next
          return pre
        }, {}) as Record<string, SeriesFormatter>
      options.tooltip.customItems = originalItem => {
        const result = []
        if (isEmpty(formatterMap)) {
          return result
        }
        const head = originalItem.properties
        if (!head) {
          return result
        }
        const { adcode } = head
        const areaName = subAreaMap['156' + adcode]
        const valItem = areaMap[areaName]
        if (!valItem) {
          return result
        }
        const formatter = formatterMap[valItem.quotaList?.[0]?.id]
        if (!isEmpty(formatter)) {
          const originValue = parseFloat(valItem.value as string)
          const value = valueFormatter(originValue, formatter.formatterCfg)
          const name = isEmpty(formatter.chartShowName) ? formatter.name : formatter.chartShowName
          result.push({ ...valItem, name, value: `${value ?? ''}` })
        }
        valItem.dynamicTooltipValue?.forEach(item => {
          const formatter = formatterMap[item.fieldId]
          if (formatter) {
            const value = valueFormatter(parseFloat(item.value), formatter.formatterCfg)
            const name = isEmpty(formatter.chartShowName) ? formatter.name : formatter.chartShowName
            result.push({ color: 'grey', name, value: `${value ?? ''}` })
          }
        })
        return result
      }
    }
    return options
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    chart.customAttr.basicStyle.areaBaseColor = '#f4f4f4'
    chart.senior.useGlobalAreaMapping = true
    return chart
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
      this.customConfigLegend,
      this.configCustomArea
    )(chart, options, context, this)
  }
}

class ColorWrapper {
  private color: string
  value?: any

  constructor(color: string, value?: any) {
    this.color = color
    if (value !== undefined) {
      this.value = value
    }
  }

  toString(): string {
    return this.color
  }
}
