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
    element: {
      type: Object,
      default: null
    },
    inDraw: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      options: null,
      operator: 'eq',
      values: null
    }
  },
  created() {
    this.options = this.element.options
  }
}
</script>

<style lang="scss" scoped>

</style>
