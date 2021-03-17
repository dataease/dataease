<template>
  <div class="my_table">
    <el-table
      ref="table"
      :data="data"
      :show-header="true"
      style="width: 100%"
      :row-style="{height: '35px'}"
      @filter-change="filterChange"
    >
      <el-table-column :column-key="fieldName" :label="columnLabel" :prop="fieldName" filter-placement="right-start" :filters="filter_options" :filter-multiple="false" :filter-method="filterHandler" />
      <el-table-column type="selection" fixd />
    </el-table>
  </div>
</template>

<script>
import { userLists } from '@/api/system/user'
import { formatCondition } from '@/utils/index'
export default {
  name: 'GrantUser',
  props: {
    resourceId: {
      type: String,
      default: null
    }
  },
  data() {
    return {
      data: [],
      defaultHeadName: '全部',
      columnLabel: null,
      filter_options: [{ text: '未分享人员', value: 0 }, { text: '已分享人员', value: 1 }],
      fieldName: 'nickName'
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
      })
    },
    filterHandler(value, row, column) {
      // const property = column['property']
      // return row[property] === value
      return row['code'] === 'admin'
    },

    filterChange(obj) {
      const arr = obj[this.fieldName]
      if (arr.length === 0) {
        this.initColumnLabel()
        return
      }
      this.columnLabel = this.filter_options[arr[0]].text
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
