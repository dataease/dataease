import { useI18n } from '@/hooks/web/useI18n'
import {
  L7ChartView,
  L7Config,
  L7DrawConfig,
  L7Wrapper
} from '@/views/chart/components/js/panel/types/impl/l7'
import { MAP_EDITOR_PROPERTY_INNER } from '@/views/chart/components/js/panel/charts/map/common'
import { hexColorToRGBA, parseJson } from '@/views/chart/components/js/util'
import { deepCopy } from '@/utils/utils'
import { Scene } from '@antv/l7-scene'
import { LineLayer } from '@antv/l7-layers'
import { PointLayer } from '@antv/l7-layers'
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
export class FlowMap extends L7ChartView<Scene, L7Config> {
  properties: EditorProperty[] = [
    'background-overall-component',
    'border-style',
    'basic-style-selector',
    'title-selector',
    'flow-map-line-selector',
    'flow-map-point-selector',
    'bubble-animate'
  ]
  propertyInner: EditorPropertyInner = {
    ...MAP_EDITOR_PROPERTY_INNER,
    'basic-style-selector': [
      'mapBaseStyle',
      'mapLineStyle',
      'zoom',
      'showLabel',
      'autoFit',
      'mapCenter',
      'zoomLevel'
    ]
  }
  axis: AxisType[] = ['xAxis', 'xAxisExt', 'filter', 'flowMapStartName', 'flowMapEndName', 'yAxis']
  axisConfig: AxisConfig = {
    xAxis: {
      name: `${t('chart.start_coordinates')} / ${t('chart.dimension')}`,
      type: 'd',
      limit: 2
    },
    xAxisExt: {
      name: `${t('chart.end_coordinates')} / ${t('chart.dimension')}`,
      type: 'd',
      limit: 2
    },
    flowMapStartName: {
      name: `${t('chart.start_name')} / ${t('chart.dimension')}`,
      type: 'd',
      limit: 1,
      allowEmpty: true
    },
    flowMapEndName: {
      name: `${t('chart.end_name')} / ${t('chart.dimension')}`,
      type: 'd',
      limit: 1,
      allowEmpty: true
    },
    yAxis: {
      name: `${t('chart.flow_map_line_width')} / ${t('chart.quota')}`,
      type: 'q',
      limit: 1,
      tooltip: t('chart.flow_map_line_width_tip'),
      allowEmpty: true
    }
  }
  constructor() {
    super('flow-map', [])
  }

  async drawChart(drawOption: L7DrawConfig<L7Config>) {
    const { chart, container } = drawOption
    const containerDom = document.getElementById(container)
    const rect = containerDom?.getBoundingClientRect()
    if (rect?.height <= 0) {
      return new L7Wrapper(drawOption.chartObj?.getScene(), [])
    }
    const xAxis = deepCopy(chart.xAxis)
    const xAxisExt = deepCopy(chart.xAxisExt)
    const { basicStyle, misc } = deepCopy(parseJson(chart.customAttr))

    const mapKey = await this.getMapKey()
    const mapStyle = getMapStyle(mapKey, basicStyle)
    // 底层
    const chartObj = drawOption.chartObj as unknown as L7Wrapper<L7Config, Scene>
    let scene = chartObj?.getScene()

    const center = getMapCenter(basicStyle)
    scene = await getMapScene(chart, scene, container, mapKey, basicStyle, misc, mapStyle, center)

    this.configZoomButton(chart, scene, mapKey)
    if (xAxis?.length < 2 || xAxisExt?.length < 2) {
      return new L7Wrapper(scene, undefined)
    }
    const configList = []
    configList.push(this.lineConfig(chart, xAxis, xAxisExt, basicStyle, misc))
    this.startAndEndNameConfig(chart, xAxis, xAxisExt, misc, configList)
    this.pointConfig(chart, xAxis, xAxisExt, misc, configList)
    configList[0].once('inited', () => {
      mapRendered(container)
    })
    for (let i = 0; i < configList.length; i++) {
      configList[i].on('inited', () => {
        qqMapRendered(scene)
      })
    }
    return new L7Wrapper(scene, configList)
  }

  lineConfig = (chart, xAxis, xAxisExt, basicStyle, misc) => {
    const flowLineStyle = {
      type: misc.flowMapConfig.lineConfig.mapLineType,
      size:
        misc.flowMapConfig.lineConfig.mapLineType === 'line'
          ? misc.flowMapConfig.lineConfig.mapLineWidth / 2
          : misc.flowMapConfig.lineConfig.mapLineWidth,
      animate: misc.flowMapConfig.lineConfig.mapLineAnimate,
      animateDuration: misc.flowMapConfig.lineConfig.mapLineAnimateDuration,
      gradient: misc.flowMapConfig.lineConfig.mapLineGradient,
      sourceColor: misc.flowMapConfig.lineConfig.mapLineSourceColor,
      targetColor: misc.flowMapConfig.lineConfig.mapLineTargetColor,
      alpha: misc.flowMapConfig.lineConfig.alpha
    }
    const colorsWithAlpha = basicStyle.colors.map(color =>
      hexColorToRGBA(color, misc.flowMapConfig.lineConfig.alpha)
    )
    flowLineStyle.sourceColor = colorsWithAlpha[0]
    flowLineStyle.targetColor = colorsWithAlpha[1]
    // 线条粗细
    let lineWidthField = null
    const yAxis = deepCopy(chart.yAxis)
    if (yAxis.length > 0) {
      lineWidthField = yAxis[0].dataeaseName
    }
    // 线条颜色
    let lineColorField = null
    const yAxisExt = deepCopy(chart.yAxisExt)
    if (yAxisExt.length > 0) {
      lineColorField = yAxisExt[0].dataeaseName
    }
    const asteriskField = '*'
    const data = []
    chart.data?.tableRow.forEach(item => {
      const newKey = 'f_record'
      const newObj = Object.keys(item).reduce((acc, key) => {
        if (key === asteriskField) {
          acc[newKey] = item[key]
        } else {
          acc[key] = item[key]
        }
        return acc
      }, {})
      data.push(newObj)
    })
    const config: L7Config = new LineLayer({
      name: 'line',
      blend: 'normal',
      autoFit: !(basicStyle.autoFit === false)
    })
      .source(data, {
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

    if (lineWidthField) {
      config.size(lineWidthField === asteriskField ? 'f_record' : lineWidthField, [1, 10])
    }
    if (lineColorField) {
      config.style({
        opacity: flowLineStyle.alpha / 100
      })
      config.color(lineColorField)
    } else {
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
    }

    return config
  }

  startAndEndNameConfig = (chart, xAxis, xAxisExt, misc, configList) => {
    const flowMapStartName = deepCopy(chart.flowMapStartName)
    const flowMapEndName = deepCopy(chart.flowMapEndName)
    const textColor = misc.flowMapConfig.pointConfig.text.color
    const textFontSize = misc.flowMapConfig.pointConfig.text.fontSize
    const has = new Map()
    if (flowMapStartName?.length > 0) {
      const startTextLayer = new PointLayer()
        .source(chart.data?.tableRow, {
          parser: {
            type: 'json',
            x: xAxis[0].dataeaseName,
            y: xAxis[1].dataeaseName
          }
        })
        .shape(flowMapStartName[0].dataeaseName, args => {
          if (has.has('from-' + args)) {
            return ''
          }
          has.set('from-' + args, args)
          return args
        })
        .size(textFontSize)
        .color(textColor)
        .style({
          textAnchor: 'top', // 文本相对锚点的位置 center|left|right|top|bottom|top-left
          textOffset: [0, 0], // 文本相对锚点的偏移量 [水平, 垂直]
          spacing: 2, // 字符间距
          padding: [1, 1], // 文本包围盒 padding [水平，垂直]，影响碰撞检测结果，避免相邻文本靠的太近
          textAllowOverlap: true,
          fontFamily: chart.fontFamily ? chart.fontFamily : undefined
        })
      configList.push(startTextLayer)
    }
    if (flowMapEndName?.length > 0) {
      const endTextLayer = new PointLayer()
        .source(chart.data?.tableRow, {
          parser: {
            type: 'json',
            x: xAxisExt[0].dataeaseName,
            y: xAxisExt[1].dataeaseName
          }
        })
        .shape(flowMapEndName[0].dataeaseName, args => {
          if (has.has('from-' + args) || has.has('to-' + args)) {
            return ''
          }
          has.set('to-' + args, args)
          return args
        })
        .size(textFontSize)
        .color(textColor)
        .style({
          textAnchor: 'top', // 文本相对锚点的位置 center|left|right|top|bottom|top-left
          textOffset: [0, 0], // 文本相对锚点的偏移量 [水平, 垂直]
          spacing: 2, // 字符间距
          padding: [1, 1], // 文本包围盒 padding [水平，垂直]，影响碰撞检测结果，避免相邻文本靠的太近
          textAllowOverlap: true,
          fontFamily: chart.fontFamily ? chart.fontFamily : undefined
        })
      configList.push(endTextLayer)
    }
  }

  pointConfig = (chart, xAxis, xAxisExt, misc, configList) => {
    const color = misc.flowMapConfig.pointConfig.point.color
    const size = misc.flowMapConfig.pointConfig.point.size
    const { bubbleCfg } = parseJson(chart.senior)
    const fromDefaultPointLayer = new PointLayer({ zIndex: -1 })
      .source(chart.data?.tableRow, {
        parser: {
          type: 'json',
          x: xAxis[0].dataeaseName,
          y: xAxis[1].dataeaseName
        }
      })
      .shape('circle')
      .size(size)
      .color(color)
      .style({
        blur: 0.6
      })
    const toDefaultPointLayer = new PointLayer({ zIndex: -1 })
      .source(chart.data?.tableRow, {
        parser: {
          type: 'json',
          x: xAxisExt[0].dataeaseName,
          y: xAxisExt[1].dataeaseName
        }
      })
      .shape('circle')
      .size(size)
      .color(color)
      .style({
        blur: 0.6
      })
    if (bubbleCfg && bubbleCfg.enable) {
      const animate = {
        enable: true,
        speed: bubbleCfg.speed,
        rings: bubbleCfg.rings
      }
      fromDefaultPointLayer.size(size * 2)
      fromDefaultPointLayer.animate(animate)
      toDefaultPointLayer.size(size * 2)
      toDefaultPointLayer.animate(animate)
    }
    configList.push(fromDefaultPointLayer)
    configList.push(toDefaultPointLayer)
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    chart.customAttr.misc.flowMapConfig.lineConfig.mapLineAnimate = true
    return chart
  }
}
