<template>
  <el-row
    ref="mainPlayer"
    style="width: 100%;height: 100%"
  >
    <div
      v-if="element.streamMediaLinks[element.streamMediaLinks.videoType].url"
      class="video-container"
    >
      <video
        :ref="'player-'+element.id"
        class="centered-video"
        name="centeredVideo"
        :loop="pOption.loop"
        :controls="inScreen"
        muted
      />
      <div
        v-if="editMode==='edit'"
        class="stream-mask edit-mask-stream"
      />
      <div
        v-if="mobileLayoutStatus"
        class="stream-mask"
      >
        <span style="opacity: 0.7;">
          <span style="color: lightgray;">{{ $t('panel.stream_mobile_tips') }}</span>
        </span>
      </div>
    </div>
    <div
      v-else
      class="info-stream-class"
    >
      <span>{{ $t('panel.link_add_tips_pre') }}</span>
      <i class="icon iconfont icon-chaolianjie"/>
      <span>{{ $t('panel.stream_media_add_tips') }}</span>
    </div>
  </el-row>
</template>
<script>

import flvjs from 'flv.js'
import '@/custom-theme.css'
import bus from '@/utils/bus'
import { mapState } from 'vuex'

export default {
  props: {
    propValue: {
      type: String,
      require: true
    },
    element: {
      type: Object
    },
    editMode: {
      type: String,
      require: false,
      default: 'edit'
    },
    active: {
      type: Boolean,
      require: false,
      default: false
    },
    h: {
      type: Number,
      default: 200
    },
    inScreen: {
      type: Boolean,
      required: false,
      default: true
    }
  },
  data() {
    return {
      pOption: this.element.streamMediaLinks[this.element.streamMediaLinks.videoType],
      flvPlayer: null,
      videoShow: true
    }
  },

  computed: {
    moveFlag() {
      return (this.element.optStatus.dragging || this.element.optStatus.resizing)
    },
    curGap() {
      return this.element.auxiliaryMatrix ? this.componentGap : 0
    },
    player() {
      return this.$refs.videoPlayer.player
    },
    ...mapState([
      'mobileLayoutStatus'
    ])
  },
  watch: {
    pOption: {
      handler: function() {
        this.initOption()
      },
      deep: true
    }
  },
  created() {
  },
  mounted() {
    this.initOption()
    bus.$on('streamMediaLinksChange-' + this.element.id, this.streamMediaLinksChange)
  },
  beforeDestroy() {
    bus.$off('streamMediaLinksChange-' + this.element.id, this.streamMediaLinksChange)
    this.destroyPlayer()
  },
  methods: {
    streamMediaLinksChange() {
      this.pOption = this.element.streamMediaLinks[this.element.streamMediaLinks.videoType]
      this.flvPlayer = null
      this.videoShow = false
      this.$nextTick(() => {
        this.videoShow = true
        this.initOption()
      })
    },
    initOption() {
      if (flvjs.isSupported() && this.pOption.url) {
        this.destroyPlayer()
        const video = this.$refs['player-' + this.element.id]
        if (video) {
          try {
            this.flvPlayer = flvjs.createPlayer(this.pOption,
              {
                enableWorker: false, // 不启用分离线程
                enableStashBuffer: false, // 关闭IO隐藏缓冲区
                isLive: this.pOption.isLive,
                lazyLoad: false
              })
            this.flvPlayer.attachMediaElement(video)
            this.flvPlayer.load()
            this.flvPlayer.play()
          } catch (error) {
            console.error('flvjs err ignore', error)
          }
        }
      }
    },
    destroyPlayer() {
      // Destroy
      if (this.flvPlayer) {
        this.flvPlayer.pause()
        this.flvPlayer.destroy()
        this.flvPlayer = null
      }
    }
  }
}
</script>

<style>
.info-stream-class {
  text-align: center;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(245, 245, 220, 0.3);
  font-size: 12px;
  color: #000000;
}

.move-bg {
  height: 100%;
  width: 100%;
  background-color: #000000;
}

.video-container {
  width: 100%;
  height: 100%;
  background-color: #000000;
}

.centered-video {
  width: 100%;
  height: 100%;
  margin-left: auto;
  margin-right: auto;
  margin-bottom: auto;
}

.stream-mask {
  display: flex;
  height: calc(100% - 60px) !important;
  width: 100% !important;
  position: absolute;
  top: 0px;
  left: 0px;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
}

.edit-mask-stream {
  opacity: 0;
}

</style>

