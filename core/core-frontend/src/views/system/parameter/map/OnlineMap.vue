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
              <el-select v-model="mapEditor.mapType" @change="initLoad">
                <el-option value="gaode" :label="t('chart.map_type_gaode')" />
                <el-option value="tianditu" :label="t('chart.map_type_tianditu')" />
                <!--                <el-option value="baidu" :label="t('chart.map_type_baidu')" />-->
                <el-option value="qq" :label="t('chart.map_type_tencent')" />
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
      <EmptyBackground
        v-if="!mapLoaded"
        img-type="noneWhite"
        :description="t('online_map.empty_desc')"
      />
    </el-main>
  </el-container>
</template>

<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { queryMapKeyApi, saveMapKeyApi, queryMapKeyApiByType } from '@/api/setting/sysParameter'
import { ElMessage } from 'element-plus-secondary'
import EmptyBackground from '@/components/empty-background/src/EmptyBackground.vue'
import OnlineMapTdt from './OnlineMapTdt.vue'
import OnlineMapGaode from './OnlineMapGaode.vue'
import OnlineMapQQ from './OnlineMapQQ.vue'

const { t } = useI18n()
const mapEditor = reactive({
  key: '',
  securityCode: '',
  mapType: ''
})
const mapLoaded = ref(false)
const mapLoading = ref(false)

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
const initLoad = (type?: string) => {
  mapLoading.value = true
  mapLoaded.value = false

  let f
  if (type) {
    f = queryMapKeyApiByType(type)
  } else {
    f = queryMapKeyApi()
  }
  f.then(res => {
    mapEditor.key = res.data.key
    mapEditor.mapType = res.data.mapType
    mapEditor.securityCode = res.data.securityCode

    if (mapEditor.key) {
      mapLoaded.value = true
    }
  })
    .catch(e => {
      console.error(e)
    })
    .finally(() => {
      setTimeout(() => {
        mapLoading.value = false
      }, 2000)
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
