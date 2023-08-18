import {
  L7PlotChartView,
  L7PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/l7plot'
import { Choropleth, ChoroplethOptions } from '@antv/l7plot/dist/esm/plots/choropleth'
import { flow, getGeoJsonFile, parseJson } from '@/views/chart/components/js/util'
import { handleGeoJson } from '@/views/chart/components/js/panel/common/common_antv'
import { FeatureCollection } from '@antv/l7plot/dist/esm/plots/choropleth/types'
import { cloneDeep } from 'lodash-es'
import { useI18n } from '@/hooks/web/useI18n'

const { t } = useI18n()

export class Map extends L7PlotChartView<ChoroplethOptions, Choropleth> {
  properties: EditorProperty[] = [
    'background-overall-component',
    'basic-style-selector',
    'title-selector',
    'label-selector',
    'tooltip-selector'
  ]
  propertyInner: EditorPropertyInner = {
    'background-overall-component': ['all'],
    'basic-style-selector': ['colors', 'alpha', 'areaBorderColor', 'suspension'],
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
    'label-selector': ['color', 'fontSize', 'labelBgColor', 'labelShadow', 'labelShadowColor'],
    'tooltip-selector': ['color', 'fontSize', 'backgroundColor', 'formatter']
  }
  axis: AxisType[] = ['xAxis', 'yAxis', 'area', 'drill', 'filter']
  axisConfig: AxisConfig = {
    xAxis: {
      name: `${t('chart.area')}/${t('chart.dimension')}`
    },
    yAxis: {
      name: `${t('chart.chart_data')}/${t('chart.quota')}`,
      limit: 1
    }
  }

  constructor() {
    super('map', [])
  }

  async drawChart(drawOption: L7PlotDrawOptions<Choropleth>): Promise<Choropleth> {
    const { chart, chartObj, level, areaId, container, action } = drawOption
    const geoJson = cloneDeep(await getGeoJsonFile(areaId))
    const options: ChoroplethOptions = {
      map: {
        type: 'mapbox',
        style: 'blank'
      },
      geoArea: {
        type: 'geojson'
      },
      source: {
        data: chart.data?.data || [],
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
        field: 'name'
      },
      state: {
        active: { stroke: 'green', lineWidth: 1 }
      },
      tooltip: {
        items: ['name', 'value']
      },
      zoom: {
        position: 'bottomright'
      },
      legend: {
        position: 'bottomleft'
      },
      // 禁用线上地图数据
      customFetchGeoData: () => null
    }
    this.setupOptions(chart, options, drawOption, geoJson)
    const view = new Choropleth(container, options)
    view.once('loaded', () => {
      view.on('fillAreaLayer:click', (_: MouseEvent) => {
        const param = {}
        action(param)
      })
    })

    return view
  }
  private configBasicStyle(
    chart: Chart,
    options: ChoroplethOptions,
    extra: any[]
  ): ChoroplethOptions {
    const { areaId }: L7PlotDrawOptions<any> = extra[0]
    const geoJson: FeatureCollection = extra[1]
    const customAttr = parseJson(chart.customAttr)
    const senior = parseJson(chart.senior)
    const curAreaNameMapping = senior.areaMapping?.[areaId]
    handleGeoJson(geoJson, curAreaNameMapping)
    options.color = {
      field: 'value',
      value: [customAttr.basicStyle.colors[0]],
      scale: {
        type: 'quantize',
        unknown: customAttr.basicStyle.areaBaseColor
      }
    }
    if (!chart.data?.data?.length || !geoJson?.features?.length) {
      return
    }
    const data = chart.data.data
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
    })
    let colors = customAttr.basicStyle.colors
    if (validArea < colors.length) {
      colors = colors.slice(0, validArea)
    }
    if (colors.length) {
      options.color.value = colors
    }
    return options
  }

  protected setupOptions(
    chart: Chart,
    options: ChoroplethOptions,
    ...extra: any[]
  ): ChoroplethOptions {
    return flow(
      this.configLabel,
      this.configStyle,
      this.configTooltip,
      this.configBasicStyle
    )(chart, options, extra)
  }
}
