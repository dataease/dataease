<template>
  <el-dialog
    ref="previewPopDialog"
    modal-class="preview_pop_custom"
    :append-to-body="true"
    :fullscreen="state.fullscreen"
    v-model="state.dialogShow"
    :style="dialogStyle"
    :modal="false"
    :width="state.width"
  >
    <div v-if="state.url" class="preview-main-frame-outer">
      <iframe
        v-if="state.dialogShow"
        class="preview-main-frame"
        id="iframe-de-preview-pop"
        :src="state.url"
        scrolling="auto"
        frameborder="0"
      />
    </div>
  </el-dialog>
  <XpackComponent ref="xpackIframe" jsname="L2NvbXBvbmVudC9lbWJlZGRlZC1pZnJhbWUvSWZyYW1lU2VsZg==" />
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useEmbedded } from '@/store/modules/embedded'
import { XpackComponent } from '@/components/plugin'
const state = reactive({
  dialogShow: false,
  name: '',
  fullscreen: false,
  url: '',
  width: '70vw',
  height: '70%'
})
const xpackIframe = ref()
const embeddedStore = useEmbedded()
const dialogStyle = computed(() => {
  if (state.fullscreen) {
    return {}
  } else {
    return { height: state.height }
  }
})

const previewInit = params => {
  if (params.url.includes('?')) {
    state.url = `${params.url}&popWindow=true`
  } else {
    state.url = `${params.url}?popWindow=true`
  }
  if (params.size === 'large') {
    state.fullscreen = true
  } else if (params.size === 'middle') {
    state.fullscreen = false
    state.width = '80vw'
    state.height = '80%'
  } else {
    state.fullscreen = false
    state.width = '65vw'
    state.height = '65%'
  }
  state.dialogShow = true
  if (embeddedStore.getToken && state.url.includes('#/preview?dvId=')) {
    if (xpackIframe?.value) {
      const pm = {
        methodName: 'iframeInit',
        args: null
      }
      xpackIframe.value.invokeMethod(pm)
    }
  }
}

defineExpose({
  previewInit
})
</script>

<style lang="less">
.preview_pop_custom {
  overflow: hidden;
  .preview-main-frame-outer {
    width: 100%;
    height: 100%;
    .preview-main-frame {
      width: 100%;
      height: 100%;
    }
  }
  .ed-dialog__body {
    height: calc(100% - 42px);
    padding: 0;
  }
  .ed-dialog__header {
    height: 36px;
    .ed-dialog__headerbtn {
      top: 4px !important;
      right: 8px !important;
    }
  }
}
</style>
