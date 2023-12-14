<script setup lang="ts">
import { toRefs } from 'vue'
import DePreview from '@/components/data-visualization/canvas/DePreview.vue'
import CanvasCore from '@/components/data-visualization/canvas/CanvasCore.vue'

const props = defineProps({
  canvasStyleData: {
    type: Object,
    required: true
  },
  componentData: {
    type: Array,
    required: true
  },
  canvasViewInfo: {
    type: Object,
    required: true
  },
  dvInfo: {
    type: Object,
    required: true
  },
  element: {
    type: Object,
    default() {
      return {
        propValue: []
      }
    }
  },
  isEdit: {
    type: Boolean,
    default: false
  },
  showPosition: {
    type: String,
    required: false,
    default: 'canvas'
  },
  canvasId: {
    type: String,
    required: true
  },
  // 仪表板刷新计时器
  searchCount: {
    type: Number,
    required: false,
    default: 0
  }
})
const { element, isEdit, showPosition, canvasStyleData, canvasViewInfo, dvInfo, componentData } =
  toRefs(props)
</script>

<template>
  <canvas-core
    v-if="isEdit"
    class="canvas-area-shadow"
    ref="canvasGroup"
    :component-data="componentData"
    :canvas-style-data="canvasStyleData"
    :canvas-view-info="canvasViewInfo"
    :canvas-id="canvasId"
  ></canvas-core>
  <de-preview
    v-else
    :ref="'dashboardPreview'"
    :dv-info="dvInfo"
    :component-data="componentData"
    :canvas-style-data="canvasStyleData"
    :canvas-view-info="canvasViewInfo"
    :canvas-id="canvasId"
    :show-position="showPosition"
  ></de-preview>
</template>

<style lang="less" scoped></style>
