<template>
  <div>
    <el-switch v-model="value" active-text="当前用户" />
  </div>
</template>

<script>
import { ComplexCondition } from 'fit2cloud-ui/src/components/search-bar/model'

export default {
  name: 'CustomCondition',
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
      value: undefined,
      operator: 'isCurrentUser',
      operatorLabel: '是当前用户'
    }
  },
  computed: {
    valueLabel() {
      return this.value ? '是' : '否'
    }
  },
  // 自定义搜索控件的2个方法
  methods: {
    init() { // 初始化方法，在打开复合搜索界面时会被调用
      // 例如清空之前填写的内容
      this.value = undefined
    },
    getCondition() { // 点击确认时调用
      const { field, label, operator, operatorLabel, value, valueLabel } = this
      // 必须返回ComplexCondition类型的对象
      return new ComplexCondition({ field, label, operator, operatorLabel, value, valueLabel })
    }
  }
}
</script>

<style scoped>

</style>
