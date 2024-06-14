<script lang="ts" setup>
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'

const dvMainStore = dvMainStoreWithOut()
import screenfull from 'screenfull'
import { onBeforeUnmount, onMounted, toRefs } from 'vue'

const props = defineProps({
  themes: {
    type: String,
    default: 'light'
  },
  componentType: {
    type: String,
    default: 'button'
  },
  showPosition: {
    required: false,
    type: String,
    default: 'preview'
  }
})
const { themes, componentType } = toRefs(props)

const fullscreenChange = () => {
  if (screenfull.isEnabled) {
    dvMainStore.setFullscreenFlag(screenfull.isFullscreen)
  }
}

const toggleFullscreen = () => {
  if (screenfull.isEnabled) {
    const bodyNode = document.querySelector('body')
    screenfull.toggle(bodyNode)
  }
}

const editToggleFullscreen = () => {
  dvMainStore.setEditMode('preview')
  toggleFullscreen()
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
  <el-button v-if="showPosition === 'preview'" secondary @click="toggleFullscreen">
    <template #icon>
      <icon name="icon_pc_fullscreen"></icon>
    </template>
    全屏</el-button
  >
  <el-dropdown-item v-else @click="editToggleFullscreen()">
    <el-icon style="margin-right: 8px; font-size: 16px">
      <icon name="icon_pc_fullscreen"></icon>
    </el-icon>
    全屏预览
  </el-dropdown-item>
</template>

<style lang="less" scoped></style>
