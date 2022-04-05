<template>
  <el-row ref="mainPlayer">
    <div v-if="element.videoLinks[element.videoLinks.videoType].sources[0].src" class="player">
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
    <div v-else class="info-class">
      {{ $t('panel.video_add_tips') }}
    </div>
  </el-row>
</template>

<script>
// custom skin css
import '@/custom-theme.css'
import { mapState } from 'vuex'
import bus from '@/utils/bus'
// import SWF_URL from 'videojs-swf/dist/video-js.swf'

export default {
  props: {
    // eslint-disable-next-line vue/require-default-prop
    propValue: {
      type: String,
      require: true
    },
    // eslint-disable-next-line vue/require-default-prop
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
    bus.$on('videoLinksChange-' + this.element.id, () => {
      this.showVideo = false
      this.$nextTick(() => {
        this.showVideo = true
        this.initOption()
      })
    })
  },
  methods: {
    initOption() {
      this.pOption = this.element.videoLinks[this.element.videoLinks.videoType]
      this.pOption.height = this.h - (this.curGap * 2)
    },
    // listen event
    onPlayerPlay(player) {
      // console.log('player play!', player)
    },
    onPlayerEnded(player) {
      // console.log('player ended!', player)
    },
    onPlayerLoadeddata(player) {
      // console.log('player Loadeddata!', player)
    },
    onPlayerWaiting(player) {
      // console.log('player Waiting!', player)
    },
    onPlayerPlaying(player) {
      // console.log('player Playing!', player)
    },
    onPlayerTimeupdate(player) {
      // console.log('player Timeupdate!', player.currentTime())
    },
    onPlayerCanplay(player) {
      // console.log('player Canplay!', player)
    },
    onPlayerCanplaythrough(player) {
      // console.log('player Ca
      // console.log('example 01nplaythrough!', player)
    },

    // or listen state event
    playerStateChanged(playerCurrentState) {
      // console.log('player current update state', playerCurrentState)
    },

    // player is ready
    playerReadied(player) {
      // seek to 10s
      // console.log('example player 1 readied', player)
      // player.currentTime(10): the player is readied', player)
    }
  }
}
</script>

<style>
  .info-class{
    text-align: center;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #FFFFFF;
    font-size: 12px;
    color: #9ea6b2;
  }
  .move-bg {
    height: 100%;
    width: 100%;
    background-color: #000000;
  }
</style>

