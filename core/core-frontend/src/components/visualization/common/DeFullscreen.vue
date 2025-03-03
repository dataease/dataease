<script lang="ts" setup>
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { onBeforeUnmount, onMounted, toRefs } from 'vue'
import { useEmitt } from '@/hooks/web/useEmitt'

const dvMainStore = dvMainStoreWithOut()

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
const { themes } = toRefs(props)

const fullscreenChange = () => {
  const isFullscreen = !!document.fullscreenElement
  dvMainStore.setFullscreenFlag(isFullscreen)

  // 编辑界面使用
  if (props.showPosition === 'edit') {
    dvMainStore.setEditMode(isFullscreen ? 'preview' : 'edit')
  }

  // 大屏编辑使用
  if (props.showPosition === 'dvEdit') {
    useEmitt().emitter.emit('canvasScrollRestore')
  }
}

const toggleFullscreen = () => {
  const bodyNode = document.querySelector('body')
  if (!document.fullscreenElement) {
    bodyNode?.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

onMounted(() => {
  document.addEventListener('fullscreenchange', fullscreenChange)
})

onBeforeUnmount(() => {
  document.removeEventListener('fullscreenchange', fullscreenChange)
})

defineExpose({
  toggleFullscreen
})
</script>

<template><span></span></template>

<style lang="less" scoped></style>
