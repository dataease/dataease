<script setup lang="ts">
import { computed, toRefs } from 'vue'
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
  }
})

const { propValue, element, view, active, searchCount, scale } = toRefs(props)

const autoStyle = computed(() => {
  const curScale = scale.value / 100
  return {
    position: 'absolute',
    height: 100 / curScale + '%!important',
    width: 100 / curScale + '%!important',
    left: 50 * (1 - 1 / curScale) + '%', // 放大余量 除以 2
    top: 50 * (1 - 1 / curScale) + '%', // 放大余量 除以 2
    transform: 'scale(' + curScale + ')'
  }
})
</script>

<template>
  <div class="bash-shape" :style="dvType === 'dataV' ? autoStyle : ''">
    <chart
      :active="active"
      :view="view"
      :element="element"
      :show-position="showPosition"
      :search-count="searchCount"
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
