import { MapLibre } from '@antv/l7-maps'
import { Scene } from '@antv/l7-scene'
import {
  buildCustomMapStyle,
  getCustomMapAttributionOptions,
  getCustomMapIdentity,
  getCustomMapZoomRange,
  isCustomMapAttributionEnabled,
  replaceVectorStyleAttribution,
  setVectorStyleLabelVisibility,
  VECTOR_STYLE_SERVICE_TYPE,
  type OnlineMapConfig
} from '@/utils/onlineMap'

interface CustomMapSceneOptions {
  scene?: Scene
  container: string
  mapConfig: OnlineMapConfig
  basicStyle: ChartBasicStyle
  miscStyle: ChartMiscAttr
  mapStyle: any
  center?: [number, number]
}

const vectorStyleCache = new Map<string, Promise<any>>()

const loadVectorStyle = (styleUrl: string) => {
  const normalizedUrl = styleUrl.trim()
  let request = vectorStyleCache.get(normalizedUrl)
  if (!request) {
    request = fetch(normalizedUrl)
      .then(response => {
        if (!response.ok) {
          throw new Error(`Style request failed: ${response.status}`)
        }
        return response.json()
      })
      .then(style => {
        if (!style || style.version !== 8 || !style.sources || !Array.isArray(style.layers)) {
          throw new Error('Invalid MapLibre Style JSON')
        }
        return style
      })
      .catch(error => {
        vectorStyleCache.delete(normalizedUrl)
        throw error
      })
    vectorStyleCache.set(normalizedUrl, request)
  }
  return request
}

const prepareVectorStyle = async (
  config: OnlineMapConfig,
  basicStyle: ChartBasicStyle,
  defaultStyle: any
) => {
  const customAttribution =
    isCustomMapAttributionEnabled(config) && !!config.styleAttribution?.trim()
  if (!customAttribution && basicStyle.showLabel !== false) {
    return defaultStyle
  }
  try {
    let style = await loadVectorStyle(config.styleUrl)
    if (customAttribution) {
      style = replaceVectorStyleAttribution(style, config.styleAttribution, config.styleUrl)
    }
    return setVectorStyleLabelVisibility(style, basicStyle.showLabel !== false, config.styleUrl)
  } catch (e) {
    console.warn('Failed to prepare custom vector map style', e)
    return defaultStyle
  }
}

export const getCustomOnlineMapStyle = (config: OnlineMapConfig) => buildCustomMapStyle(config)

export const isCustomOnlineMapScene = (scene?: Scene) => !!(scene as any)?.deCustomMapIdentity

export const getCustomOnlineMapScene = async ({
  scene,
  container,
  mapConfig,
  basicStyle,
  miscStyle,
  mapStyle,
  center
}: CustomMapSceneOptions) => {
  if (mapConfig.serviceType === VECTOR_STYLE_SERVICE_TYPE) {
    mapStyle = await prepareVectorStyle(mapConfig, basicStyle, mapStyle)
  }

  const identity = getCustomMapIdentity(mapConfig)
  if (scene && (scene as any).deCustomMapIdentity !== identity) {
    scene.destroy()
    scene = undefined
  }

  if (!scene) {
    const { minZoom, maxZoom } = getCustomMapZoomRange(mapConfig)
    const defaultZoom = Math.min(Math.max(3, minZoom), maxZoom)
    const initialZoom =
      basicStyle.autoFit === false
        ? Math.min(Math.max(Number(basicStyle.zoomLevel) || defaultZoom, minZoom), maxZoom)
        : defaultZoom
    scene = new Scene({
      id: container,
      logoVisible: false,
      map: new MapLibre({
        style: mapStyle,
        pitch: miscStyle.mapPitch,
        // 自定义底图使用 WGS84 经纬度和 Web Mercator 投影
        center: center ?? [105, 35],
        zoom: initialZoom,
        minZoom,
        maxZoom,
        ...getCustomMapAttributionOptions(mapConfig),
        fadeDuration: 0,
        renderWorldCopies: true,
        preserveDrawingBuffer: true
      } as any)
    })
    ;(scene as any).deCustomMapIdentity = identity
    if (scene.map) {
      ;(scene.map as any).deMapProvider = 'customTile'
    }
    return scene
  }

  if (scene.getLayers()?.length) {
    await scene.removeAllLayer()
  }
  try {
    scene.setPitch(miscStyle.mapPitch)
    scene.setMapStyle(mapStyle)
  } catch (e) {
    console.warn('Failed to update custom map style', e)
  }
  if (basicStyle.autoFit === false && center) {
    scene.setZoomAndCenter(basicStyle.zoomLevel, center)
  }
  return scene
}
