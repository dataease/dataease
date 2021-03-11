<template>
  <div>
    <!-- <el-switch v-model="value" active-text="当前用户" /> -->

    <treeselect
      v-model="value"
      :options="options"
      :load-options="loadData"
      style="width: 200px"
    />
  </div>
</template>

<script>
import { ComplexCondition } from 'fit2cloud-ui/src/components/search-bar/model'
import { getDeptTree } from '@/api/system/dept'
import { LOAD_CHILDREN_OPTIONS, LOAD_ROOT_OPTIONS } from '@riophae/vue-treeselect'
import Treeselect from '@riophae/vue-treeselect'
import '@riophae/vue-treeselect/dist/vue-treeselect.css'
export default {
  components: { Treeselect },
  props: {
    // eslint-disable-next-line vue/require-default-prop
    field: String,
    // eslint-disable-next-line vue/require-default-prop
    label: String,
    // eslint-disable-next-line vue/require-default-prop
    defaultOperator: String
    // eslint-disable-next-line vue/require-default-prop
    // options: Array
    // eslint-disable-next-line vue/require-default-prop
    // loadData: Function
  },
  data() {
    return {
      value: undefined,
      operator: 'dept',
      operatorLabel: '组织',
      options: null
    }
  },
  computed: {
    valueLabel() {
      return this.value
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
    },

    // 获取弹窗内部门数据
    loadData({ action, parentNode, callback }) {
      if (action === LOAD_ROOT_OPTIONS) {
        const _self = this
        !this.options && getDeptTree('0').then(res => {
          _self.options = res.data.map(node => _self.normalizer(node))
          callback()
        })
      }

      if (action === LOAD_CHILDREN_OPTIONS) {
        const _self = this
        getDeptTree(parentNode.id).then(res => {
          parentNode.children = res.data.map(function(obj) {
            return _self.normalizer(obj)
          })
          callback()
        })
      }
    },
    normalizer(node) {
      if (node.hasChildren) {
        node.children = null
      }
      return {
        id: node.deptId,
        label: node.name,
        children: node.children
      }
    }
  }
}
</script>

<style scoped>

</style>
