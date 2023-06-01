<script setup lang="ts">
import { toRefs } from 'vue'
import ChartComponentG2Plot from '../../views/chart/components/views/ChartComponentG2Plot.vue'
const props = defineProps({
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
  viewInfo: {
    type: Object,
    default() {
      return {
        propValue: null
      }
    }
  }
})

const { propValue, element } = toRefs(props)
</script>

<template>
  <div class="bash-shape">
    <chart-component-g2-plot></chart-component-g2-plot>
  </div>
</template>

<style lang="less" scoped>
.bash-shape {
  width: 100%;
  height: 100%;
  overflow: auto;
}
</style>
