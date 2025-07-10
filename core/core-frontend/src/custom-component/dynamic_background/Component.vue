<template>
  <div class="circle-shape">
    <img class="dynamic-shape" :src="findUrl(element.innerType)" alt="" />
  </div>
</template>

<script setup lang="ts">
defineProps({
  element: {
    type: Object,
    default() {
      return {
        innerType: null
      }
    }
  }
})

const findUrl = name => {
  return new URL(`/src/assets/dynamic-background/${name}`, import.meta.url).href
}
</script>

<style lang="less" scoped>
.dynamic-shape {
  width: 100%;
  height: 100%;
  overflow: auto;
  user-select: none; /* 防止文本选中 */
  -webkit-user-select: none; /* Safari */
  -moz-user-select: none; /* Firefox */
  -ms-user-select: none; /* IE10+/Edge */
  pointer-events: none; /* 防止鼠标事件 */
}
</style>
