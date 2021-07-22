<template>

  <el-input
    v-if="options!== null && options.attrs!==null"
    v-model="values"
    resize="vertical"
    :placeholder="$t(options.attrs.placeholder)"
    @keypress.enter.native="search"
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
        value: !this.values ? [] : Array.isArray(this.values) ? this.values : [this.values],
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
