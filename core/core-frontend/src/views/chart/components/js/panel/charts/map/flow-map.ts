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
import { LineLayer } from '@antv/l7-layers'
import { queryMapKeyApi } from '@/api/setting/sysParameter'
const { t } = useI18n()

/**
 * 流向地图
 */
export class FlowMap extends L7ChartView<Scene, L7Config> {
  properties: EditorProperty[] = [
    'background-overall-component',
    'basic-style-selector',
    'title-selector'
  ]
  propertyInner: EditorPropertyInner = {
    ...MAP_EDITOR_PROPERTY_INNER,
    'basic-style-selector': ['mapStyle', 'zoom']
  }
  axis: AxisType[] = ['xAxis', 'xAxisExt', 'filter']
  axisConfig: AxisConfig = {
    xAxis: {
      name: `起点经纬度 / ${t('chart.dimension')}`,
      type: 'd',
      limit: 2
    },
    xAxisExt: {
      name: `终点经纬度 / ${t('chart.dimension')}`,
      type: 'd',
      limit: 2
    }
  }
  constructor() {
    super('flow-map', [])
  }

  async drawChart(drawOption: L7DrawConfig<L7Config>) {
    const { chart, container } = drawOption
    const xAxis = deepCopy(chart.xAxis)
    const xAxisExt = deepCopy(chart.xAxisExt)
    let basicStyle
    let miscStyle
    if (chart.customAttr) {
      basicStyle = parseJson(chart.customAttr).basicStyle
      miscStyle = parseJson(chart.customAttr).misc
    }
    const flowLineStyle = {
      type: miscStyle.mapLineType,
      size: miscStyle.mapLineWidth,
      animate: miscStyle.mapLineAnimate,
      animateDuration: miscStyle.mapLineAnimateDuration,
      gradient: miscStyle.mapLineGradient,
      sourceColor: miscStyle.mapLineSourceColor,
      targetColor: miscStyle.mapLineTargetColor,
      alpha: basicStyle.alpha
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
    if (xAxis?.length < 2 || xAxisExt?.length < 2) {
      return new L7Wrapper(scene, undefined)
    }
    const config: L7Config = new LineLayer({
      name: 'line',
      blend: 'normal',
      autoFit: true
    })
      .source(chart.data?.tableRow ? chart.data.tableRow : [], {
        parser: {
          type: 'json',
          x: xAxis[0].dataeaseName,
          y: xAxis[1].dataeaseName,
          x1: xAxisExt[0].dataeaseName,
          y1: xAxisExt[1].dataeaseName
        }
      })
      .size(flowLineStyle.size)
      .shape(flowLineStyle.type)
      .animate({
        enable: flowLineStyle.animate,
        duration: flowLineStyle.animateDuration,
        interval: 1,
        trailLength: 1
      })
    if (flowLineStyle.gradient) {
      config.style({
        sourceColor: flowLineStyle.sourceColor,
        targetColor: flowLineStyle.targetColor,
        opacity: flowLineStyle.alpha / 100
      })
    } else {
      config
        .style({
          opacity: flowLineStyle.alpha / 100
        })
        .color(flowLineStyle.sourceColor)
    }
    this.configZoomButton(chart, scene)
    return new L7Wrapper(scene, config)
  }

  getMapKey = async () => {
    const key = 'online-map-key'
    if (!localStorage.getItem(key)) {
      await queryMapKeyApi().then(res => localStorage.setItem(key, res.data))
    }
    return localStorage.getItem(key)
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    chart.customAttr.misc.mapLineAnimate = true
    return chart
  }

  protected setupOptions(chart: Chart, config: L7Config): L7Config {
    return flow(this.configEmptyDataStrategy)(chart, config)
  }
}
