<template>
  <el-row>
    <el-row style="display: flex;align-items: center;">
      <el-col :span="4"> {{ $t('panel.template_nale') }}</el-col>
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
      <el-button size="mini" @click="cancel()">{{ $t('commons.cancel') }}</el-button>
      <el-button size="mini" type="primary" @click="save()">{{ $t('commons.save') }}</el-button>
    </el-row>
  </el-row>
</template>

<script>
import { save, nameCheck, showTemplateList } from '@/api/system/template'

export default {
  name: 'SaveToTemplate',
  props: {
    templateInfo: {
      type: Object
    }
  },
  data() {
    return {
      data: [],
      fieldName: 'name',
      tableRadio: null,
      keyWordSearch: '',
      columnLabel: this.$t('panel.belong_to_category')
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
      showTemplateList(param).then(response => {
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
        this.$warning(this.$t('panel.pls_select_belong_to_category'))
        return false
      }
      if (!this.templateInfo.name) {
        this.$warning(this.$t('panel.template_name_cannot_be_empty'))
        return false
      }

      const nameCheckRequest = {
        pid: this.templateInfo.pid,
        name: this.templateInfo.name,
        optType: 'insert'
      }
      nameCheck(nameCheckRequest).then(response => {
        if (response.data.indexOf('exist') > -1) {
          this.$confirm(this.$t('template.exit_same_template_check'), this.$t('template.confirm_upload'), {
            confirmButtonText: this.$t('template.override'),
            cancelButtonText: this.$t('template.cancel'),
            type: 'warning'
          }).then(() => {
            save(this.templateInfo).then(response => {
              this.$message({
                message: this.$t('commons.save_success'),
                type: 'success',
                showClose: true
              })
              this.$emit('closeSaveDialog')
            })
          }).catch(() => {
          })
        } else {
          save(this.templateInfo).then(response => {
            this.$message({
              message: this.$t('commons.save_success'),
              type: 'success',
              showClose: true
            })
            this.$emit('closeSaveDialog')
          })
        }
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

  .root-class {
    margin: 15px 0px 5px;
    text-align: center;
  }
</style>
