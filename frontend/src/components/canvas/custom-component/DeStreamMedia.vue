<template>
  <el-row ref="mainPlayer" style="width: 100%;height: 100%">
    <div v-if="element.streamMediaLinks.videoType == 'flv'">
      <div v-if="element.streamMediaLinks[element.streamMediaLinks.videoType].url" class="video-container">
        <video
          :id="myPlayer[3]"
          :ref="myPlayer[3]"
          class="centered-video"
          name="centeredVideo"
          :loop="pOption.loop"
          controls
          muted
        />
        <div v-if="editMode==='edit'" class="stream-mask" />
      </div>
      <div v-else class="info-stream-class">
        {{ $t('panel.stream_media_add_tips') }}
      </div>
    </div>
    <div v-if="element.streamMediaLinks.videoType == 'hls'">
      <div v-if="element.streamMediaLinks[element.streamMediaLinks.videoType].url" class="video-container">
        <video
          :id="myPlayer[0]"
          :ref="myPlayer[0]"
          :destroyOnClose="true"
          class="vjs-default-skin vjs-big-play-centered vjs-16-9 video-js"
          :loop="pOption.loop"
          :autoplay="pOption.autoplay"
          controls
          preload="auto"
        >
          <!-- <source src="http://ivi.bupt.edu.cn/hls/cctv1hd.m3u8" type="application/x-mpegURL" /> -->
        </video>
      </div>
    </div>
    <div v-if="element.streamMediaLinks.videoType == 'rtmp'">
      <div
        v-if="element.streamMediaLinks[element.streamMediaLinks.videoType].url"
        class="video-container "
        style="position: relative;"
      >
        <div class="mengban">
          <!--  -->
        </div>
        <video
          :id="myPlayer[1]"
          :ref="myPlayer[1]"
          :destroyOnClose="true"
          class="vjs-default-skin vjs-big-play-centered vjs-16-9 video-js"
          preload="auto"
          muted
        >
          <!-- <source src="http://ivi.bupt.edu.cn/hls/cctv1hd.m3u8" type="application/x-mpegURL" /> -->
        </video>
      </div>
    </div>
    <div v-if="element.streamMediaLinks.videoType == 'webrtc'">
      <video :id="myPlayer[2]" :ref="myPlayer[2]" controls style="width: 100%;height: 100%;object-fit: fill">
        <!--  -->
      </video>
    </div>
    </div>
  </el-row>
</template>
<script>
// RTMP || HLS
import videojs from 'video.js'
import 'video.js/dist/video-js.css'
import 'videojs-contrib-hls'

// FLV
import flvjs from 'flv.js'
import '@/custom-theme.css'
import bus from '@/utils/bus'
export default {
  components: {},
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

      myPlayerFLV: null,
      // hls -- rtmp
      myPlayer: [],
      // hls
      myPlayerHls: null,
      // rtmp
      myPlayerRtmp: null,
      // webrtc
      tt: null,
      heartCheck: null,
      timeoutObj: null,
      serverTimeoutObj: null,
      ws: null, // websocket实例
      wsUrl: '', // websocket连结url
      lockReconnect: false,
      remoteVideo: null,
      peer: null
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
    element: {
      handler: function() {
        var url = this.element.streamMediaLinks[this.element.streamMediaLinks.videoType].url
        if (this.element.streamMediaLinks.videoType === 'flv') {
          console.log('FLV', url !== this.pOption.url)
          if (url !== this.pOption.url) {
            this.pOption = this.element.streamMediaLinks[this.element.streamMediaLinks.videoType]
            console.log('FLV', url !== this.pOption.url)
            this.myPlayerFlv = null
            this.initOption(url, true)
          }
        }
        if (this.element.streamMediaLinks.videoType === 'hls') {
          if (url !== this.pOption.url) {
            this.pOption = this.element.streamMediaLinks[this.element.streamMediaLinks.videoType]
            this.initOptionHls(url, true)
          }
        }
        if (this.element.streamMediaLinks.videoType === 'rtmp') {
          if (url !== this.pOption.url) {
            this.pOption = this.element.streamMediaLinks[this.element.streamMediaLinks.videoType]
            this.initOptionRtmp(url, true)
          }
        }
        if (this.element.streamMediaLinks.videoType === 'webrtc') {
          if (url !== this.pOption.url) {
            this.pOption = this.element.streamMediaLinks[this.element.streamMediaLinks.videoType]
            this.initOptionWeb(url.substring(7))
          }
        }
      },
      deep: true
    }
    // pOption: {
    //   handler: function() {
    //     console.log('1111this.element.streamMediaLinks.videoType', this.element.streamMediaLinks.videoType)
    //     if (this.element.streamMediaLinks.videoType === 'flv') {
    //       this.initOption(this.pOption.url)
    //     }
    //   },
    //   deep: true
    // }
  },
  created() {
    var timestamp = new Date().getTime()
    var myPlayerHls = 'myPlayerHls' + timestamp
    var myPlayerRtmp = 'myPlayerRtmp' + timestamp
    var myPlayerWebrtc = 'myPlayerWebrtc' + timestamp
    var myPlayerFlv = 'myPlayerFlv' + timestamp
    this.myPlayer.push(myPlayerHls)
    this.myPlayer.push(myPlayerRtmp)
    this.myPlayer.push(myPlayerWebrtc)
    this.myPlayer.push(myPlayerFlv)
  },
  mounted() {
    if (this.element.streamMediaLinks.videoType === 'flv') {
      this.initOption(this.pOption.url)
      // bus.$on('streamMediaLinksChange-' + this.element.id, () => {
      //   if (this.pOption.url !== this.element.streamMediaLinks[this.element.streamMediaLinks.videoType].url) {
      //     this.pOption.url = this.element.streamMediaLinks[this.element.streamMediaLinks.videoType].url
      //   }
      //   this.pOption = this.element.streamMediaLinks[this.element.streamMediaLinks.videoType]
      //   this.flvPlayer = null
      //   this.videoShow = false
      //   this.$nextTick(() => {
      //     this.videoShow = true
      //     this.initOption(this.pOption.url, true)
      //   })
      // })
    }
    if (this.element.streamMediaLinks.videoType === 'hls') {
      this.initOptionHls(this.pOption.url)
    }
    if (this.element.streamMediaLinks.videoType === 'rtmp') {
      this.initOptionRtmp(this.pOption.url)
    }
    if (this.element.streamMediaLinks.videoType === 'webrtc') {
      this.initOptionWeb(this.pOption.url.substring(7))
    }
  },
  beforeDestroy() {
    if (this.myPlayerHls) {
      const myPlayerHls = this.$refs[this.myPlayer[0]] // 不能用document 获取节点
      videojs(myPlayerHls).dispose() // 销毁video实例，避免出现节点不存在 但是flash一直在执行，报 this.el.......is not function
    }
    if (this.myPlayerRtmp) {
      const myPlayerRtmp = this.$refs[this.myPlayer[1]] // 不能用document 获取节点
      videojs(myPlayerRtmp).dispose() // 销毁video实例，避免出现节点不存在 但是flash一直在执行，报 this.el.......is not function
    }
    if (this.myPlayerWebrtc) {
      const myPlayerWebrtc = this.$refs[this.myPlayer[2]] // 不能用document 获取节点
      videojs(myPlayerWebrtc).dispose() // 销毁video实例，避免出现节点不存在 但是flash一直在执行，报 this.el.......is not function
    }
    if (this.myPlayerFlv) {
      const myPlayerFlv = this.$refs[this.myPlayer[3]] // 不能用document 获取节点
      videojs(myPlayerFlv).dispose() // 销毁video实例，避免出现节点不存在 但是flash一直在执行，报 this.el.......is not function
    }
  },
  methods: {
    // FLV
    initOption(url, status) {
      this.$nextTick(() => {
        if (flvjs.isSupported() && url) {
          const video = this.$refs[this.myPlayer[3]]
          if (video) {
            if (this.pOption.segments) {
              this.pOption.segments[0].url = this.pOption.url
            }
            this.myPlayerFlv = flvjs.createPlayer(this.pOption)
            this.myPlayerFlv.attachMediaElement(video)
            try {
              this.myPlayerFlv.load()
              this.myPlayerFlv.play()
            } catch (error) {
              console.log(error)
            }
          } else {
            console.log('video失败')
          }
        }
      })
    },
    initOptionHls(url, status) {
      if (status) {
        this.myPlayerHls.reset() // 重置 video
        this.myPlayerHls.src([{
          type: 'application/x-mpegURL',
          src: url
        }])
        this.myPlayerHls.load()
        this.myPlayerHls.play()
        return false
      }
      this.myPlayerHls = videojs(
        this.myPlayer[0], {
          bigPlayButton: false,
          textTrackDisplay: false,
          posterImage: true,
          errorDisplay: false,
          sources: [{
            type: 'application/x-mpegURL',
            src: url
          }]
        },
        function() {
          if (status) {
            this.play()
          }
        }
      )
    },
    initOptionRtmp(url, status) {
      if (status) {
        this.myPlayerRtmp.reset() // 重置 video
        this.myPlayerRtmp.src([{
          type: 'rtmp/flv',
          src: url.substring(7)
        }])
        this.myPlayerRtmp.load()
        this.myPlayerRtmp.play()
        return false
      }
      this.myPlayerRtmp = videojs(this.myPlayer[1], {
        sources: [{
          type: 'rtmp/flv',
          src: this.pOption.url.substring(7)
        }],
        controls: true,
        muted: true,
        autoplay: true,
        preload: 'auto',
        textTrackDisplay: false,
        errorDisplay: false,
        controlBar: false,
        bigPlayButton: false
      })
    },
    initOptionWeb(url, status) {
      if (this.myPlayerWebrtc && status) {
        this.myPlayerWebrtc.destroy()
        this.myPlayerWebrtc = null
        this.initOptionWeb(url)
        return false
      }
      // 获取承载元素dom
      const videoDom = document.getElementById(this.myPlayer[2])
      // 初始化播放器
      this.myPlayerWebrtc = new JSWebrtc.Player(url, {
        video: videoDom,
        autoplay: true,
        onPlay: (obj) => {
          // 监听video元素状态，可播放时进行播放 。 某些情况下  autoplay 会失效
          videoDom.addEventListener('canplay', function(e) {
            videoDom.play()
          })
          console.log(obj, '播放器开始播放！')
        }
      })
      // const that = this
      // if (!this.pOption.url) {
      //   return false
      // }
      // this.wsUrl = this.pOption.url.substring(7)
      // window.clearTimeout(this.timeoutObj)
      // window.clearTimeout(this.serverTimeoutObj)
      // window.clearTimeout(this.tt)
      // this.createWebSocket()

      // this.remoteVideo = document.querySelector('#remote-video')
      // // this.remoteVideo.onloadeddata = () => {
      // //   console.log('播放对方视频')
      // //   this.remoteVideo.play()
      // // }
      // const PeerConnection = window.RTCPeerConnection || window.mozRTCPeerConnection || window
      //   .webkitRTCPeerConnection

      //   !PeerConnection && console.error('浏览器不支持WebRTC！')
      // that.peer = new PeerConnection()
      // that.peer.ontrack = e => {
      //   if (e && e.streams) {
      //     console.log('收到对方音频/视频流数据...', e.streams)
      //     that.remoteVideo.srcObject = e.streams[0]
      //   }
      // }
    },
    // 创建websocket
    createWebSocket() {
      var that = this
      try {
        this.ws = new WebSocket(this.wsUrl)
        console.log('开始连接', this.ws)
        this.initWebScoketFun()
        this.ws.onclose = () => {
          console.log('链接关闭', this.wsUrl)
          this.reconnect(this.wsUrl)
        }
        this.ws.onerror = () => {
          console.log('链接失败', this.wsUrl)
          setTimeout(() => {
            that.reconnect(that.wsUrl)
          }, 10000)
        }
        this.ws.onopen = () => {
          console.log('链接成功', this.wsUrl)
          this.heartCheck.start()
        }
      } catch (e) {
        console.log('链接错误', e)
        this.reconnect(this.wsUrl)
      }
    },
    // websocket消息提醒
    initWebScoketFun() {
      var that = this
      const timeout = 2000
      this.timeoutObj = null
      this.serverTimeoutObj = null
      this.heartCheck = {
        start: () => {
          that.timeoutObj && window.clearTimeout(that.timeoutObj)
          that.serverTimeoutObj &&
              window.clearTimeout(that.serverTimeoutObj)
          that.timeoutObj = setTimeout(() => {
            // 这里发送一个心跳，后端收到后，返回一个心跳消息，
            that.ws.send('1')
          }, timeout)
        }
      }
      // this.ws.onclose = () => {
      //   console.log('链接关闭', this.wsUrl)
      //   this.reconnect(this.wsUrl)
      // }
      // this.ws.onerror = () => {
      //   console.log('链接失败', this.wsUrl)
      //   setTimeout(() => {
      //     that.reconnect(that.wsUrl)
      //   }, that.$global.delay)
      // }
      // this.ws.onopen = () => {
      //   console.log('链接成功', this.wsUrl)
      //   this.heartCheck.start()
      // }
      this.ws.onmessage = (e) => {
        console.log(e, 'webScoket心跳链接')
        const {
          type,
          sdp,
          iceCandidate
        } = JSON.parse(e.data)
        if (type === 'answer') {
          that.peer.setRemoteDescription(new RTCSessionDescription({
            type,
            sdp
          }))
        } else if (type === 'answer_ice') {
          that.peer.addIceCandidate(iceCandidate)
        }
      }
    },
    // 重新链接websocket
    reconnect(url) {
      if (this.lockReconnect) {
        return
      }
      this.lockReconnect = true
      // 没连接上会一直重连，设置延迟避免请求过多
      this.tt && window.clearTimeout(this.tt)
      this.tt = setTimeout(() => {
        this.createWebSocket(url)
        this.lockReconnect = false
      }, 60000)
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

  .mengban {
    position: absolute;
    width: 100%;
    height: 100%;
    z-index: 99999999;
  }
</style>
