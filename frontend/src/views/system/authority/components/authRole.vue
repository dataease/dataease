<template>
  <div class="my_table">
    <el-table
      :data="data.filter(node => !filterText || node[fieldName].toLowerCase().includes(filterText.toLowerCase()))"
      :show-header="false"
      style="width: 100%"
      :row-style="{height: '26px'}"
      highlight-current-row
      @current-change="nodeClick"
    >
      <el-table-column :column-key="fieldName" :prop="fieldName" filter-placement="right-start" />
    </el-table>
  </div>
</template>

<script>
import { roleGrid } from '@/api/system/role'
import { formatCondition } from '@/utils/index'
export default {
  name: 'AuthRole',
  props: {
    resourceId: {
      type: String,
      default: null
    },
    filterText: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      data: [],
      fieldName: 'name',
      type: 1, // 类型1代表角色
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
      roleGrid(1, 0, param).then(response => {
        const data = response.data
        this.data = data.listObject
      })
    },
    cancel() {
      console.log('role cancel')
    },
    nodeClick(val) {
      // role 为授权目标 加上authTarget
      this.$emit('nodeClick', { id: val.roleId, type: 'role' })
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
