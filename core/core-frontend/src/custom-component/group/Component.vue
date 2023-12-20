<script setup lang="ts">
import { toRefs, computed } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import CanvasGroup from '@/custom-component/common/CanvasGroup.vue'
import { deepCopy } from '@/utils/utils'
import { DEFAULT_CANVAS_STYLE_DATA_DARK } from '@/views/chart/components/editor/util/dataVisualiztion'
const dvMainStore = dvMainStoreWithOut()
const { canvasViewInfo, canvasStyleData } = storeToRefs(dvMainStore)
const sourceCanvasStyle = deepCopy(DEFAULT_CANVAS_STYLE_DATA_DARK)

const props = defineProps({
  propValue: {
    type: Array,
    default: () => []
  },
  element: {
    type: Object,
    default() {
      return {
        propValue: null
      }
    }
  },
  showPosition: {
    type: String,
    required: false,
    default: 'canvas'
  },
  dvInfo: {
    type: Object,
    required: true
  },
  // 仪表板刷新计时器
  searchCount: {
    type: Number,
    required: false,
    default: 0
  },
  isEdit: {
    type: Boolean,
    required: false,
    default: false
  },
  scale: {
    type: Number,
    required: false,
    default: 1
  }
})

const { propValue, dvInfo, searchCount, element, scale } = toRefs(props)
const customCanvasStyle = computed(() => {
  const result = sourceCanvasStyle
  result.scale = canvasStyleData.value.scale
  result.width = (element.value.style.width * 100) / result.scale
  result.height = (element.value.style.height * 100) / result.scale
  return result
})
</script>

<template>
  <div class="group">
    <canvas-group
      :component-data="propValue"
      :dv-info="dvInfo"
      :show-position="showPosition"
      :canvas-id="'Group-' + element.id"
      :canvas-style-data="customCanvasStyle"
      :canvas-view-info="canvasViewInfo"
      :is-edit="isEdit"
      :element="element"
    >
    </canvas-group>
  </div>
</template>

<style lang="less" scoped>
.group {
  & > div {
    position: relative;
    width: 100%;
    height: 100%;

    .component {
      position: absolute;
    }
  }
}
</style>
