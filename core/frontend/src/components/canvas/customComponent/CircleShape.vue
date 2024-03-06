<template>
  <div class="circle-shape">
    <v-text
      :prop-value="element.propValue"
      :element="element"
    />
  </div>
</template>

<script>

export default {
  props: {
    propValue: {
      type: String,
      required: true,
      default: ''
    },
    element: {
      type: Object,
      default: () => {}
    }
  }
}
</script>

<style lang="scss" scoped>
.circle-shape {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  overflow: auto;
}
</style>
