<template>
  <el-date-picker
    v-if="options!== null && options.attrs!==null"
    ref="dateRef"
    v-model="options.value"
    :type="options.attrs.type"
    :range-separator="$t(options.attrs.rangeSeparator)"
    :start-placeholder="$t(options.attrs.startPlaceholder)"
    :end-placeholder="$t(options.attrs.endPlaceholder)"
    :placeholder="$t(options.attrs.placeholder)"
    :append-to-body="inScreen"
    style="min-height: 36px;"
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
    },
    inScreen: {
      type: Boolean,
      required: false,
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
    if ((this.options.attrs.type === 'date' || this.options.attrs.type === 'daterange') && Array.isArray(this.options.value) && this.options.value.length === 0) {
      this.options.value = null
    }
    if (!!this.options && !!this.options.value && Object.keys(this.options.value).length === 0) {
      this.options.value = null
    }
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
      this.inDraw && this.$store.commit('addViewFilter', param)
    },
    dateChange(value) {
      this.setCondition()
      this.styleChange()
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
    },
    styleChange() {
      this.$store.state.styleChangeTimes++
    }
  }
}
</script>

<style lang="scss" scoped>

</style>
