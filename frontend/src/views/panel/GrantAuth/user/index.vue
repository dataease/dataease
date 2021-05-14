<template>
  <div class="my_table">
    <el-table
      ref="table"
      :data="data.filter(node => !keyWord || node[fieldName].toLowerCase().includes(keyWord.toLowerCase()))"
      :show-header="true"
      style="width: 100%"
      :row-style="{height: '35px'}"
      @filter-change="filterChange"
    >
      <el-table-column :column-key="fieldName" :label="columnLabel" :prop="fieldName" filter-placement="right-start"
                       :filters="filter_options" :filter-multiple="false" :filter-method="filterHandler" />
      <el-table-column type="selection" fixd />
    </el-table>
  </div>
</template>

<script>
import { userLists } from '@/api/system/user'
import { formatCondition } from '@/utils/index'
import { saveShare, loadShares } from '@/api/panel/share'
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
      defaultHeadName: '全部',
      columnLabel: null,
      filter_options: [{ text: '未分享人员', value: 0 }, { text: '已分享人员', value: 1 }],
      fieldName: 'nickName',
      type: 0, // 类型0代表用户
      shares: []
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
      userLists(1, 0, param).then(response => {
        const data = response.data
        // this.total = data.itemCount
        this.data = data.listObject
        this.queryShareNodeIds()
      })
    },
    filterHandler(value, row, column) {
      // const property = column['property']
      // return row[property] === value
      const userId = row['userId']
      return !(value ^ this.shares.includes(userId))
    },

    filterChange(obj) {
      const arr = obj[this.fieldName]
      if (arr.length === 0) {
        this.initColumnLabel()
        return
      }
      this.columnLabel = this.filter_options[arr[0]].text
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
    },

    cancel() {
      console.log('user cancel')
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
        const nodeId = node.userId
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
