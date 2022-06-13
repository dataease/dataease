<template>
  <el-date-picker
    class="deDate"
    v-if="element.options!== null && element.options.attrs!==null"
    ref="dateRef"
    v-model="values"
    :type="element.options.attrs.type"
    :range-separator="$t(element.options.attrs.rangeSeparator)"
    :start-placeholder="$t(element.options.attrs.startPlaceholder)"
    :end-placeholder="$t(element.options.attrs.endPlaceholder)"
    :placeholder="$t(element.options.attrs.placeholder)"
    :append-to-body="inScreen"
    value-format="timestamp"
    :size="size"
    :editable="false"
    @change="dateChange"
    @focus="toFocus"
    @blur="onBlur"
  />
</template>

<script>
import {
  ApplicationContext
} from '@/utils/ApplicationContext'
import {
  timeSection
} from '@/utils'
import bus from '@/utils/bus'
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
    },
    size: String
  },
  data() {
    return {
      operator: 'between',
      values: null,
      onFocus: false

    }
  },
  computed: {
    defaultoptions() {
      if (!this.element || !this.element.options || !this.element.options.attrs.default) return ''
      return JSON.stringify(this.element.options.attrs.default)
    },
    defaultValueStr() {
      if (!this.element || !this.element.options || !this.element.options.value) return ''
      return this.element.options.value.toString()
    },
    viewIds() {
      if (!this.element || !this.element.options || !this.element.options.attrs.viewIds) return ''
      return this.element.options.attrs.viewIds.toString()
    },
    manualModify() {
      return !!this.element.options.manualModify
    }
  },
  watch: {
    'viewIds': function(value, old) {
      if (typeof value === 'undefined' || value === old) return
      this.setCondition()
    },
    'defaultValueStr': function(value, old) {
      if (this.element.options.attrs.default.isDynamic) {
        return
      }
      if (value === old) return
      this.values = this.fillValueDerfault()
      this.dateChange(value)
    },
    'defaultoptions': function(val, old) {
      if (!this.element.options.attrs.default.isDynamic) {
        this.values = this.fillValueDerfault()
        this.dateChange(this.values)
        return
      }
      if (val === old) return
      const widget = ApplicationContext.getService(this.element.serviceName)
      this.values = widget.dynamicDateFormNow(this.element)
      this.dateChange(this.values)
    }
  },
  created() {
    if (this.element.options.attrs.default && this.element.options.attrs.default.isDynamic) {
      if (this.element.options.attrs.default) {
        const widget = ApplicationContext.getService(this.element.serviceName)
        this.values = widget.dynamicDateFormNow(this.element)
        this.dateChange(this.values)
      }
      return
    }
    if (this.element.options.value) {
      this.values = this.fillValueDerfault()
      this.dateChange(this.values)
    }
  },
  mounted() {
    bus.$on('onScroll', () => {
      if (this.onFocus) {
        this.$refs.dateRef.hidePicker()
      }
    })
    bus.$on('reset-default-value', id => {
      if (this.inDraw && this.manualModify && this.element.id === id) {
        if (!this.element.options.attrs.default.isDynamic) {
          this.values = this.fillValueDerfault()
          this.dateChange(this.values)
          return
        }
        const widget = ApplicationContext.getService(this.element.serviceName)
        this.values = widget.dynamicDateFormNow(this.element)
        this.dateChange(this.values)
      }
    })
  },
  methods: {
    onBlur() {
      this.onFocus = false
    },
    toFocus() {
      this.onFocus = true
    },
    search() {
      this.setCondition()
    },
    setCondition() {
      const param = {
        component: this.element,
        value: this.formatFilterValue(),
        operator: this.operator
      }
      param.value = this.formatValues(param.value)
      this.inDraw && this.$store.commit('addViewFilter', param)
    },
    dateChange(value) {
      if (!this.inDraw) {
        if (value === null) {
          this.element.options.value = ''
        } else {
          this.element.options.value = Array.isArray(value) ? value.join() : value.toString()
        }
        this.element.options.manualModify = false
      } else {
        this.element.options.manualModify = true
      }
      this.setCondition()
    },
    formatFilterValue() {
      if (this.values === null) return []
      if (Array.isArray(this.values)) return this.values
      return [this.values]
    },
    formatValues(values) {
      if (!values || values.length === 0) {
        return []
      }
      if (this.element.options.attrs.type === 'daterange') {
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
        return timeSection(parseFloat(value), this.element.options.attrs.type)
      }
    },
    fillValueDerfault() {
      const defaultV = this.element.options.value === null ? '' : this.element.options.value.toString()
      if (this.element.options.attrs.type === 'daterange') {
        if (defaultV === null || typeof defaultV === 'undefined' || defaultV === '' || defaultV ===
            '[object Object]') {
          return []
        }
        return defaultV.split(',').map(item => parseFloat(item))
      } else {
        if (defaultV === null || typeof defaultV === 'undefined' || defaultV === '' || defaultV ===
            '[object Object]') {
          return null
        }
        return parseFloat(defaultV.split(',')[0])
      }
    }
  }
}

</script>

<style lang="scss" scoped>
  .deDate ::v-deep .el-input__inner {
    background-color: transparent;
  }

  .deDate ::v-deep .el-range-editor.el-input__inner {
    background-color: transparent;
  }

  .deDate ::v-deep .el-range-editor .el-range-input {
    background-color: transparent;
  }
</style>
