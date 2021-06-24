<template>

  <el-input
    v-if="options!== null && options.attrs!==null"
    v-model="options.value"
    resize="vertical"
    :placeholder="options.attrs.placeholder"
    @keyup.enter.native="search"
    @dblclick="setEdit"
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
      values: null,
      canEdit: false
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
        value: !this.options.value ? [] : Array.isArray(this.options.value) ? this.options.value : [this.options.value],
        operator: this.operator
      }
      this.inDraw && this.$store.commit('addViewFilter', param)
    },
    setEdit() {
      this.canEdit = true
    }
  }
}
</script>

<style lang="scss" scoped>

</style>
