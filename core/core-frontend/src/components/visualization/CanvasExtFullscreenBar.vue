<template>
  <div
    v-if="showButton && (!dvMainStore.mobileInPc || isMobile())"
    class="bar-main-right"
    @mousedown="handOptBarMousedown"
  >
    <el-button size="mini" type="info" @click="exitFullscreen">
      <el-icon style="margin-right: 8px">
        <Icon name="exit_fullscreen"
          ><exit_fullscreen style="font-size: 16px" class="svg-icon"
        /></Icon>
      </el-icon>
      {{ $t('visualization.ext_fullscreen') }}</el-button
    >
  </div>
</template>

<script lang="ts" setup>
import exit_fullscreen from '@/assets/svg/exit-fullscreen.svg'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { computed } from 'vue'
import { isMainCanvas } from '@/utils/canvasUtils'
import { isMobile } from '@/utils/utils'
import { storeToRefs } from 'pinia'
import Icon from '../icon-custom/src/Icon.vue'
import { ElIcon } from 'element-plus-secondary'
const dvMainStore = dvMainStoreWithOut()

const { fullscreenFlag } = storeToRefs(dvMainStore)

const props = defineProps({
  canvasId: {
    type: String,
    required: false,
    default: 'canvas-main'
  },
  showPosition: {
    required: false,
    type: String,
    default: 'preview'
  }
})

const handOptBarMousedown = e => {
  e.preventDefault()
  e.stopPropagation()
}

const showButton = computed(() => {
  if (isMainCanvas(props.canvasId)) {
    return fullscreenFlag.value && props.showPosition === 'preview'
  } else {
    return false
  }
})

const exitFullscreen = () => {
  document.exitFullscreen()
}
</script>

<style lang="less" scoped>
.bar-main-right {
  top: 2px;
  right: 2px;
  opacity: 0.8;
  z-index: 1;
  position: absolute;
}

.bar-main-edit-right {
  top: 8px;
  right: 102px !important;
}

.bar-main-left {
  left: 0px;
  opacity: 0;
  height: fit-content;
  &:hover {
    opacity: 0.8;
  }
}
</style>
