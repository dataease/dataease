<template>
  <div class="circle-shape"></div>
</template>

<script setup lang="ts">
defineProps({
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
</script>

<style lang="less" scoped>
.circle-shape {
  width: 100%;
  height: 100%;
  border-radius: 50% !important;
  overflow: auto;
}
</style>
