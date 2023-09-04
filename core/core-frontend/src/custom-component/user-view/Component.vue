<script setup lang="ts">
import { toRefs } from 'vue'
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
  }
})

const { propValue, element, view, active, searchCount } = toRefs(props)
</script>

<template>
  <div class="bash-shape">
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
  overflow: auto;
}
</style>
