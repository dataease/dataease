<template>
  <el-row ref="mainPlayer" style="width: 100%; height: 100%">
    <div
      v-if="element.streamMediaLinks[element.streamMediaLinks.videoType].url"
      class="video-container"
    >
      <video
        :ref="'player-' + element.id"
        class="centered-video"
        name="centeredVideo"
        :loop="state.pOption.loop"
        :controls="inScreen"
        muted
      />
    </div>
    <div v-else class="info-stream-class">
      <span>{{ t('visualization.stream_media_add_tips') }}</span>
    </div>
  </el-row>
</template>
<script lang="ts" setup>
import flvjs from 'flv.js'
import '@/style/custom-theme.css'
import { onMounted, reactive, toRefs, getCurrentInstance, nextTick, onBeforeUnmount } from 'vue'
import { useEmitt } from '@/hooks/web/useEmitt'
import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n()

const props = defineProps({
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
})

const { propValue, element, editMode } = toRefs(props)
let currentInstance

const state = reactive({
  pOption: element.value.streamMediaLinks[element.value.streamMediaLinks.videoType],
  flvPlayer: null,
  videoShow: true
})

onMounted(() => {
  currentInstance = getCurrentInstance()
  useEmitt({
    name: 'streamMediaLinksChange-' + element.value.id,
    callback: function () {
      initOption()
    }
  })
  initOption()
})

const initOption = () => {
  state.pOption = element.value.streamMediaLinks[element.value.streamMediaLinks.videoType]
  delete state.pOption.segments
  nextTick(() => {
    if (flvjs.isSupported() && state.pOption.url) {
      destroyPlayer()
      const video = currentInstance.proxy.$refs['player-' + element.value.id]
      if (video) {
        try {
          state.flvPlayer = flvjs.createPlayer(state.pOption, {
            enableWorker: false, // 不启用分离线程
            enableStashBuffer: false, // 关闭IO隐藏缓冲区
            isLive: state.pOption.isLive,
            lazyLoad: false
          })
          state.flvPlayer.attachMediaElement(video)
          state.flvPlayer.load()
          state.flvPlayer.play()
        } catch (error) {
          console.error('flvjs err ignore', error)
        }
      }
    }
  })
}

const destroyPlayer = () => {
  // Destroy
  if (state.flvPlayer) {
    state.flvPlayer.pause()
    state.flvPlayer.destroy()
    state.flvPlayer = null
  }
}

onBeforeUnmount(() => {
  destroyPlayer()
})
</script>

<style lang="less" scoped>
.info-stream-class {
  text-align: center;
  height: 100%;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(245, 245, 220, 0.1);
  font-size: 12px;
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
