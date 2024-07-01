<script setup lang="ts">
import { toRefs } from 'vue'
import ComponentWrapper from '@/components/data-visualization/canvas/ComponentWrapper.vue'
import { toPercent } from '@/utils/translate'

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
  scale: {
    type: Number,
    required: false,
    default: 1
  },
  canvasViewInfo: {
    type: Object,
    required: true
  }
})

const { propValue, dvInfo, searchCount, scale, canvasViewInfo } = toRefs(props)

const customGroupStyle = item => {
  return {
    width: toPercent(item.groupStyle.width),
    height: toPercent(item.groupStyle.height),
    top: toPercent(item.groupStyle.top),
    left: toPercent(item.groupStyle.left)
  }
}
</script>

<template>
  <div class="group">
    <div>
      <component-wrapper
        v-for="(item, index) in propValue"
        :id="'component' + item.id"
        :view-info="canvasViewInfo[item.id]"
        :key="index"
        :config="item"
        :index="index"
        :dv-info="dvInfo"
        :style="customGroupStyle(item)"
        :show-position="showPosition"
        :search-count="searchCount"
        :scale="scale"
      />
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
