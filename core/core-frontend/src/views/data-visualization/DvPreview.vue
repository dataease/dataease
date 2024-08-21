<script setup lang="ts">
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { computed, ref } from 'vue'
import DePreview from '@/components/data-visualization/canvas/DePreview.vue'
import { storeToRefs } from 'pinia'
import { ElScrollbar } from 'element-plus-secondary'

const dvMainStore = dvMainStoreWithOut()
const { fullscreenFlag } = storeToRefs(dvMainStore)
const dePreviewRef = ref(null)
const dataInitState = ref(true)
const props = defineProps({
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

const contentInnerClass = computed(() => {
  //屏幕适配方式 widthFirst=宽度优先(默认) heightFirst=高度优先 full=铺满全屏 keepSize=不缩放
  if (props.canvasStylePreview.screenAdaptor === 'heightFirst') {
    return 'preview-content-inner-height-first'
  } else if (props.canvasStylePreview.screenAdaptor === 'full') {
    return 'preview-content-inner-full'
  } else {
    return 'preview-content-inner-width-first'
  }
})

const outerStyle = computed(() => {
  return {
    flexDirection: props.canvasStylePreview.screenAdaptor === 'heightFirst' ? 'row' : 'column'
  }
})

defineExpose({
  restore
})
</script>

<template>
  <div
    id="de-preview-content"
    :class="{ 'de-screen-full': fullscreenFlag }"
    :style="outerStyle"
    class="content-outer"
  >
    <div class="content-inner" :class="contentInnerClass">
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
}
</style>
