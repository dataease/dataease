<template>
  <div class="my_table">
    <el-table
      ref="table"
      :data="tableData"
      :show-header="true"
      style="width: 100%"
      :row-style="{height: '35px'}"
      @select="selectOne"
      @select-all="selectAll"
    >
      <el-table-column :column-key="fieldName" :label="columnLabel" :prop="fieldName" filter-placement="right-start" :filters="filter_options" :filter-multiple="false" :filter-method="filterHandler" />
      <el-table-column type="selection" fixd />
    </el-table>
  </div>
</template>

<script>
import { roleGrid } from '@/api/system/user'
import { formatCondition } from '@/utils/index'
import { loadShares } from '@/api/panel/share'
export default {
  name: 'GrantRole',
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
      defaultHeadName: this.$t('commons.all'),
      columnLabel: null,
      filter_options: [{ text: this.$t('panel.no_auth_role'), value: 0 }, { text: this.$t('panel.auth_role'), value: 1 }],
      fieldName: 'name',
      type: 1, // 类型1代表角色
      shares: [],

      tableData: []
    }
  },
  watch: {
    'keyWord': function(val) {
      this.tableData = this.data.filter(node => !val || node[this.fieldName].toLowerCase().includes(val.toLowerCase()))
      this.setCheckNodes()
    }
  },
  created() {
    this.initColumnLabel()
    this.search()
  },
  methods: {
    initColumnLabel() {
      this.columnLabel = this.defaultHeadName
    },

    search(condition) {
      const temp = formatCondition(condition)
      const param = temp || {}
      roleGrid(1, 0, param).then(response => {
        const data = response.data
        this.data = data.listObject
        this.tableData = data.listObject
        this.queryShareNodeIds()
      })
    },
    filterHandler(value, row, column) {
      const roleId = row['roleId']
      return !(value ^ this.shares.includes(roleId))
    },

    getSelected() {
      return {
        roleIds: this.shares
      }
    },

    cancel() {
    },
    buildRequest(rows) {
      const targetIds = rows.map(row => row.roleId)
      const panelIds = [this.resourceId]
      return {
        targetIds: targetIds,
        panelIds: panelIds,
        type: this.type
      }
    },

    queryShareNodeIds(callBack) {
      const param = { resourceId: this.resourceId, type: this.type }
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
      this.$nextTick(() => {
        this.$refs.table.store.states.data.forEach(node => {
          const nodeId = node.roleId
          this.shares.includes(nodeId) && this.$refs.table.toggleRowSelection(node, true)
        })
      })
    },
    selectOne(selection, row) {
      if (selection.some(node => node.roleId === row.roleId)) {
        // 如果选中了 且 已有分享数据不包含当前节点则添加
        if (!this.shares.includes(row.roleId)) {
          this.shares.push(row.roleId)
        }
      } else {
        // 如果取消选中 则移除
        this.shares = this.shares.filter(nodeId => row.roleId !== nodeId)
        // this.shares.splice(this.shares.findIndex(item => item.roleId === row.roleId), 1)
      }
    },
    selectAll(selection) {
      // 1.全选
      if (selection && selection.length > 0) {
        selection.forEach(node => {
          if (!this.shares.includes(node.roleId)) {
            this.shares.push(node.roleId)
          }
        })
      } else {
        // 2.全部取消
        const currentNodes = this.$refs.table.store.states.data
        const currentNodeIds = currentNodes.map(node => node.roleId)
        this.shares = this.shares.filter(nodeId => !currentNodeIds.includes(nodeId))
      }
    }

  }
}
</script>

<style scoped>

.my_table ::v-deep .el-table__row>td{
  /* 去除表格线 */
  border: none;
  padding: 0 0;
}
.my_table ::v-deep .el-table th.is-leaf {
  /* 去除上边框 */
    border: none;
}
.my_table ::v-deep .el-table::before{
  /* 去除下边框 */
  height: 0;
}
.my_table ::v-deep .el-table-column--selection .cell{
  text-align: center;
}
</style>
