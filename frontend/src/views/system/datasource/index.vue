<template>
  <div v-loading="result.loading">

    <el-card class="table-card">
      <template v-slot:header>
        <ms-table-header
          :condition.sync="condition"
          :create-tip="$t('datasource.create')"
          :title="$t('commons.datasource')"
          @search="initTableData"
          @create="create"
        />
      </template>
      <!-- system menu datasource table-->
      <el-table border class="adjust-table" :data="tableData" style="width: 100%">
        <el-table-column prop="name" :label="$t('commons.name')" />
        <el-table-column prop="desc" :label="$t('commons.description')" />
        <el-table-column prop="type" :label="$t('datasource.type')" />
        <el-table-column :label="$t('commons.operating')">
          <template v-slot:default="scope">
            <ms-table-operator @editClick="edit(scope.row)" @deleteClick="handleDelete(scope.row)" />
          </template>
        </el-table-column>
      </el-table>
      <ms-table-pagination
        :change="initTableData"
        :current-page.sync="currentPage"
        :page-size.sync="pageSize"
        :total="total"
      />
    </el-card>

    <!-- add datasource form -->
    <el-dialog
      :close-on-click-modal="false"
      :title="$t('datasource.create')"
      :visible.sync="dialogDatasourceAddVisible"
      width="30%"
      :destroy-on-close="true"
      @closed="closeFunc"
    >
      <el-form ref="createDatasource" :model="form" label-position="right" label-width="100px" size="small">
        <el-form-item
          :label="$t('commons.name')"
          prop="name"
          :rules="[{required: true, message: this.$t('datasource.input_name'), trigger: 'blur'},
                   {min: 2, max: 25, message: this.$t('commons.input_limit', [2, 25]), trigger: 'blur'}]"
        >
          <el-input v-model="form.name" autocomplete="off" />
        </el-form-item>
        <el-form-item
          :label="$t('commons.description')"
          prop="desc"
          :rules="[{required: true, message: this.$t('datasource.input_desc'), trigger: 'blur'},
                   {min: 2, max: 50, message: this.$t('commons.input_limit', [2, 50]), trigger: 'blur'}]"
        >
          <el-input v-model="form.desc" autocomplete="off" type="textarea" />
        </el-form-item>
        <el-form-item :label="$t('datasource.type')" prop="type" :rules="{required: true, message: $t('datasource.please_choose_type'), trigger: 'change'}">
          <el-select v-model="form.type" :placeholder="$t('datasource.please_choose_type')" class="select-width" @change="changeType()">
            <el-option
              v-for="item in allTypes"
              :key="item.name"
              :label="item.name"
              :value="item.name"
            />
          </el-select>
        </el-form-item>

        <el-form-item v-show="form.configuration.dataSourceType=='jdbc'" :label="$t('datasource.data_base')" prop="configuration.dataBase" :rules="{required: true, message: $t('datasource.please_input_data_base'), trigger: 'blur'}">
          <el-input v-model="form.configuration.dataBase" autocomplete="off" />
        </el-form-item>
        <el-form-item v-show="form.configuration.dataSourceType=='jdbc'" :label="$t('datasource.user_name')" prop="configuration.username">
          <el-input v-model="form.configuration.username" autocomplete="off" />
        </el-form-item>
        <el-form-item v-show="form.configuration.dataSourceType=='jdbc'" :label="$t('datasource.password')" prop="configuration.password" :rules="{required: true, message: $t('datasource.please_input_password'), trigger: 'change'}">
          <el-input v-model="form.configuration.password" autocomplete="off" />
        </el-form-item>
        <el-form-item v-show="form.configuration.dataSourceType=='jdbc'" :label="$t('datasource.host')" prop="configuration.host" :rules="{required: true, message: $t('datasource.please_input_host'), trigger: 'change'}">
          <el-input v-model="form.configuration.host" autocomplete="off" />
        </el-form-item>
        <el-form-item v-show="form.configuration.dataSourceType=='jdbc'" :label="$t('datasource.port')" prop="configuration.port" :rules="{required: true, message: $t('datasource.please_input_port'), trigger: 'change'}">
          <el-input v-model="form.configuration.port" autocomplete="off" />
        </el-form-item>

      </el-form>
      <template v-slot:footer>
        <ms-dialog-footer
          :is-show-validate="true"
          @cancel="dialogDatasourceAddVisible = false"
          @validate="validaDatasource('createDatasource')"
          @confirm="createDatasource('createDatasource')"
        />
      </template>
    </el-dialog>

    <!-- update datasource form -->
    <el-dialog
      :close-on-click-modal="false"
      :title="$t('datasource.modify')"
      :visible.sync="dialogDatasourceUpdateVisible"
      width="30%"
      :destroy-on-close="true"
      @close="closeFunc"
    >
      <el-form
        ref="updateDatasourceForm"
        :model="form"
        label-position="right"
        label-width="100px"
        size="small"
        :rules="rule"
      >
        <el-form-item
          :label="$t('commons.name')"
          prop="name"
          :rules="[{required: true, message: this.$t('datasource.input_name'), trigger: 'blur'},
                   {min: 2, max: 25, message: this.$t('commons.input_limit', [2, 25]), trigger: 'blur'}]"
        >
          <el-input v-model="form.name" autocomplete="off" />
        </el-form-item>
        <el-form-item
          :label="$t('commons.description')"
          prop="desc"
          :rules="[{required: true, message: this.$t('datasource.input_desc'), trigger: 'blur'},
                   {min: 2, max: 50, message: this.$t('commons.input_limit', [2, 50]), trigger: 'blur'}]"
        >
          <el-input v-model="form.desc" autocomplete="off" />
        </el-form-item>
        <el-form-item v-show="form.configuration.dataSourceType=='jdbc'" :label="$t('datasource.data_base')" prop="configuration.dataBase" :rules="{required: true, message: $t('datasource.please_input_data_base'), trigger: 'blur'}">
          <el-input v-model="form.configuration.dataBase" autocomplete="off" />
        </el-form-item>
        <el-form-item v-show="form.configuration.dataSourceType=='jdbc'" :label="$t('datasource.user_name')" prop="configuration.username">
          <el-input v-model="form.configuration.username" autocomplete="off" />
        </el-form-item>
        <el-form-item v-show="form.configuration.dataSourceType=='jdbc'" :label="$t('datasource.password')" prop="configuration.password" :rules="{required: true, message: $t('datasource.please_input_password'), trigger: 'change'}">
          <el-input v-model="form.configuration.password" autocomplete="off" />
        </el-form-item>
        <el-form-item v-show="form.configuration.dataSourceType=='jdbc'" :label="$t('datasource.host')" prop="configuration.host" :rules="{required: true, message: $t('datasource.please_input_host'), trigger: 'change'}">
          <el-input v-model="form.configuration.host" autocomplete="off" />
        </el-form-item>
        <el-form-item v-show="form.configuration.dataSourceType=='jdbc'" :label="$t('datasource.port')" prop="configuration.port" :rules="{required: true, message: $t('datasource.please_input_port'), trigger: 'change'}">
          <el-input v-model="form.configuration.port" autocomplete="off" />
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <ms-dialog-footer
          :is-show-validate="true"
          @cancel="dialogDatasourceUpdateVisible = false"
          @validate="validaDatasource('updateDatasourceForm')"
          @confirm="updateDatasource('updateDatasourceForm')"
        />
      </template>
    </el-dialog>

    <ms-delete-confirm ref="deleteConfirm" :title="$t('datasource.delete')" @delete="_handleDelete" />

  </div>
</template>

<script>
import MsCreateBox from '../CreateBox'
import MsTablePagination from '@/metersphere/common/pagination/TablePagination'
import MsTableHeader from '@/metersphere/common/components/MsTableHeader'
import MsTableOperator from '@/metersphere/common/components/MsTableOperator'
import MsDialogFooter from '@/metersphere/common/components/MsDialogFooter'
import {
  listenGoBack,
  removeGoBackListener
} from '@/metersphere/common/js/utils'
import MsDeleteConfirm from '@/metersphere/common/components/MsDeleteConfirm'

import { dsGrid, addDs, editDs, delDs, validateDs } from '@/api/system/datasource'

export default {
  name: 'DEDatasource',
  components: {
    MsDeleteConfirm,
    MsCreateBox,
    MsTablePagination,
    MsTableHeader,
    MsTableOperator,
    MsDialogFooter
  },
  data() {
    return {
      queryPath: '/datasource/list',
      deletePath: '/datasource/delete/',
      createPath: '/datasource/add',
      updatePath: '/datasource/update',
      validatePath: '/datasource/validate',
      result: {},
      dialogDatasourceAddVisible: false,
      dialogDatasourceUpdateVisible: false,
      dialogDatasourceMemberVisible: false,
      dialogDatasourceMemberAddVisible: false,
      dialogDatasourceMemberUpdateVisible: false,
      multipleSelection: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      dialogCurrentPage: 1,
      dialogPageSize: 10,
      dialogTotal: 0,
      currentRow: {},
      condition: {},
      dialogCondition: {},
      tableData: [],
      memberLineData: [],
      form: { configuration: {}},
      allTypes: [{ name: 'mysql', type: 'jdbc' }, { name: 'sqlServer', type: 'jdbc' }],
      memberForm: {},
      rule: {
        name: [
          { required: true, message: this.$t('organization.input_name'), trigger: 'blur' },
          { min: 2, max: 25, message: this.$t('commons.input_limit', [2, 25]), trigger: 'blur' }
        ],
        desc: [
          { required: true, message: this.$t('organization.input_name'), trigger: 'blur' },
          { max: 50, message: this.$t('commons.input_limit', [0, 50]), trigger: 'blur' }
        ]
      }
    }
  },
  activated() {
    this.initTableData()
  },
  methods: {
    create() {
      this.dialogDatasourceAddVisible = true
      listenGoBack(this.closeFunc)
    },
    dataFilter(val) {
      if (val) {
        this.memberForm.userList = this.memberForm.copyUserList.filter((item) => {
          if (!!~item.id.indexOf(val) || !!~item.id.toUpperCase().indexOf(val.toUpperCase())) {
            return true
          }
        })
      } else {
        this.memberForm.userList = this.memberForm.copyUserList
      }
    },
    edit(row) {
      this.dialogDatasourceUpdateVisible = true
      this.form = Object.assign({}, row)
      this.form.configuration = JSON.parse(this.form.configuration)
      listenGoBack(this.closeFunc)
    },
    cellClick(row) {
      // 保存当前点击的组织信息到currentRow
      this.currentRow = row
      this.dialogDatasourceMemberVisible = true
      const param = {
        name: '',
        organizationId: row.id
      }
      const path = '/user/special/org/member/list'
      this.result = this.$post(path + '/' + this.dialogCurrentPage + '/' + this.dialogPageSize, param, res => {
        const data = res.data
        this.memberLineData = data.listObject
        const url = '/userrole/list/org/' + row.id
        for (let i = 0; i < this.memberLineData.length; i++) {
          this.$get(url + '/' + encodeURIComponent(this.memberLineData[i].id), response => {
            const roles = response.data
            this.$set(this.memberLineData[i], 'roles', roles)
          })
        }
        this.dialogTotal = data.itemCount
      })
      listenGoBack(this.closeFunc)
    },
    dialogSearch() {
      const row = this.currentRow
      this.dialogDatasourceMemberVisible = true
      const param = this.dialogCondition
      this.$set(param, 'organizationId', row.id)
      const path = '/user/special/org/member/list'
      this.result = this.$post(path + '/' + this.dialogCurrentPage + '/' + this.dialogPageSize, param, res => {
        const data = res.data
        this.memberLineData = data.listObject
        const url = '/userrole/list/org/' + row.id
        for (let i = 0; i < this.memberLineData.length; i++) {
          this.$get(url + '/' + encodeURIComponent(this.memberLineData[i].id), response => {
            const roles = response.data
            this.$set(this.memberLineData[i], 'roles', roles)
          })
        }
        this.dialogTotal = data.itemCount
      })
    },
    handleDelete(datasource) {
      this.$refs.deleteConfirm.open(datasource)
    },
    _handleDelete(datasource) {
      this.$confirm(this.$t('datasource.delete_confirm'), '', {
        confirmButtonText: this.$t('commons.confirm'),
        cancelButtonText: this.$t('commons.cancel'),
        type: 'warning'
      }).then(() => {
        delDs(datasource.id).then(res => {
          this.$success(this.$t('commons.delete_success'))
          this.initTableData()
        })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: this.$t('commons.delete_cancelled')
        })
      })
    },
    createDatasource(createDatasourceForm) {
      this.$refs[createDatasourceForm].validate(valid => {
        if (valid) {
          this.form.configuration = JSON.stringify(this.form.configuration)
          addDs(this.form).then(res => {
            this.$success(this.$t('commons.save_success'))
            this.initTableData()
            this.dialogDatasourceAddVisible = false
          })

          this.dialogDatasourceAddVisible = false
        } else {
          return false
        }
      })
    },
    updateDatasource(updateDatasourceForm) {
      this.$refs[updateDatasourceForm].validate(valid => {
        if (valid) {
          this.form.configuration = JSON.stringify(this.form.configuration)

          editDs(this.form).then(res => {
            this.$success(this.$t('commons.modify_success'))
            this.dialogDatasourceUpdateVisible = false
            this.initTableData()
          })
        } else {
          return false
        }
      })
    },
    validaDatasource(datasourceForm) {
      this.$refs[datasourceForm].validate(valid => {
        if (valid) {
          const data = JSON.parse(JSON.stringify(this.form))
          data.configuration = JSON.stringify(data.configuration)

          validateDs(data).then(res => {
            this.$success(this.$t('datasource.validate_success'))
          })
        } else {
          return false
        }
      })
    },
    changeType() {
      for (let i = 0; i < this.allTypes.length; i++) {
        if (this.allTypes[i].name == this.form.type) {
          this.form.configuration.dataSourceType = this.allTypes[i].type
        }
      }
    },
    initTableData() {
      dsGrid(this.currentPage, this.pageSize, this.condition).then(response => {
        const data = response.data.data
        this.tableData = data.listObject
        this.total = data.itemCount
      })
    },
    closeFunc() {
      this.memberLineData = []
      this.initTableData()
      this.form = { configuration: {}}
      removeGoBackListener(this.closeFunc)
      this.dialogDatasourceAddVisible = false
      this.dialogDatasourceUpdateVisible = false
      this.dialogDatasourceMemberVisible = false
      this.dialogDatasourceMemberAddVisible = false
      this.dialogDatasourceMemberUpdateVisible = false
    },
    handleSelectionChange(val) {
      this.multipleSelection = val
    }
  }

}
</script>

<style scoped>

  .member-size {
    text-decoration: underline;
  }

  .org-member-id {
    float: left;
  }

  .org-member-email {
    float: right;
    color: #8492a6;
    font-size: 13px;
  }

  .select-width {
    width: 100%;
  }

  .dialog-css >>> .el-dialog__header {
    padding: 0;
  }

</style>
