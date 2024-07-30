import { useI18n } from '@/hooks/web/useI18n'
import {
  L7ChartView,
  L7Config,
  L7DrawConfig,
  L7Wrapper
} from '@/views/chart/components/js/panel/types/impl/l7'
import { MAP_EDITOR_PROPERTY_INNER } from '@/views/chart/components/js/panel/charts/map/common'
import { flow, parseJson } from '@/views/chart/components/js/util'
import { deepCopy } from '@/utils/utils'
import { GaodeMap } from '@antv/l7-maps'
import { Scene } from '@antv/l7-scene'
import { HeatmapLayer } from '@antv/l7-layers'
import { DEFAULT_BASIC_STYLE } from '@/views/chart/components/editor/util/chart'
import { mapRendered, mapRendering } from '@/views/chart/components/js/panel/common/common_antv'
const { t } = useI18n()

/**
 * 流向地图
 */
export class HeatMap extends L7ChartView<Scene, L7Config> {
  properties: EditorProperty[] = [
    'background-overall-component',
    'basic-style-selector',
    'title-selector'
  ]
  propertyInner: EditorPropertyInner = {
    ...MAP_EDITOR_PROPERTY_INNER,
    'basic-style-selector': ['colors', 'heatMapStyle', 'zoom']
  }
  axis: AxisType[] = ['xAxis', 'yAxis', 'filter']
  axisConfig: AxisConfig = {
    xAxis: {
      name: `${t('chart.longitude_and_latitude')} / ${t('chart.dimension')}`,
      type: 'd',
      limit: 2
    },
    yAxis: {
      name: `${t('chart.chart_data')} / ${t('chart.quota')}`,
      type: 'q',
      limit: 1
    }
  }
  constructor() {
    super('heat-map', [])
  }

  async drawChart(drawOption: L7DrawConfig<L7Config>) {
    const { chart, container } = drawOption
    const xAxis = deepCopy(chart.xAxis)
    const yAxis = deepCopy(chart.yAxis)
    let basicStyle
    let miscStyle
    if (chart.customAttr) {
      basicStyle = parseJson(chart.customAttr).basicStyle
      miscStyle = parseJson(chart.customAttr).misc
    }
    const mapStyle = `amap://styles/${basicStyle.mapStyle ? basicStyle.mapStyle : 'normal'}`
    const key = await this.getMapKey()
    // 底层
    const scene = new Scene({
      id: container,
      logoVisible: false,
      map: new GaodeMap({
        token: key ?? undefined,
        style: mapStyle,
        pitch: miscStyle.mapPitch,
        zoom: 2.5
      })
    })
    mapRendering(container)
    scene.once('loaded', () => {
      mapRendered(container)
    })
    if (xAxis?.length < 2 || yAxis?.length < 1) {
      return new L7Wrapper(scene, undefined)
    }
    const config: L7Config = new HeatmapLayer({
      name: 'line',
      blend: 'normal',
      autoFit: true
    })
      .source(chart.data?.data, {
        parser: {
          type: 'json',
          x: 'x',
          y: 'y'
        }
      })
      .size('value', [0, 1.0]) // weight映射通道
      .shape(basicStyle.heatMapType ?? DEFAULT_BASIC_STYLE.heatMapType)

    config.style({
      intensity: basicStyle.heatMapIntensity ?? DEFAULT_BASIC_STYLE.heatMapIntensity,
      radius: basicStyle.heatMapRadius ?? DEFAULT_BASIC_STYLE.heatMapRadius,
      rampColors: {
        colors: basicStyle.colors.reverse(),
        positions: [0, 0.11, 0.22, 0.33, 0.44, 0.55, 0.66, 0.77, 0.88, 1.0]
      }
    })

    this.configZoomButton(chart, scene)
    return new L7Wrapper(scene, config)
  }

  protected setupOptions(chart: Chart, config: L7Config): L7Config {
    return flow(this.configEmptyDataStrategy)(chart, config)
  }
}
