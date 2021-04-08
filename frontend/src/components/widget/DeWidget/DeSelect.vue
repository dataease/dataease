<template>

  <el-select v-if="options!== null && options.attrs!==null" v-model="options.value" :placeholder="options.attrs.placeholder" @change="changeValue">
    <el-option
      v-for="item in options.attrs.datas"
      :key="item[options.attrs.key]"
      :label="item[options.attrs.label]"
      :value="item[options.attrs.value]"
    />
  </el-select>

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
  },
  methods: {
    changeValue(value) {
      this.$emit('value-change', value)
    }
  }
}
</script>

<style lang="scss" scoped>

</style>
