<template>
  <div class="dynamic-shape">
    <component
      :curStyle="curStyle"
      :scale="calScale"
      :is="findDecoration(element.innerType)"
      :color="curColor"
    ></component>
  </div>
</template>

<script setup lang="ts">
import { findDecoration } from '@/custom-component/de-decoration/component_details/config'
import { computed } from 'vue'
const calScale = computed(() => {
  return props.scale
})

const curColor = computed(() => {
  return [props.element.style?.color0 || null, props.element.style?.color1 || null]
})
const props = defineProps({
  curStyle: {
    type: Object
  },
  scale: {
    type: Number
  },
  element: {
    type: Object,
    default() {
      return {
        innerType: null
      }
    }
  }
})
</script>

<style lang="less" scoped>
.dynamic-shape {
  width: 100%;
  height: 100%;
}
</style>
