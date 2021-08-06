<template>
  <div style="overflow: hidden;width: 100%;height: 100%;">
    <img :src="propValue">
  </div>
</template>

<script>
export default {
  props: {
    // eslint-disable-next-line vue/require-default-prop
    propValue: {
      type: String,
      require: true
    }
  }
}
</script>

<style lang="scss" scoped>
img {
    width: 100%;
    height: 100%;
}
</style>
