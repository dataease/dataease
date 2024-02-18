<template>
  <div class="rect-main">
    <svg width="100%" height="100%" xmlns="http://www.w3.org/2000/svg">
      <rect
        width="100%"
        height="100%"
        fill="transparent"
        :stroke="style.color"
        :stroke-width="style.fontSize"
      />
    </svg>
  </div>
</template>

<script setup lang="ts">
import { computed, toRefs } from 'vue'
const props = defineProps({
  element: {
    type: Object,
    default() {
      return {
        style: {}
      }
    }
  }
})

const { element } = toRefs(props)

const style = computed(() => element.value.style)
</script>

<style lang="less" scoped>
.rect-main {
  width: 100%;
  height: 100%;
}
</style>
