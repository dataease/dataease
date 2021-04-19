<template>
  <el-row>
    <el-row>
      <el-input v-model="name" placeholder="名称" />
    </el-row>
    <el-row class="de-tab">
      <div class="my_table">
        <el-table
          ref="table"
          :data="data.filter(node => !keyWordSearch || node[fieldName].toLowerCase().includes(keyWordSearch.toLowerCase()))"
          style="width: 100%"
          :row-style="{height: '35px'}"
          highlight-current-row
          @current-change="clickChange"
        >
          <el-table-column :label="columnLabel" :column-key="fieldName" :prop="fieldName" />
          <el-table-column align="right">
            <template slot="header">
              <el-input v-model="keyWordSearch" size="mini" placeholder="输入关键字搜索" />
            </template>
            <template slot-scope="scope">
              <el-radio v-model="tableRadio" :label="scope.row"><i /></el-radio>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-row>
    <el-row class="root-class">
      <el-button @click="cancel">取 消</el-button>
      <el-button type="primary" @click="save">确 定</el-button>
    </el-row>
  </el-row>
</template>

<script>
import { post } from '@/api/panel/panel'

export default {
  name: 'SaveToTemplate',
  data() {
    return {
      data: [],
      name: '',
      fieldName: 'name',
      tableRadio: null,
      keyWordSearch: '',
      columnLabel: '所属类别'
    }
  },
  created() {
    this.search()
  },
  methods: {
    search() {
      const param = {
        template_type: 'self',
        level: '0'
      }
      post('/template/templateList', param).then(response => {
        this.data = response.data
      })
    },

    setCheckNodes() {
      this.data.forEach(node => {
        const nodeId = node.userId
        this.shares.includes(nodeId) && this.$refs.table.toggleRowSelection(node, true)
      })
    },
    clickChange(item) {
      this.tableRadio = item
    },
    cancel() {
      this.$refs[this.activeName].cancel()
      this.$emit('close-grant', 0)
    }

  }
}
</script>

<style scoped>
  .de-tab {
    border:1px solid #E6E6E6;
    min-height:200px !important;
    max-height:300px !important;
    overflow:auto;
    margin-top: 10px;
  }

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

  .root-class {
    margin: 15px 0px 5px;
    text-align: center;
  }
</style>
