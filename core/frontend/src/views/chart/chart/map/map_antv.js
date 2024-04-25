import { Scene, LineLayer } from '@antv/l7'
import { GaodeMap } from '@antv/l7-maps'
import { getLanguage } from '@/lang'
import { queryMapKey } from '@/api/map/map'

export async function baseFlowMapOption(chartId, chart) {
  const xAxis = JSON.parse(chart.xaxis)
  const xAxisExt = JSON.parse(chart.xaxisExt)
  let customAttr
  if (chart.customAttr) {
    customAttr = JSON.parse(chart.customAttr)
  }
  const size = customAttr.size
  const color = customAttr.color
  const mapStyle = `amap://styles/${color.mapStyle ? color.mapStyle : 'normal'}`
  const lang = getLanguage().includes('zh') ? 'zh' : 'en'
  let init = false
  const key = await getMapKey()
  const mapInstance = new Scene({
    id: chartId,
    map: new GaodeMap({
      token: key ?? undefined,
      lang: lang,
      pitch: size.mapPitch,
      style: mapStyle
    }),
    logoVisible: false
  })
  init = true
  if (xAxis?.length < 2 || xAxisExt?.length < 2) {
    await mapInstance.removeAllLayer()
    return mapInstance
  }
  mapInstance.removeAllLayer()
    .then(() => {
      const lineLayer = new LineLayer({
        name: 'line',
        blend: 'normal',
        autoFit: true
      })
        .source(chart.data.tableRow, {
          parser: {
            type: 'json',
            x: xAxis[0].dataeaseName,
            y: xAxis[1].dataeaseName,
            x1: xAxisExt[0].dataeaseName,
            y1: xAxisExt[1].dataeaseName
          }
        })
        .size(size.mapLineWidth)
        .shape(size.mapLineType)
        .animate({
          enable: size.mapLineAnimate,
          duration: size.mapLineAnimateDuration,
          interval: 1,
          trailLength: 1
        })
      if (color.mapLineGradient) {
        lineLayer.style({
          sourceColor: color.mapLineSourceColor,
          targetColor: color.mapLineTargetColor,
          opacity: color.alpha / 100
        })
      } else {
        lineLayer.style({
          opacity: color.alpha / 100
        })
          .color(color.mapLineSourceColor)
      }
      if (!init) {
        mapInstance.addLayer(lineLayer)
      }
      mapInstance.on('loaded', () => {
        mapInstance.addLayer(lineLayer)
      })
    })
  return mapInstance
}

const getMapKey = async() => {
  const key = 'online-map-key'
  if (!localStorage.getItem(key)) {
    await queryMapKey().then(res => localStorage.setItem(key, res.data))
  }
  return localStorage.getItem(key)
}
