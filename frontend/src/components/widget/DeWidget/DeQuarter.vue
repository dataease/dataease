<template>
  <el-quarter
    v-if="options!== null && options.attrs!==null"
    v-model="options.value"

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
