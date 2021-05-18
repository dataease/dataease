<template>
  <div class="my_table">
    <el-table
      :data="data.filter(node => !filterText || node[fieldName].toLowerCase().includes(filterText.toLowerCase()))"
      :show-header="false"
      style="width: 100%"
      :row-style="{height: '26px'}"
      highlight-current-row
      @current-change="handleCurrentChange"
    >
      <el-table-column :column-key="fieldName" :prop="fieldName" filter-placement="right-start" />
    </el-table>
  </div>
</template>

<script>
import { userLists } from '@/api/system/user'
import { formatCondition } from '@/utils/index'
export default {
  name: 'AuthUser',
  props: {
    filterText: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      data: [],
      fieldName: 'nickName',
      type: 0, // 类型0代表用户
      shares: []
    }
  },
  created() {
    this.search()
  },
  methods: {
    search(condition) {
      const temp = formatCondition(condition)
      const param = temp || {}
      userLists(1, 0, param).then(response => {
        debugger
        const data = response.data
        this.data = data.listObject
      })
    },
    cancel() {
      console.log('role cancel')
    },
    handleCurrentChange(val) {
      console.log('handleCurrentChange')
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
