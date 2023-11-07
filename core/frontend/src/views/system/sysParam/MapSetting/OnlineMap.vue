<template>
  <el-container
    v-loading="loading"
    class="online-map-container"
  >
    <el-aside class="aside-form">
      <div class="form-title">
        <span>{{ $t('online_map.onlinemap') }}</span>
      </div>

      <div class="online-form-item">
        <span class="form-label">Key</span>
        <el-input
          v-model="key"
          size="small"
        />
      </div>
      <el-button
        type="primary"
        :disabled="!key"
        @click="saveHandler"
      >{{ $t('commons.save') }}</el-button>
    </el-aside>
    <el-divider
      direction="vertical"
      class="online-divider"
    />
    <el-main class="main-content">
      <div
        v-show="mapLoaded"
        v-if="!mapReloading"
        :id="domId"
        class="map-gaode-demo"
      />
      <el-empty
        v-if="!mapLoaded"
        :description="$t('online_map.empty_desc')"
      />
    </el-main>
  </el-container>
</template>
<script>
import { saveMapKey, queryMapKey } from '@/api/map/map'
export default {
  name: 'OnlineMap',
  data() {
    return {
      key: '',
      secret: '',
      mapLoaded: false,
      mapInstance: null,
      domId: 'qwertyuiop',
      mapReloading: false,
      loading: false
    }
  },
  mounted() {
    this.initLoad()
  },

  methods: {
    loadMap() {
      if (!this.key) {
        return
      }
      const mykey = this.key
      const url = `https://webapi.amap.com/maps?v=2.0&key=${mykey}`

      this.loadScript(url).then(res => {
        if (this.mapInstance) {
          this.mapInstance.destroy()
          this.mapInstance = null
          this.mapReloading = true
          setTimeout(() => {
            this.domId = this.domId + '-de-'
            this.mapReloading = false
            this.$nextTick(() => {
              this.createMapInstance()
            })
          }, 1500)

          return
        }
        this.createMapInstance()
      }).catch(e => {
        if (this.mapInstance) {
          this.mapInstance.destroy()
          this.mapInstance = null
        }
        console.error(e)
      })
    },
    createMapInstance() {
      this.mapInstance = new window.AMap.Map(this.domId, {
        viewMode: '2D',
        zoom: 11,
        center: [116.397428, 39.90923]
      })
      this.mapLoaded = true
    },
    saveHandler() {
      this.loading = true
      saveMapKey({ key: this.key }).then(() => {
        this.$message({
          message: this.$t('commons.save_success'),
          type: 'success',
          showClose: true
        })
        this.initLoad()
      }).catch(e => {
        console.error(e)
      }).finally(() => {
        this.loading = false
      })
    },
    initLoad() {
      queryMapKey().then(res => {
        this.key = res.data
        this.loadMap()
      }).catch(e => {
        console.error(e)
      })
    },
    loadScript(url) {
      return new Promise(function(resolve, reject) {
        const scriptId = 'de-gaode-script-id'
        let dom = document.getElementById(scriptId)
        if (dom) {
          dom.parentElement.removeChild(dom)
          dom = null
          window.AMap = null
          // return resolve()
        }
        var script = document.createElement('script')

        script.id = scriptId
        script.onload = function() {
          return resolve()
        }
        script.onerror = function() {
          return reject(new Error('Load script from '.concat(url, ' failed')))
        }
        script.src = url
        var head = document.head || document.getElementsByTagName('head')[0];
        (document.body || head).appendChild(script)
      })
    }
  }
}
</script>
<style lang="scss" scoped>
.online-map-container {
    height: 100%;
    .online-divider {
      height: calc(100% - 139px);
      position: absolute;
      top: 112px;
      margin: 0 0 0 330px;
    }
    .aside-form {
        width: 320px !important;
        padding: 0 10px;
        height: 100%;
        .form-title {
            font-size: 16px;
            font-weight: 500;
            line-height: 24px;
            color: #1F2329;
            margin-bottom: 10px;
        }
        .online-form-item {
            font-size: 14px;
            .form-title {
                font-weight: 400;
                color: 1F2329;
                line-height: 22px;
            }
            height: 62px;
            margin-bottom: 10px;
        }
    }
    .main-content {
        width: 100%;
        height: 100%;
        margin-left: 13px;
        .map-gaode-demo {
            width: 100%;
            height: 100%;
            .de-map-iframe {
                width: 100%;
                height: 100%;
                border: none;
            }
        }
    }
}
</style>
