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
import { Scene } from '@antv/l7-scene'
import { HeatmapLayer } from '@antv/l7-layers'
import { DEFAULT_BASIC_STYLE } from '@/views/chart/components/editor/util/chart'
import {
  getMapCenter,
  getMapScene,
  getMapStyle,
  mapRendered,
  qqMapRendered
} from '@/views/chart/components/js/panel/common/common_antv'
const { t } = useI18n()

/**
 * 流向地图
 */
export class HeatMap extends L7ChartView<Scene, L7Config> {
  properties: EditorProperty[] = [
    'background-overall-component',
    'border-style',
    'basic-style-selector',
    'title-selector'
  ]
  propertyInner: EditorPropertyInner = {
    ...MAP_EDITOR_PROPERTY_INNER,
    'basic-style-selector': [
      'colors',
      'heatMapStyle',
      'zoom',
      'showLabel',
      'autoFit',
      'mapCenter',
      'zoomLevel'
    ]
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
    const containerDom = document.getElementById(container)
    const rect = containerDom?.getBoundingClientRect()
    if (rect?.height <= 0) {
      return new L7Wrapper(drawOption.chartObj?.getScene(), [])
    }
    const xAxis = deepCopy(chart.xAxis)
    const yAxis = deepCopy(chart.yAxis)
    let basicStyle: DeepPartial<ChartBasicStyle>
    let miscStyle: DeepPartial<ChartMiscAttr>
    if (chart.customAttr) {
      basicStyle = parseJson(chart.customAttr).basicStyle
      miscStyle = parseJson(chart.customAttr).misc
    }
    const mapKey = await this.getMapKey()
    const mapStyle = getMapStyle(mapKey, basicStyle)
    // 底层
    const chartObj = drawOption.chartObj as unknown as L7Wrapper<L7Config, Scene>
    let scene = chartObj?.getScene()
    const center = getMapCenter(basicStyle)
    scene = await getMapScene(
      chart,
      scene,
      container,
      mapKey,
      basicStyle,
      miscStyle,
      mapStyle,
      center
    )
    this.configZoomButton(chart, scene, mapKey)
    if (xAxis?.length < 2 || yAxis?.length < 1) {
      return new L7Wrapper(scene, undefined)
    }
    const config: L7Config = new HeatmapLayer({
      name: 'line',
      blend: 'normal',
      autoFit: !(basicStyle.autoFit === false),
      zIndex: 10
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

    config.once('inited', () => {
      mapRendered(container)
    })
    config.on('inited', () => {
      qqMapRendered(scene)
    })

    return new L7Wrapper(scene, config)
  }

  protected setupOptions(chart: Chart, config: L7Config): L7Config {
    return flow(this.configEmptyDataStrategy)(chart, config)
  }
}
