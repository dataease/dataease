<template>
  <div
    :style="{
      left: start.x + 'px',
      top: start.y + 'px',
      width: width + 'px',
      height: height + 'px',
    }"
    class="area"
  />
</template>

<script>
export default {
  props: {
    start: {
      type: Object,
      default: null
    },
    width: {
      type: Number,
      default: null
    },
    height: {
      type: Number,
      default: null
    }
  }
}
</script>

<style lang="scss" scoped>
.area {
    border: 1px solid #70c0ff;
    position: absolute;
}
</style>
