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
    //   this.defaultOptions = Object.assign({}, this.options)
    //   const dom = this.$refs[this.options.refId]
    //   for (const key in this.options.attrs) {
    //     if (Object.hasOwnProperty.call(this.defaultOptions.attrs, key)) {
    //       const element = this.defaultOptions.attrs[key]
    //       dom.$props[key] = element
    //     }
    //   }
    })
  }
}
</script>

<style lang="scss" scoped>

</style>
