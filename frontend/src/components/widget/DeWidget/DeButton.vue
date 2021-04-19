<template>

  <el-button
    v-if="options!== null && options.attrs!==null"
    :type="options.attrs.type"
    :round="options.attrs.round"
  >
    {{ options.value }}
  </el-button>
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
