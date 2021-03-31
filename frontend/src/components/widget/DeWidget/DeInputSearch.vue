<template>

  <el-input v-if="options!== null && options.attrs!==null" v-model="options.value" style="width:260px;" :placeholder="options.attrs.placeholder">

    <el-button slot="append" icon="el-icon-search" />
  </el-input>

</template>

<script>
export default {

  props: {
    options: {
      type: Object,
      default: null
    }
  },
  data() {
    return {

    }
  },
  mounted() {
    this.$nextTick(() => {

    })
  }
}
</script>

<style lang="scss" scoped>

</style>
