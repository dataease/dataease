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
import { formatCondition } from '@/utils/index'
export default {
  name: 'GrantDept',
  props: {
    resourceId: {
      type: String,
      default: null
    }
  },
  data() {
    return {
      data: [],
      defaultCondition: {
        field: 'pid',
        operator: 'eq',
        value: 0
      }
    //   maps: new Map()
    }
  },
  created() {
    this.search()
  },
  methods: {
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
      })
    },
    // 加载表格数据
    search(condition) {
      // this.setTableAttr()
      this.data = []
      let param = {}
      if (condition && condition.quick) {
        const con = this.quick_condition(condition)
        param = formatCondition(con)
      } else {
        param = { conditions: [this.defaultCondition] }
      }

      // param.conditions.push(this.defaultCondition)
      loadTable(param).then(res => {
        let data = res.data
        data = data.map(obj => {
          if (obj.subCount > 0) {
            obj.hasChildren = true
          }
          return obj
        })

        if (condition && condition.quick) {
          data = this.buildTree(data)
          // this.setTableAttr(true)
        }
        this.data = data
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
