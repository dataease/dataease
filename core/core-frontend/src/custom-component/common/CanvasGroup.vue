<script setup lang="ts">
import { toRefs } from 'vue'
import CanvasCore from '@/components/data-visualization/canvas/CanvasCore.vue'
import GroupPreview from '@/custom-component/group/GroupPreview.vue'

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
  },
  scale: {
    type: Number,
    required: false,
    default: 1
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
    :canvas-active="element['canvasActive']"
  ></canvas-core>
  <group-preview
    v-else
    :ref="'dashboardPreview'"
    :dv-info="dvInfo"
    :element="element"
    :search-count="searchCount"
    :prop-value="element.propValue"
    :show-position="showPosition"
    :scale="scale * 100"
    :canvas-view-info="canvasViewInfo"
  ></group-preview>
</template>

<style lang="less" scoped></style>
