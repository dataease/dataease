export const CUSTOM_TILE_MAP_TYPE = 'customTile'
export const RASTER_TILE_SERVICE_TYPE = 'raster'
export const VECTOR_STYLE_SERVICE_TYPE = 'vector'

export interface OnlineMapConfig {
  key: string
  securityCode: string
  mapType: string
  serviceType: string
  tileUrl: string
  styleUrl: string
  tileScheme: string
  tileSize: string
  tileMinZoom: string
  tileMaxZoom: string
  tileAttribution: string
  styleAttribution: string
  tileAttributionEnabled: string
  styleAttributionEnabled: string
}

export const DEFAULT_ONLINE_MAP_CONFIG: OnlineMapConfig = {
  key: '',
  securityCode: '',
  mapType: '',
  serviceType: RASTER_TILE_SERVICE_TYPE,
  tileUrl: '',
  styleUrl: '',
  tileScheme: 'xyz',
  tileSize: '256',
  tileMinZoom: '0',
  tileMaxZoom: '18',
  tileAttribution: '',
  styleAttribution: '',
  tileAttributionEnabled: 'true',
  styleAttributionEnabled: 'true'
}

export const normalizeOnlineMapConfig = (data?: Partial<OnlineMapConfig>) => {
  const result = { ...DEFAULT_ONLINE_MAP_CONFIG }
  let hasStyleAttribution = false
  if (data?.mapType === CUSTOM_TILE_MAP_TYPE && data.key) {
    try {
      const savedConfig = JSON.parse(data.key)
      hasStyleAttribution = Object.prototype.hasOwnProperty.call(savedConfig, 'styleAttribution')
      Object.assign(result, savedConfig)
    } catch (e) {
      console.warn('Invalid custom map config', e)
    }
  }
  Object.entries(data || {}).forEach(([field, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      ;(result as Record<string, string>)[field] = String(value)
      if (field === 'styleAttribution') {
        hasStyleAttribution = true
      }
    }
  })
  result.serviceType =
    result.serviceType === VECTOR_STYLE_SERVICE_TYPE
      ? VECTOR_STYLE_SERVICE_TYPE
      : RASTER_TILE_SERVICE_TYPE
  result.tileScheme = result.tileScheme === 'tms' ? 'tms' : 'xyz'
  result.tileSize = result.tileSize === '512' ? '512' : '256'
  result.tileAttributionEnabled =
    String(result.tileAttributionEnabled) === 'false' ? 'false' : 'true'
  result.styleAttributionEnabled =
    String(result.styleAttributionEnabled) === 'false' ? 'false' : 'true'
  if (
    result.serviceType === VECTOR_STYLE_SERVICE_TYPE &&
    !hasStyleAttribution &&
    result.tileAttribution
  ) {
    // 旧配置只有一个版权字段，首次读取时按当前服务类型迁移
    result.styleAttribution = result.tileAttribution
  }
  return result
}

export const serializeCustomMapConfig = (config: Partial<OnlineMapConfig>) => {
  return JSON.stringify({
    serviceType: config.serviceType,
    tileUrl: config.tileUrl,
    styleUrl: config.styleUrl,
    tileScheme: config.tileScheme,
    tileSize: config.tileSize,
    tileMinZoom: config.tileMinZoom,
    tileMaxZoom: config.tileMaxZoom,
    tileAttribution: config.tileAttribution,
    styleAttribution: config.styleAttribution,
    tileAttributionEnabled: config.tileAttributionEnabled,
    styleAttributionEnabled: config.styleAttributionEnabled
  })
}

const toNumber = (value: string, defaultValue: number) => {
  const result = Number(value)
  return Number.isFinite(result) ? result : defaultValue
}

export const normalizeTileUrl = (url: string) => {
  return url?.trim()
}

const isValidResourceUrl = (url: string) => /^(https?:\/\/|\/(?!\/)|\.{1,2}\/)/i.test(url?.trim())

export const isValidTileUrl = (url: string) => {
  const normalizedUrl = normalizeTileUrl(url)
  if (
    !normalizedUrl ||
    !normalizedUrl.includes('{z}') ||
    !normalizedUrl.includes('{x}') ||
    !normalizedUrl.includes('{y}')
  ) {
    return false
  }
  return isValidResourceUrl(normalizedUrl)
}

export const isValidStyleUrl = (url: string) => isValidResourceUrl(url)

export const getCustomMapZoomRange = (config: Partial<OnlineMapConfig>) => {
  const minZoom = Math.max(0, Math.min(24, toNumber(config.tileMinZoom, 0)))
  const defaultMaxZoom = config.serviceType === VECTOR_STYLE_SERVICE_TYPE ? 22 : 18
  const maxZoom = Math.max(minZoom, Math.min(24, toNumber(config.tileMaxZoom, defaultMaxZoom)))
  return { minZoom, maxZoom }
}

export const isValidCustomMapZoomRange = (config: Partial<OnlineMapConfig>) => {
  const minZoom = Number(config.tileMinZoom)
  const maxZoom = Number(config.tileMaxZoom)
  return (
    Number.isInteger(minZoom) &&
    Number.isInteger(maxZoom) &&
    minZoom >= 0 &&
    maxZoom <= 24 &&
    minZoom <= maxZoom
  )
}

export const isValidCustomMapConfig = (config: Partial<OnlineMapConfig>) => {
  const serviceUrlValid =
    config.serviceType === VECTOR_STYLE_SERVICE_TYPE
      ? isValidStyleUrl(config.styleUrl)
      : isValidTileUrl(config.tileUrl)
  return serviceUrlValid && isValidCustomMapZoomRange(config)
}

export const isCustomMapAttributionEnabled = (config: Partial<OnlineMapConfig>) => {
  const enabled =
    config.serviceType === VECTOR_STYLE_SERVICE_TYPE
      ? config.styleAttributionEnabled
      : config.tileAttributionEnabled
  // 旧配置没有开关字段，默认保持原有的显示行为
  return String(enabled) !== 'false'
}

export const getCustomMapAttributionOptions = (config: Partial<OnlineMapConfig>) => {
  const attributionEnabled = isCustomMapAttributionEnabled(config)
  const vectorAttribution = config.styleAttribution?.trim()
  return {
    attributionControl: attributionEnabled,
    customAttribution:
      attributionEnabled && config.serviceType === VECTOR_STYLE_SERVICE_TYPE
        ? vectorAttribution || undefined
        : undefined
  }
}

const resolveVectorStyleResourceUrl = (resourceUrl: string, styleUrl?: string) => {
  if (!resourceUrl || !styleUrl) {
    return resourceUrl
  }
  try {
    const baseUrl = new URL(styleUrl, window.location.href)
    return new URL(resourceUrl, baseUrl).toString().replace(/%7B/gi, '{').replace(/%7D/gi, '}')
  } catch (e) {
    return resourceUrl
  }
}

const resolveVectorStyleResources = (style: any, styleUrl?: string) => {
  if (!style?.sources) {
    return style
  }
  const sources = Object.fromEntries(
    Object.entries(style.sources).map(([sourceId, source]) => {
      if (!source || typeof source !== 'object') {
        return [sourceId, source]
      }
      const sourceOptions = { ...(source as Record<string, any>) }
      if (typeof sourceOptions.url === 'string') {
        sourceOptions.url = resolveVectorStyleResourceUrl(sourceOptions.url, styleUrl)
      }
      if (Array.isArray(sourceOptions.tiles)) {
        sourceOptions.tiles = sourceOptions.tiles.map(tileUrl =>
          resolveVectorStyleResourceUrl(tileUrl, styleUrl)
        )
      }
      if (typeof sourceOptions.data === 'string') {
        sourceOptions.data = resolveVectorStyleResourceUrl(sourceOptions.data, styleUrl)
      }
      return [sourceId, sourceOptions]
    })
  )
  const resolvedStyle = { ...style, sources }
  if (typeof resolvedStyle.glyphs === 'string') {
    resolvedStyle.glyphs = resolveVectorStyleResourceUrl(resolvedStyle.glyphs, styleUrl)
  }
  if (typeof resolvedStyle.sprite === 'string') {
    resolvedStyle.sprite = resolveVectorStyleResourceUrl(resolvedStyle.sprite, styleUrl)
  } else if (Array.isArray(resolvedStyle.sprite)) {
    resolvedStyle.sprite = resolvedStyle.sprite.map(sprite => ({
      ...sprite,
      url: resolveVectorStyleResourceUrl(sprite.url, styleUrl)
    }))
  }
  return resolvedStyle
}

export const replaceVectorStyleAttribution = (
  style: any,
  attribution?: string,
  styleUrl?: string
) => {
  const customAttribution = attribution?.trim()
  if (!customAttribution || !style?.sources) {
    return style
  }

  const resolvedStyle = resolveVectorStyleResources(style, styleUrl)
  const sources = Object.fromEntries(
    Object.entries(resolvedStyle.sources).map(([sourceId, source]) => {
      if (!source || typeof source !== 'object') {
        return [sourceId, source]
      }
      const sourceOptions = { ...(source as Record<string, any>) }
      // 显式版权优先于远端 TileJSON，避免资源加载后原版权重新出现
      sourceOptions.attribution = customAttribution
      return [sourceId, sourceOptions]
    })
  )
  // MapLibre 会自动去重来源版权和 customAttribution，最终仅展示自定义内容
  return { ...resolvedStyle, sources }
}

export const setVectorStyleLabelVisibility = (
  style: any,
  showLabel: boolean,
  styleUrl?: string
) => {
  const resolvedStyle = resolveVectorStyleResources(style, styleUrl)
  if (showLabel || !Array.isArray(resolvedStyle?.layers)) {
    return resolvedStyle
  }
  const layers = resolvedStyle.layers.map(layer => {
    if (layer.type !== 'symbol' || layer.layout?.['text-field'] === undefined) {
      return layer
    }
    // 清空 text-field 只隐藏底图文字，保留同一 symbol 图层中的图标
    return {
      ...layer,
      layout: {
        ...layer.layout,
        'text-field': ''
      }
    }
  })
  return { ...resolvedStyle, layers }
}

export const buildCustomMapStyle = (config: Partial<OnlineMapConfig>) => {
  if (config.serviceType === VECTOR_STYLE_SERVICE_TYPE) {
    // 矢量地图由完整 Style JSON 统一描述瓦片、字体、图标和图层
    return config.styleUrl?.trim()
  }

  const { minZoom, maxZoom } = getCustomMapZoomRange(config)
  const tileSize = toNumber(config.tileSize, 256) === 512 ? 512 : 256
  const attribution = isCustomMapAttributionEnabled(config)
    ? config.tileAttribution?.trim()
    : undefined
  const source = {
    type: 'raster',
    tiles: [normalizeTileUrl(config.tileUrl)],
    tileSize,
    minzoom: minZoom,
    maxzoom: maxZoom,
    scheme: config.tileScheme === 'tms' ? 'tms' : 'xyz',
    // 可选字段为空时不写入 Style，避免 MapLibre 将 undefined 判定为无效值
    ...(attribution ? { attribution } : {})
  }

  return {
    version: 8,
    sources: {
      'dataease-custom-tile': source
    },
    layers: [
      {
        id: 'dataease-custom-tile',
        type: 'raster',
        source: 'dataease-custom-tile',
        paint: {
          // 关闭瓦片跨层淡入，减少与 L7 叠加层在缩放时的视觉错位
          'raster-fade-duration': 0
        }
      }
    ]
  }
}
