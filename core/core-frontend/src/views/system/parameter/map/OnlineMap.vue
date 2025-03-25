<template>
  <el-container class="online-map-container">
    <el-aside width="200px" class="online-map-aside">
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
              <el-select v-model="mapEditor.mapType">
                <el-option value="gaode" :label="t('chart.map_type_gaode')" />
                <el-option value="tianditu" :label="t('chart.map_type_tianditu')" />
                <!--                <el-option value="baidu" :label="t('chart.map_type_baidu')" />-->
              </el-select>
            </div>
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
          </div>
        </el-col>
      </el-row>
      <el-row>
        <el-button type="primary" :disabled="!mapEditor.key" @click="saveHandler">
          {{ t('commons.save') }}
        </el-button>
      </el-row>
    </el-aside>
    <el-main>
      <div
        v-show="mapLoaded && mapEditor.mapType === 'gaode'"
        class="de-map-container"
        :id="domId"
      />
      <div
        v-show="mapLoaded && mapEditor.mapType === 'tianditu'"
        class="de-map-container"
        :id="domId + '-tianditu'"
      />
      <EmptyBackground
        v-if="!mapLoaded"
        img-type="noneWhite"
        :description="t('online_map.empty_desc')"
      />
    </el-main>
  </el-container>
</template>

<script lang="ts" setup>
import { onMounted, onUnmounted, reactive, ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { queryMapKeyApi, saveMapKeyApi } from '@/api/setting/sysParameter'
import { ElMessage } from 'element-plus-secondary'
import EmptyBackground from '@/components/empty-background/src/EmptyBackground.vue'
import { Scene } from '@antv/l7-scene'
import { GaodeMap, TMap } from '@antv/l7-maps'
const { t } = useI18n()
const mapEditor = reactive({
  key: '',
  securityCode: '',
  mapType: ''
})
const mapInstance = ref(null)
const domId = ref('de-map-container')
const mapLoaded = ref(false)

let scene: Scene = undefined

const loadMap = (callback?: any) => {
  if (!mapEditor.key) {
    return
  }
  const center: [number, number] = [116.397428, 39.90923]
  mapLoaded.value = true
  if (scene) {
    scene.destroy()
    destroyTdt()
    loadMapScene(mapEditor, center)
  } else {
    loadMapScene(mapEditor, center)
  }
  if (callback) {
    callback()
  }
}

function destroyTdt() {
  const tdt = document.querySelector(`#${domId.value}-tianditu`)
  if (tdt && tdt.firstChild) {
    tdt.removeChild(tdt.firstChild)
  }
}

function loadMapScene(mapEditor, center) {
  const mykey = mapEditor.key
  switch (mapEditor.mapType) {
    case 'tianditu':
      scene = new Scene({
        id: domId.value + '-tianditu',
        logoVisible: false,
        map: new TMap({
          token: mykey,
          center,
          zoom: 11
        })
      })

      scene.on('loaded', () => {
        const tdtControl = document.querySelector(
          `#${domId.value}-tianditu .tdt-control-zoom.tdt-bar.tdt-control`
        )
        if (tdtControl) {
          tdtControl.style.display = 'none'
        }
        const tdtCopyrightControl = document.querySelector(
          `#${domId.value}-tianditu .tdt-control-copyright.tdt-control`
        )
        if (tdtCopyrightControl) {
          tdtCopyrightControl.style.display = 'none'
        }
      })

      return
    default:
      scene = new Scene({
        id: domId.value,
        logoVisible: false,
        map: new GaodeMap({
          token: mykey,
          pitch: 0,
          center,
          zoom: 11,
          showLabel: true
        })
      })

      return
  }
}

const saveHandler = () => {
  saveMapKeyApi(mapEditor)
    .then(() => {
      ElMessage.success(t('commons.save_success'))
      initLoad()
    })
    .catch(e => {
      console.error(e)
    })
}
const initLoad = (callback?: any) => {
  queryMapKeyApi()
    .then(res => {
      mapEditor.key = res.data.key
      mapEditor.mapType = res.data.mapType
      mapEditor.securityCode = res.data.securityCode
      loadMap(callback)
    })
    .catch(e => {
      console.error(e)
    })
}
onMounted(() => {
  initLoad(() => {
    if (mapEditor.mapType === 'tianditu') {
      //针对天地图初次加载会错位，重新加载一遍
      scene.once('loaded', () => {
        setTimeout(() => {
          loadMap()
        }, 1000)
      })
    }
  })
})
onUnmounted(() => {
  scene.destroy()
  destroyTdt()
  scene = undefined
  mapLoaded.value = false
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
    width: 280px !important;
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
        height: 32px;
        .map-item-label {
          height: 22px;
          line-height: 22px;
          font-size: 14px;
          font-weight: 400;
          color: #1f2329;
        }
      }
    }
  }
}
</style>
