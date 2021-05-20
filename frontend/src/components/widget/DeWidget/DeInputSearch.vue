<template>

  <el-input
    v-if="options!== null && options.attrs!==null"
    v-model="options.value"
    style="width: 260px"
    :placeholder="options.attrs.placeholder"
    @keyup.enter.native="search"
  >

    <el-button slot="append" icon="el-icon-search" @click="search" />
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
      operator: 'like',
      values: null
    }
  },
  created() {
    this.options = this.element.options
  },
  methods: {
    search() {
    //   this.options.value && this.setCondition()
      this.setCondition()
    },
    setCondition() {
      const param = {
        component: this.element,
        value: [this.options.value],
        operator: this.operator
      }
      this.inDraw && this.$store.dispatch('conditions/add', param)
    }
  }
}
</script>

<style lang="scss" scoped>

</style>
