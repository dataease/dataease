<template xmlns:el-col="http://www.w3.org/1999/html">
  <el-col>
    <el-row style="margin-top: 5px">
      <el-tree
        ref="templateTree"
        :props="defaultProps"
        :load="loadNodes"
        :data="treeData"
        node-key="deptId"
        :highlight-current="true"
        :default-expanded-keys="expandedKey"
        lazy
        @node-click="nodeClick"
      />
    </el-row>
  </el-col>
</template>

<script>
import { getDeptTree, loadTable } from '@/api/system/dept'
export default {
  name: 'AuthDept',
  components: { },
  props: {
    filterText: {
      type: String,
      required: false,
      default: ''
    },
    activeName: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      treeData: [],
      changeIndex: 0,
      timeMachine: null,
      expandedKey: [],
      defaultCondition: {
        field: 'pid',
        operator: 'eq',
        value: 0
      },
      defaultProps: {
        children: 'children',
        label: 'name',
        id: 'deptId',
        parentId: 'pid',
        isLeaf: 'leaf'
      }
    }
  },
  computed: {
  },
  watch: {
    filterText(val) {
      // 当组件名和 activeName 相等时 才进行查询
      if (this.$options.name === this.activeName) {
        this.destroyTimeMachine()
        this.changeIndex++
        this.filterNode(this.changeIndex)
      }
    }
  },
  methods: {
    loadNodes(node, resolve) {
      if (node.level === 0) {
        getDeptTree(0).then(res => {
          const data = res.data
          resolve(data)
        })
      } else {
        getDeptTree(node.data.deptId).then(res => {
          const data = res.data
          resolve(data)
        })
      }
    },
    filterNode(index) {
      this.timeMachine = setTimeout(() => {
        if (index === this.changeIndex) {
          const condition = {
            field: 'name',
            operator: 'like',
            value: this.filterText
          }
          this.search(condition)
        }
        this.destroyTimeMachine()
      }, 1500)
    },
    nodeClick(data, node) {
      this.$emit('nodeClick', { id: data.deptId, type: 'dept' })
    },
    destroyTimeMachine() {
      this.timeMachine && clearTimeout(this.timeMachine)
      this.timeMachine = null
    },
    search(condition) {
      let param = {}
      if (condition && condition.value) {
        param = { conditions: [condition] }
      } else {
        param = { conditions: [this.defaultCondition] }
      }
      loadTable(param).then(res => {
        this.expandedKey = []
        this.treeData = []
        const data = res.data
        this.treeData = this.buildTree(data)
        console.log('===>' + JSON.stringify(this.treeData))
      })
    },

    buildTree(arrs) {
      const idMapping = arrs.reduce((acc, el, i) => {
        acc[el.deptId] = i
        return acc
      }, {})

      console.log('idMapping>' + JSON.stringify(idMapping))

      const roots = []
      arrs.forEach(el => {
        // 判断根节点
        if (el.pid === null || el.pid === 0) {
          roots.push(el)
          return
        }
        debugger
        // 用映射表找到父元素
        const parentEl = arrs[idMapping[el.pid]]
        console.log('parentEl>' + JSON.stringify(parentEl))
        // 把当前元素添加到父元素的`children`数组中
        parentEl.children = [...(parentEl.children || []), el]
        console.log('parentEl>' + JSON.stringify(parentEl))

        // 设置展开节点 如果没有子节点则不进行展开
        if (parentEl.children.length > 0) {
          this.expandedKey.push(parentEl.deptId)
        }
      })
      return roots
    }
  }
}
</script>

<style scoped>
  .custom-tree-node {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    padding-right: 8px;
  }
</style>
