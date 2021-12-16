<template>

  <el-input
    v-if="element.options!== null && element.options.attrs!==null"
    v-model="value"
    resize="vertical"
    :placeholder="$t(element.options.attrs.placeholder)"
    @input="valueChange"
    @keypress.enter.native="search"
    @dblclick="setEdit"
    :size="size"
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
    },
    size: String
  },
  data() {
    return {
      operator: 'like',
      value: null,
      canEdit: false
    }
  },
  watch: {
    'element.options.value': function(value, old) {
      if (value === old) return
      this.value = value
      this.search()
    }
  },
  created() {
    if (this.element.options.value) {
      this.value = this.element.options.value
      this.search()
    }
  },
  methods: {
    search() {
      if (!this.inDraw) {
        this.element.options.value = this.value
      }
      this.setCondition()
    },
    setCondition() {
      const param = {
        component: this.element,
        value: !this.value ? [] : [this.value],
        operator: this.operator
      }
      this.inDraw && this.$store.commit('addViewFilter', param)
    },
    setEdit() {
      this.canEdit = true
    },
    valueChange(val) {
      if (!this.inDraw) {
        this.element.options.value = val
      }
    }
  }
}
</script>

<style lang="scss" scoped>

</style>
