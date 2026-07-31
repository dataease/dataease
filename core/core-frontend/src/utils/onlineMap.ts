import { sanitizeHtml } from '@/utils/utils'

export const CUSTOM_TILE_MAP_TYPE = 'customTile'
export const RASTER_TILE_SERVICE_TYPE = 'raster'
export const VECTOR_STYLE_SERVICE_TYPE = 'vector'
export const ONLINE_MAP_URL_MAX_LENGTH = 2048

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

const hasCompleteCustomFields = (data?: Partial<OnlineMapConfig>) => {
  if (data?.serviceType === VECTOR_STYLE_SERVICE_TYPE) {
    return !!data.styleUrl
  }
  return data?.serviceType === RASTER_TILE_SERVICE_TYPE && !!data.tileUrl
}

export const normalizeOnlineMapConfig = (data?: Partial<OnlineMapConfig>) => {
  let source = data || {}
  if (data?.mapType === CUSTOM_TILE_MAP_TYPE && !hasCompleteCustomFields(data) && data.key) {
    try {
      const legacyConfig = JSON.parse(data.key)
      if (hasCompleteCustomFields(legacyConfig)) {
        source = { ...legacyConfig, mapType: CUSTOM_TILE_MAP_TYPE }
      }
    } catch (e) {
      console.warn('Invalid legacy custom map config', e)
    }
  }

  const result = { ...DEFAULT_ONLINE_MAP_CONFIG }
  Object.entries(source).forEach(([field, value]) => {
    if (value !== undefined && value !== null) {
      ;(result as Record<string, string>)[field] = String(value)
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
  if (result.mapType === CUSTOM_TILE_MAP_TYPE) {
    result.key = ''
    result.securityCode = ''
  }
  return result
}

const toNumber = (value: string, defaultValue: number) => {
  const result = Number(value)
  return Number.isFinite(result) ? result : defaultValue
}

const isValidResourceUrl = (url: string) => {
  const normalizedUrl = url?.trim()
  return (
    !!normalizedUrl &&
    normalizedUrl.length <= ONLINE_MAP_URL_MAX_LENGTH &&
    /^(https?:\/\/|\/(?!\/)|\.{1,2}\/)/i.test(normalizedUrl)
  )
}

export const isValidTileUrl = (url: string) => {
  const normalizedUrl = url?.trim()
  return (
    isValidResourceUrl(normalizedUrl) &&
    normalizedUrl.includes('{z}') &&
    normalizedUrl.includes('{x}') &&
    normalizedUrl.includes('{y}')
  )
}

export const isValidStyleUrl = (url: string) => isValidResourceUrl(url)

export const getCustomMapZoomRange = (config: Partial<OnlineMapConfig>) => {
  const minZoom = Math.max(0, Math.min(24, toNumber(config.tileMinZoom, 0)))
  const defaultMaxZoom = config.serviceType === VECTOR_STYLE_SERVICE_TYPE ? 22 : 18
  const maxZoom = Math.max(minZoom, Math.min(24, toNumber(config.tileMaxZoom, defaultMaxZoom)))
  return { minZoom, maxZoom }
}

export const isValidCustomMapZoomRange = (config: Partial<OnlineMapConfig>) => {
  const zoomPattern = /^\d+(?:\.\d)?$/
  const minZoomValue = String(config.tileMinZoom ?? '').trim()
  const maxZoomValue = String(config.tileMaxZoom ?? '').trim()
  if (!zoomPattern.test(minZoomValue) || !zoomPattern.test(maxZoomValue)) {
    return false
  }
  const minZoom = Number(minZoomValue)
  const maxZoom = Number(maxZoomValue)
  // 缩放范围与地图运行时能力保持一致
  return minZoom >= 0 && maxZoom <= 24 && minZoom <= maxZoom
}

export const isValidCustomMapConfig = (config: Partial<OnlineMapConfig>) => {
  const serviceUrlValid =
    config.serviceType === VECTOR_STYLE_SERVICE_TYPE
      ? isValidStyleUrl(config.styleUrl)
      : isValidTileUrl(config.tileUrl)
  const attributionValid =
    (config.tileAttribution?.length ?? 0) <= 255 && (config.styleAttribution?.length ?? 0) <= 255
  return serviceUrlValid && attributionValid && isValidCustomMapZoomRange(config)
}

export const isCustomMapAttributionEnabled = (config: Partial<OnlineMapConfig>) => {
  const enabled =
    config.serviceType === VECTOR_STYLE_SERVICE_TYPE
      ? config.styleAttributionEnabled
      : config.tileAttributionEnabled
  return String(enabled) !== 'false'
}

const sanitizeAttribution = (value?: string) => {
  const sanitized = sanitizeHtml(value?.trim() || '')
  return sanitized || undefined
}

export const getCustomMapAttributionOptions = (config: Partial<OnlineMapConfig>) => {
  const attributionEnabled = isCustomMapAttributionEnabled(config)
  const vectorAttribution = sanitizeAttribution(config.styleAttribution)
  return {
    attributionControl: attributionEnabled,
    customAttribution:
      attributionEnabled && config.serviceType === VECTOR_STYLE_SERVICE_TYPE
        ? vectorAttribution
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
  const customAttribution = sanitizeAttribution(attribution)
  if (!customAttribution || !style?.sources) {
    return resolveVectorStyleResources(style, styleUrl)
  }

  const resolvedStyle = resolveVectorStyleResources(style, styleUrl)
  const sources = Object.fromEntries(
    Object.entries(resolvedStyle.sources).map(([sourceId, source]) => {
      if (!source || typeof source !== 'object') {
        return [sourceId, source]
      }
      return [sourceId, { ...(source as Record<string, any>), attribution: customAttribution }]
    })
  )
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
    return config.styleUrl?.trim()
  }

  const { minZoom, maxZoom } = getCustomMapZoomRange(config)
  const tileSize = toNumber(config.tileSize, 256) === 512 ? 512 : 256
  const attribution = isCustomMapAttributionEnabled(config)
    ? sanitizeAttribution(config.tileAttribution)
    : undefined
  const source = {
    type: 'raster',
    tiles: [config.tileUrl?.trim()],
    tileSize,
    minzoom: minZoom,
    maxzoom: maxZoom,
    scheme: config.tileScheme === 'tms' ? 'tms' : 'xyz',
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
          'raster-fade-duration': 0
        }
      }
    ]
  }
}

export const getCustomMapIdentity = (config: Partial<OnlineMapConfig>) => {
  const source = [
    CUSTOM_TILE_MAP_TYPE,
    config.serviceType,
    config.tileUrl,
    config.styleUrl,
    config.tileScheme,
    config.tileSize,
    config.tileMinZoom,
    config.tileMaxZoom,
    config.tileAttribution,
    config.styleAttribution,
    config.tileAttributionEnabled,
    config.styleAttributionEnabled
  ].join('|')
  // Scene 只保存配置指纹，避免把含令牌的完整 URL 再复制到运行时属性
  let hash = 0
  for (let i = 0; i < source.length; i++) {
    hash = (hash << 5) - hash + source.charCodeAt(i)
    hash |= 0
  }
  return `${CUSTOM_TILE_MAP_TYPE}:${hash}`
}
