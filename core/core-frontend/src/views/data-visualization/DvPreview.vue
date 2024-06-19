<script setup lang="ts">
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { ref } from 'vue'
import DePreview from '@/components/data-visualization/canvas/DePreview.vue'
import { storeToRefs } from 'pinia'

const dvMainStore = dvMainStoreWithOut()
const { fullscreenFlag } = storeToRefs(dvMainStore)
const dePreviewRef = ref(null)
const dataInitState = ref(true)
defineProps({
  canvasStylePreview: {
    required: true,
    type: Object
  },
  canvasDataPreview: {
    required: true,
    type: Object
  },
  canvasViewInfoPreview: {
    required: true,
    type: Object
  },
  dvInfo: {
    required: true,
    type: Object
  },
  curPreviewGap: {
    required: false,
    type: Number,
    default: 0
  },
  showPosition: {
    required: false,
    type: String,
    default: 'preview'
  },
  downloadStatus: {
    required: false,
    type: Boolean,
    default: false
  }
})

const restore = () => {
  dePreviewRef.value.restore()
}

defineExpose({
  restore
})
</script>

<template>
  <div id="de-preview-content" :class="{ 'de-screen-full': fullscreenFlag }" class="content-outer">
    <div class="content-inner">
      <de-preview
        ref="dePreviewRef"
        v-if="canvasStylePreview && dataInitState"
        :component-data="canvasDataPreview"
        :canvas-style-data="canvasStylePreview"
        :canvas-view-info="canvasViewInfoPreview"
        :dv-info="dvInfo"
        :cur-gap="curPreviewGap"
        :show-position="showPosition"
        :download-status="downloadStatus"
      ></de-preview>
    </div>
  </div>
</template>

<style lang="less">
.content-outer {
  width: 100%;
  height: calc(100vh - 112px);
  background: #f5f6f7;
  display: flex;
  overflow-y: auto;
  align-items: center;
  flex-direction: column;
  justify-content: center; /* 上下居中 */
  .content-inner {
    width: 100%;
    height: auto;
    overflow-x: hidden;
    overflow-y: auto;
  }
}
</style>
