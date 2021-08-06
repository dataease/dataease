<template>
  <el-col>
    <el-row style="margin-top: 10px;" v-loading="$store.getters.loadingMap[$store.getters.currentPath]">
      <complex-table :data="data" :columns="columns" local-key="datasetTaskRecord" :search-config="searchConfig" :transCondition="transCondition" :pagination-config="paginationConfig" @select="select" @search="search" @sort-change="sortChange">
        <el-table-column prop="name" :label="$t('dataset.task_name')">
          <template slot-scope="scope">
            <span>
              <el-link :type="matchLogId && scope.row.id === matchLogId ? 'danger': ''" style="font-size: 12px" @click="jumpTask(scope.row)">{{ scope.row.name }}</el-link>
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="datasetName" :label="$t('dataset.task.dataset')" />
        <el-table-column prop="startTime" :label="$t('dataset.start_time')">
          <template slot-scope="scope">
            <span>{{ scope.row.startTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="endTime" :label="$t('dataset.end_time')">
          <template slot-scope="scope">
            <span>{{ scope.row.endTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="status" :label="$t('dataset.status')">
          <template slot-scope="scope">
            <span v-if="scope.row.status === 'Completed'" style="color: green">{{ $t('dataset.completed') }}</span>
            <span v-if="scope.row.status === 'Underway'" style="color: blue">
              <i class="el-icon-loading" />
              {{ $t('dataset.underway') }}
            </span>
            <span v-if="scope.row.status === 'Error'" style="color: red">
              <el-link type="danger" style="font-size: 12px" @click="showErrorMassage(scope.row.info)">{{ $t('dataset.error') }}</el-link>
            </span>
          </template>
        </el-table-column>
      </complex-table>
    </el-row>
    <el-dialog
      v-dialogDrag
      :title="$t('dataset.detail')"
      :visible="show_error_massage"
      :show-close="false"
      width="50%"
      class="dialog-css"
    >
      <span class="err-msg">{{ error_massage }}</span>
      <span slot="footer" class="dialog-footer">
        <el-button size="mini" @click="show_error_massage = false">{{ $t('dataset.close') }}</el-button>
      </span>
    </el-dialog>
  </el-col>
</template>

<script>
import ComplexTable from '@/components/business/complex-table'
import { formatCondition, formatQuickCondition, addOrder, formatOrders } from '@/utils/index'
import '@riophae/vue-treeselect/dist/vue-treeselect.css'
import { post } from '@/api/dataset/dataset'
import {loadMenus} from "@/permission";

export default {
  name: 'TaskRecord',
  components: { ComplexTable },
  props: {
    param: {
      type: Object,
      default: null
    },
    transCondition: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      header: '',
      columns: [],
      buttons: [
        {
          label: this.$t('commons.edit'), icon: 'el-icon-edit', type: 'primary', click: this.edit,
          show: this.checkPermission(['user:edit'])
        }
      ],
      searchConfig: {
        useQuickSearch: true,
        useComplexSearch: true,
        quickPlaceholder: this.$t('dataset.task.search_by_name'),
        components: [
          { field: 'dataset_table_task.name', label: this.$t('dataset.task_name'), component: 'FuComplexInput' },
          { field: 'dataset_table_task.id', label: this.$t('dataset.task_id'), component: 'FuComplexInput' },
          { field: 'dataset_table.name', label: this.$t('dataset.name'), component: 'DeComplexInput' },
          { field: 'dataset_table_task_log.status', label: this.$t('commons.status'), component: 'FuComplexSelect', options: [{ label: this.$t('dataset.completed'), value: 'Completed' }, { label: this.$t('dataset.underway'), value: 'Underway' }, { label: this.$t('dataset.error'), value: 'Error' }], multiple: false }
        ]
      },
      paginationConfig: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      },
      data: [],
      dialogVisible: false,
      editPasswordVisible: false,
      form: {
        roles: [{
          id: ''
        }]
      },
      checkPasswordForm: {},
      ruleForm: {},
      defaultForm: { id: null, username: null, nickName: null, gender: '男', email: null, enabled: 1, deptId: null, phone: null },
      depts: null,
      roles: [],
      roleDatas: [],
      userRoles: [],
      formType: 'add',
      orderConditions: [],
      last_condition: null,
      show_error_massage: false,
      error_massage: '',
      matchLogId: null,
      lastRequestComplete: true
    }
  },
  computed: {
  },
  created() {
    if (this.param !== null && this.param.taskId) {
      this.matchLogId = this.param.logId || this.matchLogId
      this.transCondition['dataset_table_task.id'] = {
            operator: 'eq',
            value: this.param.taskId
        }
    }
    this.createTimer()
  },
  beforeDestroy() {
    this.destroyTimer()
  },
  methods: {
    createTimer() {
      if (!this.timer) {
        this.timer = setInterval(() => {
          this.timerSearch(this.last_condition, false)
        }, 15000)
      }
    },
    destroyTimer() {
      if (this.timer) {
        clearInterval(this.timer)
        this.timer = null
      }
    },
    sortChange({ column, prop, order }) {
      this.orderConditions = []
      if (!order) {
        this.search(this.last_condition)
        return
      }
      if (prop === 'dept') {
        prop = 'u.deptId'
      }
      if (prop === 'status') {
        prop = 'u.enabled'
      }
      this.orderConditions = []
      addOrder({ field: prop, value: order }, this.orderConditions)
      this.search(this.last_condition)
    },
    select(selection) {
    },
    timerSearch(condition, showLoading = true) {
      if(!this.lastRequestComplete){
        return;
      }else {
        this.lastRequestComplete = false;
      }

      this.last_condition = condition
      condition = formatQuickCondition(condition, 'dataset_table_task.name')
      const temp = formatCondition(condition)
      const param = temp || {}
      param['orders'] = formatOrders(this.orderConditions)
      post('/dataset/taskLog/list/notexcel/' + this.paginationConfig.currentPage + '/' + this.paginationConfig.pageSize, param, showLoading).then(response => {
        this.data = response.data.listObject
        this.paginationConfig.total = response.data.itemCount
        this.lastRequestComplete = true;
      }).catch(() => {
        this.lastRequestComplete = true;
      })
    },
    search(condition, showLoading = true) {
      this.last_condition = condition
      condition = formatQuickCondition(condition, 'dataset_table_task.name')
      const temp = formatCondition(condition)
      const param = temp || {}
      param['orders'] = formatOrders(this.orderConditions)
      post('/dataset/taskLog/list/notexcel/' + this.paginationConfig.currentPage + '/' + this.paginationConfig.pageSize, param, showLoading).then(response => {
        this.data = response.data.listObject
        this.paginationConfig.total = response.data.itemCount
      })
    },
    showErrorMassage(massage) {
      this.show_error_massage = true
      this.error_massage = massage
    },
    jumpTask(item) {
      this.$emit('jumpTask', item)
    },
    rowClassMethod({ row, rowIndex }) {
      if (this.matchLogId && this.matchLogId === row.id) {
        return 'row-match-class'
      }
      return ''
    }
  }
}
</script>

<style scoped>
.el-divider--horizontal {
  margin: 12px 0;
}

.el-radio{
  margin-right: 10px;
}
.el-radio>>>.el-radio__label{
  font-size: 12px;
}

.dialog-css >>> .el-dialog__header {
  padding: 20px 20px 0;
}

.dialog-css >>> .el-dialog__body {
  padding: 10px 20px 20px;
}

.el-form-item {
  margin-bottom: 10px;
}

.err-msg{
  font-size: 12px;
  word-break:normal;
  width:auto;
  display:block;
  white-space:pre-wrap;
  word-wrap : break-word ;
  overflow: hidden ;
}

span{
  font-size: 12px;
}
</style>
