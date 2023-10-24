<script setup lang="ts">
import { CSSProperties, computed, toRefs, PropType } from 'vue'
import Chart from '@/views/chart/components/views/index.vue'

const props = defineProps({
  active: {
    type: Boolean,
    default: false
  },
  propValue: {
    type: String,
    required: true,
    default: ''
  },
  element: {
    type: Object,
    default() {
      return {
        propValue: null
      }
    }
  },
  view: {
    type: Object as PropType<ChartObj>,
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
  // 仪表板刷新计时器
  searchCount: {
    type: Number,
    required: false,
    default: 0
  },
  scale: {
    type: Number,
    required: false,
    default: 100
  },
  dvType: {
    type: String,
    required: false,
    default: 'dashboard'
  },
  disabled: {
    type: Boolean,
    required: false,
    default: false
  }
})

const { element, view, active, searchCount, scale } = toRefs(props)
const autoStyle = computed(() => {
  if (element.value.innerType === 'richText') {
    const curScale = scale.value / 100
    return {
      position: 'absolute',
      height: 100 / curScale + '%!important',
      width: 100 / curScale + '%!important',
      left: 50 * (1 - 1 / curScale) + '%', // 放大余量 除以 2
      top: 50 * (1 - 1 / curScale) + '%', // 放大余量 除以 2
      transform: 'scale(' + curScale + ')'
    } as CSSProperties
  } else {
    return {}
  }
})
</script>

<template>
  <div class="bash-shape" :style="autoStyle">
    <chart
      :active="active"
      :view="view"
      :element="element"
      :show-position="showPosition"
      :search-count="searchCount"
      :disabled="disabled"
    ></chart>
  </div>
</template>

<style lang="less" scoped>
.bash-shape {
  width: 100%;
  height: 100%;
  transform: translate(0);
}
</style>
