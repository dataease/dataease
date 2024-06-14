<script lang="ts" setup>
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'

const dvMainStore = dvMainStoreWithOut()
import screenfull from 'screenfull'
import { onBeforeUnmount, onMounted } from 'vue'

defineProps({
  themes: {
    type: String,
    default: 'light'
  }
})
const fullscreenChange = () => {
  if (screenfull.isEnabled) {
    dvMainStore.setFullscreenFlag(screenfull.isFullscreen)
  }
}

const toggleFullscreen = () => {
  if (screenfull.isEnabled) {
    const bodyNode = document.querySelector('body')
    //screenfull.toggle 此方法是执行全屏化操作。如果已是全屏状态，则退出全屏
    screenfull.toggle(bodyNode)
  }
}

onMounted(() => {
  if (screenfull.isEnabled) {
    screenfull.on('change', fullscreenChange)
  }
})

onBeforeUnmount(() => {
  screenfull.off('change', fullscreenChange)
})
</script>

<template>
  <el-button secondary @click="toggleFullscreen">
    <template #icon>
      <icon name="icon_pc_fullscreen"></icon>
    </template>
    全屏</el-button
  >
</template>

<style lang="less" scoped></style>
