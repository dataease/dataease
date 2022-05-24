<template>
  <el-row ref="mainPlayer" style="width: 100%;height: 100%">
    <div v-if="element.streamMediaLinks.videoType == 'flv'">
      <div v-if="element.streamMediaLinks[element.streamMediaLinks.videoType].url" class="video-container">
        <video ref="player" class="centered-video" name="centeredVideo" :loop="pOption.loop" controls muted />
        <div v-if="editMode==='edit'" class="stream-mask" />
      </div>
      <div v-else class="info-stream-class">
        {{ $t('panel.stream_media_add_tips') }}
      </div>
    </div>
    <div v-if="element.streamMediaLinks.videoType == 'hls'">
      <div v-if="element.streamMediaLinks[element.streamMediaLinks.videoType].url" class="video-container">
        <video
          ref="videoPlayer"
          class="video-js vjs-default-skin vjs-big-play-centered"
          :loop="pOption.loop"
          controls
          muted
        />
      </div>
    </div>
  </el-row>
</template>
<script>
import videojs from 'video.js'

import flvjs from 'flv.js'
import '@/custom-theme.css'
import bus from '@/utils/bus'

export default {
  components: {

  },
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
    }
  },
  data() {
    return {
      pOption: this.element.streamMediaLinks[this.element.streamMediaLinks.videoType],
      flvPlayer: null,
      videoShow: true,
      Hlvplayer: null
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
    }
  },
  watch: {
    pOption: {
      handler: function() {
        this.initOption()
      },
      deep: true
    }
  },
  created() {},
  mounted() {
    console.log('DeStreamMedia', this.element.streamMediaLinks.videoType, this.element.streamMediaLinks)
    if (this.element.streamMediaLinks.videoType === 'flv') {
      this.initOption()
      bus.$on('streamMediaLinksChange-' + this.element.id, () => {
        this.pOption = this.element.streamMediaLinks[this.element.streamMediaLinks.videoType],
        this.flvPlayer = null,
        this.videoShow = false
        this.$nextTick(() => {
          this.videoShow = true
          this.initOption()
        })
      })
    }
    if (this.element.streamMediaLinks.videoType === 'hls') {
      this.initOptionHlv()
    }
  },
  methods: {
    initOption() {
      if (flvjs.isSupported() && this.pOption.url) {
        const video = this.$refs.player
        if (video) {
          this.flvPlayer = flvjs.createPlayer(this.pOption)
          this.flvPlayer.attachMediaElement(video)
          try {
            this.flvPlayer.load()
            this.flvPlayer.play()
          } catch (error) {
            console.log(error)
          }
        }
      }
    },
    initOptionHlv() {
      var options = {
        autoplay: 'muted', // 自动播放
        loop: true, // 视频一结束就重新开始
        controls: true,
        muted: true, // 默认情况下将使所有音频静音
        fullscreen: {
          options: {
            navigationUI: 'hide'
          }
        },
        sources: [{
          src: '', // 实测可以
          type: 'application/x-mpegURL'
        }]
      }
      options.sources[0].src = this.pOption.url
      var obj = this.element.streamMediaLinks[this.element.streamMediaLinks.videoType]
      options = Object.assign(options, obj)
      this.Hlvplayer = videojs(this.$refs.videoPlayer, options, function onPlayerReady() {

      })
    },
    beforeDestroy() {
      if (this.Hlvplayer) {
        this.Hlvplayer.dispose()
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
    background-color: beige;
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
    background-color: #5c5e61;
    opacity: 0;
    position: absolute;
    top: 0px;
    left: 0px;
    z-index: 2;
    display: flex;
    align-items: center;
    justify-content: center;
  }
</style>
