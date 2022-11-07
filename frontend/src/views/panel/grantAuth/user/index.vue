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
      <el-table-column
        :column-key="fieldName"
        :label="columnLabel"
        :prop="fieldName"
        filter-placement="right-start"
        :filters="filter_options"
        :filter-multiple="false"
        :filter-method="filterHandler"
      />
      <el-table-column
        type="selection"
        fixd
      />
    </el-table>
  </div>
</template>

<script>
import { userListsWithOutPage } from '@/api/system/user'
import { formatCondition } from '@/utils/index'
import { loadShares } from '@/api/panel/share'
/* import { saveShare, loadShares } from '@/api/panel/share' */
export default {
  name: 'GrantUser',
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
      filter_options: [{ text: this.$t('panel.unshared_people'), value: 0 }, { text: this.$t('panel.shared_people'), value: 1 }],
      fieldName: 'nickName',
      type: 0, // 类型0代表用户
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
      userListsWithOutPage(param).then(response => {
        const data = response.data
        this.data = data.filter(ele => ele.id !== this.$store.getters.user.userId)
        this.tableData = data.filter(ele => ele.id !== this.$store.getters.user.userId)
        this.queryShareNodeIds()
      })
    },
    filterHandler(value, row, column) {
      const userId = row['userId']
      return !(value ^ this.shares.includes(userId))
    },

    getSelected() {
      return {
        userIds: this.shares
      }
    },

    cancel() {
    },
    buildRequest(rows) {
      const targetIds = rows.map(row => row.userId)
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
          const nodeId = node.userId
          this.shares.includes(nodeId) && this.$refs.table.toggleRowSelection(node, true)
        })
      })
    },
    selectOne(selection, row) {
      if (selection.some(node => node.userId === row.userId)) {
        // 如果选中了 且 已有分享数据不包含当前节点则添加
        if (!this.shares.includes(row.userId)) {
          this.shares.push(row.userId)
        }
      } else {
        // 如果取消选中 则移除
        this.shares = this.shares.filter(nodeId => row.userId !== nodeId)
      }
    },
    selectAll(selection) {
      // 1.全选
      if (selection && selection.length > 0) {
        selection.forEach(node => {
          if (!this.shares.includes(node.userId)) {
            this.shares.push(node.userId)
          }
        })
      } else {
        // 2.全部取消
        const currentNodes = this.$refs.table.store.states.data
        const currentNodeIds = currentNodes.map(node => node.userId)
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
