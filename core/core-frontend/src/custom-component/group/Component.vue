<script setup lang="ts">
import { toRefs } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import CanvasGroup from '@/custom-component/common/CanvasGroup.vue'
const dvMainStore = dvMainStoreWithOut()
const { canvasViewInfo, canvasStyleData } = storeToRefs(dvMainStore)

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
  }
})

const { propValue, dvInfo, searchCount } = toRefs(props)
</script>

<template>
  <div class="group">
    <div>
      <canvas-group
        :component-data="propValue"
        :dv-info="dvInfo"
        :show-position="showPosition"
        :canvas-id="'group-' + element.id"
        :canvas-style-data="canvasStyleData"
        :canvas-view-info="canvasViewInfo"
        :is-edit="isEdit"
        :element="element"
      >
      </canvas-group>
    </div>
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
