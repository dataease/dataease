<template>
  <de-complex-operator
    v-model="operator"
    :label="label"
    :operators="operators"
    :size="configSize"
  >
    <el-input
      v-model="value"
      :placeholder="$t('fu.search_bar.please_input')"
      :size="configSize"
      v-bind="$attrs"
    />
  </de-complex-operator>
</template>

<script>
import { ComplexCondition } from 'fit2cloud-ui/src/components/search-bar/model'
// import mixins from 'fit2cloud-ui/src/components/search-bar/complex-components/mixins'
import DeComplexOperator from './DeComplexOperator.vue'
import Cookies from 'js-cookie'
export default {
  name: 'DeComplexInput',
  components: { DeComplexOperator },
  //   mixins: [mixins],
  props: {
    // eslint-disable-next-line vue/require-default-prop
    field: String,
    // eslint-disable-next-line vue/require-default-prop
    label: String,
    defaultOperator: {
      type: String,
      default: 'like'
    }
  },
  data() {
    return {
      operator: this.defaultOperator,
      value: '',
      operators: [{
        label: 'fu.search_bar.like',
        value: 'like'
      }, {
        label: 'fu.search_bar.eq',
        value: 'eq'
      }]
    }
  },
  computed: {
    configSize() {
      return Cookies.get('size') || 'medium'
    }
  },
  methods: {
    getCondition() {
      if (!this.value) return
      const { field, label, operator, operatorLabel, value } = this
      return new ComplexCondition({ field, label, operator, operatorLabel, value })
    },
    init() {
      this.value = ''
    }
  }
}
</script>
