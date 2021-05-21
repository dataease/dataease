<template>
  <el-date-picker
    v-if="options!== null && options.attrs!==null"
    ref="dateRef"
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
import { dateFormat } from '@/utils'
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
      param.value = this.formatValues(param.value)
      this.inDraw && this.$store.dispatch('conditions/add', param)
    },
    dateChange(value) {
    //   const nvalue = dateFormat(value, this.getFormat())
    //   console.log(nvalue)
      this.setCondition()
    },
    formatValues(values) {
      if (!values || values.length === 0) {
        return []
      }
      return values.map(value => dateFormat(value, this.getFormat()))
    },
    getFormat() {
      let format = 'yyyy'
      switch (this.options.attrs.type) {
        case 'year':
          format = 'yyyy'
          break
        case 'month':
          format = 'yyyy-MM'
          break
        case 'date':
          format = 'yyyy-MM-dd'
          break
        case 'daterange':
          format = 'yyyy-MM-dd'
          this.operator = 'in'
          break
        default:
          format = 'yyyy'
          break
      }
      return format
    }
  }
}
</script>

<style lang="scss" scoped>

</style>
