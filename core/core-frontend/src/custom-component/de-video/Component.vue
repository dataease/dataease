<template>
  <el-row ref="mainPlayer">
    <div v-if="element.videoLinks[element.videoLinks.videoType].sources[0].src" class="player">
      <video-player
        v-if="state.showVideo"
        ref="videoPlayer"
        class="vjs-custom-skin"
        :options="editMode === 'preview' ? state.pOption : playerOptions"
        :playsinline="true"
      />
    </div>
    <div v-else class="info-class">
      <span>{{ $t('panel.link_add_tips_pre') }}</span>
      <span>{{ $t('panel.video_add_tips') }}</span>
    </div>
  </el-row>
</template>

<script setup lang="ts">
import VideoPlayer from 'vue-video-player'
import 'video.js/dist/video-js.css'
import { computed, nextTick, reactive, toRefs, watch } from 'vue'

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
  }
})
const state = reactive({
  pOption: {},
  showVideo: true
})
const { element, h } = toRefs(props)

const moveFlag = computed(
  () => element.value.optStatus.dragging || element.value.optStatus.resizing
)

const playerOptions = computed(() => {
  const videoPlayerOptions = element.value.videoLinks[element.value.videoLinks.videoType]
  videoPlayerOptions.height = h
  return videoPlayerOptions
})

const videoLinksChange = () => {
  state.showVideo = false
  nextTick(() => {
    state.showVideo = true
    initOption()
  })
}

const initOption = () => {
  state.pOption = element.value.videoLinks[element.value.videoLinks.videoType]
  state.pOption.height = h.value
}
watch(
  () => h.value,
  () => {
    initOption()
  }
)
</script>

<style lang="less" scoped>
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
