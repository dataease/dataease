<template>
  <el-container class="online-map-container">
    <el-aside width="360px" class="online-map-aside">
      <div class="geo-title">
        <span>{{ t('online_map.onlinemap') }}</span>
      </div>
      <el-row>
        <el-col>
          <div class="online-form-item">
            <div class="map-item">
              <div class="map-item-label">
                <span class="form-label">{{ t('chart.map_type') }}</span>
              </div>
            </div>
            <div class="map-item">
              <el-select v-model="mapEditor.mapType" @change="initLoad">
                <el-option value="gaode" :label="t('chart.map_type_gaode')" />
                <el-option value="tianditu" :label="t('chart.map_type_tianditu')" />
                <!--                <el-option value="baidu" :label="t('chart.map_type_baidu')" />-->
                <el-option value="qq" :label="t('chart.map_type_tencent')" />
                <el-option :value="CUSTOM_TILE_MAP_TYPE" :label="t('chart.map_type_custom_tile')" />
              </el-select>
            </div>
            <template v-if="!isCustomMap">
              <div class="map-item">
                <div class="map-item-label">
                  <span class="form-label">Key</span>
                </div>
              </div>
              <div class="map-item">
                <el-input v-model="mapEditor.key" />
              </div>
              <div class="map-item">
                <div class="map-item-label">
                  <span class="form-label">{{ t('chart.security_code') }}</span>
                </div>
              </div>
              <div class="map-item">
                <el-input v-model="mapEditor.securityCode" />
              </div>
            </template>
            <div v-else class="custom-map-form">
              <div class="map-item">
                <div class="map-item-label">
                  <span class="form-label">{{ t('online_map.service_type') }}</span>
                </div>
                <el-select v-model="mapEditor.serviceType">
                  <el-option
                    :value="RASTER_TILE_SERVICE_TYPE"
                    :label="t('online_map.raster_tile_url')"
                  />
                  <el-option
                    :value="VECTOR_STYLE_SERVICE_TYPE"
                    :label="t('online_map.vector_style_json')"
                  />
                </el-select>
              </div>
              <template v-if="isRasterService">
                <div class="map-item">
                  <div class="map-item-label">
                    <span class="form-label">{{ t('online_map.tile_url') }}</span>
                  </div>
                  <el-input
                    v-model="mapEditor.tileUrl"
                    type="textarea"
                    :rows="3"
                    :maxlength="ONLINE_MAP_URL_MAX_LENGTH"
                    show-word-limit
                    :placeholder="t('online_map.tile_url_placeholder', tilePlaceholderParams)"
                  />
                  <div class="map-item-tip">
                    {{ t('online_map.tile_url_tip', tilePlaceholderParams) }}
                  </div>
                </div>
                <div class="map-item">
                  <div class="map-item-label">
                    <span class="form-label">{{ t('online_map.tile_scheme') }}</span>
                  </div>
                  <el-select v-model="mapEditor.tileScheme">
                    <el-option value="xyz" label="XYZ" />
                    <el-option value="tms" label="TMS" />
                  </el-select>
                </div>
                <div class="map-item">
                  <div class="map-item-label">
                    <span class="form-label">{{ t('online_map.tile_size') }}</span>
                  </div>
                  <el-select v-model="mapEditor.tileSize">
                    <el-option value="256" label="256 × 256" />
                    <el-option value="512" label="512 × 512" />
                  </el-select>
                </div>
              </template>
              <div v-else class="map-item">
                <div class="map-item-label">
                  <span class="form-label">{{ t('online_map.style_url') }}</span>
                </div>
                <el-input
                  v-model="mapEditor.styleUrl"
                  type="textarea"
                  :rows="3"
                  :maxlength="ONLINE_MAP_URL_MAX_LENGTH"
                  show-word-limit
                  :placeholder="t('online_map.style_url_placeholder')"
                />
                <div class="map-item-tip">{{ t('online_map.style_url_tip') }}</div>
              </div>
              <div class="map-item map-item-inline">
                <div>
                  <div class="map-item-label">
                    <span class="form-label">{{ t('online_map.min_zoom') }}</span>
                  </div>
                  <el-input-number
                    v-model="tileMinZoom"
                    class="text-left map-zoom-input"
                    :min="0"
                    :max="tileMaxZoom ?? 24"
                    :step="0.1"
                    :precision="1"
                    step-strictly
                    controls-position="right"
                  />
                </div>
                <div>
                  <div class="map-item-label">
                    <span class="form-label">{{ t('online_map.max_zoom') }}</span>
                  </div>
                  <el-input-number
                    v-model="tileMaxZoom"
                    class="text-left map-zoom-input"
                    :min="tileMinZoom ?? 0"
                    :max="24"
                    :step="0.1"
                    :precision="1"
                    step-strictly
                    controls-position="right"
                  />
                </div>
              </div>
              <div class="map-item-tip map-zoom-tip">{{ t('online_map.zoom_tip') }}</div>
              <div class="map-item map-attribution-switch">
                <div class="map-item-label">
                  <span class="form-label">{{ t('online_map.show_attribution') }}</span>
                </div>
                <el-switch v-model="attributionEnabled" />
              </div>
              <div v-if="attributionEnabled" class="map-item">
                <div class="map-item-label">
                  <span class="form-label">{{ t('online_map.attribution') }}</span>
                </div>
                <el-input
                  v-if="isRasterService"
                  v-model="mapEditor.tileAttribution"
                  maxlength="255"
                  show-word-limit
                />
                <el-input
                  v-else
                  v-model="mapEditor.styleAttribution"
                  maxlength="255"
                  show-word-limit
                />
                <div class="map-item-tip">
                  {{
                    t(
                      isRasterService
                        ? 'online_map.raster_attribution_tip'
                        : 'online_map.vector_attribution_tip'
                    )
                  }}
                </div>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
      <el-row>
        <el-button type="primary" :disabled="!canSave" @click="saveHandler">
          {{ t('commons.save') }}
        </el-button>
      </el-row>
    </el-aside>
    <el-main v-loading="mapLoading">
      <OnlineMapGaode
        v-if="!mapLoading && mapLoaded && mapEditor.key && mapEditor.mapType === 'gaode'"
        :map-key="mapEditor.key"
        :security-code="mapEditor.securityCode"
      />
      <OnlineMapTdt
        v-if="!mapLoading && mapLoaded && mapEditor.key && mapEditor.mapType === 'tianditu'"
        :map-key="mapEditor.key"
      />
      <OnlineMapQQ
        v-if="!mapLoading && mapLoaded && mapEditor.key && mapEditor.mapType === 'qq'"
        :map-key="mapEditor.key"
        :security-code="mapEditor.securityCode"
      />
      <OnlineMapCustom
        v-if="!mapLoading && isCustomMap && customMapConfigValid"
        :service-type="mapEditor.serviceType"
        :tile-url="mapEditor.tileUrl"
        :style-url="mapEditor.styleUrl"
        :tile-scheme="mapEditor.tileScheme"
        :tile-size="mapEditor.tileSize"
        :tile-min-zoom="mapEditor.tileMinZoom"
        :tile-max-zoom="mapEditor.tileMaxZoom"
        :tile-attribution="mapEditor.tileAttribution"
        :style-attribution="mapEditor.styleAttribution"
        :tile-attribution-enabled="mapEditor.tileAttributionEnabled"
        :style-attribution-enabled="mapEditor.styleAttributionEnabled"
      />
      <EmptyBackground
        v-if="!mapLoading && !previewReady"
        img-type="noneWhite"
        :description="t('online_map.empty_desc')"
      />
    </el-main>
  </el-container>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { queryMapKeyApi, saveMapKeyApi, queryMapKeyApiByType } from '@/api/setting/sysParameter'
import { ElMessage } from 'element-plus-secondary'
import EmptyBackground from '@/components/empty-background/src/EmptyBackground.vue'
import OnlineMapTdt from './OnlineMapTdt.vue'
import OnlineMapGaode from './OnlineMapGaode.vue'
import OnlineMapQQ from './OnlineMapQQ.vue'
import OnlineMapCustom from './OnlineMapCustom.vue'
import { useMapStoreWithOut } from '@/store/modules/map'
import {
  CUSTOM_TILE_MAP_TYPE,
  DEFAULT_ONLINE_MAP_CONFIG,
  isValidCustomMapConfig,
  normalizeOnlineMapConfig,
  ONLINE_MAP_URL_MAX_LENGTH,
  RASTER_TILE_SERVICE_TYPE,
  VECTOR_STYLE_SERVICE_TYPE,
  type OnlineMapConfig
} from '@/utils/onlineMap'

const { t } = useI18n()
const mapStore = useMapStoreWithOut()
const tilePlaceholderParams = {
  z: '{z}',
  x: '{x}',
  y: '{y}'
}
const mapEditor = reactive<OnlineMapConfig>({ ...DEFAULT_ONLINE_MAP_CONFIG })
// 数值组件负责输入约束，持久化继续沿用字符串协议
const createZoomModel = (field: 'tileMinZoom' | 'tileMaxZoom') =>
  computed<number | undefined>({
    get: () => {
      const value = Number(mapEditor[field])
      return Number.isFinite(value) ? value : undefined
    },
    set: value => {
      mapEditor[field] = value === undefined ? '' : String(value)
    }
  })
const tileMinZoom = createZoomModel('tileMinZoom')
const tileMaxZoom = createZoomModel('tileMaxZoom')
const mapLoaded = ref(false)
const mapLoading = ref(false)
let loadVersion = 0
const isCustomMap = computed(() => mapEditor.mapType === CUSTOM_TILE_MAP_TYPE)
const isRasterService = computed(() => mapEditor.serviceType === RASTER_TILE_SERVICE_TYPE)
const attributionEnabled = computed({
  get: () =>
    (isRasterService.value
      ? mapEditor.tileAttributionEnabled
      : mapEditor.styleAttributionEnabled) !== 'false',
  set: enabled => {
    const value = enabled ? 'true' : 'false'
    if (isRasterService.value) {
      mapEditor.tileAttributionEnabled = value
    } else {
      mapEditor.styleAttributionEnabled = value
    }
  }
})
const customMapConfigValid = computed(() => isValidCustomMapConfig(mapEditor))
const canSave = computed(() => (isCustomMap.value ? customMapConfigValid.value : !!mapEditor.key))
const previewReady = computed(() =>
  isCustomMap.value ? customMapConfigValid.value : mapLoaded.value && !!mapEditor.key
)

const saveHandler = () => {
  const payload: Partial<OnlineMapConfig> = isCustomMap.value
    ? { ...mapEditor, key: '', securityCode: '' }
    : {
        mapType: mapEditor.mapType,
        key: mapEditor.key,
        securityCode: mapEditor.securityCode
      }
  saveMapKeyApi(payload)
    .then(() => {
      mapStore.setKey(payload)
      ElMessage.success(t('commons.save_success'))
      initLoad()
    })
    .catch(e => {
      console.error(e)
    })
}
const initLoad = (type?: string) => {
  const currentVersion = ++loadVersion
  mapLoading.value = true
  mapLoaded.value = false

  let f
  if (type) {
    f = queryMapKeyApiByType(type)
  } else {
    f = queryMapKeyApi()
  }
  f.then(res => {
    if (currentVersion !== loadVersion) {
      return
    }
    Object.assign(mapEditor, normalizeOnlineMapConfig(res.data))
    mapEditor.mapType = res.data.mapType || type || 'gaode'
    mapLoaded.value = isCustomMap.value ? customMapConfigValid.value : !!mapEditor.key
  })
    .catch(e => {
      console.error(e)
    })
    .finally(() => {
      if (currentVersion === loadVersion) {
        mapLoading.value = false
      }
    })
}
onMounted(() => {
  initLoad()
})
</script>

<style lang="less" scoped>
.de-map-container {
  height: 100%;
  width: 100%;
  position: relative;
}
.online-map-container {
  height: 100%;
  .online-map-aside {
    width: 360px !important;
    border-right: 1px solid #1f232926;
    padding: 16px;
    .geo-title {
      height: 24px;
      line-height: 24px;
      margin-bottom: 16px;
      span:first-child {
        font-size: 16px;
        font-weight: 500;
        line-height: 24px;
      }
    }
    .online-form-item {
      margin-bottom: 16px;
      .map-item {
        margin-bottom: 12px;
        .map-item-label {
          height: 22px;
          line-height: 22px;
          font-size: 14px;
          font-weight: 400;
          color: #1f2329;
        }
        .map-item-tip {
          margin-top: 4px;
          color: #8f959e;
          font-size: 12px;
          line-height: 18px;
        }
      }
      .map-item-inline {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 12px;
        .map-zoom-input {
          width: 100%;
        }
      }
      .map-attribution-switch {
        display: flex;
        align-items: center;
        justify-content: space-between;
      }
      .custom-map-form {
        // 自定义地图标签与控件间距保持与内置地图表单一致
        .map-item-label {
          margin-bottom: 12px;
        }
        .map-attribution-switch .map-item-label {
          margin-bottom: 0;
        }
      }
      .map-zoom-tip {
        margin-top: -8px;
        margin-bottom: 12px;
        color: #8f959e;
        font-size: 12px;
        line-height: 18px;
      }
    }
  }
}
</style>
