import { L7PlotChartView, L7PlotDrawOptions } from '@/views/chart/components/js/panel/types'
import { Choropleth, ChoroplethOptions } from '@antv/l7plot'
import { flow, parseJson } from '@/views/chart/components/js/util'
import { handleGeoJson } from '@/views/chart/components/js/panel/common/common_antv'

export class Map extends L7PlotChartView<ChoroplethOptions, Choropleth> {
  constructor() {
    super('map', [])
  }

  drawChart(drawOption: L7PlotDrawOptions<Choropleth>): Choropleth {
    const { chart, geoJson, level } = drawOption
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
    this.setupOptions(chart, options, drawOption)
    const view = new Choropleth(drawOption.container, options)
    view.once('loaded', () => {
      // 要在当前实例加载完成再去删除上一个实例，避免内存泄露
      if (drawOption.chartObj) {
        drawOption.chartObj.destroy()
      }
      view.on('fillAreaLayer:click', (_: MouseEvent) => {
        const param = {}
        drawOption.action(param)
      })
    })

    return view
  }
  private configColor(chart: Chart, options: ChoroplethOptions, extra: any[]): ChoroplethOptions {
    const { geoJson, areaId }: L7PlotDrawOptions<any> = extra[0]
    const customAttr = parseJson(chart.customAttr)
    const senior = parseJson(chart.senior)
    const curAreaNameMapping = senior.mapMapping?.[areaId]
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
    let colors = customAttr.color.colors
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
      this.configColor
    )(chart, options, extra)
  }
}
