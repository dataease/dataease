<template>
  <div class="fu-operator-component">
    <div :class="['fu-operator-component__label', 'fu-operator-component__label--' + configSize]">
      {{ label }}
    </div>
    <div class="fu-operator-component__operator">
      <el-select
        v-model="value"
        :disabled="disabled"
        class="search-operator"
        :placeholder="$t('fu.search_bar.please_select')"
        :size="configSize"
        @change="change"
        @input="change"
      >
        <el-option
          v-for="o in operators"
          :key="o.value"
          :label="$t(o.label)"
          :value="o.value"
        />
      </el-select>
    </div>
    <div class="fu-operator-component__value">
      <slot />
    </div>
  </div>
</template>

<script>
import Cookies from 'js-cookie'
export default {
  name: 'DeComplexOperator',
  model: {
    prop: 'operator',
    event: 'change'
  },
  props: {
    // eslint-disable-next-line vue/require-default-prop
    label: String,
    // eslint-disable-next-line vue/require-default-prop
    operator: String,
    // eslint-disable-next-line vue/require-default-prop
    operators: Array,
    disabled: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      value: this.operator
    }
  },
  computed: {
    configSize() {
      return Cookies.get('size') || 'medium'
    }
  },
  watch: {
    operator: function(v) {
      this.value = v
    }
  },
  methods: {
    change(value) {
      this.$emit('change', value)
    }
  }
}
</script>

<style scoped>

</style>
