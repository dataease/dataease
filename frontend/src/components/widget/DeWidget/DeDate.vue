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
import { timeSection } from '@/utils'
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
      operator: 'between',
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
      this.setCondition()
    },
    formatValues(values) {
      if (!values || values.length === 0) {
        return []
      }
      if (this.options.attrs.type === 'daterange') {
        if (values.length !== 2) {
          return null
        }
        let start = values[0]
        let end = values[1]
        start = timeSection(start, 'date')[0]
        end = timeSection(end, 'date')[1]
        const results = [start, end]
        return results
      } else {
        const value = values[0]
        return timeSection(value, this.options.attrs.type)
      }
    }
  }
}
</script>

<style lang="scss" scoped>

</style>
