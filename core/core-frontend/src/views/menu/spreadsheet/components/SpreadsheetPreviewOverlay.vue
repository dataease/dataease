<script setup lang="ts">
import type { IWorkbookData } from '@univerjs/core'
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import UniverSheet from './UniverSheet.vue'
import PluginRenderIndicator from './PluginRenderIndicator.vue'

const props = defineProps<{
  modelValue: Partial<IWorkbookData>
  locale?: string
  closeOnFullscreenExit?: boolean
}>()

const emit = defineEmits<{
  (e: 'close'): void
}>()

const shellRef = ref<HTMLElement>()
const univerSheetRef = ref<InstanceType<typeof UniverSheet>>()
const fullscreen = ref(Boolean(document.fullscreenElement))
const sheetVisible = ref(true)

const resizeSheet = async () => {
  await nextTick()
  univerSheetRef.value?.resize()
}

const enterFullscreen = async () => {
  try {
    await shellRef.value?.requestFullscreen()
  } catch (error) {
  }
}

const close = async () => {
  sheetVisible.value = false
  await nextTick()
  emit('close')
}

const handleFullscreenChange = () => {
  const wasFullscreen = fullscreen.value
  const currentFullscreen = Boolean(document.fullscreenElement)
  fullscreen.value = currentFullscreen

  if (props.closeOnFullscreenExit && wasFullscreen && !currentFullscreen) {
    void close()
    return
  }
  void resizeSheet()
}

const handleKeydown = (event: KeyboardEvent) => {
  if (event.key === 'Escape' && !event.defaultPrevented && !fullscreen.value) {
    void close()
  }
}

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
  document.addEventListener('fullscreenchange', handleFullscreenChange)

  // 全屏初始化期间若用户已退出，挂载后立即恢复编辑态。
  if (props.closeOnFullscreenExit && !fullscreen.value) {
    void close()
  }
})

onBeforeUnmount(() => {
  document.removeEventListener('keydown', handleKeydown)
  document.removeEventListener('fullscreenchange', handleFullscreenChange)
})
</script>

<template>
  <div ref="shellRef" class="spreadsheet-preview-overlay">
    <div v-if="!fullscreen" class="spreadsheet-preview-overlay__header">
      <span class="spreadsheet-preview-overlay__title">预览</span>
      <div class="spreadsheet-preview-overlay__actions">
        <el-button @click="enterFullscreen">全屏预览</el-button>
        <el-button @click="close">关闭</el-button>
      </div>
    </div>
    <div class="spreadsheet-preview-overlay__body">
      <UniverSheet
        v-if="sheetVisible"
        ref="univerSheetRef"
        :model-value="props.modelValue"
        :locale="props.locale"
        mode="preview"
      />
      <PluginRenderIndicator />
    </div>
  </div>
</template>

<style scoped lang="less">
.spreadsheet-preview-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  flex-direction: column;
  background: #fff;

  &__header {
    height: 56px;
    padding: 0 20px;
    display: flex;
    align-items: center;
    flex-shrink: 0;
    border-bottom: 1px solid #e5e6e8;
    background: #fff;
  }

  &__title {
    color: #1f2329;
    font-size: 16px;
    font-weight: 500;
  }

  &__actions {
    margin-left: auto;
    display: flex;
    gap: 8px;
  }

  &__body {
    position: relative;
    flex: 1;
    min-height: 0;
    overflow: hidden;
  }
}
</style>
