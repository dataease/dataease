<script lang="ts" setup>
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'

const dvMainStore = dvMainStoreWithOut()
import screenfull from 'screenfull'
import { onBeforeUnmount, onMounted, toRefs } from 'vue'
import { useEmitt } from '@/hooks/web/useEmitt'

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
    // 编辑界面使用
    if (props.showPosition === 'edit') {
      if (screenfull.isFullscreen) {
        dvMainStore.setEditMode('preview')
      } else {
        dvMainStore.setEditMode('edit')
      }
    }
    // 大屏编辑使用
    if (props.showPosition === 'dvEdit') {
      useEmitt().emitter.emit('canvasScrollRestore')
    }
  }
}

const toggleFullscreen = () => {
  if (screenfull.isEnabled) {
    const bodyNode = document.querySelector('body')
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

defineExpose({
  toggleFullscreen
})
</script>

<template><span></span></template>

<style lang="less" scoped></style>
