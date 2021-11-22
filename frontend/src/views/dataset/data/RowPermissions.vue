<template>
  <el-col>
    <el-row v-loading="$store.getters.loadingMap[$store.getters.currentPath]" style="margin-top: 10px;">
      <complex-table :data="data" :columns="columns" local-key="rowPermission" :search-config="searchConfig" :pagination-config="paginationConfig" @select="select" @search="search">
        <template #toolbar>
          <el-button icon="el-icon-circle-plus-outline" @click="create(undefined)">{{ $t('commons.add') }}</el-button>
        </template>

        <el-table-column prop="authTargetType" :label="$t('dataset.row_permission.type')">
          <template slot-scope="scope">
            <span v-if="scope.row.authTargetType === 'dept'">{{ $t('auth.dept') }}</span>
            <span v-if="scope.row.authTargetType === 'role'">{{ $t('auth.role') }}</span>
            <span v-if="scope.row.authTargetType === 'user'">{{ $t('auth.user') }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="authTargetName" :label="$t('dataset.row_permission.name')" />
        <el-table-column prop="fieldName" :label="$t('dataset.field_name')" />

        <el-table-column prop="filter" :label="$t('dataset.row_permission.value')">
          <template slot-scope="scope">
            <el-table :data="scope.row.filter" :show-header='false' style="width: 100%">
              <el-table-column prop="term" width="180">
                <template slot-scope="scope">
                  <span v-if="scope.row.term === 'eq'">{{ $t('chart.filter_eq') }}</span>
                  <span v-if="scope.row.term === 'not_eq'">{{ $t('chart.filter_not_eq') }}</span>
                  <span v-if="scope.row.term === 'lt'">{{ $t('chart.filter_lt') }}</span>
                  <span v-if="scope.row.term === 'gt'">{{ $t('chart.filter_gt') }}</span>
                  <span v-if="scope.row.term === 'le'">{{ $t('chart.filter_le') }}</span>
                  <span v-if="scope.row.term === 'ge'">{{ $t('chart.filter_le') }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="value" width="180"></el-table-column>
            </el-table>

          </template>
        </el-table-column>
        <fu-table-operations :buttons="buttons" :label="$t('commons.operating')" fix />
      </complex-table>
    </el-row>


    <el-dialog v-dialogDrag :title="update_row_permission_dialog_title" :visible="update_row_permission" :show-close="false" width="50%" class="dialog-css" append-to-body>
      <el-col>
        <el-form ref="rowPermissionForm" :form="rowPermissionForm" :model="rowPermissionForm" label-width="100px" >
          <el-form-item :label="$t('dataset.row_permission.type')" prop="type">
            <el-select v-model="rowPermissionForm.authTargetType" @change="onTypeChange">
              <el-option :label="$t('auth.dept')" value="dept" />
              <el-option :label="$t('auth.role')" value="role" />
              <el-option :label="$t('auth.user')" value="user" />
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('commons.name')" prop="authTargetId">
            <el-select v-model="rowPermissionForm.authTargetId">
              <el-option
                v-for="item in targetObjs"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item  :label="$t('dataset.field_name')" prop="authTargetId">
            <el-select  v-model="rowPermissionForm.datasetFieldId">
              <el-option
                v-for="item in filedList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-col>
              <el-button icon="el-icon-plus" circle size="mini" style="margin-bottom: 10px;" @click="addFilter" />
              <div style="max-height: 50vh;overflow-y: auto;">
                <el-row v-for="(f,index) in rowPermissionForm.filter" :key="index" class="filter-item">
                  <el-col :span="4">
                    <el-select v-model="f.term" size="mini">
                      <el-option-group
                        v-for="(group,idx) in options"
                        :key="idx"
                        :label="group.label"
                      >
                        <el-option
                          v-for="opt in group.options"
                          :key="opt.value"
                          :label="opt.label"
                          :value="opt.value"
                        />
                      </el-option-group>
                    </el-select>
                  </el-col>
                  <el-col :span="6">
                    <el-input v-show="!f.term.includes('null')" v-model="f.value" class="value-item" :placeholder="$t('chart.condition')" size="mini" clearable />
                  </el-col>
                  <el-col :span="2">
                    <el-button type="text" icon="el-icon-delete" circle style="float: right" @click="removeFilter(index)" />
                  </el-col>
                </el-row>
              </div>
            </el-col>
          </el-form-item>

        </el-form>
      </el-col>
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="closeDialog">{{ $t('dataset.cancel') }}</el-button>
        <el-button type="primary" size="mini" @click="save()">{{ $t('dataset.confirm') }}</el-button>
      </div>
    </el-dialog>


  </el-col>
</template>

<script>
import ComplexTable from '@/components/business/complex-table'
import { formatCondition, formatQuickCondition, formatOrders } from '@/utils/index'
import '@riophae/vue-treeselect/dist/vue-treeselect.css'
import { datasetRowPermissionsList, post, fieldList} from '@/api/dataset/dataset'
import TableSelector from '@/views/chart/view/TableSelector'

export default {
  name: 'RowPermissions',
  components: { ComplexTable, TableSelector },
  props: {
    param: {
      type: Object,
      default: null
    },
    table: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      columns: [],
      buttons: [
        {
          label: this.$t('commons.edit'), icon: 'el-icon-edit', type: 'primary', click: this.create, disabled: this.disableEdit
        },
        {
          label: this.$t('commons.delete'), icon: 'el-icon-delete', type: 'danger', click: this.deleteRowPermission, disabled: this.disableDelete
        }
      ],
      searchConfig: {
        useQuickSearch: true,
        useComplexSearch: true,
        quickPlaceholder: this.$t('dataset.task.search_by_name'),
        components: [
          { field: 'dataset_table_task.name', label: this.$t('dataset.task_name'), component: 'DeComplexInput' },
          { field: 'dataset_table_task.id', label: this.$t('dataset.task_id'), component: 'FuComplexInput' },
          { field: 'dataset_table.name', label: this.$t('dataset.name'), component: 'DeComplexInput' },
          { field: 'dataset_table_task.status', label: this.$t('dataset.task.task_status'), component: 'FuComplexSelect',
            options: [{ label: this.$t('dataset.task.stopped'), value: 'Stopped' }, { label: this.$t('dataset.task.underway'), value: 'Underway' }, { label: this.$t('dataset.task.pending'), value: 'Pending' }, { label: this.$t('dataset.underway'), value: 'Exec' }], multiple: false },
          { field: 'dataset_table_task.last_exec_status', label: this.$t('dataset.task.last_exec_status'), component: 'FuComplexSelect', options: [{ label: this.$t('dataset.completed'), value: 'Completed' }, { label: this.$t('dataset.underway'), value: 'Underway' }, { label: this.$t('dataset.error'), value: 'Error' }], multiple: false }
        ]
      },
      last_condition: null,
      orderConditions: [],
      paginationConfig: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      },
      data: [],
      update_row_permission: false,
      update_row_permission_dialog_title: '',
      defaultForm: {
        authTargetId: null,
        authTargetType: null,
        datasetFieldId: null,
        datasetId: '',
        filter: [{term: 'eq', value: ''}]
      },
      rowPermissionForm: {},
      targetObjs: [],
      filedList: [],
      depts: null,
      options: [
        {
          label: '',
          options: [{
            value: 'eq',
            label: this.$t('chart.filter_eq')
          }, {
            value: 'not_eq',
            label: this.$t('chart.filter_not_eq')
          }]
        },
        {
          label: '',
          options: [{
            value: 'lt',
            label: this.$t('chart.filter_lt')
          }, {
            value: 'gt',
            label: this.$t('chart.filter_gt')
          }]
        },
        {
          label: '',
          options: [{
            value: 'le',
            label: this.$t('chart.filter_le')
          }, {
            value: 'ge',
            label: this.$t('chart.filter_ge')
          }]
        }]
    }
  },
  computed: {
  },
  created() {
    this.rowPermissionForm.datasetId = this.table.id
    this.initFieldLists()
    this.search(this.last_condition)
  },
  beforeDestroy() {
  },
  methods: {
    initFieldLists(){
      fieldList(this.table.id).then(response => {
        this.filedList = response.data
      })
    },
    select(selection) {
    },
    search(condition, showLoading = true) {
      this.last_condition = condition
      condition = formatQuickCondition(condition, 'dataset_table_field.name')
      const temp = formatCondition(condition)
      const param = temp || {}
      param['orders'] = formatOrders(this.orderConditions)
      const { currentPage, pageSize } = this.paginationConfig
      datasetRowPermissionsList(this.table.id, currentPage, pageSize, param, showLoading).then(response => {
        this.data = response.data.listObject
        this.data.forEach(item => {
          item.filter = JSON.parse(item.filter)
          this.dataSetRowPermissionInfo(item)
        })
        this.paginationConfig.total = response.data.itemCount
      })
    },
    dataSetRowPermissionInfo(item) {
      let params = JSON.parse(JSON.stringify(item));
      params.filter = JSON.stringify(params.filter)
      post('/dataset/rowPermissions/dataSetRowPermissionInfo', params, false).then(response => {
        item.authTargetName = response.data.authTargetName
      })
    },
    create(rowPermissionObj) {
      if (!rowPermissionObj) { // add
        this.rowPermissionForm = Object.assign({}, this.defaultForm)
        this.update_row_permission_dialog_title = this.$t('dataset.row_permission.add')
      } else { // update
        this.rowPermissionForm = Object.assign({}, rowPermissionObj)
        this.update_row_permission_dialog_title = this.$t('dataset.row_permission.edit')
        this.fetchTypeList()
      }
      this.update_row_permission = true
    },
    onTypeChange(){
      this.rowPermissionForm.authTargetId = ''
      this.fetchTypeList()
    },
    fetchTypeList(){
      let params = JSON.parse(JSON.stringify(this.rowPermissionForm));
      params.filter = JSON.stringify(params.filter)
      post('/dataset/rowPermissions/authObjs', params).then(response => {
        this.targetObjs = response.data
      }).catch(() => {
        this.targetObjs = []
      })
    },
    addFilter() {
      this.rowPermissionForm.filter.push({
        term: 'eq',
        value: ''
      })
    },
    removeFilter(index) {
      this.rowPermissionForm.filter.splice(index, 1)
    },
    closeDialog() {
      this.update_row_permission = false
      this.resetTaskForm()
    },
    resetTaskForm(){
      this.rowPermissionForm={
        datasetId: this.rowPermissionForm.datasetId,
        authTargetId: null,
        filter: [{term: 'eq', value: ''}]
      }
    },
    deleteRowPermission(item) {
      this.$confirm(this.$t('dataset.confirm_delete'), this.$t('dataset.tips'), {
        confirmButtonText: this.$t('dataset.confirm'),
        cancelButtonText: this.$t('dataset.cancel'),
        type: 'warning'
      }).then(() => {
        post('/dataset/rowPermissions/delete/' + item.id, null).then(() => {
          this.$message({
            message: this.$t('dataset.delete_success'),
            type: 'success',
            showClose: true
          })
          this.search(this.last_condition, true)
        })
      }).catch(() => {
      })
    },
    save() {
      this.$refs.rowPermissionForm.validate(valid => {
        if (valid) {
          let params = JSON.parse(JSON.stringify(this.rowPermissionForm));
          params.filter = JSON.stringify(params.filter)
          post('/dataset/rowPermissions/save', params).then(() => {
            this.$message({
              message: this.$t('dataset.save_success'),
              type: 'success',
              showClose: true
            })
            this.update_row_permission = false
            this.resetTaskForm()
            this.search(this.last_condition, true)
          })
        } else {
          return false
        }
      })
    }
  }
}
</script>

<style scoped>
span{
  font-size: 12px;
}

</style>
