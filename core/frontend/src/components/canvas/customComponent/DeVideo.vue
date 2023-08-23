<template>
  <el-row ref="mainPlayer">
    <div
      v-if="element.videoLinks[element.videoLinks.videoType].sources[0].src"
      class="player"
    >
      <video-player
        v-if="showVideo"
        ref="videoPlayer"
        class="vjs-custom-skin"
        :options="editMode==='preview'?pOption:playerOptions"
        :playsinline="true"
        @play="onPlayerPlay($event)"
        @ended="onPlayerEnded($event)"
        @loadeddata="onPlayerLoadeddata($event)"
        @waiting="onPlayerWaiting($event)"
        @playing="onPlayerPlaying($event)"
        @timeupdate="onPlayerTimeupdate($event)"
        @canplay="onPlayerCanplay($event)"
        @canplaythrough="onPlayerCanplaythrough($event)"
        @ready="playerReadied"
        @statechanged="playerStateChanged($event)"
      />
    </div>
    <div
      v-else
      class="info-class"
    >
      <span>{{ $t('panel.link_add_tips_pre') }}</span>
      <i class="icon iconfont icon-chaolianjie"/>
      <span>{{ $t('panel.video_add_tips') }}</span>
    </div>
  </el-row>
</template>

<script>
import '@/custom-theme.css'
import { mapState } from 'vuex'
import bus from '@/utils/bus'

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
    }
  },
  data() {
    return {
      pOption: {},
      showVideo: true
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
    playerOptions() {
      const videoPlayerOptions = this.element.videoLinks[this.element.videoLinks.videoType]
      videoPlayerOptions.height = this.h - (this.curGap * 2)
      return videoPlayerOptions
    },
    ...mapState([
      'componentGap',
      'canvasStyleData'
    ])
  },
  watch: {
    h(newVal, oldVla) {
      this.initOption()
    }
  },
  created() {
    this.initOption()
  },
  mounted() {
    bus.$on('videoLinksChange-' + this.element.id, this.videoLinksChange)
  },
  beforeDestroy() {
    bus.$off('videoLinksChange-' + this.element.id, this.videoLinksChange)
  },
  methods: {
    videoLinksChange() {
      this.showVideo = false
      this.$nextTick(() => {
        this.showVideo = true
        this.initOption()
      })
    },
    initOption() {
      this.pOption = this.element.videoLinks[this.element.videoLinks.videoType]
      this.pOption.height = this.h - (this.curGap * 2)
    },
    // listen event
    onPlayerPlay(player) {
    },
    onPlayerEnded(player) {
    },
    onPlayerLoadeddata(player) {
    },
    onPlayerWaiting(player) {
    },
    onPlayerPlaying(player) {
    },
    onPlayerTimeupdate(player) {
    },
    onPlayerCanplay(player) {
    },
    onPlayerCanplaythrough(player) {
    },
    playerStateChanged(playerCurrentState) {
    },
    playerReadied(player) {
    }
  }
}
</script>

<style>
.info-class {
  text-align: center;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(255, 255, 255, 0.3);
  font-size: 12px;
  color: #9ea6b2;
}

.move-bg {
  height: 100%;
  width: 100%;
  background-color: #000000;
}
</style>

