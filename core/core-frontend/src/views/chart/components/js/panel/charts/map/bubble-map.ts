import { useI18n } from '@/hooks/web/useI18n'
import {
  L7PlotChartView,
  L7PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/l7plot'
import { Choropleth, ChoroplethOptions } from '@antv/l7plot/dist/esm/plots/choropleth'
import { Dot, DotOptions, IPlotLayer } from '@antv/l7plot'
import {
  MAP_AXIS_TYPE,
  MAP_EDITOR_PROPERTY,
  MAP_EDITOR_PROPERTY_INNER,
  MapMouseEvent
} from '@/views/chart/components/js/panel/charts/map/common'
import { flow, getGeoJsonFile, hexColorToRGBA, parseJson } from '@/views/chart/components/js/util'
import { cloneDeep, isEmpty } from 'lodash-es'
import { FeatureCollection } from '@antv/l7plot/dist/esm/plots/choropleth/types'
import {
  handleGeoJson,
  mapRendered,
  mapRendering
} from '@/views/chart/components/js/panel/common/common_antv'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { deepCopy } from '@/utils/utils'
import { configCarouselTooltip } from '@/views/chart/components/js/panel/charts/map/tooltip-carousel'
import { getCustomGeoArea } from '@/api/map'
import { TextLayer } from '@antv/l7plot/dist/esm'
import { centroid } from '@turf/centroid'

const { t } = useI18n()

/**
 * 气泡地图
 */
export class BubbleMap extends L7PlotChartView<ChoroplethOptions, Choropleth> {
  properties: EditorProperty[] = [...MAP_EDITOR_PROPERTY, 'bubble-animate']
  propertyInner = {
    ...MAP_EDITOR_PROPERTY_INNER,
    'tooltip-selector': [...MAP_EDITOR_PROPERTY_INNER['tooltip-selector'], 'carousel'],
    'basic-style-selector': [...MAP_EDITOR_PROPERTY_INNER['basic-style-selector'], 'areaBaseColor']
  }
  axis = MAP_AXIS_TYPE
  axisConfig: AxisConfig = {
    xAxis: {
      name: `${t('chart.area')} / ${t('chart.dimension')}`,
      type: 'd',
      limit: 1
    },
    yAxis: {
      name: `${t('chart.bubble_size')} / ${t('chart.quota')}`,
      type: 'q',
      limit: 1
    }
  }
  constructor() {
    super('bubble-map')
  }

  async drawChart(drawOption: L7PlotDrawOptions<Choropleth>): Promise<Choropleth> {
    const { chart, level, areaId, container, action, scope } = drawOption
    if (!areaId) {
      return
    }
    chart.container = container
    let geoJson = {} as FeatureCollection
    let customSubArea: CustomGeoSubArea[] = []
    let data = chart.data?.data
    if (areaId.startsWith('custom_')) {
      customSubArea = (await getCustomGeoArea(areaId)).data || []
      customSubArea.forEach(a => (a.scopeArr = a.scope?.split(',') || []))
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
      data?.forEach(d => {
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
      data = fakeData
    } else {
      if (scope) {
        geoJson = cloneDeep(await getGeoJsonFile('156'))
        geoJson.features = geoJson.features.filter(f => scope.includes('156' + f.properties.adcode))
      } else {
        geoJson = cloneDeep(await getGeoJsonFile(areaId))
      }
    }
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
        data: data || [],
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
      tooltip: {},
      legend: false,
      // 禁用线上地图数据
      customFetchGeoData: () => null
    }
    const context: Record<string, any> = { drawOption, geoJson, customSubArea }
    options = this.setupOptions(chart, options, context)

    const tooltip = deepCopy(options.tooltip)
    options = { ...options, tooltip: { ...tooltip, showComponent: false } }
    const view = new Choropleth(container, options)
    const dotLayer = this.getDotLayer(chart, geoJson, drawOption, customSubArea)
    if (!areaId.startsWith('custom_')) {
      dotLayer.options = { ...dotLayer.options, tooltip }
    }
    this.configZoomButton(chart, view)
    mapRendering(container)
    view.once('loaded', () => {
      // 修改地图鼠标样式为默认
      view.scene.map._canvasContainer.lastElementChild.style.cursor = 'default'
      const { layers } = context
      if (layers) {
        layers.forEach(l => {
          view.addLayer(l)
        })
      }
      dotLayer.addToScene(view.scene)
      dotLayer.once('add', () => {
        mapRendered(container)
      })
      view.scene.map['keyboard'].disable()
      dotLayer.on('dotLayer:click', (ev: MapMouseEvent) => {
        const data = ev.feature.properties
        let adcode, scope
        if (areaId.startsWith('custom_')) {
          adcode = '156'
          const area = customSubArea.find(a => a.name === data.name)
          scope = area?.scopeArr
        } else {
          adcode = view.currentDistrictData.features.find(
            i => i.properties.name === ev.feature.properties.name
          )?.properties.adcode
        }
        action({
          x: ev.x,
          y: ev.y,
          data: {
            data,
            extra: { adcode, scope }
          }
        })
      })
      dotLayer.once('loaded', () => {
        chart.container = container
        configCarouselTooltip(chart, view, data || [], null, customSubArea, drawOption)
      })
    })
    return view
  }

  private getDotLayer(
    chart: Chart,
    geoJson: FeatureCollection,
    drawOption: L7PlotDrawOptions<Choropleth>,
    customSubArea: CustomGeoSubArea[]
  ): IPlotLayer {
    const { areaId } = drawOption
    const { basicStyle, tooltip } = parseJson(chart.customAttr)
    const { bubbleCfg } = parseJson(chart.senior)
    const { offsetHeight, offsetWidth } = document.getElementById(drawOption.container)
    const dotData = []
    const options: DotOptions = {
      source: {
        data: dotData,
        parser: {
          type: 'json',
          x: 'x',
          y: 'y'
        }
      },
      shape: 'circle',
      size: {
        field: 'size',
        value: [5, Math.min(offsetHeight, offsetWidth) / 20]
      },
      visible: true,
      zIndex: 0.05,
      color: hexColorToRGBA(basicStyle.colors[0], basicStyle.alpha),
      name: 'bubbleLayer',
      style: {
        opacity: 1
      },
      state: {
        active: { color: 'rgba(30,90,255,1)' }
      },
      tooltip: {
        showComponent: tooltip.show
      }
    }
    if (areaId.startsWith('custom_')) {
      const geoJsonMap = geoJson.features.reduce((p, n) => {
        if (n.properties['adcode']) {
          p['156' + n.properties['adcode']] = n
        }
        return p
      }, {})
      const { areaMapping } = parseJson(chart.senior)
      const customAreaMap = customSubArea.reduce((p, n) => {
        const mappedName = areaMapping?.[areaId]?.[n.name]
        if (mappedName) {
          n.name = mappedName
        }
        p[n.name] = n
        return p
      }, {})
      chart.data?.data?.forEach(d => {
        const area = customAreaMap[d.name]
        if (area) {
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
            dotData.push({
              name: area.name,
              size: d.value,
              properties: d,
              x: center.geometry.coordinates[0],
              y: center.geometry.coordinates[1]
            })
          }
        }
      })
      if (options.tooltip && options.tooltip.showComponent) {
        options.tooltip.items = ['name', 'adcode', 'value']
        options.tooltip.customTitle = ({ name }) => {
          return name
        }
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
          const formatter = formatterMap[head.quotaList?.[0]?.id]
          if (!isEmpty(formatter)) {
            const originValue = parseFloat(head.value as string)
            const value = valueFormatter(originValue, formatter.formatterCfg)
            const name = isEmpty(formatter.chartShowName) ? formatter.name : formatter.chartShowName
            result.push({ ...head, name, value: `${value ?? ''}` })
          }
          head.dynamicTooltipValue?.forEach(item => {
            const formatter = formatterMap[item.fieldId]
            if (formatter) {
              const value = valueFormatter(parseFloat(item.value), formatter.formatterCfg)
              const name = isEmpty(formatter.chartShowName)
                ? formatter.name
                : formatter.chartShowName
              result.push({ color: 'grey', name, value: `${value ?? ''}` })
            }
          })
          return result
        }
        options.tooltip.domStyles = {
          'l7plot-tooltip': {
            'background-color': tooltip.backgroundColor,
            'font-size': `${tooltip.fontSize}px`,
            'line-height': 1.6
          },
          'l7plot-tooltip__name': {
            color: tooltip.color
          },
          'l7plot-tooltip__value': {
            color: tooltip.color
          },
          'l7plot-tooltip__title': {
            color: tooltip.color
          }
        }
      }
    } else {
      const areaMap = chart.data?.data?.reduce((obj, value) => {
        obj[value['field']] = { value: value.value, data: value }
        return obj
      }, {})
      geoJson.features.forEach(item => {
        const name = item.properties['name']
        if (areaMap?.[name]?.value) {
          dotData.push({
            x: item.properties['centroid'][0],
            y: item.properties['centroid'][1],
            size: areaMap[name].value,
            properties: areaMap[name].data,
            name: name
          })
        }
      })
    }
    if (bubbleCfg && bubbleCfg.enable) {
      return new Dot({
        ...options,
        size: {
          field: 'size',
          value: [10, Math.min(offsetHeight, offsetWidth) / 10]
        },
        animate: {
          enable: true,
          speed: bubbleCfg.speed,
          rings: bubbleCfg.rings
        }
      })
    }
    return new Dot(options)
  }

  private configBasicStyle(
    chart: Chart,
    options: ChoroplethOptions,
    context: Record<string, any>
  ): ChoroplethOptions {
    const { areaId }: L7PlotDrawOptions<any> = context.drawOption
    const geoJson: FeatureCollection = context.geoJson
    const { basicStyle, label } = parseJson(chart.customAttr)
    const senior = parseJson(chart.senior)
    const curAreaNameMapping = senior.areaMapping?.[areaId]
    handleGeoJson(geoJson, curAreaNameMapping)
    options.color = basicStyle.areaBaseColor
    if (!chart.data?.data?.length || !geoJson?.features?.length) {
      options.label && (options.label.field = 'name')
      return options
    }
    const data = chart.data.data
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
    const data = chart.data?.data
    const areaMap = data?.reduce((obj, value) => {
      obj[value['field']] = value
      return obj
    }, {})
    //处理label
    options.label = {
      visible: false
    }
    if (label.show) {
      const geoJsonMap = geoJson.features.reduce((p, n) => {
        if (n.properties['adcode']) {
          p['156' + n.properties['adcode']] = n
        }
        return p
      }, {})
      const { areaMapping } = parseJson(chart.senior)
      const labelLocation = []
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
          const content = []
          if (label.showDimension) {
            const mappedName = areaMapping?.[drawOption.areaId]?.[area.name]
            if (mappedName) {
              area.name = mappedName
            }
            content.push(area.name)
          }
          if (label.showQuota) {
            areaMap[area.name] &&
              content.push(valueFormatter(areaMap[area.name].value, label.quotaLabelFormatter))
          }
          const center = centroid(areaJson)
          labelLocation.push({
            name: content.join('\n\n'),
            x: center.geometry.coordinates[0],
            y: center.geometry.coordinates[1]
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
        zIndex: 0.06,
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
      this.configCustomArea
    )(chart, options, context, this)
  }
}
