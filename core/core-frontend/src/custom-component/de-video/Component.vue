<template>
  <el-row ref="mainPlayer">
    <div v-if="element.videoLinks[element.videoLinks.videoType].src" class="player">
      <VideoPlayer :src="playerOptions.src" :options="playerOptions" />
    </div>
    <div v-else class="info-class">
      <span>{{ t('visualization.video_add_tips') }}</span>
    </div>
  </el-row>
</template>

<script setup lang="ts">
import { VideoPlayer } from '@videojs-player/vue'
import 'video.js/dist/video-js.css'
import { computed, nextTick, reactive, toRefs, watch, onMounted, ref } from 'vue'
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
  }
})
const state = reactive({
  pOption: {
    height: null
  },
  showVideo: true
})
const { element, h } = toRefs(props)

onMounted(() => {
  useEmitt({
    name: 'videoLinksChange-' + element.value.id,
    callback: function () {
      videoLinksChange()
    }
  })
})

const playerOptions = computed(() => element.value.videoLinks[element.value.videoLinks.videoType])

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
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(255, 255, 255, 0.1);
  font-size: 12px;
}

.player {
  height: 100%;
  width: 100%;
  display: flex;
  align-items: center;
  background-color: #000000;
}

.d-player-state {
  display: none;
}
</style>
