<script setup lang="ts">
import { toRefs } from 'vue'
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
  }
})

const { propValue, element } = toRefs(props)
</script>

<template>
  <div class="rect-shape">
    this is test
  </div>
</template>

<style lang="less" scoped>
.rect-shape {
  width: 100%;
  height: 100%;
  overflow: auto;
}
</style>
