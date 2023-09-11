<template>
  <de-complex-operator
    v-model="operator"
    :label="label"
    :operators="operators"
    :size="configSize"
    disabled
  >
    <el-select
      v-model="value"
      class="fu-complex-select"
      :placeholder="$t('fu.search_bar.please_select')"
      :size="configSize"
      clearable
      v-bind="$attrs"
    >
      <el-option
        v-for="o in options"
        :key="o.value"
        :label="o.label"
        :value="o.value"
      />
    </el-select>
  </de-complex-operator>
</template>

<script>
import { ComplexCondition } from 'fit2cloud-ui/src/components/search-bar/model'
import DeComplexOperator from './DeComplexOperator.vue'
import Cookies from 'js-cookie'

const MULTIPLE_OPERATORS = [
  {
    label: 'fu.search_bar.in',
    value: 'in'
  }, {
    label: 'fu.search_bar.not_in',
    value: 'not in'
  }
]
const OPERATORS = [
  {
    label: 'fu.search_bar.eq',
    value: 'eq'
  }, {
    label: 'fu.search_bar.ne',
    value: 'ne'
  }
]

export default {
  name: 'DeComplexSelect',
  components: { DeComplexOperator },
  props: {
    // eslint-disable-next-line vue/require-default-prop
    field: String,
    // eslint-disable-next-line vue/require-default-prop
    label: String,
    // eslint-disable-next-line vue/require-default-prop
    defaultOperator: String,
    // eslint-disable-next-line vue/require-default-prop
    options: Array
  },
  data() {
    return {
      operator: '',
      value: ''
    }
  },
  computed: {
    isMultiple() {
      const { multiple } = this.$attrs
      return multiple !== undefined && multiple !== false
    },
    operators() {
      return this.isMultiple ? MULTIPLE_OPERATORS : OPERATORS
    },
    valueLabel() {
      if (this.isMultiple) {
        const values = []
        this.value.forEach(v => {
          values.push(this.getValueLabel(v))
        })
        return values.join(', ')
      }
      return this.getValueLabel(this.value)
    },
    configSize() {
      return Cookies.get('size') || 'medium'
    }
  },
  methods: {
    getValueLabel(value) {
      for (const o of this.options) {
        if (o.value === value) {
          return o.label
        }
      }
      return value
    },
    getCondition() {
      if (!this.value) return
      const { field, label, operator, operatorLabel, value, valueLabel } = this
      return new ComplexCondition({ field, label, operator, operatorLabel, value, valueLabel })
    },
    init() {
      this.operator = this.defaultOperator || this.operators[0].value
      this.value = ''
    }
  }
}
</script>
