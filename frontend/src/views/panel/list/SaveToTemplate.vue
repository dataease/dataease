<template>
  <el-row>
    <el-row>
      <el-col :span="4"> {{ $t('panel.template_nale')}}</el-col>
      <el-col :span="20">
        <el-input v-model="templateInfo.name" clearable size="mini" />
      </el-col>
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
            <template slot-scope="scope">
              <el-radio v-model="tableRadio" :label="scope.row"><i /></el-radio>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-row>
    <el-row class="root-class">
      <el-button @click="cancel()">{{ $t('commons.cancle')}}</el-button>
      <el-button type="primary" @click="save()">{{ $t('commons.save')}}</el-button>
    </el-row>
  </el-row>
</template>

<script>
import { post } from '@/api/panel/panel'

export default {
  name: 'SaveToTemplate',
  props: {
    templateInfo: {
      type: Object,
      require: true
    }
  },
  data() {
    return {
      data: [],
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
        templateType: 'self',
        level: '0'
      }
      post('/template/templateList', param).then(response => {
        this.data = response.data
      })
    },
    clickChange(item) {
      this.tableRadio = item
      this.templateInfo.pid = item.id
    },
    cancel() {
      this.$emit('closeSaveDialog')
    },
    save() {
      if (!this.templateInfo.pid) {
        this.$warning('请选择所属类别')
        return false
      }
      if (!this.templateInfo.name) {
        this.$warning('模板名称不能为空')
        return false
      }
      post('/template/save', this.templateInfo).then(response => {
        this.$message({
          message: '保存成功',
          type: 'success',
          showClose: true
        })
        this.$emit('closeSaveDialog')
      })
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
