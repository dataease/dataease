<template>
  <el-date-picker
    v-if="options!== null && options.attrs!==null"
    v-model="options.value"
    :type="options.attrs.type"
    :range-separator="options.attrs.rangeSeparator"
    :start-placeholder="options.attrs.startPlaceholder"
    :end-placeholder="options.attrs.endPlaceholder"
    :placeholder="options.attrs.placeholder"
  />
</template>

<script>
export default {

  props: {
    options: {
      type: Object,
      default: null
    }
  }
}
</script>

<style lang="scss" scoped>

</style>
