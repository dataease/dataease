<template>
  <el-container class="online-map-container">
    <el-aside width="200px" class="online-map-aside">
      <div class="geo-title">
        <span>{{ t('online_map.onlinemap') }}</span>
      </div>
      <div class="online-form-item">
        <div class="map-item">
          <div class="map-item-label">
            <span class="form-label">Key</span>
          </div>
        </div>
        <div class="map-item">
          <el-input v-model="key" />
        </div>
      </div>
      <el-button type="primary" :disabled="!key" @click="saveHandler">{{
        t('commons.save')
      }}</el-button>
    </el-aside>
    <el-main>
      <div v-show="mapLoaded" v-if="!mapReloading" class="de-map-container" :id="domId" />
      <EmptyBackground
        v-if="!mapLoaded"
        img-type="noneWhite"
        :description="t('online_map.empty_desc')"
      />
    </el-main>
  </el-container>
</template>

<script lang="ts" setup>
import { nextTick, onMounted, ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { queryMapKeyApi, saveMapKeyApi } from '@/api/setting/sysParameter'
import { ElMessage } from 'element-plus-secondary'
import EmptyBackground from '@/components/empty-background/src/EmptyBackground.vue'
const { t } = useI18n()
const key = ref('')
const mapInstance = ref(null)
const mapReloading = ref(false)
const domId = ref('de-map-container')
const mapLoaded = ref(false)

const loadMap = () => {
  if (!key.value) {
    return
  }
  const mykey = key.value
  const url = `https://webapi.amap.com/maps?v=2.0&key=${mykey}`

  loadScript(url)
    .then(() => {
      if (mapInstance.value) {
        mapInstance.value?.destroy()
        mapInstance.value = null
        mapReloading.value = true
        setTimeout(() => {
          domId.value = domId.value + '-de-'
          mapReloading.value = false
          nextTick(() => {
            createMapInstance()
          })
        }, 1500)

        return
      }
      createMapInstance()
    })
    .catch(e => {
      if (mapInstance.value) {
        mapInstance.value.destroy()
        mapInstance.value = null
      }
      console.error(e)
    })
}
const createMapInstance = () => {
  mapInstance.value = new window.AMap.Map(domId.value, {
    viewMode: '2D',
    zoom: 11,
    center: [116.397428, 39.90923]
  })
  mapLoaded.value = true
}
const saveHandler = () => {
  saveMapKeyApi({ key: key.value })
    .then(() => {
      ElMessage.success(t('commons.save_success'))
      initLoad()
    })
    .catch(e => {
      console.error(e)
    })
}
const initLoad = () => {
  queryMapKeyApi()
    .then(res => {
      key.value = res.data
      loadMap()
    })
    .catch(e => {
      console.error(e)
    })
}
const loadScript = (url: string) => {
  return new Promise(function (resolve, reject) {
    const scriptId = 'de-gaode-script-id'
    let dom = document.getElementById(scriptId)
    if (dom) {
      dom.parentElement?.removeChild(dom)
      dom = null
      window.AMap = null
    }
    var script = document.createElement('script')

    script.id = scriptId
    script.onload = function () {
      return resolve(null)
    }
    script.onerror = function () {
      return reject(new Error('Load script from '.concat(url, ' failed')))
    }
    script.src = url
    var head = document.head || document.getElementsByTagName('head')[0]
    ;(document.body || head).appendChild(script)
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
      height: 64px;
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
