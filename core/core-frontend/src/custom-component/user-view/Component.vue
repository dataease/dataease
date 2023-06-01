<script setup lang="ts">
import { toRefs } from 'vue'
import Chart from '@/views/chart/components/views/index.vue'

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
  view: {
    type: Object,
    default() {
      return {
        propValue: null
      }
    }
  }
})

const { propValue, element, view } = toRefs(props)
</script>

<template>
  <div class="bash-shape">
    <chart :view="view"></chart>
  </div>
</template>

<style lang="less" scoped>
.bash-shape {
  width: 100%;
  height: 100%;
  overflow: auto;
}
</style>
