<template>
  <el-date-picker
    v-if="options!== null && options.attrs!==null"
    v-model="options.value"
    :type="options.attrs.type"
    :range-separator="options.attrs.rangeSeparator"
    :start-placeholder="options.attrs.startPlaceholder"
    :end-placeholder="options.attrs.endPlaceholder"
    :placeholder="options.attrs.placeholder"
    @change="dateChange"
  />
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
  },
  methods: {
    search() {
      this.setCondition()
    },
    setCondition() {
      const param = {
        component: this.element,
        value: Array.isArray(this.options.value) ? this.options.value : [this.options.value],
        operator: this.operator
      }
      this.inDraw && this.$store.dispatch('conditions/add', param)
    },
    dateChange(value) {
      this.setCondition()
    }
  }
}
</script>

<style lang="scss" scoped>

</style>
