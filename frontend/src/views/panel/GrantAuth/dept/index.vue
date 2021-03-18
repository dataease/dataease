<template>
  <div class="my_table">
    <el-table
      ref="table"
      :data="data"
      lazy
      :show-header="true"
      :load="loadExpandDatas"
      style="width: 100%"
      :row-style="{height: '35px'}"
      :tree-props="{children: 'children', hasChildren: 'hasChildren'}"
      row-key="deptId"
    >
      <el-table-column label="所有组织" prop="name" />
      <el-table-column type="selection" fixd />
      <!-- <el-table-column label="分享给" prop="deptId" width="80" fixed="right">
        <template slot-scope="scope">
          <el-checkbox :v-model="scope.row.deptId===0" />
        </template>
      </el-table-column> -->
    </el-table>
  </div>
</template>

<script>
import { getDeptTree, loadTable } from '@/api/system/dept'
import { saveShare, loadShares } from '@/api/panel/share'
export default {
  name: 'GrantDept',
  props: {
    resourceId: {
      type: String,
      default: null
    },
    keyWord: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      data: [],
      defaultCondition: {
        field: 'pid',
        operator: 'eq',
        value: 0
      },
      type: 2, // 类型2代表组织
      shares: [],
      changeIndex: 0,
      timeMachine: null
    }
  },
  watch: {
    keyWord(v, o) {
      this.destryTimeMachine()
      this.changeIndex++
      this.searchWithKey(this.changeIndex)
    }
  },
  created() {
    this.search()
  },
  methods: {
    // 根据关键字搜索
    // 1500ms内 key不发生变化则执行查询
    searchWithKey(index) {
      this.timeMachine = setTimeout(() => {
        if (index === this.changeIndex) {
          const condition = {
            field: 'name',
            operator: 'like',
            value: this.keyWord
          }
          this.search(condition)
          console.log('start execute search')
        }
        this.destryTimeMachine()
      }, 1500)
    },
    destryTimeMachine() {
      this.timeMachine && clearTimeout(this.timeMachine)
      this.timeMachine = null
    },
    // 加载下一级子节点数据
    loadExpandDatas(row, treeNode, resolve) {
      getDeptTree(row.deptId).then(res => {
        let data = res.data
        data = data.map(obj => {
          if (obj.subCount > 0) {
            obj.hasChildren = true
          }
          return obj
        })
        // this.maps.set(row.deptId, { row, treeNode, resolve })
        resolve && resolve(data)
        this.$nextTick(() => {
          this.setCheckExpandNodes(data)
        })
      })
    },
    // 加载表格数据
    search(condition) {
      // this.setTableAttr()

      this.data = []
      let param = {}
      if (condition && condition.value) {
        param = { conditions: [condition] }
      } else {
        param = { conditions: [this.defaultCondition] }
      }

      loadTable(param).then(res => {
        let data = res.data
        data = data.map(obj => {
          if (obj.subCount > 0) {
            obj.hasChildren = true
          }
          return obj
        })

        if (condition && condition.value) {
          data = data.map(node => {
            delete (node.hasChildren)
            return node
          })
          this.data = this.buildTree(data)
          this.$nextTick(() => {
            data.forEach(node => {
              this.$refs.table.toggleRowExpansion(node, true)
            })
          })
        } else {
          this.data = data
        }

        this.queryShareNodeIds()
      })
    },

    buildTree(arrs) {
      const idMapping = arrs.reduce((acc, el, i) => {
        acc[el.deptId] = i
        return acc
      }, {})
      const roots = []
      arrs.forEach(el => {
        // 判断根节点
        if (el.pid === null || el.pid === 0) {
          roots.push(el)
          return
        }
        // 用映射表找到父元素
        const parentEl = arrs[idMapping[el.pid]]
        // 把当前元素添加到父元素的`children`数组中
        parentEl.children = [...(parentEl.children || []), el]
      })
      return roots
    },

    save() {
      const rows = this.$refs.table.store.states.selection
      const request = this.buildRequest(rows)
      saveShare(request).then(res => {
        this.$success('保存成功')
        return true
      }).catch(err => {
        this.$error(err.message)
        return false
      })
      console.log('dept save')
    },
    cancel() {
      console.log('dept cancel')
    },

    buildRequest(rows) {
      const targetIds = rows.map(row => row.deptId)
      const panelIds = [this.resourceId]
      return {
        targetIds: targetIds,
        panelIds: panelIds,
        type: this.type
      }
    },

    queryShareNodeIds(callBack) {
      const conditionResourceId = { field: 'panel_group_id', operator: 'eq', value: this.resourceId }
      const conditionType = { field: 'type', operator: 'eq', value: this.type }
      const param = { conditions: [conditionResourceId, conditionType] }
      loadShares(param).then(res => {
        const shares = res.data
        const nodeIds = shares.map(share => share.targetId)
        this.shares = nodeIds
        this.$nextTick(() => {
          this.setCheckNodes()
        })
        callBack && callBack()
      })
    },

    setCheckNodes() {
      this.data.forEach(node => {
        const nodeId = node.deptId
        this.shares.includes(nodeId) && this.$refs.table.toggleRowSelection(node, true)
      })
    },
    setCheckExpandNodes(rows) {
      rows.forEach(node => {
        const nodeId = node.deptId
        this.shares.includes(nodeId) && this.$refs.table.toggleRowSelection(node, true)
      })
    }

  }
}
</script>

<style scoped>

.my_table >>> .el-table__row>td{
  /* 去除表格线 */
  border: none;
  padding: 0 0;
}
.my_table >>> .el-table th.is-leaf {
  /* 去除上边框 */
    border: none;
}
.my_table >>> .el-table::before{
  /* 去除下边框 */
  height: 0;
}
</style>
