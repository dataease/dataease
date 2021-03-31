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
    options: {
      type: Object,
      default: null
    }
  }
}
</script>

<style lang="scss" scoped>

</style>
