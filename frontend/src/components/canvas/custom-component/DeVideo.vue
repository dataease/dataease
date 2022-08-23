<template>
  <el-row ref="mainPlayer">
    <div v-if="element.videoLinks[element.videoLinks.videoType].sources[0].src" class="player">
      <!-- <video-player
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
      /> -->
      <video
        muted
        class="video-js vjs-default-skin vjs-big-play-centered vjs-16-9"
        autoDisable="false"
        :id="chartId"
        preload="auto"
        playsinline="true"
        width="100%"
        ref="videoRef"
        x5-video-orientation="landscape"
      >
        <source id="sourceBox" :src="pOption.sources[0].src" type="application/x-mpegURL" />
      </video>
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

import videojs from 'video.js'
import 'videojs-contrib-hls'
import '@videojs/http-streaming'

import {
  uuid
} from 'vue-uuid'

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
      showVideo: true,
      chartId: uuid.v1(),
      player: {},
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
      // console.log('video....',this.element)
      this.pOption = this.element.videoLinks[this.element.videoLinks.videoType]
      this.pOption.height = this.h - (this.curGap * 2)
      // this.pOption.sources[0].type = 'application/x-mpegURL'

      this.player = videojs(this.chartId,{
        autoplay: true,
        // 这行是需要的，暂时注释 应该为true 下面这行
        controls: true,
        bigPlayButton: true,
        textTrackDisplay: true,
        posterImage: false,
        errorDisplay: false,
        controlBar: false,
        // playbackRates: [0.5, 1, 1.5, 2],
        ControlBar: {
          children: [
            // { name: "currentTimeDisplay" }, // 当前已播放时间
            // { name: "progressControl" }, // 播放进度条
            // { name: "durationDisplay" }, // 总时间
            // { name: "playToggle" }, // 播放按钮
            // {
            //   name: "volumePanel", // 音量控制
            //   inline: false, // 不使用水平方式
            // },
            { name: "FullscreenToggle" }, // 全屏
          ],
        },
      },
      function onPlayerReady() {
        // setTimeout(() => {
        //   //延时确保能监听到视频源错误
        //   var mediaError = this.error();
        //   if (mediaError != null && mediaError.code) {
        //     _this.isError = true;
        //     Dialog.alert({
        //       message:
        //         "啊哦，播放出错了。<br>请刷新重试，如无法播放建议您观看其它内容。",
        //       confirmButtonText: "确定",
        //     }).then(() => {
        //       _this.goback();
        //     });
        //   }
        // }, 1000);
      })
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
  },
  beforeDestroy() {
    const videoDom = this.$refs.videoRef; //不能用document 获取节点
    videojs(videoDom).dispose(); //销毁video实例，避免出现节点不存在 但是flash一直在执行,也避免重新进入页面video未重新声明
  },

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

