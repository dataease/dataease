<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { Scene } from '@antv/l7-scene'
import { MapLibre } from '@antv/l7-maps'
import { useI18n } from '@/hooks/web/useI18n'
import {
  buildCustomMapStyle,
  getCustomMapAttributionOptions,
  getCustomMapZoomRange,
  isCustomMapAttributionEnabled,
  isValidCustomMapConfig,
  replaceVectorStyleAttribution,
  VECTOR_STYLE_SERVICE_TYPE,
  type OnlineMapConfig
} from '@/utils/onlineMap'

const props = defineProps<{
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
}>()

const { t } = useI18n()
const domId = `de-custom-tile-map-${Date.now()}-${Math.random().toString(36).slice(2)}`
const mapErrorKey = ref('')
const mapErrorParams = ref<Record<string, string | number>>({})
const mapErrorMessage = computed(() =>
  mapErrorKey.value ? t(`online_map.${mapErrorKey.value}`, mapErrorParams.value) : ''
)
let scene: Scene
let renderTimer: ReturnType<typeof setTimeout>
let renderVersion = 0
let resizeObserver: ResizeObserver
let resizeFrame: number

const setMapError = (key: string, params: Record<string, string | number> = {}) => {
  mapErrorKey.value = key
  mapErrorParams.value = params
}

const clearMapError = () => {
  mapErrorKey.value = ''
  mapErrorParams.value = {}
}

const getResourceEnvironmentError = (url?: string) => {
  if (!url) {
    return ''
  }
  try {
    const resourceUrl = new URL(url, window.location.href)
    const localHosts = ['localhost', '127.0.0.1', '::1', '[::1]']
    if (
      localHosts.includes(resourceUrl.hostname.toLowerCase()) &&
      !localHosts.includes(window.location.hostname.toLowerCase())
    ) {
      return 'map_localhost_error'
    }
    if (window.location.protocol === 'https:' && resourceUrl.protocol === 'http:') {
      return 'map_mixed_content_error'
    }
  } catch (e) {
    return ''
  }
  return ''
}

const setMapResourceError = (event: any) => {
  if (navigator.onLine === false) {
    setMapError('map_network_offline_error')
    return
  }
  const configuredUrl =
    props.serviceType === VECTOR_STYLE_SERVICE_TYPE ? props.styleUrl : props.tileUrl
  const environmentError = getResourceEnvironmentError(event?.error?.url || configuredUrl)
  if (environmentError) {
    setMapError(environmentError)
    return
  }

  const errorText = [event?.error?.message, event?.message, event?.sourceId, event?.error?.url]
    .filter(Boolean)
    .join(' ')
    .toLowerCase()
  const status = event?.error?.status || errorText.match(/\b(?:4|5)\d{2}\b/)?.[0] || undefined

  if (
    props.serviceType !== VECTOR_STYLE_SERVICE_TYPE ||
    /tile|pbf|png|jpe?g|webp/.test(errorText)
  ) {
    setMapError(status ? 'map_tile_http_error' : 'map_tile_load_error', { status })
    return
  }
  if (/glyph|font/.test(errorText)) {
    setMapError('map_glyph_load_error')
    return
  }
  if (/sprite|icon|image/.test(errorText)) {
    setMapError('map_sprite_load_error')
    return
  }
  setMapError(status ? 'map_resource_http_error' : 'map_resource_load_error', { status })
}

const resizeMap = () => {
  cancelAnimationFrame(resizeFrame)
  resizeFrame = requestAnimationFrame(() => {
    ;(scene?.map as { resize?: () => void })?.resize?.()
  })
}

const destroyMap = () => {
  if (scene) {
    scene.destroy()
    scene = undefined
  }
}

const renderMap = async () => {
  clearTimeout(renderTimer)
  const currentVersion = ++renderVersion
  renderTimer = setTimeout(async () => {
    destroyMap()
    clearMapError()
    const config = { ...props } as OnlineMapConfig
    if (!isValidCustomMapConfig(config)) {
      return
    }

    await nextTick()
    let mapStyle = buildCustomMapStyle(config)
    if (props.serviceType === VECTOR_STYLE_SERVICE_TYPE) {
      let response: Response
      try {
        // 预览侧直接校验 Style，避免由 DataEase 后端代理客户地图服务
        response = await fetch(config.styleUrl.trim())
      } catch (e) {
        if (currentVersion === renderVersion) {
          const environmentError = getResourceEnvironmentError(config.styleUrl)
          setMapError(
            navigator.onLine === false
              ? 'map_network_offline_error'
              : environmentError || 'map_style_request_error'
          )
        }
        return
      }
      if (currentVersion !== renderVersion) {
        return
      }
      if (!response.ok) {
        setMapError('map_style_http_error', { status: response.status })
        return
      }

      let style: any
      try {
        style = await response.json()
      } catch (e) {
        if (currentVersion === renderVersion) {
          setMapError('map_style_json_error')
        }
        return
      }
      if (currentVersion !== renderVersion) {
        return
      }
      if (!style || style.version !== 8 || !style.sources || !Array.isArray(style.layers)) {
        setMapError('map_style_format_error')
        return
      }
      if (isCustomMapAttributionEnabled(config) && config.styleAttribution?.trim()) {
        mapStyle = replaceVectorStyleAttribution(style, config.styleAttribution, config.styleUrl)
      }
    }
    if (currentVersion !== renderVersion) {
      return
    }

    const { minZoom, maxZoom } = getCustomMapZoomRange(config)
    const mapOptions: Record<string, any> = {
      style: mapStyle,
      minZoom,
      maxZoom,
      ...getCustomMapAttributionOptions(config),
      fadeDuration: 0,
      // 与栅格瓦片保持一致，横向循环世界副本避免持续拖动露出空白
      renderWorldCopies: true,
      preserveDrawingBuffer: true
    }
    // 矢量预览固定使用中国中心，与图表中的自定义地图默认视角保持一致
    mapOptions.center =
      props.serviceType === VECTOR_STYLE_SERVICE_TYPE ? [105, 35] : [116.397428, 39.90923]
    // 全国范围的矢量预览需要保留边缘留白，避免方形容器裁切东北和南海区域。
    const defaultZoom = props.serviceType === VECTOR_STYLE_SERVICE_TYPE ? 2.5 : 3
    mapOptions.zoom = Math.min(Math.max(defaultZoom, minZoom), maxZoom)
    scene = new Scene({
      id: domId,
      logoVisible: false,
      map: new MapLibre(mapOptions as any)
    })
    scene.once('loaded', () => {
      resizeMap()
      // 地图初始化后持续监听 Style、瓦片、字体和图标资源错误
      scene.map?.on?.('error', event => {
        if (currentVersion === renderVersion) {
          setMapResourceError(event)
        }
      })
    })
  }, 300)
}

watch(
  () => [
    props.serviceType,
    props.tileUrl,
    props.styleUrl,
    props.tileScheme,
    props.tileSize,
    props.tileMinZoom,
    props.tileMaxZoom,
    props.tileAttribution,
    props.styleAttribution,
    props.tileAttributionEnabled,
    props.styleAttributionEnabled
  ],
  renderMap
)

onMounted(async () => {
  await nextTick()
  const container = document.getElementById(domId)
  if (container) {
    // 设置页左右区域变化时同步 MapLibre 画布尺寸
    resizeObserver = new ResizeObserver(resizeMap)
    resizeObserver.observe(container)
  }
  renderMap()
})
onBeforeUnmount(() => {
  renderVersion++
  clearTimeout(renderTimer)
  cancelAnimationFrame(resizeFrame)
  resizeObserver?.disconnect()
  destroyMap()
})
</script>

<template>
  <div class="custom-map-preview">
    <div :id="domId" class="de-map-container" />
    <div v-if="mapErrorMessage" class="map-error">{{ mapErrorMessage }}</div>
  </div>
</template>

<style scoped lang="less">
.custom-map-preview,
.de-map-container {
  height: 100%;
  width: 100%;
  position: relative;
}

.map-error {
  position: absolute;
  top: 16px;
  left: 50%;
  z-index: 10;
  transform: translateX(-50%);
  padding: 8px 12px;
  border-radius: 4px;
  color: #f54a45;
  background: rgb(255 255 255 / 90%);
}
</style>
