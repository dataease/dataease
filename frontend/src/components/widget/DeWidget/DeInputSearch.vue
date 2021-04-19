<template>

  <el-input v-if="options!== null && options.attrs!==null" v-model="options.value" style="width: 260px" :placeholder="options.attrs.placeholder">

    <el-button slot="append" icon="el-icon-search" />
  </el-input>

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
