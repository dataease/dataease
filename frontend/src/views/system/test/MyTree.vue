<!--
 * @Author: dawdler
 * @Date: 2020-12-26 11:52:05
 * @Description: demo
 * @LastModifiedBy: dawdler
-->
<template>
  <el-tree-select
    ref="treeSelect"
    v-model="values"
    popover-class="test-class-wrap"
    :styles="styles"
    :select-params="selectParams"
    :tree-params="treeParams"
    :filter-node-method="_filterFun"
    :tree-render-fun="_renderFun"
    @searchFun="_searchFun"
  />
</template>
<script>
import ElTreeSelect from '@/components/ElTreeSelect'
export default {
  name: 'MyTreeSelect',
  components: { ElTreeSelect },
  props: {
    params: Object,
    isSingle: {
      type: Boolean,
      default() {
        return false
      }
    }
  },
  data() {
    return {
      styles: {
        width: '300px'
      },
      // 单选value为字符串，多选为数组
      values: this.isSingle ? '' : [],
      selectParams: {
        clearable: true,
        placeholder: '请输入内容'
      },
      treeParams: {
        clickParent: false,
        filterable: true,
        // 只想要子节点，不需要父节点
        leafOnly: false,
        includeHalfChecked: false,
        'check-strictly': false,
        'default-expand-all': true,
        'expand-on-click-node': false,
        'render-content': this._renderFun,
        data: [],
        props: {
          children: 'children',
          label: 'name',
          rootId: '0',
          disabled: 'disabled',
          parentId: 'parentId',
          value: 'id'
        },
        ...this.params
      }
    }
  },
  watch: {},
  created() {},
  mounted() {
    // 手动更新树数据
    const data = []
    const { label, children, parentId, value, rootId } = this.treeParams.props
    for (let i = 0; i < 5; i++) {
      const rootNode = {
        [label]: `节点${i}`,
        [parentId]: rootId,
        [value]: i,
        [children]: []
      }
      for (let a = 0; a < 5; a++) {
        const subId = `${rootNode[value]}_${a}`
        const subNode = {
          [label]: `子节点${subId}`,
          [parentId]: rootNode[value],
          [value]: subId,
          [children]: []
        }
        for (let b = 0; b < 5; b++) {
          const endId = `${subId}_${b}`
          const endNode = {
            [label]: `末级节点${endId}`,
            [parentId]: subNode[value],
            [value]: endId,
            [children]: []
          }
          subNode[children].push(endNode)
        }
        rootNode[children].push(subNode)
      }
      data.push(rootNode)
    }
    const myNode = {
      [label]: '测试超长节点啊啊啊测试超长节点啊啊啊测试超长节点啊啊啊测试超长节点啊啊啊测试超长节点啊啊啊测试超长节点啊啊啊',
      [parentId]: rootId,
      [value]: 1000,
      [children]: []
    }
    data.push(myNode)
    this.$nextTick(() => {
      this.$refs.treeSelect.treeDataUpdateFun(data)
    })
  },
  methods: {
    _filterFun(value, data, node) {
      if (!value) return true
      return data.id.toString().indexOf(value.toString()) !== -1
    },
    // 树点击
    _nodeClickFun(data, node, vm) {
      console.log('this _nodeClickFun', this.values, data, node)
    },
    // 树过滤
    _searchFun(value) {
      console.log(value, '<--_searchFun')
      // 自行判断 是走后台查询，还是前端过滤
      this.$refs.treeSelect.filterFun(value)
      // 后台查询
      // this.$refs.treeSelect.treeDataUpdateFun(treeData);
    },
    // 自定义render
    _renderFun(h, { node, data, store }) {
      const { props, clickParent } = this.treeParams
      return (
        <span class={['custom-tree-node', !clickParent && data[props.children] && data[props.children].length ? 'disabled' : null]}>
          <span>{node.label}</span>
        </span>
      )
    }
  }
}
</script>
<style lang="less">
.disabled {
    cursor: no-drop;
}
.custom-tree-node {
    width: calc(100% - 40px);
}
</style>
